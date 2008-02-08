package eionet.meta.exports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mysql.jdbc.ResultSet;

import eionet.test.MockDbPool;
import junit.framework.TestCase;

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
			conn = (new MockDbPool()).getConnection();
			CacheServlet.deleteCacheEntry("9999", "l'll", "a'sdasd", conn);
		}
		catch (Exception e){
			fail("Was not expecting any exceptions");
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}
}