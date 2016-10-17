package eionet.datadict.model;

import java.util.Set;
import javax.persistence.OneToMany;

public class SimpleAttributeFixedValues extends SimpleAttribute implements FixedValuesOwner {

    @OneToMany(mappedBy = "owner")
    private Set<FixedValue> fixedValues;
    
    @Override
    public DisplayType getDisplayType() {
        return DisplayType.DROPDOWN_FIXED_VALUES;
    }

    @Override
    public boolean supportsValueList() {
        return true;
    }

    @Override
    public Iterable<FixedValue> getValueList() {
        return fixedValues;
    }
    
    @Override
    public Set<FixedValue> getFixedValues() {
        return fixedValues;
    }
    
    @Override
    public void setFixedValues(Set<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }
    
}
