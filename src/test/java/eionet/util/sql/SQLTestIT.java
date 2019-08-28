package eionet.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is a class for unit testing the <code>eionet.util.sql.SQL</code> class.
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
public class SQLTestIT extends TestCase{

    /**
     *
     */
    @Test
    public void test_insertStatement(){

        INParameters inParams = new INParameters();
        LinkedHashMap hash = new LinkedHashMap();
        hash.put("COL1", "'value1'");
        hash.put("COL2", inParams.add("45", Types.INTEGER));
        hash.put("COL3", "md5(" + inParams.add("value2", Types.VARBINARY) + ")");

        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            conn = ConnectionUtil.getConnection();
            assertNotNull(conn);

            stmt = SQL.preparedStatement(SQL.insertStatement("TBL1", hash), inParams, conn);
            assertNotNull(stmt);
        }
        catch (Exception e){
            fail("Was not expecting this exception: " + e.getClass().getName());
        }
        finally{
            try{
                if (stmt!=null) {
                    stmt.close();
                }
                if (conn!=null) {
                    conn.close();
                }
            }
            catch (SQLException e){}
        }
    }

}
