package eionet.meta.exports.xmlinst;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDSearchEngine;

public class TblXmlInstTestIT extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-element-schema.xml";
    }

    @Test
    public void testTableSchema() throws Exception {
        DDSearchEngine ddSearchEngine = new DDSearchEngine(getConnection().getConnection());

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        TblXmlInst tbl = new TblXmlInst(ddSearchEngine, printWriter);

        tbl.write("1");
        tbl.flush();

        writer.flush();
        printWriter.flush();
        String output = writer.toString();

        printWriter.close();
        writer.close();
        String expectedElem1 = "<skos:Concept></skos:Concept>";
        String expectedElem2 = "<dd707:IPPC></dd707:IPPC>";
        assertTrue(output.indexOf(expectedElem1) != -1);
        assertTrue(output.indexOf(expectedElem2) != -1);

        //header must contain external schema:
        String expectedSchema = "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"";
        assertTrue(StringUtils.contains(output, expectedSchema));



    }



}
