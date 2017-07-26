/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.data.TableFilter;
import org.springframework.jdbc.core.RowMapper;

/**
 * Table DAO.
 *
 * @author Juhan Voolaid
 */
@Repository
public class TableDAOImpl extends GeneralDAOImpl implements ITableDAO {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetTable> searchTables(TableFilter tableFilter) {
        StringBuilder sql = new StringBuilder();

        int nameAttrId = getNameAttributeId();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nameAttrId", nameAttrId);
        params.put("parentType", "T");

        sql.append("select dst.TABLE_ID, dst.SHORT_NAME, ds.SHORT_NAME as datasetName, ds.REG_STATUS, dst.IDENTIFIER, ds.IDENTIFIER, ds.DATASET_ID, ");
        sql.append("(select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID = :nameAttrId and DATAELEM_ID = dst.TABLE_ID ");
        sql.append("and PARENT_TYPE = :parentType limit 1 ) as fullName ");
        sql.append("from DS_TABLE as dst ");
        sql.append("inner join DST2TBL as dst2ds on dst2ds.TABLE_ID = dst.TABLE_ID ");
        sql.append("inner join DATASET as ds on dst2ds.DATASET_ID = ds.DATASET_ID ");
        sql.append("where ds.DELETED is null ");
        sql.append("and ds.WORKING_COPY = 'N' ");
        sql.append("");

        if (StringUtils.isNotEmpty(tableFilter.getIdentifier())) {
            sql.append("and dst.IDENTIFIER like :identifier ");
            params.put("identifier", "%" + tableFilter.getIdentifier() + "%");
        }

        if (StringUtils.isNotEmpty(tableFilter.getShortName())) {
            sql.append("and dst.SHORT_NAME like :shortName ");
            params.put("shortName", "%" + tableFilter.getShortName() + "%");
        }

        boolean attributesExist = false;
        for (Attribute attr : tableFilter.getAttributes()) {
            if (StringUtils.isNotEmpty(attr.getValue())) {
                attributesExist = true;
                break;
            }
        }

        if (attributesExist) {
            for (int i = 0; i < tableFilter.getAttributes().size(); i++) {
                Attribute a = tableFilter.getAttributes().get(i);
                String idKey = "attrId" + i;
                String valueKey = "attrValue" + i;
                if (StringUtils.isNotEmpty(a.getValue())) {
                    sql.append("and dst.TABLE_ID in ( ");
                    sql.append("select a.DATAELEM_ID from ATTRIBUTE a where a.PARENT_TYPE = :parentType ");
                    sql.append(" and a.M_ATTRIBUTE_ID = :" + idKey + " and a.VALUE like :" + valueKey);
                    sql.append(") ");
                    params.put(idKey, a.getId());
                    params.put(valueKey, "%" + a.getValue() + "%");
                }

            }
        }

        sql.append("order by ds.IDENTIFIER asc, ds.DATASET_ID desc, dst.IDENTIFIER asc, dst.TABLE_ID desc");

        final List<DataSetTable> resultList = new ArrayList<DataSetTable>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {

            String curDstID = null;
            String curDstIdf = null;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                DataSetTable table = new DataSetTable();
                table.setId(rs.getInt("dst.TABLE_ID"));
                table.setShortName(rs.getString("dst.SHORT_NAME"));
                table.setDataSetStatus(rs.getString("ds.REG_STATUS"));
                table.setName(rs.getString("fullName"));
                table.setDataSetName(rs.getString("datasetName"));

                // skip tables that do not actually exist (ie trash from some erroneous situation)
                if (StringUtils.isEmpty(rs.getString("dst.IDENTIFIER"))) {
                    return;
                }

                String dstID = rs.getString("ds.DATASET_ID");
                String dstIdf = rs.getString("ds.IDENTIFIER");
                if (dstID == null && dstIdf == null) {
                    return;
                }

                // Adding only the most recent version of data set table into the result
                if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                    curDstIdf = dstIdf;
                    curDstID = dstID;
                } else if (!dstID.equals(curDstID)) {
                    return;
                }
                resultList.add(table);
            }

        });
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSetTable> listForDatasets(List<DataSet> datasets) {
        StringBuilder sql = new StringBuilder();

        int nameAttrId = getNameAttributeId();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nameAttrId", nameAttrId);
        params.put("parentType", "T");

        sql.append("select dst.TABLE_ID, dst.SHORT_NAME, ds.REG_STATUS, dst.IDENTIFIER, ds.IDENTIFIER, ds.DATASET_ID, ");
        sql.append("(select VALUE from ATTRIBUTE where M_ATTRIBUTE_ID = :nameAttrId and DATAELEM_ID = dst.TABLE_ID ");
        sql.append("and PARENT_TYPE = :parentType limit 1 ) as fullName ");
        sql.append("from DS_TABLE as dst ");
        sql.append("inner join DST2TBL as dst2ds on dst2ds.TABLE_ID = dst.TABLE_ID ");
        sql.append("inner join DATASET as ds on dst2ds.DATASET_ID = ds.DATASET_ID ");
        sql.append("where ds.DELETED is null ");
        sql.append("and ds.WORKING_COPY = 'N' ");

        //set dataset filters
        if (datasets != null && datasets.size()>0) {
            sql.append("and ds.DATASET_ID IN( :datasetIds ) ");
            params.put("datasetIds", CollectionUtils.collect(datasets, new BeanToPropertyValueTransformer("id")));
        }

        sql.append("order by ds.IDENTIFIER asc, ds.DATASET_ID desc, dst.IDENTIFIER asc, dst.TABLE_ID desc");

        final List<DataSetTable> resultList = new ArrayList<DataSetTable>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                DataSetTable table = new DataSetTable();
                table.setId(rs.getInt("dst.TABLE_ID"));
                table.setShortName(rs.getString("dst.SHORT_NAME"));
                table.setName(rs.getString("fullName"));
                table.setDataSetStatus(rs.getString("ds.REG_STATUS"));

                // skip tables that do not actually exist (ie trash from some erroneous situation)
                if (StringUtils.isEmpty(rs.getString("dst.IDENTIFIER"))) {
                    return;
                }
                table.setIdentifier(rs.getString("dst.IDENTIFIER"));
                resultList.add(table);
            }
        });
        return resultList;
    }

    @Override
    public String getIdentifierById(int id) {
        String sql = "select IDENTIFIER from DS_TABLE where TABLE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        List<String> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("IDENTIFIER");
            }
        });
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Integer> getOrphanTableIds() {
        String sql = 
                "select distinct DS_TABLE.TABLE_ID from DS_TABLE left join DST2TBL on DS_TABLE.TABLE_ID = DST2TBL.TABLE_ID " +
                "where DST2TBL.TABLE_ID is null";
        
        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    @Override
    public int delete(List<Integer> ids) {
        String sql = "delete from DS_TABLE where TABLE_ID in (:ids)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        return getNamedParameterJdbcTemplate().update(sql, params);
    }

}
