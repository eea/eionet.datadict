package eionet.datadict.services.data.impl;

import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.commons.util.Predicate;
import eionet.datadict.commons.util.Selector;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DataSetDao;
import eionet.datadict.dal.DataTableDao;
import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.dal.SimpleAttributeDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DataTable;
import eionet.datadict.model.DataTableElement;
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

public class DataTableDataServiceImpl implements DataTableDataService {

    private final DataTableDao dataTableDao;
    private final DataSetDao dataSetDao;
    private final DataElementDao dataElementDao;
    private final SimpleAttributeDao simpleAttributeDao;
    private final FixedValuesDao fixedValuesDao;
    private final VocabularyDao vocabularyDao;
    
    @Autowired
    public DataTableDataServiceImpl(DataTableDao dataTableDao, DataSetDao dataSetDao, DataElementDao dataElementDao, 
            SimpleAttributeDao simpleAttributeDao, FixedValuesDao fixedValuesDao, VocabularyDao vocabularyDao) {
        this.dataTableDao = dataTableDao;
        this.dataSetDao = dataSetDao;
        this.dataElementDao = dataElementDao;
        this.simpleAttributeDao = simpleAttributeDao;
        this.fixedValuesDao = fixedValuesDao;
        this.vocabularyDao = vocabularyDao;
    }
    
    @Override
    public DataTable getFullDataTableDefinition(int tableId) throws ResourceNotFoundException {
        DataTable dataTable = this.dataTableDao.getDataTableById(tableId);
        
        if (dataTable == null) {
            throw new ResourceNotFoundException(String.format("Table with id %d could not be found.", tableId));
        }
        
        DataSet dataSet = this.dataSetDao.getDataSetById(dataTable.getDataSet().getId());
        
        if (dataSet == null) {
            throw new ResourceNotFoundException(String.format("Dataset with id %d could not be found", dataTable.getDataSet().getId()));
        }
        
        OrmUtils.link(dataSet, dataTable);
        List<SimpleAttribute> dataTableAttributes = this.simpleAttributeDao.getSimpleAttributesOfDataTable(tableId);
        dataTable.setSimpleAttributes(OrmCollectionUtils.createChildCollection(dataTableAttributes));
        List<SimpleAttributeValues> dataTableAttributeValues = this.simpleAttributeDao.getSimpleAttributesValuesOfDataTable(tableId);
        OrmUtils.link(dataTableAttributes, dataTableAttributeValues);
        OrmUtils.link(dataTable, dataTableAttributeValues);
        List<DataTableElement> dataTableElements = this.dataElementDao.getDataElementsOfDataTable(tableId);
        OrmUtils.link(dataTable, dataTableElements);
        List<DataElement> dataElements = IterableUtils.select(dataTableElements, new Selector<DataTableElement, DataElement>() {

            @Override
            public DataElement select(DataTableElement obj) {
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
        
        Map<Integer, Set<SimpleAttribute>> attributesPerDataElement = this.simpleAttributeDao.getSimpleAttributesOfDataElementsInTable(tableId);
        Set<SimpleAttribute> dataElementAttributes = new HashSet<SimpleAttribute>();
        
        for (DataElement dataElement : dataElements) {
            if (attributesPerDataElement.containsKey(dataElement.getId())) {
                dataElement.setSimpleAttributes(attributesPerDataElement.get(dataElement.getId()));
                dataElementAttributes.addAll(dataElement.getSimpleAttributes());
            }
        }
        
        List<SimpleAttributeValues> dataElementAttributeValues = this.simpleAttributeDao.getSimpleAttributesValuesOfDataElementsInTable(tableId);
        OrmUtils.link(dataElements, dataElementAttributeValues);
        OrmUtils.link(new ArrayList<SimpleAttribute>(dataElementAttributes), dataElementAttributeValues);
        
        return dataTable;
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
