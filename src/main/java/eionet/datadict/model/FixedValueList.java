package eionet.datadict.model;

import eionet.datadict.util.IteratorUpcastAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FixedValueList implements ValueList {

    private final List<FixedValue> fixedValues;
    
    public FixedValueList(Collection<FixedValue> fixedValues) {
        this.fixedValues = new ArrayList<FixedValue>(fixedValues);
    }
    
    @Override
    public Iterator<ValueListItem> iterator() {
        return new IteratorUpcastAdapter<ValueListItem, FixedValue>(this.fixedValues.iterator());
    }
    
}
