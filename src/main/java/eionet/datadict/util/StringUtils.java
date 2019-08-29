package eionet.datadict.util;

public class StringUtils {
    /**
     * Removes whiteSpaces from XML Documents ,between opening/closing tags.
     * **/
    public static String trimWhiteSpacesFromStringifiedXml(String xml){
        return xml.replaceAll(">\\s+<", "><");
    }
}
