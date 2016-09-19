/*
 * Created on Sep 30, 2003
 */
package eionet.util;

import eionet.meta.DDRuntimeException;
import eionet.meta.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationDefinitionProvider;
import eionet.propertyplaceholderresolver.ConfigurationDefinitionProviderImpl;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolverImpl;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import eionet.propertyplaceholderresolver.util.ConfigurationLoadException;
import java.util.HashSet;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class for retrieving the configured properties of this application.
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class Props implements PropsIF {

    public static ConfigurationPropertyResolver configurationPropertyResolver;
    /**
     * Static logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Props.class);
    
    /**
     * A hash-table of default values for the properties. To be initialized at first required instance.
     */
    @SuppressWarnings("rawtypes")
    private Hashtable defaults = null;

    /**
     * The instance of this class.
     */
    private static volatile Props instance = null;

    /**
     * Default constructor.
     */
    @SuppressWarnings("rawtypes")
    protected Props() {
            configurationPropertyResolver = SpringApplicationContext.getBean(ConfigurationPropertyResolverImpl.class);
            defaults = new Hashtable();
            setDefaults(defaults);
            String workingLanguage;
            workingLanguage = StringUtils.trimToNull(getPropertyInternal(DD_WORKING_LANGUAGE_KEY));
            boolean isValidWorkingLanguage = false;
            try {
                Locale locale = LocaleUtils.toLocale(workingLanguage);
                if (locale != null) {
                    isValidWorkingLanguage = StringUtils.isNotBlank(locale.getLanguage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isValidWorkingLanguage) {
                LOGGER.warn("Working language is not valid: '" + workingLanguage + "'");
            }

    }

    /**
     * Returns the instance.
     *
     * @return The instance
     */
    private static Props getInstance() {
        if (Props.instance == null) {
            Props.instance = new Props();
        }

        return Props.instance;
    }

    /**
     * Returns enumeration of all detected property names.
     *
     * @return The enumeration.
     */
    public static synchronized Set<String> getPropertyNames() {
        Set<String> propertyNames = new HashSet();
        try {
            ConfigurationDefinitionProvider configurationDefinitionProvider = 
            SpringApplicationContext.getContext().getBean(ConfigurationDefinitionProviderImpl.class);
            propertyNames.addAll(configurationDefinitionProvider.getConfigurationDefinition().keySet());
        } catch (ConfigurationLoadException ex) {
            LOGGER.warn("Error detected while loading the configuration.");
        } finally {
            propertyNames.addAll(System.getProperties().stringPropertyNames());
        }
        return propertyNames;
    }
    /**
     * Returns the value of the given property. If no property is found, the default is returned if one exists. If no default is
     * found either, returns null.
     *
     * @param name Given property name.
     * @return The value.
     */
    public static synchronized String getProperty(String name) {
       return Props.getInstance().getPropertyInternal(name);
    }

    /**
     * Returns the value of the given property as integer. If no property is found, the default is returned if one exists. If no
     * default is found either, returns 0.
     *
     * @param name Given property name.
     * @return The value.
     */
    public static synchronized int getIntProperty(String name) {

        Props propsInstance = Props.getInstance();
        String stringValue;
        stringValue = propsInstance.getPropertyInternal(name);
        // If string value is null, then it means it wasn't configured and no default was found either.
        try {
            return Integer.parseInt(stringValue);
        } catch (Exception e) {
            // Problem with parsing into integer, so resort to the default, and return 0 if that cannot be parsed either.
            return NumberUtils.toInt((String) propsInstance.getDefaults().get(name));
        }
    }

    /**
     * Returns the value of the given property. If no value is found, an no default either, then throws {@link DDRuntimeException}.
     *
     * @param name Given property name.
     * @return The value.
     */
    public static String getRequiredProperty(String name) {
        String value = Props.getInstance().getPropertyInternal(name);
        if (value == null || value.trim().length() == 0) {
            throw new DDRuntimeException("Failed to find required property: " + name);
        }
        return value;
    }

    /**
     * Internal worker method for the public property getter methods in this class.
     *
     * @param name Given property name.
     * @return The value.
     */
    protected final String getPropertyInternal(String name)  {
        String value;
        try {
            value = configurationPropertyResolver.resolveValue(name);
            return value;
        } catch (UnresolvedPropertyException ex) {
            return (String) defaults.get(name);
        } catch (CircularReferenceException ex) {
            return (String) defaults.get(name);
        }
    }

    /**
     * Get property value of time in milliseconds presented by time value and unit suffix (1h, 30m, 15s etc).
     *
     * @param key          property name
     * @param defaultValue default value to be returned if file does not contain the property
     * @return propertyValue
     */
    public static synchronized Long getTimePropertyMilliseconds(final String key, Long defaultValue) {
        Long longValue = defaultValue;
        String value;
        value = Props.getInstance().getPropertyInternal(key);
        
        value = StringUtils.trimToEmpty(value);
        if (StringUtils.isNotEmpty(value)) {
            int coefficient = 1;
            value = value.replace(" ", "").toLowerCase();

            if (value.length() > 1 && value.endsWith("ms") && value.replace("ms", "").length() == value.length() - 2) {
                coefficient = 1;
                value = value.replace("ms", "");
            }

            if (value.length() > 1 && value.endsWith("s") && value.replace("s", "").length() == value.length() - 1) {
                coefficient = 1000;
                value = value.replace("s", "");
            }

            if (value.length() > 1 && value.endsWith("m") && value.replace("m", "").length() == value.length() - 1) {
                coefficient = 1000 * 60;
                value = value.replace("m", "");
            }

            if (value.length() > 1 && value.endsWith("h") && value.replace("h", "").length() == value.length() - 1) {
                coefficient = 1000 * 60 * 60;
                value = value.replace("h", "");
            }

            try {
                longValue = Long.parseLong(value) * coefficient;
            } catch (Exception e) {
                // Ignore exceptions resulting from string-to-integer conversion here.
                e.printStackTrace();
            }
        }
        return longValue;
    } // end of function getTimePropertyMilliseconds

    /**
     * Sets the default properties.
     *
     * @param defaults The hash-table of defaults to populate.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setDefaults(Hashtable defaults) {
        defaults.put(XFORM_TEMPLATE_URL, "http://cdr-ewn.eionet.europa.eu/webq/GetXFormTemplate");
        defaults.put(INSERV_ROD_RA_URLPATTERN, "http://rod.eionet.europa.eu/obligations/<RA_ID>");
        defaults.put(XLS_SCHEMA_URL_SHEET, "DO_NOT_DELETE_THIS_SHEET");
        defaults.put(XLS_DROPDOWN_FXV_SHEET, "REFS_FOR_DD_ITEMS_DO_NOT_DEL");
        defaults.put(DD_RDF_SCHEMA_URL, "http://dd.eionet.europa.eu/schema.rdf#");
        defaults.put(PREDICATE_RDF_TYPE, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        defaults.put(PREDICATE_RDF_LABEL, "http://www.w3.org/2000/01/rdf-schema#label");
        defaults.put(OUTSERV_PRED_TITLE, "http://purl.org/dc/elements/1.1/title");

        defaults.put(SITE_CODES_MAX_ALLOCATE, "500");
        defaults.put(SITE_CODES_MAX_ALLOCATE_WITHOUT_NAMES, "100");
        defaults.put(SITE_CODES_MAX_ALLOCATE_ETC_EEA, "1000");
        defaults.put(SITE_CODES_MAX_RESERVE_AMOUNT, "10000");
        defaults.put(DD_WORKING_LANGUAGE_KEY, DD_DEFAULT_WORKING_LANGUAGE_VALUE);
        defaults.put(DD_RECENTLY_RELEASED_VOCABULARIES_KEY, DD_DEFAULT_RECENTLY_RELEASED_VOCABULARIES_VALUE);
        defaults.put(DD_RECENTLY_RELEASED_SCHEMAS_KEY, DD_DEFAULT_RECENTLY_RELEASED_SCHEMAS_VALUE);
        defaults.put(DD_RECENTLY_RELEASED_DATASETS_KEY, DD_DEFAULT_RECENTLY_RELEASED_DATASETS_VALUE);
        defaults.put(DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES, DD_VOCABULARY_API_JWT_TIMEOUT_DEFAULT_VALUE_IN_MINUTES);
        defaults.put(DD_VOCABULARY_ADI_JWT_ALGORITHM, DD_VOCABULARY_ADI_JWT_ALGORITHM_DEFAULT_VALUE);
    }
    
    /**
     * Getter for the defaults.
     *
     * @return The defaults.
     */
    @SuppressWarnings("rawtypes")
    protected Hashtable getDefaults() {
        return defaults;
    }

    /**
     * Returns true if the application is configured to use Central Authentication Service (CAS).
     * See {@link PropsIF#USE_CENTRAL_AUTHENTICATION_SERVICE} for further description.
     *
     * @return true/false as indicated.
     */
    public static synchronized boolean isUseCentralAuthenticationService() {

        String useCentralAuthenticationService = getProperty(USE_CENTRAL_AUTHENTICATION_SERVICE);
        return StringUtils.isBlank(useCentralAuthenticationService) || !useCentralAuthenticationService.equals("false");
    }  
    
}
