package eionet.meta.imp;

import java.io.InputStream;
import java.sql.Connection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.test.Seed;

/**
 * This class contains unit tests for eionet.meta.DatasetImport. It is essentially a copy of the main() routine that was used to
 * demonstrate the usage.
 *
 * @author Jaanus Heinlaid
 */
public class DatasetImportTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-emptydb.xml";
    }

    /**
     * @throws Exception
     *             if it fails.
     */
    public void testXMLDatasetImport() throws Exception {

        Connection conn = null;
        conn = getConnection().getConnection();

        DatasetImportHandler handler = new DatasetImportHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setContentHandler(handler); // pass our handler to SAX

        InputStream xmlInputStream = getClass().getClassLoader().getResourceAsStream(Seed.DST_IMPORT);
        reader.parse(new InputSource(xmlInputStream));

        assertFalse("Handler complained", handler.hasError());

        // SAX was OK, but maybe handler has problems of its own
        StringBuffer responseText = new StringBuffer();

        DDUser testUser = new FakeUser();
        testUser.authenticate("jaanus", "jaanus");

        // TODO: You can no longer provide null as the servlet context, so this fails

        DatasetImport dbImport = new DatasetImport(handler, conn, null);

        dbImport.setUser(testUser);
        dbImport.setDate(String.valueOf(System.currentTimeMillis()));
        dbImport.setImportType("DST");
        dbImport.execute();

        responseText.append(dbImport.getResponseText());
        System.out.println(responseText.toString());

    }
}
