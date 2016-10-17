package eionet.datadict.model;

import java.util.Set;
import javax.persistence.OneToMany;

public class DataElementWithFixedValues extends DataElement implements FixedValuesOwner {
    
    @OneToMany(mappedBy = "owner")
    private Set<FixedValue> fixedValues;
    
    @Override
    public SimpleAttributeOwnerCategory getSimpleAttributeOwnerCategory() {
        return SimpleAttributeOwnerCategory.DATA_ELEMENT_WITH_VALUE_LIST;
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.FIXED;
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
