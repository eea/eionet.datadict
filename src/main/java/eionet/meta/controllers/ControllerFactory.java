package eionet.meta.controllers;

import eionet.meta.service.IDataService;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ControllerFactory {

    ElementFixedValuesController createElementFixedValuesController(ControllerContextProvider contextProvider, IDataService dataService);
    
    AttributeFixedValuesController createAttributeFixedValuesController(ControllerContextProvider contextProvider, IDataService dataService);
    
}
