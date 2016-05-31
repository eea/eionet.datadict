package eionet.datadict.services.acl;

import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthorizationException;

public interface AclService {

    boolean hasPermission(DDUser user, AclEntity entity, Permission prm);
    
    boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm);
    
    void grantAccess(DDUser user, AclEntity entity, String entityId, String description) 
            throws UserAuthorizationException;
    
}
