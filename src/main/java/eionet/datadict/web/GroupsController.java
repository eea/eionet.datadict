package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.errors.UserExistsException;
import eionet.datadict.errors.XmlMalformedException;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.web.viewmodel.GroupDetails;
import eionet.meta.DDUser;
import eionet.meta.dao.LdapDaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

@Controller
@RequestMapping("/admintools")
public class GroupsController {

    private AclService aclService;
    /** */
    public static final String REMOTEUSER = "eionet.util.SecurityUtil.user";

    public static final String LDAP_GROUP_NOT_EXIST = "The LDAP group name you entered doesn't exist";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsController.class);

    @Autowired
    public GroupsController(AclService aclService) {
        this.aclService = aclService;
    }

    @GetMapping("/list")
    public String getGroupsAndUsers(Model model, HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if(!UserUtils.isUserLoggedIn(request)) {
            model.addAttribute("msgOne", PageErrorConstants.NOT_AUTHENTICATED + " Admin tools");
            return "message";
        }
        if (!UserUtils.hasAuthPermission(request, "/admintools", "v")) {
            model.addAttribute("msgOne", PageErrorConstants.FORBIDDEN_ACCESS + " Admin tools");
            return "message";
        }
        Hashtable<String, Vector<String>> ddGroupsAndUsers;
        if (UserUtils.getDdGroupsAndUsers() != null) {
            ddGroupsAndUsers = UserUtils.getDdGroupsAndUsers();
        } else {
            ddGroupsAndUsers = UserUtils.fetchGroupsAndUsers(false);
            UserUtils.setDdGroupsAndUsers(ddGroupsAndUsers);
        }
        Set<String> ddGroups = ddGroupsAndUsers.keySet();
        model.addAttribute("ddGroups", ddGroups);
        model.addAttribute("ddGroupsAndUsers", ddGroupsAndUsers);
        GroupDetails groupDetails = new GroupDetails();
        model.addAttribute("groupDetails", groupDetails);
        return "groupsAndUsers";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("groupDetails") GroupDetails groupDetails, Model model, HttpServletRequest request)
            throws UserExistsException, XmlMalformedException, LdapDaoException, AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "u")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        aclService.addUserToAclGroup(groupDetails.getUserName(), groupDetails.getGroup());
        refreshUserGroupResults(request);
        return "redirect:/v2/admintools/list";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam("ddGroupName") String groupName, @RequestParam("memberName") String userName, Model model, HttpServletRequest request)
            throws XmlMalformedException, AclPropertiesInitializationException, LdapDaoException, AclLibraryAccessControllerModifiedException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "d")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        aclService.removeUserFromAclGroup(userName, groupName);
        refreshUserGroupResults(request);
        return "redirect:/v2/admintools/list";
    }

    protected void refreshUserGroupResults(HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException, LdapDaoException {
        HttpSession session = request.getSession();
        DDUser user = session == null ? null : (DDUser) session.getAttribute(REMOTEUSER);
        if (user!=null && user.isAuthentic()) {
            UserUtils userUtils = new UserUtils();
            ArrayList<String> results = userUtils.getUserOrGroup(user.getUserName(), true, session);
            user.setGroupResults(results);
            session.setAttribute(REMOTEUSER, user);
        }
    }

    @ExceptionHandler({UserExistsException.class, XmlMalformedException.class, AclLibraryAccessControllerModifiedException.class})
    public String handleExceptions(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", exception.getMessage());
        return "message";
    }

    @ExceptionHandler(LdapDaoException.class)
    public String handleLdapDaoException(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", PageErrorConstants.LDAP_ERROR);
        return "message";
    }

    @ExceptionHandler(AclPropertiesInitializationException.class)
    public String handleAclPropertiesException(Model model, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        model.addAttribute("msgOne", PageErrorConstants.ACL_PROPS_INIT);
        return "message";
    }

}
