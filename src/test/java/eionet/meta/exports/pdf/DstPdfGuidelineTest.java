package eionet.meta.exports.pdf;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DstPdfGuidelineTest extends TestCase {

//FIXME: This should be a DBUNIT test.
    /** */
    private Connection conn = null;

    /*
     *  (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        conn = ConnectionUtil.getConnection();
    }

    /**
     * @throws Exception
     */
    public void testStoreAndDelete() throws Exception {
        String fileName = "test.txt";
        int i = DstPdfGuideline.storeCacheEntry("9999", fileName, conn);
        assertTrue(i > 0);
        String s = DstPdfGuideline.deleteCacheEntry("9999", conn);
        assertEquals(fileName, s);
    }

    /*
     *  (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {}
    }

}
