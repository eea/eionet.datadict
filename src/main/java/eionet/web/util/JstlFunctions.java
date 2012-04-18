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


import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import eionet.util.SecurityUtil;

/**
 *
 * @author Risto Alt
 *
 */
public class JstlFunctions {

    /** */
    private static final String INPUT_CHECKED_STRING = "checked=\"checked\"";
    private static final String INPUT_SELECTED_STRING = "selected=\"selected\"";
    private static final String INPUT_DISABLED_STRING = "disabled=\"disabled\"";

    /**
     * Returns the value of {@link CRUser#hasPermission(HttpSession, String, String)}, using the given inputs.
     *
     * @param session
     * @param aclPath
     * @param permission
     * @return
     */
    public static boolean userHasPermission(java.lang.String usr, java.lang.String aclPath, java.lang.String prm) throws Exception{
        return SecurityUtil.hasPerm(usr, aclPath, prm);
    }

    /**
     * 
     * @param o
     * @param seperator
     * @return
     */
    public static String join(Object o, String separator){

        if (o==null){
            return "";
        }
        else if (o instanceof String) {
            return (String) o;
        }
        else if (o instanceof Object[]){
            return StringUtils.join((Object[]) o, separator);
        }
        else if (o instanceof Collection){
            return StringUtils.join((Collection) o, separator);
        }
        else{
            throw new ClassCastException("Couldn't cast from this class: " + o.getClass().getName());
        }
    }

    /**
     * 
     * @param arrayOrCollection
     * @param object
     * @return
     */
    public static boolean contains(Object arrayOrCollection, Object object){

        if (arrayOrCollection!=null){

            if (arrayOrCollection instanceof Object[]){
                for (Object o : ((Object[]) arrayOrCollection)){
                    if (o.equals(object)){
                        return true;
                    }
                }
            }
            else if (arrayOrCollection instanceof Collection){
                for (Object o : ((Collection) arrayOrCollection)){
                    if (o.equals(object)){
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
    public static String inputCheckedString(boolean condition){
        if (condition == true){
            return INPUT_CHECKED_STRING;
        }
        else{
            return "";
        }
    }

    /**
     * 
     * @param condition
     * @return
     */
    public static String inputSelectedString(boolean condition){
        if (condition == true){
            return INPUT_SELECTED_STRING;
        }
        else{
            return "";
        }
    }

    /**
     * 
     * @param condition
     * @return
     */
    public static String inputDisabledString(boolean condition){
        if (condition == true){
            return INPUT_DISABLED_STRING;
        }
        else{
            return "";
        }
    }
}
