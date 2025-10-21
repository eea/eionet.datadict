package eionet.datadict.services.impl;

import eionet.datadict.errors.*;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.VocabularyConcept;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void delete(int attributeId, DDUser user) throws UserAuthenticationException, UserAuthorizationException {

        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete attributes.");
        }

        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, this.getAttributeAclId(attributeId), Permission.DELETE)) {
            throw new UserAuthorizationException("You are not authorized to delete this attribute.");
        }

        this.attributeDataService.deleteAttributeById(attributeId);
        this.aclService.removeAccessRightsForDeletedEntity(AclEntity.ATTRIBUTE, this.getAttributeAclId(attributeId));
    }

    @Override
    @Transactional
    public int save(Attribute attribute, DDUser user) throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        if (attribute.getId() == null) {
            return this.saveWithCreate(attribute, user);
        } else {
            return this.saveWithUpdate(attribute, user);
        }
    }
    
    @Override
    public void deleteAttributeValue(int attributeId, DataDictEntity ownerEntity, String value, DDUser user) 
            throws UserAuthenticationException, UserAuthorizationException {
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete attribute values.");
        }
        this.attributeDataService.deleteAttributeValue(attributeId, ownerEntity, value);
    }
    
    @Override
    public void deleteAllAttributeValues(int attributeId, DataDictEntity ownerEntity, DDUser user) throws UserAuthenticationException, UserAuthorizationException {
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete attribute values.");
        }
        this.attributeDataService.deleteAllAttributeValues(attributeId, ownerEntity);
    }
    
    @Override
    @Transactional
    public List<VocabularyConcept> getAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity, ValueInheritanceMode inheritanceMode) 
            throws ResourceNotFoundException, EmptyParameterException {
        Integer vocabularyId = attributeDataService.getVocabularyBinding(attributeId);
        if (vocabularyId != null) {
             List<VocabularyConcept> vocabularyConcepts = attributeDataService.getVocabularyConceptsAsAttributeValues(attributeId, ddEntity, inheritanceMode);
             return vocabularyConcepts;
        }
        return null;
    }

    @Override
    @Transactional
    public List<VocabularyConcept> getInherittedAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity) throws ResourceNotFoundException, EmptyParameterException {
        Integer vocabularyId = attributeDataService.getVocabularyBinding(attributeId);
        if (vocabularyId != null) {
            List<VocabularyConcept> vocabularyConcepts = attributeDataService.getVocabularyConceptsAsInheritedAttributeValues(attributeId, ddEntity);
            return vocabularyConcepts;
        }
        return null;
    }
    
    protected int saveWithCreate(Attribute attribute, DDUser user) throws UserAuthorizationException, BadRequestException {
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT)) {
            throw new UserAuthorizationException("You are not authorized to add new attributes.");
        }
        
        validateMandatoryAttributeFields(attribute);
        
        int newAttributeId = this.attributeDataService.createAttribute(attribute);
        this.aclService.grantAccess(user, AclEntity.ATTRIBUTE, this.getAttributeAclId(newAttributeId), "Short_name= " + attribute.getShortName());

        return newAttributeId;
    }

    protected int saveWithUpdate(Attribute attribute, DDUser user) throws UserAuthorizationException, BadRequestException {
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, this.getAttributeAclId(attribute.getId()), Permission.UPDATE)) {
            String msg = String.format("You are not authorized to edit attribute %s.", attribute.getShortName());
            throw new UserAuthorizationException(msg);
        }

        validateMandatoryAttributeFields(attribute);
        removeIncompatibleOldValues(attribute);
       
        this.attributeDataService.updateAttribute(attribute);

        return attribute.getId();
    }

    protected String getAttributeAclId(int attributeId) {
        return "s" + attributeId;
    }
    
    protected void validateMandatoryAttributeFields(Attribute attribute) throws BadRequestException {
         if (StringUtils.isEmpty(attribute.getShortName()) ||
                StringUtils.isEmpty(attribute.getName()) ||
                attribute.getNamespace() == null ||
                attribute.getNamespace().getId() == null ||
                attribute.getObligationType() == null)
        {
            throw new BadRequestException ("One of the mandatory fields are missing! Attribute cannot be saved.");
        }
    }
    
    protected void removeIncompatibleOldValues(Attribute attribute) throws ResourceNotFoundException {
        Attribute oldAttribute = attributeDataService.getAttribute(attribute.getId());
        if (oldAttribute.getDisplayType() == Attribute.DisplayType.VOCABULARY && attribute.getDisplayType() != Attribute.DisplayType.VOCABULARY) {
            this.attributeDataService.deleteAllAttributeValues(attribute.getId());
        }
        if(oldAttribute.getDisplayType() == Attribute.DisplayType.VOCABULARY && attribute.getDisplayType() == Attribute.DisplayType.VOCABULARY) {
            if (oldAttribute.getVocabulary() != null && attribute.getVocabulary()!=null && oldAttribute.getVocabulary().getId() != attribute.getVocabulary().getId()) {
                 this.attributeDataService.deleteAllAttributeValues(attribute.getId());
            }
            if (oldAttribute.getVocabulary()!=null && attribute.getVocabulary() == null){
                this.attributeDataService.deleteAllAttributeValues(attribute.getId());
            }
        }
    }

}
