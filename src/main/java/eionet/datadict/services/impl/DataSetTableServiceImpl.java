package eionet.datadict.services.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetTableServiceImpl implements DataSetTableService {

    private final DatasetTableDao datasetTableDao;
    private final DataElementDao dataElementDao;
    private final AttributeValueDao attributeValueDao;
    private final AttributeDao attributeDao;
    private final DatasetDao datasetDao;
    private final DatasetTableDataService datasetTableDataService;

    private static final String DATASETS_NAMESPACE_ID = "1";
    private static final String ISOATTRS_NAMESPACE_ID = "2";
    private static final String DDATTRS_NAMESPACE_ID = "3";

    // BELOW Static variables should be moved to an utility class  , because they are common variables for many cases
    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String NAMESPACE = "namespace";
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME = "schema-tbl-";
    private static final String DATASET_SCHEMA_LOCATION_PARTIAL_FILE_NAME = "schema-dst-";
    private static final String XSD_FILE_EXTENSION = ".xsd";
    private static final String ELEMENT = "element";
    private static final String ANNOTATION = "annotation";
    private static final String COMPLEX_TYPE = "complexType";
    private static final String SEQUENCE = "sequence";
    private static final String REF = "ref";
    private static final String DOCUMENTATION = "documentation";
    private static final String DEFAULT_XML_LANGUAGE = "en";
    private static final String NAME = "name";
    protected String appContext = Props.getRequiredProperty(PropsIF.DD_URL);
    private final static String NS_PREFIX = "xs:";

    
    
    @Autowired
    public DataSetTableServiceImpl(DatasetTableDao datasetTableDao, DataElementDao dataElementDao, AttributeValueDao attributeValueDao, AttributeDao attributeDao, DatasetDao datasetDao, DatasetTableDataService datasetTableDataService) {
        this.datasetTableDao = datasetTableDao;
        this.dataElementDao = dataElementDao;
        this.attributeValueDao = attributeValueDao;
        this.attributeDao = attributeDao;
        this.datasetDao = datasetDao;
        this.datasetTableDataService = datasetTableDataService;
    }

    
    
    
    @Override
    public Document getDataSetTableXMLSchema(int id) throws XmlExportException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDao.getById(id);
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            NameTypeElementMaker elMaker = new NameTypeElementMaker(NS_PREFIX, doc);
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, NS_PREFIX + "schema");
            schemaRoot.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                    "xsi:schemaLocation", "http://www.w3.org/2001/XMLSchema http://www.w3.org/2001/XMLSchema.xsd");
            schemaRoot.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
            schemaRoot.setAttribute("xmlns", appContext + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("xmlns:isoattrs", appContext + "/" + Namespace.URL_PREFIX + "/" + ISOATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute("xmlns:ddattrs", appContext + "/" + Namespace.URL_PREFIX + "/" + DDATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute(TARGET_NAMESPACE, appContext + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            List<DataElement> dataElements = this.dataElementDao.getDataElementsOfDatasetTable(dataSetTable.getId());
            int datasetId = datasetTableDao.getParentDatasetId(dataSetTable.getId());
            DataSet dataSet = datasetDao.getById(datasetId);
            Element tableRootElement = elMaker.createElement("element", dataSetTable.getShortName());

                 
            
            try {
                DatasetTable dsTableFull = this.datasetTableDataService.getFullDatasetTableDefinition(id);
            } catch (ResourceNotFoundException ex) {
                Logger.getLogger(DataSetTableServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
             schemaRoot.appendChild(tableRootElement);
            Element dsAnnotation = elMaker.createElement(ANNOTATION);
            dsAnnotation.setAttribute("xmlns", "");
            tableRootElement.appendChild(dsAnnotation);
            Element dsDocumentation = elMaker.createElement(DOCUMENTATION);
            dsDocumentation.setAttribute("xml:lang", DEFAULT_XML_LANGUAGE);
            dsAnnotation.appendChild(dsDocumentation);
            List<AttributeValue> attrValues = attributeValueDao.getByOwner(new DataDictEntity(dataSetTable.getId(), DataDictEntity.Entity.T));
            attrValues.addAll(attributeValueDao.getByOwner(new DataDictEntity(datasetId, DataDictEntity.Entity.DS)));
            List<Attribute> attributes = attributeDao.getCombinedDataSetAndDataTableAttributes(dataSetTable.getId(), dataSet.getId());

            for (AttributeValue attrValue : attrValues) {
                for (Attribute attr : attributes) {
                    if (attr.getId() == attrValue.getAttributeId()) {
                        Element attributeElement = elMaker.createElement(attr.getNamespace().getShortName().replace("_", ""), attr.getShortName().replace(" ", ""));
                        attributeElement.appendChild(doc.createTextNode(attrValue.getValue()));
                        dsDocumentation.appendChild(attributeElement);
                    }
                }

            }

            for (DataElement dataElement : dataElements) {
                Element xmlElement = elMaker.createElement("element", dataElement.getShortName());

                schemaRoot.appendChild(xmlElement);
                Element elemAnnotation = elMaker.createElement(ANNOTATION);
                xmlElement.appendChild(elemAnnotation);
                Element elemDocumentation = elMaker.createElement(DOCUMENTATION);
                elemDocumentation.setAttribute("xmlns", "");
                elemDocumentation.setAttribute("xml:lang", DEFAULT_XML_LANGUAGE);
                elemAnnotation.appendChild(elemDocumentation);
                List<AttributeValue> attributeValues = attributeValueDao.getByOwner(new DataDictEntity(dataSetTable.getId(), DataDictEntity.Entity.T));
                for (AttributeValue attributeValue : attributeValues) {
                    Attribute attribute = attributeDao.getById(attributeValue.getAttributeId());
                    Element attributeElement = elMaker.createElement(attribute.getNamespace().getShortName().replace("_", ""), attribute.getShortName().replace(" ", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    elemDocumentation.appendChild(attributeElement);
                }
                Element complexType = elMaker.createElement(COMPLEX_TYPE);
                complexType.setAttribute("xmlns", "");
                xmlElement.appendChild(complexType);
                Element sequence = elMaker.createElement(SEQUENCE);
                complexType.appendChild(sequence);
                Element xmlNestedElement = elMaker.createElement(ELEMENT);
                xmlNestedElement.setAttribute(REF, dataElement.getShortName());
                xmlNestedElement.setAttribute("minOccurs", "1");
                xmlNestedElement.setAttribute("maxOccurs", "1");
                sequence.appendChild(xmlNestedElement);
            }

            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        }
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
