package eionet.datadict.dal.impl;

import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.impl.converters.BooleanToMysqlEnumYesNoConverter;
import eionet.datadict.model.DataElement;
import eionet.datadict.model.DataElement.DataElementType;
import eionet.datadict.model.Namespace;
import eionet.meta.dao.domain.DatasetRegStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DataElementDaoImpl extends JdbcDaoBase implements DataElementDao {

    @Autowired
    public DataElementDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public DataElement getById(int id) {
        String sql = "SELECT * FROM DATAELEM "
                + "LEFT JOIN NAMESPACE AS NAMESPACE ON DATAELEM.NAMESPACE_ID=NAMESPACE.NAMESPACE_ID "
                + "LEFT JOIN NAMESPACE AS PARENT ON DATAELEM.PARENT_NS=PARENT.NAMESPACE_ID "
                + "LEFT JOIN NAMESPACE AS TOP ON DATAELEM.TOP_NS=TOP.NAMESPACE_ID "
                + "WHERE DATAELEM_ID= :id";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        try {    
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params, new DataElementRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public Integer getParentTableId(int elementId) {
        String sql = "SELECT TABLE_ID FROM TBL2ELEM "
                + "WHERE DATAELEM_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", elementId);
        try {    
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }
    
    public static class DataElementRowMapper implements RowMapper<DataElement> {

        @Override
        public DataElement mapRow(ResultSet rs, int i) throws SQLException {
            DataElement dataElement = new DataElement();
            dataElement.setType(DataElementType.getFromString(rs.getString("DATAELEM.TYPE")));
            
            Namespace namespace = new Namespace();
            namespace.setId(rs.getInt("DATAELEM.NAMESPACE_ID"));
            dataElement.setNamespace(namespace);
            
            namespace = new Namespace();
            int parentNs = rs.getInt("DATAELEM.PARENT_NS");
            if (!rs.wasNull()) {
                namespace.setId(parentNs);
                dataElement.setParentNS(namespace);
            } else {
                dataElement.setParentNS(null);
            }
            
            namespace = new Namespace();
            int topNs = rs.getInt("DATAELEM.TOP_NS");
            if (!rs.wasNull()) {
                namespace.setId(topNs);
                dataElement.setTopNS(namespace);
            } else {
                dataElement.setTopNS(null);
            }
            
            
            dataElement.setId(rs.getInt("DATAELEM.DATAELEM_ID"));
            dataElement.setShortName(rs.getString("DATAELEM.SHORT_NAME"));
            dataElement.setWorkingUser(rs.getString("DATAELEM.WORKING_USER"));
            dataElement.setWorkingCopy(new BooleanToMysqlEnumYesNoConverter(Boolean.FALSE).convertBack(rs.getString("DATAELEM.WORKING_COPY")));
            dataElement.setRegStatus(DatasetRegStatus.fromString(rs.getString("DATAELEM.REG_STATUS")));
            dataElement.setVersion(rs.getInt("DATAELEM.VERSION"));
            dataElement.setUser(rs.getString("DATAELEM.USER"));
            dataElement.setDate(rs.getInt("DATAELEM.DATE"));
            dataElement.setIdentifier(rs.getString("DATAELEM.IDENTIFIER"));
            dataElement.setCheckedOutCopyId(rs.getInt("DATAELEM.CHECKEDOUT_COPY_ID"));
            dataElement.setVocabularyId(rs.getInt("DATAELEM.VOCABULARY_ID"));
            return dataElement;
        }
     
    }
    
    protected void readNamespaces(ResultSet rs, DataElement dataElement) throws SQLException {
            rs.getInt("NAMESPACE.NAMESPACE_ID");
            if (rs.wasNull()) {
                return;
            }

            dataElement.getNamespace().setShortName(rs.getString("NAMESPACE.SHORT_NAME"));
            dataElement.getNamespace().setFullName(rs.getString("NAMESPACE.FULL_NAME"));
            dataElement.getNamespace().setDefinition(rs.getString("NAMESPACE.DEFINITION"));
            dataElement.getNamespace().setWorkingUser(rs.getString("NAMESPACE.WORKING_USER"));
            
            rs.getInt("PARENT.NAMESPACE_ID");
            if (rs.wasNull()) {
                return;
            }

            dataElement.getParentNS().setShortName(rs.getString("PARENT.SHORT_NAME"));
            dataElement.getParentNS().setFullName(rs.getString("PARENT.FULL_NAME"));
            dataElement.getParentNS().setDefinition(rs.getString("PARENT.DEFINITION"));
            dataElement.getParentNS().setWorkingUser(rs.getString("PARENT.WORKING_USER"));
    
            
            rs.getInt("TOP.NAMESPACE_ID");
            if(rs.wasNull()) {
                return;
            }
            
            dataElement.getTopNS().setShortName(rs.getString("TOP.SHORT_NAME"));
            dataElement.getTopNS().setFullName(rs.getString("TOP.FULL_NAME"));
            dataElement.getTopNS().setDefinition(rs.getString("TOP.DEFINITION"));
            dataElement.getTopNS().setWorkingUser(rs.getString("TOP.WORKING_USER"));
            
        }
    
}
