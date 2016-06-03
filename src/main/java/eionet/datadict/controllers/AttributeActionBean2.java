package eionet.datadict.controllers;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.resources.ResourceDbIdInfo;
import eionet.datadict.resources.ResourceType;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.NamespaceDataService;
import eionet.datadict.services.data.RdfNamespaceDataService;
import eionet.meta.DDUser;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.dao.domain.FixedValue;
import eionet.web.action.AbstractActionBean;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ScopedLocalizableError;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

@UrlBinding("/attribute/{$event}/{attribute.id}")
public class AttributeActionBean2 extends AbstractActionBean implements ValidationErrorHandler {

    @SpringBean
    private AclService aclService;
    @SpringBean
    private AttributeService attributeService;
    @SpringBean
    private AttributeDataService attributeDataService;
    @SpringBean
    private RdfNamespaceDataService rdfNamespaceDataService;
    @SpringBean
    private NamespaceDataService namespaceDataService;
    
    private Attribute attribute;
    private List<Namespace> namespaces;
    private List<RdfNamespace> rdfNamespaces;
    private List<FixedValue> fixedValues;
    private Map<DataDictEntity.Entity, Integer> entityTypesWithAttributeValues;
    private int attributeValuesCount;
    
    public Resolution view() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to view attributes.");
        }
        
        if (attribute.getId()==null) {
            throw new BadRequestException("Attribute is not specified");
        }
        
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, "s"+attribute.getId(), Permission.VIEW)) {
            throw new UserAuthorizationException("You are not authorized to view this attribute");
        }

        this.attribute = this.attributeDataService.getAttribute(attribute.getId());
        this.fixedValues = this.attributeDataService.getFixedValues(attribute.getId());
        return new ForwardResolution("/pages/attributes/viewAttribute.jsp");
    }
    
    public Resolution add() throws UserAuthenticationException, UserAuthorizationException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, Permission.INSERT)) {
            throw new UserAuthorizationException("You are not authorized to add new attributes.");
        }
        
        this.attribute = new Attribute();
        this.attribute.setValueInheritanceMode(Attribute.ValueInheritanceMode.NONE);
        this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        
        return new ForwardResolution("/pages/attributes/attributeEditor.jsp");
    }
    
    public Resolution edit() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add/edit attributes.");
        }
        
        if (attribute.getId()==null) {
            throw new BadRequestException("Attribute is not specified");
        }
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, "s"+attribute.getId(), Permission.UPDATE)) {
            throw new UserAuthorizationException ("You are not authorized to edit this attribute");
        } 

        attribute = attributeDataService.getAttribute(attribute.getId());
        
        //in case vocabularyId has been edited but not yet saved
        if (this.getContext().getRequestParameter("vocabularyId")!= null) {
            String vocabularyId = this.getContext().getRequestParameter("vocabularyId");
            attribute = attributeDataService.setNewVocabularyToAttributeObject(attribute, Integer.parseInt(vocabularyId));
        }
        
        this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        return new ForwardResolution ("/pages/attributes/attributeEditor.jsp");
    }
    
    public Resolution save() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = this.getUser();
        int attributeId = this.attributeService.save(attribute, user);
        
        return new RedirectResolution("/attribute/view/" + attributeId);
    }
    
    public Resolution delete() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
       DDUser user = this.getUser();
       
       int attributeId = attribute.getId();
       if (!this.attributeDataService.exists(attributeId)) {
           throw new ResourceNotFoundException(ResourceType.ATTRIBUTE, new ResourceDbIdInfo(attributeId));
       }
       
       this.attributeService.delete(attributeId, user);
       
       return new ForwardResolution("attributes.jsp");
    }
    
    public Resolution confirmDelete() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete attrubutes.");
        }
        
        int attributeId = attribute.getId();
        
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, "s"+attributeId, Permission.INSERT)) {
            throw new UserAuthorizationException("You are not authorized to delete this attribute.");
        }
        if (!this.attributeDataService.exists(attributeId)) {
            throw new ResourceNotFoundException(ResourceType.ATTRIBUTE, new ResourceDbIdInfo(attributeId));
        }
        attributeValuesCount = this.attributeDataService.countAttributeValues(attributeId);
        
        if (attributeValuesCount>0) {
            entityTypesWithAttributeValues = this.attributeDataService.getDistinctTypesWithAttributeValues(attributeId);
            return new ForwardResolution("/pages/attributes/confirmDelete.jsp");
        } 
        else {
           return delete();
        }
    }
    
    public Resolution reset(){
        return new RedirectResolution("/attribute/edit/"+attribute.getId());
    }
    
    public Resolution removeVocabularyBinding() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to edit attrubutes.");
        }
        
        if(!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, "s"+attribute.getId(), Permission.UPDATE)){
            throw new UserAuthorizationException("You are not allowed to edit this attribute");
        }
        
        this.attribute = attributeDataService.getAttribute(attribute.getId());
        this.attribute.setVocabulary(null);
        this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        return new ForwardResolution ("/pages/attributes/attributeEditor.jsp");
    }
    
    @Override
    public Resolution handleValidationErrors(ValidationErrors ve) throws Exception {
        if (ve.get("attribute.id")!=null) {
           for (ValidationError error: ve.get("attribute.id")) {
               if (error.getClass() == ScopedLocalizableError.class) {
                   //Error: non-numeric value used for numeric field
                   if (((ScopedLocalizableError)error).getKey().equals("invalidNumber")){
                       String nonNumericValue = error.getFieldValue();
                       throw new BadRequestException("Bad Request: "+nonNumericValue+" is not a proper identifier for attributes.");
                   }
                       
               }
           }
        }
        return null;
    }

    // ------ ActionBean bean access ---------
    
    public AclService getAclService() {
        return aclService;
    }

    public void setAclService(AclService aclService) {
        this.aclService = aclService;
    }

    public AttributeService getAttributeService() {
        return attributeService;
    }

    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }
    
    public RdfNamespaceDataService getRdfNamespaceDataService() {
        return rdfNamespaceDataService;
    }

    public void setRdfNamespaceDataService(RdfNamespaceDataService rdfNamespaceDataService) {
        this.rdfNamespaceDataService = rdfNamespaceDataService;
    }

    public NamespaceDataService getNamespaceDataService() {
        return namespaceDataService;
    }

    public void setNamespaceDataService(NamespaceDataService namespaceDataService) {
        this.namespaceDataService = namespaceDataService;
    }

    public AttributeDataService getAttributeDataService() {
        return attributeDataService;
    }

    public void setAttributeDataService(AttributeDataService attributeDataService) {
        this.attributeDataService = attributeDataService;
    }
  
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public List<RdfNamespace> getRdfNamespaces() {
        return rdfNamespaces;
    }

    public void setRdfNamespaces(List<RdfNamespace> rdfNamespaces) {
        this.rdfNamespaces = rdfNamespaces;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public int getAttributeValuesCount() {
        return attributeValuesCount;
    }

    public void setAttributeValuesCount(int attributeValuesCount) {
        this.attributeValuesCount = attributeValuesCount;
    }

    public Map<DataDictEntity.Entity, Integer> getEntityTypesWithAttributeValues() {
        return entityTypesWithAttributeValues;
    }

    public void setEntityTypesWithAttributeValues(Map<DataDictEntity.Entity, Integer> entityTypesWithAttributeValues) {
        this.entityTypesWithAttributeValues = entityTypesWithAttributeValues;
    }

}
