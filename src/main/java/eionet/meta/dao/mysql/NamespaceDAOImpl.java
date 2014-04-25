package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.Namespace;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.INamespaceDAO;
import eionet.meta.service.data.NamespaceFilter;
import eionet.meta.service.data.NamespaceResult;

/**
 * INamespaceDAO implementation in mysql.
 *
 * @author enver
 */
@Repository
public class NamespaceDAOImpl extends GeneralDAOImpl implements INamespaceDAO {

    @Override
    public NamespaceResult getNamespaces(NamespaceFilter filter) throws DAOException {
        StringBuilder sql = new StringBuilder();
        sql.append("select SQL_CALC_FOUND_ROWS * from NAMESPACE order by SHORT_NAME ");

        if (filter.isUsePaging()) {
            sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
        }

        List<Namespace> resultList =
                getNamedParameterJdbcTemplate().query(sql.toString(), new HashMap<String, Object>(), new RowMapper<Namespace>() {
                    @Override
                    public Namespace mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Namespace ns =
                                new Namespace(rs.getString("NAMESPACE_ID"), rs.getString("SHORT_NAME"), rs.getString("FULL_NAME"),
                                        null, rs.getString("DEFINITION"));

                        return ns;
                    }
                });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        NamespaceResult result = new NamespaceResult(resultList, totalItems, filter);
        return result;
    } // end of method getNamespaces
} // end of class NamespaceDAOImpl
