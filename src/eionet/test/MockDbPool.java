package eionet.test;

import static org.easymock.EasyMock.expect;

import java.sql.Connection;
import java.sql.DriverManager;

import com.tee.xmlserver.DBPoolIF;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * This class mocks an object that implements <code>com.tee.xmlserver.DBPoolIF</code> and is to be
 * used in unit tests that test servlets that load their DB connection from the DB pool of the servlet container.
 * 
 *  Usage example:
 *  <code>expect(servletContext.getInitParameter("module-db_pool")).andReturn("eionet.test.MockDbPool");</code>
 *  
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class MockDbPool implements DBPoolIF{

	/**
	 *
	 */
	public MockDbPool(){
	}

	/**
	 * 
	 * @param appName
	 */
	public MockDbPool(String appName){
		this();
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tee.xmlserver.DBPoolIF#getConnection()
	 */
	public Connection getConnection() {
	
		try{
			Class.forName(Props.getProperty(PropsIF.DBDRV));
		    return DriverManager.getConnection(
		    		Props.getProperty(PropsIF.DBURL),
		    		Props.getProperty(PropsIF.DBUSR),
		    		Props.getProperty(PropsIF.DBPSW));
		}
		catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()==null ? "" : e.getMessage(), e);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tee.xmlserver.DBPoolIF#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String usr, String pwd) {
		
		try{
			Class.forName(Props.getProperty(PropsIF.DBDRV));
		    return DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), usr, pwd);
		}
		catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()==null ? "" : e.getMessage(), e);
		}
	}

}
