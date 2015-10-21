/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.exports;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eionet.meta.exports.codelist.CodeItem;
import eionet.meta.exports.codelist.CodeItemMixin;
import javax.xml.stream.XMLInputFactory;

/**
 * Default XML Mapper for Jackson XML de- /serialization
 * 
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class DDObjectMapperProvider {
    private static XmlMapper mapper = null;
   
    protected DDObjectMapperProvider() {
      // Exists only to defeat instantiation.
    }
    
    public static XmlMapper get(){
        if ( mapper == null ){
            XMLInputFactory input = new WstxInputFactory();
            input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            mapper = new XmlMapper( new XmlFactory(input, new WstxOutputFactory() {}));
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
        return mapper;
    }
    
    /**
     * Returns an ObjectMapper which uses 
     * "value" attribute instead of "code"
     * "shortDescription" element instead of "label"
     * as per issue https://taskman.eionet.europa.eu/issues/29737
     * @return 
     */
    public static XmlMapper getLegacy(){
        XMLInputFactory input = new WstxInputFactory();
        input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        
        XmlMapper anotherMapper =  new XmlMapper( new XmlFactory(input, new WstxOutputFactory() {}));
        anotherMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        anotherMapper.setSerializationInclusion(Include.NON_NULL);
        
        //Add the CodeItem mixin
        anotherMapper.addMixInAnnotations(CodeItem.class, CodeItemMixin.class);
        
        return anotherMapper;
    }
}
