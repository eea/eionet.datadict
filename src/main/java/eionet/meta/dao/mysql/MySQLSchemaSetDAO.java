package eionet.meta.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.SchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.SchemaSet.RegStatus;
import eionet.util.Util;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class MySQLSchemaSetDAO extends MySQLBaseDAO implements SchemaSetDAO {

    /** */
    private static final String INSERT_SQL = "insert into T_SCHEMA_SET (IDENTIFIER, CONTINUITY_ID, REG_STATUS, "
        + "WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID) " + "values (?,?,?,?,?,now(),?,?,?)";

    /**
     * @see eionet.meta.dao.SchemaSetDAO#add(eionet.meta.dao.domain.SchemaSet)
     */
    @Override
    public int add(SchemaSet schemaSet) throws DAOException {

        if (schemaSet == null) {
            throw new IllegalArgumentException();
        }

        Connection conn = null;
        try {
            conn = getConnection();

            String continuityId = schemaSet.getContinuityId();
            if (StringUtils.isBlank(continuityId)) {
                continuityId = Util.generateContinuityId(schemaSet);
            }
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(schemaSet.getIdentifier());
            params.add(continuityId);
            params.add(schemaSet.getRegStatus().toString());
            params.add(schemaSet.isWorkingCopy());
            params.add(schemaSet.getWorkingUser());
            params.add(schemaSet.getUserModified());
            params.add(schemaSet.getComment());
            params.add(schemaSet.getCheckedOutCopyId());

            SQL.executeUpdate(INSERT_SQL, params, conn);
            return getLastInsertId(conn);

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /** */
    private static final String GET_BY_IDENTIFIER_SQL = "select * from T_SCHEMA_SET where IDENTIFIER=?";

    /**
     * @see eionet.meta.dao.SchemaSetDAO#getByIdentifier(java.lang.String)
     */
    @Override
    public SchemaSet getByIdentifier(String identifier) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(identifier);

        return loadSchemaSet(GET_BY_IDENTIFIER_SQL, params);
    }

    /** */
    private static final String GET_BY_ID_SQL = "select * from T_SCHEMA_SET where SCHEMA_SET_ID=?";

    /**
     * @see eionet.meta.dao.SchemaSetDAO#getById(int)
     */
    @Override
    public SchemaSet getById(int id) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(id);

        return loadSchemaSet(GET_BY_ID_SQL, params);
    }

    /**
     * 
     * @param parameterizedSQL
     * @param params
     * @return
     * @throws DAOException
     */
    private SchemaSet loadSchemaSet(String parameterizedSQL, Collection<?> params) throws DAOException {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();

            pstmt = SQL.preparedStatement(parameterizedSQL, params, conn);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return readSchemaSet(rs);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            close(rs);
            close(pstmt);
            close(conn);
        }
    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    private SchemaSet readSchemaSet(ResultSet rs) throws SQLException {
        SchemaSet schemaSet = new SchemaSet();
        schemaSet.setId(rs.getInt("SCHEMA_SET_ID"));
        schemaSet.setIdentifier(rs.getString("IDENTIFIER"));
        schemaSet.setContinuityId(rs.getString("CONTINUITY_ID"));
        schemaSet.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
        schemaSet.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
        schemaSet.setWorkingUser(rs.getString("WORKING_USER"));
        schemaSet.setDateModified(rs.getDate("DATE_MODIFIED"));
        schemaSet.setUserModified(rs.getString("USER_MODIFIED"));
        schemaSet.setComment(rs.getString("COMMENT"));
        schemaSet.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
        return schemaSet;
    }

    /** */
    private static final String UPDATE_SQL =
        "update T_SCHEMA_SET set IDENTIFIER=?, REG_STATUS=?, DATE_MODIFIED=now(), USER_MODIFIED=?, COMMENT=ifnull(?,COMMENT) where SCHEMA_SET_ID=?";
    private static final String DELETE_ATTRIBUTE_SQL =
        "delete from ATTRIBUTE where M_ATTRIBUTE_ID=? and DATAELEM_ID=? and PARENT_TYPE=?";
    private static final String INSERT_ATTRIBUTE_SQL =
        "insert into ATTRIBUTE (M_ATTRIBUTE_ID, DATAELEM_ID, PARENT_TYPE, VALUE) values (?,?,?,?)";

    /**
     * @see eionet.meta.dao.SchemaSetDAO#save(eionet.meta.dao.domain.SchemaSet, Map)
     */
    @Override
    public void save(SchemaSet schemaSet, Map<Integer, Set<String>> attributes) throws DAOException {

        if (schemaSet == null) {
            throw new IllegalArgumentException();
        }

        Connection conn = null;
        try {
            conn = getConnection();

            ArrayList<Object> params = new ArrayList<Object>();
            params.add(schemaSet.getIdentifier());
            params.add(schemaSet.getRegStatus().toString());
            params.add(schemaSet.getUserModified());
            params.add(schemaSet.getComment());
            params.add(schemaSet.getId());

            beginTransaction(conn);
            SQL.executeUpdate(UPDATE_SQL, params, conn);

            if (attributes != null) {
                for (Map.Entry<Integer, Set<String>> entry : attributes.entrySet()) {
                    Integer attrId = entry.getKey();
                    Set<String> attrValues = entry.getValue();
                    if (attrValues != null && !attrValues.isEmpty()) {

                        params = new ArrayList<Object>();
                        params.add(attrId);
                        params.add(schemaSet.getId());
                        params.add(DElemAttribute.ParentType.SCHEMA_SET.toString());
                        SQL.executeUpdate(DELETE_ATTRIBUTE_SQL, params, conn);

                        params.add("");
                        for (String attrValue : attrValues) {
                            params.set(3, attrValue);
                            SQL.executeUpdate(INSERT_ATTRIBUTE_SQL, params, conn);
                        }
                    }
                }
            }
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /** */
    private static final String UNLOCK_CHECKED_OUT_COPY_SQL = "update T_SCHEMA_SET set WORKING_USER=NULL where SCHEMA_SET_ID=?";
    private static final String UNLOCK_WORKING_COPY_SQL =
        "update T_SCHEMA_SET set WORKING_USER=NULL, WORKING_COPY=0, DATE_MODIFIED=now(), USER_MODIFIED=?, COMMENT=? where SCHEMA_SET_ID=?";

    /**
     * @see eionet.meta.dao.SchemaSetDAO#checkIn(int, String, String)
     */
    @Override
    public void checkIn(int id, String userName, String comment) throws DAOException {

        if (StringUtils.isBlank(userName)){
            throw new IllegalArgumentException("User name must not be blank!");
        }

        Connection conn = null;
        try {
            conn = getConnection();

            ArrayList<Object> params = new ArrayList<Object>();
            params.add(id);

            beginTransaction(conn);
            SchemaSet schemaSet = loadSchemaSet(GET_BY_ID_SQL, params);
            if (schemaSet == null) {
                throw new DAOException("Could not find a schema set by this id: " + id);
            }
            else if (!schemaSet.isWorkingCopy()){
                throw new DAOException("Not a working copy, cannot execute check-in!");
            }
            else if (!StringUtils.equals(userName, schemaSet.getWorkingUser())){
                throw new DAOException("Check-in user is not the current working user!");
            }

            int checkedOutCopyId = schemaSet.getCheckedOutCopyId();
            if (checkedOutCopyId > 0) {
                params = new ArrayList<Object>();
                params.add(checkedOutCopyId);
                SQL.executeUpdate(UNLOCK_CHECKED_OUT_COPY_SQL, params, conn);
            }

            params = new ArrayList<Object>();
            params.add(userName);
            params.add(comment);
            params.add(id);
            SQL.executeUpdate(UNLOCK_WORKING_COPY_SQL, params, conn);

            commit(conn);
        } catch (DAOException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            close(conn);
        }
    }
}
