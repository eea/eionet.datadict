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

import eionet.acl.*;
import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.web.UserUtils;
import eionet.directory.DirectoryService;
import eionet.meta.dao.LdapDaoException;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.sql.ConnectionUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDUser {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DDUser.class);

    /** */
    public static final String ACL_UPDATE_PRM = "u";
    public static final String ACL_SERVICE_NAME = "/";


    /**
     * permission to view MS Access template.
     */
    public static final String MSACCESS_ADVANCED_PRM = "amsa";

    /** */
    protected boolean authented = false;
    protected String username = null;
    protected String password = null;
    protected String fullName = null;
    protected String[] roles = null;
    protected HashMap acls = null;

    /**
     *
     */
    public DDUser() {
    }

    public DDUser(String userName, boolean authenticated) {
        this.username = userName;
        this.authented = authenticated;
    }
    
    /**
     *
     */
    public boolean authenticate(String userName, String userPwd) {

        invalidate();

        try {
            String masterPwdHash = Props.getProperty(PropsIF.DD_MASTER_PASSWORD_HASH);

            if (userPwd != null && masterPwdHash != null && masterPwdHash.equals(DigestUtils.md5Hex(userPwd))) {
                if (userName == null) {
                    throw new SignOnException("username not given");
                }
                fullName = userName;
                LOGGER.info("User " + userName + " logged in with master password.");
            } else {
                AuthMechanism.sessionLogin(userName, userPwd);
                fullName = AuthMechanism.getFullName(userName);
                LOGGER.debug("User " + userName + " logged in through local login page.");
            }

            authented = true;
            username = userName;
            password = userPwd;
        } catch (Exception e) {
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
        if (roles == null) {
            getUserRoles();
        }

        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(role)) {
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
        } catch (Exception e) {
            throw new DDRuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public String[] getUserRoles() {

        if (roles == null) {
            try {

                Vector v = DirectoryService.getRoles(username);
                roles = new String[v.size()];
                for (int i = 0; i < v.size(); i++) {
                    roles[i] = (String) v.elementAt(i);
                }
                LOGGER.debug("Found " + roles.length + " roles for user (" + username + ")");
            } catch (Exception e) {
                LOGGER.error("Unable to get any role for loggedin user (" + username + "). DirServiceException: " + e.getMessage());
                roles = new String[] {};
            }
        }

        return roles;
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
    @Override
    public String toString() {
        return (username == null ? "" : username);
    }

    /**
     *
     * @param name
     * @return
     * @throws SignOnException
     */
    private AccessControlListIF getAcl(String name) throws SignOnException {

        if (acls == null) {
            acls = AccessController.getAcls();
        }

        return (AccessControlListIF) acls.get(name);
    }

    /**
     * Returns the value of {@link #hasPermission(String, String, String)}, using the given ACL path, the given permission, and the
     * name of the user found in the given session. If no user found in session, the method will be called with user name set to
     * null.
     *
     * @param session
     * @param aclPath
     * @param permission
     * @return
     */
    public static boolean hasPermission(HttpSession session, String aclPath, String permission) {

        // if no session given, simply return false
        if (session == null) {
            return false;
        }

        // get user object from session
        DDUser ddUser = (DDUser) session.getAttribute(SecurityUtil.REMOTEUSER);

        // get user name from user object, or set to null if user object null
        String userName = ddUser == null ? null : ddUser.getUserName();

        // check if user with this name has this permission in this ACL
        return DDUser.hasPermission(userName, aclPath, permission);
    }

    /**
     * Looks up an ACL with the given path, and checks if the given user has the given permission in it. If no such ACL is found,
     * the method returns false. If the ACL is found, and it has the given permission for the given user, the method returns true,
     * otherwise false.
     *
     * Situation where user name is null, is handled by the ACL library (it is treated as anonymous user).
     *
     * If the ACL library throws an exception, it is not thrown onwards, but still logged at error level.
     *
     * @param userName
     * @param aclPath
     * @param permission
     * @return
     */
    public static boolean hasPermission(String userName, String aclPath, String permission) {

        // consider missing ACL path or permission to be a programming error
        if (StringUtils.isBlank(aclPath) || StringUtils.isBlank(permission)) {
            throw new IllegalArgumentException("ACL path and permission must not be blank!");
        }

        boolean result = false;
        try {
            // get the ACL by the supplied path
            AccessControlListIF acl = AccessController.getAcl(aclPath);

            // if ACL found, check its permissions
            if (acl != null) {

                result = acl.checkPermission(userName, permission);
            } else {
                LOGGER.warn("ACL \"" + aclPath + "\" not found!");
            }
        } catch (SignOnException soe) {
            if (soe instanceof AclNotFoundException) {
                LOGGER.warn("ACL \"" + aclPath + "\" not found!");
            } else {
                LOGGER.error(soe.toString(), soe);
            }
        }

        return result;
    }

    /**
     *
     * @param aclPath
     * @param permission
     * @return
     */
    public boolean hasPermission(String aclPath, String permission) {
        ArrayList<String> results = null;
        try {
            results = UserUtils.getUserOrGroup(username);
            for (String result : results) {
                if (DDUser.hasPermission(result, aclPath, permission)) {
                    return true;
                }
            }
        } catch (AclLibraryAccessControllerModifiedException | AclPropertiesInitializationException | LdapDaoException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
