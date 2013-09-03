package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDSearchEngine;

public class DataElemSchemaTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-element-schema.xml";
    }

    @Test
    public void testExternalElem() throws Exception {
        DDSearchEngine ddSearchEngine = new DDSearchEngine(getConnection().getConnection());

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        ElmSchema elem = new ElmSchema(ddSearchEngine, printWriter);

        elem.write("1");
        elem.flush();

        writer.flush();
        printWriter.flush();
        String output = writer.toString();

        printWriter.close();
        writer.close();
        String expectedElem1 = "<xs:element name=\"Concept\">";
        assertTrue(output.indexOf(expectedElem1) != -1);

        //header must contain external schema:
        String expectedSchema = "targetNamespace=\"http://www.w3.org/2004/02/skos/core#\"";
        assertTrue(StringUtils.contains(output, expectedSchema));



    }

    @Test
    public void testLocalElem() throws Exception {
        DDSearchEngine ddSearchEngine = new DDSearchEngine(getConnection().getConnection());

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        ElmSchema elmSchema = new ElmSchema(ddSearchEngine, printWriter);

        String appContext = "http://localhost:8080/datadict/";
        elmSchema.setAppContext(appContext);
        //header must contain external schema:

        elmSchema.write("3");
        elmSchema.flush();

        writer.flush();
        printWriter.flush();
        String output = writer.toString();
        System.out.println(output);

        printWriter.close();
        writer.close();
        String expectedElem1 = "<xs:element name=\"IPPC\">";
        assertTrue(output.indexOf(expectedElem1) != -1);
        String expectedSchema = "targetNamespace=\"" + appContext + "elements/IPPC\"";
        assertTrue(StringUtils.contains(output, expectedSchema));



    }


}
