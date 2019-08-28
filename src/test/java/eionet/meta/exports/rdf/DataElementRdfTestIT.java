package eionet.meta.exports.rdf;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.meta.dao.domain.DataElement;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * Tests on ElementRDFWriter.
 *
 * @author Kaido Laine
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
public class DataElementRdfTestIT {

    /**
     * tests if rdf output is correct.
     * @throws Exception if error
     */
    @Test
    public void testWriteElements() throws Exception {
        List<DataElement> elements = new ArrayList<DataElement>();

        DataElement e1 = new DataElement();
        e1.setIdentifier("element1");
        Map<String, List<String>> elemAttributeValues =   new HashMap<String, List<String>>();
        List<String> name1 = new ArrayList<String>();
        name1.add("Test Element 1");
        elemAttributeValues.put("Name", name1);

        e1.setElemAttributeValues(elemAttributeValues);
        elements.add(e1);

        DataElement e2 = new DataElement();
        e2.setIdentifier("element2");
        Map<String, List<String>> elemAttributeValues2 = new HashMap<String, List<String>>();
        List<String> name2 = new ArrayList<String>();
        name2.add("Test Element 2");
        elemAttributeValues2.put("Name", name2);

        e2.setElemAttributeValues(elemAttributeValues2);
        elements.add(e2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataElementXmlWriter writer = new DataElementXmlWriter(out);

        writer.writeRDFXml("http://localhost:8080/datadict/property/", elements);

        String output = out.toString("utf-8");

        String rdfProperty2 = "<rdf:Property rdf:about=\"element2\">";
        String rdfsLabel1 = "<rdfs:label>Test Element 1</rdfs:label>";

        Assert.assertTrue("No rdf:PropertyElement for test element2", StringUtils.contains(output, rdfProperty2));
        Assert.assertTrue("No rdfs:labelfor test element1", StringUtils.contains(output, rdfsLabel1));
    }
}
