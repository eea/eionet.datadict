package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.tee.util.*;

public class RodLinksHandler{
	
	private Connection conn = null;
	private ServletContext ctx = null;
	
	public RodLinksHandler(Connection conn, ServletContext ctx){
		this.conn = conn;
		this.ctx = ctx;
	}
	
	public void execute(HttpServletRequest req) throws Exception{
		
		String dstID = req.getParameter("dst_id");
		if (dstID==null)
			throw new Exception("RodLinksHandler: dstID is missing!");
		
		String mode = req.getParameter("mode");
		if (mode==null)
			throw new Exception("RodLinksHandler: mode is missing!");
		else if (mode.equals("add"))
			addRodLinks(req, dstID);
		else if (mode.equals("rmv"))
			rmvRodLinks(req, dstID);
		else
			throw new Exception("RodLinksHandler: unknown mode " + mode);
	}
	
	private void addRodLinks(HttpServletRequest req, String dstID) throws Exception{
	
		String raID = req.getParameter("ra_id");
		if (Util.nullString(raID)) throw new Exception("ra_id is missing!");
	
		StringBuffer buf = new StringBuffer("select count(*) from DST2ROD where DATASET_ID=").
		append(dstID).append(" and ACTIVITY_ID=").append(raID);
	
		Statement stmt = null;
		ResultSet rs = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			if (rs.next() && rs.getInt(1) > 0)
				throw new Exception("This dataset is already linked with this obligation!");
		
			String raTitle = req.getParameter("ra_title");
			String liID = req.getParameter("li_id");
			String liTitle = req.getParameter("li_title");
		
			buf = new StringBuffer("select count(*) from ROD_ACTIVITIES where ").
			append("ACTIVITY_ID=").append(raID);
			rs.close();
			rs = stmt.executeQuery(buf.toString());
			SQLGenerator gen = new SQLGenerator();
			if (rs.next() && rs.getInt(1) == 0){
				gen.setTable("ROD_ACTIVITIES");
				gen.setFieldExpr("ACTIVITY_ID", raID);
				if (raTitle!=null) gen.setField("ACTIVITY_TITLE", raTitle);
				if (liID!=null) gen.setFieldExpr("LEGINSTR_ID", liID);
				if (liTitle!=null) gen.setField("LEGINSTR_TITLE", liTitle);
				stmt.executeUpdate(gen.insertStatement());
			}
		
			gen = new SQLGenerator();
			gen.setTable("DST2ROD");
			gen.setFieldExpr("DATASET_ID", dstID);
			gen.setFieldExpr("ACTIVITY_ID", raID);
			stmt.executeUpdate(gen.insertStatement());
		}
		catch (Exception e){
			try{
				if (rs != null)   rs.close();
				if (stmt != null) stmt.close();
			}
			catch (SQLException sqlee){}
		}
	}

	private void rmvRodLinks(HttpServletRequest req, String dstID) throws Exception{
		String[] raIDs = req.getParameterValues("del_id");
		if (raIDs==null || raIDs.length==0) throw new Exception("ra_id is missing!");
	
		StringBuffer buf = new StringBuffer("delete from DST2ROD where DATASET_ID=").
		append(dstID).append(" and (");
		for (int i=0; i<raIDs.length; i++){
			if (i>0) buf.append(" or ");
			buf.append("ACTIVITY_ID=").append(raIDs[i]);
		}
		buf.append(")");
	
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());
		}
		catch (SQLException sqle){
			try{if (stmt != null) stmt.close();}catch (SQLException sqlee){}
		}
	}
}
