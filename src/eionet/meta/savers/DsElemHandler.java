package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;

import com.tee.util.*;

public class DsElemHandler {
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String dsID = null;
    
    private DDSearchEngine searchEngine = null;
    
    public DsElemHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DsElemHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.dsID = req.getParameter("ds_id");
    }
    
    public DsElemHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("DsElemHandler mode unspecified!");
        
        if (dsID == null) throw new Exception("DsElemHandler ds_id unspecified!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        String[] elems = req.getParameterValues("elem");
        for (int i=0; elems!=null && i<elems.length; i++){
            insertDsElem(elems[i]);
        }
    }

    private void insertDsElem(String elem_id) throws SQLException {
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS2ELEM");
        gen.setFieldExpr("DATASET_ID", dsID);
        gen.setFieldExpr("DATAELEM_ID", elem_id);
        

        String sql = gen.insertStatement();
        ctx.log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private void delete() throws Exception {

        String[] del_IDs = req.getParameterValues("del_id");
        for (int i=0; del_IDs!=null && i<del_IDs.length; i++){
            deleteDsElem(del_IDs[i]);
        }
    }

    private void deleteDsElem(String elem_id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from DS2ELEM where DATASET_ID=");
        buf.append(dsID);
        buf.append(" and DATAELEM_ID=");
        buf.append(elem_id);

        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}