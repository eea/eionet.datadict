package eionet.web.action;

import eionet.datadict.errors.BadRequestException;
import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.Dataset;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.DataElementDataService;
import eionet.datadict.services.data.DatasetDataService;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.DDUser;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.lang.StringUtils;


@UrlBinding("/vocabularyvalues/attribute/{attributeId}/{attrOwnerType}/{attrOwnerId}")
public class AttrVocabularyValuesActionBean extends AbstractActionBean {
    
    private final static String ATTRIBUTE_VOCABULARY_VALUES = "/pages/attributes/attributeVocabularyValues.jsp";
    private final static String ATTRIBUTE_VOCABULARY_VALUES_ADD = "/pages/attributes/addAttributeVocabularyValue.jsp";
    
    // block of post parameters
    private String conceptIdentifier;
    private String vocabularyId;
    
    // block of url parameters
    private String attributeId;
    private String attrOwnerType;
    private String attrOwnerId;
    private String currentSection;
    
    private DataDictEntity attributeOwnerEntity;
    private List<VocabularyConcept> vocabularyConcepts;
    
    // Only one of them points at a real object (depending on what is the type
    // of the owner of the attribute)
    private Dataset dataset;
    private DatasetTable datasetTable;
    private DataElement dataElement;
    
    // The Attribute object
    private Attribute attribute;
    
    // Services
    @SpringBean
    private AttributeService attributeService;
    @SpringBean
    private AttributeDataService attributeDataService;
    @SpringBean
    private VocabularyDataService vocabularyDataService;
    @SpringBean
    private DatasetDataService datasetDataService;
    @SpringBean
    private DatasetTableDataService datasetTableDataService;
    @SpringBean
    private DataElementDataService dataElementDataService;
    
    
    //-----------------Handlers---------------------------
    
