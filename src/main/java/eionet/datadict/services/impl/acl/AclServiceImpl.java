package eionet.datadict.services.impl.acl;

import eionet.acl.AccessController;
import eionet.acl.SignOnException;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import org.springframework.stereotype.Service;

@Service
public class AclServiceImpl implements AclService {

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, Permission prm) {
        try {
            return SecurityUtil.hasPerm(this.getUserName(user), entity.getPath(), prm.getValue());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean hasPermission(DDUser user, AclEntity entity, String entityId, Permission prm) {
        try {
            return SecurityUtil.hasPerm(this.getUserName(user), this.getEntityPath(entity, entityId), prm.getValue());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void grantAccess(DDUser user, AclEntity entity, String entityId, String description) {
        try {
            AccessController.addAcl(this.getEntityPath(entity, entityId), this.getUserName(user), description);
        }
        catch (SignOnException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected String getUserName(DDUser user) {
        return user == null ? "" : user.getUserName();
    }
    
    protected String getEntityPath(AclEntity entity, String entityId) {
        return entity.getPath() + "/" + entityId;
    }
    
}
