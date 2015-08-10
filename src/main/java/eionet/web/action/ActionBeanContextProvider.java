package eionet.web.action;

import eionet.meta.application.AppContextProvider;

/**
 * An adapter type that implements the {@link AppContextProvider} protocol.
 * Using this implementation with IoC pattern, we ensure that controllers
 * and services remain agnostic to the view layer and its specifics, such as
 * HTTP servlet requests etc.
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
