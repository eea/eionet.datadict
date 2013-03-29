package eionet.util;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class IrrelevantAttributes extends Hashtable {

    /** */
    private static IrrelevantAttributes instance;

    /**
     *
     */
    private IrrelevantAttributes() {
        super();
        load();
    }

    /**
     *
     *
     */
    private void load() {

        Enumeration propNames = Props.getPropertyNames();
        while (propNames != null && propNames.hasMoreElements()) {
            String propName = (String)propNames.nextElement();
            if (propName.trim().startsWith(PropsIF.IRRELEVANT_ATTRS_PREFIX)) {
                int i = propName.indexOf(PropsIF.IRRELEVANT_ATTRS_PREFIX) + PropsIF.IRRELEVANT_ATTRS_PREFIX.length();
                if (i < propName.length()) {
                    String dataType = propName.substring(i).trim().toLowerCase();
                    if (dataType.length() > 0) {
                        String skipAttrs = Props.getProperty(propName);
                        if (skipAttrs != null && skipAttrs.length() > 0) {
                            StringTokenizer st = new StringTokenizer(skipAttrs, ",");
                            while (st.hasMoreTokens()) {
                                String attrName = st.nextToken().trim().toLowerCase();
                                if (attrName.length() > 0)
                                    addMapping(dataType, attrName);;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param dataType
     * @param attrName
     */
    private void addMapping(String dataType, String attrName) {
        HashSet skipAttrs = (HashSet)this.get(dataType);
        if (skipAttrs == null)
            skipAttrs = new HashSet();
        skipAttrs.add(attrName);
        this.put(dataType, skipAttrs);
    }

    /**
     *
     * @return
     */
    public static synchronized IrrelevantAttributes getInstance() {
        if (instance == null)
            instance = new IrrelevantAttributes();
        return instance;
    }

    /**
     *
     * @param dataType
     * @param attrName
     * @return
     */
    public boolean isIrrelevant(String dataType, String attrName) {

        HashSet hashSet = (HashSet)this.get(new String(dataType.toLowerCase()));
        return hashSet != null && hashSet.contains(new String(attrName.toLowerCase()));
    }
}
