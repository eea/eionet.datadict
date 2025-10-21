/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import static eionet.meta.exports.codelist.ExportStatics.*;
import eionet.util.PredicateFiltering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Help object to encapsulate both Fixed and Vocabulary Value Code Items
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class CodeItem {
    
    private String code;
    private String label;
    private String definition;
    private List<RelationshipInfo> relationships;

    @JsonIgnore
    private Element parentElement;

    public CodeItem() {}

    public CodeItem(String code, String label, String definition) {
        this.code = code;
        this.label = label;
        this.definition = definition;
    }

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JacksonXmlProperty(namespace = DD_NAMESPACE)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JacksonXmlProperty(namespace = DD_NAMESPACE)
    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Element getParentElement() {
        return parentElement;
    }

    public void setParentElement(Element parentElement) {
        this.parentElement = parentElement;
    }

    @JacksonXmlElementWrapper(namespace = DD_NAMESPACE, localName = "relationship-list")
    @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "relationship")
    public List<RelationshipInfo> getRelationships() {
        return this.relationships;
    }

    public void setRelationships(List<RelationshipInfo> relationships) {
        this.relationships = relationships;
    }

    public void addRelationship(RelationshipInfo relationship) {
        if (this.relationships == null) {
            this.relationships = new ArrayList<RelationshipInfo>();
        }
        this.relationships.add(relationship);
    }

    RelationshipInfo findRelationship(String relationshipName) {
        Collection<RelationshipInfo> col = PredicateFiltering.filter(relationships, new RelationshipInfo.AttributePredicate(relationshipName));
        if (col.isEmpty()) {
            return null;
        }
        return col.iterator().next();
    }

    /**
     * Return this CodeItem as CSV line
     * 
     * @return 
     */
    String toCSV(){
        List<String> parts = new ArrayList<String>();
        parts.add(wrap(code));
        parts.add(wrap(label));
        parts.add(wrap(definition));
        
        if (relationships != null) {
            List<String> infoParts = new ArrayList<String>();
            for (String relAttribute : parentElement.getRelationshipNames()) {
                RelationshipInfo info = this.findRelationship(relAttribute);
                infoParts.add(info == null ? "\"\"" : wrap(info.toCSV()));
            }
            parts.add(StringUtils.join(infoParts, CSV_DELIMITER_COMMA));
        }
        return StringUtils.join(parts, CSV_DELIMITER_COMMA);
    }

}
