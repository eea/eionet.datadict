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
	
	public OutService(){
	}
	
	public Vector getParametersByActivityID(String raID) throws Exception{
		
		if (conn==null) getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn);
		return searchEngine.getParametersByActivityID(raID);
	}
	
	private void getConnection() throws Exception{
		Class.forName(Props.getProperty(PropsIF.DBDRV));
		this.conn = DriverManager.getConnection(
			Props.getProperty(PropsIF.DBURL),
			Props.getProperty(PropsIF.DBUSR),
			Props.getProperty(PropsIF.DBPSW));
	}
	
	private void closeConnection(){
		try{ if (conn!=null) conn.close(); } catch (SQLException e){}
	}
}

