package eionet.web.action;

import eionet.datadict.model.Attribute;
import eionet.datadict.services.data.AttributeDataService;
import java.util.List;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

@UrlBinding("/attributes")
public class AttributesActionBean extends AbstractActionBean {

    public static final String ATTRIBUTES_PAGE = "/pages/attributes/attributes.jsp";

    @SpringBean
    private AttributeDataService attributeDataService;

    private List<Attribute> attributes;

    public AttributeDataService getAttributeDataService() {
        return attributeDataService;
    }

    public void setAttributeDataService(AttributeDataService attributeDataService) {
        this.attributeDataService = attributeDataService;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @DefaultHandler
    public Resolution view() {
        attributes = attributeDataService.getAllAttributes();
        return new ForwardResolution(ATTRIBUTES_PAGE);
    }
    
}
