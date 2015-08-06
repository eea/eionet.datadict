package eionet.meta.application;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AppContextProvider {

    boolean isUserAuthenticated();
    
    String getUserName();
    
}
