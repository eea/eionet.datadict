package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import java.io.File;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;
import eionet.meta.schema.*;

import com.tee.util.*;

public class DataElementHandler {
    
    public static String ATTR_PREFIX = "attr_";
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    private String mode = null;
    private String type = null;
    private String delem_id = null;
    private String[] delem_ids = null;
    private String delem_name = null;
    private String delem_class = null;
    private String lastInsertID = null;
    
    private String ns_id = null;
    
    private String table_id = null;
    
    private String schemaPhysPath = null;
    private String schemaUrlPath = null;
    
    private String basensPath = null;
    
    //private boolean schemaResult = SchemaExp.EXPORT_SUCCESSFUL;
    
    public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DataElementHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");
        this.type = req.getParameter("type");
        this.delem_id = req.getParameter("delem_id");
        this.delem_ids = req.getParameterValues("delem_id");
        this.delem_name = req.getParameter("delem_name");
        this.delem_class = req.getParameter("delem_class");
        this.ns_id = req.getParameter("ns");
        this.table_id = req.getParameter("table_id");
        
        StringBuffer buf = new StringBuffer("http://");
        buf.append(req.getServerName());
		buf.append(":");
		buf.append(String.valueOf(req.getServerPort()));
		buf.append(req.getContextPath());
		this.basensPath = buf.toString();
        
        schemaPhysPath = ctx.getInitParameter("schemas-physical-path");
        schemaUrlPath = ctx.getInitParameter("schemas-url-path");
        
        if (schemaPhysPath == null)
            schemaPhysPath = System.getProperty("user.dir");
        
