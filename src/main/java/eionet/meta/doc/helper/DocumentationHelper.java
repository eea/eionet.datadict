package eionet.meta.doc.helper;

import eionet.doc.DocumentationService;
import eionet.doc.dto.DocPageDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DocumentationHelper {

    private ApplicationContext ctx;
    
    public DocumentationHelper() {
        this(new GenericXmlApplicationContext("classpath:spring-context.xml"));
    }
    
    public DocumentationHelper(ApplicationContext ctx) {
        this.ctx = ctx;
    }
    
    public DocPageDTO listDocumentationItems() throws Exception {
        DocumentationService docService = this.ctx.getBean(DocumentationService.class);
        
        return docService.view(null, null);
    }
}
