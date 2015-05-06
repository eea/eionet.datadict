package eionet.meta;

import eionet.doc.DocumentationService;
import eionet.doc.FileService;
import eionet.doc.dto.DocPageDTO;
import eionet.doc.dto.MessageDTO;
import eionet.meta.service.DBUnitHelper;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


/**
 * Integration test to verify that the eionet.doc module works correctly.
 * As long as DD doesn't configure where to load the doc module's configuration
 * this test shows that the fallback to properties file works.
 */
public class DocModuleTest {

    @BeforeClass
    public static void createDataSource() throws Exception {
        DBUnitHelper.loadData("seed-documentation.xml");
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
