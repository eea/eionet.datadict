package eionet.datadict.services.data.impl;

import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DataSetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataElementWithFixedValues;
import eionet.datadict.model.DataElementWithQuantitativeValues;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.DatasetTableElement;
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

/**

@RunWith(MockitoJUnitRunner.class)
public class DataTableDataServiceTest {

    @Mock
    private DatasetTableDao datasetTableDao;
    @Mock
    private DataSetDao dataSetDao;
    @Mock
    private DataElementDao dataElementDao;
    @Mock
    private AttributeDao attributeDao;
    @Mock
    private FixedValuesDao fixedValuesDao;
    @Mock
    private VocabularyDao vocabularyDao;
    @InjectMocks
    private DataTableDataServiceImpl dataTableDataService;
    
    @Test
    public void testGetFullDataTableDefinition() {
        final DatasetTable dataTable = this.createDataTable(17, "some_data_table", 6, 9);
        final DataSet dataSet = this.createDataSet(dataTable.getDataSet().getId(), "some_dataset", 4);
        final List<Attribute> dataTableAttributes = this.createDataTableAttributes();
        final List<SimpleAttributeValues> dataTableAttributeValues = this.createDataTableAttributeValues(dataTable, dataTableAttributes);
        
        when(datasetTableDao.getById(dataTable.getId())).thenReturn(dataTable);
        when(dataSetDao.getDataSetById(dataSet.getId())).thenReturn(dataSet);
        when(attributeDao.getAttributesOfDataTable(dataTable.getId())).thenReturn(dataTableAttributes);
        when(attributeDao.getAttributesValuesOfDataTable(dataTable.getId())).thenReturn(dataTableAttributeValues);
        
    }
    
    private DatasetTable createDataTable(int datasetTableId, String identifier, int dataSetId, int namespaceId) {
        DatasetTable datasetTable = new DatasetTable(datasetTableId);
        datasetTable.setIdentifier(identifier);
        datasetTable.setDataSet(new DataSet(dataSetId));
        datasetTable.setNamespace(new Namespace(namespaceId));
        
        return datasetTable;
    }
    
    private DataSet createDataSet(int dataSetId, String identifier, int namespaceId) {
        DataSet dataSet = new DataSet(dataSetId);
        dataSet.setIdentifier(identifier);
        dataSet.setNamespace(new Namespace(namespaceId));
        
        return dataSet;
    }
    
    private List<Attribute> createDataTableAttributes() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        Attribute attr1 = new AttributeTextBox(35);
        attr1.setShortName("attr1");
        Attribute attr2 = new AttributeTextArea(36);
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
    
    private List<SimpleAttributeValues> createDataTableAttributeValues(DatasetTable owner, List<SimpleAttribute> attributes) {
        List<SimpleAttributeValues> values = new ArrayList<SimpleAttributeValues>();
        DatasetTable ownerToSet = new DatasetTable(owner.getId());
        
        for (SimpleAttribute attribute : attributes) {
            SimpleAttributeValues val = new SimpleAttributeValues();
            val.setOwner(ownerToSet);
            val.setAttribute(OrmReflectionTestUtils.newEntityWithSameIdAs(attribute));
            val.setValues(Arrays.asList(attribute.getShortName() + "_value"));
        }
        
        return values;
    }
    
    private List<DatasetTableElement> createDataTableElements(DatasetTable datasetTable) {
        List<DatasetTableElement> datasetTableElements = new ArrayList<DatasetTableElement>();
        DatasetTable dataTableToSet = new DatasetTable(datasetTable.getId());
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
        datasetTableElements.add(new DatasetTableElement(dataTableToSet, elm1));
        datasetTableElements.add(new DatasetTableElement(dataTableToSet, elm2));
        datasetTableElements.add(new DatasetTableElement(dataTableToSet, elm3));
        datasetTableElements.add(new DatasetTableElement(dataTableToSet, elm4));
        datasetTableElements.add(new DatasetTableElement(dataTableToSet, elm5));
        
        return datasetTableElements;
    }
    
}

**/
