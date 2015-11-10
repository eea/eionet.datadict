/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import eionet.meta.dao.domain.DataElement;
import java.util.Collections;
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
public class ExportDatasetToCSVTest {
    
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
    public void datatest() {
        
        List<eionet.meta.DataElement> elements = ExportMocks.dataset();
        
        //Type: ELM, TBL, DST
        String objType = "DST";
        
        //Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get( DataElement.DataElementValueType.FIXED )).thenReturn(mockCodeValueHandler);
        //Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get( DataElement.DataElementValueType.VOCABULARY )).thenReturn(mockCodeValueHandler);
        
        //Mock Code Handler
        //stub consecutive calls
        Mockito.when( mockCodeValueHandler.getCodeItemList() )
                //Uncommon Fixed value element
                .thenReturn( ExportMocks.fixedValues())
                //Uncommon vocabulary concept with relationships element
                .thenReturn( ExportMocks.vocabularyConceptsWithRelationships());
                
        Mockito.when( mockCodeValueHandler.getRelationshipNames() )
                //Uncommon Fixed value element
                .thenReturn(null)
                //Uncommon vocabulary concept with relationships element
                .thenReturn( ExportMocks.vocabularyConceptRelationshipNames() );
        
        
        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider );
        
        String actual = codelist.write(elements, objType);
        
        String expected = ExportMocks.datasetExportCSV();
        
        Assert.assertThat( "Code Item to CSV", actual,  CoreMatchers.is(expected) );
    }
    
    @Test
    public void emptyDatatest() {
        
        List<eionet.meta.DataElement> elements = ExportMocks.dataset();
        
        //Type: ELM, TBL, DST
        String objType = "DST";
        
        //Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get( DataElement.DataElementValueType.FIXED )).thenReturn(mockCodeValueHandler);
        //Mock Code Handler Provider 
        Mockito.when(mockCodeValueHandlerProvider.get( DataElement.DataElementValueType.VOCABULARY )).thenReturn(mockCodeValueHandler);
        
        //Mock Code Handler
        //stub consecutive calls
        Mockito.when( mockCodeValueHandler.getCodeItemList() )
                //Uncommon Fixed value element
                .thenReturn( null )
                //Uncommon vocabulary concept with relationships element
                .thenReturn( Collections.EMPTY_LIST );
                
        Mockito.when( mockCodeValueHandler.getRelationshipNames() )
                //Uncommon Fixed value element
                .thenReturn(null)
                //Uncommon vocabulary concept with relationships element
                .thenReturn( null );
        
        
        Codelist codelist = new Codelist(ExportStatics.ExportType.CSV, mockCodeValueHandlerProvider );
        
        String actual = codelist.write(elements, objType);
        
        String expected = ExportMocks.emptyDatasetExportCSV();
        
        Assert.assertThat( "Code Item to CSV", actual,  CoreMatchers.is(expected) );
    }
}
