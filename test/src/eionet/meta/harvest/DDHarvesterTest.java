package eionet.meta.harvest;

import java.sql.Connection;
import java.sql.SQLException;

import eionet.util.sql.ConnectionUtil;

import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DDHarvesterTest extends TestCase{

	/** */
	private Connection conn = null;	

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		conn = ConnectionUtil.getSimpleConnection();
	}
	
	/**
	 * 
	 *
	 */
	public void testRmvDeleted(){
		try{
			DDHarvester.rmvDeleted(conn);
		}
		catch (Exception e){
			fail("Was not expecting any exceptions, but catched " + e.toString());
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		try{
			if (conn!=null) conn.close();
		}
		catch (SQLException e){}
	}
}
