package eionet.meta.exports.xls;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus@tripledev.ee">jaanus@tripledev.ee</a>
 * 
 */
public class DstXlsTest extends TestCase {

    /** Connection object */
    private Connection conn = null;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        conn = ConnectionUtil.getConnection();
    }

    /**
     * @throws SQLException
     */
    public void testStoreAndDelete() {
        try {
            String fileName = "test.txt";
            int i = DstXls.storeCacheEntry("999999", fileName, conn);
            assertTrue(i > 0);
            String s = DstXls.deleteCacheEntry("999999", conn);
            assertEquals(fileName, s);
        } catch (Exception e) {
            fail("Was not expecting any exceptions, but catched " + e.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }
}
