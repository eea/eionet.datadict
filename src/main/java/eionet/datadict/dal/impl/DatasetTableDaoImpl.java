package eionet.datadict.dal.impl;

import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.dal.impl.converters.BooleanToMysqlEnumYesNoConverter;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class DatasetTableDaoImpl extends JdbcDaoBase implements DatasetTableDao {
   
    @Autowired
    public DatasetTableDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public DatasetTable getById(int id) {
        String sql = "SELECT * FROM "
                + "DS_TABLE "
                + "LEFT JOIN NAMESPACE AS CORRESP  ON DS_TABLE.CORRESP_NS=CORRESP.NAMESPACE_ID "
                + "LEFT JOIN NAMESPACE AS PARENT ON DS_TABLE.PARENT_NS=PARENT.NAMESPACE_ID "
                + "WHERE TABLE_ID = :id";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        try {
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params, new DatasetTableRowMapper());
        }catch (EmptyResultDataAccessException ex) {
            return null;
        }          
    }

    @Override
    public Integer getParentDatasetId(int tableId) {
        String sql = "SELECT DATASET_ID FROM DST2TBL "
                + "WHERE TABLE_ID =  :tableId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableId", tableId);
        try {
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);
        }catch (EmptyResultDataAccessException ex) {
            return null;
        }          
    }

    @Override
    public List<DatasetTable> getAllByDatasetId(int datasetId) {
         List<DatasetTable> dsTables = new ArrayList<DatasetTable>();
         String sql = "SELECT TABLE_ID ,POSITION FROM DST2TBL "
                + "WHERE  DATASET_ID=  :datasetId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("datasetId", datasetId);
        try {
             List<TableIdAndPosition> idsAndPositions = this.getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<TableIdAndPosition>() {
                @Override
                public TableIdAndPosition mapRow(ResultSet rs, int rowNum) throws SQLException {
                    TableIdAndPosition tableIdAndPosition = new TableIdAndPosition();
                    tableIdAndPosition.setTableId(rs.getInt("TABLE_ID"));
                    tableIdAndPosition.setPosition(rs.getInt("POSITION"));
                    return tableIdAndPosition;
                }
            });
            for (TableIdAndPosition idAndPosition : idsAndPositions) {
                DatasetTable dsTable = this.getById(idAndPosition.getTableId());
                dsTable.setPosition(idAndPosition.getPosition());
                dsTables.add(dsTable);
            }
             
            return dsTables;
        }catch (EmptyResultDataAccessException ex) {
            return null;
        } 
    }
    
    public static class DatasetTableRowMapper implements RowMapper<DatasetTable> {

        @Override
        public DatasetTable mapRow(ResultSet rs, int i) throws SQLException {
            DatasetTable datasetTable = new DatasetTable();
            datasetTable.setId(rs.getInt("DS_TABLE.TABLE_ID"));
            datasetTable.setShortName(rs.getString("DS_TABLE.SHORT_NAME"));
            datasetTable.setName(rs.getString("DS_TABLE.NAME"));
            datasetTable.setWorkingCopy(new BooleanToMysqlEnumYesNoConverter(Boolean.FALSE).convertBack(rs.getString("DS_TABLE.WORKING_COPY")));           
            datasetTable.setWorkingUser(rs.getString("DS_TABLE.WORKING_USER"));
            datasetTable.setVersion(rs.getInt("DS_TABLE.VERSION"));
            datasetTable.setDate(rs.getLong("DS_TABLE.DATE"));
            datasetTable.setUser(rs.getString("DS_TABLE.USER"));
            datasetTable.setIdentifier(rs.getString("DS_TABLE.IDENTIFIER"));
            
            int namespaceId = rs.getInt("DS_TABLE.CORRESP_NS");
            if(!rs.wasNull()){
                Namespace namespace = new Namespace();
                namespace.setId(namespaceId);
                datasetTable.setCorrespondingNS(namespace);
            }
            return datasetTable;
        }
        
    }
    
    protected void readNamespaces(ResultSet rs, DatasetTable datasetTable) throws SQLException {
            rs.getInt("CORRESP.NAMESPACE_ID");
            if (rs.wasNull()) {
                return;
            }

            datasetTable.getCorrespondingNS().setShortName(rs.getString("CORRESP.SHORT_NAME"));
            datasetTable.getCorrespondingNS().setFullName(rs.getString("CORRESP.FULL_NAME"));
            datasetTable.getCorrespondingNS().setDefinition(rs.getString("CORRESP.DEFINITION"));
            datasetTable.getCorrespondingNS().setWorkingUser(rs.getString("CORRESP.WORKING_USER"));
            
            rs.getInt("PARENT.NAMESPACE_ID");
            if(rs.wasNull()) {
                return;
            }
            
            datasetTable.getParentNamespace().setShortName(rs.getString("PARENT.SHORT_NAME"));
            datasetTable.getParentNamespace().setFullName(rs.getString("PARENT.FULL_NAME"));
            datasetTable.getParentNamespace().setDefinition(rs.getString("PARENT.DEFINITION"));
            datasetTable.getParentNamespace().setWorkingUser(rs.getString("PARENT.WORKING_USER"));
            
        }
    
    private class TableIdAndPosition{
        private Integer tableId;
        private Integer position;

        public Integer getTableId() {
            return tableId;
        }

        public void setTableId(Integer tableId) {
            this.tableId = tableId;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }
        
    }
        
}
