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
    
    private AttributeDefinition attributeDefinition;
    private Map<String, String> displayForTypes;
    private String obligation;
    private String displayType;
    private List<FixedValue> fixedValues;

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

    public String getObligation() {
        return obligation;
    }

    public void setObligation(String obligation) {
        this.obligation = obligation;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}
