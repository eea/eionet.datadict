package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.File;

import eionet.meta.DataElement;
import eionet.meta.Namespace;
import eionet.meta.DDSearchEngine;

import com.tee.util.*;

public class Class2ElemHandler {
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    private String mode = null;
    private String class_id = null;
    private String class_name = null;
    private String ns = null;
    
    private DDSearchEngine searchEngine = null;
    
    public Class2ElemHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public Class2ElemHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.class_id = req.getParameter("class_id");
        this.class_name = req.getParameter("class_name");
        this.ns = req.getParameter("ns");

		this.searchEngine = new DDSearchEngine(conn, req.getID(), ctx);
    }
    
    public Class2ElemHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("Class2ElemHandler mode unspecified!");
        
        if (class_id == null) throw new Exception("Class2ElemHandler class_id unspecified!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        String[] dataElem = req.getParameterValues("element");
        if (dataElem!=null){
            
            for (int i=0; i<dataElem.length; i++){
                insertClass2Elem(dataElem[i]);
            }
        }
    }

    private void insertClass2Elem(String elem_id) throws SQLException {
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CLASS2ELEM");
        gen.setFieldExpr("DATAELEM_ID", elem_id);
        gen.setFieldExpr("DATACLASS_ID", class_id);

        String sql = gen.insertStatement();
        ctx.log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }

    private void delete() throws Exception {

        String[] del_IDs = req.getParameterValues("del_id");
        if (del_IDs == null || del_IDs.length == 0) return;

        for (int i=0; i<del_IDs.length; i++){
            deleteClass2Elem(del_IDs[i]);
        }
    }

    private void deleteClass2Elem(String elem_id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from CLASS2ELEM where DATACLASS_ID=");
        buf.append(class_id);
        buf.append(" and DATAELEM_ID=");
        buf.append(elem_id);

        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}