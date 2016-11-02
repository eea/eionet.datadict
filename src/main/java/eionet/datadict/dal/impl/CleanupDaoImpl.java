package eionet.datadict.dal.impl;

import eionet.datadict.dal.CleanupDao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CleanupDaoImpl extends JdbcDaoBase implements CleanupDao {
 
    @Autowired
    public CleanupDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public int deleteBrokenDatasetToTableRelations() {
        // delete DST2TBL relations where the dataset or the table does not actually exist
        String sql = 
                "delete DST2TBL from DST2TBL left join DATASET on DST2TBL.DATASET_ID = DATASET.DATASET_ID " +
                "left join DS_TABLE on DST2TBL.TABLE_ID = DS_TABLE.TABLE_ID " + 
                "where DATASET.DATASET_ID is null or DS_TABLE.TABLE_ID is null";
        return getJdbcTemplate().update(sql);
    }

    @Override
    public int deleteBrokenTableToElementRelations() {
        // delete TBL2ELEM relations where the table or the element does not actually exist
        String sql = 
                "delete TBL2ELEM from TBL2ELEM left join DS_TABLE on TBL2ELEM.TABLE_ID = DS_TABLE.TABLE_ID " +
                "left join DATAELEM on TBL2ELEM.DATAELEM_ID = DATAELEM.DATAELEM_ID " + 
                "where DS_TABLE.TABLE_ID is null or DATAELEM.DATAELEM_ID is null";
        return getJdbcTemplate().update(sql);
    }

    @Override
    public int deleteDatasetToTableRelations(List<Integer> ids) {
        String sql = "delete from DST2TBL where TABLE_ID in (:ids)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteTableRelationsWithElements(List<Integer> tableIds) {
        String sql = "delete from TBL2ELEM where TABLE_ID in (:tableIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableIds", tableIds);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteElementRelationsWithTables(List<Integer> elementIds) {
        String sql = "delete from TBL2ELEM where DATAELEM_ID in (:elementIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("elementIds", elementIds);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteDocs(String ownerType, List<Integer> ids) {
        String sql = "delete from DOC where OWNER_TYPE = :ownerType and OWNER_ID in (:ids)";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerType", ownerType);
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteForeignKeyRelations(List<Integer> ids) {
        String sql = "delete from FK_RELATION where A_ID in (:ids) or B_ID in (:ids)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteInferenceRules(List<Integer> ids) {
        String sql = "delete from INFERENCE_RULE where DATAELEM_ID in (:ids) or TARGET_ELEM_ID in (:ids)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int deleteOrphanNamespaces() {
        // delete NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table
        String sql = 
                "delete NAMESPACE from NAMESPACE left join DATASET on NAMESPACE.NAMESPACE_ID = DATASET.CORRESP_NS " +
                "left join DS_TABLE on NAMESPACE.NAMESPACE_ID = DS_TABLE.CORRESP_NS " + 
                "where DATASET.CORRESP_NS is null and DS_TABLE.CORRESP_NS is null";
        return getJdbcTemplate().update(sql);
    }

    @Override
    public int deleteOrphanAcls() {
        String sql = 
                "delete ACLS, ACL_ROWS from ACLS join ACL_ROWS on ACLS.ACL_ID = ACL_ROWS.ACL_ID left join DATASET on ACLS.ACL_NAME = DATASET.IDENTIFIER " +
                "left join DATAELEM on ACLS.ACL_NAME = DATAELEM.IDENTIFIER " + 
                "where (ACLS.PARENT_NAME='/datasets' and DATASET.IDENTIFIER is null) " +
                "or (ACLS.PARENT_NAME='/elements' and DATAELEM.PARENT_NS is null and DATAELEM.IDENTIFIER is null)";
        return getJdbcTemplate().update(sql);
    }
    
}
