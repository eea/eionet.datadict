package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;
import eionet.meta.DDSearchEngine;

public class CsiRelationHandler {

    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;

    private String mode = null;
    private String parent_id = null;
    private String child_id = null;
    private String rel_type = null;
    private String rel_description = null;
    private String parentcomp_id = null;
    private String childcomp_id = null;
    private String csi_type = null;
    private String component_type = null;
    private String[] delIDs = null;
    
    private boolean versioning = true;

    public CsiRelationHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public CsiRelationHandler(Connection conn, Parameters req, ServletContext ctx){

        this.conn = conn;
        this.req = req;
        this.ctx = ctx;

        mode = req.getParameter("mode");
        delIDs = req.getParameterValues("del_id");
        parent_id = req.getParameter("parent_id");
        child_id = req.getParameter("child_id");
        rel_type = req.getParameter("rel_type");
        rel_description = req.getParameter("rel_description");
        csi_type = req.getParameter("csi_type");
        component_type = req.getParameter("component_type");
        parentcomp_id = req.getParameter("parentcomp_id");
        childcomp_id = req.getParameter("childcomp_id");
        
        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
        }
    }

    public CsiRelationHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void setVersioning(boolean f){
        this.versioning = f;
    }

    public void execute() throws Exception {

        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete_all") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("CsiRelationHandler mode unspecified!");

        if (component_type!=null && component_type.equals("elem")){
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            boolean wc = true;
            try{ wc = searchEngine.isWorkingCopy(parentcomp_id, "elm"); }
            catch (Exception e){}
            if (!wc && versioning)
                throw new Exception("Cannot edit relations of a " +
                        "non-working copy!");
        }
        
        if (mode.equalsIgnoreCase("add")){
            insert();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else if (mode.equalsIgnoreCase("delete_all"))
            deleteAll();
        else
            delete();
    }

    private void insert() throws SQLException {

        if (parent_id==null
            || csItemExists(parent_id)==false){
            if(csi_type.equals("elem")){
                parent_id = getCsItemId(parentcomp_id, true);
            }
        }
        if (child_id==null
            || csItemExists(child_id)==false){
            if(csi_type.equals("elem")){
                child_id = getCsItemId(childcomp_id, true);
            }
        }

        if (parent_id == null)
            throw new SQLException("Parent item id must be specified!");
        if (child_id == null)
            throw new SQLException("Child item id must be specified!");
        if (exists())
            throw new SQLException("Such relation already exists!");

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CSI_RELATION");

        gen.setField("PARENT_CSI", parent_id);
        gen.setField("CHILD_CSI", child_id);
        if (!Util.nullString(rel_type))
            gen.setField("REL_TYPE", rel_type);
        if (!Util.nullString(rel_description))
            gen.setField("REL_DESCRIPTION", rel_description);

        log(gen.insertStatement());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        stmt.close();

    }

    private void update() throws SQLException {

        if (parent_id==null){
            if(csi_type.equals("elem")){
                parent_id = getCsItemId(parentcomp_id, true);
            }
        }
        if (parent_id == null)
            throw new SQLException("Parent item id must be specified!");

        if (Util.nullString(child_id))
            throw new SQLException("Child Item ID is not specified!");

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CSI_RELATION");

        gen.setField("REL_DESCRIPTION", rel_description);

        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append(" where PARENT_CSI=");
        buf.append(parent_id);
        buf.append(" and CHILD_CSI=");
        buf.append(child_id);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }

    private void delete() throws Exception {

        if (delIDs==null || delIDs.length==0)
            return;

        if (parent_id==null || parent_id.length()==0){
            if(csi_type.equals("elem")){
                parent_id = getCsItemId(parentcomp_id, false);
            }
        }

        if (parent_id == null)
            throw new SQLException("Parent item id must be specified!");

        StringBuffer buf = new StringBuffer("delete from CSI_RELATION where ");
        buf.append("PARENT_CSI=");
        buf.append(parent_id);
        buf.append(" and (");
        for (int i=0; i<delIDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("CHILD_CSI=");
            buf.append(delIDs[i]);
        }
        buf.append(")");

        log(buf.toString());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();

        deleteFxvRelations();
    }


    private void deleteAll() throws SQLException {

        if (delIDs==null || delIDs.length==0)
            return;

        StringBuffer buf = new StringBuffer("delete from CSI_RELATION where ");
        for (int i=0; i<delIDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("CHILD_CSI=");
            buf.append(delIDs[i]);
            buf.append(" or PARENT_CSI=");
            buf.append(delIDs[i]);
        }

        log(buf.toString());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }

    private void deleteFxvRelations() throws SQLException {

        int counter=0;
        //get related elements' fixed values
        StringBuffer bufQ = new StringBuffer("select FIXED_VALUE.CSI_ID from CS_ITEM AS PARENT_CSI ");
        bufQ.append("right outer join CS_ITEM AS FIXED_VALUE on PARENT_CSI.COMPONENT_ID=FIXED_VALUE.COMPONENT_ID ");
        bufQ.append("where FIXED_VALUE.CSI_TYPE='fxv' and (");
        for (int i=0; i<delIDs.length; i++){
            if (i>0)
                bufQ.append(" or ");
            bufQ.append("PARENT_CSI.CSI_ID=");
            bufQ.append(delIDs[i]);
        }
        bufQ.append(")");
        log(bufQ.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(bufQ.toString());

        StringBuffer bufRelFxvId = new StringBuffer();
        while (rs.next()){
            if (rs.getInt("csi_id")>0){
                counter++;
                if (counter>1) bufRelFxvId.append(" or ");
                bufRelFxvId.append("CHILD_CSI=");
                bufRelFxvId.append(rs.getString("csi_id"));
            }
        }
        if (bufRelFxvId.length()==0) return;

        //get parent element fixed values
        StringBuffer bufQry = new StringBuffer("select csi_id from CS_ITEM where csi_type='fxv' and COMPONENT_TYPE='elem' and ");
        bufQry.append("COMPONENT_ID=");
        bufQry.append(parentcomp_id);

        log(bufQry.toString());

        rs = stmt.executeQuery(bufQry.toString());

        StringBuffer bufFxvId = new StringBuffer();
        counter=0;
        while (rs.next()){
            if (rs.getInt("csi_id")>0){
                counter++;
                if (counter>1) bufFxvId.append(" or ");
                bufFxvId.append("PARENT_CSI=");
                bufFxvId.append(rs.getString("csi_id"));
            }
        }
        if (bufFxvId.length()==0) return;

        //log(bufFxvId.toString());
        //log(bufRelFxvId.toString());

        //delete all relations between fixed values
        StringBuffer buf = new StringBuffer("delete from CSI_RELATION where REL_TYPE='abstract' and (");
        buf.append(bufFxvId.toString());
        buf.append(") and (");
        buf.append(bufRelFxvId.toString());
        buf.append(")");

        log(buf.toString());
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    public String getCsItemId(String id, boolean addNew) throws SQLException {
        String qry =
        "select csi_id from CS_ITEM " +
        "where COMPONENT_ID=" + id +
        " and COMPONENT_TYPE=" + com.tee.util.Util.strLiteral(component_type) +
        " and CSI_TYPE=" + com.tee.util.Util.strLiteral(csi_type);

        log(qry);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        if (rs.next()){
            if (rs.getInt("csi_id")>0){
                return rs.getString("csi_id");
            }
        }
        
        stmt.close();
        
        if (addNew){
          try{
            Parameters params = new Parameters();
            params.addParameterValue("mode", "add");
            params.addParameterValue("component_id", id);
            params.addParameterValue("component_type", component_type);
            params.addParameterValue("csi_type", csi_type);

            CsItemHandler csiHandler = new CsItemHandler(conn, params, ctx);
            csiHandler.setVersioning(this.versioning);
            csiHandler.execute();
            return csiHandler.getLastInsertID();
          }
          catch(Exception e){
              throw new SQLException(e.toString());
          }
        }
        return null;
    }
    public boolean exists() throws SQLException {

        String qry =
        "select count(*) as COUNT from CSI_RELATION " +
        "where PARENT_CSI=" + parent_id +
        " and CHILD_CSI=" + child_id;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }
        
        stmt.close();

        return false;
    }
    public boolean csItemExists(String id) throws SQLException {

        if (id==null || id.length()==0) return false;
        
        String qry =
        "select count(*) as COUNT from CS_ITEM " +
        "where CSI_ID=" + id +
        " and CSI_TYPE=" + com.tee.util.Util.strLiteral(csi_type) +
        " and COMPONENT_TYPE=" + com.tee.util.Util.strLiteral(component_type);

        log(qry);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }
        
        stmt.close();

        return false;
    }

    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}
