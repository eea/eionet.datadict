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
    public static volatile boolean groupModified;
    private static Hashtable<String, Vector<String>> ddGroupsAndUsers;

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
    public static synchronized ArrayList<String> getUserOrGroup(String userName) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        if (getGroupResults() == null || (getGroupResults()!=null && getGroupResults().size()==0) || groupModified) {
            if (getGroupResults() == null) {
                groupModified = true;
            }
            setGroupResults(new ArrayList<>());
            setGroupsAndUsers();
            Set<String> ddGroups = getDdGroupsAndUsers().keySet();
            for (String ddGroup : ddGroups) {
                Vector<String> ddGroupUsers = getDdGroupsAndUsers().get(ddGroup);
                if (ddGroupUsers.contains(userName)) {
                    getGroupResults().add(userName);
                    return getGroupResults();
                }
            }
            if (getUserLdapRolesList() == null || (getUserLdapRolesList()!=null && getUserLdapRolesList().size()==0)) {
                List<LdapRole> rolesList = getLdapService().getUserLdapRoles(userName);
                setUserLdapRolesList(rolesList);
            }
            getUserLdapRolesList().forEach(role->getGroupResults().add(role.getName()));
            return getGroupResults();
        }
        return getGroupResults();
    }

    protected static synchronized void setGroupsAndUsers() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Hashtable<String, Vector<String>> results;
        if (groupModified) {
            results = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(true);
            setDdGroupsAndUsers(results);
            groupModified = false;
        } else {
            results = getAclOperationsService().getRefreshedGroupsAndUsersHashTable(false);
            setDdGroupsAndUsers(results);
        }
    }

    public static AclOperationsService getAclOperationsService() {
        return SpringApplicationContext.getBean(AclOperationsService.class);
    }

    public static LdapService getLdapService() {
        return SpringApplicationContext.getBean(LdapService.class);
    }

    public static synchronized ArrayList<String> getGroupResults() {
        return groupResults;
    }

    public static synchronized void setGroupResults(ArrayList<String> groupResults) {
        UserUtils.groupResults = groupResults;
    }

    public static synchronized List<LdapRole> getUserLdapRolesList() {
        return userLdapRolesList;
    }

    public static synchronized void setUserLdapRolesList(List<LdapRole> userLdapRolesList) {
        UserUtils.userLdapRolesList = userLdapRolesList;
    }

    public static synchronized Hashtable<String, Vector<String>> getDdGroupsAndUsers() {
        return ddGroupsAndUsers;
    }

    public static synchronized void setDdGroupsAndUsers(Hashtable<String, Vector<String>> ddGroupsAndUsers) {
        UserUtils.ddGroupsAndUsers = ddGroupsAndUsers;
    }
}
