package eionet.datadict.action;

import eionet.datadict.action.attribute.AttributeViewModel;
import eionet.datadict.action.attribute.AttributeViewModelBuilder;
import eionet.datadict.controllers.AttributeController;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.enums.Enumerations.DisplayForType;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.dao.domain.FixedValue;
import eionet.util.CompoundDataObject;
import eionet.util.SecurityUtil;
import eionet.web.action.AbstractActionBean;
import eionet.web.action.ErrorActionBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

/**
 *
 * @author Aliki Kopaneli
 */
@UrlBinding("/attribute/{$event}/{attrId}")
public class AttributeActionBean extends AbstractActionBean {

    private static final String ATTRIBUTE_PAGE = "/pages/attributes/attribute.jsp";

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
        user = SecurityUtil.getUser(this.getContext().getRequest());
        String username = user == null ? "" : user.getUserName();
        try {
            if (!SecurityUtil.hasPerm(username, "/attributes/", "v")) {
                throw new UserAuthorizationException();
            }
        } catch (Exception authorizationException) {
            return createNotAuthorizedResolution();
        }
        CompoundDataObject model = attributeControllerImpl.getAttributeViewInfo(attrId);
        viewModel = attributeViewModelBuilder.buildForView(model);

        return new ForwardResolution(ATTRIBUTE_PAGE);
    }

    private Resolution createNotAuthorizedResolution() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("message", "You are not authorized to access this page");
        params.put("type", ErrorActionBean.ErrorType.FORBIDDEN_403);
        return new ForwardResolution(ErrorActionBean.class).addParameters(params);
    }

}
