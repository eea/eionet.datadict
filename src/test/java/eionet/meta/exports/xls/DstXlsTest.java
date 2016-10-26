package eionet.meta.exports.xls;

import java.sql.SQLException;

import org.junit.Assert;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus@tripledev.ee">jaanus@tripledev.ee</a>
 * 
 */
public class DstXlsTest extends TblXlsTest {

    @Override
    protected void initVars() {
        sheetNames = new String[] {"NiD_SW_Conc", "NiD_SW_EutroMeas"};
        dataSheetValues =
                new String[][] { {"ND_AvgWintValue", "ND_TrendAnnValue"}, {"ND_TrophicState", "ND_AvgValue", "ND_TrendWintValue"}};
        fxvSheetValues =
                new String[][] { {"5", "6"}, {"Ultra-oligotrophic", "Oligotrophic", "Mesotrophic", "Eutrophic", "Hypertrophic"}};
        fxvIdentifier = new String[] {"ND_TrendAnnValue", "ND_TrophicState"};
        classInstanceUnderTest = new DstXls(searchEngine, baos, true);
        classInstanceUnderTestWithoutDD = new DstXls(searchEngine, baos, false);
        objId = "4";
    }

    /**
     * @throws SQLException
     */
    @Override
    public void testStoreAndDelete() {
        try {
            String fileName = "test.txt";
            int i = classInstanceUnderTest.storeCacheEntry("999999", fileName, conn);
            Assert.assertTrue(i > 0);
        } catch (Exception e) {
            Assert.fail("Was not expecting any exceptions, but catched " + e.toString());
        }
    }

}
