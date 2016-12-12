package eionet.datadict.dal.impl;

import eionet.datadict.dal.NamespaceDao;
import eionet.datadict.model.Namespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class NamespaceDaoImpl extends JdbcDaoBase implements NamespaceDao {

    @Autowired
    public NamespaceDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Namespace> getAttributeNamespaces() {
        String sql = "select * from NAMESPACE where SHORT_NAME like '%attributes%' or FULL_NAME like '%attributes%'";
        
        return this.getJdbcTemplate().query(sql, new NamespaceRowMapper());
    }
    
    protected static class NamespaceRowMapper implements RowMapper<Namespace> {
        
        @Override
        public Namespace mapRow(ResultSet rs, int i) throws SQLException {
            Namespace namespace = new Namespace();
            namespace.setId(rs.getInt("NAMESPACE_ID"));
            namespace.setShortName(rs.getString("SHORT_NAME"));
            namespace.setFullName(rs.getString("FULL_NAME"));
            namespace.setDefinition(rs.getString("DEFINITION"));
            namespace.setWorkingUser(rs.getString("WORKING_USER"));
            
            return namespace;
        }
        
    }

}
