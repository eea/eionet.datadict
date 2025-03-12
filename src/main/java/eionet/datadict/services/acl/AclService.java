package eionet.datadict.services.acl;

import eionet.datadict.errors.UserAuthorizationException;
import eionet.datadict.errors.UserExistsException;
import eionet.datadict.errors.XmlMalformedException;
import eionet.meta.DDUser;

public interface AclService {

    boolean hasPermission(DDUser user, AclEntity entity, Permission prm);
    
    boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm);
    
    void grantAccess(DDUser user, AclEntity entity, String entityId, String description) 
            throws UserAuthorizationException;
    
    void removeAccessRightsForDeletedEntity(AclEntity entity, String entityId);

    void addUserToAclGroup(String username,String groupName) throws UserExistsException, XmlMalformedException;

    void removeUserFromAclGroup(String userName, String groupName) throws XmlMalformedException;
}
