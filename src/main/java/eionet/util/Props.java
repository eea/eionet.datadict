/*
 * Created on Sep 30, 2003
 */
package eionet.util;

import eionet.meta.DDRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for retrieving the configured properties of this application.
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Props implements PropsIF {

    /** Static logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(Props.class);

    /** The resource bundle where to look for the configured properties. */
    private ResourceBundle bundle = null;

    /** A hash-table of default values for the properties. To be initialized at first required instance. */
    @SuppressWarnings("rawtypes")
    private Hashtable defaults = null;

    /** The instance of this class. */
    private static volatile Props instance = null;

    /**
     * Default constructor.
     */
    @SuppressWarnings("rawtypes")
    protected Props() {

        defaults = new Hashtable();
        setDefaults(defaults);

        try {
            bundle = ResourceBundle.getBundle(getBundleName());
        } catch (MissingResourceException mre) {
            LOGGER.warn("Properties file " + getBundleName() + ".properties not found. Using defaults.");
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
    @SuppressWarnings("rawtypes")
    public static synchronized Enumeration getPropertyNames() {
        return Props.getInstance().getBundle().getKeys();
    }

    /**
     * Returns the value of the given property. If no property is found, the default is returned if one exists.
     * If no default is found either, returns null.
     *
     * @param name Given property name.
     * @return The value.
     */
    public static synchronized String getProperty(String name) {
        return Props.getInstance().getPropertyInternal(name);
    }

    /**
     * Returns the value of the given property as integer. If no property is found, the default is returned if one exists.
     * If no default is found either, returns 0.
     *
     * @param name Given property name.
     * @return The value.
     */
    public static synchronized int getIntProperty(String name) {

        Props propsInstance = Props.getInstance();
        String stringValue = propsInstance.getPropertyInternal(name);
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
    protected final String getPropertyInternal(String name) {

        String value = null;
        if (bundle != null) {
            try {
                value = bundle.getString(name);
            } catch (MissingResourceException mre) {
                // Missing property is not considered a problem.
                // If a property is mandatory, use getRequiredProperty() that throws proper exception.
                mre.printStackTrace();
            }
        }

        if (value == null) {
            value = (String) defaults.get(name);
        } else {
            value = value.trim();

            // If the value looks like a property placeholder, then use default.
            if (value.startsWith("${") && value.endsWith("}")) {
                if (defaults.containsKey(name)) {
                    value = (String) defaults.get(name);
                }
            }
        }

        return value;
    }
	
    /**
     * Get property value of time in milliseconds presented by time value and unit suffix (1h, 30m, 15s etc).
     *
     * @param key
     *            property name
     * @param defaultValue
     *            default value to be returned if file does not contain the property
     * @return propertyValue
     */
    public static synchronized Long getTimePropertyMilliseconds(final String key, Long defaultValue) {
        Long longValue = defaultValue;
        String value = Props.getInstance().getPropertyInternal(key);
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
    @SuppressWarnings( {"rawtypes", "unchecked"} )
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
    }

    /**
     * Returns the name of the resource bundle where the properties are looked for.
     *
     * @return The resource bundle name.
     */
    protected String getBundleName() {
        return PROP_FILE;
    }

    /**
     * Returns the resource bundle where the properties are looked for.
     *
     * @return The bundle.
     */
    protected ResourceBundle getBundle() {
        return bundle;
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
}
