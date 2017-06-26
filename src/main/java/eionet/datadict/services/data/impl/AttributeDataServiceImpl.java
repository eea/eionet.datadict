package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.model.Attribute;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AttributeDataServiceImpl implements AttributeDataService {

    private final AttributeDao attributeDao;
    private final AttributeValueDao attributeValueDao;
    private final VocabularyDao vocabularyDao;
    private final IVocabularyConceptDAO vocabularyConceptDAO;
    private final IFixedValueDAO fixedValueDao;
    private final DatasetTableDao datasetTableDao;
    private final DataElementDao dataElementDao;
    
    @Autowired
    public AttributeDataServiceImpl
        (AttributeDao attributeDao, AttributeValueDao attributeValueDao, VocabularyDao vocabularyDao, 
                IVocabularyConceptDAO vocabularyConceptDAO, IFixedValueDAO fixedValueDao, 
                DatasetTableDao datasetTableDao, DataElementDao dataElementDao) {
        this.attributeDao = attributeDao;
        this.attributeValueDao = attributeValueDao;
        this.vocabularyDao = vocabularyDao;
        this.fixedValueDao = fixedValueDao;
        this.vocabularyConceptDAO = vocabularyConceptDAO;
        this.datasetTableDao = datasetTableDao;
        this.dataElementDao = dataElementDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Attribute getAttribute(int id) throws ResourceNotFoundException {
        Attribute attribute = attributeDao.getById(id);
        
        if (attribute == null) {
            throw new ResourceNotFoundException("Attribute with id: "+id+" does not exist.");
        }
        
        if (attribute.getVocabulary() != null) {
            VocabularyFolder voc = attribute.getVocabulary();
            voc = vocabularyDao.getPlainVocabularyById(voc.getId());
            attribute.setVocabulary(voc);
        }
        
        return attribute;
    }
    
    @Override
    public boolean existsAttribute(int id) {
        return this.attributeDao.exists(id);
    }
    
    @Override
    public int createAttribute(Attribute attribute) {
        return this.attributeDao.create(attribute);
    }

    @Override
    @Transactional
    public void updateAttribute(Attribute attribute) {
        this.attributeDao.update(attribute);
        if (attribute.getVocabulary() != null) {
            this.attributeDao.updateVocabularyBinding(attribute.getId(), attribute.getVocabulary().getId());
        }
        else {
            this.attributeDao.deleteVocabularyBinding(attribute.getId());
        }
    }
    
    @Override
    @Transactional
    public void deleteAttributeById(int attributeId) {
        this.attributeDao.deleteValues(attributeId);
        this.attributeDao.deleteVocabularyBinding(attributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, attributeId);
        this.attributeDao.delete(attributeId);
    }
    
    @Override
    public int countAttributeValues(int attributeId) {
        return this.attributeDao.countAttributeValues(attributeId);
    }
     
    @Override
    public Attribute setNewVocabularyToAttributeObject(Attribute attribute, int vocabularyId) throws ResourceNotFoundException {
        attribute.setVocabulary(this.vocabularyDao.getPlainVocabularyById(vocabularyId));
        return attribute;
    }

    @Override
    public Map<DataDictEntity.Entity, Integer> getDistinctTypesWithAttributeValues(int attributeId) {
        return this.attributeDao.getConceptsWithAttributeValues(attributeId);
    }
    
    @Override
    public void deleteVocabularyBinding(int attributeId){
        this.attributeDao.deleteVocabularyBinding(attributeId);
    }

    @Override
    public void deleteRelatedFixedValues(int attributeId) {
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, attributeId);
    }
    
    @Override
    public List<FixedValue> getFixedValues(int attributeId) {
        return this.fixedValueDao.getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, attributeId);
    }

    @Override
    public List<AttributeValue> getOriginalAttributeValues(int attributeId, DataDictEntity ddEntity) {
        return this.attributeValueDao.getByAttributeAndOwner(attributeId, ddEntity);
    }
    
    @Override
    @Transactional
    public List<VocabularyConcept> getVocabularyConceptsAsAttributeValues(int attributeId, DataDictEntity attributeOwner, ValueInheritanceMode inheritanceMode) 
            throws ResourceNotFoundException, EmptyParameterException {
       
        List<AttributeValue> attributeValues = getAttributeValues(attributeId, attributeOwner, inheritanceMode);
        return convertAttributeValuesToVocabularyConcepts(attributeValues);
    }
    
    @Override
    @Transactional
    public List<VocabularyConcept> getVocabularyConceptsAsOriginalAttributeValues(int attributeId, DataDictEntity attributeOwner) 
            throws ResourceNotFoundException, EmptyParameterException {
       
        List<AttributeValue> attributeValues = getOriginalAttributeValues(attributeId, attributeOwner);
        return convertAttributeValuesToVocabularyConcepts(attributeValues);
    }
    
    @Override
    @Transactional
    public List<VocabularyConcept> getVocabularyConceptsAsInheritedAttributeValues(int attributeId, DataDictEntity attributeOwner) 
            throws ResourceNotFoundException, EmptyParameterException {
       
        List<AttributeValue> attributeValues = getInheritedAttributeValues(attributeId, attributeOwner);
        return convertAttributeValuesToVocabularyConcepts(attributeValues);
    }
    
    @Override
    public void deleteAttributeValue(int attributeId, DataDictEntity ddEntity, String value) {
        this.attributeValueDao.deleteAttributeValue(attributeId, ddEntity, value);
    }

    @Override
    public void deleteAllAttributeValues(int attributeId, DataDictEntity ddEntity) {
        this.attributeValueDao.deleteAllAttributeValues(attributeId, ddEntity);
    }

    @Override
    public void deleteAllAttributeValues(int attributeId) {
        this.attributeValueDao.deleteAllAttributeValues(attributeId);
    }
    
    @Override
    public void createAttributeValues(int attributeId, DataDictEntity ownerEntity, List<String> values) {
       this.attributeValueDao.addAttributeValues(attributeId, ownerEntity, values);
    }

    @Override
    public Integer getVocabularyBinding(int attributeId) {
        return this.attributeDao.getVocabularyBinding(attributeId);
    }

    @Override
    @Transactional
    public List<AttributeValue> getInheritedAttributeValues(int attributeId, DataDictEntity ddEntity) throws ResourceNotFoundException {
        
        if (ddEntity.getType() == DataDictEntity.Entity.DS || ddEntity.getType() == DataDictEntity.Entity.SCH || ddEntity.getType() == DataDictEntity.Entity.SCS) {
            return Collections.EMPTY_LIST;
        }
        
        if (ddEntity.getType() == DataDictEntity.Entity.T) {
            Integer datasetId = this.datasetTableDao.getParentDatasetId(ddEntity.getId());
            if (datasetId == null) {
                throw new ResourceNotFoundException("Parent dataset id does not exist!");
            }
            return this.attributeValueDao.getByAttributeAndOwner(attributeId, new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
        }
        
        if (ddEntity.getType() == DataDictEntity.Entity.E) {    
            
            Integer tableId = this.dataElementDao.getParentTableId(ddEntity.getId());
            if (tableId == null) {
                return Collections.EMPTY_LIST;
            }
            
            Integer datasetId = this.datasetTableDao.getParentDatasetId(tableId);
            if (datasetId == null) {
                throw new ResourceNotFoundException("Parent dataset does not exist!");
            }
            
            List<AttributeValue> tableInheritedValues = this.attributeValueDao.getByAttributeAndOwner(attributeId, new DataDictEntity(tableId, DataDictEntity.Entity.T));
            List<AttributeValue> datasetInheritedValues = this.attributeValueDao.getByAttributeAndOwner(attributeId, new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
            
            return this.concatenateListsRemovingDuplicates(tableInheritedValues, datasetInheritedValues);
            
        }
        
        return null;
    }

    @Override
    @Transactional
    public List<AttributeValue> getAttributeValues(int attributeId, DataDictEntity attributeOwner, ValueInheritanceMode inheritance) 
            throws ResourceNotFoundException, EmptyParameterException {
        
        if (inheritance.equals(ValueInheritanceMode.NONE)) {
            return this.getOriginalAttributeValues(attributeId, attributeOwner);
        }
        
        if (inheritance.equals(ValueInheritanceMode.PARENT_WITH_EXTEND)){
            List<AttributeValue> original = this.getOriginalAttributeValues(attributeId, attributeOwner);
            List<AttributeValue> inherited = this.getInheritedAttributeValues(attributeId, attributeOwner);

            return this.concatenateListsRemovingDuplicates(original, inherited);
        }

        if (inheritance.equals(ValueInheritanceMode.PARENT_WITH_OVERRIDE)){
            List<AttributeValue> original = this.getOriginalAttributeValues(attributeId, attributeOwner);
            if (!original.isEmpty()) {
                return original;
            } else {
                List<AttributeValue> inherited = this.getInheritedAttributeValues(attributeId, attributeOwner);
                return inherited;
            }
        }
        
        throw new EmptyParameterException("Inheritance type is not defined!");
        
    }
    
    protected List<AttributeValue> concatenateListsRemovingDuplicates(List<AttributeValue> listA, List<AttributeValue> listB) {
        if (listA.isEmpty()) {
            return listB;
        }

        if (listB.isEmpty()) {
            return listA;
        }

        return ListUtils.union(listA, listB);
    }

    protected List<VocabularyConcept> convertAttributeValuesToVocabularyConcepts(List<AttributeValue> attributeValues) {
        if (attributeValues.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<Integer> conceptIds = new ArrayList<>();
        for (AttributeValue attributeValue : attributeValues) {
            Integer conceptId = NumberUtils.toInt(attributeValue.getValue(), 0);
            if (conceptId > 0) {
                conceptIds.add(conceptId);
            }
        }
        return vocabularyConceptDAO.getVocabularyConcepts(conceptIds);
    }

}
