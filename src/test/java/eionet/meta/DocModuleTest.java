package eionet.meta;

import eionet.JNDISupport;
import eionet.DataSourceSupport;
import eionet.doc.DocumentationService;
import eionet.doc.FileService;
import eionet.doc.dto.DocPageDTO;
import eionet.doc.dto.MessageDTO;

import java.util.List;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


/**
 * Integration test to verify that the eionet.doc module works correctly.
 * As long as DD doesn't configure where to load the doc module's configuration
 * this test shows that the fallback to properties file works.
 */
public class DocModuleTest {

    private static DataSource ds;

    @BeforeClass
    public static void createDataSource() throws Exception {
        ds = DataSourceSupport.getDataSource();
        loadData("/seed-documentation.xml");
    }

    private static void loadData(String seedFileName) throws Exception {
        IDatabaseConnection dbConn = new DatabaseConnection(ds.getConnection());
        IDataSet dataSet = new FlatXmlDataSet(DocModuleTest.class.getResourceAsStream(seedFileName));
        DatabaseOperation.CLEAN_INSERT.execute(dbConn, dataSet);
    }

    @Before
    public void setUpIC() throws Exception {
        JNDISupport.setUpCore();
        JNDISupport.addSubCtxToTomcat("jdbc");
        JNDISupport.addPropToTomcat("jdbc/datadict", ds);
        // Tell the Doc module under what name the datasource is located
        JNDISupport.addSubCtxToTomcat("doc");
        JNDISupport.addPropToTomcat("doc/datasourcename", "jdbc/datadict");
    }

    @After
    public void cleanUpIC() throws Exception {
        JNDISupport.cleanUp();
    }

    @Test
    public void fileServiceFolderAvailable() throws Exception {
        FileService fs = FileService.getInstance();
        assertNotNull("There must be a doc.files.folder property", fs);
    }

    @Test
    public void getNonExistentPage() throws Exception {
        DocumentationService docSvc = DocumentationService.getInstance();
        DocPageDTO page = docSvc.view("nosuchpage", "");
        assertNotNull("No test page", page);
        List<MessageDTO> msgs = page.getMessages();
        MessageDTO firstMsg = msgs.get(0);
        assertEquals("Such page ID doesn't exist in database: nosuchpage", firstMsg.getMessage());
    }

    @Test
    public void getAboutPage() throws Exception {
        DocumentationService docSvc = DocumentationService.getInstance();
        DocPageDTO page = docSvc.view("about", "");
        assertNotNull("No about page", page);
        assertEquals("About page", page.getTitle());
    }
}
