package eionet.meta;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.services.AttributeService;
import eionet.datadict.services.data.AttributeDataService;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.*;
import eionet.meta.service.*;
import eionet.meta.service.data.SchemaConversionsData;
import eionet.meta.spring.SpringApplicationContext;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.StringOrdinalComparator;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Search engine.
 *
 * @author Jaanus Heinlaid
 *
 */
public class DDSearchEngine {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DDSearchEngine.class);

    /** */
    public static final String ORDER_BY_M_ATTR_NAME = "SHORT_NAME";
    public static final String ORDER_BY_M_ATTR_DISP_ORDER = "DISP_ORDER";
    private static final String ELEMENT_TYPE = "elm";

    private Connection conn = null;
    private String sessionID = "";

    // TODO: Check if the domain needs to be updated
    private String rodObligUrl = "http://rod.eionet.eu.int/obligations/";
    private String predIdentifier = "http://purl.org/dc/elements/1.1/identifier";
    private String predTitle = "http://purl.org/dc/elements/1.1/title";

    private DDUser user = null;
    private ApplicationContext springContext;

    /**
     *
     * @param conn
     */
    public DDSearchEngine(Connection conn) {
        this.conn = conn;
        initSpringContext();

        String s = Props.getProperty(PropsIF.OUTSERV_ROD_OBLIG_URL);
        if (s != null && s.length() > 0) {
            rodObligUrl = s;
        }

        s = Props.getProperty(PropsIF.OUTSERV_PRED_IDENTIFIER);
        if (s != null && s.length() > 0) {
            predIdentifier = s;
        }

        s = Props.getProperty(PropsIF.OUTSERV_PRED_TITLE);
        if (s != null && s.length() > 0) {
            predTitle = s;
        }
    }

    /**
     * Initializes Spring context.
     */
    protected void initSpringContext() {
        springContext = SpringApplicationContext.getContext();
    }

    public DDSearchEngine(Connection conn, String sessionID) {
        this(conn);
        this.sessionID = sessionID;
    }

    public void setUser(final DDUser user) {
        this.user = user;
    }

    public DDUser getUser() {
        return this.user;
    }

    /**
     *
     * @param datasetID
     * @return all dataset elements
     * @throws SQLException
     *             if database query fails
     */
    public Vector getAllDatasetElements(String datasetID) throws SQLException {

        if (datasetID == null || datasetID.trim().length() == 0) {
            throw new IllegalArgumentException("Dataset ID must not be blank!");
        }

        INParameters inPrms = new INParameters();

        StringBuffer buf =
                new StringBuffer()
                        .append("select distinct DATAELEM.*, ")
                        .append("TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION, TBL2ELEM.MULTIVAL_DELIM, TBL2ELEM.MANDATORY, TBL2ELEM.PRIM_KEY, ")
                        .append("DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, ")
                        .append("DATASET.DATASET_ID, DATASET.IDENTIFIER, DATASET.SHORT_NAME, ")
                        .append("DATASET.VERSION, DATASET.REG_STATUS ").append("from TBL2ELEM ")
                        .append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ")
                        .append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ")
                        .append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ")
                        .append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ").append("where ")
                        .append("DST2TBL.DATASET_ID=").append(inPrms.add(datasetID, Types.INTEGER)).append(" and ")
                        .append("DATASET.DELETED is null and DATAELEM.WORKING_COPY='N' ")
                        .append("order by DATAELEM.DATAELEM_ID asc");

        LOGGER.debug(buf.toString());

        // prepare the statement for dynamic attributes
        ResultSet attrsRs = null;
        PreparedStatement attrsStmt = null;
        StringBuffer attrsQry =
                new StringBuffer().append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ")
                        .append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ")
                        .append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
        attrsStmt = conn.prepareStatement(attrsQry.toString());

        // finally execute the monster query
        Vector result = new Vector();
        PreparedStatement elemsStmt = null;
        ResultSet elemsRs = null;

        int counter = 0;

        try {
            elemsStmt = SQL.preparedStatement(buf.toString(), inPrms, conn);
            elemsRs = elemsStmt.executeQuery();

            // process ResultSet
            String curElmIdf = null;
            while (elemsRs.next()) {

                counter++;

                String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
                if (elmIdf == null) {
                    continue;
                }

                // the following if block skips non-latest ELEMENTS
                if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                    continue;
                } else {
                    curElmIdf = elmIdf;
                }

                // construct the element
                int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
                DataElement elm =
                        new DataElement(String.valueOf(elmID), elemsRs.getString("DATAELEM.SHORT_NAME"),
                                elemsRs.getString("DATAELEM.TYPE"));

                elm.setIdentifier(elmIdf);
                elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
                elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
                elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
                elm.setTopNs(elemsRs.getString("DATAELEM.TOP_NS"));
                elm.setTableID(elemsRs.getString("TBL2ELEM.TABLE_ID"));
                elm.setPositionInTable(elemsRs.getString("TBL2ELEM.POSITION"));
                elm.setValueDelimiter(elemsRs.getString("TBL2ELEM.MULTIVAL_DELIM"));
                elm.setMandatoryFlag(elemsRs.getBoolean("TBL2ELEM.MANDATORY"));
                elm.setPrimaryKey(elemsRs.getBoolean("TBL2ELEM.PRIM_KEY"));
                elm.setDatasetID(elemsRs.getString("DATASET.DATASET_ID"));
                elm.setDstShortName(elemsRs.getString("DATASET.SHORT_NAME"));
                elm.setTblShortName(elemsRs.getString("DS_TABLE.SHORT_NAME"));
                elm.setTblIdentifier(elemsRs.getString("DS_TABLE.IDENTIFIER"));
                elm.setDstIdentifier(elemsRs.getString("DATASET.IDENTIFIER"));
                elm.setNamespace(new Namespace(elemsRs.getString("DATAELEM.PARENT_NS"), "", "", "", ""));
                elm.setCheckedoutCopyID(elemsRs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
                elm.setDate(elemsRs.getString("DATAELEM.DATE"));

                // execute the statement prepared for dynamic attributes
                attrsStmt.setInt(1, elmID);
                attrsRs = attrsStmt.executeQuery();
                while (attrsRs.next()) {
                    String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
                    DElemAttribute attr = elm.getAttributeById(attrID);
                    if (attr == null) {
                        attr =
                                new DElemAttribute(attrID, attrsRs.getString("M_ATTRIBUTE.NAME"),
                                        attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"),
                                        attrsRs.getString("ATTRIBUTE.VALUE"), attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
                                        attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
                                        attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                        elm.addAttribute(attr);
                    } else {
                        attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
                    }
                }

                // add the element to the result Vector
                result.add(elm);
            }
        } finally {
            try {
                if (elemsRs != null) {
                    elemsRs.close();
                }
                if (attrsRs != null) {
                    attrsRs.close();
                }
                if (elemsStmt != null) {
                    elemsStmt.close();
                }
                if (attrsStmt != null) {
                    attrsStmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return result;
    }

    /**
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector<DataElement> getDataElements() throws SQLException {
        return getDataElements(null, null, null, null);
    }

    /**
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector<DataElement> getDataElements(Vector params, String type, String datasetIdf, String shortName) throws SQLException {
        return getDataElements(params, type, datasetIdf, shortName, null);
    }

    /**
     * Get data elements by table id. 5 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector<DataElement> getDataElements(Vector unUsed1, String unUsed2, String unUsed3, String unUsed4, String tableID)
            throws SQLException {

        // make sure we have the tableID
        if (Util.isEmpty(tableID)) {
            throw new SQLException("getDataElements(): tableID is missing!");
        }

        // build the monster query.
        INParameters inPrms = new INParameters();
        StringBuffer monsterQry =
                new StringBuffer()
                        .append("select distinct DATAELEM.*, ")
                        .append("TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION, TBL2ELEM.MULTIVAL_DELIM, TBL2ELEM.MANDATORY, TBL2ELEM.PRIM_KEY, ")
                        .append("DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, ")
                        .append("DATASET.DATASET_ID, DATASET.IDENTIFIER, DATASET.SHORT_NAME, ")
                        .append("DATASET.VERSION, DATASET.REG_STATUS ").append("from TBL2ELEM ")
                        .append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ")
                        .append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ")
                        .append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ")
                        .append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ").append("where ")
                        .append("TBL2ELEM.TABLE_ID=").append(inPrms.add(tableID, Types.INTEGER))
                        .append(" and DATASET.DELETED is null and ").append("DATAELEM.WORKING_COPY='N'") // JH200505 - don't want
                        // working
                        // copies coming up here
                        .append(" order by TBL2ELEM.POSITION asc");

        // log the monster query
        LOGGER.debug(monsterQry.toString());

        // prepare the statement for dynamic attributes
        ResultSet attrsRs = null;
        PreparedStatement attrsStmt = null;
        StringBuffer attrsQry =
                new StringBuffer().append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ")
                        .append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ")
                        .append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
        attrsStmt = conn.prepareStatement(attrsQry.toString());

        // finally execute the monster query
        Vector<DataElement> result = new Vector<DataElement>();
        PreparedStatement elemsStmt = null;
        ResultSet elemsRs = null;

        try {
            elemsStmt = SQL.preparedStatement(monsterQry.toString(), inPrms, conn);
            elemsRs = elemsStmt.executeQuery();

            // process ResultSet
            String curElmIdf = null;
            while (elemsRs.next()) {
                String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
                if (elmIdf == null) {
                    continue;
                }

                // the following if block skips non-latest ELEMENTS
                if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                    continue;
                } else {
                    curElmIdf = elmIdf;
                }

                // construct the element
                int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
                DataElement elm =
                        new DataElement(String.valueOf(elmID), elemsRs.getString("DATAELEM.SHORT_NAME"),
                                elemsRs.getString("DATAELEM.TYPE"));

                elm.setIdentifier(elmIdf);
                elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
                elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
                elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
                elm.setTopNs(elemsRs.getString("DATAELEM.TOP_NS"));
                elm.setTableID(elemsRs.getString("TBL2ELEM.TABLE_ID"));
                elm.setPositionInTable(elemsRs.getString("TBL2ELEM.POSITION"));
                elm.setValueDelimiter(elemsRs.getString("TBL2ELEM.MULTIVAL_DELIM"));
                elm.setMandatoryFlag(elemsRs.getBoolean("TBL2ELEM.MANDATORY"));
                elm.setPrimaryKey(elemsRs.getBoolean("TBL2ELEM.PRIM_KEY"));
                elm.setDatasetID(elemsRs.getString("DATASET.DATASET_ID"));
                elm.setDstShortName(elemsRs.getString("DATASET.SHORT_NAME"));
                elm.setTblShortName(elemsRs.getString("DS_TABLE.SHORT_NAME"));
                elm.setTblIdentifier(elemsRs.getString("DS_TABLE.IDENTIFIER"));
                elm.setDstIdentifier(elemsRs.getString("DATASET.IDENTIFIER"));
                elm.setNamespace(new Namespace(elemsRs.getString("DATAELEM.PARENT_NS"), "", "", "", ""));
                elm.setCheckedoutCopyID(elemsRs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
                elm.setDate(elemsRs.getString("DATAELEM.DATE"));

                elm.setVocabularyId(elemsRs.getString("DATAELEM.VOCABULARY_ID"));
                elm.setAllConceptsValid(elemsRs.getBoolean("DATAELEM.ALL_CONCEPTS_LEGAL"));

                // execute the statement prepared for dynamic attributes
                attrsStmt.setInt(1, elmID);
                attrsRs = attrsStmt.executeQuery();
                while (attrsRs.next()) {
                    String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
                    DElemAttribute attr = elm.getAttributeById(attrID);
                    if (attr == null) {
                        attr =
                                new DElemAttribute(attrID, attrsRs.getString("M_ATTRIBUTE.NAME"),
                                        attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"), 
                                        attrsRs.getString("ATTRIBUTE.VALUE"), attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
                                        attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
                                        attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                        elm.addAttribute(attr);
                    } else {
                        attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
                    }
                }

                // add the element to the result Vector
                result.add(elm);
            }
        } finally {
            try {
                if (elemsRs != null) {
                    elemsRs.close();
                }
                if (attrsRs != null) {
                    attrsRs.close();
                }
                if (elemsStmt != null) {
                    elemsStmt.close();
                }
                if (attrsStmt != null) {
                    attrsStmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return result;
    }

    /**
     * Get data elements by table id and dataset id 6 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector
            getDataElements(Vector params, String type, String datasetIdf, String shortName, String tableID, String datasetID)
                    throws SQLException {

        return getDataElements(params, type, datasetIdf, shortName, tableID, datasetID, false);
    }

    /**
     * Get data elements with control over working copies 7 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDataElements(Vector params, String type, String datasetIdf, String shortName, String tableID,
            String datasetID, boolean wrkCopies) throws SQLException {

        return getDataElements(params, type, datasetIdf, shortName, tableID, datasetID, wrkCopies, null);
    }

    /**
     * Get data elements, control over working copies & params oper 8 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDataElements(Vector params, String type, String datasetIdf, String shortName, String tableID,
            String datasetID, boolean wrkCopies, String oper) throws SQLException {
        return getDataElements(params, type, datasetIdf, shortName, null, tableID, datasetID, wrkCopies, null);
    }

    /**
     * Get data elements, control over working copies & params oper 9 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDataElements(Vector params, String type, String datasetIdf, String shortName, String idfier, String tableID,
            String datasetID, boolean wrkCopies, String oper) throws SQLException {

        return getDataElements(params, type, datasetIdf, shortName, idfier, tableID, datasetID, wrkCopies, false, oper);
    }

    /**
     * Get data elements, control over working copies, historic versions & params oper 10 inputs.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDataElements(Vector params, String type, String datasetIdf, String shortName, String idfier, String tableID,
            String datasetID, boolean wrkCopies, boolean isIncludeHistoricVersions, String oper) throws SQLException {

        // set up the IN parameters for the upcoming PreparedStatement
        INParameters inPrms = new INParameters();

        // oper defines the search precision. If it's missing, set it to substring search
        if (oper == null) {
            oper = " like ";
        }

        // build the "from" part of the SQL query
        StringBuffer tables = new StringBuffer("DATAELEM, TBL2ELEM, DS_TABLE, DST2TBL, DATASET");

        // start building the "where" part of the SQL query
        StringBuffer constraints = new StringBuffer();

        // join the "from tables"
        constraints.append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID and ").append("TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID and ")
                .append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID and ").append("DST2TBL.DATASET_ID=DATASET.DATASET_ID and ");

        // we look only in non-deleted datasets
        constraints.append("DATASET.DELETED is null");
        // if not looking in a concrete dataset copy, take into account the working copies parameter
        if (datasetID == null) {
            if (wrkCopies) {
                constraints.append(" and DATASET.WORKING_COPY='Y'");
            } else {
                constraints.append(" and DATASET.WORKING_COPY='N'");
            }
        }
        // we look only for non-common elements here, so DATAELEM.PARENT_NS must NOT be null
        constraints.append(" and DATAELEM.PARENT_NS is not null");

        // set the element type (CH1 or CH2)
        if (type != null && type.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.TYPE=").append(inPrms.add(type));
        }

        // set the element short name
        if (shortName != null && shortName.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.SHORT_NAME");
            // short name is not fulltext-indexed, so force "match" to "like"
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inPrms.add("%" + shortName + "%"));
            } else {
                constraints.append(inPrms.add(shortName));
            }
        }

        // set the element identifier
        if (idfier != null && idfier.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.IDENTIFIER");
            // identifier is not fulltext-indexed, so force "match" to "like"
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inPrms.add("%" + idfier + "%"));
            } else {
                constraints.append(inPrms.add(idfier));
            }
        }

        // see if looking for elements of a concrete table
        if (tableID != null && tableID.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("TBL2ELEM.TABLE_ID=").append(inPrms.add(tableID, Types.INTEGER));
        }

        // see if looking for elements of a concrete dataset copy
        if (datasetID != null && datasetID.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            if (datasetID.equals("-1")) {
                constraints.append("DATASET.DATASET_ID IS NULL");
            } else {
                constraints.append("DATASET.DATASET_ID=").append(inPrms.add(datasetID, Types.INTEGER));
            }
        }

        // see if looking for elements of datasets with a concrete identifier
        if (datasetIdf != null && datasetIdf.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATASET.IDENTIFIER=").append(inPrms.add(datasetIdf));
        }

        // the loop for processing dynamic search parameters
        for (int i = 0; params != null && i < params.size(); i++) {

            String index = String.valueOf(i + 1);
            DDSearchParameter param = (DDSearchParameter) params.get(i);
            String attrID = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();
            // Deal with the "from" part. For each dynamic attribute,
            // create an alias to the ATTRIBUTE table
            tables.append(", ATTRIBUTE as ATTR" + index);

            // deal with the where part
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("ATTR").append(index).append(".M_ATTRIBUTE_ID").append(idOper)
                    .append(inPrms.add(attrID, Types.INTEGER)).append(" and ");

            // concatenate the searched values with "or"
            if (attrValues != null && attrValues.size() != 0) {
                constraints.append("(");
                for (int j = 0; j < attrValues.size(); j++) {
                    if (j > 0) {
                        constraints.append(" or ");
                    }
                    if (valueOper != null && valueOper.trim().equalsIgnoreCase("MATCH")) {
                        constraints.append("match(ATTR").append(index).append(".VALUE) against(")
                                .append(inPrms.add(attrValues.get(j), Types.VARCHAR)).append(")");
                    } else {
                        constraints.append("ATTR").append(index).append(".VALUE").append(valueOper)
                                .append(inPrms.add(attrValues.get(j), Types.VARCHAR));
                    }
                }
                constraints.append(")");
            }
            // join alias'ed ATTRIBUTE tables with DATAELEM
            constraints.append(" and ").append("ATTR").append(index).append(".DATAELEM_ID=DATAELEM.DATAELEM_ID and ")
                    .append("ATTR").append(index).append(".PARENT_TYPE='E'");
        }
        // end of dynamic parameters loop

        // compile the query
        StringBuffer monsterQry =
                new StringBuffer().append("select distinct DATAELEM.*, TBL2ELEM.TABLE_ID, TBL2ELEM.POSITION, ")
                        .append("DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ").append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, ")
                        .append("DATASET.DATASET_ID, DATASET.IDENTIFIER, DATASET.SHORT_NAME, DATASET.WORKING_USER, ")
                        .append("DATASET.VERSION, DATASET.REG_STATUS from ").append(tables.toString());
        if (constraints.length() != 0) {
            monsterQry.append(" where ").append(constraints.toString());
        }
        if (tableID != null && tableID.length() != 0) {
            monsterQry.append(" order by TBL2ELEM.POSITION");
        } else {
            monsterQry.append(" order by ").append("DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, ")
                    .append("DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc, ")
                    .append("DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");
        }

        LOGGER.debug(monsterQry.toString());

        // see if dynamic attributes of elements should be fetched and if so,
        // prepare the relevant statement
        ResultSet attrsRs = null;
        PreparedStatement attrsStmt = null;
        boolean getAttributes = Util.isEmpty(tableID) ? false : true;
        if (getAttributes) {
            StringBuffer attrsQry =
                    new StringBuffer().append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ")
                            .append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ")
                            .append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
            attrsStmt = conn.prepareStatement(attrsQry.toString());
        }

        // finally execute the monster query
        Vector result = new Vector();
        PreparedStatement elemsStmt = null;
        ResultSet elemsRs = null;

        LOGGER.info("Non common elements query: " + monsterQry.toString());
        try {
            elemsStmt = SQL.preparedStatement(monsterQry.toString(), inPrms, conn);
            elemsRs = elemsStmt.executeQuery();

            String curDstID = null;
            String curDstIdf = null;
            String curTblID = null;
            String curTblIdf = null;
            String curElmIdf = null;

            // process ResultSet
            while (elemsRs.next()) {

                String dstID = elemsRs.getString("DATASET.DATASET_ID");
                String dstIdf = elemsRs.getString("DATASET.IDENTIFIER");
                if (dstID == null || dstIdf == null) {
                    continue;
                }

                // the following if block skips elements from non-latest DATASETS
                if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                    curDstID = dstID;
                    curDstIdf = dstIdf;
                } else if (!isIncludeHistoricVersions) {
                    if (!curDstID.equals(dstID)) {
                        continue;
                    }
                }

                String tblID = elemsRs.getString("DS_TABLE.TABLE_ID");
                String tblIdf = elemsRs.getString("DS_TABLE.IDENTIFIER");
                // skip non-existing tables, ie trash from some erroneous situation
                if (tblID == null || tblIdf == null) {
                    continue;
                }

                int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");
                String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
                // skip non-existing elements, ie trash from some erroneous situation
                if (elmIdf == null) {
                    continue;
                }

                // construct the element object
                DataElement elm =
                        new DataElement(String.valueOf(elmID), elemsRs.getString("DATAELEM.SHORT_NAME"),
                                elemsRs.getString("DATAELEM.TYPE"));
                elm.setIdentifier(elemsRs.getString("DATAELEM.IDENTIFIER"));
                elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
                elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
                elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
                elm.setTopNs(elemsRs.getString("DATAELEM.TOP_NS"));
                elm.setDate(elemsRs.getString("DATAELEM.DATE"));
                elm.setTableID(elemsRs.getString("TBL2ELEM.TABLE_ID"));
                elm.setPositionInTable(elemsRs.getString("TBL2ELEM.POSITION"));
                elm.setDatasetID(elemsRs.getString("DATASET.DATASET_ID"));
                elm.setDstShortName(elemsRs.getString("DATASET.SHORT_NAME"));
                elm.setTblShortName(elemsRs.getString("DS_TABLE.SHORT_NAME"));
                elm.setDstIdentifier(elemsRs.getString("DATASET.IDENTIFIER"));
                elm.setTblIdentifier(elemsRs.getString("DS_TABLE.IDENTIFIER"));
                elm.setNamespace(new Namespace(elemsRs.getString("DATAELEM.PARENT_NS"), "", "", "", ""));
                elm.setCheckedoutCopyID(elemsRs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
                elm.setDstWorkingUser(elemsRs.getString("DATASET.WORKING_USER"));
                elm.setDstStatus(elemsRs.getString("DATASET.REG_STATUS"));
                elm.setVocabularyId(elemsRs.getString("DATAELEM.VOCABULARY_ID"));
                elm.setAllConceptsValid(elemsRs.getBoolean("DATAELEM.ALL_CONCEPTS_LEGAL"));


                // if attributes should be fetched, execute the relevant statement
                if (getAttributes) {
                    attrsStmt.setInt(1, elmID);
                    attrsRs = attrsStmt.executeQuery();
                    while (attrsRs.next()) {
                        String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
                        DElemAttribute attr = elm.getAttributeById(attrID);
                        if (attr == null) {
                            attr =
                                    new DElemAttribute(attrID, attrsRs.getString("M_ATTRIBUTE.NAME"),
                                            attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"), 
                                            attrsRs.getString("ATTRIBUTE.VALUE"), attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
                                            attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
                                            attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                            elm.addAttribute(attr);
                        } else {
                            attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
                        }
                    }
                }

                // add the element object to the result Vector
                result.add(elm);
            }
        } finally {
            try {
                if (elemsRs != null) {
                    elemsRs.close();
                }
                if (attrsRs != null) {
                    attrsRs.close();
                }
                if (elemsStmt != null) {
                    elemsStmt.close();
                }
                if (attrsStmt != null) {
                    attrsStmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return result;
    }

    /**
     *
     * @param params
     * @param type
     * @param shortName
     * @param idfier
     * @param wrkCopies
     * @param oper
     * @return common elements as vector
     * @throws SQLException
     *             if database query fails
     */
    public Vector getCommonElements(Vector params, String type, String shortName, String idfier, boolean wrkCopies, String oper)
            throws SQLException {
        return getCommonElements(params, type, shortName, idfier, wrkCopies, false, oper);
    }

    /**
     *
     * @param params
     * @param type
     * @param shortName
     * @param idfier
     * @param wrkCopies
     * @param isIncludeHistoricVersions
     * @param oper
     * @return common elements as vector
     * @throws SQLException
     *             if database query fails
     */
    public Vector getCommonElements(Vector params, String type, String shortName, String idfier, boolean wrkCopies,
            boolean isIncludeHistoricVersions, String oper) throws SQLException {

        return getCommonElements(params, type, shortName, idfier, null, wrkCopies, false, oper);
    }

    /**
     *
     * @param params
     * @param type
     * @param shortName
     * @param idfier
     * @param regStatus
     * @param wrkCopies
     * @param isIncludeHistoricVersions
     * @param oper
     * @return common elements as vector
     * @throws SQLException
     *             if database query fails
     */
    public Vector getCommonElements(Vector params, String type, String shortName, String idfier, String regStatus,
            boolean wrkCopies, boolean isIncludeHistoricVersions, String oper) throws SQLException {

        Vector result = new Vector();

        // set up the IN parameters for the up-coming query
        INParameters inParams = new INParameters();

        // oper defines the search precision. If it's missing, set it to substring search
        if (oper == null) {
            oper = " like ";
        }

        // set up the "from" part of the SQL query
        StringBuffer tables = new StringBuffer("DATAELEM");

        // start building the "where" part of the SQL query
        StringBuffer constraints = new StringBuffer();

        // we look only for common elements here, so DATAELEM.PARENT_NS must be null
        constraints.append("DATAELEM.PARENT_NS is null");

        // set the registration status
        if (regStatus != null && regStatus.trim().length() > 0) {
            if (constraints.length() > 0) {
                constraints.append(" and ");
            }

            List<String> regStatuses = buildListFromCsv(regStatus);

            constraints.append("DATAELEM.REG_STATUS IN(").append(inParams.addArray(regStatuses)).append(")");
        }

        // set the element type (CH1 or CH2)
        if (type != null && type.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.TYPE=").append(inParams.add(type));
        }

        // set the element short name
        if (shortName != null && shortName.length() != 0) {

            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.SHORT_NAME");

            // SHORT_NAME is not fulltext indexed, so force "match" to "like"
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);

            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + shortName + "%"));
            } else {
                constraints.append(inParams.add(shortName));
            }
        }

        // set the element identifier
        if (idfier != null && idfier.length() != 0) {

            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATAELEM.IDENTIFIER");

            // IDENTIFIER is not fulltext indexed, so force "match" to "like"
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);

            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + idfier + "%"));
            } else {
                constraints.append(inParams.add(idfier));
            }
        }

        // the loop for processing dynamic search parameters
        for (int i = 0; params != null && i < params.size(); i++) {

            String index = String.valueOf(i + 1);
            DDSearchParameter param = (DDSearchParameter) params.get(i);

            String attrID = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();

            // Deal with the "from" part. For each dynamic attribute,
            // create an alias to the ATTRIBUTE table
            tables.append(", ATTRIBUTE as ATTR" + index);

            // deal with the where part
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }

            constraints.append("ATTR").append(index).append(".M_ATTRIBUTE_ID").append(idOper)
                    .append(inParams.add(attrID, Types.INTEGER)).append(" and ");

            // concatenate the searched values with "or"
            if (attrValues != null && attrValues.size() != 0) {
                constraints.append("(");
                for (int j = 0; j < attrValues.size(); j++) {
                    if (j > 0) {
                        constraints.append(" or ");
                    }
                    if (valueOper != null && valueOper.trim().equalsIgnoreCase("MATCH")) {
                        constraints.append("match(ATTR").append(index).append(".VALUE) against(")
                                .append(inParams.add(attrValues.get(j))).append(")");
                    } else {
                        constraints.append("ATTR").append(index).append(".VALUE").append(valueOper)
                                .append(inParams.add(attrValues.get(j)));
                    }
                }
                constraints.append(")");
            }

            // join alias'ed ATTRIBUTE tables with DATAELEM
            constraints.append(" and ").append("ATTR").append(index).append(".DATAELEM_ID=DATAELEM.DATAELEM_ID and ")
                    .append("ATTR").append(index).append(".PARENT_TYPE='E'");
        }
        // end of dynamic parameters loop

        // if the user is missing, override the wrkCopies argument
        if (user == null) {
            wrkCopies = false;
        }
        if (constraints.length() != 0) {
            constraints.append(" and ");
        }
        if (wrkCopies) {
            constraints.append("DATAELEM.WORKING_COPY='Y' and DATAELEM.WORKING_USER=").append(inParams.add(user.getUserName()));
        } else {
            constraints.append("DATAELEM.WORKING_COPY='N'");
        }

        // now build the monster query.
        // first the "select from" part
        StringBuffer monsterQry = new StringBuffer().append("select distinct DATAELEM.* from ").append(tables.toString());

        // then the "where part"
        if (constraints.length() != 0) {
            monsterQry.append(" where ").append(constraints.toString());
        }

        // finally the "order by"
        monsterQry.append(" order by DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");

        // log the monster query
        LOGGER.debug(monsterQry.toString());

        // prepare the statement for fecthing the elements' dynamic attributes
        ResultSet attrsRs = null;
        PreparedStatement attrsStmt = null;
        StringBuffer attrsQry =
                new StringBuffer().append("select M_ATTRIBUTE.*, ATTRIBUTE.VALUE from M_ATTRIBUTE, ATTRIBUTE ")
                        .append("where ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID and ")
                        .append("ATTRIBUTE.PARENT_TYPE='E' and ATTRIBUTE.DATAELEM_ID=?");
        attrsStmt = conn.prepareStatement(attrsQry.toString());

        // finally execute the monster query
        PreparedStatement elemsStmt = null;
        ResultSet elemsRs = null;

        LOGGER.info("Common elements query: " + monsterQry.toString());
        try {
            elemsStmt = SQL.preparedStatement(monsterQry.toString(), inParams, conn);
            elemsRs = elemsStmt.executeQuery();

            String curElmIdf = null;

            // process ResultSet
            while (elemsRs.next()) {

                String elmIdf = elemsRs.getString("DATAELEM.IDENTIFIER");
                if (elmIdf == null) {
                    continue;
                }

                // the following if block skips non-latest
                if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                    if (!isIncludeHistoricVersions) {
                        continue;
                    }
                } else {
                    curElmIdf = elmIdf;
                }

                // construct the element
                int elmID = elemsRs.getInt("DATAELEM.DATAELEM_ID");

                DataElement elm =
                        new DataElement(String.valueOf(elmID), elemsRs.getString("DATAELEM.SHORT_NAME"),
                                elemsRs.getString("DATAELEM.TYPE"));

                elm.setIdentifier(elemsRs.getString("DATAELEM.IDENTIFIER"));
                elm.setVersion(elemsRs.getString("DATAELEM.VERSION"));
                elm.setStatus(elemsRs.getString("DATAELEM.REG_STATUS"));
                elm.setWorkingCopy(elemsRs.getString("DATAELEM.WORKING_COPY"));
                elm.setWorkingUser(elemsRs.getString("DATAELEM.WORKING_USER"));
                elm.setDate(elemsRs.getString("DATAELEM.DATE"));

                // fetch the element's dynamic attributes
                attrsStmt.setInt(1, elmID);
                attrsRs = attrsStmt.executeQuery();
                while (attrsRs.next()) {
                    String attrID = attrsRs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID");
                    DElemAttribute attr = elm.getAttributeById(attrID);
                    if (attr == null) {
                        attr =
                                new DElemAttribute(attrID, attrsRs.getString("M_ATTRIBUTE.NAME"),
                                        attrsRs.getString("M_ATTRIBUTE.SHORT_NAME"), 
                                        attrsRs.getString("ATTRIBUTE.VALUE"), attrsRs.getString("M_ATTRIBUTE.DEFINITION"),
                                        attrsRs.getString("M_ATTRIBUTE.OBLIGATION"),
                                        attrsRs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                        elm.addAttribute(attr);
                    } else {
                        attr.addValue(attrsRs.getString("ATTRIBUTE.VALUE"));
                    }
                }

                // add the element to the result Vector
                result.add(elm);
            }
        } finally {
            try {
                if (elemsRs != null) {
                    elemsRs.close();
                }
                if (attrsRs != null) {
                    attrsRs.close();
                }
                if (elemsStmt != null) {
                    elemsStmt.close();
                }
                if (attrsStmt != null) {
                    attrsStmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return result;
    }

    /**
     *
     * @param elmIdf
     * @param tblIdf
     * @param dstIdf
     * @param statuses
     * @return latest element ID as string
     * @throws SQLException
     *             if database query fails
     */
    public String getLatestElmID(String elmIdf, String tblIdf, String dstIdf, Vector statuses) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer("select DATAELEM.DATAELEM_ID from DATAELEM");
        if (!Util.isEmpty(tblIdf) && !Util.isEmpty(dstIdf)) {

            // non-common element

            buf.append(", TBL2ELEM, DST2TBL, DS_TABLE, DATASET where ").append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID and ")
                    .append("TBL2ELEM.TABLE_ID=DST2TBL.TABLE_ID and ").append("DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID and ")
                    .append("DST2TBL.DATASET_ID=DATASET.DATASET_ID and ")
                    .append("DATASET.WORKING_COPY='N' and DATASET.DELETED is null and ");

            if (statuses != null && !statuses.isEmpty()) {

                buf.append("(");
                for (int i = 0; i < statuses.size(); i++) {

                    if (i > 0) {
                        buf.append(" or ");
                    }
                    buf.append("DATASET.REG_STATUS=").append(inParams.add(statuses.get(i).toString(), Types.VARCHAR));
                }
                buf.append(") and ");
            }

            buf.append("DATAELEM.IDENTIFIER=? and DS_TABLE.IDENTIFIER=? and DATASET.IDENTIFIER=?").append(
                    "order by DATASET.DATASET_ID desc limit 1");

            inParams.add(elmIdf);
            inParams.add(tblIdf);
            inParams.add(dstIdf);
        } else {
            buf.append(" where ").append("DATAELEM.WORKING_COPY='N' and DATAELEM.PARENT_NS is null and ");

            if (statuses != null && !statuses.isEmpty()) {

                buf.append("(");
                for (int i = 0; i < statuses.size(); i++) {

                    if (i > 0) {
                        buf.append(" or ");
                    }
                    buf.append("DATAELEM.REG_STATUS=").append(inParams.add(statuses.get(i).toString(), Types.VARCHAR));
                }
                buf.append(") and ");
            }

            buf.append("DATAELEM.IDENTIFIER=? ").append("order by DATAELEM.DATAELEM_ID desc limit 1");

            inParams.add(elmIdf);
        }

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param elmIdf
     * @param tblIdf
     * @param dstIdf
     * @param statuses
     * @return latest element as DataElement
     * @throws SQLException
     *             if database query fails
     */
    public DataElement getLatestElm(String elmIdf, String tblIdf, String dstIdf, Vector statuses) throws SQLException {

        String latestID = getLatestElmID(elmIdf, tblIdf, dstIdf, statuses);
        return latestID == null ? null : getDataElement(latestID);
    }

    /**
     *
     * @param identifier
     * @param datasetIdentifier
     * @param statuses
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public DsTable getLatestTbl(String identifier, String datasetIdentifier, List statuses) throws SQLException {

        String latestID = getLatestTblID(identifier, datasetIdentifier, statuses);
        return latestID == null ? null : getDatasetTable(latestID);
    }

    /**
     *
     * @param identifier
     * @param datasetIdentifier
     * @param statuses
     * @return table id as String
     * @throws SQLException
     *             if database query fails
     */
    public String getLatestTblID(String identifier, String datasetIdentifier, List statuses) throws SQLException {

        if (Util.isEmpty(identifier) || Util.isEmpty(datasetIdentifier)) {
            return null;
        }

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer();
        buf.append("select DST2TBL.TABLE_ID from DS_TABLE")
                .append(" left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID")
                .append(" left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID").append(" where DS_TABLE.IDENTIFIER=")
                .append(inParams.add(identifier, Types.VARCHAR)).append(" and DATASET.IDENTIFIER=")
                .append(inParams.add(datasetIdentifier, Types.VARCHAR))
                .append(" and DATASET.WORKING_COPY='N' and DATASET.CHECKEDOUT_COPY_ID is null")
                .append(" and DATASET.WORKING_USER is null and DATASET.DELETED is null");

        if (statuses != null && !statuses.isEmpty()) {

            buf.append(" and (");
            for (int i = 0; i < statuses.size(); i++) {

                if (i > 0) {
                    buf.append(" or ");
                }
                buf.append("DATASET.REG_STATUS=").append(inParams.add(statuses.get(i)));
            }
            buf.append(")");
        }
        buf.append(" order by DST2TBL.DATASET_ID desc");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            SQL.close(rs);
            SQL.close(pstmt);
        }
    }

    /**
     *
     * @param idf
     * @param statuses
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getLatestDstID(String idf, Vector statuses) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer();
        buf.append("select DATASET_ID from DATASET where WORKING_COPY='N'");
        buf.append(" and DELETED is null and IDENTIFIER=").append(inParams.add(idf));
        if (statuses != null && statuses.size() > 0) {
            buf.append(" and (");
            for (int i = 0; i < statuses.size(); i++) {
                if (i > 0) {
                    buf.append(" or ");
                }
                buf.append("REG_STATUS=").append(inParams.add(statuses.get(i)));
            }
            buf.append(")");
        }
        buf.append(" order by DATASET_ID desc limit 0,1");

        String dstID = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    rs.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return null;
    }

    /**
     *
     * @param idf
     * @param statuses
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Dataset getLatestDst(String idf, Vector statuses) throws SQLException {
        String latestID = getLatestDstID(idf, statuses);
        return latestID == null ? null : getDataset(latestID);
    }

    /**
     *
     * @param idf
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Dataset getLatestDst(String idf) throws SQLException {
        return getLatestDst(idf, null);
    }

    /*
     *
     */
    public DataElement getDataElement(String elmID) throws SQLException {
        return getDataElement(elmID, null);
    }

    /*
     *
     */
    public DataElement getDataElement(String elmID, String tblID) throws SQLException {
        return getDataElement(elmID, tblID, true);
    }

    /**
     *
     * @param elmID
     * @param tblID
     * @param inheritAttrs
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public DataElement getDataElement(String elmID, String tblID, boolean inheritAttrs) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer qry = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DataElement elm = null;
        try {
            // first find out if this is a common element
            qry = new StringBuffer("select * from DATAELEM where DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER));
            stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            boolean elmCommon = Util.isEmpty(rs.getString("PARENT_NS"));

            // Build the query which takes into account the tblID.
            // If the latter is null then take the table which is latest in history,
            // otherwise take exactly the table wanted by tblID
            inParams = new INParameters();
            qry = new StringBuffer("select DATAELEM.*");
            if (!elmCommon) {
                qry.append(", TBL2ELEM.POSITION, DS_TABLE.TABLE_ID, DS_TABLE.IDENTIFIER, ")
                        .append("DS_TABLE.SHORT_NAME, DS_TABLE.VERSION, DATASET.DATASET_ID, ")
                        .append("DATASET.IDENTIFIER, DATASET.SHORT_NAME, DATASET.VERSION");
            }
            qry.append(" from DATAELEM");
            if (!elmCommon) {
                qry.append(" left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ")
                        .append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ")
                        .append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ")
                        .append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID");
            }
            qry.append(" where DATAELEM.DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER));
            if (!elmCommon && !Util.isEmpty(tblID)) {
                qry.append(" and DS_TABLE.TABLE_ID=").append(inParams.add(tblID, Types.INTEGER));
            }

            qry.append(" order by ").append("DATAELEM.DATAELEM_ID desc");
            if (!elmCommon) {
                qry.append(", DS_TABLE.TABLE_ID desc, DATASET.DATASET_ID desc");
            }

            LOGGER.debug(qry.toString());

            // execute the query
            stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                elm =
                        new DataElement(rs.getString("DATAELEM.DATAELEM_ID"), rs.getString("DATAELEM.SHORT_NAME"),
                                rs.getString("DATAELEM.TYPE"));

                elm.setIdentifier(rs.getString("DATAELEM.IDENTIFIER"));
                elm.setVersion(rs.getString("DATAELEM.VERSION"));
                elm.setStatus(rs.getString("DATAELEM.REG_STATUS"));
                elm.setTopNs(rs.getString("DATAELEM.TOP_NS"));
                elm.setWorkingCopy(rs.getString("DATAELEM.WORKING_COPY"));
                elm.setWorkingUser(rs.getString("DATAELEM.WORKING_USER"));
                elm.setUser(rs.getString("DATAELEM.USER"));
                elm.setCheckedoutCopyID(rs.getString("DATAELEM.CHECKEDOUT_COPY_ID"));
                elm.setDate(rs.getString("DATAELEM.DATE"));
                elm.setNamespace(new Namespace(rs.getString("DATAELEM.PARENT_NS"), null, null, null, null));
                elm.setSuccessorId(rs.getString("DATAELEM.SUCCESSOR"));

                if (!elmCommon) {
                    elm.setTableID(rs.getString("DS_TABLE.TABLE_ID"));
                    elm.setDatasetID(rs.getString("DATASET.DATASET_ID"));
                    elm.setDstShortName(rs.getString("DATASET.SHORT_NAME"));
                    elm.setTblShortName(rs.getString("DS_TABLE.SHORT_NAME"));
                    elm.setDstIdentifier(rs.getString("DATASET.IDENTIFIER"));
                    elm.setTblIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
                    elm.setPositionInTable(rs.getString("TBL2ELEM.POSITION"));
                }

                elm.setVocabularyId(rs.getString("DATAELEM.VOCABULARY_ID"));
                elm.setAllConceptsValid(rs.getBoolean("ALL_CONCEPTS_LEGAL"));

                Vector attributes =
                        !elmCommon && inheritAttrs ? getAttributes(elmID, "E", elm.getTableID(), elm.getDatasetID())
                                : getAttributes(elmID, "E");

                elm.setAttributes(attributes);
            } else {
                return null;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return elm;
    }

    public Vector getDElemAttributes() throws SQLException {
        return getDElemAttributes(null, null, null);
    }

    public Vector getDElemAttributes(String attrId) throws SQLException {
        return getDElemAttributes(attrId, null);
    }

    public Vector getDElemAttributes(String attrId, String orderBy) throws SQLException {
        return getDElemAttributes(attrId, orderBy, null);
    }

    public Vector getDElemAttributes(String attrId, String orderBy, String inheritable) throws SQLException {
        INParameters inParams = new INParameters();
        StringBuffer qry = new StringBuffer();
        qry.append("select distinct M_ATTRIBUTE_ID as ID, M_ATTRIBUTE.*, T_RDF_NAMESPACE.URI as RDF_URI, T_RDF_NAMESPACE.ID as RDF_ID from M_ATTRIBUTE ");
        qry.append("LEFT JOIN T_RDF_NAMESPACE  ON M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID = T_RDF_NAMESPACE.ID");
        if (attrId != null) {
            qry.append(" where M_ATTRIBUTE_ID=").append(inParams.add(attrId, Types.INTEGER));;
        }

        if (inheritable != null) {
            if (attrId != null) {
                qry.append(" AND ");
            }
            if (attrId == null) {
                qry.append(" WHERE ");
            }
            qry.append("INHERIT=").append(inParams.add(inheritable));
        }

        if (orderBy == null) {
            orderBy = ORDER_BY_M_ATTR_NAME;
        }
        qry.append(" order by ");
        qry.append(orderBy);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try {
            stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
            rs = stmt.executeQuery();

            while (rs.next()) {
                DElemAttribute attr =
                        new DElemAttribute(rs.getString("ID"), rs.getString("NAME"), rs.getString("SHORT_NAME"), null,
                                rs.getString("DEFINITION"), rs.getString("OBLIGATION"));

                Namespace ns = getNamespace(rs.getString("NAMESPACE_ID"));
                if (ns != null) {
                    attr.setNamespace(ns);
                }

                attr.setDisplayProps(rs.getString("DISP_TYPE"), rs.getInt("DISP_ORDER"), rs.getInt("DISP_WHEN"),
                        rs.getString("DISP_WIDTH"), rs.getString("DISP_HEIGHT"), rs.getString("DISP_MULTIPLE"));
                attr.setRdfNamespaceId(rs.getInt("RDF_ID"));
                attr.setRdfPropertyUri(rs.getString("RDF_URI"));
                attr.setRdfPropertyName(rs.getString("RDF_PROPERTY_NAME"));

                attr.setInheritable(rs.getString("INHERIT"));

                v.add(attr);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }           

    public Vector<FixedValue> getFixedValues(String delemId) throws SQLException, DDException {
        return getFixedValues(delemId, "elem");
    }

    public Vector<FixedValue> getFixedValues(String delemId, String parentType) throws SQLException, DDException {

        if (isFixedValuesVocElement(delemId, parentType)) {
            return getVocabularyFixedValues(delemId);
        }
        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer();
        buf.append("select * from FXV where OWNER_ID=").append(inParams.add(delemId, Types.INTEGER)).append(" and OWNER_TYPE=")
                .append(inParams.add(parentType)).append(" order by VALUE asc");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector<FixedValue> v = new Vector<FixedValue>();

        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            while (rs.next()) {

                FixedValue fxv = new FixedValue(rs.getString("FXV_ID"), rs.getString("OWNER_ID"), rs.getString("VALUE"));

                String isDefault = rs.getString("IS_DEFAULT");
                if (isDefault != null && isDefault.equalsIgnoreCase("Y")) {
                    fxv.setDefault();
                }

                fxv.setDefinition(rs.getString("DEFINITION"));
                fxv.setShortDesc(rs.getString("SHORT_DESC"));

                v.add(fxv);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }


    public Vector<FixedValue> getFixedValuesOrderedByValue(String delemId, String parentType) throws SQLException, DDException {
        Vector<FixedValue> fixedValuesOrderedByValue = null;
        fixedValuesOrderedByValue = getFixedValues(delemId, parentType);

        Collections.sort(fixedValuesOrderedByValue, new Comparator<FixedValue>() {

            private StringOrdinalComparator cmp = new StringOrdinalComparator();

            @Override
            public int compare(FixedValue o1, FixedValue o2) {
                return cmp.compare(o1.getValue(), o2.getValue());
            }
        });

        return fixedValuesOrderedByValue;
    }

    /**
     * checks if CH3 element.
     * @param elemenID
     * @param type
     * @return
     */
    public boolean isFixedValuesVocElement(String elemenID, String type) throws ServiceException {
        //if "elem" and type = CH3 return true;
        if (type.equals("elem")) {
            IDataService dataService = springContext.getBean(IDataService.class);
            return dataService.getDataElement(Integer.valueOf(elemenID)).getType().equals("CH3");
        }

        return false;
    }

    private Vector getVocabularyFixedValues(String elementId) throws ServiceException {
        //get concepts from the dataservice
        //build FixedValue objects from them
        IDataService dataService = springContext.getBean(IDataService.class);

        List<VocabularyConcept> concepts = dataService.getElementVocabularyConcepts(Integer.valueOf(elementId));
        Vector<FixedValue> result = new Vector<FixedValue>();
        for (VocabularyConcept concept : concepts) {
            //simulate FXV id = concept.ID
            FixedValue fxv = new FixedValue(String.valueOf(concept.getId()), elementId, concept.getNotation());

            //TODO default?
            fxv.setDefinition(concept.getDefinition());
            fxv.setShortDesc(concept.getLabel());
            fxv.setCsID(concept.getIdentifier());

            result.add(fxv);
        }

        return result;
    }

    /**
     *
     * @param attr_id
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getSimpleAttributeValues(String attr_id) throws SQLException {

        if (attr_id == null) {
            return null;
        }

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer();
        buf.append("select distinct value from ATTRIBUTE where M_ATTRIBUTE_ID=");
        buf.append(inParams.add(attr_id, Types.INTEGER));
        buf.append(" order by VALUE");

        LOGGER.debug(buf.toString());

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            while (rs.next()) {

                String value = rs.getString("value");
                if (value == null) {
                    continue;
                }
                if (!value.equals("")) {
                    v.add(value);
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }

    /**
     *
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getNamespaces() throws SQLException {
        return getNamespaces(null);
    }

    /**
     *
     * @param id
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Namespace getNamespace(String id) throws SQLException {
        Vector v = getNamespaces(id);
        if (v != null && v.size() != 0) {
            return (Namespace) v.get(0);
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getNamespaces(String id) throws SQLException {

        if (id != null) {
            id = id.trim();
        }

        // this is for the case where id is given in the format "number url" (happens when someone clicks the schemaLocation given
        // in generated XML Schemas)
        if (id != null && id.length() > 0) {
            String[] ss = id.split("\\s+");
            if (ss.length == 2) {
                try {
                    Integer.parseInt(ss[0]);
                    if (ss[1].startsWith("http://") && ss[1].indexOf("GetSchema") > 6) {
                        id = ss[0];
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer("select * from NAMESPACE");
        if (id != null && id.length() != 0) {
            buf.append(" where NAMESPACE_ID=").append(inParams.add(id, Types.INTEGER));
        }
        buf.append(" order by SHORT_NAME");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Namespace namespace =
                        new Namespace(rs.getString("NAMESPACE_ID"), rs.getString("SHORT_NAME"), rs.getString("FULL_NAME"), null,
                                rs.getString("DEFINITION"));
                namespace.setWorkingUser(rs.getString("WORKING_USER"));
                v.add(namespace);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }

    /**
     *
     * @param datasetID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Dataset getDeletedDataset(String datasetID) throws SQLException {
        Vector v = getDatasets(datasetID, false, true);
        if (v == null || v.size() == 0) {
            return null;
        } else {
            return (Dataset) v.get(0);
        }
    }

    /**
     *
     * @param datasetID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Dataset getDataset(String datasetID) throws SQLException {

        Vector v = getDatasets(datasetID, false, false);
        if (v == null || v.size() == 0) {
            return null;
        } else {
            return (Dataset) v.get(0);
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets() throws SQLException {
        return getDatasets(null, false, false);
    }

    /**
     *
     * @param wrkCopies
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(boolean wrkCopies) throws SQLException {
        return getDatasets(null, wrkCopies, false);
    }

    /**
     *
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDeletedDatasets() throws SQLException {
        if (user == null || !user.isAuthentic()) {
            throw new SQLException("User not authorized");
        }
        return getDatasets(null, false, true);
    }

    /**
     *
     * @param datasetID
     * @param wrkCopies
     * @param deleted
     * @return
     * @throws SQLException
     *             if database query fails
     */
    private Vector getDatasets(String datasetID, boolean wrkCopies, boolean deleted) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer();
        buf.append("select distinct DATASET.* ");
        buf.append("from DATASET ");
        buf.append("where CORRESP_NS is not null");
        if (datasetID != null && datasetID.length() != 0) {
            buf.append(" and DATASET.DATASET_ID=").append(inParams.add(datasetID, Types.INTEGER));
        }

        // JH141003
        // if datasetID specified, ignore the DELETED flag, otherwise follow it
        if (!Util.isEmpty(datasetID)) {
            buf.append(" ");
        } else if (deleted && user != null) {
            buf.append(" and DATASET.DELETED=").append(inParams.add(user.getUserName())).append(" ");
        } else {
            buf.append(" and DATASET.DELETED is null ");
        }

        // prune out the working copies
        // (the business logic at edit view will lead the user eventually
        // to the working copy anyway)
        // But only in case if the ID is not exolicitly specified
        if (Util.isEmpty(datasetID)) {
            if (wrkCopies && (user == null || !user.isAuthentic())) {
                wrkCopies = false;
            }
            if (buf.length() != 0) {
                buf.append(" and ");
            }
            if (!wrkCopies) {
                buf.append("DATASET.WORKING_COPY='N'");
            } else {
                buf.append("DATASET.WORKING_COPY='Y' and DATASET.WORKING_USER=").append(inParams.add(user.getUserName()));
            }
        }
        buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");

        LOGGER.debug(buf.toString());

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();

        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            Dataset ds = null;
            while (rs.next()) {

                String idf = rs.getString("IDENTIFIER");

                // make sure we get the latest version of the dataset
                // JH101003 - unless were in 'restore' mode
                if (!deleted && ds != null && idf.equals(ds.getIdentifier())) {
                    continue;
                } 

                ds = new Dataset(rs.getString("DATASET_ID"), rs.getString("SHORT_NAME"), rs.getString("VERSION"));

                ds.setWorkingCopy(rs.getString("WORKING_COPY"));
                ds.setStatus(rs.getString("REG_STATUS"));
                ds.setVisual(rs.getString("VISUAL"));
                ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
                ds.setNamespaceID(rs.getString("CORRESP_NS"));
                ds.setDisplayCreateLinks(rs.getInt("DISP_CREATE_LINKS"));
                ds.setIdentifier(rs.getString("IDENTIFIER"));
                ds.setCheckedoutCopyID(rs.getString("CHECKEDOUT_COPY_ID"));
                ds.setWorkingUser(rs.getString("WORKING_USER"));
                ds.setDate(rs.getString("DATE"));
                ds.setUser(rs.getString("USER"));
                ds.setSuccessorId(rs.getString("SUCCESSOR"));

                ds.setDesirializedDisplayDownloadLinksFromSerializedMap(rs.getString("DISPLAY_DOWNLOAD_LINKS"));

                v.add(ds);
            }
        } catch (IOException e) {
            LOGGER.error("Error Desirializing Dataset DisplayDownloadLinks",e);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }

    /**
     *
     * @param params
     * @param shortName
     * @param version
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String version) throws SQLException {
        return getDatasets(params, shortName, version, null);
    }

    /**
     *
     * @param params
     * @param shortName
     * @param version
     * @param oper
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String version, String oper) throws SQLException {
        return getDatasets(params, shortName, version, oper, false);
    }

    /**
     *
     * @param params
     * @param shortName
     * @param version
     * @param oper
     * @param wrkCopies
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String version, String oper, boolean wrkCopies) throws SQLException {
        return getDatasets(params, shortName, null, version, oper, wrkCopies);
    }

    /**
     * get datasets by params, control oper & working copies.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String idfier, String version, String oper, boolean wrkCopies)
            throws SQLException {

        return getDatasets(params, shortName, idfier, version, oper, wrkCopies, null);
    }

    /**
     * get datasets by params, control oper & working copies.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String idfier, String version, String oper, boolean wrkCopies,
            HashSet statuses) throws SQLException {
        return getDatasets(params, shortName, idfier, version, oper, wrkCopies, false, statuses);
    }

    /**
     * Get datasets by params, control oper, working copies & historic versions.
     *
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasets(Vector params, String shortName, String idfier, String version, String oper, boolean wrkCopies,
            boolean isIncludeHistoricVersions, HashSet statuses) throws SQLException {

        // get the id of simple attribute "Name"
        Statement stmt1 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery("select M_ATTRIBUTE_ID from " + "M_ATTRIBUTE where SHORT_NAME='Name'");
        String nameID = rs.next() ? rs.getString(1) : null;
        rs.close();
        stmt1.close();

        INParameters inParams = new INParameters();

        // prepare different parts of the query for getting datasets
        if (oper == null) {
            oper = " like ";
        }

        StringBuffer tables = new StringBuffer("DATASET");
        StringBuffer constraints = new StringBuffer("DATASET.DELETED is null");

        // short name into constraints
        if (shortName != null && shortName.length() != 0) {
            constraints.append(" and DATASET.SHORT_NAME");
            // overwrite 'match' operator with 'like', because short name is not fulltext-indexed
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + shortName + "%"));
            } else {
                constraints.append(inParams.add(shortName));
            }
        }

        // identifier into constraints
        if (idfier != null && idfier.length() != 0) {
            constraints.append(" and DATASET.IDENTIFIER");
            // overwrite 'match' operator with 'like', because identifier is not fulltext-indexed
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like "; //
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + idfier + "%"));
            } else {
                constraints.append(inParams.add(idfier));
            }
        }

        // version into constraints
        if (version != null && version.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DATASET.VERSION=").append(inParams.add(version, Types.INTEGER));
        }

        // statuses into constraints
        if (statuses != null && statuses.size() > 0) {
            if (constraints.length() != 0) {
                constraints.append(" and (");
            }
            int i = 0;
            for (Iterator iter = statuses.iterator(); iter.hasNext(); i++) {
                if (i > 0) {
                    constraints.append(" or ");
                }
                constraints.append("REG_STATUS=");
                constraints.append(inParams.add(iter.next()));
            }
            constraints.append(")");
        }

        // params into constraints (if no params, we ask for all)
        for (int i = 0; params != null && i < params.size(); i++) {
            String index = String.valueOf(i + 1);
            DDSearchParameter param = (DDSearchParameter) params.get(i);
            String attrID = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();

            tables.append(", ATTRIBUTE as ATTR" + index);

            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + inParams.add(attrID, Types.INTEGER));
            constraints.append(" and ");

            if (attrValues != null && attrValues.size() != 0) {
                constraints.append("(");
                for (int j = 0; j < attrValues.size(); j++) {
                    if (j > 0) {
                        constraints.append(" or ");
                    }
                    if (valueOper != null && valueOper.trim().equalsIgnoreCase("MATCH")) {
                        constraints.append("match(ATTR" + index + ".VALUE) against(" + inParams.add(attrValues.get(j)) + ")");
                    } else {
                        constraints.append("ATTR" + index + ".VALUE" + valueOper + inParams.add(attrValues.get(j)));
                    }
                }
                constraints.append(")");
            }
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DATASET.DATASET_ID");
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='DS'");
        }

        // unless requested otherwise, prune out the working copies (the business logic in
        // UI will lead the user eventually to the working copy anyway)
        if (wrkCopies && (user == null || !user.isAuthentic())) {
            wrkCopies = false;
        }
        if (constraints.length() != 0) {
            constraints.append(" and ");
        }
        if (!wrkCopies) {
            constraints.append("DATASET.WORKING_COPY='N'");
        } else {
            constraints.append("DATASET.WORKING_COPY='Y' and DATASET.WORKING_USER=" + inParams.add(user.getUserName()));
        }

        // compile the query from the above-prepared parts
        StringBuffer buf = new StringBuffer("select DATASET.* from ");
        buf.append(tables.toString());
        if (constraints.length() != 0) {
            buf.append(" where ");
            buf.append(constraints.toString());
        }
        buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc");
        LOGGER.debug(buf.toString());

        // preprare the statement for getting attributes
        PreparedStatement ps = null;
        if (nameID != null) {
            String s =
                    "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" + nameID + " and PARENT_TYPE='DS' and " + "DATAELEM_ID=?";
            ps = conn.prepareStatement(s);
        }

        // execute the query for datasets
        PreparedStatement stmt = null;
        rs = null;
        ResultSet rs2 = null;
        Vector v = new Vector();
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            Dataset ds = null;
            while (rs.next()) {

                String regStatus = rs.getString("REG_STATUS");
                String idf = rs.getString("DATASET.IDENTIFIER");

                // if history not wanted, make sure we get the latest version of the dataset
                if (isIncludeHistoricVersions == false) {
                    if (ds != null && idf.equals(ds.getIdentifier())) {
                        continue;
                    }
                }

                ds = new Dataset(rs.getString("DATASET_ID"), rs.getString("SHORT_NAME"), rs.getString("VERSION"));

                ds.setWorkingCopy(rs.getString("WORKING_COPY"));
                ds.setVisual(rs.getString("VISUAL"));
                ds.setDetailedVisual(rs.getString("DETAILED_VISUAL"));
                ds.setNamespaceID(rs.getString("CORRESP_NS"));
                ds.setDisplayCreateLinks(rs.getInt("DISP_CREATE_LINKS"));
                ds.setDate(rs.getString("DATASET.DATE"));

                // set the name if nameID was previously successfully found
                if (nameID != null) {
                    ps.setInt(1, rs.getInt("DATASET.DATASET_ID"));
                    rs2 = ps.executeQuery();
                    if (rs2.next()) {
                        ds.setName(rs2.getString(1));
                    }
                }

                ds.setStatus(regStatus);
                ds.setIdentifier(rs.getString("IDENTIFIER"));
                ds.setCheckedoutCopyID(rs.getString("CHECKEDOUT_COPY_ID"));
                ds.setWorkingUser(rs.getString("WORKING_USER"));

                v.add(ds);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (SQLException sqle) {
            }
        }

        if (isIncludeHistoricVersions == false) {
            for (Iterator it = v.iterator(); it.hasNext();) {
                Dataset dataset = (Dataset) it.next();
                if (!dataset.getID().equals(getLatestDstID(dataset.getIdentifier(), null))) {
                    it.remove();
                }
                
            }
        }
        return v;
    }

    /**
     * Returns true if this.user should not see definition in the given status.
     *
     * @param regStatus
     * @return
     */
    public boolean skipByRegStatus(String regStatus) {

        if (regStatus != null) {
            if (user == null || !user.isAuthentic()) {
                if (regStatus.equals("Incomplete") || regStatus.equals("Candidate") || regStatus.equals("Qualified") || regStatus.equals("Retired") || regStatus.equals("Superseded")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @param dstID
     * @param isOrderByPositions
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector<DsTable> getDatasetTables(String dstID, boolean isOrderByPositions) throws SQLException {

        INParameters inParams = new INParameters();

        // prepare the query
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct DS_TABLE.*, DST2TBL.POSITION");
        buf.append(" from DS_TABLE ");
        buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
        buf.append("where DS_TABLE.CORRESP_NS is not null and ").append("DST2TBL.DATASET_ID=")
                .append(inParams.add(dstID, Types.INTEGER));

        if (isOrderByPositions) {
            buf.append(" order by DST2TBL.POSITION asc");
        } else {
            buf.append(" order by DS_TABLE.IDENTIFIER,DS_TABLE.TABLE_ID desc");
        }

        LOGGER.debug(buf.toString());

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector<DsTable> v = new Vector<DsTable>();

        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            DsTable dsTable = null;
            while (rs.next()) {

                // make sure you get the latest version
                String idf = rs.getString("DS_TABLE.IDENTIFIER");
                if (dsTable != null) {
                    if (idf.equals(dsTable.getIdentifier())) {
                        continue;
                    }
                }

                dsTable = new DsTable(rs.getString("DS_TABLE.TABLE_ID"), dstID, rs.getString("DS_TABLE.SHORT_NAME"));

                dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
                dsTable.setWorkingUser(rs.getString("DS_TABLE.WORKING_USER"));
                dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));
                dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                dsTable.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
                dsTable.setPositionInDataset(rs.getInt("DST2TBL.POSITION"));

                v.add(dsTable);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;
    }

    /**
     *
     * @param params
     * @param shortName
     * @param fullName
     * @param definition
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasetTables(Vector params, String shortName, String fullName, String definition) throws SQLException {
        return getDatasetTables(params, shortName, fullName, definition, null);
    }

    /**
     *
     * @param params
     * @param shortName
     * @param fullName
     * @param definition
     * @param oper
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasetTables(Vector params, String shortName, String fullName, String definition, String oper)
            throws SQLException {

        return getDatasetTables(params, shortName, null, fullName, definition, oper);
    }

    /**
     *
     * @param params
     * @param shortName
     * @param idfier
     * @param fullName
     * @param definition
     * @param oper
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector
            getDatasetTables(Vector params, String shortName, String idfier, String fullName, String definition, String oper)
                    throws SQLException {

        return getDatasetTables(params, shortName, idfier, fullName, definition, oper, null, true);
    }

    /**
     *
     * @param params
     * @param shortName
     * @param idfier
     * @param fullName
     * @param definition
     * @param oper
     * @param dstStatuses
     * @param latestOnly
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDatasetTables(Vector params, String shortName, String idfier, String fullName, String definition,
            String oper, HashSet dstStatuses, boolean latestOnly) throws SQLException {

        INParameters inParams = new INParameters();

        // get the id of simple attribute "Name"
        Statement stmt1 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery("select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Name'");
        String nameID = rs.next() ? rs.getString(1) : null;
        rs.close();
        stmt1.close();

        // prepare different parts of the query for tables
        if (oper == null) {
            oper = " like ";
        }

        StringBuffer tables = new StringBuffer("DS_TABLE, DST2TBL, DATASET");
        StringBuffer constraints =
                new StringBuffer().append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID and DST2TBL.DATASET_ID=DATASET.DATASET_ID").append(
                        " and DATASET.DELETED is null and DATASET.WORKING_COPY='N'");

        // dataset statuses into constraints
        if (dstStatuses != null && dstStatuses.size() > 0) {
            if (constraints.length() > 0) {
                constraints.append(" and ");
            }
            constraints.append("(");
            int i = 0;
            for (Iterator iter = dstStatuses.iterator(); iter.hasNext(); i++) {
                if (i > 0) {
                    constraints.append(" or ");
                }
                constraints.append("DATASET.REG_STATUS=").append(inParams.add(iter.next()));
            }
            constraints.append(")");
        }

        // short name into constraints
        if (shortName != null && shortName.length() != 0) {
            constraints.append(" and DS_TABLE.SHORT_NAME");
            // overwrite 'match' operator with 'like', because short name is not fulltext-indexed
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + shortName + "%"));
            } else {
                constraints.append(inParams.add(shortName));
            }
        }

        // identifier into constraints
        if (idfier != null && idfier.length() != 0) {
            constraints.append(" and DS_TABLE.IDENTIFIER");
            // overwrite 'match' operator with 'like', because identifier is not fulltext-indexed
            if (oper.trim().equalsIgnoreCase("match")) {
                oper = " like ";
            }
            constraints.append(oper);
            if (oper.trim().equalsIgnoreCase("like")) {
                constraints.append(inParams.add("%" + idfier + "%"));
            } else {
                constraints.append(inParams.add(idfier));
            }
        }

        // full name into constraints
        if (fullName != null && fullName.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DS_TABLE.NAME like ").append(inParams.add("%" + fullName + "%"));
        }
        // definition into constraints
        if (definition != null && definition.length() != 0) {
            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("DS_TABLE.DEFINITION like ").append(inParams.add("%" + definition + "%"));
        }

        // params into constraints (if params == null, we ask for all)
        for (int i = 0; params != null && i < params.size(); i++) {

            String index = String.valueOf(i + 1);
            DDSearchParameter param = (DDSearchParameter) params.get(i);
            String attrID = param.getAttrID();
            Vector attrValues = param.getAttrValues();
            String valueOper = param.getValueOper();
            String idOper = param.getIdOper();
            String attrName = param.getAttrShortName();

            tables.append(", ATTRIBUTE as ATTR" + index);

            if (constraints.length() != 0) {
                constraints.append(" and ");
            }
            constraints.append("ATTR" + index + ".M_ATTRIBUTE_ID" + idOper + inParams.add(attrID, Types.INTEGER));
            constraints.append(" and ");

            if (attrValues != null && attrValues.size() != 0) {
                constraints.append("(");
                for (int j = 0; j < attrValues.size(); j++) {
                    if (j > 0) {
                        constraints.append(" or ");
                    }
                    if (valueOper != null && valueOper.trim().equalsIgnoreCase("MATCH")) {
                        constraints.append("match(ATTR" + index + ".VALUE) against(" + inParams.add(attrValues.get(j)) + ")");
                    } else {
                        constraints.append("ATTR" + index + ".VALUE" + valueOper + inParams.add(attrValues.get(j)));
                    }
                }
                constraints.append(")");
            }
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".DATAELEM_ID=DS_TABLE.TABLE_ID");
            constraints.append(" and ");
            constraints.append("ATTR" + index + ".PARENT_TYPE='T'");
        }

        // compile the query from above-prepared parts
        StringBuffer buf = new StringBuffer("select distinct DS_TABLE.*, DATASET.* from ");
        buf.append(tables.toString());
        if (constraints.length() != 0) {
            buf.append(" where ");
            buf.append(constraints.toString());
        }
        buf.append(" order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, "
                + "DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");

        // preprare the statement for getting attributes
        PreparedStatement attrsPstmt = null;
        if (nameID != null) {
            String s =
                    "select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID=" + nameID + " and PARENT_TYPE='T' and " + "DATAELEM_ID=?";
            attrsPstmt = conn.prepareStatement(s);
        }

        // execute the query for tables
        PreparedStatement stmt = null;
        ResultSet rs2 = null;
        Vector v = new Vector();
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            String curDstID = null;
            String curDstIdf = null;
            while (rs.next()) {

                String dstID = rs.getString("DATASET.DATASET_ID");
                String dstIdf = rs.getString("DATASET.IDENTIFIER");
                if (dstID == null && dstIdf == null) {
                    continue;
                }

                if (latestOnly) {
                    // the following if-else block skips tables in non-latest DATASETS
                    if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                        curDstIdf = dstIdf;
                        curDstID = dstID;
                    } else if (!dstID.equals(curDstID)) {
                        LOGGER.debug("Skipping: " + rs.getString("DS_TABLE.TABLE_ID") + "; " + rs.getString("DATASET.SHORT_NAME"));
                        continue;
                    }
                }

                // skip tables that do not actually exist (ie trash from some erroneous situation)
                String tblIdf = rs.getString("DS_TABLE.IDENTIFIER");
                if (tblIdf == null) {
                    continue;
                }

                // skip this dataset if this.user should not see a dataset in given status
                String dstStatus = rs.getString("DATASET.REG_STATUS");

                // construct the table object
                DsTable tbl =
                        new DsTable(rs.getString("DS_TABLE.TABLE_ID"), rs.getString("DATASET.DATASET_ID"),
                                rs.getString("DS_TABLE.SHORT_NAME"));
                tbl.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                tbl.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                tbl.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
                tbl.setDstIdentifier(dstIdf);
                tbl.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
                tbl.setDstStatus(rs.getString("DATASET.REG_STATUS"));
                tbl.setDstWorkingUser(rs.getString("DATASET.WORKING_USER"));
                tbl.setDstDate(rs.getString("DATASET.DATE"));

                // set the name if nameID was previously successfully found
                if (nameID != null) {
                    attrsPstmt.setInt(1, rs.getInt("DS_TABLE.TABLE_ID"));
                    rs2 = attrsPstmt.executeQuery();
                    if (rs2.next()) {
                        tbl.setName(rs2.getString(1));
                    }
                }
                // set comparation string
                tbl.setCompStr(tbl.getName());
                // add this table object into result set
                v.add(tbl);
            }
        } finally {
            try {
                if (rs2 != null) {
                    rs2.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        Collections.sort(v);
        return v;
    }

    /**
     *
     * @param tableID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public DsTable getDatasetTable(String tableID) throws SQLException {
        return getDatasetTable(tableID, null);
    }

    /**
     *
     * @param tableID
     * @param dstID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public DsTable getDatasetTable(String tableID, String dstID) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer buf = new StringBuffer();
        buf.append("select distinct DS_TABLE.*, DATASET.* ");
        buf.append("from DS_TABLE ");

        // JH140803
        // there's now a many-to-many relation btw DS_TABLE & DATASET
        buf.append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
        buf.append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ");
        buf.append("where DS_TABLE.CORRESP_NS is not null and DS_TABLE.TABLE_ID=");
        buf.append(inParams.add(tableID, Types.INTEGER));

        if (Util.isEmpty(dstID)) {
            buf.append(" and DATASET.DELETED is null");
        } else {
            buf.append(" and DATASET.DATASET_ID=").append(inParams.add(dstID, Types.INTEGER));
        }

        buf.append(" order by DATASET.DATASET_ID desc");

        PreparedStatement stmt = null;
        ResultSet rs = null;
        DsTable dsTable = null;

        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            if (rs.next()) {

                dsTable = new DsTable(rs.getString("DS_TABLE.TABLE_ID"),
                // rs.getString("DATASET_ID"),
                        rs.getString("DATASET.DATASET_ID"), rs.getString("DS_TABLE.SHORT_NAME"));

                dsTable.setWorkingCopy(rs.getString("DS_TABLE.WORKING_COPY"));
                dsTable.setVersion(rs.getString("DS_TABLE.VERSION"));

                dsTable.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                dsTable.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));

                dsTable.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
                dsTable.setDstIdentifier(rs.getString("DATASET.IDENTIFIER"));
                dsTable.setDstStatus(rs.getString("DATASET.REG_STATUS"));
                dsTable.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return dsTable;
    }

    /**
     *
     * @param parentID
     * @param parentType
     * @param attrType
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getAttributes(String parentID, String parentType) throws SQLException {
        return getAttributes(parentID, parentType, null, null);
    }

    /**
     *
     * @param parentID
     * @param parentType
     * @param inheritTblID
     * @param inheritDstID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getAttributes(String parentID, String parentType, String inheritTblID, String inheritDstID)
            throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer qry = new StringBuffer("select M_ATTRIBUTE.*, NAMESPACE.*, ATTRIBUTE.VALUE, ATTRIBUTE.PARENT_TYPE from ");
        qry.append("M_ATTRIBUTE left outer join NAMESPACE on ");
        qry.append("M_ATTRIBUTE.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID ");
        qry.append("left outer join ATTRIBUTE on M_ATTRIBUTE.M_ATTRIBUTE_ID=ATTRIBUTE.M_ATTRIBUTE_ID ");
        qry.append("where (ATTRIBUTE.DATAELEM_ID=");
        qry.append(inParams.add(parentID, Types.INTEGER));
        qry.append(" and ATTRIBUTE.PARENT_TYPE=").append(inParams.add(parentType)).append(")");

        // Ek 291003 search inhrted attributes from table and/or dataset level
        if (!Util.isEmpty(inheritTblID)) {
            qry.append(" or (ATTRIBUTE.DATAELEM_ID=");
            qry.append(inParams.add(inheritTblID, Types.INTEGER));
            qry.append(" and ATTRIBUTE.PARENT_TYPE='T' and M_ATTRIBUTE.INHERIT!='0')");
        }
        if (!Util.isEmpty(inheritDstID)) {
            qry.append(" or (ATTRIBUTE.DATAELEM_ID=");
            qry.append(inParams.add(inheritDstID, Types.INTEGER));
            qry.append(" and ATTRIBUTE.PARENT_TYPE='DS' and M_ATTRIBUTE.INHERIT!='0')");
        }

        LOGGER.debug(qry.toString());

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        try {
            stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String parent_type = rs.getString("ATTRIBUTE.PARENT_TYPE");
                String value = rs.getString("ATTRIBUTE.VALUE");
                String inherited = parent_type.equals(parentType) ? null : parent_type;

                DElemAttribute attr = getAttributeById(v, rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
                if (attr == null) {
                    attr =
                            new DElemAttribute(rs.getString("M_ATTRIBUTE.M_ATTRIBUTE_ID"), rs.getString("M_ATTRIBUTE.NAME"),
                                    rs.getString("M_ATTRIBUTE.SHORT_NAME"), null, 
                                    rs.getString("M_ATTRIBUTE.DEFINITION"), rs.getString("M_ATTRIBUTE.OBLIGATION"),
                                    rs.getString("M_ATTRIBUTE.DISP_MULTIPLE"));
                    attr.setInheritable(rs.getString("INHERIT"));
                    attr.setDisplayType(rs.getString("M_ATTRIBUTE.DISP_TYPE"));

                    // value will be set afterwards
                    Namespace ns =
                            new Namespace(rs.getString("NAMESPACE.NAMESPACE_ID"), rs.getString("NAMESPACE.SHORT_NAME"),
                                    rs.getString("NAMESPACE.FULL_NAME"), null, rs.getString("NAMESPACE.DEFINITION"));
                    attr.setNamespace(ns);

                    v.add(attr);
                }

                if (inherited != null) {
                    if (attr.getInheritable().equals("1")) { // inheritance type 1 - show all values from upper levels
                        attr.setValue(value);
                        attr.setInheritedValue(value);
                        attr.setInheritedLevel(inherited);
                    } else { // inheritance type 2 - show values from upper levels or if current level has values then onlycurrent
                        // level
                        if (attr.getInheritedLevel() == null) {
                            attr.setInheritedValue(value);
                            attr.setInheritedLevel(inherited);
                        } else {
                            if (attr.getInheritedLevel().equals("DS") && inherited.equals("T")) {
                                attr.clearInherited();
                            }
                            if (attr.getInheritedLevel().equals(inherited) || inherited.equals("T")) {
                                attr.setInheritedValue(value);
                                attr.setInheritedLevel(inherited);
                            }
                        }
                    }
                } else { // get values from original level
                    attr.setValue(value);
                    attr.setOriginalValue(value);
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return v;

    }

    /**
     *
     * @param fxvId
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public FixedValue getFixedValue(String fxvId) throws SQLException {

        if (fxvId == null || fxvId.length() == 0) {
            FixedValue fxv = new FixedValue();
            Vector attributes = getDElemAttributes();
            for (int i = 0; i < attributes.size(); i++) {
                fxv.addAttribute(attributes.get(i));
            }
            return fxv;
        }

        INParameters inParams = new INParameters();
        String qry = "select * from FXV where FXV_ID=" + inParams.add(fxvId, Types.INTEGER);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        FixedValue fxv = null;
        try {
            stmt = SQL.preparedStatement(qry, inParams, conn);
            rs = stmt.executeQuery();

            if (rs.next()) {
                fxv = new FixedValue(rs.getString("FXV_ID"), rs.getString("OWNER_ID"), rs.getString("VALUE"));

                String isDefault = rs.getString("IS_DEFAULT");
                if (isDefault != null && isDefault.equalsIgnoreCase("Y")) {
                    fxv.setDefault();
                }

                fxv.setDefinition(rs.getString("DEFINITION"));
                fxv.setShortDesc(rs.getString("SHORT_DESC"));
            } else {
                return null;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return fxv;
    }

    /**
     *
     * @param v
     * @param id
     * @return
     */
    private DElemAttribute getAttributeById(Vector v, String id) {

        if (v == null) {
            return null;
        }
        if (id == null || id.length() == 0) {
            return null;
        }

        for (int i = 0; i < v.size(); i++) {
            DElemAttribute attribute = (DElemAttribute) v.get(i);
            if (attribute.getID().equalsIgnoreCase(id)) {
                return attribute;
            }
        }

        return null;
    }

    /**
     * Get the last insert ID from database.
     *
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";
        String id = null;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        rs.clearWarnings();
        if (rs.next()) {
            id = rs.getString(1);
        }

        stmt.close();
        return id;
    }

    /**
     *
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDstOtherVersions(String idfier, String ofID) throws SQLException {

        Vector v = getDatasets(null, null, idfier, null, "=", false, true, null);
        for (int i = 0; v != null && i < v.size(); i++) {
            if (((Dataset) v.get(i)).getID().equals(ofID)) {
                v.remove(i);
                break;
            }
        }
        return v;
    }

    /**
     *
     * @param idfier
     * @param ofID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getElmOtherVersions(String idfier, String ofID) throws SQLException {

        Vector v = getCommonElements(null, null, null, idfier, false, true, "=");
        for (int i = 0; v != null && i < v.size(); i++) {
            if (((DataElement) v.get(i)).getID().equals(ofID)) {
                v.remove(i);
                break;
            }
        }
        return v;
    }

    /**
     *
     * @param id
     * @param type
     * @return
     * @throws Exception
     */
    public boolean isWorkingCopy(String id, String type) throws Exception {

        if (type == null) {
            throw new Exception("Type not specified!");
        }

        String tblName = "";
        if (type.equals("elm")) {
            tblName = "DATAELEM";
        } else if (type.equals("tbl")) {
            tblName = "DS_TABLE";
        } else if (type.equals("dst")) {
            tblName = "DATASET";
        } else {
            throw new Exception("Unknown type!");
        }

        String idField = type.equals("tbl") ? "TABLE_ID" : tblName + "_ID";

        INParameters inParams = new INParameters();
        String q = "select WORKING_COPY from " + tblName + " where " + idField + "=" + inParams.add(id, Types.INTEGER);

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(q, inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals("Y")) {
                    return true;
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return false;
    }

    /**
     *
     * @return
     */
    public boolean hasUserWorkingCopies() {

        if (user == null || !user.isAuthentic()) {
            return false;
        }

        INParameters inParams = new INParameters();
        StringBuffer constraints = new StringBuffer();
        constraints.append("WHERE WORKING_COPY='Y' and WORKING_USER=").append(inParams.add(user.getUserName()));

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement("SELECT count(*) FROM DATAELEM " + constraints.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }

            rs.close();
            stmt.close();

            stmt = SQL.preparedStatement("SELECT count(*) FROM DATASET " + constraints.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }

            String sql = "select count(*) from T_SCHEMA_SET where WORKING_COPY=true and WORKING_USER=?";
            stmt = SQL.preparedStatement(sql, inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }

            sql = "select count(*) from VOCABULARY where WORKING_COPY=true and WORKING_USER=?";
            stmt = SQL.preparedStatement(sql, inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }

            // sql = "select count(*) from T_SCHEMA where WORKING_COPY=true and WORKING_USER=?";
            // stmt = SQL.preparedStatement(sql, inParams, conn);
            // rs = stmt.executeQuery();
            // if (rs.next() && rs.getInt(1)>0) {
            // return true;
            // }
        } catch (SQLException sqle) {
            LOGGER.error(sqle.toString(), sqle);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return false;
    }

    public int getAttributeUseCount(String attrID) throws SQLException {
        INParameters inParams = new INParameters();

        StringBuffer sql = new StringBuffer();
        sql.append("select count(distinct PARENT_TYPE, DATAELEM_ID) from ATTRIBUTE where M_ATTRIBUTE_ID=").
                append(inParams.add(attrID, Types.INTEGER));

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(sql.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return 0;
    }

    /**
     * Return true if the given common element has newer Released version. Otherwise returns false.
     *
     * Note that this method does not check whether the given element is really a common one, so it's on caller's responsibility!
     *
     * @param elm
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public boolean hasNewerReleases(DataElement elm) throws SQLException {

        if (elm == null) {
            return false;
        }

        INParameters inParams = new INParameters();
        String sql =
                "select DATAELEM_ID from DATAELEM where PARENT_NS is null and " + "IDENTIFIER="
                        + inParams.add(elm.getIdentifier()) + " and REG_STATUS='Released' and WORKING_COPY='N' and "
                        + "CHECKEDOUT_COPY_ID is null and DATAELEM_ID > " + inParams.add(elm.getID(), Types.INTEGER)
                        + " order by DATAELEM_ID desc limit 1";

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(sql, inParams, conn);
            rs = stmt.executeQuery();
            return rs != null && rs.next();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     *
     * @param elmIdentifier
     * @param elmId
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getNewerReleases(String elmIdentifier, String elmId) throws SQLException {

        Vector result = new Vector();
        if (!Util.isEmpty(elmIdentifier) && !Util.isEmpty(elmId)) {
            Vector v = getCommonElements(null, null, null, elmIdentifier, "Released", false, true, "=");
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    DataElement elm = (DataElement) v.get(i);
                    String id = elm.getID();
                    if (!id.equals(elmId)) {
                        result.add(elm);
                    }
                }
            }
        }

        Collections.sort(result, new DataElementComparator(DataElementComparator.ID, DataElementComparator.DESC));
        return result;
    }

    /**
     *
     * @param relID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Hashtable getFKRelation(String relID) throws SQLException {

        if (Util.isEmpty(relID)) {
            return null;
        }

        INParameters inParams = new INParameters();

        StringBuffer buf =
                new StringBuffer("select ").append("A_ELM.SHORT_NAME as A_NAME, A_TBL.SHORT_NAME as A_TABLE, ")
                        .append("B_ELM.SHORT_NAME as B_NAME, B_TBL.SHORT_NAME as B_TABLE, ").append("FK_RELATION.* ")
                        .append("from FK_RELATION ").append("left outer join DATAELEM as A_ELM ")
                        .append("on FK_RELATION.A_ID=A_ELM.DATAELEM_ID ").append("left outer join DATAELEM as B_ELM on ")
                        .append("FK_RELATION.B_ID=B_ELM.DATAELEM_ID ").append("left outer join DS_TABLE as A_TBL ")
                        .append("on A_ELM.PARENT_NS=A_TBL.CORRESP_NS ").append("left outer join DS_TABLE as B_TBL ")
                        .append("on B_ELM.PARENT_NS=B_TBL.CORRESP_NS ").append("where REL_ID=")
                        .append(inParams.add(relID, Types.INTEGER));

        LOGGER.debug(buf.toString());

        Hashtable hash = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {

                hash = new Hashtable();
                hash.put("a_id", rs.getString("A_ID"));
                hash.put("a_name", rs.getString("A_NAME"));
                hash.put("a_tbl", rs.getString("A_TABLE"));
                hash.put("a_cardin", rs.getString("A_CARDIN"));

                hash.put("b_id", rs.getString("B_ID"));
                hash.put("b_name", rs.getString("B_NAME"));
                hash.put("b_tbl", rs.getString("B_TABLE"));
                hash.put("b_cardin", rs.getString("B_CARDIN"));
                hash.put("cardin", rs.getString("A_CARDIN") + " to " + rs.getString("B_CARDIN"));

                hash.put("definition", rs.getString("FK_RELATION.DEFINITION"));
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return hash;
    }

    /**
     *
     * @param elmID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getFKRelationsElm(String elmID) throws SQLException {
        return getFKRelationsElm(elmID, null);
    }

    /**
     *
     * @param elmID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getFKRelationsElm(String elmID, String dstID) throws SQLException {

        Vector v = new Vector();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            for (int i = 1; i <= 2; i++) {

                String side = i == 1 ? "A_ID" : "B_ID";
                String contraSide = i == 1 ? "B_ID" : "A_ID";

                INParameters inParams = new INParameters();
                StringBuffer buf = new StringBuffer("select distinct ");
                buf.append("DATAELEM.SHORT_NAME, ");
                buf.append("DATAELEM.DATAELEM_ID, ");
                buf.append("DS_TABLE.SHORT_NAME, ");
                buf.append("DST2TBL.DATASET_ID, ");
                buf.append("FK_RELATION.* ");
                buf.append("from FK_RELATION ");
                buf.append("left outer join DATAELEM on FK_RELATION.");
                buf.append(contraSide);
                buf.append("=DATAELEM.DATAELEM_ID ");
                buf.append("left outer join TBL2ELEM on ");
                buf.append("DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ");
                buf.append("left outer join DS_TABLE on ");
                buf.append("TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ");
                buf.append("left outer join DST2TBL on ");
                buf.append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
                if (!Util.isEmpty(elmID)) {
                    buf.append(" where ");
                    buf.append(side);
                    buf.append("=");
                    buf.append(inParams.add(elmID, Types.INTEGER));
                }

                // close rs and stmt if they were opened in the loop's previous step
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                }

                HashSet added = new HashSet();
                stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                rs = stmt.executeQuery();
                while (rs.next()) {

                    if (dstID != null) {
                        String ds = rs.getString("DST2TBL.DATASET_ID");
                        if (!dstID.equals(ds)) {
                            continue;
                        }
                    }

                    Hashtable hash = new Hashtable();
                    String id = rs.getString("DATAELEM.DATAELEM_ID");
                    if (id == null) {
                        continue;
                    }
                    if (added.contains(id)) {
                        continue;
                    } else {
                        added.add(id);
                    }
                    hash.put("elm_id", id);
                    hash.put("elm_name", rs.getString("DATAELEM.SHORT_NAME"));
                    hash.put("tbl_name", rs.getString("DS_TABLE.SHORT_NAME"));
                    hash.put("rel_id", rs.getString("REL_ID"));

                    hash.put("a_cardin", rs.getString("A_CARDIN"));
                    hash.put("b_cardin", rs.getString("B_CARDIN"));
                    hash.put("definition", rs.getString("DEFINITION") == null ? "" : rs.getString("DEFINITION"));
                    hash.put("cardin", rs.getString("A_CARDIN") + " to " + rs.getString("B_CARDIN"));

                    v.add(hash);
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return v;
    }

    /**
     *
     * @param ownerID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDocs(String ownerID) throws SQLException {
        return getDocs(ownerID, "dst");
    }

    /**
     *
     * @param ownerID
     * @param ownerType
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getDocs(String ownerID, String ownerType) throws SQLException {

        INParameters inParams = new INParameters();

        StringBuffer buf =
                new StringBuffer("select * from DOC where ").append("OWNER_ID=").append(inParams.add(ownerID, Types.INTEGER))
                        .append(" and OWNER_TYPE=").append(inParams.add(ownerType)).append(" order by TITLE asc");

        Vector v = new Vector();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String file = rs.getString("ABS_PATH");
                Hashtable hash = new Hashtable();
                hash.put("md5", rs.getString("MD5_PATH"));
                hash.put("file", file);
                hash.put("icon", eionet.util.Util.getIcon(file));
                hash.put("title", rs.getString("TITLE"));
                v.add(hash);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return v;
    }

    /**
     *
     * @param shortName
     * @return
     */
    public String getAttrHelpByShortName(String shortName) {
        if (shortName == null) {
            return "";
        }
        return getSimpleAttrHelpByShortName(shortName);
    }

    /**
     *
     * @param attrID
     *            - attribute ID
     * @return
     */
    public String getAttrHelp(String attrID) {
        if (attrID == null) {
            return "";
        }
        return getSimpleAttrHelp(attrID);
    }

    /**
     *
     * @param attrID
     *            - attribute ID
     * @return
     */
    public String getSimpleAttrHelp(String attrID) {
        return getSimpleAttrHelp("M_ATTRIBUTE_ID", attrID);
    }

    /**
     *
     * @param shortName
     * @return
     */
    public String getSimpleAttrHelpByShortName(String shortName) {
        return getSimpleAttrHelp("SHORT_NAME", SQL.toLiteral(shortName));
    }

    /**
     *
     * @param field
     * @param value
     * @return
     */
    public String getSimpleAttrHelp(String field, String value) {

        StringBuffer retBuf = new StringBuffer();
        INParameters inParams = new INParameters();

        StringBuffer qryBuf = new StringBuffer("select * from M_ATTRIBUTE where ");
        qryBuf.append(field).append("=").append(inParams.add(value, Types.VARCHAR));

        if (field != null && value != null) {

            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = SQL.preparedStatement(qryBuf.toString(), inParams, conn);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    retBuf.append("<br/><b>").append(rs.getString("NAME")).append("</b><br/><br/>")
                            .append(rs.getString("DEFINITION"));
                }
            } catch (SQLException e) {
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                }
            }
        }

        return retBuf.toString();
    }

    /**
     *
     * @param objID
     * @param objType
     * @param article
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getCacheFileName(String objID, String objType, String article) throws SQLException {

        if (objID == null || objType == null || article == null) {
            throw new SQLException("getCacheFileName(): objID or objType or article is null");
        }

        INParameters inParams = new INParameters();
        StringBuffer buf =
                new StringBuffer("select FILENAME from CACHE where ").append("OBJ_ID=").append(inParams.add(objID, Types.INTEGER))
                        .append(" and OBJ_TYPE=").append(inParams.add(objType)).append(" and ARTICLE=")
                        .append(inParams.add(article));

        String fileName = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            fileName = rs.next() ? rs.getString(1) : null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return fileName;
    }

    /**
     *
     * @param objID
     * @param objType
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Hashtable getCache(String objID, String objType) throws SQLException {

        if (objID == null || objType == null) {
            throw new SQLException("getCache(): objID or objType or article is null");
        }

        INParameters inParams = new INParameters();
        StringBuffer buf =
                new StringBuffer("select * from CACHE where ").append("OBJ_ID=").append(inParams.add(objID, Types.INTEGER))
                        .append(" and OBJ_TYPE=").append(inParams.add(objType));

        Hashtable result = new Hashtable();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String filename = rs.getString("FILENAME");
                String article = rs.getString("ARTICLE");
                Long created = new Long(rs.getLong("CREATED"));
                if (Util.isEmpty(filename)) {
                    continue;
                }

                Hashtable hash = new Hashtable();
                hash.put("filename", filename);
                hash.put("created", created);

                result.put(rs.getString("ARTICLE"), hash);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }

        return result;
    }

    /**
     *
     * @param elmID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public Vector getReferringTables(String elmID) throws SQLException {

        // JH110705 - first get the owners of datasets (we need to display them)
        StringBuffer qry = new StringBuffer("select * from ACLS where PARENT_NAME='/datasets'");

        Hashtable owners = new Hashtable();
        Statement stmt1 = null;
        ResultSet rs = null;
        try {
            stmt1 = conn.createStatement();
            rs = stmt1.executeQuery(qry.toString());
            while (rs.next()) {
                String idf = rs.getString("ACL_NAME");
                String own = rs.getString("OWNER");
                if (idf != null && own != null) {
                    owners.put(idf, own);
                }
            }
        } finally {
            try {
                if (stmt1 != null) {
                    stmt1.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }

        INParameters inParams = new INParameters();

        // and now get the referring tables
        qry =
                new StringBuffer("select DS_TABLE.*, ")
                        .append("DATASET.DATASET_ID,DATASET.IDENTIFIER,DATASET.SHORT_NAME,DATASET.REG_STATUS ")
                        .append("from TBL2ELEM ").append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID ")
                        .append("left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ")
                        .append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID ")
                        .append("where TBL2ELEM.DATAELEM_ID=").append(inParams.add(elmID, Types.INTEGER)).append(" and ")
                        .append("DS_TABLE.TABLE_ID is not null and ").append("DATASET.DELETED is null ")
                        .append("order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc, ")
                        .append("DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");

        Vector result = new Vector();
        PreparedStatement stmt = null;
        rs = null;
        try {
            stmt = SQL.preparedStatement(qry.toString(), inParams, conn);
            rs = stmt.executeQuery();

            String curDstID = null;
            String curDstIdf = null;
            String curTblIdf = null;
            while (rs.next()) {

                String dstID = rs.getString("DATASET.DATASET_ID");
                String dstIdf = rs.getString("DATASET.IDENTIFIER");
                if (dstID == null && dstIdf == null) {
                    continue;
                }

                // the following if block skips tables in non-latest DATASETS
                if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                    curDstIdf = dstIdf;
                    curDstID = dstID;
                } else if (!dstID.equals(curDstID)) {
                    continue;
                }

                String tblIdf = rs.getString("DS_TABLE.IDENTIFIER");
                if (tblIdf == null) {
                    continue;
                }

                // the following if block skips non-latest TABLES
                if (curTblIdf != null && tblIdf.equals(curTblIdf)) {
                    continue;
                } else {
                    curTblIdf = tblIdf;
                }

                // see if the table should be skipped by DATASET.REG_STATUS
                String dstStatus = rs.getString("DATASET.REG_STATUS");
                if (skipByRegStatus(dstStatus)) {
                    continue;
                }

                // start constructing the table
                DsTable tbl =
                        new DsTable(rs.getString("DS_TABLE.TABLE_ID"), rs.getString("DATASET.DATASET_ID"),
                                rs.getString("DS_TABLE.SHORT_NAME"));

                tbl.setNamespace(rs.getString("DS_TABLE.CORRESP_NS"));
                tbl.setParentNs(rs.getString("DS_TABLE.PARENT_NS"));
                tbl.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));

                tbl.setDatasetName(rs.getString("DATASET.SHORT_NAME"));
                tbl.setDstIdentifier(dstIdf);
                if (dstIdf != null) {
                    tbl.setOwner((String) owners.get(dstIdf));
                }

                tbl.setCompStr(tbl.getShortName());
                result.add(tbl);
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (SQLException e) {
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     *
     * @param dstID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getDatasetIdentifierById(String dstID) throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("select IDENTIFIER from DATASET where DATASET_ID=");
        buf.append(inParams.add(dstID, Types.INTEGER));

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs != null && rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     *
     * @param namespaceId
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getDatasetIdentifierByNamespace(String namespaceId) throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("select IDENTIFIER from DATASET where CORRESP_NS=");
        buf.append(inParams.add(namespaceId, Types.INTEGER));

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            return rs != null && rs.next() ? rs.getString(1) : null;
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param tableNamespaceID
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String[] getDataElementParentIdentifiers(String tableNamespaceID) throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf =
                new StringBuffer().append("select DATASET.IDENTIFIER as DST, DS_TABLE.IDENTIFIER as TBL from DS_TABLE, DATASET ")
                        .append("where DS_TABLE.PARENT_NS=DATASET.CORRESP_NS and DS_TABLE.CORRESP_NS=? limit 1");
        inParams.add(Integer.valueOf(tableNamespaceID), Types.INTEGER);

        String[] result = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String dstIdentifier = rs.getString(1);
                String tblIdentifier = rs.getString(2);
                if (!Util.isEmpty(dstIdentifier) && !Util.isEmpty(dstIdentifier)) {
                    result = new String[2];
                    result[0] = dstIdentifier;
                    result[1] = tblIdentifier;
                }
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }

        return result;
    }

    /**
     *
     * @param elmIdfier
     * @return
     * @throws SQLException
     *             if database query fails
     */
    public String getElmOwner(String elmIdfier) throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("select OWNER from ACLS where PARENT_NAME='/elements' ");
        buf.append(" and ACL_NAME=").append(inParams.add(elmIdfier));

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            if (rs != null && rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public static DDSearchEngine create() throws DAOException {
        try {
            return new DDSearchEngine(ConnectionUtil.getConnection());
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    public void close() {
        if (this.conn != null) {
            SQL.close(conn);
        }
    }

    /**
     *
     * @param searchEngine
     */
    public static void close(DDSearchEngine searchEngine) {
        if (searchEngine != null) {
            searchEngine.close();
        }
    }

    /**
     *
     * @param objectId
     * @param objectType
     * @return
     * @throws DAOException
     */
    public LinkedHashMap<Integer, DElemAttribute> getObjectAttributes(int objectId, DElemAttribute.ParentType objectType) throws DAOException {

        LinkedHashMap<Integer, DElemAttribute> resultMap = new LinkedHashMap<Integer, DElemAttribute>();

        try {

            // First get the metadata of all possible attributes for this object type
            List<DElemAttribute> attributes = getDElemAttributes(null, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
            if (attributes != null && !attributes.isEmpty()) {

                for (DElemAttribute attribute : attributes) {
                    if (attribute.displayFor(objectType.toString())) {
                        resultMap.put(Integer.valueOf(attribute.getID()), attribute);
                    }
                }

                // Now, if the object id is given, get the values of attributes of this particular object,
                // place them into the above-constructed map
                if (objectId > 0) {
                    attributes = getAttributes(String.valueOf(objectId), objectType.toString());
                    if (attributes != null && !attributes.isEmpty()) {
                        for (DElemAttribute attribute : attributes) {
                            resultMap.put(Integer.valueOf(attribute.getID()), attribute);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }

        return resultMap;
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public List<SchemaSet> getSchemaSetWorkingCopies() throws ServiceException {

        if (user == null || !user.isAuthentic()) {
            return new ArrayList<SchemaSet>();
        }

        ISchemaService schemaService = springContext.getBean(ISchemaService.class);
        return schemaService.getSchemaSetWorkingCopiesOf(user.getUserName());
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public List<Schema> getSchemaWorkingCopies() throws ServiceException {

        if (user == null || !user.isAuthentic()) {
            return new ArrayList<Schema>();
        }

        ISchemaService schemaService = springContext.getBean(ISchemaService.class);
        return schemaService.getSchemaWorkingCopiesOf(user.getUserName());
    }

    /**
     *
     * @return
     * @throws ServiceException
     */
    public List<VocabularyFolder> getVocabularyWorkingCopies() throws ServiceException {

        if (user == null || !user.isAuthentic()) {
            return new ArrayList<VocabularyFolder>();
        }

        IVocabularyService vocabularyService = springContext.getBean(IVocabularyService.class);
        return vocabularyService.getWorkingCopies(user.getUserName());
    }

    /**
     * @param schemaUrl
     * @return
     */
    public SchemaConversionsData getXmlConvData(String schemaUrl) {
        IXmlConvService xmlConvService = springContext.getBean(IXmlConvService.class);
        try {
            return xmlConvService.getSchemaConversionsData(schemaUrl);
        } catch (ServiceException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @return
     */
    public List<RdfNamespace> getRdfNamespaces() {
        IAttributeDAO attributeDAO = springContext.getBean(IAttributeDAO.class);
        try {
            return attributeDAO.getRdfNamespaces();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     *
     */
    private static class DataElementComparator implements Comparator {

        /** */
        private static final int ID = 0;

        /** */
        private static final int ASC = 1;
        private static final int DESC = -1;

        /** */
        private int compField = -1;
        private int sortOrder = DataElementComparator.ASC;

        /**
         *
         */
        private DataElementComparator(int compField, int sortOrder) {
            this.compField = compField;
            this.sortOrder = sortOrder;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Object o1, Object o2) {

            if (compField == DataElementComparator.ID) {
                return (sortOrder * (((DataElement) o1).getID()).compareTo(((DataElement) o2).getID()));
            } else {
                throw new DDRuntimeException("Unknown comparator field: " + compField);
            }
        }
    }

    /**
     * Spring context can be used in classes following the old design pattern: handlers etc.
     *
     * @return spring context object
     */
    public ApplicationContext getSpringContext() {
        if (springContext == null) {
            initSpringContext();
        }
        return springContext;
    }

    private List<String> buildListFromCsv(String csvValues) throws SQLException {
        StringTokenizer tokens = new StringTokenizer(csvValues, ",");

        ArrayList<String> values = new ArrayList<String>();

        while (tokens.hasMoreTokens()) {
            values.add(tokens.nextToken());
        }

        return values;

    }

    /**
     * returns vocabulary.
     * @param vocabularyId vocabulary id
     * @return vocabulary
     */
    public VocabularyFolder getVocabulary(int vocabularyId) {
        //to avoid seniding weeks for redesign use this legacy code to bind vocabulary to data elements
        IVocabularyFolderDAO dao = springContext.getBean(IVocabularyFolderDAO.class);
        return dao.getVocabularyFolder(vocabularyId);
    }
    
   
    /*
     * Method created to support data_elememnt.jsp, dataset.jsp and dstable.jsp with the AttributeService spring bean.
     * @see AttributeService javadoc for reference. 
     */
    public List<VocabularyConcept> getAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity, String inheritanceModeCode) 
            throws ResourceNotFoundException, EmptyParameterException {
        AttributeService attributeService = springContext.getBean(AttributeService.class);
        return attributeService.getAttributeVocabularyConcepts(attributeId, ddEntity, Attribute.ValueInheritanceMode.getInstance(inheritanceModeCode));
    }
    
    /*
     * Method Created to support data_element.jsp and dstable.jsp with the AttributeService spring bean.
     * @see AttributeService javadoc for reference.
     */
    public List<VocabularyConcept> getInheritedAttributeVocabularyConcepts(int attributeId, DataDictEntity ddEntity) 
            throws ResourceNotFoundException, EmptyParameterException {
        AttributeService attributeService = springContext.getBean(AttributeService.class);
        return attributeService.getInherittedAttributeVocabularyConcepts(attributeId, ddEntity);
    }
    
    /*
     * Method created to support data_element.jsp, dataset.jsp and dstable.jsp with the AttributeDataService spring bean.
     * @see AttributeDataService javadoc for reference.
     */
    public boolean existsVocabularyBinding(int attributeId){
        AttributeDataService attributeDataService = springContext.getBean(AttributeDataService.class);
        Integer vocabularyId = attributeDataService.getVocabularyBinding(attributeId);
        return vocabularyId != null;
    }

}
