/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eionet.meta.exports.DDObjectMapperProvider;
import static eionet.meta.exports.codelist.ExportStatics.*;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@JsonRootName(namespace = DD_NAMESPACE, value = "value-lists")
public class ExportElement {
    
    private static final Logger LOGGER = Logger.getLogger(ExportElement.class);
    
    @JsonIgnore
    private boolean datasetAware;
    
    private final String xsiSchema = XSI_NAMESPACE;
    
    private List<Element> elements;

    public boolean isDatasetAware() {return datasetAware;}
    public void setDatasetAware(boolean datasetAware) {this.datasetAware = datasetAware;}
    
    @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
    public String getXsiSchema() {return xsiSchema;}

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "value-list" )
    public List<Element> getElements() {return elements;}
    public void setElements(List<Element> elements) {this.elements = elements;}

    
    /**
     * Return this CodeItem as CSV 
     * 
     * @return 
     */
    String toCSV(){
        StringBuilder str = new StringBuilder("");
        for( Element el: elements ){
            str.append( el.toCSV(datasetAware) );
            
            str.append(CSV_NEW_LINE);
        }
        return str.toString();
    }
    
    /**
     * Return this CodeItem as XML
     * 
     * @param mapper
     * @return 
     */
    String toXML(){
        try {
            ObjectMapper mapper = DDObjectMapperProvider.get();
            return mapper.writeValueAsString( this );
        } catch ( JsonProcessingException jpe ){
            LOGGER.error("Failed to export element to XML", jpe);
            return "";
        }
        
    }
    public static class Element{
        
        @JacksonXmlProperty(isAttribute = true, localName = "element")
        private String identifier;
        @JacksonXmlProperty(isAttribute = true, localName = "table")
        private String tableIdentifier;
        @JacksonXmlProperty(isAttribute = true, localName = "dataset")
        private String datasetIdentifier;
        @JacksonXmlProperty(isAttribute = true, localName = "fixed")
        private boolean fixed;
        
        @JsonIgnore
        private List<String> relationshipNames;
        
        private List<CodeItem> values;

        public String getIdentifier() {return identifier;}
        public void setIdentifier(String identifier) {this.identifier = identifier;}

        public String getTableIdentifier() {return tableIdentifier;}
        public void setTableIdentifier(String tableIdentifier) {this.tableIdentifier = tableIdentifier;}
       
        public String getDatasetIdentifier() {return datasetIdentifier;}
        public void setDatasetIdentifier(String datasetIdentifier) {this.datasetIdentifier = datasetIdentifier;}

        public boolean isFixed() {return fixed;}
        public void setFixed(boolean fixed) {this.fixed = fixed;}

        public List<String> getRelationshipNames() {return relationshipNames;}
        public void setRelationshipNames(List<String> relationshipNames) {this.relationshipNames = relationshipNames;}

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "value")
        public List<CodeItem> getValues() {return values;}
        public void setValues(List<CodeItem> values) {this.values = values;}
        
        String toCSV( boolean datasetAware ){
       
            StringBuilder header = new StringBuilder("");
            StringBuilder list = new StringBuilder();
            
            //Dataset / Table Upper Header
            if ( datasetAware ){
                header.append(CSV_HEADER_DATASET ).append(CSV_DELIMITER_LABEL).append( datasetIdentifier ).append(CSV_DELIMITER_SPACE);
                header.append(CSV_HEADER_TABLE ).append(CSV_DELIMITER_LABEL).append( tableIdentifier).append(CSV_DELIMITER_SPACE);
            }
            //Element name Upper Header
            header.append(CSV_HEADER_ELEMENT).append(CSV_DELIMITER_LABEL).append( identifier ).append(CSV_DELIMITER_SPACE);
            
            //Fixed Upper Header 
            header.append(CSV_HEADER_FIXED).append(CSV_DELIMITER_LABEL).append( fixed );
            
            //New line
            header.append(CSV_NEW_LINE );
            
            if ( values == null || values.isEmpty() ){
                return header.toString();
            }
            //Code-Label-Definition Header
            header.append( CSV_HEADER_CODEVALUE );
            
            //Add relationship names to Header
            String relName = StringUtils.join( relationshipNames, CSV_DELIMITER_COMMA);
            if (relName != null && relName.length() > 0) {
                header.append(CSV_DELIMITER_COMMA).append(relName);
            }
            
            for (CodeItem value : values){
                list.append( CSV_NEW_LINE );
                list.append( value.toCSV() );
            }
         
            return header.toString() + list.toString() + CSV_NEW_LINE;
        }

    }
}
