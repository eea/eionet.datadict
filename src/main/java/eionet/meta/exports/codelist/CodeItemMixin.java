/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports.codelist;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import static eionet.meta.exports.codelist.ExportStatics.DD_NAMESPACE;

/**
 * Adjust the XML element names so that:
 * "value" attribute instead of "code"
 * "shortDescription" element instead of "label"
 * as per issue https://taskman.eionet.europa.eu/issues/29737
 * @author Lena KARGIOTI eka@eworx.gr
 */
public abstract class CodeItemMixin {
    
    @JacksonXmlProperty(isAttribute = true, localName = "value")
    abstract String getCode();
    
    @JacksonXmlProperty(namespace = DD_NAMESPACE, localName = "shortDescription")
    abstract String getLabel();
    
}
