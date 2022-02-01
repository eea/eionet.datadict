package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclOperationsService;
import eionet.meta.DDUser;
import eionet.meta.dao.LdapDaoException;
import eionet.meta.spring.SpringApplicationContext;
import eionet.util.SecurityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class UserUtils {

    private static Hashtable<String, Vector<String>> ddGroupsAndUsers;
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";

    public UserUtils() {
    }

    /**
     * checks if user is authenticated
     * @param request
     * @return
     */
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        return getUser(request) != null;
    }

    /**
     * checks if user is authenticated and has specific permission to aclPath
     * @param request
     * @param aclPath
     * @param perm
     * @return
     */
    public static boolean hasAuthPermission(HttpServletRequest request, String aclPath, String perm) {
        DDUser user = getUser(request);
        if (user!=null) {
            return hasPermission(user, aclPath, perm);
        }
        return false;
    }

    /**
     * checks if user has specific permission to aclPath
     * @param user
     * @param perm
     * @return
     */
    public static Boolean hasPermission(DDUser user, String aclPath, String perm) {
        return user.hasPermission(aclPath, perm);
    }

    /**
     * retrieves user
     * @param request
     * @return
     */
    public static DDUser getUser(HttpServletRequest request) {
        return SecurityUtil.getUser(request);
    }

    /**
     * If a user belongs to specific acl group retrieves user's username. If not retrieves user's ldap groups.
     * @param userName
     * @return
     * @throws AclLibraryAccessControllerModifiedException
     * @throws AclPropertiesInitializationException
     */
    public ArrayList<String> getUserOrGroup(String userName, boolean init, HttpSession session) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        ArrayList<String> userGroupResults = new ArrayList<>();
        Hashtable<String, Vector<String>> results = fetchGroupsAndUsers(init);
        setDdGroupsAndUsers(results);
        Set<String> ddGroups = getDdGroupsAndUsers().keySet();
        for (String ddGroup : ddGroups) {
            Vector<String> ddGroupUsers = getDdGroupsAndUsers().get(ddGroup);
            if (ddGroupUsers.contains(userName)) {
                userGroupResults.add(userName);
            }
        }
        DDUser user = session == null ? null : (DDUser) session.getAttribute(REMOTEUSER);
        if (user!=null && !user.isLocalUser()) {
            List<LdapRole> rolesList = this.getLdapService().getUserLdapRoles(userName);
            rolesList.forEach(role -> userGroupResults.add(role.getName()));
        }
        return userGroupResults;
    }

    public static Hashtable<String, Vector<String>> fetchGroupsAndUsers(boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Hashtable<String, Vector<String>> results;
        if (init) {
            return getAclOperationsService().getRefreshedGroupsAndUsersHashTable(true);
        }
        return getAclOperationsService().getRefreshedGroupsAndUsersHashTable(false);
    }

    public static AclOperationsService getAclOperationsService() {
        return SpringApplicationContext.getBean(AclOperationsService.class);
    }

    public LdapService getLdapService() {
        return SpringApplicationContext.getBean(LdapService.class);
    }

    public static Hashtable<String, Vector<String>> getDdGroupsAndUsers() {
        return ddGroupsAndUsers;
    }

    public static void setDdGroupsAndUsers(Hashtable<String, Vector<String>> ddGroupsAndUsers) {
        UserUtils.ddGroupsAndUsers = ddGroupsAndUsers;
    }
}
