package eionet.util.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 *
 * @author Rait Väli
 */
public class SQLGeneratorTest {
    SQLGenerator sqlGenerator;

    @Test
    public void testSetField() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setField("IDENTIFIER", "testFieldValue1");
        assertEquals("testFieldValue1", sqlGenerator.getFieldValue("IDENTIFIER"));

        sqlGenerator = new SQLGenerator();
        sqlGenerator.setField("IDENTIFIER", "te'stFiel'dVal'ue1");
        assertEquals("te''stFiel''dVal''ue1", sqlGenerator.getFieldValue("IDENTIFIER"));
    }

    @Test
    public void testSetFieldExpr() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setFieldExpr("IDENTIFIER", "fldExpr");
        assertEquals("fldExpr", sqlGenerator.getFieldValue("IDENTIFIER"));

        sqlGenerator = new SQLGenerator();
        sqlGenerator.setFieldExpr("IDENTIFIER", "fld'Ex'pr");
        assertEquals("fld'Ex'pr", sqlGenerator.getFieldValue("IDENTIFIER"));
    }

    @Test
    public void testRemoveField() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setField("IDENTIFIER", "testFieldValue2");
        sqlGenerator.removeField("IDENTIFIER");
        assertEquals(null, sqlGenerator.getFieldValue("IDENTIFIER"));
    }

    @Test
    public void testGetFieldValue() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setField("IDENTIFIER", "testFieldValue3");
        assertEquals("testFieldValue3", sqlGenerator.getFieldValue("IDENTIFIER"));

    }

    @Test
    public void testGetValues() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setField("VERSION", "4");
        sqlGenerator.setField("IDENTIFIER", "testFieldValue5");
        assertEquals("'4', 'testFieldValue5'", sqlGenerator.getValues());
    }

    @Test
    public void testUpdateStatement() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setField("VERSION", "6");
        sqlGenerator.setField("IDENTIFIER", "testFieldValue7");
        assertEquals("UPDATE dataset SET VERSION='6',IDENTIFIER='testFieldValue7' ", sqlGenerator.updateStatement());

        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setField("VERSION", "6");
        sqlGenerator.setField("IDENTIFIER", "test<Fi'eld'Val'ue7");
        assertEquals("UPDATE dataset SET VERSION='6',IDENTIFIER='test<Fi''eld''Val''ue7' ", sqlGenerator.updateStatement());
    }

    @Test
    public void testInsertStatement() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setField("VERSION", "8");
        sqlGenerator.setField("IDENTIFIER", "testFieldValue9");
        assertEquals("INSERT INTO dataset (VERSION,IDENTIFIER) VALUES('8', 'testFieldValue9')", sqlGenerator.insertStatement());

        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setField("VERSION", "568'");
        sqlGenerator.setField("IDENTIFIER", "testFieldValue9");
        assertEquals("INSERT INTO dataset (VERSION,IDENTIFIER) VALUES('568''', 'testFieldValue9')", sqlGenerator.insertStatement());
    }

    @Test
    public void testDeleteStatement() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        assertEquals("DELETE FROM dataset ", sqlGenerator.deleteStatement());
    }

    @Test
    public void testClear() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setPKField("dataset_ID");
        sqlGenerator.setState(1);
        sqlGenerator.setField("VERSION", "testFieldValue10");
        sqlGenerator.setField("IDENTIFIER", "testFieldValue11");

        sqlGenerator.clear();

        assertEquals("", sqlGenerator.getValues());
        assertEquals(-1, sqlGenerator.getState());
        assertEquals(null, sqlGenerator.getPKField());
        assertEquals("", sqlGenerator.getTableName());

    }

    @Test
    public void testClone() {
        sqlGenerator = new SQLGenerator();
        sqlGenerator.setTable("dataset");
        sqlGenerator.setPKField("pkField2");
        sqlGenerator.setState(2);
        sqlGenerator.setField("VERSION", "testFieldValue12");
        sqlGenerator.setField("testField14", "testFieldValue13");

        assertNotEquals(sqlGenerator.toString(), sqlGenerator.clone().toString());
        SQLGenerator newSqlGenerator = (SQLGenerator) sqlGenerator.clone();
        assertEquals("'testFieldValue12', 'testFieldValue13'", newSqlGenerator.getValues());
        assertEquals(2, newSqlGenerator.getState());
        assertEquals("pkField2", newSqlGenerator.getPKField());
        assertEquals("dataset", newSqlGenerator.getTableName());

    }

}
