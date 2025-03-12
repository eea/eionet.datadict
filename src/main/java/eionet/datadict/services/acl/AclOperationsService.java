package eionet.datadict.services.acl;

import eionet.datadict.errors.AclLibraryAccessControllerModifiedException;
import eionet.datadict.errors.AclPropertiesInitializationException;

import java.util.Hashtable;
import java.util.Vector;

public interface AclOperationsService {

    Hashtable<String, Vector<String>> getRefreshedGroupsAndUsersHashTable(boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException;
}