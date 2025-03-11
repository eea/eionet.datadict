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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportDatasetToXmlTest {

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
    public void dataset() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.dataset();

        // Type: ELM, TBL, DST
        String objType = "DST";

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        // Mock Code Handler
        // stub consecutive calls
        Mockito.when(mockCodeValueHandler.getCodeItemList())
                // Uncommon Fixed value element
                .thenReturn( ExportMocks.fixedValues())
                // Uncommon vocabulary concept with relationships element
                .thenReturn( ExportMocks.vocabularyConceptsWithRelationships());

        Mockito.when(mockCodeValueHandler.getRelationshipNames())
                // Uncommon Fixed value element
                .thenReturn(null)
                // Uncommon vocabulary concept with relationships element
                .thenReturn(ExportMocks.vocabularyConceptRelationshipNames());

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML(ExportMocks.datasetExportXML());
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }
    
    @Test
    public void emptyDataset() throws SAXException, IOException {
        List<eionet.meta.DataElement> elements = ExportMocks.dataset();

        // Type: ELM, TBL, DST
        String objType = "DST";
        
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        // Mock Code Handler
        // stub consecutive calls
        Mockito.when(mockCodeValueHandler.getCodeItemList())
                // Uncommon Fixed value element
                .thenReturn( null )
                // Uncommon vocabulary concept with relationships element
                .thenReturn( Collections.EMPTY_LIST );

        Mockito.when(mockCodeValueHandler.getRelationshipNames())
                // Uncommon Fixed value element
                .thenReturn(null)
                // Uncommon vocabulary concept with relationships element
                .thenReturn(null);

        Codelist codelist = new Codelist(ExportStatics.ExportType.XML, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.emptyDatasetExportXML();
        Diff diff = new Diff(expected, actual);
        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

}
