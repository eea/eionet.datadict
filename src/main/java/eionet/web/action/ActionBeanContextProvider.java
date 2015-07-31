package eionet.web.action;

import eionet.meta.controllers.ControllerContextProvider;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class ActionBeanContextProvider implements ControllerContextProvider {

    private final AbstractActionBean actionBean;
    
    public ActionBeanContextProvider(AbstractActionBean actionBean) {
        this.actionBean = actionBean;
    }
    
    @Override
    public boolean isUserAuthenticated() {
        return this.actionBean.isUserLoggedIn();
    }

    @Override
    public String getUserName() {
        return this.actionBean.getUserName();
    }
    
}
