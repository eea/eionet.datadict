package eionet.datadict.dal.ldap.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;
import eionet.util.Props;
import eionet.util.PropsIF;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.io.IOException;
import java.util.*;

@Repository
public class LdapRoleDaoImpl extends BaseLdapDao implements LdapRoleDao {

    private String usersDn;
    private String rolesDn;

    public LdapRoleDaoImpl() {
        usersDn = Props.getProperty(PropsIF.LDAP_USER_DIR) + "," + baseDn;
        rolesDn = Props.getProperty(PropsIF.LDAP_ROLE_DIR) + "," + baseDn;
    }

    @Override
    public List<LdapRole> findUserRoles(String user) throws LdapDaoException {
        List<LdapRole> result = new ArrayList<>();
        DirContext ctx = null;
        try {
            String myFilter = "(&(objectClass=groupOfUniqueNames)(uniqueMember=uid=" + user + "," + usersDn + "))";
            NamingEnumeration results = getResults(myFilter);
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                String dn = sr.getName();
                if (dn != null && dn.length() > 0){
                    String cn = (String)sr.getAttributes().get(Props.getProperty(PropsIF.LDAP_ROLE_NAME)).get();
                    LdapRole r = new LdapRole();
                    r.setFullDn(dn + "," + rolesDn);
                    r.setName("cn=" + cn);
                    result.add(r);
                }
            }
        } catch (NamingException e) {
            throw new LdapDaoException(e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    @Override
    public List<LdapRole> findAllRoles() throws LdapDaoException {
        List<LdapRole> result = new ArrayList(1);
        LdapContext ctx = null;
        try {
            ctx = getPagedLdapContext();
            String myFilter = "objectclass=groupOfUniqueNames";
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setCountLimit(0);
            sc.setTimeLimit(0);
            sc.setReturningObjFlag(true);

            byte[] cookie = null;
            int total;
            do {
                NamingEnumeration results = ctx.search(rolesDn, myFilter, sc);
                while (results != null && results.hasMore()) {
                    SearchResult sr = (SearchResult) results.next();
                    String dn = sr.getName();
                    if (dn != null && dn.length() > 0) {
                        String cn = (String)sr.getAttributes().get(Props.getProperty(PropsIF.LDAP_ROLE_NAME)).get();
                        LdapRole r = new LdapRole();
                        r.setFullDn(dn + "," + baseDn);
                        r.setName("cn=" + cn);
                        result.add(r);
                    }
                }
                Control[] controls = ctx.getResponseControls();
                if (controls != null) {
                    for (int i = 0; i < controls.length; i++) {
                        if (controls[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc =
                                    (PagedResultsResponseControl) controls[i];
                            total = prrc.getResultSize();
                            cookie = prrc.getCookie();
                        }
                    }
                }
                // Re-activate paged results
                ctx.setRequestControls(new Control[]{
                        new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL)});
            } while (cookie != null);
        } catch (IOException | NamingException e) {
            throw new LdapDaoException("Error: " + e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    @Override
    public List<String> findRoleUsers(String roleName) throws LdapDaoException {
        List<String> result = new ArrayList<>();
        DirContext ctx = null;
        try {
            String myFilter = "(&(objectClass=groupOfUniqueNames)(" + roleName + "))";
            NamingEnumeration results = getResults(myFilter);
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();
                Attribute usersAttr = attrs.get("uniquemember");
                if (usersAttr != null && usersAttr.get() != null){
                    result = parseUsersAttr(usersAttr);
                }
            }
        } catch (NamingException e) {
            throw new LdapDaoException(e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }

    protected List<String> parseUsersAttr(Attribute usersAttr) throws NamingException {
        List<String> result = new ArrayList<>();
        for (int i=0; i<usersAttr.size(); i++) {
            String user = (String) usersAttr.get(i);
            int pos = user.indexOf("ou=");
            String userName = user.substring(4, pos-1);
            result.add(userName);
        }
        return result;
    }

    protected NamingEnumeration getResults(String myFilter) throws NamingException {
        DirContext ctx = getDirContext();
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return ctx.search(rolesDn, myFilter, sc);
    }

    @Override
    public Hashtable<String, Object> getRoleInfo(String roleName) throws LdapDaoException {
        Hashtable<String, Object> result = new Hashtable<>();
        DirContext ctx = null;
        try {
            String myFilter = "(&(objectClass=groupOfUniqueNames)(cn=" + roleName + "))";
            NamingEnumeration results = getResults(myFilter);
            if (results == null || !results.hasMore()) {
                throw new LdapDaoException("Unable to find role " + roleName);
            }
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                String cn = (String)sr.getAttributes().get(Props.getProperty(PropsIF.LDAP_ROLE_NAME)).get();
                String description = "";
                String mail = "";
                Attributes attrs = sr.getAttributes();
                try {
                    description = getDescription(description, attrs);
                    mail = getMailAttr(mail, attrs);
                } catch (NullPointerException e) {

                }
                Vector<String> occupants = getOccupants(roleName, attrs);

                result.put("ID", roleName);
                result.put("NAME", cn);
                result.put("MAIL", mail);
                result.put("DESCRIPTION", description);
                result.put("OCCUPANTS", occupants);
            }
        } catch (NoSuchElementException e1) {

        } catch (NamingException e2) {
            throw new LdapDaoException(e2);
        } catch (Exception e3) {
            throw new LdapDaoException("Getting role information for role " + roleName + " failed: " + e3.toString());
        }
        finally {
            closeContext(ctx);
        }
        return result;
    }

    protected String getDescription(String description, Attributes attrs) throws NamingException {
        Attribute uniqueMember = attrs.get(Props.getProperty(PropsIF.LDAP_DESCRIPTION));
        if (uniqueMember != null) {
            description = (String) uniqueMember.get();
        }
        return description;
    }

    protected String getMailAttr(String mail, Attributes attrs) throws NamingException {
        Attribute mAttr = attrs.get(Props.getProperty(PropsIF.LDAP_ATTR_MAIL));
        if (mAttr != null) {
            mail = (String) mAttr.get();
        }
        return mail;
    }

    protected Vector<String> getOccupants(String roleName, Attributes attrs) throws LdapDaoException, NamingException {
        Attribute uniqueMember = null;
        Vector<String> occupants = new Vector();
        try {
           uniqueMember = attrs.get("uniquemember");
        } catch (Exception e) {
            throw new LdapDaoException("Error getting occupants for role : " + roleName);
        }

        if (uniqueMember != null && uniqueMember.get() != null){
            occupants = new Vector<>(parseUsersAttr(uniqueMember));
        }
        return occupants;
    }

}
