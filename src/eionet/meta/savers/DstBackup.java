/*
 * Created on Oct 9, 2003
 */
package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;
import com.tee.xmlserver.*;

import eionet.meta.*;

/**
 * @author jaanus
 */
public class DstBackup {
	
	private Connection conn = null;
	private Parameters req = null;
	private ServletContext ctx = null;
	
	private DDSearchEngine searchEngine = null;
	
	private String mode = null;
	private String[] ids = null;
	
	private AppUserIF user = null;
	
	private Vector scriptLines = new Vector();
	
	/*
	 * 
	 */
	public DstBackup(Connection conn,
						 HttpServletRequest req,
						 ServletContext ctx){
			this(conn, new Parameters(req), ctx);
	}
    
    /*
     * 
     */
	public DstBackup(Connection conn, Parameters req, ServletContext ctx){
		this.conn = conn;
		this.req = req;
		this.ctx = ctx;
		
		searchEngine = new DDSearchEngine(conn);

		this.mode = req.getParameter("mode");
	}
	
	/*
	 * 
	 */
	public void setUser(AppUserIF user){
			this.user = user;
	}
	
	/*
	 * 
	 */
	public void execute() throws Exception {
    	
		if (mode==null || (!mode.equalsIgnoreCase("add") &&
						  !mode.equalsIgnoreCase("edit") &&
						  !mode.equalsIgnoreCase("delete")))
			throw new Exception("DatasetBackup mode unspecified!");
        
		ids = req.getParameterValues("id");
		if (ids==null || ids.length==0)
			return;
					
		if (mode.equalsIgnoreCase("add"))
			insert();
		else
			delete();
	}
	
	/*
	 * 
	 */
	private void insert() throws Exception {
		for (int i=0; i<ids.length; i++)
			insert(ids[i]);
	}

	private void insert(String id) throws Exception {

		// dataset
				
		String q = "select * from DATASET where DATASET_ID=" + id;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		if (!rs.next())
			throw new Exception("Dataset not found!");
			
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i<=rsmd.getColumnCount(); i++)
			gen.setField(rsmd.getColumnName(i), rs.getString(i));

		scriptLines.add(gen.insertStatement());
		
		// dataset simple attributes
		
		gen.clear();
		gen.setTable("ATTRIBUTE");
		
		q ="select * from ATTRIBUTE where PARENT_TYPE='DS' and DATAELEM_ID="+id;
		rs = stmt.executeQuery(q);
		rsmd = rs.getMetaData();
		while (rs.next()){
			for (int i=1; i<=rsmd.getColumnCount(); i++)
				gen.setField(rsmd.getColumnName(i), rs.getString(i));
		}

		// dataset complex attributes
		
		gen.clear();
		gen.setTable("COMPLEX_ATTR_ROW");
		q = "select * from COMPLEX_ATTR_ROW where PARENT_TYPE='DS' and " +
						"PARENT_ID=" + id;
		rs = stmt.executeQuery(q);
		rsmd = rs.getMetaData();
		while (rs.next()){
			for (int i=1; i<=rsmd.getColumnCount(); i++)
				gen.setField(rsmd.getColumnName(i), rs.getString(i));
		}
		
		//v = searchEngine.getComplexAttributes(id, "DS");
		
		// dataset tables
		
		//v = searchEngine.getDatasetTables(id);
				
		// dataset namespace
	}

	/*
	 * 
	 */
	private void delete() throws Exception {
	}
}
