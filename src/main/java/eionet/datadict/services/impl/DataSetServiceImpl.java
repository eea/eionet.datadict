package eionet.datadict.services.impl;

import eionet.datadict.commons.DataDictXMLConstants;
import eionet.datadict.commons.util.XMLUtils;
import eionet.datadict.errors.IllegalParameterException;
import eionet.datadict.model.DataElement;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.DataSetService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.AttributeValueDataService;
import eionet.datadict.services.data.DataElementDataService;
import eionet.datadict.services.data.DataSetDataService;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.meta.dao.domain.VocabularyConcept;
import java.util.Collections;
import java.util.Comparator;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class DataSetServiceImpl implements DataSetService {

    private final DataSetDataService dataSetDataService;
    private final DatasetTableDataService datasetTableDataService;
    private final AttributeDataService attributeDataService;
    private final AttributeValueDataService attributeValueDataService;
    private final DataElementDataService dataElementDataService;

    @Autowired
    public DataSetServiceImpl(DataSetDataService dataSetDataService, DatasetTableDataService datasetTableDataService, AttributeDataService attributeDataService, AttributeValueDataService attributeValueDataService, DataElementDataService dataElementDataService) {
        this.dataSetDataService = dataSetDataService;
        this.datasetTableDataService = datasetTableDataService;
        this.attributeDataService = attributeDataService;
        this.attributeValueDataService = attributeValueDataService;
        this.dataElementDataService = dataElementDataService;
    }

    @Override
    public Document getDataSetXMLSchema(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        DataSet dataset = this.dataSetDataService.getDatasetWithoutRelations(id);
        if (dataset == null) {
            throw new ResourceNotFoundException(String.format("Dataset with id %d not found", id));
        }
        List<DatasetTable> dsTables = this.datasetTableDataService.getAllTablesByDatasetId(dataset.getId());
        Collections.sort(dsTables, new Comparator<DatasetTable>() {
            public int compare(DatasetTable o1, DatasetTable o2) {
                if (o1.getPosition() == o2.getPosition()) {
                    return 0;
                }
                return o1.getPosition() < o2.getPosition() ? -1 : 1;
            }
        });
        List<AttributeValue> attributeValues = this.attributeValueDataService.getAllByDataSetId(dataset.getId());

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SCHEMA);
            schemaRoot.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, XMLConstants.W3C_XML_SCHEMA_NS_URI + " " + XMLConstants.W3C_XML_SCHEMA_NS_URI + ".xsd");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DATASETS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.DATASETS_NAMESPACE_ID);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.ISO_ATTRS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.ISOATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_ATTRS, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DataDictXMLConstants.DDATTRS_NAMESPACE_ID);
            schemaRoot.setAttribute(DataDictXMLConstants.TARGET_NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_PREFIX + dataset.getCorrespondingNS().getId(), DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());

            this.setXSImportStatementsToDataSetXmlSchema(doc, dsTables, schemaRoot, id);

            Element rootDataSetelement = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ELEMENT);
            rootDataSetelement.setAttribute(DataDictXMLConstants.NAME, dataset.getIdentifier());
            schemaRoot.appendChild(rootDataSetelement);

            this.setXSAnnotationAndDocumentationElementsToDataSetXmlSchema(doc, dataset, rootDataSetelement, attributeValues);
            this.setXSComplexTypeWithSequenceElements(doc, rootDataSetelement, dsTables);

            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        } catch (EmptyParameterException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);

        }
    }

    private void setXSImportStatementsToDataSetXmlSchema(Document doc, List<DatasetTable> dsTables, Element schemaRoot, int datasetId) {
        for (DatasetTable dsTable : dsTables) {
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_PREFIX + dsTable.getCorrespondingNS().getId(), DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
            Element importElement = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.IMPORT);
            importElement.setAttribute(DataDictXMLConstants.NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId());
            importElement.setAttribute(DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.APP_CONTEXT + "/" + DataDictXMLConstants.SCHEMAS_API_V2_PREFIX + "/" + DataDictXMLConstants.DATASET + "/" + datasetId + "/" + DataDictXMLConstants.TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dsTable.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);
            schemaRoot.appendChild(importElement);
        }
    }

    private void setXSAnnotationAndDocumentationElementsToDataSetXmlSchema(Document doc, DataSet dataset, Element rootDataSetelement, List<AttributeValue> attributeValues) throws ResourceNotFoundException, EmptyParameterException {
        Element annotation = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ANNOTATION);
        rootDataSetelement.appendChild(annotation);
        Element documentation = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.DOCUMENTATION);
        documentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
        annotation.appendChild(documentation);
        for (AttributeValue attributeValue : attributeValues) {
            Attribute attribute = this.attributeDataService.getAttribute(attributeValue.getAttributeId());

            if (attribute != null && attribute.getDisplayType().equals(Attribute.DisplayType.VOCABULARY)) {

                List<VocabularyConcept> concepts = this.attributeDataService.getVocabularyConceptsAsAttributeValues(attribute.getId(), new DataDictEntity(dataset.getId(), DataDictEntity.Entity.DS), Attribute.ValueInheritanceMode.NONE);
                for (VocabularyConcept concept : concepts) {
                    attributeValue.setValue(concept.getLabel());
                    Element attributeElement = doc.createElement(attribute.getNamespace().getShortName().concat(":").replace("_", "").concat(attribute.getShortName()).replace(" ", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    documentation.appendChild(attributeElement);
                }
            } else if (attribute != null) {
                Element attributeElement = doc.createElement(attribute.getNamespace().getShortName().concat(":").replace("_", "").concat(attribute.getShortName()).replace(" ", ""));
                attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                documentation.appendChild(attributeElement);

            }
        }
    }

    private void setXSComplexTypeWithSequenceElements(Document doc, Element rootDataSetelement, List<DatasetTable> dsTables) {
        Element complexType = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.COMPLEX_TYPE);
        rootDataSetelement.appendChild(complexType);
        Element sequence = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SEQUENCE);
        complexType.appendChild(sequence);
        for (DatasetTable dsTable : dsTables) {
            Element tableElement = doc.createElement(DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.ELEMENT);
            tableElement.setAttribute(DataDictXMLConstants.REF, DataDictXMLConstants.DD_PREFIX.concat(dsTable.getCorrespondingNS().getId().toString()).concat(":").concat(XMLUtils.replaceAllIlegalXMLCharacters(dsTable.getIdentifier())));
            tableElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, "1");
            tableElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "1");
            sequence.appendChild(tableElement);
        }
    }

    protected String getNamespacePrefix(Namespace ns) {
        return ns == null ? "dd" : "dd" + ns.getId();
    }

    @Override
    public Document getDataSetXMLInstance(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        DataSet dataset = this.dataSetDataService.getDatasetWithoutRelations(id);
        if (dataset == null) {
            throw new ResourceNotFoundException(String.format("Dataset with id %d not found", id));
        }
        List<DatasetTable> dsTables = this.datasetTableDataService.getAllTablesByDatasetId(dataset.getId());
        
         Collections.sort(dsTables, new Comparator<DatasetTable>() {
            public int compare(DatasetTable o1, DatasetTable o2) {
                if (o1.getPosition() == o2.getPosition()) {
                    return 0;
                }
                return o1.getPosition() < o2.getPosition() ? -1 : 1;
            }
        });
        try {
            docFactory.setNamespaceAware(true);
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElement(dataset.getIdentifier().replace(" ", ""));
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XSI_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            schemaRoot.setAttribute(DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataset.getCorrespondingNS().getId() + "  "
                    + DataDictXMLConstants.APP_CONTEXT + "/" + DataDictXMLConstants.SCHEMAS_API_V2_PREFIX + "/" + DataDictXMLConstants.DATASET + "/" + dataset.getId() + "/" + DataDictXMLConstants.DATASET_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dataset.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);

            this.setXSTableElementAndRowElementsForDataSetXMLInstance(doc, schemaRoot, dsTables);

            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException("Error while parsing XML File", ex);
        }
    }

    private void setXSTableElementAndRowElementsForDataSetXMLInstance(Document doc, Element schemaRoot, List<DatasetTable> dsTables) throws XmlExportException {
        for (DatasetTable dsTable : dsTables) {
            String tableNS = DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dsTable.getCorrespondingNS().getId();
            Element tableElement = doc.createElementNS(tableNS, dsTable.getIdentifier().replace(":", "-"));
            Element row = doc.createElementNS(tableNS, DataDictXMLConstants.ROW);
            row.removeAttribute(XMLConstants.XMLNS_ATTRIBUTE);
            tableElement.appendChild(row);
            schemaRoot.appendChild(tableElement);
            List<DataElement> dataElements = this.dataElementDataService.getLatestDataElementsOfDataSetTable(dsTable.getId());
            for (DataElement dataElement : dataElements) {
                if (dataElement != null && dataElement.getIdentifier() != null) {
                    try {
                        Element xmlDataElement = doc.createElementNS(tableNS, XMLUtils.replaceAllIlegalXMLCharacters(dataElement.getIdentifier()));
                        xmlDataElement.appendChild(doc.createTextNode(""));
                        row.appendChild(xmlDataElement);
                    } catch (Exception e) {
                        throw new XmlExportException(e);
                    }
                }
            }
        }
    }

    @Override
    public Document getDataSetXMLInstanceWithNS(int id) throws XmlExportException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateDatasetDisplayDownloadLinks(int datasetId, String dispDownloadLinkType, String dispDownloadLinkValue) throws IllegalParameterException {
        if (DataSet.DISPLAY_DOWNLOAD_LINKS.valueOf(dispDownloadLinkType) == null) {
            throw new IllegalParameterException(dispDownloadLinkType, dispDownloadLinkValue);
        }
        DataSet.DISPLAY_DOWNLOAD_LINKS displDownloadLink = DataSet.DISPLAY_DOWNLOAD_LINKS.valueOf(dispDownloadLinkType);
        displDownloadLink.setValue(dispDownloadLinkValue);
        this.dataSetDataService.updateDatasetDispDownloadLinks(datasetId, displDownloadLink);
    }

}
