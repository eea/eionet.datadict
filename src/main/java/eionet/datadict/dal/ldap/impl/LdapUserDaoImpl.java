package eionet.datadict.dal.ldap.impl;

import eionet.datadict.dal.ldap.LdapUserDao;
import eionet.datadict.model.LdapUser;
import org.springframework.stereotype.Repository;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LdapUserDaoImpl extends BaseLdapDao implements LdapUserDao {

    private String usersDn;

    public LdapUserDaoImpl() {
        usersDn = "ou=Users," + baseDn;
    }

    @Override
    public List findAllUsers() {
        List ldapUsers = new ArrayList<LdapUser>();
        LdapContext ctx = null;
        try {
            ctx = getPagedLdapContext();
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setCountLimit(0);
            ctls.setTimeLimit(0);
            ctls.setReturningObjFlag(true);
            NamingEnumeration<SearchResult> results = ctx.search(usersDn, "(objectClass=top)", ctls);
            while (results != null && results.hasMore()) {
                SearchResult result = (SearchResult) results.next();
                Attributes attrs = result.getAttributes();
                Attribute fullnameAttr = attrs.get("cn");

                String fullname = "";
                if (fullnameAttr != null && fullnameAttr.get() != null) {
                    fullname = (String) fullnameAttr.get();
                }

                LdapUser ldapUser = new LdapUser();
                ldapUser.setFullName(fullname);

                ldapUsers.add(ldapUser);
            }
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ldapUsers;
    }
}
