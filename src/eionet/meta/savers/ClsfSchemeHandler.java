package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;

public class ClsfSchemeHandler {
    
    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String id = null;
    private String[] delIDs = null;
    private String name = null;
    private String version = null;
    private String type = null;
    private String description = null;
    
    private String lastInsertID = null;
    
    public ClsfSchemeHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public ClsfSchemeHandler(Connection conn, Parameters req, ServletContext ctx){
        
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        
        mode = req.getParameter("mode");
        id = req.getParameter("id");
        delIDs = req.getParameterValues("del_id");
        name = req.getParameter("name");
        version = req.getParameter("version");
        type = req.getParameter("type");
        description = req.getParameter("description");
    }
    
    public ClsfSchemeHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("ClsfSchemeHandler mode unspecified!");
            
        if (mode.equalsIgnoreCase("add")){
            insert();
            id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }
    
    private void insert() throws SQLException {
        
        if (name == null || version == null)
            throw new SQLException("Name and version must be specified!");
        
        if (exists())
            throw new SQLException("Such a classification scheme already exists!");
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CLSF_SCHEME");
        
        gen.setField("CS_NAME", name);
        gen.setField("CS_VERSION", version);
        if (!Util.nullString(type))
            gen.setField("CS_TYPE", type);
        if (!Util.nullString(description))
            gen.setField("CS_DESCRIPTION", description);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        stmt.close();
        
        setLastInsertID();
    }
    
    private void update() throws SQLException {
        
        if (Util.nullString(id))
            throw new SQLException("ID not specified!");

        if (Util.nullString(type) || Util.nullString(description))
            return;
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CLSF_SCHEME");
        
        gen.setField("CS_TYPE", type);
        gen.setField("CS_DESCRIPTION", description);
        
        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append(" where CS_ID=");
        buf.append(id);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void delete() throws Exception {
        
        if (delIDs==null || delIDs.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from CLSF_SCHEME where ");
        for (int i=0; i<delIDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("CS_ID=");
            buf.append(delIDs[i]);
        }
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        deleteItems();
    }
    
    private void deleteItems() throws Exception {
        
        Parameters pars = new Parameters();
        pars.addParameterValue("mode", "delete");
        for (int i=0; i<delIDs.length; i++){
            pars.addParameterValue("del_id", delIDs[i]);
        }
        
        FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
        fvHandler.execute();
    }
   
    private void setLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        
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
        "select count(*) as COUNT from CLSF_SCHEME " +
        "where CS_NAME=" + com.tee.util.Util.strLiteral(name) +
        " and CS_VERSION=" + com.tee.util.Util.strLiteral(version);
        
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