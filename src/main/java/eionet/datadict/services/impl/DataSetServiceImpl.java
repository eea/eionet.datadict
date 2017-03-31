package eionet.datadict.services.impl;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.DataSet;
import eionet.datadict.services.DataSetService;
import eionet.datadict.services.data.DatasetDataService;
import eionet.datadict.services.data.NamespaceDataService;
import eionet.meta.DDSearchEngine;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import eionet.datadict.model.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetServiceImpl implements DataSetService {
    
    @Autowired
    DatasetDataService datasetDataService;
    
    @Autowired
    NamespaceDataService namespaceDataService;
    
    private static final String DATASETS_NAMESPACE = "1";
    
    protected DDSearchEngine searchEngine = null;
    StringBuilder writer = new StringBuilder();
    private List<String> content = new ArrayList<String>();
    private List<String> namespaces = new ArrayList<String>();
    private List<String> imports = new ArrayList<String>();
    
    private String identitation = "";
    protected String appContext = Props.getRequiredProperty(PropsIF.DD_URL);
    private final static String NS_PREFIX = "xs:";
    
    protected String lineTerminator = "\n";
    
    protected String targetNsUrl = "";
    protected String referredNsPrefix = "";
    protected String referredNsID = "";
    
    protected Map<String, String> nonAnnotationAttributes = new HashMap<String, String>();
    
    private String containerNamespaceID = null;
    
    @Override
    public Document getDataSetXMLSchema(String id) throws XmlExportException {
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            // Create xs:schema element:
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, NS_PREFIX + "schema");
            schemaRoot.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd");
            schemaRoot.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
            //   schemaRoot.setAttribute("xmlns:dd688","http://dd.eionet.europa.eu/namespaces/688");
            // We need this:  xmlns:datasets="http://dd.eionet.europa.eu/namespaces/1"
            schemaRoot.setAttribute("xmlns:xs", appContext + "/" + Namespace.URL_PREFIX + "/" + DATASETS_NAMESPACE);
            // We need this: xmlns:isoattrs="http://dd.eionet.europa.eu/namespaces/2"
            //xmlns:ddattrs="http://dd.eionet.europa.eu/namespaces/3"   
            schemaRoot.setAttribute(id, id);
            doc.appendChild(schemaRoot);
            NameTypeElementMaker elMaker = new NameTypeElementMaker(NS_PREFIX, doc);
            // We need this:
            // xsi:schemaLocation="http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd"
            // <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">

            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        }
        
    }
    
    protected String getNamespacePrefix(Namespace ns) {
        return ns == null ? "dd" : "dd" + ns.getId();
    }
    
    private void transformAndPrintDocument(Document doc) throws TransformerException {
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(System.out);
        // If we wanted to write it to file:
        //		StreamResult result = new StreamResult(new File("C:\\file.xml"));
        transformer.transform(source, result);
    }
    
    private static class NameTypeElementMaker {

        private String nsPrefix;
        private Document doc;
        
        public NameTypeElementMaker(String nsPrefix, Document doc) {
            this.nsPrefix = nsPrefix;
            this.doc = doc;
        }
        
        public Element createElement(String elementName, String nameAttrVal, String typeAttrVal) {
            Element element = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, nsPrefix + elementName);
            if (nameAttrVal != null) {
                element.setAttribute("name", nameAttrVal);
            }
            if (typeAttrVal != null) {
                element.setAttribute("type", typeAttrVal);
            }
            return element;
        }
        
        public Element createElement(String elementName, String nameAttrVal) {
            return createElement(elementName, nameAttrVal, null);
        }
        
        public Element createElement(String elementName) {
            return createElement(elementName, null, null);
        }
    }
    
}
