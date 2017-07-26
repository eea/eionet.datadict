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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eionet.meta.exports.codelist.ExportStatics.*;
import java.util.List;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@JsonRootName(namespace = DD_NAMESPACE, value = "value-lists")
public class ExportElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportElement.class);

    //@JsonIgnore
    private ObjectMapper mapper;

    @JsonIgnore
    private boolean datasetAware;

    private final String xsiSchema = XSI_NAMESPACE;

    private List<Element> elements;

    public ExportElement() {}

    public ExportElement(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public boolean isDatasetAware() {
        return datasetAware;
    }

    public void setDatasetAware(boolean datasetAware) {
        this.datasetAware = datasetAware;
    }

    @JacksonXmlProperty(localName = "xmlns:xsi", isAttribute = true)
    public String getXsiSchema() {
        return xsiSchema;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "value-list" )
    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }
    
    /**
     * Return this CodeItem as CSV 
     * 
     * @return 
     */
    String toCSV() {
        StringBuilder str = new StringBuilder("");
        for (Element el: elements) {
            str.append(el.toCSV(datasetAware));
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
    String toXML() {
        try {
            if (mapper == null) {
                //use default mapper
                mapper = DDObjectMapperProvider.get();
            }
            else {
                //use legacy mapper
                mapper = DDObjectMapperProvider.getLegacy();
            }
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
            LOGGER.error("Failed to export element to XML", jpe);
            return "";
        }
    }

}
