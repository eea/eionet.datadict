package eionet.datadict.dal.impl;

import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.impl.converters.BooleanToMysqlEnumYesNoConverter;
import eionet.datadict.model.DataSet;
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
public class DatasetDaoImpl extends JdbcDaoBase implements DatasetDao {

    @Autowired
    public DatasetDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public DataSet getById(int id) {
        String sql = "select "
                + "DATASET.*, "
                + "NAMESPACE.* "
                + "FROM DATASET "
                + "LEFT JOIN NAMESPACE ON DATASET.CORRESP_NS = NAMESPACE.NAMESPACE_ID "
                + "WHERE DATASET_ID = :id";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        try {
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params, new DataSetRowMapper());
        }catch (EmptyResultDataAccessException ex) {
            return null;
        }          
    }
    
    public static class DataSetRowMapper implements RowMapper<DataSet> {

        @Override
        public DataSet mapRow(ResultSet rs, int i) throws SQLException {
            DataSet dataset = new DataSet();
            dataset.setId(rs.getInt("DATASET.DATASET_ID"));
            dataset.setShortName(rs.getString("DATASET.SHORT_NAME"));
            dataset.setVersion(rs.getInt("DATASET.VERSION"));
            dataset.setVisual(rs.getString("DATASET.VISUAL"));
            dataset.setDetailedVisual(rs.getString("DATASET.DETAILED_VISUAL"));
            dataset.setWorkingUser(rs.getString("DATASET.WORKING_USER"));
            dataset.setWorkingCopy(new BooleanToMysqlEnumYesNoConverter(Boolean.FALSE).convertBack(rs.getString("DATASET.WORKING_COPY")));
            dataset.setDate(rs.getInt("DATASET.DATE"));
            dataset.setUser(rs.getString("DATASET.USER"));
            dataset.setDeleted(rs.getString("DATASET.DELETED"));
            dataset.setIdentifier(rs.getString("DATASET.IDENTIFIER"));
            dataset.setDispCreateLinks(rs.getInt("DATASET.DISP_CREATE_LINKS"));
            dataset.setRegStatus(DatasetRegStatus.fromString(rs.getString("DATASET.REG_STATUS")));
            
            Integer checkedoutCopyId = rs.getInt("DATASET.CHECKEDOUT_COPY_ID");
            if (rs.wasNull()){
                checkedoutCopyId = null;
            }
            dataset.setCheckedOutCopyId(checkedoutCopyId);
            
            int namespaceId = rs.getInt("DATASET.CORRESP_NS");
            if (!rs.wasNull()) {
                Namespace namespace = new Namespace();
                namespace.setId(namespaceId);
                dataset.setCorrespondingNS(namespace);
            }
            return dataset;
        }
        
        protected void readNamespace(ResultSet rs, DataSet dataset) throws SQLException {
            rs.getInt("NAMESPACE.NAMESPACE_ID");

            if (rs.wasNull()) {
                return;
            }

            dataset.getCorrespondingNS().setShortName(rs.getString("NAMESPACE.SHORT_NAME"));
            dataset.getCorrespondingNS().setFullName(rs.getString("NAMESPACE.FULL_NAME"));
            dataset.getCorrespondingNS().setDefinition(rs.getString("NAMESPACE.DEFINITION"));
            dataset.getCorrespondingNS().setWorkingUser(rs.getString("NAMESPACE.WORKING_USER"));
        }

        
 
    }
    
}
