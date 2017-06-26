package eionet.datadict.commons.util;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public final class XMLUtils {

    /**
     * @param s The XML Element to check for any Illegal Characters and replace
     * them
     * @return An XML Element clear of illegal characters
     *
     * This method checks for Illegal characters based on the XML 1.0 version
     *
     */
    public static String replaceAllIlegalXMLCharacters(String s) {
        String xml10pattern = "[^"
                + "\u0009\r\n"
                + "\u0020-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]";
        return s.replaceAll(xml10pattern, "").replaceAll("\\s+", "");
    }
}
