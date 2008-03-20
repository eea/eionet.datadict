package eionet.meta.imp;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.XMLReader;

import eionet.meta.DDUser;
import eionet.meta.TestUser;
import eionet.test.Seed;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.sql.ConnectionUtil;

/**
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 * This class contains unit tests for <code>eionet.meta.DatasetImportHandler</code>.
 * 
 */
public class DatasetImportHandlerTest extends TestCase {
	
	/**
	 * Imports the contents of a file with a given systemID into DD database. The file is expected to be in the XML
	 * format that is produced by DD's MS-Access import tool and it is expected that the file contains import data for
	 * a dataset or several datasets only. i.e. it is not expected that the file contains an import for fixed values only.
	 * 
	 * The method expects <code>java.sql.Connection</code> object given via
	 * @param systemID system ID of the file to be imported
	 * @param conn <code>java.sql.Connection</code> to the DD database
	 */
	public DatasetImport simpleDatasetImport(String systemID, Connection conn, DDUser user) throws Exception{
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        
        DatasetImportHandler handler = new DatasetImportHandler();
        reader.setContentHandler(handler);

        reader.parse(systemID);
        
        if (!handler.hasError()){

            DatasetImport dstImport =
                new DatasetImport(handler, conn, null);
            
			dstImport.setUser(user);
			dstImport.setDate(String.valueOf(System.currentTimeMillis()));
            dstImport.setImportType("DST");
            dstImport.execute();
            
            return dstImport;
        }
        else
            throw new Exception(handler.getErrorBuff().toString());
	}

	/**
	 * @throws Exception 
	 *
	 */
	public void testSimpleDatasetImport() throws Exception{
		
		TestUser testUser = new TestUser();
        testUser.authenticate("heinlja", "");
        
		DatasetImport dstImport = simpleDatasetImport(
				getClass().getClassLoader().getResource(Seed.DST_IMPORT).getFile(), ConnectionUtil.getSimpleConnection(), testUser);
		
		assertEquals((int)0, dstImport.getErrorCount());
		assertEquals((int)0, dstImport.getWarningCount());
		assertEquals(dstImport.getCountDatasetsImported(), dstImport.getCountDatasetsFound());
		assertEquals(dstImport.getCountTablesImported(), dstImport.getCountTablesFound());
		assertEquals(dstImport.getCountElementsImported(), dstImport.getCountElementsFound());
	}
}
