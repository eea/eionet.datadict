package eionet.meta.doc.helper;

import eionet.doc.dto.DocPageDTO;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DocumentationHelperTest {

    private static ApplicationContext ctx;
    
    @BeforeClass
    public static void setUpClass() {
         ctx = new GenericXmlApplicationContext("classpath:mock-spring-context.xml");
     
    }
    
    @Test
    public void testListDocumentationItems() throws Exception {
        DocumentationHelper docHelper = new DocumentationHelper(ctx);
        DocPageDTO docs = docHelper.listDocumentationItems();
        
        assertNotNull(docs);
        assertTrue(docs.getDocs() == null || docs.getDocs().isEmpty());
    }
}
