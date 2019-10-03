package eionet.util;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Singleton class providing information on whether certain attributes are prohibited for elements whose type or datatype is of
 * certain value. This information is obtained from configuration.
 *
 * @author Jaanus
 */
public class IrrelevantAttributes extends Hashtable<String, Set<String>> {

    /** Auto-generated serial ID. */
    private static final long serialVersionUID = 910371094928805649L;

    /** Singleton instance. */
    private static IrrelevantAttributes instance;

    /**
     * Private constructor.
     */
    private IrrelevantAttributes() {
        super();
        load();
    }

    /**
     * Load irrelevance mappings from configuration.
     */
    private void load() {

        @SuppressWarnings("rawtypes")
        Set<String> propNames = Props.getPropertyNames();
        for (String propName : propNames) {
            if (propName.trim().startsWith(PropsIF.IRRELEVANT_ATTRS_PREFIX)) {
                int i = propName.indexOf(PropsIF.IRRELEVANT_ATTRS_PREFIX) + PropsIF.IRRELEVANT_ATTRS_PREFIX.length();
                if (i < propName.length()) {
                    String dataType = propName.substring(i).trim();
                    if (dataType.length() > 0) {
                        String skipAttrs = Props.getProperty(propName);
                        if (skipAttrs != null && skipAttrs.length() > 0) {
                            StringTokenizer st = new StringTokenizer(skipAttrs, ",");
                            while (st.hasMoreTokens()) {
                                String attrName = st.nextToken().trim();
                                if (attrName.length() > 0) {
                                    addMapping(dataType, attrName);
                                }
                                ;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add irrelevance mapping.
     *
     * @param type Data or element type.
     * @param attrName Attribute short name.
     */
    private void addMapping(String type, String attrName) {

        Set<String> attrs = this.get(type);
        if (attrs == null) {
            attrs = new HashSet<String>();
        }
        attrs.add(attrName);
        this.put(type, attrs);
    }

    /**
     * Get this singleton's instance.
     *
     * @return the instance
     */
    public static synchronized IrrelevantAttributes getInstance() {
        if (instance == null) {
            instance = new IrrelevantAttributes();
        }
        return instance;
    }

    /**
     * Check if attribute by given short name is irrelevant for given data or element type.
     *
     * @param type Data or element type.
     * @param attrName Attribute short name.
     * @return
     */
    public boolean isIrrelevant(String type, String attrName) {

        Set<String> set = this.get(type);
        return set != null && set.contains(attrName);
    }
}
