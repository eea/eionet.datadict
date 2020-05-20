package eionet.datadict.services.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LdapServiceImpl implements LdapService {

    private final LdapRoleDao ldapRoleDao;

    private List<LdapRole> ldapRoles;

    @Autowired
    public LdapServiceImpl(LdapRoleDao ldapRoleDao) {
        this.ldapRoleDao = ldapRoleDao;
    }

    @Override
    public List<LdapRole> getUserLdapRoles(String user) {
        try {
            ldapRoles = ldapRoleDao.findUserRoles(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ldapRoles;
    }

    @Override
    public List<LdapRole> getAllLdapRoles() {
        try {
            ldapRoles = ldapRoleDao.findAllRoles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ldapRoles;
    }
}
