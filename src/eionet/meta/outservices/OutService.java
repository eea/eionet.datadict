package eionet.meta.outservices;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.util.Props;
import eionet.util.PropsIF;

public class OutService {
	
	Connection conn = null;
	
	/*
	 * 
	 */
	public OutService(){
	}
	
	/*
	 * 
	 */
	public Vector getParametersByActivityID(String raID) throws Exception{
		
		try{
			if (conn==null) getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn);
			return searchEngine.getParametersByActivityID(raID);
		}
		finally{
			closeConnection();
		}
	}

	/*
	 * This one returns the IDs and titles of all ogligations that have
	 * a released dataset definition present in DD
	 */
	public Vector getObligationsWithDatasets() throws Exception{
		
		try{
			if (conn==null) getConnection();
			DDSearchEngine searchEngine = new DDSearchEngine(conn);
			return searchEngine.getObligationsWithDatasets();
		}
		finally{
			closeConnection();
		}
	}
	
	/*
	 * 
	 */
	private void getConnection() throws Exception{
		Class.forName(Props.getProperty(PropsIF.DBDRV));
		this.conn = DriverManager.getConnection(
			Props.getProperty(PropsIF.DBURL),
			Props.getProperty(PropsIF.DBUSR),
			Props.getProperty(PropsIF.DBPSW));
	}
	
	/*
	 * 
	 */
	private void closeConnection(){
		try{ if (conn!=null) conn.close(); } catch (SQLException e){}
	}
}

