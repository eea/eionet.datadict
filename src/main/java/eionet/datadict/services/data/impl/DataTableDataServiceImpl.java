package eionet.datadict.services.data.impl;

import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DataTableDao;
import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.dal.SimpleAttributeDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataTable;
import eionet.datadict.model.SimpleAttribute;
import eionet.datadict.model.SimpleAttributeValues;
import eionet.datadict.services.data.DataTableDataService;

public class DataTableDataServiceImpl implements DataTableDataService {

    private final DataTableDao dataTableDao;
    private final DataElementDao dataElementDao;
    private final SimpleAttributeDao simpleAttributeDao;
    private final FixedValuesDao fixedValuesDao;
    private final VocabularyDao vocabularyDao;
    
    public DataTableDataServiceImpl(DataTableDao dataTableDao, DataElementDao dataElementDao, 
            SimpleAttributeDao simpleAttributeDao, FixedValuesDao fixedValuesDao, VocabularyDao vocabularyDao) {
        this.dataTableDao = dataTableDao;
        this.dataElementDao = dataElementDao;
        this.simpleAttributeDao = simpleAttributeDao;
        this.fixedValuesDao = fixedValuesDao;
        this.vocabularyDao = vocabularyDao;
    }
    
    @Override
    public DataTable getFullDataTableDefinition(int tableId) throws ResourceNotFoundException {
        DataTable table = this.dataTableDao.getDataTableById(tableId);
        
        if (table == null) {
            throw new ResourceNotFoundException(String.format("Table with id %d could not be found.", tableId));
        }
        
        table.setSimpleAttributes(this.simpleAttributeDao.getSimpleAttributesOfDataTable(tableId));
        table.setSimpleAttributesValues(this.simpleAttributeDao.getSimpleAttributesValuesOfDataTable(tableId));
        /*
        DataObjectUtils.linkParentChild(table.getSimpleAttributes(), table.getSimpleAttributesValues(), 
                new SimpleAttributeToValuesLinker(), new SimpleAttributeIdProvider(), new SimpleAttributeValuesAttributeIdProvider());
        
        DataObjectUtils.linkParentChild(table.getSimpleAttributes(), table.getSimpleAttributesValues(),
            new ObjectLinker<SimpleAttribute, SimpleAttributeValues>() {

                @Override
                public void link(SimpleAttribute left, SimpleAttributeValues right) {
                    right.setAttribute(left);
                }
            },
            new ObjectKeyProvider<SimpleAttribute, Integer>() {
                
                @Override
                public Integer getKey(SimpleAttribute obj) {
                    return obj.getId();
                }
            },
            new ObjectKeyProvider<SimpleAttributeValues, Integer>() {
                
                @Override
                public Integer getKey(SimpleAttributeValues obj) {
                    return obj.getAttribute().getId();
                }
            }
        );
        */
        return null;
    }
    
}
