package eionet.meta.service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface ConfigurationPropertyValueProvider {

    String getPropertyValue(String propertyName);
    
    int getPropertyIntValue(String propertyName);
    
}
