package eionet.datadict.services.acl.impl;

import eionet.acl.AccessController;
import eionet.acl.AclProperties;
import eionet.datadict.errors.AclAccessControllerInitializationException;
import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.datadict.services.acl.AclOperationsService;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

@Service
public class AclOperationsServiceImpl implements AclOperationsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AclOperationsServiceImpl.class);

    ConfigurationPropertyResolver configurationPropertyResolver;

    @Autowired
    public AclOperationsServiceImpl(ConfigurationPropertyResolver configurationPropertyResolver) {
        this.configurationPropertyResolver = configurationPropertyResolver;
    }

    public Hashtable<String, Vector<String>> getRefreshedGroupsAndUsersHashTable() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        try {
            AccessController accessController = this.getAclLibraryAccessControllerInstance(this.getAclProperties());
            Method initAcls = null;
            initAcls = AccessController.class.getDeclaredMethod("initAcls");
            initAcls.setAccessible(true);
            initAcls.invoke(accessController);
            initAcls.setAccessible(false);
            Method getGroupsMethod = null;
            getGroupsMethod = AccessController.class.getDeclaredMethod("getGroups");
            getGroupsMethod.setAccessible(true);
            Hashtable<String, Vector<String>> usersAndGroups = (Hashtable<String, Vector<String>>) getGroupsMethod.invoke(accessController);
            getGroupsMethod.setAccessible(false);
            return usersAndGroups;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | AclAccessControllerInitializationException e) {
            throw new AclLibraryAccessControllerModifiedException("eionet.acl library AccessController.class method:getGroups is modified. Can not retrieve hashtable with users and their groups",e.getCause());
        }
    }

    protected AclProperties getAclProperties() throws AclPropertiesInitializationException {
        AclProperties aclProperties = new AclProperties();
        try {
            aclProperties.setOwnerPermission(getConfigurationPropertyResolver().resolveValue("owner.permission"));
            aclProperties.setAnonymousAccess(getConfigurationPropertyResolver().resolveValue("anonymous.access"));
            aclProperties.setAuthenticatedAccess(getConfigurationPropertyResolver().resolveValue("authenticated.access"));
            aclProperties.setDefaultdocPermissions(getConfigurationPropertyResolver().resolveValue("defaultdoc.permissions"));
            aclProperties.setPersistenceProvider(getConfigurationPropertyResolver().resolveValue("persistence.provider"));
            aclProperties.setInitialAdmin(getConfigurationPropertyResolver().resolveValue("initial.admin"));
            aclProperties.setFileAclfolder(getConfigurationPropertyResolver().resolveValue("file.aclfolder"));
            aclProperties.setFileLocalgroups(getConfigurationPropertyResolver().resolveValue("file.localgroups"));
            aclProperties.setFileLocalusers(getConfigurationPropertyResolver().resolveValue("file.localusers"));
            aclProperties.setFilePermissions(getConfigurationPropertyResolver().resolveValue("file.permissions"));
            aclProperties.setDbDriver(getConfigurationPropertyResolver().resolveValue("db.driver"));
            aclProperties.setDbUrl(getConfigurationPropertyResolver().resolveValue("db.url"));
            aclProperties.setDbUser(getConfigurationPropertyResolver().resolveValue("db.user"));
            aclProperties.setDbPwd(getConfigurationPropertyResolver().resolveValue("db.pwd"));
            return aclProperties;
        } catch (UnresolvedPropertyException | CircularReferenceException e) {
            LOGGER.error(e.getMessage(),e.getCause());

            throw new AclPropertiesInitializationException(e.getMessage(), e.getCause());
        }
    }

    protected AccessController getAclLibraryAccessControllerInstance(AclProperties aclProperties) throws AclAccessControllerInitializationException {
        try {
            AccessController accessController = new AccessController(aclProperties);
            return accessController;
        } catch (Exception e) {
            throw new AclAccessControllerInitializationException("Could not initialize eionet.acl AccessController:", e.getCause());
        }
    }

    public ConfigurationPropertyResolver getConfigurationPropertyResolver() {
        return configurationPropertyResolver;
    }

}