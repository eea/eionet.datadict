
package eionet.meta.savers;

import java.util.*;
import java.sql.*;

import javax.servlet.ServletContext;

public abstract class BaseHandler {
    
    public static final String ADD  = "add";
    public static final String EDIT = "edit";
    public static final String DEL  = "delete";
    
    protected Connection conn = null;
    protected Parameters req = null;
    protected ServletContext ctx = null;
    
    protected HashSet legalModes = new HashSet();
    
    protected String mode = null;
    protected String lastInsertID = null;
    
    /*public BaseHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public BaseHandler(Connection conn, Parameters req, ServletContext ctx){
        
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        
        this.mode = req.getParameter("mode");
        
        this.legalModes.add(ADD);
        this.legalModes.add(EDIT);
        this.legalModes.add(DEL);
        
        init();
    }
    
    protected void init(){
    }*/
    
    public final void execute() throws Exception {
        
        if (mode == null || !legalModes.contains(mode))
            throw new Exception("Handler mode not specified!");
        
        beforeExecute();
        
        if (mode.equalsIgnoreCase(ADD))
            insert();
        else if (mode.equalsIgnoreCase(EDIT)){
            update();
        }
        else if (mode.equalsIgnoreCase(DEL))
            delete();
        
		afterExecute();
    }
    
    //protected abstract String[] getThis();
    
	protected abstract void beforeExecute() throws Exception;
	protected abstract void afterExecute() throws Exception;
    
    protected abstract void insert() throws Exception;
    
    protected abstract void update() throws Exception;
    
    protected abstract void delete() throws Exception;
    
    protected void setLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        if (rs.next())
            lastInsertID = rs.getString(1);
            
        stmt.close();
    }
    
    protected void setLastInsertID(String id){
        this.lastInsertID = id;
    }
    
    public String getLastInsertID(){
        return this.lastInsertID;
    }
    
    protected void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}