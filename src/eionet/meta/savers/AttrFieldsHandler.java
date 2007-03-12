package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import com.tee.util.*;
import eionet.meta.DDSearchEngine;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;

public class AttrFieldsHandler {

    public static final String FLD_PREFIX = "field_";
    private static LogServiceIF logger = new Log4jLoggerImpl();

    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    String mode = null;
    String parent_id = null;
    String parent_type = null;
    String m_attr_id = null;
    String[] del_rows = null;
    String[] del_attrs = null;

    private boolean versioning = true;
    
    private String harvAttrID = null;

    public AttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public AttrFieldsHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.parent_id = req.getParameter("parent_id");
        this.parent_type = req.getParameter("parent_type");
        this.m_attr_id = req.getParameter("attr_id");
        this.del_rows = req.getParameterValues("del_row");
        this.del_attrs = req.getParameterValues("del_attr");
        
		harvAttrID = req.getParameter("harv_attr_id");

        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
        }
    }

    public AttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }

    public void setVersioning(boolean f){
        this.versioning = f;
    }

    public void execute() throws Exception {
    	
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("AttrFieldsHandler mode unspecified!");
        
        if (parent_type!=null){
            
            // if in versioning mode, we cannot edit attributes of
            // non-working copies
                
            String _type = null;
            if (parent_type.equals("E"))
                _type = "elm";
            else if (parent_type.equals("T"))
                _type = "tbl";
            else if (parent_type.equals("DS"))
                _type = "dst";
        }

        if (mode.equalsIgnoreCase("add")){
            if (m_attr_id == null) throw new Exception("AttrFieldsHandler: attribute id not specified!");
            if (parent_id == null) throw new Exception("AttrFieldsHandler: parent id not specified!");
            if (parent_type == null) throw new Exception("AttrFieldsHandler: parent type not specified!");
            insert();
        }
        else{
            if (del_rows == null || del_rows.length==0)
                if (del_attrs == null || del_attrs.length==0)
                    if (parent_id == null && parent_type == null)
                        throw new Exception("AttrFieldsHandler: no rows, no attributes, no parents for deletion specified!");
            delete();
        }
    }

    private void insert() throws Exception {
    	
        Enumeration params = req.getParameterNames();
        if (params == null || !params.hasMoreElements()) return;

        if (Util.nullString(harvAttrID) && !hasFields()) return;

        String row_id = insertRow();
		if (Util.nullString(harvAttrID))
        	insertFields(row_id, params);
    }

    private String insertRow() throws SQLException {

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("COMPLEX_ATTR_ROW");

        gen.setField("M_COMPLEX_ATTR_ID", m_attr_id);
        gen.setField("PARENT_ID", parent_id);
        gen.setField("PARENT_TYPE", parent_type);

        String position = req.getParameter("position");
        if (position == null || position.length()==0) position = "0";
        gen.setField("POSITION", position);


        String rowID = "md5('" + parent_id + parent_type + m_attr_id + position + "')";
        gen.setFieldExpr("ROW_ID", rowID);
        
        if (!Util.nullString(harvAttrID))
			gen.setField("HARV_ATTR_ID", harvAttrID);

        String sql = gen.insertStatement();

        logger.debug(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();

        return rowID;
    }

    private void insertFields(String row_id, Enumeration params) throws SQLException {

        if (row_id == null) return;

        do {
            String parName = (String)params.nextElement();
            if (!parName.startsWith(FLD_PREFIX)) continue;

            if (Util.nullString(req.getParameter(parName))) continue;

            String fieldID = parName.substring(FLD_PREFIX.length());

            SQLGenerator gen = new SQLGenerator();
            gen.setTable("COMPLEX_ATTR_FIELD");

            gen.setFieldExpr("ROW_ID", row_id);
            gen.setField("M_COMPLEX_ATTR_FIELD_ID", fieldID);
            gen.setField("VALUE", req.getParameter(parName));

            String sql = gen.insertStatement();
            logger.debug(sql);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
        while (params.hasMoreElements());
    }
    
    private void delete() throws SQLException {
        
        if (del_rows == null || del_rows.length == 0)
            setDelRows();
        
        if (del_rows == null || del_rows.length == 0)
            return;
        
        StringBuffer bufRow = new StringBuffer();
        bufRow.append("delete from COMPLEX_ATTR_ROW where ");
        
        for (int i=0; del_rows!=null && i<del_rows.length; i++){
            if (i>0) bufRow.append(" or ");
            bufRow.append("ROW_ID='");
            bufRow.append(del_rows[i]);
            bufRow.append("'");
        }
        
        StringBuffer bufFld = new StringBuffer();
        bufFld.append("delete from COMPLEX_ATTR_FIELD where ");
        
        for (int i=0; del_rows!=null && i<del_rows.length; i++){
            if (i>0) bufFld.append(" or ");
            bufFld.append("ROW_ID='");
            bufFld.append(del_rows[i]);
            bufFld.append("'");
        }
        
        Statement stmt = conn.createStatement();
        
        logger.debug(bufRow.toString());
        stmt.executeUpdate(bufRow.toString());
        
        logger.debug(bufFld.toString());
        stmt.executeUpdate(bufFld.toString());
        
        stmt.close();
    }
    
    private void setDelRows() throws SQLException {
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_ID=");
        buf.append(parent_id);
        buf.append(" and PARENT_TYPE='");
        buf.append(parent_type);
        buf.append("'");
        
        if (del_attrs != null && del_attrs.length != 0){
            buf.append(" and ("); 
        
            for (int i=0; i<del_attrs.length; i++){
                if (i>0) buf.append(" or ");
                buf.append("M_COMPLEX_ATTR_ID=");
                buf.append(del_attrs[i]);
            }
                
            buf.append(")");
        }
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        Vector v = new Vector();
        while (rs.next()){
            v.add(rs.getString("ROW_ID"));
        }

        stmt.close();

        del_rows = new String[v.size()];
        for (int i=0; i<v.size(); i++)
            del_rows[i] = (String)v.get(i);
    }
    private boolean hasFields(){
        Enumeration pars = req.getParameterNames();
        do {
            String parName = (String)pars.nextElement();
            if (!parName.startsWith(FLD_PREFIX)) continue;

            if (Util.nullString(req.getParameter(parName))) continue;

            return true;
        }
        while (pars.hasMoreElements());

        return false;

    }
}
