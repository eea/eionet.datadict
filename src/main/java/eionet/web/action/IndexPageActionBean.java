/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.doc.dto.DocPageDTO;
import eionet.doc.dto.DocumentationDTO;
import eionet.help.Helps;
import eionet.meta.doc.helper.DocumentationHelper;
import java.util.List;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 *
 * @author eworx-alk
 */
@UrlBinding("/index")
public class IndexPageActionBean extends AbstractActionBean {

    public static final String INDEX_PAGE = "/pages/index.jsp";

    private String newsSection;
    private String supportSection;
    private List<DocumentationDTO> documents;

    public String getNewsSection() {
        return newsSection;
    }

    public String getSupportSection() {
        return supportSection;
    }

    public List<DocumentationDTO> getDocuments() {
        return documents;
    }

    @DefaultHandler
    public Resolution view() {
        newsSection = getFrontPageNews();
        supportSection = getFrontPageSupport();
        try {
            DocPageDTO docPageDTO = getDocumentationItems();
            documents = docPageDTO.getDocs();
         } catch (Exception e) {
             // fail-silently
        }
        return new ForwardResolution(INDEX_PAGE);
    }

    protected DocPageDTO getDocumentationItems() throws Exception {
         return new DocumentationHelper().listDocumentationItems();
    }

    protected String getFrontPageNews() {
        return Helps.get("front_page", "news");
    }

    protected String getFrontPageSupport() {
        return Helps.get("front_page", "support");
    }

}
