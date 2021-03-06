package eionet.datadict.services.acl.impl;

import eionet.acl.AccessController;
import eionet.acl.AclProperties;
import eionet.datadict.errors.AclAccessControllerInitializationException;
import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class AclOperationsServiceTest {


    @Mock
    ConfigurationPropertyResolver configurationPropertyResolver;

    AclOperationsServiceImpl aclOperationsService;

    @Mock
    AclOperationsServiceImpl aclOperationsServiceMocked;

    @Before
    public void setUp() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        MockitoAnnotations.initMocks(this);
        this.aclOperationsService = new AclOperationsServiceImpl(configurationPropertyResolver);
        when(aclOperationsServiceMocked.getConfigurationPropertyResolver()).thenReturn(configurationPropertyResolver);
        when(aclOperationsServiceMocked.getRefreshedGroupsAndUsersHashTable(anyBoolean())).thenCallRealMethod();
    }


    @Test(expected = AclPropertiesInitializationException.class)
    public void testGetAclPropertiesThrowsAclPropertiesInitializationException() throws UnresolvedPropertyException, CircularReferenceException, AclPropertiesInitializationException {
        when(this.configurationPropertyResolver.resolveValue(any(String.class))).thenThrow(UnresolvedPropertyException.class);
        this.aclOperationsService.getAclProperties();
    }

    @Test
    public void testSucessGettingAclProperties() throws AclPropertiesInitializationException {
        final AclProperties aclProperties = mock(AclProperties.class);
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() {
                return aclProperties;
            }
        };
        assertThat(aclOperationsServiceImpl.getAclProperties(), equalTo(aclProperties));
    }


    @Test(expected = AclLibraryAccessControllerModifiedException.class)
    public void testGetGroupsAndUsersHashTableThrowsAclLibraryAccessControllerModifiedException() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        final AclProperties aclProperties = mock(AclProperties.class);
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() {
                return aclProperties;
            }

            @Override
            protected AccessController getAclLibraryAccessControllerInstance(AclProperties aclProperties) throws AclAccessControllerInitializationException {
                throw new AclAccessControllerInitializationException();
            }
        };
        aclOperationsServiceImpl.getRefreshedGroupsAndUsersHashTable(false);
    }


    @Test(expected = AclPropertiesInitializationException.class)
    public void testGetGroupsAndUsersHashTableThrowsAclPropertiesInitializationException() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() throws AclPropertiesInitializationException {
                throw new AclPropertiesInitializationException();
            }

            @Override
            protected AccessController getAclLibraryAccessControllerInstance(AclProperties aclProperties) throws AclAccessControllerInitializationException {
                throw new AclAccessControllerInitializationException();
            }
        };
        aclOperationsServiceImpl.getRefreshedGroupsAndUsersHashTable(false);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

}