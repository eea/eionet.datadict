package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;

public class CsItemHandler {

    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;

    private String mode = null;
    private String csi_id = null;
    private String csi_value = null;
    private String csi_type = null;
    private String component_id = null;
    private String component_type = null;
    private String[] delIDs = null;

    private String lastInsertID = null;
    
    private boolean versioning = true;

    public CsItemHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public CsItemHandler(Connection conn, Parameters req, ServletContext ctx){

        this.conn = conn;
        this.req = req;
        this.ctx = ctx;

        mode = req.getParameter("mode");
        delIDs = req.getParameterValues("del_id");
        
        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
        }
    }

    public CsItemHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void setVersioning(boolean f){
        this.versioning = f;
    }

    public void execute() throws Exception {

        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("CsItemHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add")){
            insert();
        }
        else
            delete();

        //delete and update are not implemented yet

       // else if (mode.equalsIgnoreCase("edit"))
            //update();
    }
    private void insert() throws SQLException {

        csi_type = req.getParameter("csi_type");
        component_type = req.getParameter("component_type");
        component_id = req.getParameter("component_id");

        if (csi_type == null)
            throw new SQLException("csi_type must be specified!");
        if (component_type == null)
            throw new SQLException("component_type must be specified!");
        if (component_id == null)
            throw new SQLException("component_id must be specified!");

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CS_ITEM");

        gen.setField("COMPONENT_ID", component_id);
        log(gen.insertStatement());
        if (!Util.nullString(csi_type))
            gen.setField("CSI_TYPE", csi_type);
        if (!Util.nullString(component_type))
            gen.setField("COMPONENT_TYPE", component_type);


        log(gen.insertStatement());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        stmt.close();

        setLastInsertID();
    }

    private void delete() throws Exception {
        
        if (delIDs==null || delIDs.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from CS_ITEM where ");
        for (int i=0; i<delIDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("CSI_ID=");
            buf.append(delIDs[i]);
        }
        
        log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
        
        deleteRelations();
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

        return false;
    }
    private void deleteRelations() throws Exception {

        Parameters pars = new Parameters();
        pars.addParameterValue("mode", "delete_all");
        for (int i=0; i<delIDs.length; i++){
            pars.addParameterValue("del_id", delIDs[i]);
        }
        
        if (component_type!=null)
            pars.addParameterValue("component_type", component_type);

        CsiRelationHandler crHandler = new CsiRelationHandler(conn, pars, ctx);
        crHandler.setVersioning(this.versioning);
        crHandler.execute();
    }

    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}
