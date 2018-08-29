
package eionet.datadict.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Component
public class ApacheJenaHelper {
    
    
    private  String BASE="";

    
    private ApacheJenaHelper(){
    }
    
   // @Autowired
    public ApacheJenaHelper(String BASE) {
        this.BASE = BASE;
    }
    
    
     public  Resource r ( String localname ) {
        return ResourceFactory.createResource ( BASE + localname );
    }
    
    public Property p ( String localname ) {
        return ResourceFactory.createProperty ( BASE, localname );
    }

   public Literal l ( Object value ) {
        return ResourceFactory.createTypedLiteral ( value );
    }

   public Literal l ( String lexicalform, RDFDatatype datatype ) {
        return ResourceFactory.createTypedLiteral ( lexicalform, datatype );
    }
}
