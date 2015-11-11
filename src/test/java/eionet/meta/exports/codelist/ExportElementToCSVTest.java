/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.domain.DataElement;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportElementToCSVTest {

    @Mock
    private CodeValueHandler mockCodeValueHandler;
    @Mock
    private CodeValueHandlerProvider mockCodeValueHandlerProvider;

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {}

    @Test
    public void commonVocabularyValuesWithRel() {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyCommonDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.vocabularyConceptsWithRelationships() );
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(ExportMocks.vocabularyConceptRelationshipNames());

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.commonDataElementWithVocabularyValuesWithRelationshipsExportCSV();
        Assert.assertThat("Code Item to CSV", actual, CoreMatchers.is(expected));
    }

    @Test
    public void vocabularyValuesWithRel() {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.vocabularyConceptsWithRelationships() );
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(ExportMocks.vocabularyConceptRelationshipNames());
        
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.uncommonDataElementWithVocabularyValuesWithRelationshipsExportCSV();
        Assert.assertThat("Code Item to CSV", actual, CoreMatchers.is(expected));
    }

    @Test
    public void commonVocabularyValues() {
        List<eionet.meta.DataElement> elements = ExportMocks.vocabularyCommonDataElementSimple();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.vocabularyConcepts() );
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.VOCABULARY)).thenReturn(mockCodeValueHandler);
        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.commonDataElementWithVocabularyValuesExportCSV();
        Assert.assertThat("Code Item to CSV", actual, CoreMatchers.is(expected));
    }

    @Test
    public void commonFixedValues() {
        List<eionet.meta.DataElement> elements = ExportMocks.commonFixedValueDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.fixedValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);
        
        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.commonDataElementWithFixedValuesExportCSV();
        Assert.assertThat("Code Item to CSV", actual,  CoreMatchers.is(expected));
    }

    @Test
    public void uncommonFixedValues(){
        List<eionet.meta.DataElement> elements = ExportMocks.uncommonFixedValueDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.fixedValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);
        
        // Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get(DataElement.DataElementValueType.FIXED)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.uncommonDataElementWithFixedValuesExportCSV();
        Assert.assertThat("Code Item to CSV", actual,  CoreMatchers.is(expected));
    }

    @Test
    public void quantitativeValues()  {
        List<eionet.meta.DataElement> elements = ExportMocks.quantitativeDataElement();

        // Type: ELM, TBL, DST
        String objType = "ELM";

        // Mock Code Handler
        Mockito.when(mockCodeValueHandler.getCodeItemList()).thenReturn( ExportMocks.quantitativeValues());
        Mockito.when(mockCodeValueHandler.getRelationshipNames()).thenReturn(null);

        //Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get( DataElement.DataElementValueType.QUANTITIVE)).thenReturn(mockCodeValueHandler);

        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.quantitativeValuesExportCSV();
        Assert.assertThat("Code Item to CSV", actual,  CoreMatchers.is(expected));
    }

}
