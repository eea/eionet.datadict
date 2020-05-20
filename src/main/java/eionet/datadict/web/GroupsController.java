package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.errors.UserExistsException;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclOperationsService;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.web.viewmodel.GroupDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admintools")
public class GroupsController {

    private AclService aclService;
    private AclOperationsService aclOperationsService;
    private LdapService ldapService;

    public static final String LDAP_GROUP_NOT_EXIST = "The LDAP group name you entered doesn't exist";
    private static final String USERS_OU = "Users";
    private static final String ROLES_OU = "Roles";

    @Autowired
    public GroupsController(AclService aclService, AclOperationsService aclOperationsService, LdapService ldapService) {
        this.aclService = aclService;
        this.aclOperationsService = aclOperationsService;
        this.ldapService = ldapService;
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
        Hashtable<String, Vector<String>> ddGroupsAndUsers = getGroupsAndUsers();
        Set<String> ddGroups = ddGroupsAndUsers.keySet();
        model.addAttribute("ddGroups", ddGroups);
        model.addAttribute("ddGroupsAndUsers", ddGroupsAndUsers);
        GroupDetails groupDetails = new GroupDetails();
        model.addAttribute("groupDetails", groupDetails);
        HashMap<String, ArrayList<String>> ldapRolesByUser = new HashMap<>();
        for (String ddGroup : ddGroups) {
            Vector<String> ddGroupUsers = ddGroupsAndUsers.get(ddGroup);
            for (String user : ddGroupUsers) {
                ArrayList<String> ldapRoles = new ArrayList<>();
                List<LdapRole> userLdapRolesList = ldapService.getUserLdapRoles(user);
                userLdapRolesList.forEach(role->ldapRoles.add(role.getName()));
                ldapRolesByUser.put(user, ldapRoles);
            }
        }
        model.addAttribute("memberLdapGroups", ldapRolesByUser);
        return "groupsAndUsers";
    }

    @RequestMapping(value = "/ldapOptions")
    @ResponseBody
    public List<String> getLdapList(@RequestParam(value="term", required = false, defaultValue="") String term) {
        List<String> ldapRoleNames = getAllLdapRoles();
        List<String> results = new ArrayList<>();
        for (String roleName : ldapRoleNames) {
            if (roleName.startsWith(term)) {
                results.add(roleName);
            }
        }
        return results;
    }

    List<String> getAllLdapRoles() {
        List<String> ldapRoleNames = new ArrayList<>();
        List<LdapRole> ldapRoles = ldapService.getAllLdapRoles();
        for (LdapRole ldapRole : ldapRoles) {
            String roleName = ldapRole.getName();
            ldapRoleNames.add(roleName);
        }
        return ldapRoleNames;
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("groupDetails") GroupDetails groupDetails, Model model, HttpServletRequest request)
            throws ParserConfigurationException, TransformerException, SAXException, XPathExpressionException, IOException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "u")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        try {
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
        } catch (UserExistsException e) {
            model.addAttribute("msgOne", e.getMessage());
            return "message";
        }
        return "redirect:/v2/admintools/list";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam("ddGroupName") String groupName, @RequestParam("memberName") String userName, Model model, HttpServletRequest request)
            throws SAXException, TransformerException, ParserConfigurationException, XPathExpressionException, IOException {
        if (!UserUtils.hasAuthPermission(request, "/admintools", "d")) {
            model.addAttribute("msgOne", PageErrorConstants.PERMISSION_REQUIRED);
            return "message";
        }
        aclService.removeUserFromAclGroup(userName, groupName);
        return "redirect:/v2/admintools/list";
    }

    Hashtable<String, Vector<String>> getGroupsAndUsers() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        return aclOperationsService.getGroupsAndUsersHashTable();
    }

}
