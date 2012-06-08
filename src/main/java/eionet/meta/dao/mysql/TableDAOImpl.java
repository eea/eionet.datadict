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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.ITableDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.service.data.TableFilter;

/**
 * Table DAO.
 *
 * @author Juhan Voolaid
 */
@Repository
public class TableDAOImpl extends GeneralDAOImpl implements ITableDAO {

    /** Dynamic attribute id of the "name" attribute in database. */
    private static final int NAME_ATTR_ID = 1;

    @Override
    public List<DataSetTable> searchTables(TableFilter tableFilter) {
        StringBuilder sql = new StringBuilder();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nameAttrId", NAME_ATTR_ID);
        params.put("parentType", DElemAttribute.ParentType.TABLE.toString());

        sql.append("select dst.TABLE_ID, dst.SHORT_NAME, ds.SHORT_NAME as datasetName, ds.REG_STATUS, ");
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
        for (Attribute attr: tableFilter.getAttributes()) {
            if (StringUtils.isNotEmpty(attr.getValue())) {
                attributesExist = true;
                break;
            }
        }

        if (attributesExist) {
            sql.append("and dst.TABLE_ID in ( ");
            sql.append("select a.DATAELEM_ID from ATTRIBUTE a where a.PARENT_TYPE = :parentType ");
            for (int i = 0; i < tableFilter.getAttributes().size(); i++) {
                Attribute a = tableFilter.getAttributes().get(i);
                String idKey = "attrId" + i;
                String valueKey = "attrValue" + i;
                if (StringUtils.isNotEmpty(a.getValue())) {
                    sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " and a.VALUE like :" + valueKey);
                    sql.append(") ");
                }

                params.put(idKey, a.getId());
                params.put(valueKey, "%" + a.getValue() + "%");
            }
            sql.append(") ");
        }

        sql.append("order by dst.SHORT_NAME ");

        List<DataSetTable> resultList = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<DataSetTable>() {
            public DataSetTable mapRow(ResultSet rs, int rowNum) throws SQLException {
                DataSetTable table = new DataSetTable();
                table.setId(rs.getInt("dst.TABLE_ID"));
                table.setShortName(rs.getString("dst.SHORT_NAME"));
                table.setDataSetStatus(rs.getString("ds.REG_STATUS"));
                table.setName(rs.getString("fullName"));
                table.setDataSetName(rs.getString("datasetName"));
                return table;
            }
        });


        return resultList;
    }

}
