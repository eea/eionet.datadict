package eionet.meta.savers;

import java.util.HashMap;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eionet.util.sql.SQL;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)

public class CopyHandlerStaticTestIT extends TestCase {
    /**
     *
     */
    @Test
    public void testCopyRowsStatement1() {

        String whereClause = "USER='heinlja'";
        String start = "insert into DATASET (";
        String actual = CopyHandler.rowsCopyStatement("DATASET", whereClause, null);

        assertTrue(actual.startsWith(start));
        int i = actual.indexOf(")", start.length());
        assertTrue(i > start.length());

        String columnsCSV = actual.substring(start.length(), i);
        String expected = start + columnsCSV + ") select " + columnsCSV + " from DATASET where " + whereClause;
        assertEquals(expected, actual);
    }

    /**
     *
     */
    @Test
    public void testCopyRowsStatement2() {

        HashMap<String,Object> newValues = new HashMap<String,Object>();
        newValues.put("REG_STATUS", SQL.toLiteral("new_reg_status"));
        newValues.put("VERSION", Integer.valueOf("1111"));
        newValues.put("SHORT_NAME", null);

        String whereClause = "USER='heinlja'";
        String start = "insert into DATASET (";
        String actual = CopyHandler.rowsCopyStatement("DATASET", whereClause, newValues);

        assertTrue(actual.startsWith(start));
        int i = actual.indexOf(")", start.length());
        assertTrue(i > start.length());

        String columnsCSV = actual.substring(start.length(), i);
        String valuesCSV = new String(columnsCSV);
        for (Entry<String,Object> entry : newValues.entrySet()) {
            Object value = entry.getValue();
            valuesCSV = StringUtils.replace(valuesCSV, entry.getKey(), (value == null) ? "NULL" : value.toString());
        }

        String expected = start + columnsCSV + ") select " + valuesCSV + " from DATASET where " + whereClause;

        assertEquals(expected, actual);
    }

}
