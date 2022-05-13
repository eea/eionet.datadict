package eionet.datadict.services.impl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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

public class DataSetServiceTestIT {

    @Autowired
    DataSetService dataSetService;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-datasetIT.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-datasetIT.xml");
    }
    @Test
    public void testGetDataSetXMLSchema() throws XmlExportException, ResourceNotFoundException, TransformerConfigurationException, TransformerException, SAXException, ParserConfigurationException, IOException {
        Document XMlDocument = dataSetService.getDataSetXMLSchema(2827);
        javax.xml.transform.Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult actualXMLResult = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(XMlDocument);
        transformer.transform(source, actualXMLResult);
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetXMLSChemaTestIT.xsd"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedXMLResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualXMLResult.getWriter().toString()));
        assertTrue(diff.similar());
    }

    @Test
    public void testGetDataSetXMLInstance() throws XmlExportException, TransformerException, SAXException, IOException, ResourceNotFoundException {
        Document XMlDocument = dataSetService.getDataSetXMLInstance(2827);
        javax.xml.transform.Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult actualXMLResult = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(XMlDocument);
        transformer.transform(source, actualXMLResult);
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetXMLInstanceTestIT.xml"));
        Diff diff = new Diff(StringUtils.trimWhiteSpacesFromStringifiedXml(expectedXMLResultString), StringUtils.trimWhiteSpacesFromStringifiedXml(actualXMLResult.getWriter().toString()));
        assertTrue(diff.similar());
    }

}
