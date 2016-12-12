package eionet.datadict.dal.impl;

import eionet.datadict.dal.impl.AttributeDaoImpl.AttributeRowMapper;
import eionet.datadict.dal.impl.AttributeDaoImpl.DisplayTypeConverter;
import eionet.datadict.dal.impl.AttributeDaoImpl.ObligationTypeConverter;
import eionet.datadict.dal.impl.AttributeDaoImpl.ValueInheritanceConverter;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.Attribute.DisplayType;
import eionet.datadict.model.Attribute.ObligationType;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.util.data.DataConverter;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
        

public class AttributeDaoImplTest {

    @Mock
    private RdfNamespace rdfNamespace;
            
    @Mock
    private Namespace namespace;
    
    @Mock
    private Attribute attribute;
    
    @Mock 
    private ResultSet resultSet;
    
    @Spy
    private AttributeRowMapper rowMapper;
      
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testAttributeRowMapper_mapRow()throws SQLException{

        when(resultSet.getInt("M_ATTRIBUTE.M_ATTRIBUTE_ID")).thenReturn(1);
        when(resultSet.getInt("M_ATTRIBUTE.DISP_ORDER")).thenReturn(999);
        when(resultSet.getInt("M_ATTRIBUTE.DISP_WHEN")).thenReturn(0);
        when(resultSet.getInt("M_ATTRIBUTE.DISP_WIDTH")).thenReturn(AttributeDaoImpl.DISPLAY_WIDTH_DEFAULT);
        when(resultSet.getInt("M_ATTRIBUTE.DISP_HEIGHT")).thenReturn(AttributeDaoImpl.DISPLAY_HEIGHT_DEFAULT);
        when(resultSet.getBoolean("M_ATTRIBUTE.LANGUAGE_USED")).thenReturn(Boolean.FALSE);
        when(resultSet.getString("M_ATTRIBUTE.NAME")).thenReturn("name");
        when(resultSet.getString("M_ATTRIBUTE.DEFINITION")).thenReturn("definition");
        when(resultSet.getString("M_ATTRIBUTE.SHORT_NAME")).thenReturn("short name");
        when(resultSet.getBoolean("M_ATTRIBUTE.DISP_MULTIPLE")).thenReturn(Boolean.FALSE);
        when(resultSet.getString("M_ATTRIBUTE.RDF_PROPERTY_NAME")).thenReturn("rdfPropertyName");
        when(resultSet.getString("M_ATTRIBUTE.OBLIGATION")).thenReturn("M");
        when(resultSet.getString("M_ATTRIBUTE.INHERIT")).thenReturn("0");
        when(resultSet.getString("M_ATTRIBUTE.DATA_TYPE")).thenReturn("reference");
  
        when(resultSet.getInt("M_ATTRIBUTE.NAMESPACE_ID")).thenReturn(1);
        when(resultSet.getInt("M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID")).thenReturn(1);
        when(resultSet.getInt("VOCABULARY_ID")).thenReturn(1);
        
        Mockito.doNothing().when(rowMapper).readNamespace(any(ResultSet.class), any(Attribute.class));
        Mockito.doNothing().when(rowMapper).readRdfNamespace(any(ResultSet.class), any(Attribute.class));
        
        Attribute attributeRes = rowMapper.mapRow(resultSet, 0);
        
        assertEquals(new Integer(1), attributeRes.getId());
        assertEquals(null, attributeRes.getDisplayOrder());
        assertEquals(0, attributeRes.getTargetEntities().size());
        assertEquals(AttributeDaoImpl.DISPLAY_WIDTH_DEFAULT, attributeRes.getDisplayWidth().intValue());
        assertEquals(AttributeDaoImpl.DISPLAY_HEIGHT_DEFAULT, attributeRes.getDisplayHeight().intValue());
        assertEquals(false, attributeRes.isLanguageUsed());
        assertEquals("name", attributeRes.getName());
        assertEquals("definition", attributeRes.getDefinition());
        assertEquals("short name", attributeRes.getShortName());
        assertEquals(false, attributeRes.isDisplayMultiple());
        assertEquals("rdfPropertyName", attributeRes.getRdfPropertyName());
        assertEquals(ObligationType.MANDATORY, attributeRes.getObligationType());
        assertEquals(ValueInheritanceMode.NONE, attributeRes.getValueInheritanceMode());
        
        assertNotNull(attributeRes.getNamespace());
        assertNotNull(attributeRes.getRdfNamespace());
        assertNotNull(attributeRes.getVocabulary());
    }
    
