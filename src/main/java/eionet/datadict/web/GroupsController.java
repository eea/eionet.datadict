package eionet.datadict.web;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.services.LdapService;
import eionet.datadict.services.acl.AclOperationsService;
import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import eionet.web.action.ErrorActionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.acl.Group;
import java.util.*;

@Controller
@RequestMapping("/admintools")
public class GroupsController {

    @Autowired
    private AclOperationsService aclOperationsService;

    @Autowired
    private LdapService ldapService;

    @GetMapping("/list")
    public String getGroupsAndUsers(Model model, HttpServletRequest request) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        if(!UserUtils.isUserLoggedIn(request)) {
            model.addAttribute("msgOne", ErrorActionBean.ErrorType.NOT_AUTHENTICATED_401);
            model.addAttribute("msgTwo", "You have to login to access Admin tools page");
            return "message";
        }
        if (!UserUtils.hasAuthorizationPermission(request, "/v2/admintools", "v")) {
            model.addAttribute("errorType", ErrorActionBean.ErrorType.FORBIDDEN_403);
            model.addAttribute("errorMessage", "You are not authorized to access Admin tools page");
            return "message";
        }
        Hashtable<String, Vector<String>> ddGroupsAndUsers = getGroupsAndUsers();
        Set<String> ddGroups = ddGroupsAndUsers.keySet();
        model.addAttribute("ddGroups", ddGroups);
        model.addAttribute("ddGroupsAndUsers", ddGroupsAndUsers);
        //REAL IMPLEMENTATION - CODE TO BE ADDED
//        HashMap<String, ArrayList<String>> ldapRolesByUser = new HashMap<String, ArrayList<String>>();
//        for (String ddGroup : ddGroups) {
//            Vector<String> ddGroupUsers = ddGroupsAndUsers.get(ddGroup);
//            for (String user : ddGroupUsers) {
//                ArrayList<String> ldapRoles = new ArrayList<>();
//                List<LdapRole> userLdapRolesList = ldapService.getUserLdapRoles(user, "Users", "DD_roles");
//                for (LdapRole ldapRole : userLdapRolesList) {
//                    ldapRoles.add(ldapRole.getName());
//                }
//                ldapRolesByUser.put(user, ldapRoles);
//            }
//        }
//        model.addAttribute("memberLdapGroups", ldapRolesByUser);
        //REAL IMPLEMENTATION - CODE TO BE ADDED

        //CODE FOR GETTING TEST RESULTS - TO BE DELETED
        String[] ldapGroups = getTestLdapGroups(model);
        HashMap<String, ArrayList<String>> memberLdapGroups = getTestMemberLdapGroups(ldapGroups);
        model.addAttribute("memberLdapGroups", memberLdapGroups);
        //CODE FOR GETTING TEST RESULTS - TO BE DELETED
        return "groupsAndUsers";
    }

    private HashMap<String, ArrayList<String>> getTestMemberLdapGroups(String[] ldapGroups) {
        HashMap<String, ArrayList<String>> memberLdapGroups = new HashMap<>();
        ArrayList<String> memberList = new ArrayList<>();
        memberList.add(ldapGroups[0]);
        memberList.add(ldapGroups[1]);
        memberLdapGroups.put("favvmary", memberList);
        ArrayList<String> memberList2 = new ArrayList<>();
        memberList2.add(ldapGroups[0]);
        memberList2.add(ldapGroups[1]);
        memberList2.add(ldapGroups[2]);
        memberLdapGroups.put("cryan", memberList2);
        return memberLdapGroups;
    }

    private String[] getTestLdapGroups(Model model) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        String[] ldapGroups = new String[100];
        ldapGroups[0] = "administrator";
        ldapGroups[1] = "simple_user";
        ldapGroups[2] = "eea_user";
        ldapGroups[3] = "authors";
        ldapGroups[4] = "developers";
        ldapGroups[5] = "dd_administrators";
        return ldapGroups;
    }

    @RequestMapping(value = "/ldapOptions")
    @ResponseBody
    public List<String> getLdapList(@RequestParam(value="term", required = false, defaultValue="") String term) {
        //REAL IMPLEMENTATION - CODE TO BE ADDED
//        List<String> ldapRoleNames = new ArrayList<>();
//        List<LdapRole> ldapRoles = ldapService.getAllLdapRoles("Users", "DD_roles");
//        for (LdapRole ldapRole : ldapRoles) {
//            String roleName = ldapRole.getName();
//            ldapRoleNames.add(roleName);
//        }
//        return ldapRoleNames;
        //REAL IMPLEMENTATION - CODE TO BE ADDED

        //CODE FOR GETTING TEST RESULTS - TO BE DELETED
        List<String> results = getTestLdapList(term);
        //CODE FOR GETTING TEST RESULTS - TO BE DELETED
        return results;
    }

    private List<String> getTestLdapList(String term) {
        List<String> options = new ArrayList<>();
        options.add("admins");
        options.add("dd_admins");
        options.add("eea");
        options.add("authors");
        List<String> results = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(term)) {
                results.add(option);
            }
        }
        return results;
    }

    @GetMapping("/addUser")
    public String addUser() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Set<String> groups = getGroups();
        return "";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam("ddGroupName") String groupName, @RequestParam("memberName") String memberName)
            throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Hashtable<String, Vector<String>> groupsAndUsers = getGroupsAndUsers();
        Hashtable<String, Group> groups = new Hashtable<String, Group>();
        for (String groupN : groupsAndUsers.keySet()) {
            Vector<String> groupVector = groupsAndUsers.get(groupN);
            groups.put(groupN, (Group) groupsAndUsers.get(groupN));
        }
        Group group = (Group) groupsAndUsers.get(groupName);
        Enumeration<? extends Principal> members = group.members();
        while (members.hasMoreElements()) {
            Principal member = members.nextElement();
            if (member.getName().equals(memberName)) {
                group.removeMember(member);
            }
            break;
        }
//        groups.put(groupName, group);
//        GroupImpl group = new GroupImpl(groupName);
//        for (String member : members) {
//            group.addMember(member);
//       }
//        aclOperationsService.setGroups(groups);
        return "redirect:/v2/groups/list";
    }

    @GetMapping("/addGroup")
    public String addGroup() {
        return "";
    }

    Set<String> getGroups() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        Hashtable<String, Vector<String>> groupsAndUsers = getGroupsAndUsers();
        return groupsAndUsers.keySet();
    }

    Hashtable<String, Vector<String>> getGroupsAndUsers() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        return aclOperationsService.getGroupsAndUsersHashTable();
    }

}
