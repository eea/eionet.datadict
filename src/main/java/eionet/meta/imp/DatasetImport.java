
package eionet.meta.imp;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.DElemAttribute;
import eionet.meta.savers.DataElementHandler;
import eionet.meta.savers.DatasetHandler;
import eionet.meta.savers.DsTableHandler;
import eionet.meta.savers.FixedValuesHandler;
import eionet.meta.savers.Parameters;

/**
 * This class is the core worker when importing definitions from XML into DD. The XML is
 * the one generated from the MS-Access import tool.
 *
 * @author Enriko Käsper
 * @author Jaanus Heinlaid
 */
public class DatasetImport {

    /** */
    public static final String SEPARATOR = "_";
    public static final String IMPORT_TYPE_FXV_ONLY = "fixedValues";
    public static final String IMPORT_TYPE_WHOLE_DATASETS = "datasets";
    private static final String RESPONSE_NEW_LINE = "<br/>";

    /** */
    private DatasetImportHandler handler;
    private DDSearchEngine searchEngine;
    private Connection conn = null;
    private ServletContext ctx = null;

    /** */
    private StringBuffer responseText = new StringBuffer();
    private String lastInsertID = null;
    private Hashtable tables;
    private Vector dbSimpleAttrs;

    /** */
    private Hashtable allParams = null;
    private Hashtable tblMap = null;
    private Hashtable unknownTbl = null;

    /** */
    private Hashtable dstID = null;
    private Hashtable tblID = null;
    private Hashtable elmID = null;
    private Hashtable fxvID = null;
    private Hashtable fxvElmID = null;

    /** */
    private int countDatasetsImported = 0;
    private int countElementsImported = 0;
    private int countTablesImported = 0;
    private int countFixedValuesImported = 0;

    /** */
    private int countDatasetsFound = 0;
    private int countElementsFound = 0;
    private int countTablesFound = 0;
    private int countFixedValuesFound = 0;

    /** */
    private String importType = null;
    private String importParentID = null; // this can be delem_id, table_id or dataset_id, depends on the import_type

    /** */
    private DDUser user = null;
    private String date = null;

    /** */
    private int errorCount = 0;
    private int warningCount = 0;

    /**
     *
     * @param handler
     * @param conn
     * @param ctx
     * @param type
     */
    public DatasetImport(DatasetImportHandler handler, Connection conn, ServletContext ctx) {
        this.handler = handler;
        this.conn = conn;
        this.searchEngine = new DDSearchEngine(conn);
        this.ctx = ctx;
        tblMap = new Hashtable();
        allParams = new Hashtable();
        setMapping();
    }

