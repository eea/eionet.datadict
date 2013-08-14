package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.meta.DataElement;
import eionet.util.RequestMessages;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import eionet.util.sql.SQLGenerator;

public class DsTableHandler extends BaseHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(DsTableHandler.class);

    public static String ATTR_PREFIX = "attr_";
    public static String ATTR_MULT_PREFIX = "attr_mult_";

    public static String INHERIT_ATTR_PREFIX = "inherit_";
    public static String INHERIT_COMPLEX_ATTR_PREFIX = "inherit_complex_";

    public static String POS_PREFIX = "pos_";
    public static String OLDPOS_PREFIX = "oldpos_";

    private String mode = null;
    private String lastInsertID = null;

    private DDSearchEngine searchEngine = null;

    private String nsID = null;
    private String dstID = null;

    boolean copy = false; // making a copy, exists() not performed
    String version = null; // used only when making a copy

    boolean versioning = true;
    boolean superUser = false;
    private String date = null;

    /** for storing dataset ID returned by VersionManager.deleteTbl() */
    private String newDstID = null;

    /** for storing restored table ID returned by Restorer.restoreTbl() */
    private String restoredID = null;

    private boolean importMode = false;

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     */
    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx) {
        this(conn, new Parameters(req), ctx);
        httpServletRequest = req;
    }

    public DsTableHandler(Connection conn, Parameters req, ServletContext ctx) {
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");

        dstID = req.getParameter("ds_id");

        if (ctx != null) {
            String _versioning = ctx.getInitParameter("versioning");
            if (_versioning != null && _versioning.equalsIgnoreCase("false")) {
                setVersioning(false);
            }
        }
    }

    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode) {
        this(conn, req, ctx);
        this.mode = mode;
    }

    public void setImport(boolean importMode) {
        this.importMode = importMode;
    }

    public void setVersioning(boolean f) {
        this.versioning = f;
    }

    public boolean getVersioning() {
        return this.versioning;
    }

    public void setSuperUser(boolean su) {
        this.superUser = su;
    }

    /**
     *
     * @return
     */
    public String getNewDstID() {
        return this.newDstID;
    }

    /**
     *
     * @throws Exception
     */
    @Override
    public void execute_() throws Exception {

        if (mode != null && mode.equalsIgnoreCase("copy")) {
            mode = "add";
            copy = true;
            version = req.getParameter("version");
        }

        if (mode == null
                || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("edit") && !mode.equalsIgnoreCase("delete") && !mode
                        .equalsIgnoreCase("edit_order"))) {

            throw new Exception("DsTableHandler mode unspecified or unknown!");
        }

        if (mode.equalsIgnoreCase("add")) {
            insert();
        } else if (mode.equalsIgnoreCase("edit")) {
            update();
        } else if (mode.equalsIgnoreCase("edit_order")) {
            saveTablesOrderInDataset();
        } else {
            delete();
            cleanVisuals();
        }
    }

    /**
     * @throws SQLException
     *
     */
    private void saveTablesOrderInDataset() throws Exception {

        HashMap positions = new HashMap();

        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()) {

            String parName = (String) parNames.nextElement();
            if (parName.startsWith(OLDPOS_PREFIX)) {

                Integer id = Integer.valueOf(parName.substring(OLDPOS_PREFIX.length()));
                Integer oldPos = Integer.valueOf(req.getParameter(parName));
                Integer newPos = Integer.valueOf(req.getParameter(POS_PREFIX + id));

                if (!oldPos.equals(newPos)) {
                    positions.put(id, newPos);
                }
            }
        }

        if (!positions.isEmpty()) {

            if (dstID == null || dstID.length() == 0) {
                throw new Exception("Missing request parameter: ds_id");
            }

            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("update DST2TBL set POSITION=? where TABLE_ID=? and DATASET_ID=?");
                for (Iterator iter = positions.entrySet().iterator(); iter.hasNext();) {

                    Map.Entry entry = (Map.Entry) iter.next();
                    stmt.setInt(1, ((Integer) entry.getValue()).intValue());
                    stmt.setInt(2, ((Integer) entry.getKey()).intValue());
                    stmt.setInt(3, Integer.parseInt(this.dstID));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } finally {
                SQL.close(stmt);
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    private void insert() throws Exception {

        // if linking to another element, do the linking and return
        String link_elm = req.getParameter("link_elm");
        String rplc_elm = req.getParameter("rplc_elm");
        if (link_elm != null && link_elm.length() > 0) {

            INParameters inParams = new INParameters();
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("TBL2ELEM");
            gen.setFieldExpr("TABLE_ID", inParams.add(req.getParameter("table_id"), Types.INTEGER));
            gen.setFieldExpr("DATAELEM_ID", inParams.add(req.getParameter("link_elm"), Types.INTEGER));
            gen.setFieldExpr("POSITION", inParams.add(req.getParameter("elmpos"), Types.INTEGER));

            PreparedStatement stmt = SQL.preparedStatement(gen.insertStatement(), inParams, conn);
            stmt.executeUpdate();
            stmt.close();
            return;
        } else if (rplc_elm != null && rplc_elm.length() > 0) {

            INParameters inParams = new INParameters();
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("TBL2ELEM");
            gen.setFieldExpr("TABLE_ID", inParams.add(req.getParameter("table_id"), Types.INTEGER));
            gen.setFieldExpr("DATAELEM_ID", inParams.add(req.getParameter("rplc_elm"), Types.INTEGER));
            gen.setFieldExpr("POSITION", inParams.add(req.getParameter("rplc_pos"), Types.INTEGER));

            PreparedStatement stmt = SQL.preparedStatement(gen.insertStatement(), inParams, conn);
            stmt.executeUpdate();

            inParams = new INParameters();
            String q =
                    "delete from TBL2ELEM where TABLE_ID=" + inParams.add(req.getParameter("table_id"), Types.INTEGER)
                            + " and DATAELEM_ID=" + inParams.add(req.getParameter("rplc_id"), Types.INTEGER);

            stmt = SQL.preparedStatement(q, inParams, conn);
            stmt.executeUpdate();
            stmt.close();
            RequestMessages.add(this.httpServletRequest, RequestMessages.system, "Replacement successful!");

            return;
        }

        // check the dataset id number
        if (dstID == null || dstID.length() == 0) {
            throw new Exception("Missing request parameter: ds_id");
        }

        // get the table identifier
        String idfier = req.getParameter("idfier");
        if (idfier == null || idfier.length() == 0) {
            throw new Exception("Missing request parameter: idfier");
        }

        // now make sure such a table does not exist within this dataset
        if (existsInDataset(dstID, idfier)) {
            throw new Exception("The dataset already has a table with this Identifier: " + idfier);
        }

        // get parent namespace id (the getter will throw exception if not found)
        String parentNS = getParentNamespaceID();

        // if new table across this dataset's versions, create table's corresponding namespace
        String correspNS = existsInDatasetVersions(parentNS, idfier);
        if (correspNS == null || correspNS.equals("0")) {
            correspNS = createNamespace(req.getParameter("ds_name"), idfier, parentNS);
        }
        if (correspNS == null) {
            throw new Exception("Failed to obtain the ID of corresponding namespace for this table");
        }

        // create the new table
        INParameters inParams = new INParameters();
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setFieldExpr("IDENTIFIER", inParams.add(idfier, Types.VARCHAR));
        if (user != null) {
            gen.setFieldExpr("USER", inParams.add(user.getUserName(), Types.VARCHAR));
        }
        if (date == null) {
            date = String.valueOf(System.currentTimeMillis());
        }
        gen.setFieldExpr("DATE", inParams.add(date));
        gen.setFieldExpr("CORRESP_NS", inParams.add(correspNS, Types.INTEGER));

        if (parentNS != null) {
            gen.setFieldExpr("PARENT_NS", inParams.add(parentNS, Types.INTEGER));
        }

        String shortName = req.getParameter("short_name");
        if (shortName == null || shortName.length() == 0) {
            shortName = idfier;
        }
        gen.setFieldExpr("SHORT_NAME", inParams.add(shortName, Types.VARCHAR));

        PreparedStatement stmt = SQL.preparedStatement(gen.insertStatement(), inParams, conn);
        stmt.executeUpdate();
        stmt.close();

        setLastInsertID();

        // create row in DST2TBL
        inParams = new INParameters();
        gen.clear();
        gen.setTable("DST2TBL");
        gen.setFieldExpr("TABLE_ID", inParams.add(lastInsertID, Types.INTEGER));
        gen.setFieldExpr("DATASET_ID", inParams.add(dstID, Types.INTEGER));

        stmt = SQL.preparedStatement(gen.insertStatement(), inParams, conn);
        stmt.executeUpdate();

        stmt.close();

        if (!copy) {
            processAttributes();
        }
    }

    /**
     *
     * @throws Exception
     */
    private void update() throws Exception {

        // get the table id number
        String tableID = req.getParameter("table_id");
        if (tableID == null || tableID.length() == 0) {
            throw new Exception("Missing request parameter: table_id");
        }

        INParameters inParams = new INParameters();
        // update short name
        String shortName = req.getParameter("short_name");
        if (shortName != null && shortName.length() > 0) {
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("DS_TABLE");
            gen.setFieldExpr("SHORT_NAME", inParams.add(shortName, Types.VARCHAR));

            String q = gen.updateStatement() + " where TABLE_ID=" + inParams.add(tableID, Types.INTEGER);

            PreparedStatement stmt = SQL.preparedStatement(q, inParams, conn);
            stmt.executeUpdate();
        }

        lastInsertID = tableID;
        String[] delIDs = {tableID};
        deleteAttributes(delIDs);
        processAttributes();
    }

    /**
     *
     * @throws Exception
     */
    private void delete() throws Exception {

        // get id numbers of tables to delete
        String[] del_IDs = req.getParameterValues("del_id");
        if (del_IDs == null || del_IDs.length == 0) {
            return;
        }

        INParameters inParams = new INParameters();
        // get id numbers of corresponding namespaces
        HashSet correspNss = new HashSet();
        StringBuffer buf = new StringBuffer("select distinct CORRESP_NS from DS_TABLE where ");
        for (int i = 0; i < del_IDs.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("TABLE_ID=").append(inParams.add(del_IDs[i], Types.INTEGER));
        }

        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        ResultSet rs = stmt.executeQuery();
        while (rs != null && rs.next()) {
            correspNss.add(rs.getString("CORRESP_NS"));
        }

        // delete table attributes
        deleteAttributes(del_IDs);
        deleteComplexAttributes(del_IDs);

        // delete table elements
        deleteElements(del_IDs);

        inParams = new INParameters();
        // delete the tbl2dst relations
        buf = new StringBuffer("delete from DST2TBL where ");
        for (int i = 0; i < del_IDs.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("TABLE_ID=").append(inParams.add(del_IDs[i], Types.INTEGER));
        }
        stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        stmt.executeUpdate();

        deleteDocs(del_IDs);
        deleteCache(del_IDs);

        inParams = new INParameters();
        // delete the tables themselves
        buf = new StringBuffer("delete from DS_TABLE where ");
        for (int i = 0; i < del_IDs.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("TABLE_ID=").append(inParams.add(del_IDs[i], Types.INTEGER));
        }
        stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        stmt.executeUpdate();

        // delete namespaces that have no corresponding table any more
        deleteUnmatchedNamespaces(stmt, correspNss);

        stmt.close();
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteDocs(String[] del_IDs) throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("delete from DOC where OWNER_TYPE='tbl' and OWNER_ID=?");
            for (int i = 0; i < del_IDs.length; i++) {
                stmt.setInt(1, Integer.valueOf(del_IDs[i]).intValue());
                stmt.executeUpdate();
            }
        } finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteCache(String[] del_IDs) throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("delete from CACHE where OBJ_TYPE='tbl' and OBJ_ID=?");
            for (int i = 0; i < del_IDs.length; i++) {
                stmt.setInt(1, Integer.valueOf(del_IDs[i]).intValue());
                stmt.executeUpdate();
            }
        } finally {
            SQL.close(stmt);
        }
    }

    /*
     *
     */
    private void deleteElements(String[] del_IDs) throws Exception {

        // get all non-common elements in these tables
        INParameters inParams = new INParameters();
        HashSet elems = new HashSet();
        StringBuffer buf = new StringBuffer("select distinct TBL2ELEM.DATAELEM_ID from TBL2ELEM ");
        buf.append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID where (");
        for (int i = 0; i < del_IDs.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("TBL2ELEM.TABLE_ID=");
            buf.append(inParams.add(del_IDs[i], Types.INTEGER));
        }
        buf.append(") and DATAELEM.DATAELEM_ID is not null and DATAELEM.PARENT_NS is not null");

        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            elems.add(rs.getString("TBL2ELEM.DATAELEM_ID"));
        }

        // delete the above found non-common elements
        if (elems.size() > 0) {

            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");

            String completeDelete = req.getParameter("complete");
            if (completeDelete != null && completeDelete.equals("true")) {
                params.addParameterValue("complete", "true");
            }

            for (Iterator iter = elems.iterator(); iter.hasNext();) {
                params.addParameterValue("delem_id", (String) iter.next());
            }

            DataElementHandler delemHandler = new DataElementHandler(conn, params, ctx);
            delemHandler.setUser(user);
            delemHandler.setSuperUser(superUser);
            delemHandler.setVersioning(false);
            delemHandler.execute();
        }

        // delete tbl2elm relations (also takes care of links to common elements
        // and links to elements that do not exist due to some erroneous situation)
        inParams = new INParameters();
        buf = new StringBuffer("delete from TBL2ELEM where ");
        for (int i = 0; i < del_IDs.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("TABLE_ID=");
            buf.append(inParams.add(del_IDs[i], Types.INTEGER));
        }

        stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     *
     * @param originals
     * @throws Exception
     */
    private void processOriginals(HashSet originals) throws Exception {

        if (originals == null || originals.size() == 0) {
            return;
        }

        // build the query
        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct TABLE_ID from DS_TABLE where ");
        Iterator iter = originals.iterator();
        int i = 0;
        while (iter.hasNext()) {
            String s = (String) iter.next();
            int pos = s.indexOf(",");
            String tblName = s.substring(pos + 1);
            String parentNs = s.substring(0, pos);
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("(IDENTIFIER=");
            buf.append(inParams.add(tblName, Types.VARCHAR));
            buf.append(" and PARENT_NS=");
            buf.append(inParams.add(parentNs, Types.INTEGER));
        }

        // execute the query
        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        ResultSet rs = stmt.executeQuery();

        // get the ids of originals
        HashSet hash = new HashSet();
        while (rs.next()) {
            hash.add(rs.getString("TABLE_ID"));
        }

        // reset the WORKING_USER in all found originals
        iter = hash.iterator();
        Statement statement = conn.createStatement();
        while (iter.hasNext()) {
            statement.executeUpdate("update DS_TABLE set WORKING_USER=NULL " + "where TABLE_ID=" + (String) iter.next());
        }
    }

    /**
     *
     */
    private String createNamespace(String dstName, String tblIdfier, String tblParentNS) throws Exception {
        dstName = dstName == null ? "" : dstName;
        String shortName = tblIdfier + "_tbl_" + dstName + "_dst";
        String fullName = tblIdfier + " table in " + dstName + " dataset";
        String definition = "The namespace of " + fullName;

        Parameters pars = new Parameters();
        pars.addParameterValue("mode", "add");
        pars.addParameterValue("short_name", shortName);
        pars.addParameterValue("fullName", fullName);
        pars.addParameterValue("description", definition);
        if (tblParentNS != null && tblParentNS.length() > 0) {
            pars.addParameterValue("parent_ns", tblParentNS);
        }

        NamespaceHandler nsHandler = new NamespaceHandler(conn, pars, ctx);
        nsHandler.execute();

        return nsHandler.getLastInsertID();
    }

    private void deleteAttributes(String[] del_IDs) throws SQLException {

        if (del_IDs == null || del_IDs.length == 0) {
            return;
        }

        StringBuffer buf =
                new StringBuffer().append("delete from ATTRIBUTE where PARENT_TYPE='T' and DATAELEM_ID in (")
                        .append(eionet.util.Util.toCSV(del_IDs)).append(")");

        // Skip the deletion of image-attributes if not in complete-delete mode.
        // That's because image-attributes are handled by image upload servlet instead.
        // Complete-delete mode is used for example by version manager, and means
        // that all must be deleted with no exceptions.
        String completeDelete = req.getParameter("complete");
        if (completeDelete == null || !completeDelete.equals("true")) {
            buf.append(" and M_ATTRIBUTE_ID not in ").append("(select M_ATTRIBUTE_ID from M_ATTRIBUTE where DISP_TYPE='image')");
        }

        LOGGER.debug(buf.toString());

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
        } finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     */
    private void deleteComplexAttributes(String[] del_IDs) throws SQLException {

        for (int i = 0; del_IDs != null && i < del_IDs.length; i++) {

            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("legal_delete", "true");
            params.addParameterValue("parent_id", del_IDs[i]);
            params.addParameterValue("parent_type", "T");

            AttrFieldsHandler attrFieldsHandler = new AttrFieldsHandler(conn, params, ctx);
            // attrFieldsHandler.setVersioning(this.versioning);
            attrFieldsHandler.setVersioning(false);
            try {
                attrFieldsHandler.execute();
            } catch (Exception e) {
                throw new SQLException(e.toString());
            }
        }
    }

    /**
     *
     */
    private void processAttributes() throws SQLException {
        String attrID = null;
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()) {
            String parName = (String) parNames.nextElement();
            if (parName.startsWith(ATTR_PREFIX) && !parName.startsWith(ATTR_MULT_PREFIX)) {
                String attrValue = req.getParameter(parName);
                if (attrValue.length() == 0) {
                    continue;
                }
                attrID = parName.substring(ATTR_PREFIX.length());
                if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID) != null) {
                    continue; // some attributes will be inherited from dataset level
                }
                insertAttribute(attrID, attrValue);
            } else if (parName.startsWith(ATTR_MULT_PREFIX)) {
                String[] attrValues = req.getParameterValues(parName);
                if (attrValues == null || attrValues.length == 0) {
                    continue;
                }
                attrID = parName.substring(ATTR_MULT_PREFIX.length());

                if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID) != null) {
                    continue; // some attributes will be inherited from dataset level
                }

                for (int i = 0; i < attrValues.length; i++) {
                    insertAttribute(attrID, attrValues[i]);
                }
            } else if (parName.startsWith(INHERIT_ATTR_PREFIX) && !parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)) {
                attrID = parName.substring(INHERIT_ATTR_PREFIX.length());
                if (dstID == null) {
                    continue;
                }
                CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
                ch.setUser(user);
                ch.copyAttribute(lastInsertID, dstID, "T", "DS", attrID);
            } else if (parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)) {
                attrID = parName.substring(INHERIT_COMPLEX_ATTR_PREFIX.length());
                if (dstID == null) {
                    continue;
                }
                CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
                ch.setUser(user);
                ch.copyComplexAttrs(lastInsertID, dstID, "DS", "T", attrID);
            }
        }
    }

    private void insertAttribute(String attrId, String value) throws SQLException {

        INParameters inParams = new INParameters();

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");

        gen.setFieldExpr("M_ATTRIBUTE_ID", inParams.add(attrId, Types.INTEGER));
        gen.setFieldExpr("DATAELEM_ID", inParams.add(lastInsertID, Types.INTEGER));
        gen.setFieldExpr("VALUE", inParams.add(value));
        gen.setField("PARENT_TYPE", "T");

        String sql = gen.insertStatement();
        LOGGER.debug(sql);

        PreparedStatement stmt = SQL.preparedStatement(sql, inParams, conn);
        stmt.executeUpdate();
        stmt.close();
    }

    private void setLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";

        LOGGER.debug(qry);

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        rs.clearWarnings();
        if (rs.next()) {
            lastInsertID = rs.getString(1);
        }
        stmt.close();
    }

    public String getLastInsertID() {
        return lastInsertID;
    }

    /**
     *
     * @return
     */
    public String getNamespaceID() {
        return nsID;
    }

    /**
     *
     * @param dstID
     * @param tblIdfier
     * @return
     * @throws SQLException
     */
    public boolean existsInDataset(String dstID, String tblIdfier) throws SQLException {

        if (copy) {
            return false;
        }

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer();
        buf.append("select count(DS_TABLE.TABLE_ID) from DST2TBL ")
                .append("left outer join DS_TABLE on DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID where ").append("DST2TBL.DATASET_ID=")
                .append(inParams.add(dstID, Types.INTEGER)).append(" and DS_TABLE.TABLE_ID is not null and DS_TABLE.IDENTIFIER=")
                .append(inParams.add(tblIdfier, Types.VARCHAR));

        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            if (rs.getInt(1) > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a table already exists with such identifier in such a dataset namespace. If so, then returns the table's
     * CORRESP_NS. Otherwise returns null.
     *
     * @param dstNamespaceID
     * @param tblIdfier
     * @return
     * @throws SQLException
     */
    public String existsInDatasetVersions(String dstNamespaceID, String tblIdfier) throws SQLException {

        if (copy) {
            return null;
        }
        INParameters inParams = new INParameters();

        String qry =
                "select distinct CORRESP_NS from DS_TABLE" + " where DS_TABLE.IDENTIFIER="
                        + inParams.add(tblIdfier, Types.VARCHAR) + " and DS_TABLE.PARENT_NS="
                        + inParams.add(dstNamespaceID, Types.INTEGER);

        PreparedStatement stmt = SQL.preparedStatement(qry, inParams, conn);
        ResultSet rs = stmt.executeQuery();

        return rs.next() ? rs.getString(1) : null;
    }

    /**
     *
     */
    public void copyTbl2Elem(String srcTblID) throws SQLException {

        if (searchEngine == null) {
            searchEngine = new DDSearchEngine(conn, "");
        }
        searchEngine.setUser(user);
        Vector elems = searchEngine.getDataElements(null, null, null, null, srcTblID);

        if (elems == null) {
            return;
        }

        for (int i = 0; i < elems.size(); i++) {
            DataElement elem = (DataElement) elems.get(i);

            String elem_id = elem.getID();

            Parameters pars = new Parameters();
            // "copy" is a new mode where exists() is not performed
            pars.addParameterValue("mode", "copy");
            pars.addParameterValue("table_id", getLastInsertID());
            pars.addParameterValue("delem_name", elem.getShortName());
            pars.addParameterValue("idfier", elem.getIdentifier());
            pars.addParameterValue("ns", elem.getNamespace().getID());
            pars.addParameterValue("type", elem.getType());
            pars.addParameterValue("copy_elem_id", elem_id);

            try {
                DataElementHandler handler = new DataElementHandler(conn, pars, ctx);
                handler.setUser(user);
                handler.setVersioning(false);
                handler.execute();
            } catch (Exception e) {
                throw new SQLException(e.toString());
            }
        }

    }

    public void setUser(DDUser user) {
        this.user = user;
    }

    public String getRestoredID() {
        return this.restoredID;
    }

    /*
     *
     */
    public void setDate(String unixTimestampMillisec) {
        this.date = unixTimestampMillisec;
    }

    /**
     * Deletes those given namespaces that are not present in CORRESP_NS of DS_TABLE. NB! Modifies the namespaces HashSet by
     * removing those that are not to be deleted.
     *
     * @throws SQLException
     */
    private void deleteUnmatchedNamespaces(Statement stmt, HashSet nss) throws SQLException {

        ResultSet rs = stmt.executeQuery("select distinct CORRESP_NS from DS_TABLE");
        while (rs.next()) {
            String nsid = rs.getString(1);
            if (nss.contains(nsid)) {
                nss.remove(nsid);
            }
        }

        if (nss.size() == 0) {
            return;
        }

        int i = 0;
        StringBuffer buf = new StringBuffer("delete from NAMESPACE where ");
        for (Iterator iter = nss.iterator(); iter.hasNext(); i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("NAMESPACE_ID=").append(iter.next());
        }

        stmt.executeUpdate(buf.toString());
    }

    /**
     * @throws Exception
     */
    private String getParentNamespaceID() throws Exception {

        String parentNsID = req.getParameter("parent_ns");
        if (parentNsID == null) {
            INParameters inParams = new INParameters();
            StringBuffer buf = new StringBuffer("select CORRESP_NS from DATASET where DATASET_ID=");
            buf.append(inParams.add(dstID, Types.INTEGER));

            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                rs = stmt.executeQuery();
                parentNsID = rs.next() ? rs.getString(1) : null;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException sqle) {
                }
            }
        }

        if (parentNsID == null) {
            throw new Exception("Failed to obtain parent namespace ID which is required");
        }

        return parentNsID;
    }

    /**
     *
     * @param checkedOutTableID
     * @param newID
     * @throws SQLException
     */
    public static void replaceTableId(String checkedOutTableID, String newID, Connection conn) throws SQLException {

        if (checkedOutTableID == null || checkedOutTableID.length() == 0 || newID == null || newID.length() == 0) {
            return;
        }

        PreparedStatement stmt = null;
        try {
            SQLGenerator gen = new SQLGenerator();

            INParameters inParams = new INParameters();
            inParams.add(checkedOutTableID, Types.INTEGER);
            inParams.add(newID, Types.INTEGER);

            gen.setTable("DS_TABLE");
            gen.setFieldExpr("TABLE_ID", "?");
            StringBuffer buf = new StringBuffer(gen.updateStatement());
            buf.append(" where TABLE_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen.setTable("DST2TBL");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where TABLE_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen.setTable("TBL2ELEM");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where TABLE_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen = new SQLGenerator();
            gen.setTable("ATTRIBUTE");
            gen.setFieldExpr("DATAELEM_ID", "?");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where PARENT_TYPE='T' and DATAELEM_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen = new SQLGenerator();
            gen.setTable("COMPLEX_ATTR_ROW");
            gen.setFieldExpr("PARENT_ID", "?");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where PARENT_TYPE='T' and PARENT_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen = new SQLGenerator();
            gen.setTable("COMPLEX_ATTR_ROW");
            gen.setFieldExpr("ROW_ID", "md5(concat(PARENT_ID, PARENT_TYPE, M_COMPLEX_ATTR_ID, POSITION))");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where PARENT_TYPE='T' and PARENT_ID=").append(checkedOutTableID);
            stmt.executeUpdate(buf.toString());

            gen = new SQLGenerator();
            gen.setTable("CACHE");
            gen.setFieldExpr("OBJ_ID", "?");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where OBJ_TYPE='tbl' and OBJ_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            gen = new SQLGenerator();
            gen.setTable("DOC");
            gen.setFieldExpr("OWNER_ID", "?");
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where OWNER_TYPE='tbl' and OWNER_ID=?");
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
        } finally {
            SQL.close(stmt);
        }
    }

}
