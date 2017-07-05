package eionet.datadict.dal.impl;

import eionet.datadict.dal.FixedValuesDao;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.AttributeValue;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.FixedValue;
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
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Repository
public class FixedValuesDaoImpl extends JdbcDaoBase implements FixedValuesDao {

    @Autowired
    public FixedValuesDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<FixedValue> getValueListCodesOfDataElementsInTable(int tableId) {
        String sql = "select * "
                + "from FXV "
                + "where OWNER_TYPE = :ownerType and OWNER_ID = :tableId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableId", tableId);
        
        return null;
        
    }

    
        public static class FixedValueRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            FixedValue fixedValue = new FixedValue();
              fixedValue.setId(rs.getInt("FXV_ID"));
                fixedValue.setValue(rs.getString("VALUE"));
                 String ownerType = rs.getString("OWNER_TYPE");
            Integer ownerId = rs.getInt("OWNER_ID");
        //    DataDictEntity parentEntity = new DataDictEntity(ownerId, DataDictEntity.Entity.getFromString(parentType));
            /**
            switch(DataDictEntity.Entity.getFromString(parentType)) {
            
                case DS:  attrValue.setOwner((DataSet)new DataSet(parentId));
                          break;
               case T:  attrValue.setOwner((DatasetTable)new DatasetTable(parentId));
                          break;
            
            }
            **/
           // attrValue.setParentEntity(parentEntity);
         //   attrValue.setValue(rs.getString("VALUE"));
            Attribute at= new Attribute();
            at.setId(rs.getInt("M_ATTRIBUTE_ID"));
         //   attrValue.setAttribute(at);
          //  return attrValue;
          return null;
        }

    }
}
