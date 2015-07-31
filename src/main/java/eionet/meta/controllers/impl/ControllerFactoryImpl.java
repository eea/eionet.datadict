package eionet.meta.controllers.impl;

import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.controllers.ControllerContextProvider;
import eionet.meta.controllers.ControllerFactory;
import eionet.meta.controllers.ElementFixedValuesController;
import eionet.meta.service.IDataService;
import org.springframework.stereotype.Component;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Component
public final class ControllerFactoryImpl implements ControllerFactory {

    @Override
    public ElementFixedValuesController createElementFixedValuesController(ControllerContextProvider contextProvider, IDataService dataService) {
        return new ElementFixedValuesControllerImpl(contextProvider, dataService);
    }

    @Override
    public AttributeFixedValuesController createAttributeFixedValuesController(ControllerContextProvider contextProvider, IDataService dataService) {
        return new AttributeFixedValuesControllerImpl(contextProvider, dataService);
    }
    
}
