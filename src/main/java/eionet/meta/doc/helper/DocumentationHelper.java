package eionet.meta.doc.helper;

import eionet.doc.DocumentationService;
import eionet.doc.dto.DocPageDTO;
import eionet.meta.spring.SpringApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class DocumentationHelper {

    private ApplicationContext ctx;
    
    public DocumentationHelper() {
        this(SpringApplicationContext.getContext());
    }
    
    public DocumentationHelper(ApplicationContext ctx) {
        this.ctx = ctx;
    }
    
    public DocPageDTO listDocumentationItems() throws Exception {
        DocumentationService docService = this.ctx.getBean(DocumentationService.class);
        
        return docService.view(null, null);
    }
}
