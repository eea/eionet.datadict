/*
 * Created on Oct 7, 2003
 */
package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;

public class FKHandler {

	private Connection conn = null;
	private Parameters req = null;
	private ServletContext ctx = null;
	
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

	public void execute() throws Exception {
		
		mode = req.getParameter("mode");
		
		if (mode.equalsIgnoreCase("add"))
			insert();
		else if (mode.equalsIgnoreCase("edit"))
			update();
		else
			delete();
	}
	
	private void insert() throws Exception {
		
		String aID = req.getParameter("a_id");
		String bID = req.getParameter("b_id");
		
		if (Util.nullString(aID) || Util.nullString(bID))
			throw new Exception("One or two of the element IDs is missing!");
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("FK_RELATION");
		gen.setField("A_ID", aID);
		gen.setField("B_ID", bID);
		
		String aCardin = req.getParameter("a_cardin");
		if (!Util.nullString(aCardin))
			gen.setField("A_CARDIN", aCardin);
		String bCardin = req.getParameter("b_cardin");
		if (!Util.nullString(bCardin))
			gen.setField("B_CARDIN", bCardin);
		String definition = req.getParameter("definition");
		if (!Util.nullString(definition))
			gen.setField("DEFINITION", definition);
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(gen.insertStatement());
		stmt.close();

		setLastInsertID();
	}
	
	private void update() throws Exception {
		
		String rel_id = req.getParameter("rel_id");
		if (Util.nullString(rel_id))
			return;
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("FK_RELATION");
		
		String aCardin = req.getParameter("a_cardin");
		if (!Util.nullString(aCardin))
			gen.setField("A_CARDIN", aCardin);
		String bCardin = req.getParameter("b_cardin");
		if (!Util.nullString(bCardin))
			gen.setField("B_CARDIN", bCardin);
		String definition = req.getParameter("definition");
		if (!Util.nullString(definition))
			gen.setField("DEFINITION", definition);
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(gen.updateStatement() + " where REL_ID=" + rel_id);
		stmt.close();
		
		lastInsertID = rel_id;
	}

	private void delete() throws Exception {
		
		String[] rel_ids = req.getParameterValues("rel_id");
		if (rel_ids==null || rel_ids.length==0)
			return;
        
		StringBuffer buf = new StringBuffer("delete from FK_RELATION where ");
		for (int i=0; i<rel_ids.length; i++){
			if (i>0)
				buf.append(" or ");
			buf.append("REL_ID=");
			buf.append(rel_ids[i]);
		}
        
		log(buf.toString());

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(buf.toString());
		stmt.close();
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
}
