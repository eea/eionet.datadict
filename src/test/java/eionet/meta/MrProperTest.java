package eionet.meta;


import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import eionet.meta.savers.Parameters;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.DDDatabaseTestCase;


public class MrProperTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-dataelement.xml";
    }

    /**
     * This test comes from MrProper.java. It appears to be irrelevant to the class
     */
    public static void test_hashsets() {
        HashSet set = new HashSet();
            
        HashMap hash1 = new HashMap();

        hash1.put("kala", null);
        hash1.put("mees", "auto");
        set.add(hash1);
        
        HashMap hash2 = new HashMap();

        hash2.put("kala", null);
        hash2.put("mees", "auto");
            
        TestCase.assertTrue(set.contains(hash2));
    }

    public static void test_rls_nowc() throws Exception {
        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection conn = DriverManager.getConnection(
                Props.getProperty(PropsIF.DBURL),
                Props.getProperty(PropsIF.DBUSR),
                Props.getProperty(PropsIF.DBPSW));

        MrProper mrProper = null;
        
        DDUser testUser = new TestUser();

        testUser.authenticate("jaanus", "jaanus");

        mrProper = new MrProper(conn);
        mrProper.setUser(testUser);
            
        Parameters pars = new Parameters();

        pars.addParameterValue(MrProper.FUNCTIONS_PAR, MrProper.RLS_NOWC);
        // pars.addParameterValue(MrProper.FUNCTIONS_PAR, RMV_MULT_VERS);
            
        mrProper.execute(pars);
        Vector expected = new Vector();

        expected.add("Releasing locked objects was <b>OK!</b>");
        TestCase.assertEquals(expected, mrProper.getResponse());

    }
}
