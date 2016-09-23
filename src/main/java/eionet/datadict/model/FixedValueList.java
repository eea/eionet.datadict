package eionet.datadict.model;

import java.util.Iterator;
import java.util.List;

public class FixedValueList implements ValueList<FixedValue> {

    private FixedValuesOwner owner;
    private List<FixedValue> fixedValues;
    
    public FixedValuesOwner getOwner() {
        return owner;
    }

    public void setOwner(FixedValuesOwner owner) {
        this.owner = owner;
    }

    public List<FixedValue> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<FixedValue> fixedValues) {
        this.fixedValues = fixedValues;
    }
    
    @Override
    public Iterator<FixedValue> iterator() {
        return this.fixedValues.iterator();
    }
    
}
