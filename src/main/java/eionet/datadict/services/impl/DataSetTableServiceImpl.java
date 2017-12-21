package eionet.datadict.services.impl;

import eionet.datadict.commons.DataDictXMLConstants;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.services.data.AttributeValueDataService;
import eionet.datadict.services.data.DataElementDataService;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.datadict.services.data.VocabularyDataService;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import java.util.ArrayList;
import java.util.LinkedList;
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

    private final AttributeDataService attributeDataService;
    private final DatasetTableDataService datasetTableDataService;
    private final DataElementDataService dataElementDataService;
    private final AttributeValueDataService attributeValueDataService;
    private final VocabularyDataService vocabularyDataService;

    @Autowired
    public DataSetTableServiceImpl(AttributeDataService attributeDataService, DatasetTableDataService datasetTableDataService, DataElementDataService dataElementDataService, AttributeValueDataService attributeValueDataService, VocabularyDataService vocabularyDataService) {
        this.attributeDataService = attributeDataService;
        this.datasetTableDataService = datasetTableDataService;
        this.dataElementDataService = dataElementDataService;
        this.attributeValueDataService = attributeValueDataService;
        this.vocabularyDataService = vocabularyDataService;
    }

    @Override
    public Document getDataSetTableXMLSchema(int id) throws XmlExportException, ResourceNotFoundException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDataService.getFullDatasetTableDefinition(id);
        int datasetId = dataSetTable.getDataSet().getId();

        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            NameTypeElementMaker elMaker = new NameTypeElementMaker(DataDictXMLConstants.XS_PREFIX + ":", doc);
            Element schemaRoot = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, DataDictXMLConstants.XS_PREFIX + ":" + DataDictXMLConstants.SCHEMA);
            schemaRoot.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, XMLConstants.W3C_XML_SCHEMA_NS_URI + "  " + XMLConstants.W3C_XML_SCHEMA_NS_URI + ".xsd");
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.ISO_ATTRS, DataDictXMLConstants.ISOATTRS_NAMESPACE);
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.DD_ATTRS, DataDictXMLConstants.DDATTRS_NAMESPACE);
            schemaRoot.setAttribute(DataDictXMLConstants.TARGET_NAMESPACE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute("elementFormDefault", "qualified");
            schemaRoot.setAttribute("attributeFormDefault", "unqualified");
            Element tableRootElement = elMaker.createElement(DataDictXMLConstants.ELEMENT, dataSetTable.getIdentifier());
            schemaRoot.appendChild(tableRootElement);

            List<Attribute> dataSetAttributes = this.attributeDataService.getAllByDatasetId(datasetId);
            List<AttributeValue> dataSetAttributesValues = new ArrayList<AttributeValue>();
            List<DataElement> datasetTableElementsList = dataElementDataService.getLatestDataElementsOfDataSetTable(dataSetTable.getId());

            for (Attribute dataSetAttribute : dataSetAttributes) {
                List<AttributeValue> attributeValues = this.attributeValueDataService.getAllByAttributeAndDataSetId(dataSetAttribute.getId(), datasetId);
                dataSetAttributesValues.add(attributeValues.get(0));
            }

            this.setXSAnnotationAndDocumentationElementsToTableXmlSchema(elMaker, doc, tableRootElement, dataSetTable, dataSetAttributes, dataSetAttributesValues);

            this.setXSComplexTypeAndRowSequenceElementsToTableXMLSchema(elMaker, tableRootElement, datasetTableElementsList);

            this.setXSDataElementsToTableXmlSchema(doc, elMaker, schemaRoot, datasetTableElementsList, dataSetAttributesValues);

            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException | EmptyParameterException ex) {
            Logger.getLogger(DataSetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new XmlExportException(ex);
        }
    }

    private void setXSAnnotationAndDocumentationElementsToTableXmlSchema(NameTypeElementMaker elMaker, Document doc, Element tableRootElement, DatasetTable dataSetTable, List<Attribute> dataSetAttributes, List<AttributeValue> dataSetAttributesValues) {
        Element dsAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
        tableRootElement.appendChild(dsAnnotation);
        Element dsDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
        dsDocumentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
        dsAnnotation.appendChild(dsDocumentation);

        for (Attribute dataSetTableAttribute : dataSetTable.getAttributes()) {
            AttributeValue attributeValue = this.attributeValueDataService.getAllByAttributeAndDataSetTableId(dataSetTableAttribute.getId(), dataSetTable.getId()).get(0);
            if (dataSetTableAttribute.getShortName() != null && !dataSetTableAttribute.getShortName().equals("Keyword") && !dataSetTableAttribute.getShortName().equals("obligation") && dataSetTableAttribute.getNamespace() != null && dataSetTableAttribute.getNamespace().getShortName() != null) {
                Element attributeElement = elMaker.createElement(dataSetTableAttribute.getShortName().replace(" ", ""), null, dataSetTableAttribute.getNamespace().getShortName().replace("_", ""));
                if (attributeValue != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }
        }
        for (Attribute dataSetAttribute : dataSetAttributes) {
            List<AttributeValue> attributeValues = this.attributeValueDataService.getAllByAttributeAndDataSetId(dataSetAttribute.getId(), dataSetTable.getDataSet().getId());
            dataSetAttributesValues.add(attributeValues.get(0));
            if (dataSetAttribute.getShortName() != null && !dataSetAttribute.getShortName().equals("Keyword") && !dataSetAttribute.getShortName().equals("obligation") && dataSetAttribute.getNamespace() != null && dataSetAttribute.getNamespace().getShortName() != null) {
                Element attributeElement = elMaker.createElement(dataSetAttribute.getShortName().replace(" ", ""), null, dataSetAttribute.getNamespace().getShortName().replace("_", ""));
                if (attributeValues.get(0) != null) {
                    attributeElement.appendChild(doc.createTextNode(attributeValues.get(0).getValue()));
                }
                dsDocumentation.appendChild(attributeElement);
            }
        }
    }

    private void setXSComplexTypeAndRowSequenceElementsToTableXMLSchema(NameTypeElementMaker elMaker, Element tableRootElement, List<DataElement> datasetTableElementsList) {

        Element complexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);
        tableRootElement.appendChild(complexType);
        Element sequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
        complexType.appendChild(sequence);
        Element rowElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
        rowElement.setAttribute(DataDictXMLConstants.NAME, DataDictXMLConstants.ROW);
        rowElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, "0");
        rowElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "unbounded");
        sequence.appendChild(rowElement);
        Element rowComplexType = elMaker.createElement(DataDictXMLConstants.COMPLEX_TYPE);
        rowElement.appendChild(rowComplexType);
        Element rowSequence = elMaker.createElement(DataDictXMLConstants.SEQUENCE);
        rowComplexType.appendChild(rowSequence);

        for (DataElement dataElement : datasetTableElementsList) {
            Element tableElement = elMaker.createElement(DataDictXMLConstants.ELEMENT);
            tableElement.setAttribute(DataDictXMLConstants.REF, dataElement.getIdentifier());
            tableElement.setAttribute(DataDictXMLConstants.MIN_OCCURS, this.dataElementDataService.isDataElementMandatory(dataElement.getDatasetTable().getId(), dataElement.getId()) ? "1" : "0");
            String delimiter = this.dataElementDataService.getDataElementMultiValueDelimiter(dataElement.getDatasetTable().getId(), dataElement.getId());
            if (delimiter != null) {
                tableElement.setAttribute(DataDictXMLConstants.MULTI_VALUE_DELIM, delimiter);
            }

            tableElement.setAttribute(DataDictXMLConstants.MAX_OCCURS, "1");
            rowSequence.appendChild(tableElement);
        }
    }

    private void setXSDataElementsToTableXmlSchema(Document doc, NameTypeElementMaker elMaker, Element schemaRoot, List<DataElement> datasetTableElementsList, List<AttributeValue> dataSetAttributesValues) throws ResourceNotFoundException, EmptyParameterException {
        for (DataElement dataElement : datasetTableElementsList) {
          //  Element xmlElement = elMaker.createElement(DataDictXMLConstants.ELEMENT, dataElement.getIdentifier());
            String MinSize = "";
            String MaxSize = "";
            String Datatype = "";
            String MinInclusiveValue = "";
            String MaxInclusiveValue = "";
            List<VocabularyConcept> vocabularyConcepts = new LinkedList<VocabularyConcept>();
            if (dataElement.getVocabularyId() != null) {
                vocabularyConcepts = this.vocabularyDataService.getVocabularyConcepts(dataElement.getVocabularyId(), StandardGenericStatus.VALID);
            }
           // schemaRoot.appendChild(xmlElement);
            Element elemAnnotation = elMaker.createElement(DataDictXMLConstants.ANNOTATION);
           // xmlElement.appendChild(elemAnnotation);
            Element elemDocumentation = elMaker.createElement(DataDictXMLConstants.DOCUMENTATION);
            elemDocumentation.setAttribute(XMLConstants.XML_NS_PREFIX + ":" + DataDictXMLConstants.LANGUAGE_PREFIX, DataDictXMLConstants.DEFAULT_XML_LANGUAGE);
            elemAnnotation.appendChild(elemDocumentation);
            List<AttributeValue> attributeValues = this.attributeValueDataService.getAllByDataElementId(dataElement.getId());
            attributeValues.addAll(dataSetAttributesValues);
            for (AttributeValue attributeValue : attributeValues) {
                Attribute attribute = this.attributeDataService.getAttribute(attributeValue.getAttributeId());
                if (attribute.getShortName().equals("MinSize")) {
                    MinSize = attributeValue.getValue();
                    continue;
                }
                if (attribute.getShortName().equals("MaxSize")) {
                    MaxSize = attributeValue.getValue();
                    continue;
                }
                if (attribute.getShortName().equals("Datatype")) {
                    Datatype = attributeValue.getValue();
                    continue;
                }
                if (attribute.getShortName().equals("MinInclusiveValue")) {
                    MinInclusiveValue = attributeValue.getValue();
                    continue;
                }
                if (attribute.getShortName().equals("MaxInclusiveValue")) {
                    MaxInclusiveValue = attributeValue.getValue();
                    continue;
                }
                if (attribute != null && attribute.getShortName() != null && !attribute.getShortName().equals("Keyword") && !attribute.getShortName().equals("obligation") && attribute.getNamespace() != null && attribute.getNamespace().getShortName() != null) {
                    Element attributeElement = elMaker.createElement(attribute.getShortName().replace(" ", ""), null, attribute.getNamespace().getShortName().replace("_", ""));
                    attributeElement.appendChild(doc.createTextNode(attributeValue.getValue()));
                    elemDocumentation.appendChild(attributeElement);
                }
            }
             Element xmlElement = elMaker.createElement(DataDictXMLConstants.ELEMENT, dataElement.getIdentifier(),Datatype,null);
             if(Datatype!=""){
             xmlElement.setAttribute(XMLConstants.XML_NS_PREFIX + ":"+DataDictXMLConstants.TYPE, Datatype);
             }
            schemaRoot.appendChild(xmlElement);
            xmlElement.appendChild(elemAnnotation);

            Element dataElementSimpleType = elMaker.createElement(DataDictXMLConstants.SIMPLE_TYPE);
            Element dataElementRestriction = elMaker.createElement(DataDictXMLConstants.RESTRICTION);
            if (Datatype.equals("decimal")) {
                dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + ":" + Datatype);
                if (!MaxSize.equals("")) {
                    Element totalDigitsElement = elMaker.createElement("totalDigits");
                    totalDigitsElement.setAttribute("value", MaxSize);
                    dataElementRestriction.appendChild(totalDigitsElement);
                }
                if (!MinInclusiveValue.equals("")) {
                    Element minInclusiveElement = elMaker.createElement("minInclusive");
                    minInclusiveElement.setAttribute("value", MinInclusiveValue);
                    dataElementRestriction.appendChild(minInclusiveElement);
                }
                if (!MaxInclusiveValue.equals("")) {
                    Element maxInclusiveElement = elMaker.createElement("maxInclusive");
                    maxInclusiveElement.setAttribute("value", MaxInclusiveValue);
                    dataElementRestriction.appendChild(maxInclusiveElement);
                }
                dataElementSimpleType.appendChild(dataElementRestriction);
            }
            if (Datatype.equals("integer") && !MaxSize.equals("")) {
                dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + ":" + Datatype);
                Element totalDigitsElement = elMaker.createElement("totalDigits");
                totalDigitsElement.setAttribute("value", MaxSize);
                dataElementRestriction.appendChild(totalDigitsElement);
                if (!MinInclusiveValue.equals("")) {
                    Element minInclusiveElement = elMaker.createElement("minInclusive");
                    minInclusiveElement.setAttribute("value", MinInclusiveValue);
                    dataElementRestriction.appendChild(minInclusiveElement);
                }
                if (!MaxInclusiveValue.equals("")) {
                    Element maxInclusiveElement = elMaker.createElement("maxInclusive");
                    maxInclusiveElement.setAttribute("value", MaxInclusiveValue);
                    dataElementRestriction.appendChild(maxInclusiveElement);
                }
                dataElementSimpleType.appendChild(dataElementRestriction);
            }
            if (Datatype.equals("string")) {
                dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + ":" + Datatype);
                if (!MaxSize.equals("")) {
                    Element minLengthElement = elMaker.createElement("minLength");
                    minLengthElement.setAttribute("value", MinSize);
                    dataElementRestriction.appendChild(minLengthElement);
                }
                if (!MaxSize.equals("")) {
                    Element maxLengthElement = elMaker.createElement("maxLength");
                    maxLengthElement.setAttribute("value", MaxSize);

                    dataElementRestriction.appendChild(maxLengthElement);
                }
                dataElementSimpleType.appendChild(dataElementRestriction);
            }
            if (Datatype.equals("reference")) {
                //If datatype of attribute is reference, it means it has a vocabulary relation
                for (VocabularyConcept vocConcept : vocabularyConcepts) {
                    Element enumerationElement = elMaker.createElement("enumeration");
                    enumerationElement.setAttribute("value", vocConcept.getNotation());
                    dataElementRestriction.setAttribute(DataDictXMLConstants.BASE, DataDictXMLConstants.XS_PREFIX + ":" + "string");
                    dataElementRestriction.appendChild(enumerationElement);
                }
            dataElementSimpleType.appendChild(dataElementRestriction);
            }
          //Determine if simpleType is Empty before appending it
           if(dataElementSimpleType.hasAttributes() || dataElementSimpleType.hasChildNodes()){
          xmlElement.appendChild(dataElementSimpleType);
           }

        }

    }

    @Override
    public Document getDataSetTableXMLInstance(int id) throws XmlExportException, ResourceNotFoundException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        DatasetTable dataSetTable = this.datasetTableDataService.getFullDatasetTableDefinition(id);
        try {
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element schemaRoot = doc.createElement(dataSetTable.getIdentifier());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId());
            schemaRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + DataDictXMLConstants.XSI_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            schemaRoot.setAttribute(DataDictXMLConstants.XSI_PREFIX + ":" + DataDictXMLConstants.SCHEMA_LOCATION, DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId() + "  " + DataDictXMLConstants.APP_CONTEXT + "/" + DataDictXMLConstants.SCHEMAS_API_V2_PREFIX + "/" + DataDictXMLConstants.DATASET + "/" + dataSetTable.getDataSet().getId() + "/"
                    + DataDictXMLConstants.TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME + dataSetTable.getId() + DataDictXMLConstants.XSD_FILE_EXTENSION);
            List<DataElement> dataElements = this.dataElementDataService.getLatestDataElementsOfDataSetTable(dataSetTable.getId());
            String tableNS = DataDictXMLConstants.APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + dataSetTable.getCorrespondingNS().getId();
            Element row = doc.createElementNS(tableNS, DataDictXMLConstants.ROW);
            schemaRoot.appendChild(row);
            for (DataElement dataElement : dataElements) {
                if (dataElement.getIdentifier() != null) {
                    Element xmlDataElement = doc.createElementNS(tableNS, dataElement.getIdentifier());
                    xmlDataElement.appendChild(doc.createTextNode(""));
                    row.appendChild(xmlDataElement);
                }

            }
            doc.appendChild(schemaRoot);
            return doc;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataSetTableServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
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

        public Element createElement(String elementName, String nameAttrVal, String typeAttrVal, String nameSpacePrefix) {
            Element element;
            if (nameSpacePrefix != null && nameSpacePrefix.equals(DataDictXMLConstants.ISO_ATTRS)) {
                element = doc.createElementNS(DataDictXMLConstants.ISOATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else if (nameSpacePrefix != null && nameSpacePrefix.equals(DataDictXMLConstants.DD_ATTRS)) {
                element = doc.createElementNS(DataDictXMLConstants.DDATTRS_NAMESPACE, nameSpacePrefix + ":" + elementName);
            } else {
                element = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, nsPrefix + elementName);

            }
            if (nameAttrVal != null) {
                element.setAttribute(DataDictXMLConstants.NAME, nameAttrVal);
            }
            if (typeAttrVal != null) {
                element.setAttribute(DataDictXMLConstants.TYPE, typeAttrVal);
            }
            return element;
        }

        public Element createElement(String elementName, String nameAttrVal) {
            return createElement(elementName, nameAttrVal, null, null);
        }

        public Element createElement(String elementName, String nameAttrVal, String nameSpacePrefix) {
            return createElement(elementName, nameAttrVal, null, nameSpacePrefix);
        }

        public Element createElement(String elementName) {
            return createElement(elementName, null, null, null);
        }
    }

}
