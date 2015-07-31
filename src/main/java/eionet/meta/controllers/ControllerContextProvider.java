package eionet.meta.controllers;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ControllerContextProvider {

    boolean isUserAuthenticated();
    
    String getUserName();
    
}
