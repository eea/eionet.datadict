package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import java.io.File;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;
import com.tee.util.*;

public class DataClassHandler {
    
    public static String ATTR_PREFIX = "attr_";
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    private String mode = null;
    private String class_id = null;
    private String[] class_ids = null;
    private String class_name = null;
    private String lastInsertID = null;
    
    private String ns_id = null;
    
    public DataClassHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DataClassHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");
        this.class_id = req.getParameter("class_id");
        this.class_ids = req.getParameterValues("class_id");
        this.class_name = req.getParameter("class_name");
        this.ns_id = req.getParameter("ns");
        
    }

    public DataClassHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("DataClassHandler mode unspecified!");


        if (mode.equalsIgnoreCase("add")){
            insert();
            class_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();    
    }
    
    private void insert() throws SQLException {
        
        if (exists())
            throw new SQLException("A data class with this name in this namespace already exists!");
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATA_CLASS");
        gen.setField("SHORT_NAME", class_name);
        gen.setField("NAMESPACE_ID", ns_id);
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        setLastInsertID();
        processAttributes();
    }
    
    private void update() throws SQLException {
        
        lastInsertID = class_id;
        
        deleteAttributes();
        processAttributes();
    }
    
    private void delete() throws SQLException {
        
        if (class_ids==null || class_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from DATA_CLASS where ");
        for (int i=0; i<class_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATACLASS_ID=");
            buf.append(class_ids[i]);
        }
        
        ctx.log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        deleteAttributes();
        deleteElements();
    }

    private void deleteAttributes() throws SQLException {

        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where PARENT_TYPE='C'");
        for (int i=0; i<class_ids.length; i++){
            if (i==0)
                buf.append(" AND (");
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(class_ids[i]);
            if (i==class_ids.length -1)
                buf.append(")");
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
        gen.setField("PARENT_TYPE", "C");
        gen.setField("VALUE", value);
        
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
        gen.setFieldExpr("DATAELEM_ID", class_id);
        gen.setField("PARENT_TYPE", "C");
        gen.setField("VALUE", value);
        
        String sql = gen.updateStatement();
        ctx.log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void deleteElements() throws SQLException {

        StringBuffer buf = new StringBuffer("delete from CLASS2ELEM where ");
        for (int i=0; i<class_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATACLASS_ID=");
            buf.append(class_ids[i]);
        }

        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
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
        "select count(*) as COUNT from DATA_CLASS where " +
        "SHORT_NAME=" + com.tee.util.Util.strLiteral(class_name);
        
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