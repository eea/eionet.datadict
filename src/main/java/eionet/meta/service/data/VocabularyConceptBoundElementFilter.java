/*
 * VocabularyConceptBoundElementFilter.java
 * 
 * Created on Dec 9, 2015
 */
package eionet.meta.service.data;

import eionet.meta.dao.domain.DataElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a bound element drop-down filter for vocabulary concepts with label and options
 * 
 * @author js
 */
public class VocabularyConceptBoundElementFilter {
    
    private int id;
    private String label;
    private Map<String, String> options = new LinkedHashMap<String, String>();

    public VocabularyConceptBoundElementFilter(DataElement dataElement) {
        this.id = dataElement.getId();
        this.label = dataElement.getIdentifier();
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

}
