package eionet.datadict.web;

import eionet.datadict.errors.UserExistsException;
import eionet.datadict.errors.XmlMalformedException;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.web.viewmodel.GroupDetails;
import eionet.meta.dao.LdapDaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/admintools")
public class GroupsController {

    private AclService aclService;
    private LdapService ldapService;

    public static final String LDAP_GROUP_NOT_EXIST = "The LDAP group name you entered doesn't exist";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsController.class);
    private static HashMap<String, ArrayList<String>> ldapRolesByUser;
    private static List<LdapRole> ldapRoles;
    public static boolean groupModified;

    @Autowired
    public GroupsController(AclService aclService, LdapService ldapService) {
        this.aclService = aclService;
        this.ldapService = ldapService;
    }

    @GetMapping("/list")
    public String getGroupsAndUsers(Model model, HttpServletRequest request) throws LdapDaoException {
        if(!UserUtils.isUserLoggedIn(request)) {
            model.addAttribute("msgOne", PageErrorConstants.NOT_AUTHENTICATED + " Admin tools");
            return "message";
        }
        if (!UserUtils.hasAuthPermission(request, "/admintools", "v")) {
            model.addAttribute("msgOne", PageErrorConstants.FORBIDDEN_ACCESS + " Admin tools");
            return "message";
        }
        Hashtable<String, Vector<String>> ddGroupsAndUsers = UserUtils.ddGroupsAndUsers;
        Set<String> ddGroups = ddGroupsAndUsers.keySet();
        model.addAttribute("ddGroups", ddGroups);
        model.addAttribute("ddGroupsAndUsers", ddGroupsAndUsers);
        GroupDetails groupDetails = new GroupDetails();
        model.addAttribute("groupDetails", groupDetails);
        if (ldapRolesByUser == null || (ldapRolesByUser!=null && ldapRolesByUser.size()==0) || groupModified) {
            ldapRolesByUser = getUserLdapRoles(ddGroupsAndUsers, ddGroups);
            groupModified = false;
        }
        model.addAttribute("memberLdapGroups", ldapRolesByUser);
        return "groupsAndUsers";
    }

    protected HashMap<String, ArrayList<String>> getUserLdapRoles(Hashtable<String, Vector<String>> ddGroupsAndUsers, Set<String> ddGroups) throws LdapDaoException {
        HashMap<String, ArrayList<String>> rolesByUser = new HashMap<>();
        for (String ddGroup : ddGroups) {
            Vector<String> ddGroupUsers = ddGroupsAndUsers.get(ddGroup);
            for (String user : ddGroupUsers) {
                ArrayList<String> ldapRoles = new ArrayList<>();
                List<LdapRole> userLdapRolesList = ldapService.getUserLdapRoles(user);
                userLdapRolesList.forEach(role->ldapRoles.add(role.getName()));
                rolesByUser.put(user, ldapRoles);
            }
        }
        return rolesByUser;
    }

    @RequestMapping(value = "/ldapOptions")
    @ResponseBody
    public List<String> getLdapList(@RequestParam(value="term", required = false, defaultValue="") String term) throws LdapDaoException {
        List<String> ldapRoleNames = getAllLdapRoles();
        List<String> results = new ArrayList<>();
        for (String roleName : ldapRoleNames) {
            if (roleName.startsWith(term)) {
                results.add(roleName);
            }
        }
        return results;
    }

    protected List<String> getAllLdapRoles() throws LdapDaoException {
        List<String> ldapRoleNames = new ArrayList<>();
        if (ldapRoles == null || (ldapRoles!=null && ldapRoles.size()==0)) {
            ldapRoles = ldapService.getAllLdapRoles();
        }
        ldapRoles.forEach(role->ldapRoleNames.add(role.getName()));
        return ldapRoleNames;
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("groupDetails") GroupDetails groupDetails, Model model, HttpServletRequest request) throws UserExistsException, XmlMalformedException, LdapDaoException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "u")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        if (groupDetails.getGroupNameOptionOne()!=null) {
            aclService.addUserToAclGroup(groupDetails.getUserName(), groupDetails.getGroupNameOptionOne());
        } else {
            List<String> ldapRoles = getAllLdapRoles();
            if (!ldapRoles.contains(groupDetails.getLdapGroupName())) {
                model.addAttribute("msgOne", LDAP_GROUP_NOT_EXIST);
                return "message";
            }
            aclService.addUserToAclGroup(groupDetails.getLdapGroupName(), groupDetails.getGroupNameOptionTwo());
        }
        return "redirect:/v2/admintools/list";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam("ddGroupName") String groupName, @RequestParam("memberName") String userName, Model model, HttpServletRequest request) throws XmlMalformedException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "d")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        aclService.removeUserFromAclGroup(userName, groupName);
        return "redirect:/v2/admintools/list";
    }

    @ExceptionHandler({UserExistsException.class, XmlMalformedException.class})
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

}
