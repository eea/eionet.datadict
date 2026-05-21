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

}
