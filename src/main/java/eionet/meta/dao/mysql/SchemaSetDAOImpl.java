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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.dao.domain.SchemaSet.RegStatus;
import eionet.meta.service.data.SchemaSetFilter;
import eionet.meta.service.data.SchemaSetsResult;
import eionet.util.Util;

/**
 * SchemaSet DAO implementation.
 *
 * @author Juhan Voolaid
 */
@Repository
public class SchemaSetDAOImpl extends GeneralDAOImpl implements ISchemaSetDAO {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SchemaSetDAOImpl.class);

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getSchemaSets(eionet.meta.service.data.PagedRequest)
     */
    @Override
    public SchemaSetsResult searchSchemaSets(SchemaSetFilter searchFilter) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SQL_CALC_FOUND_ROWS SCHEMA_SET_ID, IDENTIFIER, CONTINUITY_ID, REG_STATUS, WORKING_COPY, ");
        sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID ");
        sql.append("FROM T_SCHEMA_SET ");

        Map<String, Object> parameters = new HashMap<String, Object>();
        // Where clause
        if (searchFilter.isValued()) {
            boolean andOperator = false;
            sql.append("WHERE ");
            if (StringUtils.isNotEmpty(searchFilter.getIdentifier())) {
                sql.append("IDENTIFIER like :identifier ");
                String identifier = "%" + searchFilter.getIdentifier() + "%";
                parameters.put("identifier", identifier);
                andOperator = true;
            }
            if (StringUtils.isNotEmpty(searchFilter.getRegStatus())) {
                if (andOperator) {
                    sql.append("AND ");
                }
                sql.append("REG_STATUS = :regStatus ");
                parameters.put("regStatus", searchFilter.getRegStatus());
                andOperator = true;
            }
        }

        // Sorting
        if (StringUtils.isNotEmpty(searchFilter.getSortProperty())) {
            sql.append("ORDER BY ").append(searchFilter.getSortProperty());
            if (SortOrderEnum.ASCENDING.equals(searchFilter.getSortOrder())) {
                sql.append(" ASC ");
            } else {
                sql.append(" DESC ");
            }
        }
        sql.append("LIMIT ").append(searchFilter.getOffset()).append(",").append(searchFilter.getPageSize());

        // LOGGER.debug("SQL: " + sql.toString());

        List<SchemaSet> items = getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getDate("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        SchemaSetsResult result = new SchemaSetsResult(items, totalItems, searchFilter);
        return result;
    }

    @Override
    public List<SchemaSet> getSchemaSets(boolean limited) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SCHEMA_SET_ID, IDENTIFIER, CONTINUITY_ID, REG_STATUS, WORKING_COPY, ");
        sql.append("WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID ");
        sql.append("FROM T_SCHEMA_SET ");

        Map<String, Object> parameters = new HashMap<String, Object>();

        if (limited) {
            parameters.put("regStatus", SchemaSet.RegStatus.RELEASED.toString());
            sql.append("WHERE REG_STATUS = :regStatus ");
        }

        sql.append("ORDER BY IDENTIFIER");

        List<SchemaSet> items = getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getDate("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });

        return items;
    }

    @Override
    public void deleteSchemaSets(List<Integer> ids) {
        String sql = "DELETE FROM T_SCHEMA_SET WHERE SCHEMA_SET_ID IN (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    @Override
    public void deleteAttributes(List<Integer> ids) {
        String sql = "DELETE FROM ATTRIBUTE WHERE DATAELEM_ID IN (:ids) AND PARENT_TYPE = :parentType";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        parameters.put("parentType", "scs");

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    @Override
    public SchemaSet getSchemaSet(int id) {
        String sql = "select * from T_SCHEMA_SET where SCHEMA_SET_ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", id);

        SchemaSet result = getNamedParameterJdbcTemplate().queryForObject(sql, parameters, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getDate("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });
        return result;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#createSchemaSet(eionet.meta.dao.domain.SchemaSet)
     */
    @Override
    public int createSchemaSet(SchemaSet schemaSet) {
        String insertSql =
            "insert into T_SCHEMA_SET (IDENTIFIER, CONTINUITY_ID, REG_STATUS, "
            + "WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID) "
            + "values (:identifier,  :continuityId, :regStatus, :workingCopy, :workingUser, now(), :userModified, :comment, :checkedOutCopyId)";

        String continuityId = schemaSet.getContinuityId();
        if (StringUtils.isBlank(continuityId)) {
            continuityId = Util.generateContinuityId(schemaSet);
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", schemaSet.getIdentifier());
        parameters.put("continuityId", continuityId);
        parameters.put("regStatus", schemaSet.getRegStatus().toString());
        parameters.put("workingCopy", schemaSet.isWorkingCopy());
        parameters.put("workingUser", schemaSet.getWorkingUser());
        parameters.put("userModified", schemaSet.getUserModified());
        parameters.put("comment", schemaSet.getComment());
        parameters.put("checkedOutCopyId", schemaSet.getCheckedOutCopyId());

        getNamedParameterJdbcTemplate().update(insertSql, parameters);

        String idSql = "select last_insert_id()";
        int id = getJdbcTemplate().queryForInt(idSql);

        return id;
    }

    @Override
    public void updateSchemaSet(SchemaSet schemaSet) {
        String sql =
            "update T_SCHEMA_SET set IDENTIFIER = :identifier, REG_STATUS = :regStatus, DATE_MODIFIED = now(), USER_MODIFIED = :userModified, "
            + "COMMENT=ifnull(:comment, COMMENT) where SCHEMA_SET_ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", schemaSet.getId());
        parameters.put("identifier", schemaSet.getIdentifier());
        parameters.put("regStatus", schemaSet.getRegStatus().toString());
        parameters.put("userModified", schemaSet.getUserModified());
        parameters.put("comment", schemaSet.getComment());

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    @Override
    public void updateSchemaSetAttributes(int schemaSetId, Map<Integer, Set<String>> attributes) {
        if (attributes == null) {
            return;
        }

        String deleteSql =
            "delete from ATTRIBUTE where M_ATTRIBUTE_ID = :attributeId and DATAELEM_ID = :elementId and PARENT_TYPE = :parentType";
        String insertSql =
            "insert into ATTRIBUTE (M_ATTRIBUTE_ID, DATAELEM_ID, PARENT_TYPE, VALUE) values (:attributeId,:elementId,:parentType,:value)";

        for (Map.Entry<Integer, Set<String>> entry : attributes.entrySet()) {
            Integer attrId = entry.getKey();
            Set<String> attrValues = entry.getValue();
            if (attrValues != null && !attrValues.isEmpty()) {

                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("attributeId", attrId);
                parameters.put("elementId", schemaSetId);
                parameters.put("parentType", DElemAttribute.ParentType.SCHEMA_SET.toString());

                getNamedParameterJdbcTemplate().update(deleteSql, parameters);

                for (String attrValue : attrValues) {
                    parameters.put("value", attrValue);
                    getNamedParameterJdbcTemplate().update(insertSql, parameters);
                }
            }
        }
    }

    @Override
    public void checkInSchemaSet(SchemaSet schemaSet, String username, String comment) {
        int checkedOutCopyId = schemaSet.getCheckedOutCopyId();
        if (checkedOutCopyId > 0) {
            // Unlocks checked out copy
            String sql = "update T_SCHEMA_SET set WORKING_USER=NULL where SCHEMA_SET_ID=?";
            getJdbcTemplate().update(sql, checkedOutCopyId);
        }

        // Unlocks working copy
        String sql =
            "update T_SCHEMA_SET set WORKING_USER = NULL, WORKING_COPY = 0, DATE_MODIFIED = now(), USER_MODIFIED = :username, "
            + "COMMENT = :comment where SCHEMA_SET_ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", schemaSet.getId());
        parameters.put("username", username);
        parameters.put("comment", comment);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

}
