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
import eionet.meta.DDUser;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptBoundElementFilter;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;


@UrlBinding("/vocabularyvalues/attribute/{attributeId}/{attrOwnerType}/{attrOwnerId}")
public class AttrVocabularyValuesActionBean extends AbstractActionBean {
    
    private final static String ATTRIBUTE_VOCABULARY_VALUES = "/pages/attributes/attributeVocabularyValues.jsp";
    private final static String ATTRIBUTE_VOCABULARY_VALUES_ADD = "/pages/attributes/addAttributeVocabularyValue.jsp";
    
    // block of post parameters
    private List<String> conceptIds = new ArrayList<String>();

    // block of url parameters
    private String attributeId;
    private String attrOwnerType;
    private String attrOwnerId;
    private String currentSection;
    
    private DataDictEntity attributeOwnerEntity;
    private List<VocabularyConcept> vocabularyConcepts;
    private VocabularyConceptResult vocabularyConceptResult;

    // Only one of them points at a real object (depending on what is the type
    // of the owner of the attribute)
    private Dataset dataset;
    private DatasetTable datasetTable;
    private DataElement dataElement;
    private SchemaSet schemaSet;
    private Schema schema;
    private VocabularyFolder vocabularyFolder;

    // The Attribute object
    private Attribute attribute;
    
    // Services
    @SpringBean
    private AttributeService attributeService;
    @SpringBean
    private AttributeDataService attributeDataService;
    @SpringBean
    private IVocabularyService vocabularyService;
    @SpringBean
    private DatasetDataService datasetDataService;
    @SpringBean
    private DatasetTableDataService datasetTableDataService;
    @SpringBean
    private DataElementDataService dataElementDataService;
    @SpringBean
    private ISchemaService schemaService;

    /**
     * Vocabulary concept filter.
     */
    private VocabularyConceptFilter filter;

    /**
     * Vocabulary bound data elements.
     */
    private List<eionet.meta.dao.domain.DataElement> boundElements;

    /**
     * List of bound element filters
     */
    private List<VocabularyConceptBoundElementFilter> boundElementFilters = new ArrayList<VocabularyConceptBoundElementFilter>();

