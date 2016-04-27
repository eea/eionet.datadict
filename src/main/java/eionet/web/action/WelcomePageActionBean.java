/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.help.Helps;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 *
 * @author eworx-alk
 */
public class WelcomePageActionBean extends AbstractActionBean {

    public static final String WELCOME_PAGE = "/pages/welcome.jsp";
    
    String helps;
    String support;

    String errorMessage;
    String errorTrace;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorTrace() {
        return errorTrace;
    }

    public String getSupport() {
        return support;
    }
    
    public String getHelps() {
        return helps;
    }

    @DefaultHandler
    public Resolution welcome() {
        HttpServletRequest request = this.getContext().getRequest();
        errorTrace = "";
        errorMessage = (String) request.getAttribute("DD_ERR_MSG");
        if (errorMessage != null) {
            errorTrace = (String) request.getAttribute("DD_ERR_TRC");
        } else {
            errorMessage = "";
        }
        helps = getFrontPageNews();
        support = getFrontPageSupport();
        return new ForwardResolution(WELCOME_PAGE);
    }
    
    protected String getFrontPageNews(){
        return Helps.get("front_page", "news");
    }
    
    protected String getFrontPageSupport(){
        return  Helps.get("front_page", "support");
    }
}
