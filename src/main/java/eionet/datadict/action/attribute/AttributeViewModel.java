package eionet.datadict.action.attribute;

import eionet.datadict.model.AttributeDefinition;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eworx-alk
 */
public final class AttributeViewModel {
    
    private String submitActionBeanName;
    
    private AttributeDefinition attributeDefinition;
    private Map<String, String> displayForTypes;
    private List<FixedValue> fixedValues;

    public String getSubmitActionBeanName() {
        return submitActionBeanName;
    }

    public void setSubmitActionBeanName(String submitActionBean) {
        this.submitActionBeanName = submitActionBean;
    }
    
    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public Map<String, String> getDisplayForTypes() {
        return displayForTypes;
    }

    public void setDisplayForTypes(Map<String, String> displayForTypes) {
        this.displayForTypes = displayForTypes;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}
