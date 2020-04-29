package eionet.datadict.dal.ldap.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.Role;
import eionet.meta.dao.DAOException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;

public class LdapRoleDaoImpl extends BaseLdapDao implements LdapRoleDao {

    private String usersDn;
    private String rolesDn;

    public LdapRoleDaoImpl() {
        usersDn = "ou=Users," + baseDn;
        rolesDn = "ou=Roles," + baseDn;
    }

    @Override
    public List<Role> findUserRoles(String user) throws Exception {
        List<Role> result = new ArrayList<>();
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
                    Role r = new Role();
                    r.setFullDn(dn + "," + baseDn);
                    r.setName(dn);
                    result.add(r);
                }
            }
        } catch (NamingException e) {
            throw new Exception(e);
        } finally {
            closeContext(ctx);
        }
        return result;
    }
}
