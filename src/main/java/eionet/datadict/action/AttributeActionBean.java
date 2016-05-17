package eionet.datadict.action;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.action.attribute.AttributeViewModelBuilder;
import eionet.datadict.controllers.AttributeController;
import eionet.meta.DDUser;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.util.CompoundDataObject;
import eionet.util.SecurityUtil;
import eionet.web.action.AbstractActionBean;
import eionet.web.action.ErrorActionBean;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

/**
 *
 * @author Aliki Kopaneli
 */
@UrlBinding("/attribute/{$event}/{attrId}")
public class AttributeActionBean extends AbstractActionBean {

    private static final String VIEW_PAGE = "/pages/attributes/view_attribute.jsp";
    private static final String EDIT_PAGE = "/pages/attributes/edit_attribute.jsp";

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
        }
        return new ForwardResolution(EDIT_PAGE);

    }

    public Resolution save() throws Exception {
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

}
