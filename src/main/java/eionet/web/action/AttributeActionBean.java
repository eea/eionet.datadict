package eionet.web.action;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.acl.AclEntity;
import eionet.datadict.services.acl.AclService;
import eionet.datadict.services.acl.Permission;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.NamespaceDataService;
import eionet.datadict.services.data.RdfNamespaceDataService;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.FixedValue;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

@UrlBinding("/attribute/{$event}/{attribute.id}")
public class AttributeActionBean extends AbstractActionBean {

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
    private String vocabularyId;
    
    /**
     * Validates the attribute.id request parameter (must be a numeric value). 
     * 
     * @throws BadRequestException 
     */
    @Before(stages=LifecycleStage.BindingAndValidation, on={"save", "edit", "delete", "confirmDelete", "removeVocabularyBinding"})
    public void checkForNumericAttributeID() throws BadRequestException{
        String attributeId = this.getContext().getRequestParameter("attribute.id");
        if (!StringUtils.isNumeric(attributeId)) throw new BadRequestException(" Bad Request:"+ attributeId +" is not a proper identifier for attributes.");
    }
    
    //http://stripes-users.narkive.com/ZQ071553/repopulating-drop-downs-after-validation-error
    @Before(stages=LifecycleStage.BindingAndValidation, on={"add", "edit","save", "revmoveVocabularyBinding"})
    public void populateSelectListsInterceptor() {
        this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
    }
    
    /**
     * Handles requests for the view event. Is responsible for displaying info of an attribute with a given id.
     * 
     * @return A {@link ForwardResolution} to the jsp file responsible for displaying attribute information.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException
     * @throws BadRequestException 
     */
    @DefaultHandler
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
    
    /**
     * Handles requests for the add event. Is responsible for displaying a form for creating new attributes.
     * 
     * @return a {@link ForwardResolution} to the jsp file responsible for displaying the creation form.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
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
        
        if (this.namespaces==null || this.namespaces.isEmpty()){
            this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        }
        if (this.rdfNamespaces==null || this.rdfNamespaces.isEmpty()){
            this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        }
        
        return new ForwardResolution("/pages/attributes/attributeEditor.jsp");
    }
    
    /**
     * Handles requests for the edit event. Is responsible for displaying the edit page of an attribute with a given id.
     * 
     * @return a {@link ForwardResolution} to the attributeEditor.jsp file. 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException
     * @throws BadRequestException 
     */
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

        if (vocabularyId!= null) {
            attribute = attributeDataService.setNewVocabularyToAttributeObject(attribute, Integer.parseInt(vocabularyId));
        }
        
        if (this.namespaces==null || this.namespaces.isEmpty()) {
            this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        }
        
        if (this.rdfNamespaces==null || this.rdfNamespaces.isEmpty()) {
            this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        }
        
        return new ForwardResolution ("/pages/attributes/attributeEditor.jsp");
    }
    
    /**
     * Handle requests for the editVocabulary event. Is responsible for detecting whether the vocabulary binding to an attribute is to be updated.
     * 
     * @return
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException
     * @throws BadRequestException 
     */
    public Resolution editVocabulary() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        if (getRequestParameter("vocabularyId")!= null ) {
            vocabularyId = getRequestParameter("vocabularyId");
        }
        return edit();
    }
    
    /**
     * Handles requests for the save event. Is responsible for saving an attribute (either by creating a new one or updating an existing one).
     * 
     * @return A {@link RedirectResolution} to the view page of the saved attribute
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws BadRequestException 
     */
    public Resolution save() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        Thread.currentThread().setName("SAVE-ATTRIBUTE");
        ActionMethodUtils.setLogParameters(getContext());
        DDUser user = this.getUser();
        
        int attributeId = this.attributeService.save(attribute, user);
        
        if (this.namespaces==null || this.namespaces.isEmpty()){
            this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        }
        
        if (this.rdfNamespaces==null || this.rdfNamespaces.isEmpty()){
            this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        }
        
        return new RedirectResolution("/attribute/view/" + attributeId);
    }
    
    /**
     * Handles requests for the delete event. Is responsible for deleting an attribute with a given id.
     * 
     * @return A ${@link RedirectResolution} to the attributes page.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException 
     */
    public Resolution delete() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
       Thread.currentThread().setName("DELETE-ATTRIBUTE");
        ActionMethodUtils.setLogParameters(getContext());
        DDUser user = this.getUser();
       
       int attributeId = attribute.getId();
       
       if (!this.attributeDataService.existsAttribute(attributeId)) {
           throw new ResourceNotFoundException("Attribute with id: "+attributeId+" does not exist.");
       }
       
       this.attributeService.delete(attributeId, user);
       
       return new RedirectResolution("/attributes");
    }

    /**
     * Handles requests for the confirmDelete event. Is responsible for searching for dependencies between an attribute to be deleted
     * and other DataDict entities. If dependencies are found a confirmation page is provided, otherwise the delete method is called.
     * 
     * @return A {@link ForwardResolution} if confirmation is needed or a {@link RedirectResolution} if delete() is to be called.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException 
     */
    public Resolution confirmDelete() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException {
        DDUser user = this.getUser();
        
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete attrubutes.");
        }
        
        int attributeId = attribute.getId();
        
        if (!this.aclService.hasPermission(user, AclEntity.ATTRIBUTE, "s"+attributeId, Permission.DELETE)) {
            throw new UserAuthorizationException("You are not authorized to delete this attribute.");
        }
        
        if (!this.attributeDataService.existsAttribute(attributeId)) {
            throw new ResourceNotFoundException("Attribute with id: "+attributeId+" does not exist.");
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
    
    /**
     * Handles requests for the reset event. Reloads information of an attribute with the given id.
     * 
     * @return A {@link RedirectResolution} to the edit page.
     */
    public Resolution reset() {
        return new RedirectResolution("/attribute/edit/"+attribute.getId());
    }
     
    /**
     * Handles requests for the removeVocabularyBindig event. Is responsible for removing a vocabulary binding of an attribute with the given id.
     * 
     * @return A {@link  ForwardResolution} to the edit page.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException 
     */
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
        
        if (this.namespaces==null || this.namespaces.isEmpty()){
            this.namespaces = this.namespaceDataService.getAttributeNamespaces();
        }
        
        if (this.rdfNamespaces==null || this.rdfNamespaces.isEmpty()){
            this.rdfNamespaces = this.rdfNamespaceDataService.getRdfNamespaces();
        }
        
        return new ForwardResolution ("/pages/attributes/attributeEditor.jsp");
    }

    protected String getRequestParameter(String paramName) {
        return this.getContext().getRequestParameter(paramName);
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
 
    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

}
