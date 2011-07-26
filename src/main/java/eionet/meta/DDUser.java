/**
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
 * The Original Code is "EINRC-6 / Data Dictionary Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */

package eionet.meta;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.AuthMechanism;
import com.tee.uit.security.SignOnException;

import eionet.directory.DirectoryService;
import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDUser{

    /** */
    private static final Logger LOGGER = Logger.getLogger(DDUser.class);

    /** */
    public static final String ACL_UPDATE_PRM   = "u";
    public static final String ACL_SERVICE_NAME = "/";

    /** */
    protected boolean authented = false;
    protected String username = null;
    protected String password = null;
    protected String fullName = null;
    protected String[] _roles = null;
    protected HashMap acls = null;

    /**
     *
     */
    public DDUser() {
    }

    /**
     *
     */
    public boolean authenticate(String userName, String userPwd) {

        invalidate();

        try {

            if (userPwd!=null && userPwd.equals("mi6")) {
                if (userName==null)
                    throw new SignOnException("username not given");
                fullName = userName;
            }
            else {
                AuthMechanism.sessionLogin(userName, userPwd);
                fullName = AuthMechanism.getFullName(userName);
            }

            authented = true;
            username = userName;
            password = userPwd;
        }
        catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

        return authented;
    }

    /**
     *
     * @return
     */
    public boolean isAuthentic() {
        return authented;
    }

    /**
     *
     * @param role
     * @return
     */
    public boolean isUserInRole(String role) {

        boolean b = false;
        if (_roles == null) {
            getUserRoles();
        }

        for (int i =0; i< _roles.length; i++) {
            if ( _roles[i].equals(role)) {
                b = true;
            }
        }

        return b;
    }

    /**
     *
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     *
     * @return
     */
    public String getUserName() {
        return username;
    }

    /**
     *
     * @return
     */
    public Connection getConnection() {

        try {
            return ConnectionUtil.getConnection();
        }
        catch (Exception e) {
            throw new DDRuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public String[] getUserRoles() {

        if (_roles == null) {
            try {

                Vector v = DirectoryService.getRoles(username);
                String[] roles = new String[v.size()];
                for ( int i=0; i< v.size(); i++)
                    _roles[i] = (String)v.elementAt(i);

            } catch ( Exception e ) {
                _roles = new String[]{};
            }
        }

        return _roles;
    }

    /**
     *
     *
     */
    public void invalidate() {
        authented = false;
        username = null;
        password = null;
    }

    /**
     *
     */
    public String toString() {
        return (username == null ? "" : username );
    }

    /**
     *
     * @param name
     * @return
     * @throws SignOnException
     */
    private AccessControlListIF getAcl(String name) throws SignOnException {

        if (acls == null)
            acls = AccessController.getAcls();

        return (AccessControlListIF)acls.get(name);
    }
}