    @Test
    public void testAttributeRowMapper_readNamespace() throws SQLException {
        when(attribute.getNamespace()).thenReturn(namespace);
        when(resultSet.getInt(anyString())).thenReturn(1);
        when(resultSet.getString(anyString())).thenReturn("");
        
        rowMapper.readNamespace(resultSet, attribute);
        
        verify(namespace, times(1)).setDefinition(anyString());
        verify(namespace, times(1)).setFullName(anyString());
        verify(namespace, times(1)).setShortName(anyString());
        verify(namespace, times(1)).setWorkingUser(anyString());
    }
    
    @Test
    public void testAttributeRowMapper_readRdfNamespace() throws SQLException {
        when(attribute.getRdfNamespace()).thenReturn(rdfNamespace);
        when(resultSet.getInt(anyString())).thenReturn(1);
        when(resultSet.getString(anyString())).thenReturn("");
        
        rowMapper.readRdfNamespace(resultSet, attribute);
        
        verify(rdfNamespace, times(1)).setPrefix(anyString());
        verify(rdfNamespace, times(1)).setUri(anyString());
    }
    
    @Test
    public void testObligationTypeConverter_convert() {
        DataConverter converter = new ObligationTypeConverter();
        assertEquals("M", converter.convert(ObligationType.MANDATORY));
        assertEquals("O", converter.convert(ObligationType.OPTIONAL));
        assertEquals("C", converter.convert(ObligationType.CONDITIONAL));
    }
       
    @Test
    public void testDisplayTypeConverter_convert() {
        DataConverter converter = new DisplayTypeConverter();
        assertEquals(null, converter.convert(null));
        assertEquals("select", converter.convert(DisplayType.SELECT));
        assertEquals("image", converter.convert(DisplayType.IMAGE));
        assertEquals("text", converter.convert(DisplayType.TEXT));
        assertEquals("textarea", converter.convert(DisplayType.TEXTAREA));
        assertEquals("vocabulary", converter.convert(DisplayType.VOCABULARY));
    }
   
    @Test
    public void testValueInheritanceConverter_convert() {
        DataConverter converter = new ValueInheritanceConverter();
        assertEquals("0", converter.convert(ValueInheritanceMode.NONE));
        assertEquals("1", converter.convert(ValueInheritanceMode.PARENT_WITH_EXTEND));
        assertEquals("2", converter.convert(ValueInheritanceMode.PARENT_WITH_OVERRIDE));
    }
    
    @Test
    public void testObligationTypeConverter_convertBack() {
        DataConverter converter = new ObligationTypeConverter();
        assertEquals(ObligationType.MANDATORY, converter.convertBack("M"));
        assertEquals(ObligationType.OPTIONAL, converter.convertBack("O"));
        assertEquals(ObligationType.CONDITIONAL, converter.convertBack("C"));
    }
    
    @Test
    public void testDisplayTypeConverter_convertBack() {
        DataConverter converter = new DisplayTypeConverter();
        assertEquals(null, converter.convertBack(null));
        assertEquals(DisplayType.SELECT, converter.convertBack("select"));
        assertEquals(DisplayType.IMAGE, converter.convertBack("image"));
        assertEquals(DisplayType.TEXT, converter.convertBack("text"));
        assertEquals(DisplayType.TEXTAREA, converter.convertBack("textarea"));
        assertEquals(DisplayType.VOCABULARY, converter.convertBack("vocabulary"));
    }
    
    @Test
    public void testValueInheritanceConverter_convertBack() {
        DataConverter converter = new ValueInheritanceConverter();
        assertEquals(ValueInheritanceMode.NONE, converter.convertBack("0"));
        assertEquals(ValueInheritanceMode.PARENT_WITH_EXTEND, converter.convertBack("1"));
        assertEquals(ValueInheritanceMode.PARENT_WITH_OVERRIDE, converter.convertBack("2"));
    }
}
