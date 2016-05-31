package eionet.datadict.services.impl;

import eionet.datadict.model.Attribute;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributeServiceImpl implements AttributeService {

    private final AclService aclService;
    private final AttributeDataService attributeDataService;
    
    @Autowired
    public AttributeServiceImpl(AclService aclService, AttributeDataService attributeDataService) {
        this.aclService = aclService;
        this.attributeDataService = attributeDataService;
    }
    
    @Override
    @Transactional
    public int save(Attribute attribute, DDUser user) throws UserAuthenticationException, UserAuthorizationException {
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        if (attribute.getId() == null) {
            return this.saveWithCreate(attribute, user);
        }
        else {
            return this.saveWithUpdate(attribute, user);
        }
    }
    
    protected int saveWithCreate(Attribute attribute, DDUser user) throws UserAuthorizationException {
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT)) {
            throw new UserAuthorizationException("You are not authorized to add new attributes.");
        }
        
        // TODO: input validation
        int newAttributeId = this.attributeDataService.createAttribute(attribute);
        this.aclService.grantAccess(user, AclEntity.ATTRIBUTE, this.getAttributeAclId(newAttributeId), "Short_name= " + attribute.getShortName());
        
        return newAttributeId;
    }
    
    protected int saveWithUpdate(Attribute attribute, DDUser user) throws UserAuthorizationException {
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, this.getAttributeAclId(attribute.getId()), Permission.UPDATE)) {
            String msg = String.format("You are not authorized to edit attribute %s.", attribute.getShortName());
            throw new UserAuthorizationException(msg);
        }
        
        // TODO: input validation
        this.attributeDataService.updateAttribute(attribute);
        
        return attribute.getId();
    }
    
    protected String getAttributeAclId(int attributeId) {
        return "s" + attributeId;
    }
    
}
