package eionet.datadict.model;

import java.util.Set;
import javax.persistence.OneToMany;

public class DataElementWithQuantitativeValues extends DataElement implements FixedValuesOwner {
    
    @OneToMany(mappedBy = "owner")
    private Set<FixedValue> fixedValues;

    public DataElementWithQuantitativeValues() {
        super();
    }

    public DataElementWithQuantitativeValues(Integer id) {
        super(id);
    }
    
    @Override
    public AttributeOwnerType getAttributeOwnerType() {
         return new AttributeOwnerType(super.getId(),AttributeOwnerType.Type.DATA_ELEMENT_WITH_QUANTITATIVE_VALUES);
     
    }
    
    @Override
    public ValueType getValueType() {
        return ValueType.QUANTITATIVE;
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
