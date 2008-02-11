package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import com.tee.util.*;
import eionet.meta.DDSearchEngine;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public class AttrFieldsHandler extends BaseHandler {

    public static final String FLD_PREFIX = "field_";

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

    public void execute_() throws Exception {
    	
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

    /**
     * 
     * @return
     * @throws SQLException
     */
    private String insertRow() throws SQLException {

        INParameters inParams = new INParameters();
        LinkedHashMap map = new LinkedHashMap();
        map.put("M_COMPLEX_ATTR_ID", inParams.add(m_attr_id, Types.INTEGER));
        map.put("PARENT_ID", inParams.add(parent_id, Types.INTEGER));
        map.put("PARENT_TYPE", inParams.add(parent_type));

        String position = req.getParameter("position");
        if (position == null || position.length()==0)
        	position = "0";
        map.put("POSITION", inParams.add(position, Types.INTEGER));

        String rowID = parent_id + parent_type + m_attr_id + position;
        map.put("ROW_ID", "md5(" + inParams.add(rowID) + ")");
        if (!Util.nullString(harvAttrID))
			map.put("HARV_ATTR_ID", inParams.add(harvAttrID));

        PreparedStatement stmt = null;
        try{
        	stmt = SQL.preparedStatement(SQL.insertStatement("COMPLEX_ATTR_ROW", map), inParams, conn);
        	stmt.executeUpdate();
        }
        finally{
        	try{
        		if (stmt!=null) stmt.close();
        	}
        	catch (SQLException e){}
        }
        
        return rowID;
    }

    /**
     * 
     * @param rowID
     * @param params
     * @throws SQLException
     */
    private void insertFields(String rowID, Enumeration params) throws SQLException {

        if (rowID == null)
        	return;

        PreparedStatement stmt = null;
        try{
	        do {
	            String parName = (String)params.nextElement();
	            if (!parName.startsWith(FLD_PREFIX))
	            	continue;
	
	            if (Util.nullString(req.getParameter(parName)))
	            	continue;
	
	            String fieldID = parName.substring(FLD_PREFIX.length());

	            INParameters inParams = new INParameters();
	            LinkedHashMap map = new LinkedHashMap();
	            map.put("ROW_ID", "md5(" + inParams.add(rowID) + ")");
	            map.put("M_COMPLEX_ATTR_FIELD_ID", inParams.add(fieldID, Types.INTEGER));
	            map.put("VALUE", inParams.add(req.getParameter(parName)));
	            
	            stmt = SQL.preparedStatement(SQL.insertStatement("COMPLEX_ATTR_FIELD", map), inParams, conn);
	            stmt.executeUpdate();
	        }
	        while (params.hasMoreElements());
        }
        catch (SQLException sqle){
        	sqle.printStackTrace(System.out);
        }
        finally{
        	try{
        		if (stmt!=null) stmt.close();
        	}
        	catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @throws SQLException
     */
    private void delete() throws SQLException {
        
        if (del_rows == null || del_rows.length == 0)
            setDelRows();
        if (del_rows == null || del_rows.length == 0)
            return;

        INParameters inParamsRow = new INParameters();
        StringBuffer bufRow = new StringBuffer();
        bufRow.append("delete from COMPLEX_ATTR_ROW where ");
        for (int i=0; del_rows!=null && i<del_rows.length; i++){
            if (i>0)
            	bufRow.append(" or ");
            bufRow.append("ROW_ID=").append(inParamsRow.add(del_rows[i]));
        }
        
        INParameters inParamsFld = new INParameters();
        StringBuffer bufFld = new StringBuffer();
        bufFld.append("delete from COMPLEX_ATTR_FIELD where ");
        for (int i=0; del_rows!=null && i<del_rows.length; i++){
            if (i>0)
            	bufFld.append(" or ");
            bufFld.append("ROW_ID=").append(inParamsFld.add(del_rows[i]));
        }
        
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try{
        	stmt1 = SQL.preparedStatement(bufRow.toString(), inParamsRow, conn);
        	stmt2 = SQL.preparedStatement(bufFld.toString(), inParamsFld, conn);
        	stmt1.executeUpdate();
        	stmt2.executeUpdate();
        }
        finally{
        	try{
        		if (stmt1!=null) stmt1.close();
        		if (stmt2!=null) stmt2.close();
        	}
        	catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @throws SQLException
     */
    private void setDelRows() throws SQLException {
        
    	INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_ID=").append(inParams.add(parent_id, Types.INTEGER)).
        append(" and PARENT_TYPE=").append(inParams.add(parent_type));
        if (del_attrs != null && del_attrs.length != 0){
            buf.append(" and ("); 
            for (int i=0; i<del_attrs.length; i++){
                if (i>0)
                	buf.append(" or ");
                buf.append("M_COMPLEX_ATTR_ID=").append(inParams.add(del_attrs[i], Types.INTEGER));
            }
            buf.append(")");
        }
        
        Vector v = new Vector();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try{
        	stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        	rs = stmt.executeQuery();
            while (rs.next()){
                v.add(rs.getString("ROW_ID"));
            }
        }
        finally{
        	try{
        		if (rs!=null) rs.close();
        		if (stmt!=null) stmt.close();
        	}
        	catch (SQLException e){}
        }

        del_rows = new String[v.size()];
        for (int i=0; i<v.size(); i++)
            del_rows[i] = (String)v.get(i);
    }
    
    /**
     * 
     * @return
     */
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
