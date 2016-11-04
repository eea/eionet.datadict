package eionet.meta;

import eionet.doc.DocumentationService;
import eionet.doc.io.FileService;
import eionet.doc.dto.DocPageDTO;
import eionet.doc.dto.MessageDTO;
import eionet.doc.extensions.stripes.DocumentationValidator;
import eionet.meta.service.DBUnitHelper;

import java.util.List;
import org.junit.AfterClass;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Integration test to verify that the eionet.doc module works correctly.
 * As long as DD doesn't configure where to load the doc module's configuration
 * this test shows that the fallback to properties file works.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class DocModuleTest {

    @Autowired
    private FileService fileService;
    
    @Autowired
    private DocumentationService documentationService;
    
    @Autowired
    private DocumentationValidator validator;
    
    @BeforeClass
    public static void createDataSource() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.loadData("seed-documentation.xml");
    }
    
    @AfterClass
    public static void removeDataSource() throws Exception {
        ActionBeanUtils.getServletContext();
        DBUnitHelper.deleteData("seed-documentation.xml");
    }

    @Test
    public void fileServiceFolderAvailable() throws Exception {
        assertNotNull("There must be a doc.files.folder property", this.fileService);
    }

    @Test
    public void validatorInstance() {
        assertNotNull("Validator instanciated successfully", this.validator);
    }
    
    @Test
    public void getNonExistentPage() throws Exception {
        DocPageDTO page = this.documentationService.view("nosuchpage", "");
        assertNotNull("No test page", page);
        
        List<MessageDTO> msgs = page.getMessages();
        MessageDTO firstMsg = msgs.get(0);
        assertEquals("Such page ID doesn't exist in database: nosuchpage", firstMsg.getMessage());
    }

    @Test
    public void getAboutPage() throws Exception {
        DocPageDTO page = this.documentationService.view("about", "");
        assertNotNull("No about page", page);
        assertEquals("About page", page.getTitle());
    }
}