    /**
     * Concepts table page number.
     */
    private int page = 1;


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
        } else if (attrOwnerType.equals("schemaset")) {
            configureSchemaSet();
            if (!this.schemaSet.isWorkingCopy() || (this.schemaSet.getWorkingUser() != null && !this.schemaSet.getWorkingUser().equals(user.getUserName()))) {
                throw new UserAuthorizationException("You are not authorized to edit this schema set.");
            }
        } else if (attrOwnerType.equals("schema")) {
            configureSchema();
            // check for root level schemas
            if (this.schema.getSchemaSetId() <= 0 && 
                    (!this.schema.isWorkingCopy() || (this.schema.getWorkingUser() != null && !this.schema.getWorkingUser().equals(user.getUserName())))) {
                throw new UserAuthorizationException("You are not authorized to edit this schema.");
            }
        } else if (attrOwnerType.equals("vocabulary")) {
            configureVocabulary();
            if (!this.vocabularyFolder.isWorkingCopy() || (this.vocabularyFolder.getWorkingUser() != null && !this.vocabularyFolder.getWorkingUser().equals(user.getUserName()))) {
                throw new UserAuthorizationException("You are not authorized to edit this vocabulary.");
            }
        }

        this.attribute = attributeDataService.getAttribute(Integer.parseInt(attributeId));
        
        if(attribute.getVocabulary() == null) {
            throw new BadRequestException("No vocabulary binding exists for this attribute.");
        }
        this.vocabularyConcepts = this.attributeDataService.getVocabularyConceptsAsOriginalAttributeValues(attribute.getId(), attributeOwnerEntity);
        
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
        } else if (attrOwnerType.equals("schemaset")) {
            configureSchemaSet();
            if (!this.schemaSet.isWorkingCopy() || (this.schemaSet.getWorkingUser() != null && !this.schemaSet.getWorkingUser().equals(user.getUserName()))) {
                throw new UserAuthorizationException("You are not authorized to edit this schema set.");
            }
        } else if (attrOwnerType.equals("schema")) {
            configureSchema();
            // check for root level schemas
            if (this.schema.getSchemaSetId() <= 0 && 
                    (!this.schema.isWorkingCopy() || (this.schema.getWorkingUser() != null && !this.schema.getWorkingUser().equals(user.getUserName())))) {
                throw new UserAuthorizationException("You are not authorized to edit this schema.");
            }
        } else if (attrOwnerType.equals("vocabulary")) {
            configureVocabulary();
            if (!this.vocabularyFolder.isWorkingCopy() || (this.vocabularyFolder.getWorkingUser() != null && !this.vocabularyFolder.getWorkingUser().equals(user.getUserName()))) {
                throw new UserAuthorizationException("You are not authorized to edit this vocabulary.");
            }
        }

        this.attribute = attributeDataService.getAttribute(Integer.parseInt(attributeId));
        
        try {
            boundElements = vocabularyService.getVocabularyDataElements(attribute.getVocabulary().getId());
            Collections.sort(boundElements, new Comparator<eionet.meta.dao.domain.DataElement>() {
                @Override
                public int compare(eionet.meta.dao.domain.DataElement o1, eionet.meta.dao.domain.DataElement o2) {
                    return (o1.getIdentifier().compareToIgnoreCase(o2.getIdentifier()));
                }
            });
            
            initFilter();
            vocabularyConceptResult = vocabularyService.searchVocabularyConcepts(filter);
        } catch(ServiceException ex) {
            throw new RuntimeException();
        }

        return new ForwardResolution(ATTRIBUTE_VOCABULARY_VALUES_ADD);
    }

    public List<Integer> getBoundElementFilterIds() {
        List<Integer> boundElementFilterIds = new ArrayList<Integer>();
        for (VocabularyConceptBoundElementFilter currentFilter : boundElementFilters) {
            boundElementFilterIds.add(currentFilter.getId());
        }
        return boundElementFilterIds;
    }

    private void initFilter() {
        if (filter == null) {
            filter = new VocabularyConceptFilter();
            filter.setConceptStatus(StandardGenericStatus.ACCEPTED);
        }
        filter.setVocabularyFolderId(attribute.getVocabulary().getId());
        filter.setPageNumber(page);
        filter.setNumericIdentifierSorting(attribute.getVocabulary().isNumericConceptIdentifiers());

        if (!filter.getBoundElementIds().isEmpty()) {
            List<Integer> cids = vocabularyService.getVocabularyConceptIds(attribute.getVocabulary().getId());
            for (Integer boundElementId : filter.getBoundElementIds()) {
                boundElementFilters.add(vocabularyService.getVocabularyConceptBoundElementFilter(boundElementId, cids));
            }
        }
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
        
        if (!conceptIds.isEmpty()) {
            this.attribute = attributeDataService.getAttribute(Integer.parseInt(attributeId));
            if (attribute.getVocabulary() == null) {
                throw new BadRequestException("No vocabulary binding exists for this attribute.");
            }
            this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
            this.vocabularyConcepts = this.attributeDataService.getVocabularyConceptsAsOriginalAttributeValues(attribute.getId(), attributeOwnerEntity);

            Set<String> noDuplicateConceptIds = new LinkedHashSet<String>(conceptIds);
            Set<String> existingIds = new HashSet<String>();
            for (Iterator<VocabularyConcept> it = vocabularyConcepts.iterator(); it.hasNext();) {
                VocabularyConcept concept = it.next();
                existingIds.add(String.valueOf(concept.getId()));
            }

            noDuplicateConceptIds.removeAll(existingIds);
            conceptIds = new ArrayList(noDuplicateConceptIds);

            this.attributeDataService.createAttributeValues(Integer.parseInt(attributeId), attributeOwnerEntity, conceptIds);
        }
        return new RedirectResolution("/vocabularyvalues/attribute/" + attributeId + "/" + attrOwnerType + "/" + attrOwnerId);
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
        
        if (conceptIds.isEmpty()) {
            throw new BadRequestException("Malformed request: Attribute value missing.");
        }
        
        this.attributeOwnerEntity = new DataDictEntity(Integer.parseInt(attrOwnerId), DataDictEntity.Entity.getFromString(attrOwnerType));
        this.attributeService.deleteAttributeValue(Integer.parseInt(attributeId), attributeOwnerEntity, conceptIds.iterator().next(), user);
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

    protected void configureSchemaSet() throws ResourceNotFoundException {
        this.currentSection = "schemas";
        try {
            this.schemaSet = this.schemaService.getSchemaSet(this.attributeOwnerEntity.getId());
        } catch (ServiceException ex) {
            throw new ResourceNotFoundException("Schema set with id: " + this.attributeOwnerEntity.getId() + " does not exist.", ex);
        }
    }

    protected void configureSchema() throws ResourceNotFoundException {
        this.currentSection = "schemas";
        try {
            this.schema = this.schemaService.getSchema(this.attributeOwnerEntity.getId());
            if (this.schema != null && this.schema.getSchemaSetId() > 0) {
                SchemaSet parent = schemaService.getSchemaSet(schema.getSchemaSetId());
                if (parent != null) {
                    schema.setSchemaSetId(parent.getId());
                    schema.setSchemaSetIdentifier(parent.getIdentifier());
                }
            }
        } catch (ServiceException ex) {
            throw new ResourceNotFoundException("Schema with id: " + this.attributeOwnerEntity.getId() + " does not exist.", ex);
        }
    }

    protected void configureVocabulary() throws ResourceNotFoundException {
        this.currentSection = "vocabularies";
        try {
            this.vocabularyFolder = this.vocabularyService.getVocabularyFolder(this.attributeOwnerEntity.getId());
        } catch (ServiceException ex) {
            throw new ResourceNotFoundException("Vocabulary with id: " + this.attributeOwnerEntity.getId() + " does not exist.", ex);
        }
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

    public SchemaSet getSchemaSet() {
        return schemaSet;
    }

    public void setSchemaSet(SchemaSet schemaSet) {
        this.schemaSet = schemaSet;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public VocabularyFolder getVocabularyFolder() {
        return vocabularyFolder;
    }

    public void setVocabularyFolder(VocabularyFolder vocabularyFolder) {
        this.vocabularyFolder = vocabularyFolder;
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

    public List<String> getConceptIds() {
        return conceptIds;
    }

    public void setConceptIds(List<String> conceptIds) {
        this.conceptIds = conceptIds;
    }

    public VocabularyConceptResult getVocabularyConceptResult() {
        return vocabularyConceptResult;
    }

    public VocabularyConceptFilter getFilter() {
        return filter;
    }

    public void setFilter(VocabularyConceptFilter filter) {
        this.filter = filter;
    }

    public List<eionet.meta.dao.domain.DataElement> getBoundElements() {
        return boundElements;
    }

    public void setBoundElements(List<eionet.meta.dao.domain.DataElement> boundElements) {
        this.boundElements = boundElements;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<VocabularyConceptBoundElementFilter> getBoundElementFilters() {
        return boundElementFilters;
    }

    public void setBoundElementFilters(List<VocabularyConceptBoundElementFilter> boundElementFilters) {
        this.boundElementFilters = boundElementFilters;
    }

}
