package eionet.meta.service;

import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IDataSetDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.InferenceRule;
import eionet.meta.dao.domain.InferenceRule.RuleType;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.data.DataElementsFilter;
import eionet.meta.service.data.DataElementsResult;
import eionet.util.IrrelevantAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class DataServiceImpl implements IDataService {

    /** Data set DAO. */
    @Autowired
    private IDataSetDAO dataSetDao;

    /** Attribute DAO. */
    @Autowired
    private IAttributeDAO attributeDao;

    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    @Autowired
    private IVocabularyFolderDAO vocabularyFolderDAO;

    /** Fixed Value DAO */
    @Autowired
    private IFixedValueDAO fixedValueDao;

    /** Vocabulary concept DAO. */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSet> getDataSets() throws ServiceException {
        try {
            return dataSetDao.getDataSets();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data sets: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DataSet> getRecentlyReleasedDatasets(int limit) throws ServiceException {
        try {
            return dataSetDao.getRecentlyReleasedDatasets(limit);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data sets: " + e.getMessage(), e);
        }
    } // end of method getRecentlyReleasedDatasets

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByName(String shortName) throws ServiceException {
        try {
            return attributeDao.getAttributeByName(shortName);
        } catch (Exception e) {
            throw new ServiceException("Failed to get the attribute for '" + shortName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataElementsResult searchDataElements(DataElementsFilter filter) throws ServiceException {
        try {
            return dataElementDao.searchDataElements(filter);
        } catch (Exception e) {
            throw new ServiceException("Failed search data elements: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> getDataElementAttributes() throws ServiceException {
        try {
            return dataElementDao.getDataElementAttributes();
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element attributes: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<FixedValue> getDataElementFixedValues(int dataElementId) throws ServiceException {
        try{
            return dataElementDao.getFixedValues(dataElementId);
        }
        catch(Exception e){
            throw new ServiceException("Failed to get data element's fixed values: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElement getDataElement(int id) throws ServiceException {
        try {
            return dataElementDao.getDataElement(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean dataElementExists(int id) throws ServiceException {
        try{
            return dataElementDao.dataElementExists(id);
        }
        catch(Exception e){
            throw new ServiceException("Failed to check if data element exists: " + e.getMessage(), e);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> getDataElementsWithFixedValues() throws ServiceException {
        try {
            DataElementsFilter commonElementsFilter = new DataElementsFilter();
            commonElementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
            commonElementsFilter.setRegStatus("Released");
            commonElementsFilter.setType("CH1");
            DataElementsResult commonResult = dataElementDao.searchDataElements(commonElementsFilter);

            DataElementsFilter nonCommonElementsFilter = new DataElementsFilter();
            nonCommonElementsFilter.setElementType(DataElementsFilter.NON_COMMON_ELEMENT_TYPE);
            nonCommonElementsFilter.setRegStatus("Released");
            nonCommonElementsFilter.setType("CH1");
            DataElementsResult nonCommonResult = dataElementDao.searchDataElements(nonCommonElementsFilter);

            List<DataElement> result = new ArrayList<DataElement>();
            result.addAll(commonResult.getDataElements());
            result.addAll(nonCommonResult.getDataElements());
            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to get data elements with fixed values: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataElementDataType(int dataElementId) throws ServiceException {
        try {
            return dataElementDao.getDataElementDataType(dataElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element's data type: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, List<String>> getDataElementSimpleAttributeValues(int dataElementId) throws ServiceException {
        try {
            return attributeDao.getAttributeValues(dataElementId, ParentType.ELEMENT.toString());
        } catch (Exception e) {
            throw new ServiceException("Failed to get data element's attributes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DataElement> getReleasedCommonDataElements() throws ServiceException {

        DataElementsFilter commonElementsFilter = new DataElementsFilter();
        commonElementsFilter.setElementType(DataElementsFilter.COMMON_ELEMENT_TYPE);
        commonElementsFilter.setRegStatus("Released");
        commonElementsFilter.setIncludeOnlyInternal(true);

        DataElementsResult result = dataElementDao.searchDataElements(commonElementsFilter);

        return result.getDataElements();

    }

    @Override
    public int getCommonElementIdByIdentifier(String identifier) throws ServiceException {
        return dataElementDao.getCommonDataElementId(identifier);
    }

    @Override
    public void setDataElementAttributes(DataElement dataElement) throws ServiceException {
        Map<String, List<String>> attributeValues = dataElementDao.getDataElementAttributeValues(dataElement.getId());

        dataElement.setElemAttributeValues(attributeValues);

    }

    @Override
    public List<DataElement> getUnreleasedCommonElements(int datasetId) throws ServiceException {

        List<DataElement> datasetElements = dataElementDao.getDataSetElements(datasetId);
        List<DataElement> unreleasedElems = new ArrayList<DataElement>();
        for (DataElement elem : datasetElements) {
            if (!elem.getStatus().equalsIgnoreCase("Released") && elem.isCommonElement() && !elem.isWorkingCopy()) {
                unreleasedElems.add(elem);
            }
        }

        return unreleasedElems;
    }

    @Override
    public List<DataElement> getVocabularySourceElements(List<Integer> vocabularyIds) {
        return dataElementDao.getVocabularySourceElements(vocabularyIds);
    }

    @Override
    public List<VocabularyConcept> getElementVocabularyConcepts(int elementId) {
        DataElement elem = dataElementDao.getDataElement(elementId);
        List<VocabularyConcept> result = new ArrayList<VocabularyConcept>();
        Integer vocabularyId = elem.getVocabularyId();
        if (vocabularyId != null) {

            List<VocabularyConcept> concepts = vocabularyConceptDao.getVocabularyConcepts(vocabularyId);

            for (VocabularyConcept concept : concepts) {
                boolean conceptDateValid = true;
                if (!elem.getAllConceptsValid()) {
                    conceptDateValid = concept.getStatus().isValid();
                }
                if (conceptDateValid) {
                    result.add(concept);
                }

            }

        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.service.IDataService#switchDataElemType(int, java.lang.String)
     */
    @Override
    public void switchDataElemType(int elemId, String oldType, String newType) throws ServiceException {

        // Check if the new type is at all known.
        if (!Arrays.asList("CH1", "CH2", "CH3").contains(newType)) {
            throw new ServiceException("Unknown data element type: " + newType);
        }

        // Load the data element and compare to its current type.
        DataElement dataElement = dataElementDao.getDataElement(elemId);
        if (dataElement == null) {
            throw new ServiceException("Found no data element with this id: " + elemId);
        } else {
            if (newType.equals(dataElement.getType())) {
                throw new ServiceException("Data element (id=" + elemId + ") already has this type: " + newType);
            }
        }

        // Change type in database.
        dataElementDao.changeDataElemType(elemId, newType);

        // Remove simple attributes that are considered irrelevant for the new type.
        IrrelevantAttributes instance = IrrelevantAttributes.getInstance();
        Set<String> irrelevantAttrs = instance.get(newType);
        if (CollectionUtils.isNotEmpty(irrelevantAttrs)) {
            dataElementDao.removeSimpleAttrsByShortName(elemId, irrelevantAttrs.toArray(new String[irrelevantAttrs.size()]));
        }
        
        //CH3 has to be reference, CH1 and CH2 cannot be reference. Change the relevant attribute accordingly
        DataElement element = dataElementDao.getDataElement(elemId);

        String newDataType;
        if ("CH3".equals(oldType) || "CH3".equals(newType)) {
            Map<String, List<String>> attrValues = dataElementDao.getDataElementAttributeValues(elemId);
            newDataType = "CH3".equals(oldType) ? "string" : "reference";

            if (attrValues.containsKey("Datatype")) {
                List<String> dataTypeList = attrValues.get("Datatype");
                if (dataTypeList != null & !dataTypeList.isEmpty()) {
                    attributeDao.updateSimpleAttributeValue("Datatype", elemId, "E", newDataType);
                }
            }
        }

    }
    
    @Override
    public Collection<InferenceRule> getDataElementRules(int dataElementId) throws ServiceException {
        try{
            DataElement dataElement = dataElementDao.getDataElement(dataElementId);
            return dataElementDao.listInferenceRules(dataElement);
        }
        catch(Exception e){
            throw new ServiceException("Failed to get inference rules for element " + dataElementId + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public Collection<InferenceRule> listDataElementRules(int dataElementId) throws ServiceException {
        try{
            DataElement dataElement = dataElementDao.getDataElement(dataElementId);
            return dataElementDao.getInferenceRules(dataElement);
        }
        catch(Exception e){
            throw new ServiceException("Failed to list inference rules for element " + dataElementId + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public void createDataElementRule(int sourceDElementId, RuleType type, int targetDElementId) throws ServiceException {
        try{
            DataElement sourceDElement = dataElementDao.getDataElement(sourceDElementId);
            DataElement targetDElement = dataElementDao.getDataElement(targetDElementId);
            InferenceRule rule = new InferenceRule(sourceDElement, type, targetDElement);
            
            dataElementDao.createInferenceRule(rule);
        }
        catch(Exception e){
            throw new ServiceException("Failed to create new rule (" + type.getName() + "," + targetDElementId + ") for element " + sourceDElementId + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteDataElementRule(int sourceDElementId, RuleType type, int targetDElementId) throws ServiceException {
        try{
            DataElement sourceDElement = dataElementDao.getDataElement(sourceDElementId);
            DataElement targetDElement = dataElementDao.getDataElement(targetDElementId);
            InferenceRule rule = new InferenceRule(sourceDElement, type, targetDElement);
            
            dataElementDao.deleteInferenceRule(rule);
        }
        catch(Exception e){
            throw new ServiceException("Failed to delete rule (" + type.getName() + "," + targetDElementId + ") for element " + sourceDElementId + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean ruleExists(int sourceDElementId, RuleType type, int targetDElementId) throws ServiceException {
        try{
            DataElement sourceDElement = dataElementDao.getDataElement(sourceDElementId);
            DataElement targetDElement = dataElementDao.getDataElement(targetDElementId);
            InferenceRule rule = new InferenceRule(sourceDElement, type, targetDElement);
            
            return  dataElementDao.inferenceRuleExists(rule);
        }
        catch(Exception e){
            throw new ServiceException("Failed to check if rule (" + type.getName() + "," + targetDElementId + ") for element " + sourceDElementId + " exists : " + e.getMessage(), e);
        }
    }
    
    @Override
    public void updateDataElementRule(int sourceDElementId, RuleType type, int targetDElementId, RuleType newType, int newTargetElementId) throws ServiceException {
        try{
            DataElement sourceDElement = dataElementDao.getDataElement(sourceDElementId);
            DataElement targetDElement = dataElementDao.getDataElement(targetDElementId);
            InferenceRule rule = new InferenceRule(sourceDElement, type, targetDElement);
            
            DataElement newTargetDElement = dataElementDao.getDataElement(newTargetElementId);
            InferenceRule newRule = new InferenceRule(sourceDElement, newType, newTargetDElement);
            
            dataElementDao.updateInferenceRule(rule, newRule);
        }
        catch(Exception e){
            throw new ServiceException("Failed to update rule (" + type.getName() + "," + targetDElementId + ") for element " + sourceDElementId + " : " + e.getMessage(), e);
        }
    }
    
    @Override
    public Collection<DataElement> grepDataElement(String pattern) throws ServiceException {
        try{
            return dataElementDao.grepDataElement(pattern);
        }
        catch(Exception e){
            throw new ServiceException("Failed to grep for data element : " + e.getMessage(), e);
        }
    }


    @Override
    public void handleElementTypeChange(String elementId, String checkedOutCopyId) throws ServiceException {
        
        int newId = Integer.valueOf(elementId);
        int oldId = Integer.valueOf(checkedOutCopyId);
        
        DataElement dataElement = dataElementDao.getDataElement(newId);
        DataElement originalElement = dataElementDao.getDataElement(oldId);
        
        String oldType = originalElement.getType();
        String newType = dataElement.getType();
        
        if (oldType.equals(newType) || (!"CH3".equals(oldType) && !"CH3".equals(newType))) {
            return;
        }
        
        //old type may have some referential entries, replace textual value with composed url
        List<VocabularyConcept> valuedConcepts = vocabularyConceptDao.getConceptsWithValuedElement(oldId);
        
        //vocabularyId:[conceptIds]
        Map<Integer, List<Integer>> vocabularyIds = new HashMap<Integer, List<Integer>>();
        for (VocabularyConcept concept : valuedConcepts) {
            if (!vocabularyIds.containsKey(concept.getVocabularyId())) {
                vocabularyIds.put(concept.getVocabularyId(), new ArrayList<Integer>());
            }
            vocabularyIds.get(concept.getVocabularyId()).add(concept.getId());
        }
        Map<Integer, List<List<DataElement>>> allConceptValues = new HashMap<Integer, List<List<DataElement>>>();
        
        for (Integer vocabularyId : vocabularyIds.keySet()) {
            List<Integer> conceptIdList = vocabularyIds.get(vocabularyId);
            int[] conceptIds = new int[conceptIdList.size()];
            for (int i = 0; i < conceptIdList.size(); i++) {
                conceptIds[i] = conceptIdList.get(i);
            }
            Map<Integer, List<List<DataElement>>> vocabularyConceptsDataElementValues =
                    dataElementDao.getVocabularyConceptsDataElementValues(vocabularyId, conceptIds, false);

            allConceptValues.putAll(vocabularyConceptsDataElementValues);
        }
        
        for (Integer conceptId : allConceptValues.keySet()) {
            List<List<DataElement>> values = allConceptValues.get(conceptId);
            for (List<DataElement> elementValues : values) {
                if (elementValues != null && !elementValues.isEmpty()) {
                    DataElement valueMeta = elementValues.get(0);
                    if (valueMeta.getId() == oldId) {
                        for (DataElement value : elementValues) {
                            if ("CH3".equals(oldType)) {
                                if (value.getRelatedConceptId() != null && value.getRelatedConceptId() != 0) {
                                    String attrValue = value.getRelatedConceptBaseURI() + value.getRelatedConceptIdentifier();
                                    dataElementDao.updateVocabularyConceptDataElementValue(value.getValueId(), attrValue, null, null);
                                }
                            } else if ("CH3".equals(newType)) {
                                if (StringUtils.isNotBlank(value.getAttributeValue())) {
                                    VocabularyFolder relatedVocabulary = vocabularyFolderDAO.getVocabularyFolder(dataElement.getVocabularyId());
                                    String attrValue = relatedVocabulary.getBaseUri() + value.getAttributeValue();
                                    dataElementDao.updateVocabularyConceptDataElementValue(value.getValueId(), attrValue, null, null);
                                    
                                }
                            }
                        }
                        
                    }
                    
                    
                }
                
            }
        }
    }
}
