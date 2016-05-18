package eionet.datadict.dal.impl;

import eionet.datadict.dal.RdfNamespaceDAO;
import eionet.datadict.model.RdfNamespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author exorx-alk
 */
@Repository("ddRdfNamespaceDAOImpl")
public class RdfNamespaceDAOImpl extends JdbcRepositoryBase implements RdfNamespaceDAO {

    @Autowired
    public RdfNamespaceDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<RdfNamespace> getRdfNamespaces() {
        String sql = "select * from T_RDF_NAMESPACE order by URI";
        List<RdfNamespace> namespaces
                = this.getJdbcTemplate().query(sql, new RowMapper<RdfNamespace>() {
                    @Override
                    public RdfNamespace mapRow(ResultSet rs, int i) throws SQLException {
                        return createFromSimpleSelectStatement(rs);
                    }
                });
        return namespaces;
    }

    private RdfNamespace createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        RdfNamespace namespace = new RdfNamespace();

        namespace.setId(rs.getInt("ID"));
        namespace.setUri(rs.getString("URI"));
        namespace.setNamePrefix(rs.getString("NAME_PREFIX"));

        return namespace;
    }

}
