package eionet.meta.outservices;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DsTable;
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

	/**
	 * Created by Dusko Kolundzija(ED)
	 * Get dataset tables
	 * @return Vector of Dataset tables
	 * @throws Exception
	 */
		public Vector getDSTables() throws Exception{
			
			try{
				if (conn==null) getConnection();
				DDSearchEngine searchEngine = new DDSearchEngine(conn);
				Vector result = searchEngine.getDatasetTables(null, null, null, null, null);
		
				Vector ret = new Vector();
				for (int i=0; i<result.size(); i++){
					DsTable table = (DsTable)result.get(i);
					
					String table_id = table.getID();
					String table_name = table.getShortName();
					String ds_id = table.getDatasetID();
					String ds_name = table.getDatasetName();
					String dsNs = table.getParentNs();
		
					Hashtable hash = new Hashtable();
					hash.put("tblId", table.getID());
					hash.put("identifier", table.getIdentifier());
					hash.put("shortName", table.getShortName());
					hash.put("dataSet", table.getDatasetName());				
					ret.add(hash);			
					
				}		
				return ret;

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

