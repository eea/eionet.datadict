package eionet.datadict.dal.impl;

import eionet.datadict.model.Attribute;
import eionet.datadict.model.enums.Enumerations.DDEntitiyType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import eionet.datadict.dal.AttributeDAO;

/**
 *
 * @author eworx-alk
 */
@Repository ("ddAttributeDAOImpl")
public class AttributeDAOImpl extends JdbcRepositoryBase implements AttributeDAO {

    @Autowired
    public AttributeDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Attribute> getAttributeByDeclarationId(int id) {
        String sql = "Select * from ATTRIBUTE where M_ATTRIBUTE_ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        List<Attribute> attributes = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Attribute>() {
            @Override
            public Attribute mapRow(ResultSet rs, int i) throws SQLException {
                return createFromSimpleSelectStatement(rs);
            }

        });
        return attributes;
    }
    
    

    private Attribute createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        Attribute attr = new Attribute();
        attr.setDataElementId(rs.getInt("M_ATTRIBUTE_ID"));
        attr.setDataElementId(rs.getInt("DATAELEM_ID"));
        attr.setParentType(DDEntitiyType.getEnum(rs.getString("PARENT_TYPE")));
        attr.setValue(rs.getString("VALUE"));
        return attr;
    }

}
