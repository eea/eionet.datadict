/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.web.util;

import eionet.util.Props;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import eionet.util.SecurityUtil;
import eionet.util.Util;

/**
 * JSTL functions to be used in JSP.
 *
 * @author Risto Alt
 *
 */
public final class JstlFunctions {

    /** */
    private static final String INPUT_CHECKED_STRING = "checked=\"checked\"";
    private static final String INPUT_SELECTED_STRING = "selected=\"selected\"";
    private static final String INPUT_DISABLED_STRING = "disabled=\"disabled\"";

    /**
     * Prevent initialization.
     */
    private JstlFunctions() {

    }

    /**
     * Returns the value of {@link CRUser#hasPermission(HttpSession, String, String)}, using the given inputs.
     *
     * @param session
     * @param aclPath
     * @param permission
     * @return
     */
    public static boolean userHasPermission(java.lang.String usr, java.lang.String aclPath, java.lang.String prm) throws Exception {
        return SecurityUtil.hasPerm(usr, aclPath, prm);
    }

    /**
     *
     * @param o
     * @param seperator
     * @return
     */
    public static String join(Object o, String separator) {

        if (o == null) {
            return "";
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Object[]) {
            return StringUtils.join((Object[]) o, separator);
        } else if (o instanceof Collection) {
            return StringUtils.join((Collection) o, separator);
        } else {
            throw new ClassCastException("Couldn't cast from this class: " + o.getClass().getName());
        }
    }

    /**
     * Returns true if provided array or collection contains the specified element.
     *
     * @param arrayOrCollection array or collection holding list of elements
     * @param object element whose presence in this array is to be tested
     * @return true if this array contains the specified element
     */
    public static boolean contains(Object arrayOrCollection, Object object) {

        if (arrayOrCollection != null) {

            if (arrayOrCollection instanceof Object[]) {
                for (Object o : ((Object[]) arrayOrCollection)) {
                    if (o.equals(object)) {
                        return true;
                    }
                }
            } else if (arrayOrCollection instanceof Collection) {
                for (Object o : ((Collection<?>) arrayOrCollection)) {
                    if (o.equals(object)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     *
     * @param condition
     * @return
     */
    public static String inputCheckedString(boolean condition) {
        if (condition == true) {
            return INPUT_CHECKED_STRING;
        } else {
            return "";
        }
    }

    /**
     *
     * @param condition
     * @return
     */
    public static String inputSelectedString(boolean condition) {
        if (condition == true) {
            return INPUT_SELECTED_STRING;
        } else {
            return "";
        }
    }

    /**
     *
     * @param condition
     * @return
     */
    public static String inputDisabledString(boolean condition) {
        if (condition == true) {
            return INPUT_DISABLED_STRING;
        } else {
            return "";
        }
    }

    /**
     * Returns the text where URLs are replaced by HTML links.
     *
     * @param text
     * @return
     */
    public static String linkify(String text) {
        return Util.processForLink(text, false, text.length());
    }

    /**
     * Cuts the given String if exceeds the length. Finds the first whitespace after the position to be cut. Adds an ellipsis in the
     * end if the length is exceeded.
     *
     * @param str given string value
     * @param cutAtLength max len of string
     *
     * @return cut string
     */
    public static java.lang.String cutAtSpace(java.lang.String str, int cutAtLength) {

        char c = (char) Integer.parseInt(String.valueOf(2026), 16);
        final String ellipsis = String.valueOf(c);

        if (str == null) {
            return "";
        }

        if (str.length() <= cutAtLength) {
            return str;
        }

        String stringStart = str.substring(0, cutAtLength);
        // if the Nth char is space ther is not need to split the string
        if (str.charAt(cutAtLength) == ' ') {
            stringStart = stringStart + " ";
        }
        int prevSpace = StringUtils.lastIndexOf(stringStart, " ");

        str = prevSpace == -1 ? stringStart : str.substring(0, prevSpace + 1);

        return str + ellipsis;

    }

    /**
     * URLEncodes given String.
     *
     * @param str value to urlencode
     * @return encoded value
     */
    public static java.lang.String urlEncode(String str) {
        try {
            URI uri = new URI(null, str, null);
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            return str;
        }
    }

    /**
     * Returns true if provided array contains the specified element.
     *
     * @param array array holding list of elements
     * @param o element whose presence in this array is to be tested
     * @return true if this array contains the specified element
     */
    public static boolean arrayContains(Object[] array, Object o) {
        return Arrays.asList(array).contains(o);
    }
    
    /**
     * Returns a UTF8 character representation of a boolean value.
     * 
     * @param value the value to convert.
     * @return checkmark character if true; ballot-x character otherwise.
     */
    public static char checkmark(boolean value) {
        return value ? '\u2713' : '\u2717';
    }
    
    /**
     * Returns the value of the defined property
     * 
     * @param property
     * @return  value
     */
    public static java.lang.String getProperty(java.lang.String property) {
        return Props.getProperty(property);
    }
}
