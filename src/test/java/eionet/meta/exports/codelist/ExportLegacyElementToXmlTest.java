/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.exports.DDObjectMapperProvider;
import java.io.IOException;
import java.util.List;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class ExportLegacyElementToXmlTest {

    @Mock
    private CodeValueHandler mockCodeValueHandler;
    @Mock
    private CodeValueHandlerProvider mockCodeValueHandlerProvider;

    private static ObjectMapper mapper;

    @BeforeClass
    public static void setUpClass() {
        XMLUnit.setIgnoreWhitespace(true); 
        mapper = DDObjectMapperProvider.getLegacy();
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
        codelist.setObjectMapper(mapper);
        String actual = codelist.write(elements, objType);
        String expected = ExportMocks.wrapXML( ExportMocks.quantitativeValuesLegacyExportXML());
        
        Diff diff = new Diff(expected, actual);
        DetailedDiff detDiff = new DetailedDiff(diff);
        List differences = detDiff.getAllDifferences();
        for (Object object : differences) {
            Difference difference = (Difference) object;
            System.out.println("***********************");
            System.out.println(difference);
            System.out.println("***********************");
        }

        Assert.assertTrue("Exported XML is similar", diff.similar());
    }

}
