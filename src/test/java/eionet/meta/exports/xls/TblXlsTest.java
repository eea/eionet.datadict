package eionet.meta.exports.xls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Assert;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDSearchEngine;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus@tripledev.ee">jaanus@tripledev.ee</a>
 * 
 */
public class TblXlsTest extends DDDatabaseTestCase {

    protected Connection conn = null;
    protected DDSearchEngine searchEngine = null;
    protected String[] sheetNames = null;
    protected String[][] dataSheetValues = null;
    protected String[] fxvIdentifier = null;
    protected String[][] fxvSheetValues = null;
    protected TblXls classInstanceUnderTest = null;
    protected TblXls classInstanceUnderTestWithoutDD = null;
    protected ByteArrayOutputStream baos = null;
    protected String objId = null;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        conn = ConnectionUtil.getConnection();
        searchEngine = new DDSearchEngine(conn);
        baos = new ByteArrayOutputStream();
        initVars();
    }

    protected void initVars() {
        sheetNames = new String[] {"NiD_SW_EutroMeas"};
        dataSheetValues = new String[][] {{"ND_TrophicState", "ND_AvgValue", "ND_TrendWintValue"}};
        fxvIdentifier = new String[] {"ND_TrophicState"};
        fxvSheetValues = new String[][] {{"Ultra-oligotrophic", "Oligotrophic", "Mesotrophic", "Eutrophic", "Hypertrophic"}};
        classInstanceUnderTest = new TblXls(searchEngine, baos, true);
        classInstanceUnderTestWithoutDD = new TblXls(searchEngine, baos, false);
        objId = "7";
    }

    /**
     * @throws SQLException
     */
    public void testStoreAndDelete() {
        try {
            String fileName = "test.txt";
            int i = TblXls.storeCacheEntry("999999", fileName, conn);
            Assert.assertTrue(i > 0);
            String s = TblXls.deleteCacheEntry("999999", conn);
            Assert.assertEquals(fileName, s);
        } catch (Exception e) {
            Assert.fail("Was not expecting any exceptions, but catched " + e.toString());
        }
    }

    /**
     * order of values change, so instead of following strict order, all elements checked
     * 
     * @throws SQLException
     */
    public void testExcelOutput() {
        try {
            classInstanceUnderTest.create(objId);
            classInstanceUnderTest.write();

            HSSFWorkbook testWb = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));

            // check sheet names, order of elements change, so follow a more elastic way to control
            // first one is fixed
            Assert.assertEquals("Incorrect sheet name", "REFERENCES_FOR_DROPDOWN_ITEMS_DO_NOT_DELETE", testWb.getSheetName(0));
            ArrayList<String> temp = new ArrayList<String>(Arrays.asList(sheetNames));
            for (int i = 0; i < sheetNames.length; i++) {
                String sheetName = testWb.getSheetName(i + 1);
                Assert.assertTrue("Sheet '" + sheetName + "' cannot be found!", temp.remove(sheetName));
            }
            // last one is also fixed
            Assert.assertEquals("Incorrect sheet name", "DO_NOT_DELETE_THIS_SHEET", testWb.getSheetName(1 + sheetNames.length));
            Assert.assertEquals("Incorrect number of sheets", (2 + sheetNames.length), testWb.getNumberOfSheets());
            Assert.assertTrue("Some sheets did not matched: " + temp.toString(), temp.size() == 0);

            // check for columns in each sheet
            for (int i = 0; i < dataSheetValues.length; i++) {
                HSSFSheet dataSheet = testWb.getSheet(sheetNames[i]);
                HSSFRow dataRow = dataSheet.getRow(0); // it is always first row
                temp = new ArrayList<String>(Arrays.asList(dataSheetValues[i]));
                for (int j = 0; j < dataSheetValues[i].length; j++) {
                    HSSFCell dataCell = dataRow.getCell(j);
                    String cellValue = dataCell.toString().trim();
                    Assert.assertTrue("Cell with value '" + cellValue + "' cannot be found!", temp.remove(cellValue));
                }
                Assert.assertTrue("Some cells did not matched: " + temp.toString(), temp.size() == 0);
            }

            // check for fixed values stored for drop-down menu item, because it is not ordered, code below is a bit complicated
            HSSFSheet fxvSheet = testWb.getSheetAt(0); // first sheet always
            temp = new ArrayList<String>(Arrays.asList(fxvIdentifier));
            for (int i = 0; i < fxvSheetValues.length; i++) {// Don't iterate more than number of drop-down item
                HSSFRow fxvRow = null;
                HSSFCell fxvCell = null;
                int rowIndex = 0;
                boolean found = false;
                for (rowIndex = 0; rowIndex < fxvSheetValues.length; rowIndex++) {
                    fxvRow = fxvSheet.getRow(rowIndex + 1); // first row is info, so start +1
                    fxvCell = fxvRow.getCell(0);// first one is label, get it first and then check for position
                    String value = fxvCell.toString().replace("Fixed Values of ", "").trim();
                    if (temp.remove(value)) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue("Identifier can not be found", found);

                ArrayList<String> temp2 = new ArrayList<String>(Arrays.asList(fxvSheetValues[rowIndex]));
                fxvRow = fxvSheet.getRow(rowIndex + 1); // first row is info, so start +1
                for (int j = 0; j < fxvSheetValues[rowIndex].length; j++) {
                    fxvCell = fxvRow.getCell(j + 1);// first column is label, so start +1
                    String fixedCellValue = fxvCell.toString().trim();
                    Assert.assertTrue("Fixed cell value '" + fixedCellValue + "' cannot be found!", temp2.remove(fixedCellValue));                    
                }
                Assert.assertTrue("Some fxvs did not matched: " + temp2.toString(), temp2.size() == 0);
            }
            Assert.assertTrue("Some fxvs did not matched: " + temp.toString(), temp.size() == 0);

        } catch (Exception e) {
            Assert.fail("Was not expecting any exceptions, but catched " + e.toString());
        }
    }
    
    
    /**
     * order of values change, so instead of following strict order, all elements checked
     * 
     * @throws SQLException
     */
    public void testExcelOutputWithoutDD() {
        try {
            classInstanceUnderTestWithoutDD.create(objId);
            classInstanceUnderTestWithoutDD.write();

            HSSFWorkbook testWb = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));

            // check sheet names, order of elements change, so follow a more elastic way to control
            ArrayList<String> temp = new ArrayList<String>(Arrays.asList(sheetNames));
            for (int i = 0; i < sheetNames.length; i++) {
                String sheetName = testWb.getSheetName(i);
                Assert.assertTrue("Sheet '" + sheetName + "' cannot be found!", temp.remove(sheetName));
            }
            // last one is fixed
            Assert.assertEquals("Incorrect sheet name", "DO_NOT_DELETE_THIS_SHEET", testWb.getSheetName(sheetNames.length));
            Assert.assertEquals("Incorrect number of sheets", (1 + sheetNames.length), testWb.getNumberOfSheets());
            Assert.assertTrue("Some sheets did not matched: " + temp.toString(), temp.size() == 0);

            // check for columns in each sheet
            for (int i = 0; i < dataSheetValues.length; i++) {
                HSSFSheet dataSheet = testWb.getSheet(sheetNames[i]);
                HSSFRow dataRow = dataSheet.getRow(0); // it is always first row
                temp = new ArrayList<String>(Arrays.asList(dataSheetValues[i]));
                for (int j = 0; j < dataSheetValues[i].length; j++) {
                    HSSFCell dataCell = dataRow.getCell(j);
                    String cellValue = dataCell.toString().trim();
                    Assert.assertTrue("Cell with value '" + cellValue + "' cannot be found!", temp.remove(cellValue));
                }
                Assert.assertTrue("Some cells did not matched: " + temp.toString(), temp.size() == 0);
            }
            
        } catch (Exception e) {
            Assert.fail("Was not expecting any exceptions, but catched " + e.toString());
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

            if (baos != null) {
                baos.close();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected String getSeedFilename() {
        return "seed-small-dataset.xml";
    }

}
