package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import eionet.meta.DElemAttribute;

import com.tee.util.*;

public class NamespaceHandler {
    
    public static final String BASENS = "basens";
    
    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String[] nsID = null;
    private String fullName = null;
    private String description = null;
    
    private String lastInsertID = null;
    
    public NamespaceHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public NamespaceHandler(Connection conn, Parameters req, ServletContext ctx){
        
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        
        this.mode = req.getParameter("mode");
        this.nsID = req.getParameterValues("ns_id");
        this.fullName = req.getParameter("fullName");
        this.description = req.getParameter("description");
    }
    
    public NamespaceHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("NamespaceHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        if (nsID==null || nsID.length==0)
            throw new Exception("NamespaceHandler: short name not specified!");
        
        if (nsID[0].equals(BASENS))
            throw new Exception("NamespaceHandler: " + BASENS + " is a reserved namespace!");
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        gen.setField("NAMESPACE_ID", nsID[0]);
        if (!Util.nullString(fullName))
            gen.setField("FULL_NAME", fullName);
        if (!Util.nullString(description))
            gen.setField("DESCRIPTION", description);
        
        String sql = gen.insertStatement();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
        
        setLastInsertID();
    }
    
    private void update() throws Exception {
        
        if (nsID==null || nsID.length==0)
            throw new Exception("NamespaceHandler: short name not specified!");
        
        if (nsID[0].equals(BASENS))
            throw new Exception("NamespaceHandler: " + BASENS + " is a reserved namespace, it cannot be updated!");
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        if (!Util.nullString(fullName))
            gen.setField("FULL_NAME", fullName);
        if (!Util.nullString(description))
            gen.setField("DESCRIPTION", description);
        
        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append(" where NAMESPACE_ID=");
        buf.append(com.tee.util.Util.strLiteral(nsID[0]));
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        setLastInsertID();
    }
    
    private void delete() throws Exception {
        
        if (nsID==null || nsID.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from NAMESPACE where ");
        for (int i=0; i<nsID.length; i++){
            
            if (nsID[i].equals(BASENS))
                throw new Exception("NamespaceHandler: " + BASENS + " is reserved, it cannot be deleted");
            
            if (i>0) buf.append(" or ");
            buf.append("NAMESPACE_ID=");
            buf.append(com.tee.util.Util.strLiteral(nsID[i]));
        }
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void setLastInsertID() throws SQLException {
        lastInsertID = nsID[0];
    }
    
    public String getLastInsertID(){
        return lastInsertID;
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}