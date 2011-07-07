package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.util.Vector;

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
    public void write(String elemID) throws Exception {

        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");

        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID);
        if (elem != null) {

            // get and set the element's complex attributes
            elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E", null, elem.getTableID(), elem.getDatasetID()));

            if (elem.getType().equalsIgnoreCase("CH1")) {

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
        // if (parentNsID!=null) setTargetNsUrl(parentNsID);

        String cNamespaceID = getContainerNamespaceID();
        if (Util.voidStr(cNamespaceID)) {
            Namespace parentNs = elem.getNamespace();
            if (parentNs == null || Util.voidStr(parentNs.getID()))
                this.targetNsUrl = this.appContext + "elements/" + elem.getIdentifier();
            else
                setTargetNsUrl(parentNs.getID());
        } else
            setTargetNsUrl(cNamespaceID);

        // writeElemStart(elem.getShortName());
        writeElemStart(elem.getIdentifier());
        writeAnnotation(elem.getAttributes(), elem.getComplexAttributes());
        writeContent(elem);
        writeElemEnd();
    }

    private void writeContent(DataElement elem) throws Exception {
        writeSimpleContent(elem);
    }

    private void writeSimpleContent(DataElement elem) throws Exception {

        String dataType = (String) nonAnnotationAttributes.get("Datatype");
        String minSize = (String) nonAnnotationAttributes.get("MinSize");
        String maxSize = (String) nonAnnotationAttributes.get("MaxSize");
        String minInclusiveValue = (String) nonAnnotationAttributes.get("MinInclusiveValue");
        String maxInclusiveValue = (String) nonAnnotationAttributes.get("MaxInclusiveValue");
        String minExclusiveValue = (String) nonAnnotationAttributes.get("MinExclusiveValue");
        String maxExclusiveValue = (String) nonAnnotationAttributes.get("MaxExclusiveValue");
        String decPrec = (String) nonAnnotationAttributes.get("DecimalPrecision");

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

            addString("\t\t");
            addString("<xs:restriction base=\"xs:");
            addString(dataType);
            addString("\">");
            newLine();

            if (!Util.voidStr(minSize)) {
                addString("\t\t\t");
                addString("<xs:minLength value=\"");
                addString(minSize);
                addString("\"/>");
                newLine();
            }

            if (!Util.voidStr(maxSize)) {
                addString("\t\t\t");
                if (dataType.equalsIgnoreCase("string"))
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

            if (!Util.voidStr(decPrec)) {
                addString("\t\t\t");
                addString("<xs:fractionDigits value=\"");
                addString(decPrec);
                addString("\"/>");
                newLine();
            }

            if (!dataType.equalsIgnoreCase("boolean")) {
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

}