        if (schemaUrlPath == null){
			schemaUrlPath = buf.toString();
        }
    }
    
    public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("DataElementHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add")){
            if (type==null || (!type.equalsIgnoreCase("AGG") &&
                            !type.equalsIgnoreCase("CH1") &&
                            !type.equalsIgnoreCase("CH2")))
                throw new Exception("DataElementHandler type unspecified!");
        }
        
        if (mode.equalsIgnoreCase("add")){
            insert();
            delem_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }
    
    private void insert() throws SQLException {
        
        if (!Util.nullString(table_id)){
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            DsTable dsTable = searchEngine.getDatasetTable(table_id);
            if (dsTable != null) ns_id = dsTable.getNamespace();
        }
        
        if (delem_name == null || ns_id == null)
            throw new SQLException("Short name or namespace not specified!");
        
        if (exists())
            throw new SQLException("A data element with this name in this namespace already exists!");
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        
        gen.setField("SHORT_NAME", delem_name);
        gen.setField("TYPE", type);
        gen.setField("NAMESPACE_ID", ns_id);
        
        //if (!Util.nullString(table_id))
        //    gen.setField("TABLE_ID", table_id);
        
        String extension = req.getParameter("extends");
        if (extension != null && extension.length()!=0)
            gen.setFieldExpr("EXTENDS", extension);
            
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        setLastInsertID();
        processDataClass();
        processAttributes();
        insertTableElem();
    }
    
    private void update() throws SQLException {
        
        lastInsertID = delem_id;
        
        String elm = req.getParameter("elm");
        if (elm!=null && elm.equals("exs")){
            insertTableElem();
            return;
        }
        
        deleteAttributes();
        processAttributes();
        processDataClass();
    }
    
    private void delete() throws Exception {
        
        if (delem_ids==null || delem_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from DATAELEM where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        deleteTableElem();
        deleteAttributes();
        deleteComplexAttributes();
        deleteFixedValues();
        deleteContents();
        //deleteRelations();
        //deleteContentDefinition();
        processDataClass();
    }
    
    private void deleteContentDefinition() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from CONTENT_DEFINITION where DATAELEM_ID=");
        buf.append(delem_id);
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteAttributes() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where (");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }

        buf.append(") and PARENT_TYPE='E'");

        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteComplexAttributes() throws SQLException {
        
        for (int i=0; delem_ids!=null && i<delem_ids.length; i++){
            
            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("parent_id", delem_ids[i]);
            params.addParameterValue("parent_type", "E");
            
            AttrFieldsHandler attrFieldsHandler = new AttrFieldsHandler(conn, params, ctx);
            try{
                attrFieldsHandler.execute();
            }
            catch (Exception e){
                throw new SQLException(e.toString());
            }
        }
    }
    
    private void deleteRelations() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from RELATION where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("PARENT_ID=");
            buf.append(delem_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteContents() throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select CHILD_ID, CHILD_TYPE from CONTENT where PARENT_TYPE='elm' and (");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("PARENT_ID=");
            buf.append(delem_ids[i]);
        }
        buf.append(")");
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
            String contentID = rs.getString("CHILD_ID");
            String contentType = rs.getString("CHILD_TYPE");
            if (contentID != null && contentType != null){
                buf = null;
                if (contentType.equals("seq")){
                    buf = new StringBuffer("delete from SEQUENCE where SEQUENCE_ID=");
                    buf.append(contentID);
                }
                else if (contentType.equals("chc")){
                    buf = new StringBuffer("delete from CHOICE where CHOICE_ID=");
                    buf.append(contentID);
                }
                
                if (buf == null) continue;
                
                stmt.executeUpdate(buf.toString());
            }
        }
        
        buf = new StringBuffer("delete from CONTENT where PARENT_TYPE='elm' and (");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("PARENT_ID=");
            buf.append(delem_ids[i]);
        }
        buf.append(")");
        
        ctx.log(buf.toString());
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteFixedValues() throws Exception {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct CSI_ID from CS_ITEM where COMPONENT_TYPE='elem' and (");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("COMPONENT_ID=");
            buf.append(delem_ids[i]);
        }
        buf.append(")");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        Parameters pars = new Parameters();
        while (rs.next()){
            pars.addParameterValue("del_id", rs.getString("CSI_ID"));
        }
        stmt.close();
        
        pars.addParameterValue("mode", "delete");
        FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
        fvHandler.execute();
    }
    
    /*private void deleteFixedValues() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from FIXED_VALUE where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }*/
    
    private void insertTableElem() throws SQLException {
        
        if (table_id == null || table_id.length()==0)
            return;
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("TBL2ELEM");
        gen.setField("TABLE_ID", table_id);
        gen.setField("DATAELEM_ID", getLastInsertID());
        
        String position = req.getParameter("pos");
        if (!Util.nullString(position))
            gen.setField("POSITION", position);
        
        String sql = gen.insertStatement();
        log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void deleteTableElem() throws SQLException {
        
        if (delem_ids==null || delem_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from TBL2ELEM where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void processAttributes() throws SQLException {
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()){
            String parName = (String)parNames.nextElement();
            if (!parName.startsWith(ATTR_PREFIX))
                continue;
            String attrValue = req.getParameter(parName);
            if (attrValue.length()==0)
                continue;
            String attrID = parName.substring(ATTR_PREFIX.length());            
            insertAttribute(attrID, attrValue);
        }
    }
    
    private void insertAttribute(String attrId, String value) throws SQLException {
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        
        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setFieldExpr("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "E");
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void updateAttribute(String attrId, String value) throws SQLException {
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        
        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setFieldExpr("DATAELEM_ID", delem_id);
        gen.setFieldExpr("PARENT_TYPE", "E");
        gen.setField("VALUE", value);

        String sql = gen.updateStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void processDataClass() throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        Statement stmt = conn.createStatement();
        if (!mode.equals("add")){
            
            buf.append("delete from CLASS2ELEM where DATAELEM_ID=");
            buf.append(delem_id);
            
            stmt.executeUpdate(buf.toString());
        }
        
        if (!mode.equals("del")){
            
            if (lastInsertID==null || delem_class == null || delem_class.length()==0) return;
            
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("CLASS2ELEM");
            
            gen.setFieldExpr("DATAELEM_ID", lastInsertID);
            gen.setFieldExpr("DATACLASS_ID", delem_class);
        
            stmt.executeUpdate(gen.insertStatement());
        }
    }
    
    private void setLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        
        ctx.log(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        if (rs.next())
            lastInsertID = rs.getString(1);
        stmt.close();
    }
    
    public String getLastInsertID(){
        return lastInsertID;
    }
    
    public boolean exists() throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select count(*) as COUNT from DATAELEM where SHORT_NAME=");
        buf.append(com.tee.util.Util.strLiteral(delem_name));
        buf.append(" and NAMESPACE_ID=");
        buf.append(com.tee.util.Util.strLiteral(ns_id));
        
        /*buf.append(" and TABLE_ID");
        if (Util.nullString(table_id))
            buf.append(" is null");
        else{
            buf.append("=");
            buf.append(table_id);
        }*/
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }
        
        return false;
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}