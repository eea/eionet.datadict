package eionet.meta.exports.pdf;

import eionet.DDDatabaseTestCase;
import java.sql.Connection;
import java.sql.SQLException;

import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DstPdfGuidelineTest extends DDDatabaseTestCase {

    private Connection conn = null;
    private DstPdfGuideline dstPdfGuideline = null;

    /*
     *  (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        conn = ConnectionUtil.getConnection();
        dstPdfGuideline = new DstPdfGuideline(conn);
    }

    /**
     * @throws Exception
     */
    public void testStoreAndDelete() throws Exception {
        String fileName = "test.txt";
        int i = dstPdfGuideline.storeCacheEntry("9999", fileName, conn);
        assertTrue(i > 0);
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
