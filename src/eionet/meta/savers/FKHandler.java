/*
 * Created on Oct 7, 2003
 */
package eionet.meta.savers;

import java.sql.*;
import java.util.LinkedHashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;

import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public class FKHandler extends BaseHandler{

	private String mode = null;
	private String lastInsertID = null;

	public FKHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
		this(conn, new Parameters(req), ctx);
	}

	public FKHandler(Connection conn, Parameters req, ServletContext ctx){

		this.conn = conn;
		this.req = req;
		this.ctx = ctx;
	}

	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.savers.BaseHandler#execute_()
	 */
	public void execute_() throws Exception {
		
		mode = req.getParameter("mode");
		
		if (mode.equalsIgnoreCase("add"))
			insert();
		else if (mode.equalsIgnoreCase("edit"))
			update();
		else
			delete();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void insert() throws Exception {
		
		String aID = req.getParameter("a_id");
		String bID = req.getParameter("b_id");
		
		if (Util.nullString(aID) || Util.nullString(bID))
			throw new Exception("One or two of the element IDs is missing!");
		
		INParameters inParams = new INParameters();
        LinkedHashMap map = new LinkedHashMap();
        
		map.put("A_ID", inParams.add(aID, Types.INTEGER));
		map.put("B_ID", inParams.add(bID, Types.INTEGER));
		
		String aCardin = req.getParameter("a_cardin");
		if (!Util.nullString(aCardin))
			map.put("A_CARDIN", inParams.add(aCardin));
		String bCardin = req.getParameter("b_cardin");
		if (!Util.nullString(bCardin))
			map.put("B_CARDIN", inParams.add(bCardin));
		String definition = req.getParameter("definition");
		if (!Util.nullString(definition))
			map.put("DEFINITION", inParams.add(definition));
		
		SQL.executeUpdate(SQL.insertStatement("FK_RELATION", map), inParams, conn);
		setLastInsertID();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void update() throws Exception {
		
		String rel_id = req.getParameter("rel_id");
		if (Util.nullString(rel_id))
			return;

		INParameters inParams = new INParameters();
        LinkedHashMap map = new LinkedHashMap();

		//gen.setTable("FK_RELATION");
		
		String aCardin = req.getParameter("a_cardin");
		if (!Util.nullString(aCardin))
			map.put("A_CARDIN", inParams.add(aCardin));
		String bCardin = req.getParameter("b_cardin");
		if (!Util.nullString(bCardin))
			map.put("B_CARDIN", inParams.add(bCardin));
		String definition = req.getParameter("definition");
		if (!Util.nullString(definition))
			map.put("DEFINITION", inParams.add(definition));
		
		StringBuffer buf = new StringBuffer(SQL.updateStatement("FK_RELATION", map));
		buf.append(" where REL_ID=").append(inParams.add(rel_id, Types.INTEGER));
		SQL.executeUpdate(buf.toString(), inParams, conn);
		
		lastInsertID = rel_id;
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void delete() throws Exception {
		
		String[] rel_ids = req.getParameterValues("rel_id");
		if (rel_ids==null || rel_ids.length==0)
			return;
        
		INParameters inParams = new INParameters();
		StringBuffer buf = new StringBuffer("delete from FK_RELATION where ");
		for (int i=0; i<rel_ids.length; i++){
			if (i>0)
				buf.append(" or ");
			buf.append("REL_ID=").append(inParams.add(rel_ids[i], Types.INTEGER));
		}

		SQL.executeUpdate(buf.toString(), inParams, conn);
	}

	/**
	 * 
	 * @throws SQLException
	 */
	private void setLastInsertID() throws SQLException {

		String qry = "SELECT LAST_INSERT_ID()";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(qry);
		rs.clearWarnings();
		if (rs.next())
			lastInsertID = rs.getString(1);
		stmt.close();
	}

	/**
	 * 
	 * @return
	 */
	public String getLastInsertID(){
		return lastInsertID;
	}
}
