package eionet.meta.dao.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.DAOException;
import eionet.meta.dao.SchemaDAO;
import eionet.meta.dao.domain.Schema;
import eionet.util.Util;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class MySQLSchemaDAO extends MySQLBaseDAO implements SchemaDAO {

    /** */
    private static final String INSERT_SQL = "insert into T_SCHEMA (FILENAME, CONTINUITY_ID, REG_STATUS, "
        + "WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID) "
        + "values (?,?,?,?,?,now(),?,?,?)";

    /**
     * @see eionet.meta.dao.SchemaDAO#add(Schema)
     */
    @Override
    public int add(Schema schema) throws DAOException {

        if (schema == null) {
            throw new IllegalArgumentException();
        }

        Connection conn = null;
        try {
            conn = getConnection();

            String continuityId = schema.getContinuityId();
            if (StringUtils.isBlank(continuityId)) {
                continuityId = Util.generateContinuityId(schema);
            }
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(schema.getFileName());
            params.add(continuityId);
            params.add(schema.getRegStatus().toString());
            params.add(schema.isWorkingCopy());
            params.add(schema.getWorkingUser());
            params.add(schema.getUserModified());
            params.add(schema.getComment());
            params.add(schema.getCheckedOutCopyId());

            SQL.executeUpdate(INSERT_SQL, params, conn);
            return getLastInsertId(conn);

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    /**
     * @see eionet.meta.dao.SchemaDAO#listForSchemaSet(int)
     */
    @Override
    public List<Schema> listForSchemaSet(int schemaSetId) throws DAOException {

        // TODO Auto-generated method stub
        return new ArrayList<Schema>();
    }

}
