package eionet.meta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import junit.framework.TestCase;
import eionet.DDDatabaseTestCase;
import eionet.meta.savers.Parameters;
import eionet.util.Props;
import eionet.util.PropsIF;

public class MrProperTestIT extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-dataelement.xml";
    }

    /**
     * This test comes from MrProper.java. It appears to be irrelevant to the class
     */
    public void test_hashsets() {
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

    public void test_rls_nowc() throws Exception {
        Class.forName(Props.getProperty(PropsIF.DBDRV));
        Connection conn =
                DriverManager.getConnection(Props.getProperty(PropsIF.DBURL), Props.getProperty(PropsIF.DBUSR),
                        Props.getProperty(PropsIF.DBPSW));

        MrProper mrProper = null;

        DDUser testUser = new FakeUser();

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
        conn.close();
    }
}
