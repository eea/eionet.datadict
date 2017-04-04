package eionet.datadict.dal.impl;


import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AttributeValueDaoImpl extends JdbcDaoBase implements AttributeValueDao{

    @Autowired
    public AttributeValueDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public List<AttributeValue> getByAttributeAndOwner(int attributeId, DataDictEntity ddEntity) {
        String sql = "SELECT "
                + "ATTRIBUTE.* "
                + "FROM ATTRIBUTE "
                + "WHERE M_ATTRIBUTE_ID = :attributeId AND PARENT_TYPE = :parentType AND DATAELEM_ID = :ddEntityId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeId", attributeId);
        params.put("parentType", ddEntity.getType().toString());
        params.put("ddEntityId", ddEntity.getId());
        
        try {
            return this.getNamedParameterJdbcTemplate().query(sql, params, new AttributeValueRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public void deleteAttributeValue(int attributeId, DataDictEntity ddEntity, String value) {
        String sql = "DELETE FROM ATTRIBUTE WHERE "
                + "M_ATTRIBUTE_ID = :attributeId "
                + "AND PARENT_TYPE = :parentType "
                + "AND DATAELEM_ID = :ddEntityId "
                + "AND VALUE = :value";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeId", attributeId);
        params.put("parentType", ddEntity.getType().toString());
        params.put("ddEntityId", ddEntity.getId());
        params.put("value", value);
        
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    @Override
    public void deleteAllAttributeValues(int attributeId, DataDictEntity ddEntity) {
        String sql = "DELETE FROM ATTRIBUTE WHERE "
                + "M_ATTRIBUTE_ID = :attributeId "
                + "AND PARENT_TYPE = :parentType "
                + "AND DATAELEM_ID = :ddEntityId";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeId", attributeId);
        params.put("parentType", ddEntity.getType().toString());
        params.put("ddEntityId", ddEntity.getId());
        
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteAllAttributeValues(int attributeId) {
        String sql = "DELETE FROM ATTRIBUTE WHERE "
                + "M_ATTRIBUTE_ID = :attributeId ";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeId", attributeId);
        
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    @Override
    public void addAttributeValues(int attributeId, DataDictEntity ownerEntity, List<String> values) {
        String sql = "INSERT INTO ATTRIBUTE VALUES(:attributeId, :ownerId, :value, :ownerType)";

        Map<String, Object>[] batchValues = new HashMap[values.size()];

        for (int i = 0; i < values.size(); i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("attributeId", attributeId);
            params.put("ownerId", ownerEntity.getId());
            params.put("value", values.get(i));
            params.put("ownerType", ownerEntity.getType().toString());
            batchValues[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    @Override
    public List<AttributeValue> getByOwner(DataDictEntity owner) {
 String sql = "SELECT "
                + "ATTRIBUTE.* "
                + "FROM ATTRIBUTE "
                + "WHERE PARENT_TYPE = :parentType AND DATAELEM_ID = :ddEntityId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentType", owner.getType().toString());
        params.put("ddEntityId", owner.getId());
        
        try {
            return this.getNamedParameterJdbcTemplate().query(sql, params, new AttributeValueRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }    }
    
    public static class AttributeValueRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            AttributeValue attrValue = new AttributeValue();
            attrValue.setAttributeId(rs.getInt("M_ATTRIBUTE_ID"));
            String parentType = rs.getString("PARENT_TYPE");
            Integer parentId = rs.getInt("DATAELEM_ID");
            DataDictEntity parentEntity = new DataDictEntity(parentId, DataDictEntity.Entity.getFromString(parentType));
            attrValue.setParentEntity(parentEntity);
            attrValue.setValue(rs.getString("VALUE"));
            return attrValue;
        }
        
    }
    
}
