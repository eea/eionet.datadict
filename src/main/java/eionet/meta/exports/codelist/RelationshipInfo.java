/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eionet.util.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class for describing the relationship between this CodeItem and others
 */
public class RelationshipInfo {
    @JacksonXmlProperty(isAttribute = true)
    private String attribute;
    @JacksonXmlProperty(isAttribute = true)
    private String vocabulary;
    @JacksonXmlProperty(isAttribute = true)
    private String vocabularySet;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(namespace = ExportStatics.DD_NAMESPACE, localName = "value")
    private List<CodeItem> items;

    public RelationshipInfo() {
    }

    public RelationshipInfo(String attribute, String vocabulary, String vocabularySet, List<CodeItem> items) {
        this.attribute = attribute;
        this.vocabulary = vocabulary;
        this.vocabularySet = vocabularySet;
        this.items = items;
    }

    public String getAttribute() {return attribute;}
    public void setAttribute(String attribute) {this.attribute = attribute;}

    public String getVocabulary() {return vocabulary;}
    public void setVocabulary(String vocabulary) {this.vocabulary = vocabulary;}

    public String getVocabularySet() {return vocabularySet;}
    public void setVocabularySet(String vocabularySet) {this.vocabularySet = vocabularySet;}

    public List<CodeItem> getItems() {return items;}
    public void setItems(List<CodeItem> items) {this.items = items;}

    String toCSV() {
        if (items.isEmpty()) {
            return "\"\"";
        }
        String prefix = vocabularySet + "::" + vocabulary + "::";
        List<String> str = new ArrayList<String>();
        for (CodeItem rel : items) {
            str.add(rel.getCode());
        }
        String itemStr = StringUtils.join(str, ExportStatics.CSV_DELIMITER_COMMA);
        return prefix + itemStr;
    }
    
    AttributePredicate getAttributePredicate( String attribute ){
        return new AttributePredicate( attribute );
    }
    
    static class AttributePredicate implements Predicate<RelationshipInfo>{
        private final String attribute;
        
        AttributePredicate( String atttribute ){
            this.attribute = atttribute;
        }
        @Override
        public boolean apply(RelationshipInfo type) {
            return this.attribute.equals(type.getAttribute());
        }
    }
    
}
