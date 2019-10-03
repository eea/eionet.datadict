package eionet.datadict.dal.impl;

import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.impl.converters.BooleanToMysqlEnumYesNoConverter;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.Namespace;
import eionet.meta.dao.domain.DatasetRegStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public void updateDataSet(DataSet dataSet) {
        String sql = "UPDATE datadict.DATASET\n"
                + "SET SHORT_NAME= :short_name,"
                + " VERSION= :version, "
                + "VISUAL= :visual,"
                + " DETAILED_VISUAL= :detailed_visual,"
                + " WORKING_USER= :working_user,"
                + " WORKING_COPY= :working_copy,"
                + " REG_STATUS= :reg_status,"
                + " `DATE`= :date, "
                + "`USER`= :user,"
                + " CORRESP_NS= :corresp_ns"
                + ", DELETED= :deleted, "
                + "IDENTIFIER= :identifier,"
                + " DISP_DOWNLOAD_LINKS= :disp_download_links,"
                + " CHECKEDOUT_COPY_ID= :checkout_copy_id, "
                + "SUCCESSOR= :successor\n"
                + "WHERE DATASET_ID= :datasetId;";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("datasetId", dataSet.getId());
        params.put("short_name", dataSet.getShortName());
        params.put("version", dataSet.getVersion());
        params.put("visual", dataSet.getVisual());
        params.put("detailed_visual", dataSet.getDetailedVisual());
        params.put("working_user", dataSet.getWorkingUser());
        params.put("working_copy", dataSet.getWorkingCopy());
        params.put("reg_status", dataSet.getRegStatus());
        params.put("date", dataSet.getDate());
        params.put("user", dataSet.getUser());
        params.put("deleted", dataSet.getDeleted());
        params.put("identifier", dataSet.getIdentifier());
        params.put("dispDownloadLinks", dataSet.getSerializedDisplayDownloadLinks());
        params.put("checkout_copy_id", dataSet.getCheckedOutCopyId());
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        getNamedParameterJdbcTemplate().update(sql, parameterMap);
    }

    @Override
    public void updateDataSetDispDownloadLinks(int id, String dispDownloadLinks) {
        String sql = "UPDATE DATASET  SET DISPLAY_DOWNLOAD_LINKS= :disp_download_links WHERE DATASET_ID= :datasetId;";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("datasetId", id);

        params.put("disp_download_links", dispDownloadLinks);
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        getNamedParameterJdbcTemplate().update(sql, parameterMap);
    }

    @Override
    public List<DataSet> getByIdentifierAndWorkingCopyAndRegStatusesOrderByIdentifierAscAndIdDesc(String datasetIdentifier, boolean workingCopy, List<DatasetRegStatus> statuses) {
         String sql = "select * "
                + "FROM DATASET "
                + "WHERE DATASET.DELETED is null and DATASET.IDENTIFIER= :identifier and DATASET.WORKING_COPY= :workingCopy andRegStatusIn order by DATASET.IDENTIFIER asc, DATASET.DATASET_ID desc";
        String regStatuses="";
        String partialRegStatusInSqlStatement ="and REG_STATUS IN (regStatusesValues)";
      if(!statuses.isEmpty()){
        for (DatasetRegStatus status : statuses) {
            regStatuses=regStatuses.concat("'"+status.getName()+"',");
        }
       if(regStatuses.endsWith(","))
         {
          regStatuses = regStatuses.substring(0,regStatuses.length() - 1);
         }
        partialRegStatusInSqlStatement = partialRegStatusInSqlStatement.replace("regStatusesValues",regStatuses);  
        sql = sql.replace("andRegStatusIn", partialRegStatusInSqlStatement);
      }
      else{
      sql = sql.replace("andRegStatusIn","");
      }
       Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", datasetIdentifier);
        params.put("workingCopy",workingCopy ? "Y" :"N");
        
        try {
            return this.getNamedParameterJdbcTemplate().query(sql, params, new DataSetRowMapper());
        } catch (EmptyResultDataAccessException ex) {
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
            dataset.setDate(rs.getLong("DATASET.DATE"));
            dataset.setUser(rs.getString("DATASET.USER"));
            dataset.setDeleted(rs.getString("DATASET.DELETED"));
            dataset.setIdentifier(rs.getString("DATASET.IDENTIFIER"));
            dataset.setDispCreateLinks(rs.getInt("DATASET.DISP_CREATE_LINKS"));
            dataset.setRegStatus(DatasetRegStatus.fromString(rs.getString("DATASET.REG_STATUS")));
            dataset.setSerializedDisplayDownloadLinks(rs.getString("DISPLAY_DOWNLOAD_LINKS"));
            Integer checkedoutCopyId = rs.getInt("DATASET.CHECKEDOUT_COPY_ID");
            if (rs.wasNull()) {
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
