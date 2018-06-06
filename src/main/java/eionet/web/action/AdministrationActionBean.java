package eionet.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/administration")
public class AdministrationActionBean extends AbstractActionBean {
    
    public static final String ADMINISTRATION_PAGE = "/pages/administration.jsp";

    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(ADMINISTRATION_PAGE);
    }

}
