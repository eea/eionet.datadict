package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDSearchEngine;

public class TblSchemaTestIT extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-element-schema.xml";
    }

    @Test
    public void testTableSchema() throws Exception {
        DDSearchEngine ddSearchEngine = new DDSearchEngine(getConnection().getConnection());

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        TblSchema tbl = new TblSchema(ddSearchEngine, printWriter);

        tbl.write("1");
        tbl.flush();

        writer.flush();
        printWriter.flush();
        String output = writer.toString();

        printWriter.close();
        writer.close();
        String expectedElem1 = "<xs:element ref=\"skos:Description\" minOccurs=\"1\" maxOccurs=\"1\"/>";
        String expectedElem2 = "<xs:element ref=\"dd707:IPPC\" minOccurs=\"0\" maxOccurs=\"1\"/>";
        assertTrue(output.indexOf(expectedElem1) != -1);
        assertTrue(output.indexOf(expectedElem2) != -1);

        //header must contain external schema:
        String expectedSchema = "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"";
        assertTrue(StringUtils.contains(output, expectedSchema));



    }



}
