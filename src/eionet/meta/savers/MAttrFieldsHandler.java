package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Vector;

import com.tee.util.*;

public class MAttrFieldsHandler {
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    String mode = null;
    String attr_id = null;
    String attr_name = null;
    String attr_ns = null;
    
    public MAttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public MAttrFieldsHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.attr_id = req.getParameter("attr_id");
        this.attr_name = req.getParameter("attr_name");
        this.attr_ns = req.getParameter("attr_ns");
    }
    
    public MAttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("MAttrFieldsHandler mode unspecified!");
        
        if (attr_id == null) throw new Exception("MAttrFieldsHandler attr_id unspecified!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        String[] newFields = req.getParameterValues("new_field");
        if (newFields!=null){
            for (int i=0; i<newFields.length; i++){
                insertField(newFields[i]);
            }
        }
    }
    
    private void insertField(String field) throws SQLException {
        
        String definition = req.getParameter("definition");
        if (definition == null) definition = "";
        
        String position = req.getParameter("position");
        if (position == null || position.length()==0) position = "0";
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("M_COMPLEX_ATTR_FIELD");
        
        gen.setField("M_COMPLEX_ATTR_ID", attr_id);
        gen.setField("NAME", field);
        gen.setField("DEFINITION", definition);
        gen.setField("POSITION", position);
        
        String sql = gen.insertStatement();
                                    
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }
    
    private void delete() throws Exception {
        
        String[] del_Fields = req.getParameterValues("del_field");
        if (del_Fields == null || del_Fields.length == 0) return;
                
        for (int i=0; i<del_Fields.length; i++){
            deleteField(del_Fields[i]);
        }
    }
    
    private void deleteField(String id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(id);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        
        buf = new StringBuffer("delete from COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(id);
        
        stmt.executeUpdate(buf.toString());
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}
