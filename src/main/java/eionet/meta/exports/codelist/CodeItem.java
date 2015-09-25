/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import static eionet.meta.exports.codelist.ExportStatics.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;


/**
 * Help object to encapsulate both Fixed and Vocabulary Value Code Items
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class CodeItem {
    
    @JacksonXmlProperty(isAttribute = true, localName = "code")
    private String code;
    @JacksonXmlProperty(namespace = DD_NAMESPACE)
    private String label;
    @JacksonXmlProperty(namespace = DD_NAMESPACE)
    private String definition;
    @JacksonXmlProperty(namespace = DD_NAMESPACE)
    private String notation;
    
    @JacksonXmlElementWrapper(namespace = DD_NAMESPACE, localName = "relationship-list")
    @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "relationship")
    private List<RelationshipInfo> relationships;
    
    public CodeItem(){}
    
    public CodeItem( String code, String label, String definition ){
        this.code = code;
        this.label = label;
        this.definition = definition;
    }
    public CodeItem( String code, String label, String definition, String notation ){
        this.code = code;
        this.label = label;
        this.definition = definition;
        this.notation = notation;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getNotation() {
        return notation;
    }
    public void setNotation(String notation) {
        this.notation = notation;
    }
    
    public List<RelationshipInfo> getRelationships(){
        return this.relationships;
    }
    public void setRelationships(List<RelationshipInfo> relationships){
        this.relationships = relationships;
    }
    public void addRelationship( RelationshipInfo relationship ){
        if ( this.relationships == null ){
            this.relationships = new ArrayList<RelationshipInfo>();
        }
        this.relationships.add(relationship);
    }
    
    /**
     * Return this CodeItem as CSV line
     * 
     * @return 
     */
    String toCSV(){
        List<String> parts = new ArrayList<String>();
        parts.add(code);
        parts.add(label);
        parts.add(definition);
        
        if ( relationships != null ){
            List<String> infoParts = new ArrayList<String>();
            for ( RelationshipInfo info : relationships ){
                if ( info.getItems().isEmpty() )
                    continue;
                infoParts.add( info.toCSV() );
            }
            parts.add( StringUtils.join(infoParts, CSV_DELIMITER_LIST) );
        }
        return StringUtils.join(parts, CSV_DELIMITER_COMMA);
    }
    
    /**
     * Utility class for describing the relationship between this CodeItem and others
     */
    public static class RelationshipInfo{
        @JacksonXmlProperty(isAttribute = true)
        private String attribute;
        @JacksonXmlProperty(isAttribute = true)
        private String vocabulary;
        @JacksonXmlProperty(isAttribute = true)
        private String vocabularySet;
        
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "value")
        private List<CodeItem> items;
        
        public RelationshipInfo(){}
        
        public RelationshipInfo(String attribute, String vocabulary, String vocabularySet, List<CodeItem> items){
            this.attribute = attribute;
            this.vocabulary = vocabulary;
            this.vocabularySet = vocabularySet;
            this.items = items;
        }

        public String getAttribute() { return attribute;}
        public void setAttribute(String attribute) {this.attribute = attribute;}

        public String getVocabulary() {return vocabulary;}
        public void setVocabulary(String vocabulary) {this.vocabulary = vocabulary;}

        public String getVocabularySet() { return vocabularySet; }
        public void setVocabularySet(String vocabularySet) { this.vocabularySet = vocabularySet; }

        public List<CodeItem> getItems() {return items;}
        public void setItems(List<CodeItem> items) {this.items = items;}
        
        String toCSV(){
            String prefix = vocabularySet + "::" + vocabulary + "::";
            List<String> str = new ArrayList<String>();
            for ( CodeItem rel : items){
                str.add( rel.getNotation() );
            }
            String itemStr = StringUtils.join(str, CSV_DELIMITER_COMMA );
            return prefix+itemStr;
        }
        
    }
}
