package eionet.datadict.services.acl;


import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;

public interface AclOperationsService {

    public void reinitializeAclRights() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException;
}