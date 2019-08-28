package eionet.meta.doc.helper;

import eionet.doc.dto.DocPageDTO;
import eionet.meta.ActionBeanUtils;
import eionet.meta.spring.SpringApplicationContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DocumentationHelperTestIT {

    private static ApplicationContext ctx;
    
    @BeforeClass
    public static void setUpClass() {
        ActionBeanUtils.getServletContext();
        ctx = SpringApplicationContext.getContext();
    }
    
    @Test
    public void testListDocumentationItems() throws Exception {
        DocumentationHelper docHelper = new DocumentationHelper(ctx);
        DocPageDTO docs = docHelper.listDocumentationItems();
        
        assertNotNull(docs);
        assertTrue(docs.getDocs() == null || docs.getDocs().isEmpty());
    }
}
