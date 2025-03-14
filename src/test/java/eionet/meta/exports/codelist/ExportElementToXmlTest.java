/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.domain.DataElement;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportElementToXmlTest {

    @Mock
    private CodeValueHandler mockCodeValueHandler;
    @Mock
    private CodeValueHandlerProvider mockCodeValueHandlerProvider;

    @BeforeClass
    public static void setUpClass() {
        XMLUnit.setIgnoreWhitespace(true); 
    }

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {}

    @Test
    public void commonVocabularyValuesWithRelationships() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyCommonDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.vocabularyConceptsWithRelationships());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(ExportMocks.vocabularyConceptRelationshipNames());

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.commonDataElementWithVocabularyValuesWithRelationshipsExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

    @Test
    public void uncommonVocabularyValuesWithRelationships() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";
        
        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn(ExportMocks.vocabularyConceptsWithRelationships());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(ExportMocks.vocabularyConceptRelationshipNames());

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.uncommonDataElementWithVocabularyValuesWithRelationshipsExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

    @Test
    public void commonVocabularyValues() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyCommonDataElementSimple();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn(ExportMocks.vocabularyConcepts());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.commonDataElementWithVocabularyValuesExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }
    
    @Test
    public void commonFixedValues() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.commonFixedValueDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn(ExportMocks.fixedValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.commonDataElementWithFixedValuesExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

    @Test
    public void uncommonFixedValues() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.uncommonFixedValueDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.fixedValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);
        
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);
        
        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.uncommonDataElementWithFixedValuesExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

    @Test
    public void quantitativeValues() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.quantitativeDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn(ExportMocks.quantitativeValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.QUANTITIVE)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML( ExportMocks.quantitativeValuesExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

}