    /**
     * Handles requests for managing the attribute-vocabulary values.
     * 
     * @return a {@link ForwardResolution} to the page which handles the attribute-vocabulary values for a specific datadict entity.
     * 
     * @throws ResourceNotFoundException
     * @throws EmptyParameterException 
     * @throws eionet.datadict.errors.UserAuthenticationException 
     * @throws eionet.datadict.errors.UserAuthorizationException 
     */
    @DefaultHandler
    public Resolution manageValues() throws ResourceNotFoundException, EmptyParameterException, UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = this.getUser();
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to access the vocabulary attribute values page.");
        }
        
        if (DataDictEntity.Entity.getFromString(attrOwnerType) == null) {
            throw new BadRequestException("Malformed request. " + attrOwnerType + " is not a valid datadict entity type.");
        }
        
        try {
            Integer.parseInt(attributeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attributeId + " is not a valid attribute id.");
        }
        
        try {
            Integer.parseInt(attrOwnerId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attrOwnerId + " is not a valid datadict entity id.");
        }
        
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
        
        if (attrOwnerType.equals("dataelement")) {
            configureDataElement();
            if (!this.dataElementDataService.isWorkingUser(dataElement, user)) {
                throw new UserAuthorizationException("You are not authorized to edit this data element.");
            }
        } else if (attrOwnerType.equals("table")) {
            configureTable();
            if (!this.datasetTableDataService.isWorkingCopy(datasetTable, user)){
                throw new UserAuthorizationException("You are not authorized to edit this table.");
            }   
        } else if (attrOwnerType.equals("dataset")) {
            configureDataset();
            if (!this.dataset.getWorkingCopy() || (this.dataset.getWorkingUser() != null && !this.dataset.getWorkingUser().equals(user.getUserName()))) {
                throw new UserAuthorizationException("You are not authorized to edit this dataset.");
            }
        }
        
        this.attribute = attributeDataService.getAttribute(Integer.parseInt(attributeId));
        
        if(attribute.getVocabulary() == null) {
            throw new BadRequestException("No vocabulary binding exists for this attribute.");
        }
        this.vocabularyConcepts = this.attributeDataService.getVocabularyConceptsAsOriginalAttributeValues(
                attribute.getVocabulary().getId(), attribute.getId(), attributeOwnerEntity);
        
        return new ForwardResolution(ATTRIBUTE_VOCABULARY_VALUES);
    }
    
    /**
     * Handles requests for adding attribute-vocabulary values.
     * 
     * @return a {@link ForwardResolution} to the page responsible for adding attribute-vocabulary values.
     * 
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException
     * @throws ResourceNotFoundException 
     */
    public Resolution add() throws UserAuthenticationException, UserAuthorizationException, ResourceNotFoundException, BadRequestException {
        DDUser user = this.getUser();
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to add a vocabulary attribute value.");
        }
        
        if (DataDictEntity.Entity.getFromString(attrOwnerType) == null) {
            throw new BadRequestException("Malformed request. " + attrOwnerType + " is not a valid datadict entity type.");
        }
        
        try {
            Integer.parseInt(attributeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attributeId + " is not a valid attribute id.");
        }
        
        try {
            Integer.parseInt(attrOwnerId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attrOwnerId + " is not a valid datadict entity id.");
        }
        
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
        
        if (attrOwnerType.equals("dataelement")) {
            configureDataElement();
            if (!this.dataElementDataService.isWorkingUser(dataElement, user)) {
                throw new UserAuthorizationException("You are not authorized to edit this data element.");
            }
        } else if (attrOwnerType.equals("table")) {
            configureTable();
            if (!this.datasetTableDataService.isWorkingCopy(datasetTable, user)){
                throw new UserAuthorizationException("You are not authorized to edit this table.");
            }
        } else if (attrOwnerType.equals("dataset")) {
            configureDataset();
            if (!this.dataset.getWorkingCopy() || this.dataset.getWorkingUser() != null && !this.dataset.getWorkingUser().equals(user.getUserName())) {
                throw new UserAuthorizationException("You are not authorized to edit this dataset.");
            }
        }
        
        this.attribute = attributeDataService.getAttribute(Integer.parseInt(attributeId));
        this.vocabularyConcepts = this.vocabularyDataService.getVocabularyConcepts(attribute.getVocabulary().getId(), StandardGenericStatus.ACCEPTED);
        Collections.sort(vocabularyConcepts, new Comparator<VocabularyConcept>() {
            @Override
            public int compare(VocabularyConcept c1, VocabularyConcept c2) {
                return attribute.getVocabulary().isNumericConceptIdentifiers() ?
                        Long.valueOf(c1.getIdentifier()).compareTo(Long.valueOf(c2.getIdentifier())) :
                        c1.getIdentifier().compareTo(c2.getIdentifier());
            }
        });

        return new ForwardResolution(ATTRIBUTE_VOCABULARY_VALUES_ADD);
    }
    
    /**
     * Handles post requests for adding a new attribute-vocabulary value.
     * 
     * @return a {@link RedirectResolution} to the default page of the attribute-vocabulary value handler.
     * 
     * @throws UserAuthorizationException
     * @throws UserAuthenticationException
     * @throws ConflictException 
     */
    public Resolution saveAdd() throws UserAuthorizationException, UserAuthenticationException, ConflictException, BadRequestException {
        DDUser user = this.getUser();
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to save a vocabulary attribute value.");
        }
        
        if (DataDictEntity.Entity.getFromString(attrOwnerType) == null) {
            throw new BadRequestException("Malformed request. " + attrOwnerType + " is not a valid datadict entity type.");
        }
        
        try {
            Integer.parseInt(attributeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attributeId + " is not a valid attribute id.");
        }
        
        try {
            Integer.parseInt(attrOwnerId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attrOwnerId + " is not a valid datadict entity id.");
        }
        
        if (StringUtils.isEmpty(conceptIdentifier)) {
            throw new BadRequestException("Malformed request: Attribute value missing.");
        }
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType)); 
        this.attributeService.saveAttributeVocabularyValue(Integer.parseInt(attributeId), attributeOwnerEntity, conceptIdentifier, user);
        return new RedirectResolution("/vocabularyvalues/attribute/" + attributeId + "/" + attrOwnerType + "/"+attrOwnerId);
    }
    
    /**
     * Handles post requests for deleting an attribute-vocabulary value.
     * 
     * @return a {@link RedirectResolution} to the default page of the attribute-vocabulary value handler.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public Resolution delete() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = this.getUser();
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete a vocabulary attribute value.");
        }
        
        if (DataDictEntity.Entity.getFromString(attrOwnerType) == null) {
            throw new BadRequestException("Malformed request. " + attrOwnerType + " is not a valid datadict entity type.");
        }
        
        try {
            Integer.parseInt(attributeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attributeId + " is not a valid attribute id.");
        }
        
        try {
            Integer.parseInt(attrOwnerId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attrOwnerId + " is not a valid datadict entity id.");
        }
        
        if (StringUtils.isEmpty(conceptIdentifier)) {
            throw new BadRequestException("Malformed request: Attribute value missing.");
        }
        
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
        this.attributeService.deleteAttributeValue(Integer.parseInt(attributeId), attributeOwnerEntity, conceptIdentifier, user);
        return new RedirectResolution("/vocabularyvalues/attribute/" + attributeId + "/" + attrOwnerType + "/" + attrOwnerId);
    }
    
    /**
     * Handles post requests for deleting all attribute-vocabulary values.
     * 
     * @return a {@link RedirectResolution} to the default page of the attribute-vocabulary value handler.
     * @throws UserAuthenticationException
     * @throws UserAuthorizationException 
     */
    public Resolution deleteAll() throws UserAuthenticationException, UserAuthorizationException, BadRequestException {
        DDUser user = this.getUser();
        if (user == null) {
            throw new UserAuthenticationException("You must be signed in in order to delete vocabulary attribute values.");
        }
        
        if (DataDictEntity.Entity.getFromString(attrOwnerType) == null) {
            throw new BadRequestException("Malformed request. " + attrOwnerType + " is not a valid datadict entity type.");
        }
        
        try {
            Integer.parseInt(attributeId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attributeId + " is not a valid attribute id.");
        }
        
        try {
            Integer.parseInt(attrOwnerId);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Malformend request. " + attrOwnerId + " is not a valid datadict entity id.");
        }
        
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
        this.attributeService.deleteAllAttributeValues(Integer.parseInt(attributeId), attributeOwnerEntity, user);
        return new RedirectResolution("/vocabularyvalues/attribute/" + attributeId + "/" + attrOwnerType + "/" + attrOwnerId);
    }
    
    
    //---------------- Attribute-Vocabulary value owners specific methods ----------------------------
    
    protected void configureDataset() throws ResourceNotFoundException {
        this.currentSection = "datasets";
        this.dataset = this.datasetDataService.getDataset(this.attributeOwnerEntity.getId());
    }
    
    protected void configureDataElement() throws ResourceNotFoundException {
        this.currentSection = "dataElements";
        this.dataElement = this.dataElementDataService.getDataElement(this.attributeOwnerEntity.getId());
    }
    
    protected void configureTable() throws ResourceNotFoundException {
        this.currentSection = "tables";
        this.datasetTable = this.datasetTableDataService.getDatasetTable(this.attributeOwnerEntity.getId());
    }
    
    //----------------- Getters and Setters ------------------------------
    
    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public DatasetTable getDatasetTable() {
        return datasetTable;
    }

    public void setDatasetTable(DatasetTable datasetTable) {
        this.datasetTable = datasetTable;
    }

    public DataElement getDataElement() {
        return dataElement;
    }

    public void setDataElement(DataElement dataElement) {
        this.dataElement = dataElement;
    }
    
    public DataDictEntity getAttributeOwnerEntity() {
        return attributeOwnerEntity;
    }

    public void setAttributeOwnerEntity(DataDictEntity attributeOwnerEntity) {
        this.attributeOwnerEntity = attributeOwnerEntity;
    }

    public String getAttrOwnerType() {
        return attrOwnerType;
    }

    public void setAttrOwnerType(String attrOwnerType) {
        this.attrOwnerType = attrOwnerType;
    }

    public String getAttrOwnerId() {
        return attrOwnerId;
    }

    public void setAttrOwnerId(String attrOwnerId) {
        this.attrOwnerId = attrOwnerId;
    }

    public String getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(String currentSection) {
        this.currentSection = currentSection;
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
    
    public List<VocabularyConcept> getVocabularyConcepts() {
        return vocabularyConcepts;
    }

    public String getConceptIdentifier() {
        return conceptIdentifier;
    }

    public void setConceptIdentifier(String conceptIdentifier) {
        this.conceptIdentifier = conceptIdentifier;
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }
    
}
