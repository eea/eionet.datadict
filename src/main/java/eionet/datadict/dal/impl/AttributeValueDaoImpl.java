package eionet.datadict.dal.impl;


import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
    public void addAttributeValue(int attributeId, DataDictEntity ownerEntity, String value) throws DuplicateResourceException {
        String sql = "INSERT INTO ATTRIBUTE VALUES(:attributeId, :ownerId, :value, :ownerType)";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeId", attributeId);
        params.put("ownerId", ownerEntity.getId());
        params.put("value", value);
        params.put("ownerType", ownerEntity.getType().toString());
        
        try {
            this.getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException ex){
            if (ex.getCause() instanceof SQLException){
                if (((SQLException)ex.getCause()).getErrorCode() == 1062){
                    throw new DuplicateResourceException("Attribute value already exists for this "+ownerEntity.getType().getLabel().toLowerCase()+".");
                }
            }
            throw ex;
        }
    }
    
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
