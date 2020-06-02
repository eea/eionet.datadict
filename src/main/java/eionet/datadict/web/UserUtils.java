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
import java.util.*;

public class UserUtils {

    private static List<LdapRole> userLdapRolesList;
    private static ArrayList<String> groupResults;
    public static boolean groupModified;
    public static Hashtable<String, Vector<String>> ddGroupsAndUsers;

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
    public static ArrayList<String> getUserOrGroup(String userName) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        if (groupResults == null || groupModified) {
            groupResults = new ArrayList<>();
            setGroupsAndUsers();
            Set<String> ddGroups = ddGroupsAndUsers.keySet();
            for (String ddGroup : ddGroups) {
                Vector<String> ddGroupUsers = ddGroupsAndUsers.get(ddGroup);
                if (ddGroupUsers.contains(userName)) {
                    groupResults.add(userName);
                    return groupResults;
                }
            }
            if (userLdapRolesList == null) {
                userLdapRolesList = getLdapService().getUserLdapRoles(userName);
            }
            userLdapRolesList.forEach(role->groupResults.add(role.getName()));
            return groupResults;
        }
        return groupResults;
    }

    protected static void setGroupsAndUsers() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        ddGroupsAndUsers = new Hashtable<>();
        if (groupModified) {
            ddGroupsAndUsers = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(true);
            groupModified = false;
        } else {
            ddGroupsAndUsers = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(false);
        }
    }

    public static AclOperationsService getAclOperationsService() {
        return SpringApplicationContext.getBean(AclOperationsService.class);
    }

    public static LdapService getLdapService() {
        return SpringApplicationContext.getBean(LdapService.class);
    }

}