    /**
     * @throws SQLException
     */
    public void execute() throws SQLException {

        // get names of DD tables for which the XML handler found data in the XML file
        tables = handler.getTables();

        // get import type
        String importTypeInXML = handler.getImportType();
        if (importTypeInXML == null) {
            importTypeInXML = IMPORT_TYPE_WHOLE_DATASETS;
        }
        if (importType == null) {
            importType = IMPORT_TYPE_WHOLE_DATASETS;
        }

        // check if the XML file has the expected import type declared in it
        if (!importType.equalsIgnoreCase(importTypeInXML)) {
            handleError(null, RESPONSE_NEW_LINE + "Import failed!"
                + RESPONSE_NEW_LINE + "Imported xml file does not have the same type."
                + RESPONSE_NEW_LINE + "Import type:" + importType + "; Xml file import type:" + importTypeInXML);
            return;
        }

        // get names and IDs of attributes from DD database
        setDBAttrs();

        // if we are importing only fixed values for one particular element
        if (importType.equalsIgnoreCase(IMPORT_TYPE_FXV_ONLY)) {
            if (importParentID == null) {
                handleError(null, RESPONSE_NEW_LINE + "Import failed!" + RESPONSE_NEW_LINE + "Data element id is not specified");
                return;
            }
            setParams("FIXED_VALUE", true, "FXV", "new_value", null, null);
            elmID = new Hashtable();
            saveFixedValues();
            responseText.append(RESPONSE_NEW_LINE).append("Fixed values found:" + countFixedValuesFound + "; successfully imported:" + countFixedValuesImported);
        } else {
            // if we are importing whole new dataset(s)
            setParams("DATASET", true, "DST", "ds_name", "DS", "ds_id");
            setParams("DS_TABLE", true, "TBL", "short_name", null, null);
            setParams("DATAELEM", true, null, "delem_name", "E", "delem_id");
            setParams("TBL2ELEM", false, null, null, null, null);
            setParams("FIXED_VALUE", true, "FXV", "new_value", null, null);

            saveDataset();
            saveTables();
            saveDElem();
            saveFixedValues();

            responseText.append(RESPONSE_NEW_LINE);
            responseText.append(RESPONSE_NEW_LINE).append("Datasets found:" + countDatasetsFound + "; successfully imported:" + countDatasetsImported);
            responseText.append(RESPONSE_NEW_LINE).append("Dataset tables found:" + countTablesFound + "; successfully imported:" + countTablesImported);
            responseText.append(RESPONSE_NEW_LINE).append("Data elements found:" + countElementsFound + "; successfully imported:" + countElementsImported);
            responseText.append(RESPONSE_NEW_LINE).append("Fixed values found:" + countFixedValuesFound + "; successfully imported:" + countFixedValuesImported);
            responseText.append(RESPONSE_NEW_LINE);

            if (unknownTbl.size() > 0) {
                handleWarning(null, RESPONSE_NEW_LINE + "Unknown fields found from the following tables:");
                Enumeration keys = unknownTbl.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    responseText.append(RESPONSE_NEW_LINE + key + ": " + unknownTbl.get(key).toString());
                }
            }
        }
    }

    /**
     *
     */
    private void saveDataset() {

        DatasetHandler dsHandler;
        Parameters par;
        dstID = new Hashtable();

        Vector ds_params = (Vector) allParams.get("DATASET");
        if (ds_params == null) {
            return;
        }
        if (ds_params.size() == 0) {
            return;
        }

        for (int i = 0; i < ds_params.size(); i++) {
            par = (Parameters) ds_params.get(i);
            try {
                dsHandler = new DatasetHandler(conn, par, ctx);
                dsHandler.setUser(user);
                dsHandler.setDate(date);
                dsHandler.setImportMode(true);
                dsHandler.execute();
                countDatasetsImported++;
                dstID.put(par.getParameter("ds_id"), dsHandler.getLastInsertID());
            } catch (Exception e) {
                handleError(e, "Import had errors! Failed storing dataset: " + par.getParameter("ds_name") + RESPONSE_NEW_LINE
                    + e.toString() + RESPONSE_NEW_LINE);
            }
        }
    }

    /**
     *
     *
     */
    private void saveTables() {

        DsTableHandler tblHandler;
        Parameters par;
        String ds_id;
        tblID = new Hashtable();

        Vector tbl_params = (Vector) allParams.get("DS_TABLE");

        if (tbl_params == null) {
            return;
        }
        if (tbl_params.size() == 0) {
            return;
        }

        for (int i = 0; i < tbl_params.size(); i++) {
            par = (Parameters) tbl_params.get(i);
            ds_id = par.getParameter("ds_id");
            if (dstID.containsKey(ds_id)) {
                par.removeParameter("ds_id");
                par.addParameterValue("ds_id", (String) dstID.get(ds_id));
            } else {
                responseText.append("Dataset id was not found for table: "
                    + par.getParameter("short_name") + RESPONSE_NEW_LINE);
                continue;
            }
            try {
                tblHandler = new DsTableHandler(conn, par, ctx);
                tblHandler.setUser(user);
                tblHandler.setDate(date);
                tblHandler.setVersioning(false);
                tblHandler.setImport(true);
                tblHandler.execute();
                countTablesImported++;
                tblID.put(par.getParameter("tbl_id"), tblHandler.getLastInsertID());
            } catch (Exception e) {
                handleError(e, "Import had errors! Failed storing table: " + par.getParameter("short_name") + RESPONSE_NEW_LINE
                    + e.toString() + RESPONSE_NEW_LINE);
            }
        }
    }

    /**
     *
     *
     */
    private void saveDElem() {

        DataElementHandler delemHandler;
        Parameters par;
        elmID = new Hashtable();
        String delem_id;

        Vector delem_params = (Vector) allParams.get("DATAELEM");

        if (delem_params == null) {
            return;
        }
        if (delem_params.size() == 0) {
            return;
        }

        for (int i = 0; i < delem_params.size(); i++) {
            par = (Parameters) delem_params.get(i);
            delem_id = par.getParameter("delem_id");
            getTbl2elem(delem_id, par);
            par.removeParameter("delem_id");
            try {
                delemHandler = new DataElementHandler(conn, par, ctx);
                delemHandler.setUser(user);
                delemHandler.setDate(date);
                delemHandler.setVersioning(false);
                delemHandler.setImportMode(true);
                delemHandler.execute();
                countElementsImported++;
                elmID.put(delem_id, delemHandler.getLastInsertID());
            } catch (Exception e) {
                handleError(e, "Import had errors! Failed storing element: " + par.getParameter("delem_name") + RESPONSE_NEW_LINE
                    + e.toString() + RESPONSE_NEW_LINE);
            }
        }
    }

    /**
     *
     * @param elem_id
     * @param par
     */
    private void getTbl2elem(String elem_id, Parameters par) {
        Parameters tbl2elem_par;
        String tbl_id;
        String parelem_id;

        Vector tbl2elem_params = (Vector) allParams.get("TBL2ELEM");

        for (int i = 0; i < tbl2elem_params.size(); i++) {
            tbl2elem_par = (Parameters) tbl2elem_params.get(i);
            parelem_id = tbl2elem_par.getParameter("delem_id");
            if (parelem_id.equals(elem_id)) {
                tbl_id = tbl2elem_par.getParameter("table_id");
                par.addParameterValue("table_id", (String) tblID.get(tbl_id));
                break;
            }
        }
    }

    /**
     *
     *
     */
    private void saveFixedValues() {
        FixedValuesHandler fxvHandler = null;
        Parameters par;
        String delem_id = null;
        String parent_id = null;
        String parentType = "elem";
        String fxv_val = null;
        fxvID = new Hashtable();  // stores keys - fxv id in xml; values - dataelem id in db
        fxvElmID = new Hashtable();  // stores keys - fxv id in xml; values - dataelem id in db

        Vector fxv_params = (Vector) allParams.get("FIXED_VALUE");

        if (fxv_params == null) {
            return;
        }
        if (fxv_params.size() == 0) {
            return;
        }

        countFixedValuesFound = fxv_params.size();

        for (int i = 0; i < fxv_params.size(); i++) {
            par = (Parameters) fxv_params.get(i);

            fxv_val = par.getParameter("new_value");
            if (fxv_val == null) {
                par.addParameterValue("new_value", "");
            }

            delem_id = par.getParameter("delem_id");
            parent_id = par.getParameter("parent_id");

            if (delem_id == null) {
                delem_id = "0";
            }

            if (importType.equalsIgnoreCase(IMPORT_TYPE_FXV_ONLY)) {
                elmID.put(delem_id, importParentID);
            }

            if (elmID.containsKey(delem_id)) {
                delem_id = (String) elmID.get(delem_id);
                par.removeParameter("delem_id");
                par.addParameterValue("delem_id", delem_id);
            } else {
                responseText.append("Data element id was not found for fixed value "
                        + fxv_val + RESPONSE_NEW_LINE);
                continue;
            }

            try {
                par.addParameterValue("parent_type", parentType);

                if (fxvHandler == null || !delem_id.equals(fxvHandler.getOwnerID())) {
                    fxvHandler = new FixedValuesHandler(conn, par, ctx);
                    fxvHandler.setVersioning(false);
                }

                fxvHandler.execute(par);
                countFixedValuesImported++;

                if (fxvHandler.isAllowed()) {
                    fxvID.put(par.getParameter("id"), fxvHandler.getLastInsertID());
                    fxvElmID.put(par.getParameter("id"), delem_id);
                }
            } catch (Exception e) {
                handleError(e, "Import had errors! Could not store fixed value into database: " + fxv_val + RESPONSE_NEW_LINE
                        + e.toString() + RESPONSE_NEW_LINE);
            }
        }
    }

    /**
     *
     * @param table
     * @param hasAttrs
     * @param type
     * @param context
     * @param parent_type
     * @param id_field
     */
    private void setParams(String table, boolean hasAttrs, String type, String context, String parent_type, String id_field) {

        Parameters params;
        Vector tbl_params;
        Hashtable row;
        Vector rowMap;
        Hashtable fieldMap;
        String importValue;
        String allowNull;

        tbl_params = new Vector();

        Vector tbl = (Vector) tables.get(table);
        if (table.equals("DATASET")) {
            countDatasetsFound = tbl.size();
        } else if (table.equals("DS_TABLE")) {
            countTablesFound = tbl.size();
        } else if (table.equals("DATAELEM")) {
            countElementsFound = tbl.size();
        }

        for (int i = 0; i < tbl.size(); i++) {
            row= (Hashtable) tbl.get(i);

            params = new Parameters();
            params.addParameterValue("mode", "add");

            rowMap= (Vector) tblMap.get(table);
            for (int c = 0; c < rowMap.size(); c++) {
                fieldMap = (Hashtable) rowMap.get(c);
                importValue = (String) row.get(fieldMap.get("imp"));
                allowNull = (String) fieldMap.get("allowNull");
                if (allowNull.equals("false")) {
                    if (importValue == null || importValue.length() == 0) {
                        responseText.append((String) fieldMap.get("text") + " is empty!").append(RESPONSE_NEW_LINE);
                        break;
                    }
                }
                row.remove(fieldMap.get("imp"));
                params.addParameterValue((String) fieldMap.get("param"), importValue);
            }
            if (hasAttrs) {
                if (type == null || table.equals("DATAELEM")) {
                    type = params.getParameter("type");
                }
                getSimpleAttrs(row, params, type, params.getParameter(context));
            }

            addUnknown(table, row);
            tbl_params.add(params);
        }

        allParams.put(table, tbl_params);
    }

    /**
     *
     * @param row
     * @param params
     * @param type
     * @param context_name
     */
    private void getSimpleAttrs(Hashtable row, Parameters params, String type, String context_name) {
        String attrName = null;
        String attrValue = null;
        String impAttrName = null;
        String impAttrValue = null;
        boolean dispMult = false;
        for (int i = 0; i < dbSimpleAttrs.size(); i++) {
            DElemAttribute delemAttr = (DElemAttribute) dbSimpleAttrs.get(i);
            attrName = delemAttr.getShortName();
            if (delemAttr.displayFor(type)) {
                dispMult = delemAttr.getDisplayMultiple().equals("1") ? true : false;

                //find attributes with multiple values
                if (dispMult) {
                    for (int c = 1; c <= 9; c++) {
                        impAttrName = attrName.toLowerCase() + SEPARATOR + Integer.toString(c);
                        if (row.containsKey(impAttrName)) {
                            impAttrValue = (String) row.get(impAttrName);
                            if (impAttrValue != null && impAttrValue.length() > 0) {
                                params.addParameterValue(DataElementHandler.ATTR_MULT_PREFIX + delemAttr.getID(), impAttrValue);
                            }
                            row.remove(impAttrName);
                        }
                    }
                } else {
                    //find mandatory attributes
                    if (delemAttr.getObligation().equals("M")) {
                        if (row.containsKey(attrName.toLowerCase())) {
                            attrValue = (String) row.get(attrName.toLowerCase());
                            if (attrValue == null || attrValue.length() == 0) {
                                handleError(null, "Could not find mandatory attribute (" + attrName + ") value from specified xml for "
                                    + getContextName(type) + " - " + context_name + "!" + RESPONSE_NEW_LINE);
                            } else {
                                params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                            }
                            row.remove(attrName.toLowerCase());
                        } else {
                            handleError(null, "Could not find mandatory attribute (" + attrName + ") value from specified xml for "
                                + getContextName(type) + " - " + context_name + "!" + RESPONSE_NEW_LINE);
                        }
                    } else {
                        //find other attributes
                        if (row.containsKey(attrName.toLowerCase())) {
                            attrValue = (String) row.get(attrName.toLowerCase());
                            if (attrValue != null && attrValue.length() != 0) {
                                params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                            }
                            row.remove(attrName.toLowerCase());
                        }
                    }
                }
            } else {
                // this attribute is irrelevant for this element type, but nevertheless
                // remove it from row, because otherwise the output will say it's an unknown field
                row.remove(attrName.toLowerCase());
            }
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void setDBAttrs() throws SQLException {
        dbSimpleAttrs = searchEngine.getDElemAttributes();
    }

    /**
     *
     *
     */
    private void setMapping() {
        Vector rowMap = new Vector();

        //DATASET
        rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DATASET table"));
        rowMap.add(getFieldMap("short_name", "ds_name", false, "dataset short name in DATASET table"));
        rowMap.add(getFieldMap("identifier", "idfier", false, "dataset identifier in DATASET table"));
        rowMap.add(getFieldMap("regstatus", "reg_status", true, "REG_STATUS in DATASET table"));

        tblMap.put("DATASET", rowMap);
        rowMap = new Vector();

        //DS_TABLE
        rowMap.add(getFieldMap("table_id", "tbl_id", false, "dataset table id in DS_TABLE table"));
        rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DS_TABLE table"));
        rowMap.add(getFieldMap("short_name", "short_name", false, "dataset table short name in DS_TABLE table"));
        rowMap.add(getFieldMap("identifier", "idfier", false, "dataset table identifier in DS_TABLE table"));

        tblMap.put("DS_TABLE", rowMap);
        rowMap = new Vector();

        //DATA ELEMENT
        rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in DATAELEM table"));
        rowMap.add(getFieldMap("type", "type", false, "data element type in DATAELEM table"));
        rowMap.add(getFieldMap("short_name", "delem_name", false, "data element short name in DATAELEM table"));
        rowMap.add(getFieldMap("identifier", "idfier", false, "data element identifier in DATAELEM table"));
        // The "GIS type" is a legacy attribute of data elements. It is backward-supported in dataset import, though ignored
        // by the import handler.
        rowMap.add(getFieldMap("gistype", "gis", true, "GIS in DATAELEM table"));

        tblMap.put("DATAELEM", rowMap);
        rowMap = new Vector();

        //TBL2ELEM
        rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in TBL2ELEM table"));
        rowMap.add(getFieldMap("table_id", "table_id", false, "dataset table id in TBL2ELEM"));
        //rowMap.add(getFieldMap("position", "pos", true, "position in TBL2ELEM table"));
        tblMap.put("TBL2ELEM", rowMap);
        rowMap = new Vector();

        //FIXED VALUE
        rowMap.add(getFieldMap("dataelem_id", "delem_id", true, "data element id in FIXED_VALUE table"));
        //rowMap.add(getFieldMap("parent_id", "parent_id", true, "parent fixed value id in FIXED_VALUE table"));
        rowMap.add(getFieldMap("fixed_value_id", "id", true, "id"));
        rowMap.add(getFieldMap("value", "new_value", true, "fixed value in FIXED_VALUE table"));
        rowMap.add(getFieldMap("definition", "definition", true, "definition in FIXED_VALUE table"));
        rowMap.add(getFieldMap("shortdescription", "short_desc", true, "shortdescription in FIXED_VALUE table"));
        tblMap.put("FIXED_VALUE", rowMap);
    }

    /**
     *
     * @param table
     * @param row
     */
    private void addUnknown(String table, Hashtable row) {

        Vector unknown = null;
        String field;

        if (unknownTbl == null) {
            unknownTbl = new Hashtable();
        }
        if (unknownTbl.containsKey(table)) {
            unknown = (Vector) unknownTbl.get(table);
        }
        if (unknown == null) {
            unknown = new Vector();
        }
        Enumeration keys = row.keys();
        while (keys.hasMoreElements()) {
            field = (String) keys.nextElement();
            if (unknown.indexOf(field) == -1) {
                unknown.add(field);
            }
        }
        if (unknown.size() > 0) {
            unknownTbl.put(table, unknown);
        }

    }

    /**
     *
     * @param impField
     * @param param
     * @param allowNull
     * @param text
     * @return
     */
    private Hashtable getFieldMap(String impField, String param, boolean allowNull, String text) {
        Hashtable fieldMap = new Hashtable();
        fieldMap.put("imp", impField);
        fieldMap.put("param", param);
        fieldMap.put("allowNull", String.valueOf(allowNull));
        fieldMap.put("text", text);

        return fieldMap;

    }

    /**
     *
     * @param code
     * @return
     */
    private String getContextName(String code) {
        String ret = "";
        if (code.equals("DST")) {
            ret = "dataset";
        }
        if (code.equals("FXV")) {
            ret = "allowable value";
        }
        if (code.equals("CH1")) {
            ret = "data element with fixed values";
        }
        if (code.equals("CH2")) {
            ret = "data element with quantitative values";
        }
        if (code.equals("TBL")) {
            ret = "dataset table";
        }
        return ret;
    }

    /**
     *
     * @return
     */
    public String getResponseText() {
        return responseText.toString();
    }

    /**
     *
     * @param type
     */
    public void setImportType(String type) {
        if (type.equals("FXV")) {
            importType = IMPORT_TYPE_FXV_ONLY;
        } else {
            importType = IMPORT_TYPE_WHOLE_DATASETS;
        }
    }

    /**
     *
     * @param parent_id
     */
    public void setParentID(String parent_id) {
        this.importParentID = parent_id;
    }

    /**
     *
     * @param user
     */
    public void setUser(DDUser user) {
        this.user = user;
    }

    /**
     *
     * @param unixTimestampMillisec
     */
    public void setDate(String unixTimestampMillisec) {
        this.date = unixTimestampMillisec;
    }

    /**
     *
     * @param t
     * @param errMsg
     */
    private void handleError(Throwable t, String errMsg) {

        errorCount++;

        if (t != null) {
            t.printStackTrace();
        }

        if (errMsg != null) {
            responseText.append(errMsg);
        }
    }

    /**
     *
     * @param t
     * @param errMsg
     */
    private void handleWarning(Throwable t, String errMsg) {

        warningCount++;

        if (t != null) {
            t.printStackTrace();
        }

        if (errMsg != null) {
            responseText.append(errMsg);
        }
    }

    /**
     *
     * @return
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     *
     * @return
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * @return
     */
    public int getCountDatasetsFound() {
        return countDatasetsFound;
    }

    /**
     * @return
     */
    public int getCountDatasetsImported() {
        return countDatasetsImported;
    }

    /**
     * @return
     */
    public int getCountElementsFound() {
        return countElementsFound;
    }

    /**
     * @return
     */
    public int getCountElementsImported() {
        return countElementsImported;
    }

    /**
     * @return
     */
    public int getCountFixedValuesFound() {
        return countFixedValuesFound;
    }

    /**
     * @return
     */
    public int getCountFixedValuesImported() {
        return countFixedValuesImported;
    }

    /**
     * @return
     */
    public int getCountTablesFound() {
        return countTablesFound;
    }

    /**
     * @return
     */
    public int getCountTablesImported() {
        return countTablesImported;
    }
}
