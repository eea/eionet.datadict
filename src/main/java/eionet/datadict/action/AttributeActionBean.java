package eionet.datadict.action;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.action.attribute.AttributeViewModelBuilder;
import eionet.datadict.controllers.AttributeController;
import eionet.meta.DDUser;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.service.ValidationException;
import eionet.util.CompoundDataObject;
import eionet.util.SecurityUtil;
import eionet.web.action.AbstractActionBean;
import eionet.web.action.ErrorActionBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 *
 * @author Aliki Kopaneli
 */
@UrlBinding("/attribute/{$event}/{attrId}")
public class AttributeActionBean extends AbstractActionBean implements ValidationErrorHandler {

    private static final String VIEW_PAGE = "/pages/attributes/view_attribute.jsp";
    private static final String EDIT_PAGE = "/pages/attributes/edit_attribute.jsp";
    private static final String ADD_PAGE = "/pages/attributes/add_attribute.jsp";

    private String attrId;

    @SpringBean
    AttributeController attributeControllerImpl;

    @SpringBean
    AttributeViewModelBuilder attributeViewModelBuilder;

    private AttributeViewModel viewModel;
    private DDUser user;

    public AttributeViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(AttributeViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public DDUser getUser() {
        return user;
    }

    public void setUser(DDUser user) {
        this.user = user;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    @DefaultHandler
    public Resolution view() throws Exception {
        try {
            authorizationInitializations("v");
            CompoundDataObject model = attributeControllerImpl.getAttributeViewInfo(attrId);
            viewModel = attributeViewModelBuilder.buildForView(model);
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        } catch (ResourceNotFoundException e) {
            return createAttributeNotFoundResolution(attrId);
        }
        return new ForwardResolution(VIEW_PAGE);
    }

    public Resolution edit() throws Exception {
        try {
            authorizationInitializations("u");
            CompoundDataObject model = attributeControllerImpl.getAttributeEditInfo(attrId);
            viewModel = attributeViewModelBuilder.buildForEdit(model);
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        } catch (ResourceNotFoundException e) {
            return createAttributeNotFoundResolution(attrId);
        }
        return new ForwardResolution(EDIT_PAGE);

    }

    public Resolution add() throws Exception {
        try {
            authorizationInitializations("i");
            CompoundDataObject model = attributeControllerImpl.getAttributeAddInfo();
            viewModel = attributeViewModelBuilder.buildForAdd(model);
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        }
        return new ForwardResolution(ADD_PAGE);
    }

    public Resolution saveAdd() throws Exception {
        try {
            authorizationInitializations("i");
            attrId = String.valueOf(attributeControllerImpl.saveNewAttribute(viewModel));
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        } catch (ValidationException e) {
            return createAttributeNotFoundResolution(attrId);
        }
        return new RedirectResolution("/attribute/view/" + attrId);
    }

    public Resolution saveEdit() throws Exception {
        try {
            authorizationInitializations("u");
            attributeControllerImpl.saveAttribute(viewModel);
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        }
        return new RedirectResolution("/attribute/view/" + attrId);
    }

    public Resolution delete() throws Exception {
        try {
            authorizationInitializations("d");
            attributeControllerImpl.deleteAttribute(attrId);
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        }
        return new ForwardResolution("/attributes.jsp");
    }

    public Resolution selectVocabulary() throws Exception {
        try {
            authorizationInitializations("u");
            if (viewModel != null) {
                attributeControllerImpl.saveNewVocabulary(attrId, viewModel.getVocabularyId());
            } else {
                attributeControllerImpl.saveNewVocabulary(attrId, null);
            }
        } catch (UserAuthorizationException e) {
            return createNotAuthorizedResolution();
        }
        return new RedirectResolution("/attribute/edit/" + attrId);
    }

    private Resolution createAttributeNotFoundResolution(String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("message", "Attribute with id " + id + " does not exist");
        params.put("type", ErrorActionBean.ErrorType.NOT_FOUND_404);
        return new ForwardResolution(ErrorActionBean.class).addParameters(params);
    }

    private Resolution createNotAuthorizedResolution() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("message", "You are not authorized to access this page");
        params.put("type", ErrorActionBean.ErrorType.FORBIDDEN_403);
        return new ForwardResolution(ErrorActionBean.class).addParameters(params);
    }

    private void authorizationInitializations(String perm) throws UserAuthorizationException, Exception {
        user = SecurityUtil.getUser(this.getContext().getRequest());
        String username = user == null ? "anonymous" : user.getUserName();
        if (!SecurityUtil.hasPerm(username, "/attributes/", perm)) {
            throw new UserAuthorizationException();
        }
    }

    @ValidationMethod(on = "saveAdd")
    public void validateMandatoryFieldsAdd(ValidationErrors errors) {
        String errorLabel = "saveAdd";
        if (viewModel.getAttributeDefinition().getName()==null) {
            errors.add(errorLabel, new LocalizableError("attr.name"));
        }  
        if (viewModel.getAttributeDefinition().getShortName()==null) {
            errors.add(errorLabel, new LocalizableError("attr.shortName"));
        }
    }
    
    @ValidationMethod(on = "saveEdit")
    public void validateMandatoryFieldsEdit(ValidationErrors errors) {
        String errorLabel = "saveEdit";
        if (viewModel.getAttributeDefinition().getName()==null) {
            errors.add(errorLabel, new LocalizableError("attr.name"));
        }
    }
            
    @Override
    public Resolution handleValidationErrors(ValidationErrors ve) throws Exception {
            List<ValidationError> arrayL = ve.get("saveAdd");
            if (arrayL!=null) {
                CompoundDataObject model = attributeControllerImpl.getAttributeAddInfo();
                viewModel = attributeViewModelBuilder.buildForAdd(model);
            }
            arrayL = ve.get("saveEdit");
            if (arrayL!=null) {
                CompoundDataObject model = attributeControllerImpl.getAttributeEditInfo(attrId);
                viewModel = attributeViewModelBuilder.buildForEdit(model);
        }
        return null;
    }

}
