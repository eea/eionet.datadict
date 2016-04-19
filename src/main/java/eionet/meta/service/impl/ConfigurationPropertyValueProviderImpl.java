package eionet.meta.service.impl;

import eionet.meta.service.ConfigurationPropertyValueProvider;
import eionet.util.Props;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class ConfigurationPropertyValueProviderImpl implements ConfigurationPropertyValueProvider {

    @Override
    public String getPropertyValue(String propertyName) {
        return Props.getProperty(propertyName);
    }

    @Override
    public int getPropertyIntValue(String propertyName) {
        return Props.getIntProperty(propertyName);
    }
    
}
