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
import org.apache.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.SchemaSet.RegStatus;
import eionet.meta.service.data.PagedRequest;
import eionet.meta.service.data.SchemaSetsResult;

/**
 * SchemaSet DAO implementation.
 *
 * @author Juhan Voolaid
 */
@Repository
public class SchemaSetDAOImpl extends GeneralDAOImpl implements ISchemaSetDAO {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SchemaSetDAOImpl.class);

    @Override
    public SchemaSetsResult getSchemaSets(PagedRequest pagedRequest) {

        String totalSql = "SELECT COUNT(*) FROM SCHEMA_SET";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SCHEMA_SET_ID, IDENTIFIER, CONTINUITY_ID, REG_STATUS, WORKING_COPY, ");
        sql.append("WORKING_USER, DATE, USER, COMMENT, CHECKEDOUT_COPY_ID ");
        sql.append("FROM SCHEMA_SET ");
        if (StringUtils.isNotEmpty(pagedRequest.getSortProperty())) {
            sql.append("ORDER BY ").append(pagedRequest.getSortProperty());
            if (SortOrderEnum.ASCENDING.equals(pagedRequest.getSortOrder())) {
                sql.append(" ASC ");
            } else {
                sql.append(" DESC ");
            }
        }
        sql.append("LIMIT ").append(pagedRequest.getOffset()).append(",").append(pagedRequest.getPageSize());

        List<SchemaSet> items = getJdbcTemplate().query(sql.toString(), new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDate(rs.getDate("DATE"));
                ss.setUser(rs.getString("USER"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });

        LOGGER.debug("SQL: " + sql);

        SchemaSetsResult result = new SchemaSetsResult(items, totalItems, pagedRequest);
        return result;
    }

    @Override
    public void deleteSchemaSets(List<Integer> ids) {
        String sql = "DELETE FROM SCHEMA_SET WHERE SCHEMA_SET_ID IN (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

}
