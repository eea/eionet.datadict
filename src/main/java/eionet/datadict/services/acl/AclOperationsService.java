package eionet.datadict.services.acl;


import eionet.acl.AccessController;
import eionet.acl.AclProperties;
import eionet.datadict.errors.AclAccessControllerInitializationException;
import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;

import java.util.Hashtable;
import java.util.Vector;

public interface AclOperationsService {

    Hashtable<String, Vector<String>> getGroupsAndUsersHashTable() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException;
}