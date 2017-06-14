package eionet.datadict.services;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import eionet.config.ApplicationTestContext;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import static org.junit.Assert.assertTrue;
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

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
        value = "classpath:seed-datasetTableIT.xml")
public class DataSetTableServiceTestIT {

    @Autowired
    DataSetTableService dataSetTableService;

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
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLSchemaTestIT.xsd"));
        Diff diff = new Diff(expectedXMLResultString, actualXMLResult.getWriter().toString());
        assertTrue(diff.similar());
    }

    @Test
    public void testGetDataSetTableXMLInstace() throws XmlExportException, TransformerConfigurationException, TransformerException, IOException, SAXException {
        Document xmlDocument = dataSetTableService.getDataSetTableXMLInstance(6661);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult actualXMLResult = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDocument);
        transformer.transform(source, actualXMLResult);
        ClassLoader classLoader = getClass().getClassLoader();
        String expectedXMLResultString = IOUtils.toString(classLoader.getResourceAsStream("datasetTableXMLInstanceTestIT.xml"));
        Diff diff = new Diff(expectedXMLResultString, actualXMLResult.getWriter().toString());
        assertTrue(diff.similar());
    }
}
