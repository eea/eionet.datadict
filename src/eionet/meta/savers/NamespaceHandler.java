package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.tee.util.*;

public class NamespaceHandler {
    
    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String[] nsID = null;
    private String shortName = null;
    private String fullName = null;
    private String definition = null;
    private String dsID = null;
    private String tblID = null;
    private String dsName = null;
    private String tblName = null;
    
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
        this.shortName = req.getParameter("short_name");
        this.fullName = req.getParameter("fullName");
        this.definition = req.getParameter("description");
        this.dsID = req.getParameter("ds_id");
        this.tblID = req.getParameter("tbl_id");
        
        this.dsName = req.getParameter("ds_name");
        this.tblName = req.getParameter("tbl_name");
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
        
        // at least the short_name is required
        if (Util.nullString(shortName))
            throw new Exception("NamespaceHandler: at least the short_name " +
                                        "is required!");
        
        /* if no dataset and table, there must be a short name
        if (Util.nullString(dsID) && Util.nullString(tblID) && Util.nullString(shortName))
            throw new Exception("If dataset and table are not specified then at least short name is needed!");
        
        // if no dataset and table, there must be a short name
        if (exists())
            throw new Exception("A namespace with such credentials already exists!");
        
        // if table or dataset is specified, overwrite the names and definition
        if (!Util.nullString(tblID)){
            
            if (Util.nullString(dsID))
                throw new Exception("Datset ID missing!");
                
            shortName = "ds" + dsID + "tb" + tblID;
            if (tblName!=null){
                fullName = tblName + " table";
                definition = "The namespace of " + tblName + " table";
                if (dsName != null){
                    fullName = fullName + " in " + dsName + " dataset";
                    definition = definition + " in " + dsName + " dataset";
                }
            }
        }
        else if (!Util.nullString(dsID)){
            shortName = "ds" + dsID;
            if (dsName != null){
                fullName = dsName + " dataset";
                definition = "The namespace of " + dsName + " dataset";
            }
        }*/
        
        // build SQL
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        if (!Util.nullString(fullName))
            gen.setField("FULL_NAME", fullName);
        if (!Util.nullString(shortName))
            gen.setField("SHORT_NAME", shortName);
        if (!Util.nullString(definition))
            gen.setField("DEFINITION", definition);
        /*if (!Util.nullString(dsID))
            gen.setField("DATASET_ID", dsID);
        if (!Util.nullString(tblID))
            gen.setField("TABLE_ID", tblID);*/

        String wrkUser = req.getParameter("wrk_user");
        if (!Util.nullString(wrkUser))
            gen.setField("WORKING_USER", wrkUser);
        else
            gen.setFieldExpr("WORKING_USER", "NULL");
            
        // execute
        String sql = gen.insertStatement();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
        
        setLastInsertID();
    }
    
    private void update() throws Exception {
        
        if (nsID == null || nsID.length==0)
            throw new Exception("Namespace ID not specified!");
            
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        // don't allow change of SHORT_NAME in the first approach
        //if (!Util.nullString(shortName))
            //gen.setField("SHORT_NAME", shortName);
        if (!Util.nullString(fullName))
            gen.setField("FULL_NAME", fullName);
        if (!Util.nullString(definition))
            gen.setField("DEFINITION", definition);
        
        String wrkUser = req.getParameter("wrk_user");
        if (!Util.nullString(wrkUser))
            gen.setField("WORKING_USER", wrkUser);
        else
            gen.setFieldExpr("WORKING_USER", "NULL");
        
        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append(" where NAMESPACE_ID=");
        buf.append(nsID[0]);
        
        log(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        lastInsertID = nsID[0];
    }
    
    private void delete() throws Exception {
        
        // don't allow manual deletion of namespaces in the first approach
        return;
        
        /*if (nsID==null || nsID.length==0)
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
        stmt.close();*/
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
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
    
    private boolean exists() throws SQLException {
        
        String qry = "";
        if (Util.nullString(dsID) && Util.nullString(tblID)){
            
            qry = "select count(*) as COUNT from NAMESPACE where " +
                    "DATASET_ID is null and TABLE_ID is null and SHORT_NAME=" +
                    Util.strLiteral(shortName);
        }
        else if (!Util.nullString(tblID)){
            
            qry = "select count(*) as COUNT from NAMESPACE where " +
                    "TABLE_ID=" + tblID;
        }
        else{
            
            qry = "select count(*) as COUNT from NAMESPACE where " +
                    "TABLE_ID is null and DATASET_ID=" + dsID;
        }
        
        log(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
            
        if (rs.next())
            if (rs.getInt("COUNT")>0)
                return true;
        
        stmt.close();
        
        return false;
    }
}