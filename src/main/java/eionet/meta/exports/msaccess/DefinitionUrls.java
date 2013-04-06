package eionet.meta.exports.msaccess;

import java.util.HashMap;

import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class DefinitionUrls extends HashMap<Class,String>{

    /** */
    private static DefinitionUrls prefixes;

    /** */
    private static Object lock = new Object();

    /**
     *
     */
    private DefinitionUrls() {

        super();

        String urlPrefix = Props.getRequiredProperty(PropsIF.JSP_URL_PREFIX).trim();
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix + "/";
        }

        put(Dataset.class, urlPrefix + "/datasets/");
        put(DsTable.class, urlPrefix + "/tables/");
        put(DataElement.class, urlPrefix + "/dataelements/");
    }

    /**
     *
     * @return
     */
    private static DefinitionUrls getPrefixes() {

        if (prefixes == null) {

            synchronized (lock) {

                // double-checked locking pattern
                // (http://www.ibm.com/developerworks/java/library/j-dcl.html)
                if (prefixes == null) {
                    prefixes = new DefinitionUrls();
                }
            }
        }

        return prefixes;
    }

    /**
     *
     * @param dst
     * @return
     */
    public static String get(Dataset dst) {

        String prefix = getPrefixes().get(dst.getClass());
        return prefix == null ? null : prefix + dst.getID();
    }

    /**
     *
     * @param tbl
     * @return
     */
    public static String get(DsTable tbl) {

        String prefix = getPrefixes().get(tbl.getClass());
        return prefix == null ? null : prefix + tbl.getID();
    }

    /**
     *
     * @param elm
     * @return
     */
    public static String get(DataElement elm) {

        String prefix = getPrefixes().get(elm.getClass());
        return prefix == null ? null : prefix + elm.getID();
    }

}
