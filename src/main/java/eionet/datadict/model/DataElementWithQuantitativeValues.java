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
    public AttributeOwnerCategory getAttributeOwnerCategory() {
        return AttributeOwnerCategory.DATA_ELEMENT_WITH_QUANTITATIVE_VALUES;
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

    @Override
    public Set<Attribute> getAttributes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributes(Set<Attribute> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<AttributeValue> getAttributesValues() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributesValues(Set<AttributeValue> attributesValues) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
