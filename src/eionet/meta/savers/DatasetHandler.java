package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;

public class DatasetHandler {
    
    public static String ATTR_PREFIX = "attr_";
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String ds_id = null;
    private String[] ds_ids = null;
    private String ds_name = null;
    private String version = null;
    private String lastInsertID = null;
    
    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DatasetHandler(Connection conn, Parameters req, ServletContext ctx){
        
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        
        this.mode = req.getParameter("mode");
        this.ds_id = req.getParameter("ds_id");
        this.ds_ids = req.getParameterValues("ds_id");
        this.ds_name = req.getParameter("ds_name");
        this.version = req.getParameter("version");
    }
    
    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("DatasetHandler mode unspecified!");
            
        if (mode.equalsIgnoreCase("add")){
            insert();
            ds_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }
    
    private void insert() throws SQLException {
        
        if (ds_name == null || version == null)
            throw new SQLException("Short name and version must be specified!");
        
        if (exists())
            throw new SQLException("A dataset with this short name and version already exists!");
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATASET");
        
        gen.setField("SHORT_NAME", ds_name);
        gen.setField("VERSION", version);
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        setLastInsertID();
        processAttributes();
    }
    
    private void update() throws SQLException {
        
        lastInsertID = ds_id;
        
        deleteAttributes();
        processAttributes();
    }
    
    private void delete() throws SQLException {
        
        if (ds_ids==null || ds_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from DATASET where ");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        deleteAttributes();
        deleteComplexAttributes();
        deleteTablesElems();
    }
    
    private void deleteAttributes() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where (");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(ds_ids[i]);
        }
        
        buf.append(") and PARENT_TYPE='DS'");
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteComplexAttributes() throws SQLException {
        
        for (int i=0; ds_ids!=null && i<ds_ids.length; i++){
            
            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("parent_id", ds_ids[i]);
            params.addParameterValue("parent_type", "DS");
            
            AttrFieldsHandler attrFieldsHandler = new AttrFieldsHandler(conn, params, ctx);
            try{
                attrFieldsHandler.execute();
            }
            catch (Exception e){
                throw new SQLException(e.toString());
            }
        }
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
        gen.setField("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "DS");
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void deleteTablesElems() throws SQLException {
        
        // get the tables
        
        StringBuffer buf = new StringBuffer("select distinct TABLE_ID from DS_TABLE where ");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        Vector tables = new Vector();
        while (rs.next()){
            tables.add(rs.getString("TABLE_ID"));
        }
        
        stmt.close();
        
        // delete the tables
        
        if (tables.size() == 0) return;
        
        Parameters params = new Parameters();
        params.addParameterValue("mode", "delete");
        for (int i=0; i<tables.size(); i++){
            params.addParameterValue("del_id", (String)tables.get(i));
        }
                
        DsTableHandler tableHandler = new DsTableHandler(conn, params, ctx);
        try{ tableHandler.execute(); } catch (Exception e){
            throw new SQLException(e.toString());
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
        
        String qry =
        "select count(*) as COUNT from DATASET " +
        "where SHORT_NAME=" + com.tee.util.Util.strLiteral(ds_name) +
        " and VERSION=" + com.tee.util.Util.strLiteral(version);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
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