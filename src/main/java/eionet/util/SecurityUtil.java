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
 * The Original Code is "EINRC-4 / Meta Project".
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

package eionet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.meta.AfterCASLoginServlet;
import eionet.meta.DDCASUser;
import eionet.meta.DDRuntimeException;
import eionet.meta.DDUser;
import eionet.meta.LoginServlet;
import eionet.meta.filters.CASFilterConfig;

/**
 * This is a class containing several utility methods for keeping security.
 *
 * @author Jaanus Heinlaid
 */
public final class SecurityUtil {

    /** */
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";

    /** */
    private static String casLoginUrl;
    private static String casServerName;

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class);

    /**
     *
     * prevent util class initialization.
     */
    private SecurityUtil() {
        throw new IllegalStateException("cannot initialize util class");
    }

    /**
     * Returns current user, or null, if the current session does not have user attached to it.
     */
    public static final DDUser getUser(HttpServletRequest request) {

        HttpSession session = request.getSession();
        DDUser user = session == null ? null : (DDUser) session.getAttribute(REMOTEUSER);

        if (user == null) {
            String casUserName = session == null ? null : (String) session.getAttribute(CASFilter.CAS_FILTER_USER);
            if (casUserName != null) {
                user = DDCASUser.create(casUserName);
                session.setAttribute(REMOTEUSER, user);
            }
        } else if (user instanceof DDCASUser) {
            String casUserName = (String) session.getAttribute(CASFilter.CAS_FILTER_USER);
            if (casUserName == null) {
                user.invalidate();
                user = null;
                session.removeAttribute(REMOTEUSER);
            } else if (!casUserName.equals(user.getUserName())) {
                user.invalidate();
                user = DDCASUser.create(casUserName);
                session.setAttribute(REMOTEUSER, user);
            }
        }

        if (user != null) {
            return user.isAuthentic() ? user : null;
        } else {
            return null;
        }
    }

    /**
     *
     * @param request
     * @param aclPath
     * @param prm
     * @return
     * @throws Exception
     */
    public static boolean userHasPerm(HttpServletRequest request, String aclPath, String prm) throws Exception {

        DDUser user = SecurityUtil.getUser(request);
        return SecurityUtil.hasPerm(user == null ? null : user.getUserName(), aclPath, prm);
    }

    /**
     * Checks if the user has permission for the ACl.
     * NB If user has permission to the parent ACL *and parent ACL is not root ACL* - no children ACL is checked!
     *
     * @param usr user name
     * @param aclPath full acl path
     * @param prm permission
     * @return true if user has permission
     * @throws Exception if check fails
     */
    public static boolean hasPerm(String usr, String aclPath, String prm) throws Exception {
        if (!aclPath.startsWith("/")) {
            return false;
        }

        boolean has = false;
        AccessControlListIF acl = null;
        int i = aclPath.length() <= 1 ? -1 : aclPath.indexOf("/", 1); // not forgetting root path ("/")
        while (i != -1 && !has) {
            String subPath = aclPath.substring(0, i);
            try {
                acl = AccessController.getAcl(subPath);
            } catch (Exception e) {
                acl = null;
            }

            if (acl != null) {
                has = acl.checkPermission(usr, prm);
            }

            i = aclPath.indexOf("/", i + 1);
        }

        if (!has) {
            try {
                acl = AccessController.getAcl(aclPath);
            } catch (Exception e) {
                acl = null;
            }

            if (acl != null) {
                has = acl.checkPermission(usr, prm);
            }
        }

        return has;
    }

    /**
     *
     * @param usr
     * @param aclPath
     * @param prm
     * @return
     * @throws Exception
     */
    public static boolean hasChildPerm(String usr, String aclPath, String prm) throws Exception {
        HashMap acls = AccessController.getAcls();
        Iterator aclNames = acls.keySet().iterator();
        AccessControlListIF acl;
        while (aclNames.hasNext()) {
            String aclName = (String) aclNames.next();
            if (aclName.startsWith(aclPath)) {
                acl = (AccessControlListIF) acls.get(aclName);
                if (acl.checkPermission(usr, prm)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @param request
     * @return
     */
    public static String getLoginURL(HttpServletRequest request) {

        // Legacy login mechanism. Used if the application is configured to not use Central Authentication Service (CAS).
        //String result = "javascript:login('" + request.getContextPath() + "')";
        String result = request.getContextPath() + "/" + LoginServlet.LOGIN_JSP;

        boolean rememberAfterLoginUrl = false;
        if (Props.isUseCentralAuthenticationService()) {

            CASFilterConfig casFilterConfig = CASFilterConfig.getInstance();
            if (casFilterConfig != null) {

                String casLoginUrl = casFilterConfig.getInitParameter(CASFilter.LOGIN_INIT_PARAM);
                if (casLoginUrl != null) {

                    String casServerName = casFilterConfig.getInitParameter(CASFilter.SERVERNAME_INIT_PARAM);
                    if (casServerName == null) {
                        throw new DDRuntimeException("If " + CASFilter.LOGIN_INIT_PARAM
                                + " context parameter has been specified, so must be " + CASFilter.SERVERNAME_INIT_PARAM);
                    }

                    rememberAfterLoginUrl = true;
                    try {
                        result =
                                casLoginUrl
                                        + "?service="
                                        + URLEncoder.encode(request.getScheme() + "://" + casServerName + request.getContextPath()
                                                + "/login", "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new DDRuntimeException(e.toString(), e);
                    }
                }
            }
        } else {
            String servletPath = request.getServletPath();
            if (servletPath == null || !servletPath.endsWith(LoginServlet.LOGIN_JSP)) {
                rememberAfterLoginUrl = true;
            }
        }

        if (rememberAfterLoginUrl) {

            String requestURL = request.getRequestURL().toString();
            if (requestURL != null && !AfterCASLoginServlet.isSkipUrl(requestURL)) {

                request.getSession().setAttribute(AfterCASLoginServlet.AFTER_LOGIN_ATTR_NAME, buildAfterLoginURL(request));
            }
        }

        return result;
    }

    /**
     *
     * @param request
     * @return
     */
    public static String getLogoutURL(HttpServletRequest request) {

        // The default result used when the application is configured to not use Central Authentication Service (CAS).
        String result = "index.jsp";

        if (Props.isUseCentralAuthenticationService()) {

            CASFilterConfig casFilterConfig = CASFilterConfig.getInstance();
            if (casFilterConfig != null) {

                String casLoginUrl = casFilterConfig.getInitParameter(CASFilter.LOGIN_INIT_PARAM);
                if (casLoginUrl != null) {

                    String casServerName = casFilterConfig.getInitParameter(CASFilter.SERVERNAME_INIT_PARAM);
                    if (casServerName == null) {
                        throw new DDRuntimeException("If " + CASFilter.LOGIN_INIT_PARAM
                                + " context parameter has been specified, so must be " + CASFilter.SERVERNAME_INIT_PARAM);
                    }

                    try {
                        result =
                                casLoginUrl.replaceFirst("/login", "/logout")
                                        + "?url="
                                        + URLEncoder.encode(
                                                request.getScheme() + "://" + casServerName + request.getContextPath(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new DDRuntimeException(e.toString(), e);
                    }
                }
            }
        }

        return result;
    }

    /**
     *
     * @return
     */
    private static String getUrlWithContextPath(HttpServletRequest request) {

        StringBuffer url = new StringBuffer(request.getScheme());
        url.append("://").append(request.getServerName());
        if (request.getServerPort() > 0) {
            url.append(":").append(request.getServerPort());
        }
        url.append(request.getContextPath());
        return url.toString();
    }

    /**
     * @return the rEMOTEUSER
     */
    public static String getREMOTEUSER() {
        return REMOTEUSER;
    }

    /**
     *
     * @param request
     * @return
     */
    public static String buildAfterLoginURL(HttpServletRequest request) {

        String requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
        if (requestUri == null) {
            requestUri = request.getRequestURL().toString();
        }

        String queryString = (String) request.getAttribute("javax.servlet.forward.query_string");
        if (queryString == null) {
            queryString = request.getQueryString();
        }

        return queryString == null ? requestUri : requestUri + "?" + queryString;
    }

    /**
     * Returns the list of countries the logged in user represents detected from the roles assigned for the user in LDAP.
     * The country code is last 2 digits on role name. The country codes are detected only for the parent roles given as method
     * argument.
     *
     * @param dduser Logged in user object.
     * @param parentRoles List of parent roles, where country code will be detected as last 2 digits.
     * @return List of ISO2 country codes in upper codes. Null if user object or parentRoles are null.
     */
    public static List<String> getUserCountriesFromRoles(DDUser dduser, String[] parentRoles) {

        if (dduser == null || dduser.getUserRoles() == null || parentRoles == null) {
            return null;
        }

        List<String> countries = new ArrayList<String>();

        for (String role : dduser.getUserRoles()) {
            for (String parentRole : parentRoles) {
                if (!parentRole.endsWith("-")) {
                    parentRole = parentRole.concat("-");
                }
                if (role.startsWith(parentRole)) {
                    String roleSuffix = StringUtils.substringAfter(role, parentRole).toUpperCase();
                    if (roleSuffix.length() == 2 && !countries.contains(roleSuffix)) {
                        countries.add(roleSuffix);
                    }
                }
            }
        }
        return countries;
    }
}
