/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.doc.DocumentationService;
import eionet.doc.dto.DocPageDTO;
import eionet.help.Helps;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

/**
 *
 * @author eworx-alk
 */
@UrlBinding("/")
public class WelcomePageActionBean extends AbstractActionBean {

    public static final String WELCOME_PAGE = "/pages/welcome.jsp";

    @SpringBean
    private DocumentationService documentationService;

    private DocPageDTO pageObject;
    private String pageNews;
    private String pageSupport;

    public DocPageDTO getPageObject() {
        return pageObject;
    }

    public void setPageObject(DocPageDTO pageObject) {
        this.pageObject = pageObject;
    }

    public String getPageSupport() {
        return pageSupport;
    }

    public String getPageNews() {
        return pageNews;
    }

    @DefaultHandler
    public Resolution welcome() {
        pageNews = getFrontPageNews();
        pageSupport = getFrontPageSupport();
        try {
            pageObject =  getDocumentation();
         } catch (Exception e) {
             String msg = "Documentation Service Error";
             return super.createErrorResolution(ErrorActionBean.ErrorType.UNKNOWN, msg);
        }
        return new ForwardResolution(WELCOME_PAGE);
    }

    protected DocPageDTO getDocumentation() throws Exception {   
         return this.documentationService.view(" ", "view");
    }
    
    protected String getFrontPageNews() {
        return Helps.get("front_page", "news");
    }

    protected String getFrontPageSupport() {
        return Helps.get("front_page", "support");
    }
}
