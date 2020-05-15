package eionet.datadict.web;

import eionet.meta.DDUser;
import eionet.util.SecurityUtil;

import javax.servlet.http.HttpServletRequest;

public class UserUtils {

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
}
