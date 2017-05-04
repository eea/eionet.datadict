package eionet.datadict.services.data.impl;

import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.commons.util.Predicate;
import eionet.datadict.commons.util.Selector;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.DatasetTableElement;
import eionet.datadict.model.FixedValue;
import eionet.datadict.model.SimpleAttribute;
import eionet.datadict.model.SimpleAttributeValues;
import eionet.datadict.model.Vocabulary;
import eionet.datadict.orm.OrmCollectionUtils;
import eionet.datadict.orm.OrmUtils;
import eionet.datadict.services.data.DataTableDataService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
@Service
public class DataTableDataServiceImpl implements DataTableDataService {

    private final DatasetTableDao datasetTableDao;
    private final DatasetDao datasetDao;
    private final DataElementDao dataElementDao;
    private final AttributeDao attributeDao;
    private final FixedValuesDao fixedValuesDao;
    private final VocabularyDao vocabularyDao;

    @Autowired
    public DataTableDataServiceImpl(DatasetTableDao datasetTableDao, DatasetDao datasetDao, DataElementDao dataElementDao, AttributeDao attributeDao, FixedValuesDao fixedValuesDao, VocabularyDao vocabularyDao) {
        this.datasetTableDao = datasetTableDao;
        this.datasetDao = datasetDao;
        this.dataElementDao = dataElementDao;
        this.attributeDao = attributeDao;
        this.fixedValuesDao = fixedValuesDao;
        this.vocabularyDao = vocabularyDao;
    }
    
    
    
    
    @Override
    public DatasetTable getFullDatasetTableDefinition(int tableId) throws ResourceNotFoundException {
        DatasetTable datasetTable = this.datasetTableDao.getById(tableId);
        
        if (datasetTable == null) {
            throw new ResourceNotFoundException(String.format("Table with id %d could not be found.", tableId));
        }
        
        DataSet dataSet = this.datasetDao.getById(datasetTable.getDataSet().getId());
        
        if (dataSet == null) {
            throw new ResourceNotFoundException(String.format("Dataset with id %d could not be found", datasetTable.getDataSet().getId()));
        }
        
        OrmUtils.link(dataSet, datasetTable);
        List<Attribute> datasetTableAttributes = this.attributeDao.getAttributesOfDataTable(tableId);
        datasetTable.setAttributes(OrmCollectionUtils.createChildCollection(datasetTableAttributes));
        List<SimpleAttributeValues> datasetTableAttributeValues = this.attributeDao.getSimpleAttributesValuesOfDataElementsInTable(tableId);
        OrmUtils.link(datasetTableAttributes, datasetTableAttributeValues);
        OrmUtils.link(datasetTable, datasetTableAttributeValues);
        List<DatasetTableElement> datasetTableElements = this.dataElementDao.getDatasetTableElementsOfDatasetTable(tableId);
        OrmUtils.link(datasetTable, datasetTableElements);
        List<DataElement> dataElements = IterableUtils.select(datasetTableElements, new Selector<DatasetTableElement, DataElement>() {

            @Override
            public DataElement select(DatasetTableElement obj) {
                return obj.getDataElement();
            }
            
        });
        List<DataElement> dataElementsWithFixedValues = this.filterDataElementsByType(dataElements, DataElement.ValueType.FIXED);
        List<DataElement> dataElementsWithQuantitativeValues = this.filterDataElementsByType(dataElements, DataElement.ValueType.QUANTITATIVE);
        List<DataElement> dataElementsWithVocabularyValues = this.filterDataElementsByType(dataElements, DataElement.ValueType.VOCABULARY);
        
        if (!dataElementsWithFixedValues.isEmpty() || !dataElementsWithQuantitativeValues.isEmpty()) {
            List<FixedValue> fixedValues = this.fixedValuesDao.getValueListCodesOfDataElementsInTable(tableId);
            
            if (!dataElementsWithFixedValues.isEmpty()) {
                OrmUtils.link(dataElementsWithFixedValues, fixedValues);
            }
            
            if (!dataElementsWithQuantitativeValues.isEmpty()) {
                OrmUtils.link(dataElementsWithQuantitativeValues, fixedValues);
            }
        }
        
        if (!dataElementsWithVocabularyValues.isEmpty()) {
            List<Vocabulary> vocabularies = this.vocabularyDao.getValueListCodesOfDataElementsInTable(tableId);
            OrmUtils.link(vocabularies, dataElementsWithVocabularyValues);
        }
        
        Map<Integer, Set<Attribute>> attributesPerDataElement = this.attributeDao.getAttributesOfDataElementsInTable(tableId);
        Set<Attribute> dataElementAttributes = new HashSet<Attribute>();
        
        for (DataElement dataElement : dataElements) {
            if (attributesPerDataElement.containsKey(dataElement.getId())) {
                dataElement.setAttributes(attributesPerDataElement.get(dataElement.getId()));
                dataElementAttributes.addAll(dataElement.getAttributes());
            }
        }
        
        List<SimpleAttributeValues> dataElementAttributeValues = this.attributeDao.getSimpleAttributesValuesOfDataElementsInTable(tableId);
        OrmUtils.link(dataElements, dataElementAttributeValues);
        OrmUtils.link(new ArrayList<Attribute>(dataElementAttributes), dataElementAttributeValues);
        
        return datasetTable;
    }
    
    private List<DataElement> filterDataElementsByType(List<DataElement> dataElements, final DataElement.ValueType valueType) {
        return IterableUtils.filter(dataElements, new Predicate<DataElement>() {

            @Override
            public boolean evaluate(DataElement obj) {
                return valueType.equals(obj.getValueType());
            }
        });
    }
    
}
**/
