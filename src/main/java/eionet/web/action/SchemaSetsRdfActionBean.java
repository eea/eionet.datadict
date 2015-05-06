package eionet.web.action;

import eionet.meta.exports.rdf.SchemaSetsManifestXmlWriter;
import eionet.meta.service.ISchemaService;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

/**
 * Action bean that serves RDF output of schema sets.
 * 
 * @author Nakas Nikolaos
 */
@UrlBinding("/schemasets/rdf")
public class SchemaSetsRdfActionBean extends AbstractActionBean {
    
    @SpringBean
    private ISchemaService schemaService;

    /**
     * Handles the request for viewing schema set RDF.
     *
     * @return
     */
    @DefaultHandler
    public Resolution getRdf() {
        StreamingResolution result = new StreamingResolution("application/xml") {
            
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                SchemaSetsManifestXmlWriter xmlWriter = new SchemaSetsManifestXmlWriter(response.getOutputStream(), schemaService);
                xmlWriter.writeManifestXml();
            }
        };
        
        return result;
    }
}
