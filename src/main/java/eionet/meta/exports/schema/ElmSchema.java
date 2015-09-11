package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.FixedValue;
import eionet.meta.Namespace;
import eionet.util.Util;

public class ElmSchema extends Schema {

    public ElmSchema(DDSearchEngine searchEngine, PrintWriter writer) {
        super(searchEngine, writer);
    }

    /**
     * Write a schema for an object given by ID.
     */
    @Override
    public void write(String elemID) throws Exception {

        if (Util.isEmpty(elemID))
            throw new Exception("Data element ID not specified!");

        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID);
        if (elem != null) {

            // get and set the element's complex attributes
            elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E", null, elem.getTableID(), elem.getDatasetID()));

            if (elem.getType().equalsIgnoreCase("CH1") || elem.getType().equalsIgnoreCase("CH3")) {

                Vector fixedValues = searchEngine.getFixedValues(elem.getID(), "elem");
                elem.setFixedValues(fixedValues);
            }

            write(elem);
        }
    }

    /**
     * Write a schema for a given object.
     */
    private void write(DataElement elem) throws Exception {

        // set target namespace (being the parent table's namespace)
        // String parentNsID = elem.getNamespace().getID();
        // if (parentNsID != null) setTargetNsUrl(parentNsID);

        String cNamespaceID = getContainerNamespaceID();
        if (Util.isEmpty(cNamespaceID)) {
            // if it is a common element from an external schema use this schema uri as namespace:
            if (elem.isExternalSchema()) {
                this.targetNsUrl = namespaceDao.getNamespace(elem.getNameSpacePrefix()).getUri();
            } else {

                Namespace parentNs = elem.getNamespace();
                if (parentNs == null || Util.isEmpty(parentNs.getID())) {
                    this.targetNsUrl = this.appContext + "elements/" + elem.getIdentifier();
                } else {
                    setTargetNsUrl(parentNs.getID());
                }
            }
        } else {
            setTargetNsUrl(cNamespaceID);
            //external elements namespace to be added in container:
            if (elem.isExternalSchema()) {
                String nsUri = namespaceDao.getNamespace(elem.getNameSpacePrefix()).getUri();
                addNamespace(elem.getNameSpacePrefix(), nsUri);
            }
        }
        
        writeElemStart(elem);
        writeAnnotation(elem.getAttributes(), elem.getComplexAttributes());
        writeContent(elem);
        writeElemEnd();
    }

    private void writeElemStart(DataElement dataElement) {
        String elementName = this.getElementName(dataElement);
        
        if (!this.includeElementTypeAsXmlAttribute(dataElement)) {
            super.writeElemStart(elementName);
            return;
        }
        
        String dataType = dataElement.getAttributeValueByShortName("Datatype");
        String coercedDataType = this.coerceDataType(dataType);
        
        addString(String.format("<xs:element name=\"%s\" type=\"xs:%s\">", elementName, coercedDataType));        
        newLine();
    }
    
    private String getElementName(DataElement dataElement) {
        // writeElemStart(elem.getShortName());
        //in element schema use external names without NS prefix because it is targetNamespace:
        String elementName = dataElement.getIdentifier();
        
        if (!isIncontainer() && dataElement.isExternalSchema()) {
            elementName = StringUtils.substringAfter(elementName, ":");
        }
        
        return elementName;
    }
    
    private void writeContent(DataElement elem) throws Exception {
        if (!this.includeElementTypeAsXmlAttribute(elem)) {
            writeSimpleContent(elem);
        }
    }

    /**
     * Write the schema declaration for a simple type with restrictions.
     *
     * @param elem - Data element
     */
    private void writeSimpleContent(DataElement elem) throws Exception {

        String dataType = nonAnnotationAttributes.get("Datatype");
        String minSize = nonAnnotationAttributes.get("MinSize");
        String maxSize = nonAnnotationAttributes.get("MaxSize");
        String minInclusiveValue = nonAnnotationAttributes.get("MinInclusiveValue");
        String maxInclusiveValue = nonAnnotationAttributes.get("MaxInclusiveValue");
        String minExclusiveValue = nonAnnotationAttributes.get("MinExclusiveValue");
        String maxExclusiveValue = nonAnnotationAttributes.get("MaxExclusiveValue");
        String decPrec = nonAnnotationAttributes.get("DecimalPrecision");

        // overwrite above-prepared attribute values if they are not allowed for this particular datatype
        if (Util.skipAttributeByDatatype("MinSize", dataType)) {
            minSize = null;
        }
        if (Util.skipAttributeByDatatype("MaxSize", dataType)) {
            maxSize = null;
        }
        if (Util.skipAttributeByDatatype("MinInclusiveValue", dataType)) {
            minInclusiveValue = null;
        }
        if (Util.skipAttributeByDatatype("MaxInclusiveValue", dataType)) {
            maxInclusiveValue = null;
        }
        if (Util.skipAttributeByDatatype("MinExclusiveValue", dataType)) {
            minExclusiveValue = null;
        }
        if (Util.skipAttributeByDatatype("MaxExclusiveValue", dataType)) {
            maxExclusiveValue = null;
        }
        if (Util.skipAttributeByDatatype("DecimalPrecision", dataType)) {
            decPrec = null;
        }

        addString("\t");
        addString("<xs:simpleType>");
        newLine();

        if (dataType != null) {
            String coercedDataType = this.coerceDataType(dataType);

            addString("\t\t");
            addString("<xs:restriction base=\"xs:");
            addString(coercedDataType);
            addString("\">");
            newLine();

            if (!Util.isEmpty(minSize)) {
                addString("\t\t\t");
                addString("<xs:minLength value=\"");
                addString(minSize);
                addString("\"/>");
                newLine();
            }

            if (!Util.isEmpty(maxSize)) {
                addString("\t\t\t");
                if (coercedDataType.equalsIgnoreCase("string"))
                    addString("<xs:maxLength value=\"");
                else
                    addString("<xs:totalDigits value=\"");
                addString(maxSize);
                addString("\"/>");
                newLine();
            }

            if (minInclusiveValue != null && minInclusiveValue.trim().length() > 0) {
                addString("\t\t\t");
                addString("<xs:minInclusive value=\"");
                addString(minInclusiveValue);
                addString("\"/>");
                newLine();
            }

            if (maxInclusiveValue != null && maxInclusiveValue.trim().length() > 0) {
                addString("\t\t\t");
                addString("<xs:maxInclusive value=\"");
                addString(maxInclusiveValue);
                addString("\"/>");
                newLine();
            }

            if (minExclusiveValue != null && minExclusiveValue.trim().length() > 0) {
                addString("\t\t\t");
                addString("<xs:minExclusive value=\"");
                addString(minExclusiveValue);
                addString("\"/>");
                newLine();
            }

            if (maxExclusiveValue != null && maxExclusiveValue.trim().length() > 0) {
                addString("\t\t\t");
                addString("<xs:maxExclusive value=\"");
                addString(maxExclusiveValue);
                addString("\"/>");
                newLine();
            }

            if (!Util.isEmpty(decPrec)) {
                addString("\t\t\t");
                addString("<xs:fractionDigits value=\"");
                addString(decPrec);
                addString("\"/>");
                newLine();
            }

            if (!coercedDataType.equalsIgnoreCase("boolean")) {
                Vector fixedValues = elem.getFixedValues();
                for (int k = 0; fixedValues != null && k < fixedValues.size(); k++) {

                    FixedValue fxv = (FixedValue) fixedValues.get(k);

                    addString("\t\t\t");
                    addString("<xs:enumeration value=\"");
                    addString(escape(fxv.getValue()));
                    addString("\"/>");
                    newLine();
                }
            }

            addString("\t\t");
            addString("</xs:restriction>");
            newLine();
        }

        addString("\t");
        addString("</xs:simpleType>");
        newLine();
    }

    /**
     * Indicates if element is in container.
     * @return true if element schema is inside container
     */
    private boolean isIncontainer() {
        return StringUtils.isNotBlank(getContainerNamespaceID());
    }
    
    private String coerceDataType(String dataType) {
        final String stringType = "string";
        final String anyType = "anyType";
        
        if ("reference".equals(dataType)) {
            return stringType;
        }
        
        if ("localref".equals(dataType)) {
            return stringType;
        }
        
        if (this.isGeometryDataType(dataType)) {
            return anyType;
        }
        
        return dataType;
    }
    
    private boolean isGeometryDataType(String dataType) {
        if ("point".equals(dataType)) {
            return true;
        }
        
        if ("linestring".equals(dataType)) {
            return true;
        }
        
        if ("polygon".equals(dataType)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isGeometryDataElement(DataElement dataElement) {
        String dataType = dataElement.getAttributeValueByShortName("Datatype");
        
        return this.isGeometryDataType(dataType);
    }
    
    private boolean includeElementTypeAsXmlAttribute(DataElement dataElement) {
        return this.isGeometryDataElement(dataElement);
    }
    
}
