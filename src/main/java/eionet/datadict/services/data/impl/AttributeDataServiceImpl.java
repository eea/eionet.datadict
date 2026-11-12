package eionet.datadict.services.data.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.model.Attribute;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AttributeDataServiceImpl implements AttributeDataService {

    private final AttributeDao attributeDao;
    private final AttributeValueDao attributeValueDao;
    private final IFixedValueDAO fixedValueDao;
    private final DatasetTableDao datasetTableDao;
    private final DataElementDao dataElementDao;

    @Autowired
    public AttributeDataServiceImpl
        (AttributeDao attributeDao, AttributeValueDao attributeValueDao,
                IFixedValueDAO fixedValueDao, DatasetTableDao datasetTableDao, DataElementDao dataElementDao) {
        this.attributeDao = attributeDao;
        this.attributeValueDao = attributeValueDao;
        this.fixedValueDao = fixedValueDao;
        this.datasetTableDao = datasetTableDao;
        this.dataElementDao = dataElementDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Attribute getAttribute(int id) throws ResourceNotFoundException {
        Attribute attribute = attributeDao.getById(id);
        
        if (attribute == null) {
            throw new ResourceNotFoundException("Attribute with id: " + id + " does not exist.");
        }

        return attribute;
    }

    @Override
    public List<Attribute> getAllAttributes() {
        return this.attributeDao.getAll();
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
    }

    @Override
    @Transactional
    public void deleteAttributeById(int attributeId) {
        this.attributeDao.deleteValues(attributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, attributeId);
        this.attributeDao.delete(attributeId);
    }
    
    @Override
    public int countAttributeValues(int attributeId) {
        return this.attributeDao.countAttributeValues(attributeId);
    }
     
    @Override
    public Map<DataDictEntity.Entity, Integer> getDistinctTypesWithAttributeValues(int attributeId) {
        return this.attributeDao.getConceptsWithAttributeValues(attributeId);
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

    @Override
    public List<Attribute> getAllByDatasetId(int datasetId) {
        return this.attributeDao.getByDataDictEntity(new DataDictEntity(datasetId, DataDictEntity.Entity.DS));
    }

}
