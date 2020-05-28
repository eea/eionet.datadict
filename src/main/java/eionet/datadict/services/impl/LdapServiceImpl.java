package eionet.datadict.services.impl;

import eionet.datadict.dal.ldap.LdapRoleDao;
import eionet.datadict.model.LdapRole;
import eionet.datadict.services.LdapService;
import eionet.meta.DDUser;
import eionet.meta.dao.LdapDaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LdapServiceImpl implements LdapService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapServiceImpl.class);

    private final LdapRoleDao ldapRoleDao;

    private List<LdapRole> ldapRoles = new ArrayList<>();

    @Autowired
    public LdapServiceImpl(LdapRoleDao ldapRoleDao) {
        this.ldapRoleDao = ldapRoleDao;
    }

    @Override
    public List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException {
        ldapRoles = ldapRoleDao.findUserRoles(user);
        return ldapRoles;
    }

    @Override
    public List<LdapRole> getAllLdapRoles() throws LdapDaoException {
        ldapRoles = ldapRoleDao.findAllRoles();
        return ldapRoles;
    }
}
