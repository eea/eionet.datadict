package eionet.meta.controllers.impl;

import eionet.meta.controllers.ControllerContextProvider;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public abstract class AbstractController {
    
    private final ControllerContextProvider contextProvider;
    
    public AbstractController(ControllerContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }
    
    protected final ControllerContextProvider getContextProvider() {
        return this.contextProvider;
    }
    
}
