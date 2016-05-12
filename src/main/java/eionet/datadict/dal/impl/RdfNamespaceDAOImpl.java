package eionet.datadict.dal.impl;

import eionet.datadict.dal.RdfNamespaceDAO;
import eionet.datadict.model.RdfNamespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author exorx-alk
 */
@Repository ("ddRdfNamespaceDAOImpl")
public class RdfNamespaceDAOImpl extends JdbcRepositoryBase implements RdfNamespaceDAO {

    @Autowired
    public RdfNamespaceDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public RdfNamespace getRdfNamespaceById(int id) {

        String sql = "Select * from T_RDF_NAMESPACE where ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        RdfNamespace rdfNamespace
                = getNamedParameterJdbcTemplate()
                .queryForObject(sql, params, new RowMapper<RdfNamespace>() {
                    @Override
                    public RdfNamespace mapRow(ResultSet rs, int i) throws SQLException {
                        return createFromSimpleSelectStatement(rs);
                    }

                });
        
        return rdfNamespace;
    }

    private RdfNamespace createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        RdfNamespace namespace = new RdfNamespace();

        namespace.setId(rs.getInt("ID"));
        namespace.setUri(rs.getString("URI"));
        namespace.setNamePrefix(rs.getString("NAME_PREFIX"));

        return namespace;
    }

}
