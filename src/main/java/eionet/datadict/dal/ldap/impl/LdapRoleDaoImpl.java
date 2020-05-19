package eionet.datadict.dal.ldap.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import eionet.meta.dao.LdapDaoException;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LdapRoleDaoImpl extends BaseLdapDao implements LdapRoleDao {

    private String usersDn;
    private String rolesDn;

    public LdapRoleDaoImpl() {

    }

    @Override
    public List<LdapRole> findUserRoles(String user, String usersOU, String rolesOU) throws Exception {
        usersDn = "ou=" + usersOU + "," + baseDn;
        rolesDn = "ou=" + rolesOU + "," + baseDn;
        List<LdapRole> result = new ArrayList<>();
        DirContext ctx = null;
        try {
            ctx = getDirContext();
            String myFilter = "(&(objectClass=groupOfUniqueNames)(uniqueMember=uid=" + user + "," + usersDn + "))";
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(rolesDn, myFilter, sc);
            while (results != null && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                String dn = sr.getName();
                if (dn != null && dn.length() > 0){
                    LdapRole r = new LdapRole();
                    r.setFullDn(dn + "," + rolesDn);
                    r.setName(dn);
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
    public List<LdapRole> findAllRoles(String rolesOU) throws Exception {
        rolesDn = "ou=" + rolesOU + "," + baseDn;
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
                        LdapRole r = new LdapRole();
                        r.setFullDn(dn + "," + baseDn);
                        r.setName(dn);
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
        } catch (NamingException e) {
            throw new Exception(e);
        } catch (IOException e) {
            throw new LdapDaoException("Error: " + e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }
}
