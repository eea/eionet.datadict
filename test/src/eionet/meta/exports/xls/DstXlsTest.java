package eionet.meta.exports.xls;

import java.sql.Connection;
import java.sql.SQLException;

import eionet.meta.exports.pdf.DstPdfGuideline;
import eionet.util.sql.ConnectionUtil;
import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DstXlsTest extends TestCase{

	
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
	 * @throws SQLException 
	 */
	public void testStoreAndDelete(){
		try{
			String fileName = "test.txt";
			int i = DstXls.storeCacheEntry("999999", fileName, conn);
			assertTrue(i>0);
			String s = DstXls.deleteCacheEntry("999999", conn);
			assertEquals(fileName, s);
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
