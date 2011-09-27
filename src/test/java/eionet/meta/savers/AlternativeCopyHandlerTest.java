package eionet.meta.savers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class AlternativeCopyHandlerTest extends DatabaseTestCase {

    /** */
    private static final String SEED_INSERT_TESTS_XML = "seed-insert-tests.xml";

    /** */
    private static final Logger LOGGER = Logger.getLogger(AlternativeCopyHandlerTest.class);

    /** */
    private static IDataSet xmlDataset;

    /** */
    HashMap<String, String> idMap;

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return new DatabaseConnection(ConnectionUtil.getConnection());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {

        return xmlDataset;
    }

    /*
     * (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception{

        LOGGER.debug("Running setup ...");

        prepareDataset();
        prepareIds();
        super.setUp();
    }

    /**
     *
     * @throws DataSetException
     * @throws IOException
     */
    private void prepareDataset() throws DataSetException, IOException{

        if (xmlDataset==null){
            xmlDataset = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream(SEED_INSERT_TESTS_XML));
        }
    }

    /**
     * @throws DataSetException
     *
     */
    private void prepareIds() throws DataSetException{

        if (idMap==null){

            idMap = new HashMap<String, String>();
            ITable table = xmlDataset.getTable("ATTRIBUTE");
            int rowCount = table.getRowCount();
            for (int i=0; i<rowCount; i++){

                String oldId = table.getValue(i, "DATAELEM_ID").toString();
                if (!idMap.containsKey(oldId)){

                    String newId = String.valueOf(Integer.parseInt(oldId) + 10000);
                    idMap.put(oldId, newId);
                }
            }
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testManyInsertSelects1() throws Exception{

        Connection conn = null;
        try{
            conn = getConnection().getConnection();
            conn.setAutoCommit(false);

            int i=0;
            LOGGER.debug("Starting inserts ...");

            long startTime = System.currentTimeMillis();
            for (Entry<String, String> entry : idMap.entrySet()){

                Statement stmt = null;
                try{
                    stmt = conn.createStatement();
                    String sql = AlternativeCopyHandler.simpleAttrsCopyStatement(entry.getKey(), entry.getValue(), "E");
                    stmt.addBatch(sql);
                    stmt.executeBatch();
                    i++;
                }
                finally{
                    SQL.close(stmt);
                }
            }

            conn.commit();
            long endTime = System.currentTimeMillis();
            LOGGER.debug(i + " inserts took " + (endTime-startTime) + " ms");
        }
        catch (Exception e){
            SQL.rollback(conn);
            throw e;
        }
        finally{
            SQL.close(conn);
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testManyInsertSelects2() throws Exception{

        Statement stmt = null;
        Connection conn = null;
        try{
            conn = getConnection().getConnection();
            conn.setAutoCommit(false);

            int i=0;
            LOGGER.debug("Starting inserts ...");

            long startTime = System.currentTimeMillis();
            stmt = conn.createStatement();

            for (Entry<String, String> entry : idMap.entrySet()){

                String sql = AlternativeCopyHandler.simpleAttrsCopyStatement(entry.getKey(), entry.getValue(), "E");
                stmt.addBatch(sql);
                i++;
            }

            stmt.executeBatch();
            conn.commit();
            long endTime = System.currentTimeMillis();
            LOGGER.debug(i + " inserts took " + (endTime-startTime) + " ms");
        }
        catch (Exception e){
            SQL.rollback(conn);
            throw e;
        }
        finally{
            SQL.close(stmt);
            SQL.close(conn);
        }
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testBatchedInsertSelects() throws Exception{

        PreparedStatement pstmt = null;
        Connection conn = null;
        try{
            conn = getConnection().getConnection();
            conn.setAutoCommit(false);

            String sql = AlternativeCopyHandler.simpleAttrsCopyStatement("?", "?", "E");

            int i=0;
            LOGGER.debug("Starting inserts ...");

            long startTime = System.currentTimeMillis();
            pstmt = conn.prepareStatement(sql);

            for (Entry<String, String> entry : idMap.entrySet()){

                pstmt.setInt(1, Integer.parseInt(entry.getValue()));
                pstmt.setInt(2, Integer.parseInt(entry.getKey()));
                pstmt.addBatch();
                i++;
            }

            pstmt.executeBatch();
            conn.commit();
            long endTime = System.currentTimeMillis();
            LOGGER.debug(i + " inserts took " + (endTime-startTime) + " ms");
        }
        catch (Exception e){
            SQL.rollback(conn);
            throw e;
        }
        finally{
            SQL.close(pstmt);
            SQL.close(conn);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSingleExtendedInsert() throws Exception{

        String getCurrentRowsSQL = "select * from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID in ("
            + Util.toCSV(idMap.keySet()) + ")";

        StringBuilder insertNewRowsSQL = new StringBuilder();
        insertNewRowsSQL.append("insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) values ");

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        try{
            conn = getConnection().getConnection();
            conn.setAutoCommit(false);

            int i=0;
            LOGGER.debug("Starting single extended insert ...");

            long startTime = System.currentTimeMillis();

            stmt = conn.createStatement();
            rs = stmt.executeQuery(getCurrentRowsSQL);
            while (rs.next()){

                int oldId = rs.getInt("DATAELEM_ID");
                int newId = oldId + 10000;
                String parentType = rs.getString("PARENT_TYPE");
                int mAttrId = rs.getInt("M_ATTRIBUTE_ID");
                String value = rs.getString("VALUE");

                if (i>0){
                    insertNewRowsSQL.append(",");
                }
                insertNewRowsSQL.append("(");
                insertNewRowsSQL.append(newId);
                insertNewRowsSQL.append(",");
                insertNewRowsSQL.append(SQL.toLiteral(parentType));
                insertNewRowsSQL.append(",");
                insertNewRowsSQL.append(mAttrId);
                insertNewRowsSQL.append(",");
                insertNewRowsSQL.append(SQL.toLiteral(value));
                insertNewRowsSQL.append(")");
                i++;
            }
            SQL.close(rs);

            LOGGER.debug(i + " current rows retreived for single extended insert");

            stmt.executeUpdate(insertNewRowsSQL.toString());

            conn.commit();
            long endTime = System.currentTimeMillis();
            LOGGER.debug("The single extended insert took " + (endTime-startTime) + " ms");
        }
        catch (Exception e){
            SQL.rollback(conn);
            throw e;
        }
        finally{
            SQL.close(rs);
            SQL.close(stmt);
            SQL.close(conn);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSingleBatchInsert() throws Exception{

        String sql = "insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) values (?,?,?,?)";

        ITable table = xmlDataset.getTable("ATTRIBUTE");
        int rowCount = table.getRowCount();

        PreparedStatement pstmt = null;
        Connection conn = null;
        try{
            conn = getConnection().getConnection();
            conn.setAutoCommit(false);

            LOGGER.debug("Starting single batch insert ...");

            long startTime = System.currentTimeMillis();
            pstmt = conn.prepareStatement(sql);

            for (int i=0; i<rowCount; i++){

                String oldId = table.getValue(i, "DATAELEM_ID").toString();
                int newId = Integer.parseInt(oldId) + 10000;
                String parentType = table.getValue(i, "PARENT_TYPE").toString();
                int mAttrId = Integer.parseInt(table.getValue(i, "M_ATTRIBUTE_ID").toString());
                String value = table.getValue(i, "VALUE").toString();

                pstmt.setInt(1, newId);
                pstmt.setString(2, parentType);
                pstmt.setInt(3, mAttrId);
                pstmt.setString(4, value);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();
            long endTime = System.currentTimeMillis();
            LOGGER.debug("The single batch insert took " + (endTime-startTime) + " ms");
        }
        catch (Exception e){
            SQL.rollback(conn);
            throw e;
        }
        finally{
            SQL.close(pstmt);
            SQL.close(conn);
        }
    }

}