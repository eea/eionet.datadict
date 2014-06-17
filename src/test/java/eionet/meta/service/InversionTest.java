package eionet.meta.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;


import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * Tests inversions.
 *
 * @author Kaido Laine
 */
@SpringApplicationContext("spring-context.xml")
public class InversionTest extends UnitilsJUnit4 {
    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(InversionTest.class);

    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-vocabularyinverse.xml");
    }

    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-vocabularyinverse.xml");
    }

    @Test
    public void testGetInverseElem() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("http://www.dbunit.org/properties/datatypeFactory", "org.dbunit.ext.mysql.MySqlDataTypeFactory");

        Connection conn =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));

        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SELECT GetInverseElemId(1) as X FROM dual");

        rs.next();
        int reverseId = rs.getInt("X");
        Assert.assertTrue("Should have inverse elem 2", 2 == reverseId);

        rs = st.executeQuery("SELECT GetInverseElemId(3) as X FROM dual");
        rs.next();

        Assert.assertTrue("ID 3 should have no inverse element", rs.getObject("X") == null);





    }
}
