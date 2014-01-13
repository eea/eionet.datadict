package eionet.meta.exports.xls;

import java.sql.SQLException;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus@tripledev.ee">jaanus@tripledev.ee</a>
 * 
 */
public class DstXlsTest extends TblXlsTest {

    @Override
    protected void initVars() {
        sheetNames = new String[] {"NiD_SW_EutroMeas", "NiD_SW_Conc"};
        dataSheetValues =
                new String[][] { {"ND_TrophicState", "ND_AvgValue", "ND_TrendWintValue"}, {"ND_AvgWintValue", "ND_TrendAnnValue"}};
        fxvSheetValues =
                new String[][] { {"Eutrophic", "Hypertrophic", "Mesotrophic", "Oligotrophic", "Ultra-oligotrophic"}, {"5", "6"}};
        fxvIdentifier = new String[] {"ND_TrophicState", "ND_TrendAnnValue"};
        classInstanceUnderTest = new DstXls(searchEngine, baos);
        objId = "4";
    }

    /**
     * @throws SQLException
     */
    @Override
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

}
