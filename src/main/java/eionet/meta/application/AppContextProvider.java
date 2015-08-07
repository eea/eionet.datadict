package eionet.meta.application;

/**
 * Defines the protocol for objects that can provide controllers and services 
 * with context info.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface AppContextProvider {

    /**
     * Answers whether the current user is logged in.
     * 
     * @return true if the user is logged in; false otherwise;
     */
    boolean isUserAuthenticated();
    
    /**
     * Gets the current user's login name.
     * 
     * @return the user's login name if the user is logged in; null otherwise;
     */
    String getUserName();
    
}
