package eionet.datadict.dal.impl;

import eionet.datadict.dal.RdfNamespaceDao;
import eionet.datadict.model.RdfNamespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Aliki Kopaneli
 */
@Repository
public class RdfNamespaceDaoImpl extends JdbcDaoBase implements RdfNamespaceDao {

    @Autowired
    public RdfNamespaceDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<RdfNamespace> getRdfNamespaces() {
        String sql = "select * from T_RDF_NAMESPACE order by URI";
        List<RdfNamespace> namespaces = this.getJdbcTemplate().query(sql, new RdfNamespaceRowMapper());
        
        return namespaces;
    }
    
    protected static class RdfNamespaceRowMapper implements RowMapper<RdfNamespace> {
            
        @Override
        public RdfNamespace mapRow(ResultSet rs, int i) throws SQLException {
            RdfNamespace namespace = new RdfNamespace();
            namespace.setId(rs.getInt("ID"));
            namespace.setUri(rs.getString("URI"));
            namespace.setPrefix(rs.getString("NAME_PREFIX"));

            return namespace;
        }
        
    }

}
