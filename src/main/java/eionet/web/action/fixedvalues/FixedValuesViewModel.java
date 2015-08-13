package eionet.web.action.fixedvalues;

import eionet.meta.dao.domain.FixedValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public final class FixedValuesViewModel {
    
    private String actionBeanName;
    
    private FixedValueOwnerDetails owner;
    private final List<FixedValue> fixedValues;
    private FixedValueCategory fixedValueCategory;
    private boolean defaultValueRequired;

    public FixedValuesViewModel() {
        this.fixedValues = new ArrayList<FixedValue>();
    }

    public String getActionBeanName() {
        return actionBeanName;
    }

    public void setActionBeanName(String actionBeanName) {
        this.actionBeanName = actionBeanName;
    }
    
    public FixedValueOwnerDetails getOwner() {
        return owner;
    }

    public void setOwner(FixedValueOwnerDetails owner) {
        this.owner = owner;
    }

    public Iterable<FixedValue> getFixedValues() {
        return fixedValues;
    }
    
    public void setFixedValues(Collection<FixedValue> fixedValues) {
        this.fixedValues.clear();
        this.fixedValues.addAll(fixedValues);
        Collections.sort(this.fixedValues, new Comparator<FixedValue>() {

            @Override
            public int compare(FixedValue o1, FixedValue o2) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
        });
    }
    
    public FixedValueCategory getFixedValueCategory() {
        return fixedValueCategory;
    }

    public void setFixedValueCategory(FixedValueCategory fixedValueCategory) {
        this.fixedValueCategory = fixedValueCategory;
    }

    public boolean isDefaultValueRequired() {
        return defaultValueRequired;
    }

    public void setDefaultValueRequired(boolean defaultValueRequired) {
        this.defaultValueRequired = defaultValueRequired;
    }
    
    public FixedValue getFixedValue() {
        return this.fixedValues.isEmpty() ? null : this.fixedValues.get(0);
    }
    
    public void setFixedValue(FixedValue fixedValue) {
        if (this.fixedValues.isEmpty()) {
            this.fixedValues.add(0, fixedValue);
        }
        else {
            this.fixedValues.set(0, fixedValue);
        }
    }
    
    public String getFixedValueCategoryUpper() {
        switch (this.getFixedValueCategory()) {
            case ALLOWABLE: return "Allowable";
            case SUGGESTED: return "Suggested";
            default: return null;
        }
    }
    
    public String getFixedValueCategoryLower() {
        switch (this.getFixedValueCategory()) {
            case ALLOWABLE: return "allowable";
            case SUGGESTED: return "suggested";
            default: return null;
        }
    }
}
