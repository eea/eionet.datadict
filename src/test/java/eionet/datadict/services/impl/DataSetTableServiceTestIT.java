package eionet.datadict.services.impl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.util.StringUtils;
import eionet.meta.service.DBUnitHelper;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class
    })

public class DataSetTableServiceTestIT {

    @Autowired
    DataSetTableService dataSetTableService;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-datasetTableIT.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-datasetTableIT.xml");
    }
    @Test
    public void testGetDataSetTableXMlSchema() throws XmlExportException, ResourceNotFoundException, TransformerException, IOException, SAXException {
        Document xmlDocument = dataSetTableService.getDataSetTableXMLSchema(6661);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult actualXMLResult = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDocument);
        transformer.transform(source, actualXMLResult);
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLSchemaTestIT-2.xsd"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedXMLResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualXMLResult.getWriter().toString()));
        assertTrue(diff.similar());
    }

    @Test
    public void testGetDataSetTableXMLInstace() throws XmlExportException, TransformerConfigurationException, TransformerException, IOException, SAXException, ResourceNotFoundException {
        Document xmlDocument = dataSetTableService.getDataSetTableXMLInstance(6661);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult actualXMLResult = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDocument);
        transformer.transform(source, actualXMLResult);
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLInstanceTestIT.xml"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedXMLResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualXMLResult.getWriter().toString()));
        assertTrue(diff.similar());
    }
}
