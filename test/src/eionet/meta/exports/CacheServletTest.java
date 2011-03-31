package eionet.meta.exports;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class CacheServletTest extends TestCase{

	/**
	 * 
	 */
	public void test_deleteCacheEntry(){
		
		Connection conn = null;
		try{
			conn = ConnectionUtil.getSimpleConnection();
			CacheServlet.deleteCacheEntry("9999", "l'll", "a'sdasd", conn);
		}
		catch (Exception e){
			fail("Was not expecting any exceptions, but catched " + e.toString());			
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}
}