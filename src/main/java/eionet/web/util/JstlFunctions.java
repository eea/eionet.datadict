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


import javax.servlet.http.HttpSession;

import eionet.util.SecurityUtil;

/**
 *
 * @author Risto Alt
 *
 */
public class JstlFunctions {

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
}
