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
//            mapper = new XmlMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
        return mapper;
    }
}
