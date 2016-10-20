package eionet.datadict.services.data.impl;

import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DataSetDao;
import eionet.datadict.dal.DataTableDao;
import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.dal.SimpleAttributeDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataElementWithFixedValues;
import eionet.datadict.model.DataElementWithQuantitativeValues;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DataTable;
import eionet.datadict.model.DataTableElement;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.SimpleAttribute;
import eionet.datadict.model.SimpleAttributeFixedValues;
import eionet.datadict.model.SimpleAttributeTextArea;
import eionet.datadict.model.SimpleAttributeTextBox;
import eionet.datadict.model.SimpleAttributeValues;
import eionet.datadict.orm.OrmReflectionTestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataTableDataServiceTest {

    @Mock
    private DataTableDao dataTableDao;
    @Mock
    private DataSetDao dataSetDao;
    @Mock
    private DataElementDao dataElementDao;
    @Mock
    private SimpleAttributeDao simpleAttributeDao;
    @Mock
    private FixedValuesDao fixedValuesDao;
    @Mock
    private VocabularyDao vocabularyDao;
    @InjectMocks
    private DataTableDataServiceImpl dataTableDataService;
    
    @Test
    public void testGetFullDataTableDefinition() {
        final DataTable dataTable = this.createDataTable(17, "some_data_table", 6, 9);
        final DataSet dataSet = this.createDataSet(dataTable.getDataSet().getId(), "some_dataset", 4);
        final List<SimpleAttribute> dataTableAttributes = this.createDataTableAttributes();
        final List<SimpleAttributeValues> dataTableAttributeValues = this.createDataTableAttributeValues(dataTable, dataTableAttributes);
        
        when(dataTableDao.getDataTableById(dataTable.getId())).thenReturn(dataTable);
        when(dataSetDao.getDataSetById(dataSet.getId())).thenReturn(dataSet);
        when(simpleAttributeDao.getSimpleAttributesOfDataTable(dataTable.getId())).thenReturn(dataTableAttributes);
        when(simpleAttributeDao.getSimpleAttributesValuesOfDataTable(dataTable.getId())).thenReturn(dataTableAttributeValues);
        
    }
    
    private DataTable createDataTable(int dataTableId, String identifier, int dataSetId, int namespaceId) {
        DataTable dataTable = new DataTable(dataTableId);
        dataTable.setIdentifier(identifier);
        dataTable.setDataSet(new DataSet(dataSetId));
        dataTable.setNamespace(new Namespace(namespaceId));
        
        return dataTable;
    }
    
    private DataSet createDataSet(int dataSetId, String identifier, int namespaceId) {
        DataSet dataSet = new DataSet(dataSetId);
        dataSet.setIdentifier(identifier);
        dataSet.setNamespace(new Namespace(namespaceId));
        
        return dataSet;
    }
    
    private List<SimpleAttribute> createDataTableAttributes() {
        List<SimpleAttribute> attributes = new ArrayList<SimpleAttribute>();
        SimpleAttribute attr1 = new SimpleAttributeTextBox(35);
        attr1.setShortName("attr1");
        SimpleAttribute attr2 = new SimpleAttributeTextArea(36);
        attr1.setShortName("attr2");
        SimpleAttribute attr3 = new SimpleAttributeFixedValues(37);
        attr1.setShortName("attr3");
        SimpleAttribute attr4 = new SimpleAttributeTextBox(38);
        attr1.setShortName("attr4");
        attributes.add(attr1);
        attributes.add(attr2);
        attributes.add(attr3);
        attributes.add(attr4);
        
        return attributes;
    }
    
    private List<SimpleAttributeValues> createDataTableAttributeValues(DataTable owner, List<SimpleAttribute> attributes) {
        List<SimpleAttributeValues> values = new ArrayList<SimpleAttributeValues>();
        DataTable ownerToSet = new DataTable(owner.getId());
        
        for (SimpleAttribute attribute : attributes) {
            SimpleAttributeValues val = new SimpleAttributeValues();
            val.setOwner(ownerToSet);
            val.setAttribute(OrmReflectionTestUtils.newEntityWithSameIdAs(attribute));
            val.setValues(Arrays.asList(attribute.getShortName() + "_value"));
        }
        
        return values;
    }
    
    private List<DataTableElement> createDataTableElements(DataTable dataTable) {
        List<DataTableElement> dataTableElements = new ArrayList<DataTableElement>();
        DataTable dataTableToSet = new DataTable(dataTable.getId());
        DataElement elm1 = new DataElementWithFixedValues(23);
        elm1.setIdentifier("elm1");
        DataElement elm2 = new DataElementWithQuantitativeValues(24);
        elm2.setIdentifier("elm2");
        DataElement elm3 = new DataElementWithQuantitativeValues(25);
        elm3.setIdentifier("elm3");
        DataElement elm4 = new DataElementWithFixedValues(26);
        elm4.setIdentifier("elm4");
        DataElement elm5 = new DataElementWithFixedValues(27);
        elm5.setIdentifier("elm5");
        dataTableElements.add(new DataTableElement(dataTableToSet, elm1));
        dataTableElements.add(new DataTableElement(dataTableToSet, elm2));
        dataTableElements.add(new DataTableElement(dataTableToSet, elm3));
        dataTableElements.add(new DataTableElement(dataTableToSet, elm4));
        dataTableElements.add(new DataTableElement(dataTableToSet, elm5));
        
        return dataTableElements;
    }
    
}
