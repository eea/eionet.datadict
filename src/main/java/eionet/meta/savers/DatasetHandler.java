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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

import eionet.meta.DDUser;
import eionet.meta.Dataset;
import eionet.meta.VersionManager;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import eionet.util.sql.SQLGenerator;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class DatasetHandler extends BaseHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(DatasetHandler.class);

    public static String ATTR_PREFIX = "attr_";
    public static String ATTR_MULT_PREFIX = "attr_mult_";

    private String mode = null;
    private String ds_id = null;
    private String[] ds_ids = null;
    private String ds_name = null;
    private String idfier = null;
    private String lastInsertID = null;
    private boolean versioning = false;
    private boolean isImportMode = false;
    private String date = null;
    private String checkedInCopyID = null;
    private boolean useForce = false;

    /** indicates if top namespace needs to be released after an exception*/
    private boolean doCleanup = false;

    /** hashes for remembering originals and top namespaces for cleanup */
    HashSet origs = new HashSet();
    HashSet nss = new HashSet();

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     */
    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx) {
        this(conn, new Parameters(req), ctx);
    }

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     */
    public DatasetHandler(Connection conn, Parameters req, ServletContext ctx) {

        this.conn = conn;
        this.req = req;
        this.ctx = ctx;

        this.mode = req.getParameter("mode");
        this.ds_id = req.getParameter("ds_id");
        this.ds_ids = req.getParameterValues("ds_id");
        this.ds_name = req.getParameter("ds_name");
        this.idfier = req.getParameter("idfier");
    }

    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode) {
        this(conn, req, ctx);
        this.mode = mode;
    }

    public void setUser(DDUser user) {
        this.user = user;
    }

    public void setImportMode(boolean importMode) {
        this.isImportMode = importMode;
    }

    public void setVersioning(boolean f) {
        // override as part of the process of losing the versioning attribute at all eventually
        this.versioning = false;
    }

    public boolean getVersioning() {
        return this.versioning;
    }

    /**
     *
     * @throws Exception
     */
    public void cleanup() throws Exception {

        if (!doCleanup) return;

        processOriginals(origs);

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        gen.setFieldExpr("WORKING_USER", "NULL");
        for (Iterator i=nss.iterator(); i.hasNext(); ) {
            conn.createStatement().executeUpdate(gen.updateStatement() +
                    " where NAMESPACE_ID=" + (String)i.next());
        }
    }

    public void execute_() throws Exception {

        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                !mode.equalsIgnoreCase("edit") &&
                !mode.equalsIgnoreCase("restore") &&
                !mode.equalsIgnoreCase("delete")))
            throw new Exception("DatasetHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add")) {
            insert();
            ds_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else if (mode.equalsIgnoreCase("restore"))
            restore();
        else {
            delete();
            cleanVisuals();
        }
    }

    private void insert() throws Exception {

        if (this.idfier == null)
            throw new SQLException("Identifier must be specified!");

        if (exists()) throw new SQLException("Such a dataset already exists!");

        if (Util.voidStr(ds_name))
            ds_name = idfier;

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATASET");
        gen.setField("IDENTIFIER", idfier);
        gen.setField("SHORT_NAME", ds_name);

        // if not in import mode, treat new datasets as working copies until checked in
        if (!isImportMode) {
            gen.setField("WORKING_COPY", "Y");
            if (user!=null && user.isAuthentic())
                gen.setField("WORKING_USER", user.getUserName());
            gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        }
        if (user!=null)
            gen.setField("USER", user.getUserName());
        if (date==null)
            date = String.valueOf(System.currentTimeMillis());
        gen.setFieldExpr("DATE", date);

        // set the status
        String status = req.getParameter("reg_status");
        if (!Util.voidStr(status))
            gen.setField("REG_STATUS", status);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        setLastInsertID();

        // add acl
        if (user!=null) {
            String aclPath = "/datasets/" + idfier;
            HashMap acls = AccessController.getAcls();
            if (!acls.containsKey(aclPath)) {
                String aclDesc = "Identifier: " + idfier;
                AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
            }
        }

        // create the corresponding namespace
        // (this also sets the WORKING_USER)
        String correspNS = createNamespace(idfier);
        if (correspNS!=null) {
            gen.clear();
            gen.setTable("DATASET");
            gen.setField("CORRESP_NS", correspNS);
            stmt.executeUpdate(gen.updateStatement() +
                    " where DATASET_ID=" + lastInsertID);
        }

        stmt.close();

        // process dataset attributes
        processAttributes();
    }

    private void update() throws Exception {

        if (ds_id==null) throw new Exception("Dataset ID missing!");

        // see if it's just an unlock
        String unlock = req.getParameter("unlock");
        if (unlock!=null && !unlock.equals("false")) {

            // check if the user has the right to do this operation
            // ...

            unlockNamespace(unlock);
            return;
        }

        lastInsertID = ds_id;

        // if check-in, do the action and exit
        String checkIn = req.getParameter("check_in");
        if (checkIn!=null && checkIn.equalsIgnoreCase("true")) {

            VersionManager verMan = new VersionManager(conn, user);
            verMan.setContext(ctx);
            verMan.setServlRequestParams(req);

            String updVer = req.getParameter("upd_version");
            if (updVer!=null && updVer.equalsIgnoreCase("true")) {
                verMan.setVersionUpdate(true);
                setCheckedInCopyID(ds_id);
            }
            else
                setCheckedInCopyID(req.getParameter("checkedout_copy_id"));

            verMan.checkIn(ds_id, "dst",
                    req.getParameter("reg_status"));

            return;
        }

        // handle the update of data model
        String dsVisual = req.getParameter("visual");
        if (!Util.voidStr(dsVisual)) {

            SQLGenerator gen = new SQLGenerator();
            gen.setTable("DATASET");

            String strType = req.getParameter("str_type");
            String fldName = strType.equals("simple") ? "VISUAL" :
                "DETAILED_VISUAL";

            if (dsVisual.equalsIgnoreCase("NULL"))
                gen.setFieldExpr(fldName, dsVisual);
            else
                gen.setField(fldName, dsVisual);

            INParameters inParams = new INParameters();
            String q = gen.updateStatement()+" where DATASET_ID="+inParams.add(ds_id, Types.INTEGER);

            PreparedStatement stmt = null;
            stmt = SQL.preparedStatement(q, inParams, conn);
            stmt.executeUpdate();
            stmt.close();
            return; // we only changed the 'visual'. no need to deal with attrs
        }

        // update the DATASET table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATASET");

        // set the status
        String status = req.getParameter("reg_status");
        if (!Util.voidStr(status)) gen.setField("REG_STATUS", status);

        // short name
        if (!Util.voidStr(ds_name)) gen.setField("SHORT_NAME", ds_name);

        // display create links
        gen.setFieldExpr("DISP_CREATE_LINKS", getDisplayCreateLinks());

        // execute the statement

        INParameters inParams = new INParameters();
        String q = gen.updateStatement()+" where DATASET_ID="+inParams.add(ds_id, Types.INTEGER);

        PreparedStatement stmt = null;
        stmt = SQL.preparedStatement(q, inParams, conn);
        stmt.executeUpdate();
        stmt.close();

        deleteAttributes();
        processAttributes();
    }

    private void restore() throws Exception {

        if (ds_ids==null || ds_ids.length==0)
            return;

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATASET");
        gen.setFieldExpr("DELETED", "NULL");

        PreparedStatement stmt = null;
        for (int i=0; i<ds_ids.length; i++) {

            INParameters inParams = new INParameters();
            String q = gen.updateStatement() + " where DATASET_ID=" + inParams.add(ds_ids[i], Types.INTEGER);
            stmt = SQL.preparedStatement(q, inParams, conn);
            stmt.executeUpdate();
        }

        stmt.close();
    }

    /**
     * @throws Exception
     *
     *
     */
    private void delete() throws Exception {

        Statement stmt = null;
        try {
            // create SQL statement object and start transaction
            stmt = conn.createStatement();

            // do the delete
            delete(stmt);

        }
        finally {
            try {
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e) {}
        }
    }

    /**
     *
     * @throws Exception
     */
    private void delete(Statement stmt) throws Exception {

        if (ds_ids==null || ds_ids.length==0)
            return;

        if (user==null)
            throw new Exception("You have no permission to delete");

        String s = req.getParameter("complete");
        boolean isCompleteDelete = s!=null && s.equals("true");
        int i=0;

        // go through the given datasets in database, make sure they can be deleted
        // and gather information we need when starting the deletion
        HashSet namespaces = new HashSet();
        HashSet identifiers = new HashSet();
        HashSet unlockNamespaces = new HashSet();
        HashSet unlockCheckedoutCopies = new HashSet();
        StringBuffer buf = new StringBuffer("select * from DATASET where ");
        for (i=0; i<ds_ids.length; i++) {
            if (i>0) {
                buf.append(" or ");
            }
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        ResultSet rs = stmt.executeQuery(buf.toString());
        while (rs.next()) {
            String identifier = rs.getString("IDENTIFIER");
            String workingCopy = rs.getString("WORKING_COPY");
            String workingUser = rs.getString("WORKING_USER");
            String regStatus = rs.getString("REG_STATUS");
            String namespace = rs.getString("CORRESP_NS");
            if (workingCopy==null || regStatus==null || identifier==null) {
                throw new NullPointerException();
            }

            identifiers.add(identifier);
            if (namespace!=null) {
                namespaces.add(namespace);
            }

            if (workingCopy.equals("Y")) {
                if (workingUser==null && useForce==false) {
                    throw new Exception("Working copy without a working user");
                } else if (!workingUser.equals(user.getUserName()) && useForce==false) {

                    throw new Exception("Cannot delete working copy of another user");
                } else {
                    if (!isCompleteDelete) {
                        throw new Exception("Trying to delete working copy not completely");
                    } else {
                        try {
                            unlockNamespaces.add(namespace);
                            String checkedOutCopyID = rs.getString("CHECKEDOUT_COPY_ID");
                            if (checkedOutCopyID!=null && checkedOutCopyID.length()>0)
                                unlockCheckedoutCopies.add(rs.getString("CHECKEDOUT_COPY_ID"));
                        } catch (NullPointerException npe) {}
                    }
                }
            }
            else if (workingUser!=null && useForce==false) {
                throw new Exception("Dataset checked out by another user: " + workingUser);
            } else if (useForce==false) {
                boolean canDelete = false;
                if (regStatus.equals("Released") || regStatus.equals("Recorded")) {
                    canDelete = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + identifier, "er");
                } else {
                    canDelete = SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + identifier, "u") ||
                    SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + identifier, "er");
                }
                if (!canDelete) {
                    throw new Exception("You have no permission to delete this dataset: " +
                            rs.getString("DATASET_ID"));
                }
            }
        }

        // if not deleting completely, just set the DELETED flag and return
        if (!isCompleteDelete) {
            setDeletedFlag(stmt);
            return;
        }

        // delete dataset dependencies
        deleteAttributes();
        deleteComplexAttributes();
        deleteRodLinks();
        deleteCache();
        deleteDocs();
        deleteTablesAndElements();

        // delete the datasets themselves
        buf = new StringBuffer("delete from DATASET where ");
        for (i=0; i<ds_ids.length; i++) {
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        stmt.executeUpdate(buf.toString());

        // delete namespaces that have no corresponding dataset any more
        deleteUnmatchedNamespaces(stmt, namespaces);

        // remove acls of datasets whose identifiers are not present any more
        removeAcls(stmt, identifiers);

        // unlock checked out copies whose working copies were deleted
        if (unlockCheckedoutCopies.size()>0) {
            i=0;
            buf = new StringBuffer("update DATASET set WORKING_USER=NULL where ");
            for (Iterator iter=unlockCheckedoutCopies.iterator(); iter.hasNext(); i++) {
                if (i>0) {
                    buf.append(" or ");
                }
                buf.append("DATASET_ID=").append(iter.next());
            }
            stmt.executeUpdate(buf.toString());
        }

        // unlock namespaces of deleted working copies
        if (unlockNamespaces.size()>0) {
            i=0;
            buf = new StringBuffer("update NAMESPACE set WORKING_USER=NULL where ");
            for (Iterator iter=unlockNamespaces.iterator(); iter.hasNext(); i++) {
                if (i>0) {
                    buf.append(" or ");
                }
                buf.append("NAMESPACE_ID=").append(iter.next());
            }
            stmt.executeUpdate(buf.toString());
        }
    }

    /**
     * Removes ACLs of those datasets not present any more in IDENTIFIER from DATASET.
     * NB! Modifies the identifiers HashSet by removing those whose acl is not to be deleted.
     *
     * @throws SQLException
     * @throws SignOnException
     */
    private void removeAcls(Statement stmt, HashSet identifiers) throws SQLException, SignOnException {

        ResultSet rs = stmt.executeQuery("select distinct IDENTIFIER from DATASET");
        while (rs.next()) {
            String identifier = rs.getString(1);
            if (identifiers.contains(identifier)) {
                identifiers.remove(identifier);
            }
        }

        if (identifiers.size()==0)
            return;

        int i=0;
        for (Iterator iter = identifiers.iterator(); iter.hasNext(); i++) {
            AccessController.removeAcl("/datasets/" + (String)iter.next());
        }
    }

    /**
     * Deletes those given namespaces that are not present in CORRESP_NS of DATASET.
     * NB! Modifies the namespaces HashSet by removing those that are not to be deleted.
     *
     * @throws SQLException
     */
    private void deleteUnmatchedNamespaces(Statement stmt, HashSet namespaces) throws SQLException {

        ResultSet rs = stmt.executeQuery("select distinct CORRESP_NS from DATASET");
        while (rs.next()) {
            String nsid = rs.getString(1);
            if (namespaces.contains(nsid)) {
                namespaces.remove(nsid);
            }
        }

        if (namespaces.size()==0) {
            return;
        }

        int i=0;
        StringBuffer buf = new StringBuffer("delete from NAMESPACE where ");
        for (Iterator iter = namespaces.iterator(); iter.hasNext(); i++) {
            if (i>0) {
                buf.append(" or ");
            }
            buf.append("NAMESPACE_ID=").append(iter.next());
        }
        stmt.executeUpdate(buf.toString());
    }

    /**
     * @throws SQLException
     *
     *
     */
    private void setDeletedFlag(Statement stmt) throws SQLException {

        if (ds_ids==null || ds_ids.length==0)
            return;

        StringBuffer buf = new StringBuffer("update DATASET set DELETED=");
        buf.append(Util.strLiteral(user.getUserName()));
        buf.append(" where ");
        for (int i=0; i<ds_ids.length; i++) {
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }

        stmt.executeUpdate(buf.toString());
    }

    /**
     *
     */
    private String createNamespace(String idfier) throws Exception {

        String shortName  = idfier + "_dst";
        String fullName   = idfier + " dataset";
        String definition = "The namespace of " + fullName;

        Parameters pars = new Parameters();
        pars.addParameterValue("mode", "add");
        pars.addParameterValue("short_name", shortName);
        pars.addParameterValue("fullName", fullName);
        pars.addParameterValue("description", definition);

        if (!isImportMode && user!=null) {
            pars.addParameterValue("wrk_user", user.getUserName());
        }

        NamespaceHandler nsHandler = new NamespaceHandler(conn, pars, ctx);
        nsHandler.execute();

        return nsHandler.getLastInsertID();
    }

    private void deleteAttributes() throws SQLException {

        ResultSet rs = null;
        PreparedStatement stmt = null;
        INParameters inParams = new INParameters();
        try {
            // find out image attributes, so to skip them later

            StringBuffer buf = new StringBuffer("select M_ATTRIBUTE_ID ");
            buf.append("from M_ATTRIBUTE where DISP_TYPE='image'");

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();

            Vector imgAttrs = new Vector();
            while (rs.next()) {
                imgAttrs.add(rs.getString(1));
            }
            SQL.close(rs);
            SQL.close(stmt);

            // prepare attribute deletion SQL

            buf = new StringBuffer("delete from ATTRIBUTE where (");
            for (int i=0; i<ds_ids.length; i++) {
                if (i>0) {
                    buf.append(" or ");
                }
                buf.append("DATAELEM_ID=");
                buf.append(inParams.add(ds_ids[i], Types.INTEGER));
            }
            buf.append(") and PARENT_TYPE='DS'");
            // skip image attributes
            for (int i=0; i<imgAttrs.size(); i++) {
                buf.append(" and M_ATTRIBUTE_ID<>").append((String)imgAttrs.get(i));
            }

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
        }
        finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    private void deleteComplexAttributes() throws SQLException {

        for (int i=0; ds_ids!=null && i<ds_ids.length; i++) {

            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("legal_delete", "true");
            params.addParameterValue("parent_id", ds_ids[i]);
            params.addParameterValue("parent_type", "DS");

            AttrFieldsHandler attrFieldsHandler =
                new AttrFieldsHandler(conn, params, ctx);
            //attrFieldsHandler.setVersioning(this.versioning);
            attrFieldsHandler.setVersioning(false);
            try {
                attrFieldsHandler.execute();
            }
            catch (Exception e) {
                throw new SQLException(e.toString());
            }
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteRodLinks() throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("delete from DST2ROD where ");
        for (int i=0; i<ds_ids.length; i++) {
            if (i>0) buf.append(" or ");
            buf.append("DATASET_ID=").append(inParams.add(ds_ids[i], Types.INTEGER));
        }

        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
        }
        finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteDocs() throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("delete from DOC where OWNER_TYPE='dst' and OWNER_ID=?");
            for (int i=0; i<ds_ids.length; i++) {
                stmt.setInt(1, Integer.valueOf(ds_ids[i]).intValue());
                stmt.executeUpdate();
            }
        }
        finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void deleteCache() throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("delete from CACHE where OBJ_TYPE='dst' and OBJ_ID=?");
            for (int i=0; i<ds_ids.length; i++) {
                stmt.setInt(1, Integer.valueOf(ds_ids[i]).intValue());
                stmt.executeUpdate();
            }
        }
        finally {
            SQL.close(stmt);
        }
    }


    /**
     *
     * @throws SQLException
     */
    private void processAttributes() throws SQLException {
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()) {
            String parName = (String)parNames.nextElement();

            if (!parName.startsWith(ATTR_PREFIX)) {
                continue;
            }
            if (parName.startsWith(ATTR_MULT_PREFIX)) {
                String[] attrValues = req.getParameterValues(parName);
                if (attrValues == null || attrValues.length == 0) {
                    continue;
                }
                String attrID = parName.substring(ATTR_MULT_PREFIX.length());
                for (int i=0; i<attrValues.length; i++) {
                    insertAttribute(attrID, attrValues[i]);
                }
            }
            else {
                String attrValue = req.getParameter(parName);
                if (attrValue.length()==0)
                    continue;
                String attrID = parName.substring(ATTR_PREFIX.length());
                insertAttribute(attrID, attrValue);
            }
        }
    }

    private void insertAttribute(String attrId, String value) throws SQLException {

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");

        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setField("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "DS");

        String sql = gen.insertStatement();
        LOGGER.debug(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private void deleteTablesAndElements() throws Exception {

        ResultSet rs = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        try {
            // loop through datasets
            for (int i=0; i<ds_ids.length; i++) {

                // get the tables in this dataset

                INParameters inParams = new INParameters();
                String qry = "select distinct TABLE_ID from DST2TBL where DATASET_ID="
                    + inParams.add(ds_ids[i], Types.INTEGER);

                pstmt1 = SQL.preparedStatement(qry, inParams, conn);
                rs = pstmt1.executeQuery();

                Vector v = new Vector();
                while (rs.next()) {
                    v.add(rs.getString("TABLE_ID"));
                }
                SQL.close(rs);
                SQL.close(pstmt1);

                // delete the tables found
                if (v.size()>0) {

                    Parameters params = new Parameters();
                    params.addParameterValue("mode", "delete");

                    String completeDelete = req.getParameter("complete");
                    if (completeDelete!=null && completeDelete.equals("true")) {
                        params.addParameterValue("complete", "true");
                    }

                    for (int j=0; j<v.size(); j++) {
                        params.addParameterValue("del_id", (String)v.get(j));
                    }

                    DsTableHandler tableHandler = new DsTableHandler(conn, params, ctx);
                    tableHandler.setUser(user);
                    tableHandler.setVersioning(false);
                    tableHandler.execute();
                }

                // delete dataset-to-table relations
                inParams = new INParameters();
                StringBuffer buf = new StringBuffer("delete from DST2TBL where DATASET_ID=");
                buf.append(inParams.add(ds_ids[i], Types.INTEGER));

                pstmt2 = SQL.preparedStatement(buf.toString(), inParams, conn);
                pstmt2.executeUpdate();
                SQL.close(pstmt2);
            }
        }
        finally {
            SQL.close(rs);
            SQL.close(pstmt1);
            SQL.close(pstmt2);
        }
    }

    private void deleteNamespaces() throws SQLException {

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("delete from NAMESPACE where ");
        for (int i=0; i<ds_ids.length; i++) {
            if (i>0) {
                buf.append(" or ");
            }
            buf.append("NAMESPACE_ID=");
            buf.append(inParams.add(ds_ids[i], Types.INTEGER));
        }

        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
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

    public boolean exists() throws SQLException {

        INParameters inParams = new INParameters();

        String qry =
            "select count(*) as COUNT from DATASET " +
            "where IDENTIFIER=" + inParams.add(idfier, Types.VARCHAR);

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(qry, inParams, conn);
            rs = stmt.executeQuery();

            if (rs!=null && rs.next()) {
                if (rs.getInt("COUNT")>0) {
                    return true;
                }
            }

        }
        finally {
            SQL.close(rs);
            SQL.close(stmt);
        }

        return false;
    }

    /**
     *
     */
    private void processOriginals(HashSet originals) throws Exception {

        if (originals==null || originals.size()==0) {
            return;
        }

        INParameters inParams = new INParameters();

        // build the SQL
        StringBuffer buf = new StringBuffer();
        buf.append("update DATASET set WORKING_USER=NULL where ");
        buf.append("WORKING_USER=" + inParams.add(user.getUserName(), Types.VARCHAR) + " and (");
        int i=0;
        for (Iterator iter=originals.iterator(); iter.hasNext(); i++) {
            if (i>0) {
                buf.append(" or ");
            }
            buf.append("IDENTIFIER=" + inParams.add(iter.next(), Types.VARCHAR));
        }
        buf.append(")");
        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        stmt.executeUpdate();
    }

    private void unlockNamespace(String nsID) throws SQLException {

        INParameters inParams = new INParameters();

        String s = "update NAMESPACE set WORKING_USER=NULL where NAMESPACE_ID="
            + inParams.add(nsID, Types.INTEGER);

        PreparedStatement stmt = SQL.preparedStatement(s, inParams, conn);
        stmt.executeUpdate();
    }

    private String getDisplayCreateLinks() {

        String[] dispCreateLinks = req.getParameterValues("disp_create_links");
        if (dispCreateLinks == null || dispCreateLinks.length == 0) {
            return "0";
        }

        int k = 0;
        Hashtable weights = Dataset.getCreateLinkWeights();
        for (int i=0; i<dispCreateLinks.length; i++) {
            Integer weight = (Integer)weights.get(dispCreateLinks[i]);
            if (weight != null) {
                k = k + weight.intValue();
            }
        }

        return String.valueOf(k);
    }

    /*
     *
     */
    public void setDate(String unixTimestampMillisec) {
        this.date = unixTimestampMillisec;
    }


    /**
     *
     * @return
     */
    public String getCheckedInCopyID() {
        return checkedInCopyID;
    }

    /**
     *
     * @param latestDatasetID
     */
    private void setCheckedInCopyID(String latestDatasetID) {
        this.checkedInCopyID = latestDatasetID;
    }

    /**
     *
     * @param oldID
     * @param newID
     * @param conn
     */
    public static void replaceID(String oldID, String newID, Connection conn) throws SQLException {

        PreparedStatement stmt = null;
        SQLGenerator gen = null;
        StringBuffer buf = null;
        try {

            INParameters inParams = new INParameters();

            gen = new SQLGenerator();
            gen.setTable("DATASET");
            gen.setFieldExpr("DATASET_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where DATASET_ID=").append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();


            inParams = new INParameters();
            gen.clear();
            gen.setTable("ATTRIBUTE");
            gen.setFieldExpr("DATAELEM_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where PARENT_TYPE='DS' and DATAELEM_ID=").
            append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            inParams = new INParameters();
            gen.clear();
            gen.setTable("COMPLEX_ATTR_ROW");
            gen.setFieldExpr("PARENT_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where PARENT_TYPE='DS' and PARENT_ID=").
            append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            inParams = new INParameters();
            gen.clear();
            gen.setTable("DST2TBL");
            gen.setFieldExpr("DATASET_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where DATASET_ID=").
            append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            inParams = new INParameters();
            gen.clear();
            gen.setTable("DST2ROD");
            gen.setFieldExpr("DATASET_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where DATASET_ID=").
            append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

            inParams = new INParameters();
            gen.clear();
            gen.setTable("DOC");
            gen.setFieldExpr("OWNER_ID", newID);
            buf = new StringBuffer(gen.updateStatement());
            buf.append(" where OWNER_TYPE='dst' and OWNER_ID=").
            append(inParams.add(oldID, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();

        }
        finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param useForce
     */
    public void setUseForce(boolean useForce) {
        this.useForce = useForce;
    }
}
