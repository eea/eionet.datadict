package eionet.web.action;

import eionet.meta.application.AppContextProvider;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class ActionBeanContextProvider implements AppContextProvider {

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
