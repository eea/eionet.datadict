package eionet.datadict.dal.impl;

import eionet.datadict.dal.NamespaceDAO;
import eionet.datadict.model.Namespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Aliki Kopaneli
 */
@Repository("ddNamespaceDAOImpl")
public class NamespaceDAOImpl extends JdbcRepositoryBase implements NamespaceDAO {

    private static final String GET_ATTRIBUTE_NAMESPACES
            = "select * from NAMESPACE";

    @Autowired
    public NamespaceDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Namespace> getAttributeNamespaces() {
        String sql = GET_ATTRIBUTE_NAMESPACES;
        List<Namespace> namespaces
                = this.getJdbcTemplate().query(sql, new RowMapper<Namespace>() {
                    @Override
                    public Namespace mapRow(ResultSet rs, int i) throws SQLException {
                        Namespace namespace = createFromSimpleSelectStatement(rs);
                        if (namespace.getNamespaceID() != 1 && (namespace.getShortName().contains("attributes") || namespace.getFullName().contains("attributes"))) {
                            return namespace;
                        }
                        return null;
                    }
                });
        namespaces.removeAll(Collections.singleton(null));
        return namespaces;
    }

    private Namespace createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        Namespace namespace = new Namespace();
        namespace.setNamespaceID(rs.getInt("NAMESPACE_ID"));
        namespace.setShortName(rs.getString("SHORT_NAME"));
        namespace.setFullName(rs.getString("FULL_NAME"));
        namespace.setDefinition(rs.getString("DEFINITION"));
        namespace.setParentNS(rs.getInt("PARENT_NS"));
        namespace.setWorkingUser(rs.getString("WORKING_USER"));
        return namespace;
    }

}
