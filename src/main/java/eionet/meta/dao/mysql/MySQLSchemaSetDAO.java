package eionet.meta.dao.mysql;

import java.sql.Connection;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import eionet.meta.dao.DAOException;
import eionet.meta.dao.SchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
import eionet.util.Util;
import eionet.util.sql.SQL;

/**
 * 
 * @author Jaanus Heinlaid
 * 
 */
public class MySQLSchemaSetDAO extends MySQLBaseDAO implements SchemaSetDAO {

    /** */
    private static final String INSERT_SQL = "insert into SCHEMA_SET (IDENTIFIER, CONTINUITY_ID, REG_STATUS, "
        + "WORKING_COPY, WORKING_USER, DATE, USER, COMMENT, CHECKEDOUT_COPY_ID) " + "values (?,?,?,?,?,now(),?,?,?)";

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
            if (StringUtils.isBlank(continuityId)){
                continuityId = Util.generateContinuityId(schemaSet);
            }
            ArrayList<Object> params = new ArrayList<Object>();
            params.add(schemaSet.getIdentifier());
            params.add(continuityId);
            params.add(schemaSet.getRegStatus().toString());
            params.add(schemaSet.isWorkingCopy());
            params.add(schemaSet.getWorkingUser());
            params.add(schemaSet.getUser());
            params.add(schemaSet.getComment());
            params.add(schemaSet.getCheckedOutCopyId());

            SQL.executeUpdate(INSERT_SQL, params, conn);
            return getLastInsertId(conn);

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            close(conn);
        }
    }
}
