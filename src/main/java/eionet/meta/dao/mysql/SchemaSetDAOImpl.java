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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DElemAttribute;
import eionet.meta.dao.ISchemaSetDAO;
import eionet.meta.dao.domain.Attribute;
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

    /** */
    private static final String GET_SCHEMA_MAPPINGS_SQL =
            "select SCHEMA1.SCHEMA_ID as ID1, SCHEMA2.SCHEMA_ID as ID2 "
                    + "from T_SCHEMA as SCHEMA1, T_SCHEMA as SCHEMA2 "
                    + "where SCHEMA1.FILENAME=SCHEMA2.FILENAME and SCHEMA1.SCHEMA_SET_ID=:schemaSetId1 and SCHEMA2.SCHEMA_SET_ID=:schemaSetId2";

    /** */
    private static final String COPY_SCHEMA_SET_ROW = "insert into T_SCHEMA_SET "
            + "(IDENTIFIER, CONTINUITY_ID, WORKING_COPY, WORKING_USER, USER_MODIFIED, CHECKEDOUT_COPY_ID) select "
            + "ifnull(:identifier,IDENTIFIER), CONTINUITY_ID, true, :userName, :userName, :checkedOutCopyId from T_SCHEMA_SET "
            + "where SCHEMA_SET_ID=:schemaSetId";

    /** */
    private static final String SET_WORKING_USER_SQL =
            "update T_SCHEMA_SET set WORKING_USER=:userName where SCHEMA_SET_ID=:schemaSetId";

    /** */
    private static final String REPLACE_ID_SQL =
            "update T_SCHEMA_SET set SCHEMA_SET_ID=:substituteId where SCHEMA_SET_ID=:replacedId";

    /** */
    private static final String GET_WORKING_COPY_OF_SQL =
            "select * from T_SCHEMA_SET where WORKING_COPY=true and CHECKEDOUT_COPY_ID = :checkedOutCopyId";

    /** */
    private static final String GET_WORKING_COPIES_SQL =
            "select * from T_SCHEMA_SET where WORKING_COPY=true and WORKING_USER=:userName order by IDENTIFIER asc";

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#searchSchemaSets(eionet.meta.service.data.SchemaSetFilter)
     */
    @Override
    public SchemaSetsResult searchSchemaSets(SchemaSetFilter searchFilter) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SQL_CALC_FOUND_ROWS ss.*, ATTRIBUTE.VALUE as NAME_ATTR ");

        sql.append("FROM T_SCHEMA_SET ss ");
        sql.append("left outer join ATTRIBUTE on ");
        sql.append("(ss.SCHEMA_SET_ID=ATTRIBUTE.DATAELEM_ID and ATTRIBUTE.PARENT_TYPE=:attrParentType ");
        sql.append("and ATTRIBUTE.M_ATTRIBUTE_ID=:nameAttrId) ");

        parameters.put("attrParentType", DElemAttribute.ParentType.SCHEMA_SET.toString());
        parameters.put("nameAttrId", NAME_ATTR_ID);

        sql.append("where ");
        String searchingUser = searchFilter.getSearchingUser();
        if (StringUtils.isBlank(searchingUser)) {
            sql.append("(ss.WORKING_COPY=false and ss.REG_STATUS=:regStatus) ");
            parameters.put("regStatus", SchemaSet.RegStatus.RELEASED.toString());
        } else {
            sql.append("(ss.WORKING_COPY=false or ss.WORKING_USER=:workingUser) ");
            parameters.put("workingUser", searchingUser);
        }

        // Where clause
        if (searchFilter.isValued()) {
            if (StringUtils.isNotEmpty(searchFilter.getIdentifier())) {
                sql.append("AND ");
                sql.append("ss.IDENTIFIER like :identifier ");
                String identifier = "%" + searchFilter.getIdentifier() + "%";
                parameters.put("identifier", identifier);
            }
            if (StringUtils.isNotEmpty(searchFilter.getRegStatus())) {
                sql.append("AND ");
                sql.append("ss.REG_STATUS = :regStatus ");
                parameters.put("regStatus", searchFilter.getRegStatus());
            }
            if (searchFilter.isAttributesValued()) {
                for (int i = 0; i < searchFilter.getAttributes().size(); i++) {
                    Attribute a = searchFilter.getAttributes().get(i);
                    String idKey = "attrId" + i;
                    String valueKey = "attrValue" + i;
                    if (StringUtils.isNotEmpty(a.getValue())) {
                        sql.append("AND ");
                        sql.append("ss.SCHEMA_SET_ID IN ( ");
                        sql.append("SELECT a.DATAELEM_ID FROM ATTRIBUTE a WHERE ");
                        sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey
                                + " AND a.PARENT_TYPE = :parentType ");
                        sql.append(") ");
                    }
                    parameters.put(idKey, a.getId());
                    String value = "%" + a.getValue() + "%";
                    parameters.put(valueKey, value);
                    parameters.put("parentType", DElemAttribute.ParentType.SCHEMA_SET.toString());
                }
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
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                ss.setNameAttribute(rs.getString("NAME_ATTR"));
                return ss;
            }
        });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        SchemaSetsResult result = new SchemaSetsResult(items, totalItems, searchFilter);
        return result;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getSchemaSets(String)
     */
    @Override
    public List<SchemaSet> getSchemaSets(String userName) {

        // Get the ID of 'Name' attribute beforehand.
        int nameAttrId = getJdbcTemplate().queryForInt("select M_ATTRIBUTE_ID from M_ATTRIBUTE where SHORT_NAME='Name'");

        // Now build the main sql, joining to ATTRIBUTE table via above-found ID of 'Name'.

        StringBuilder sql =
                new StringBuilder().append("select SCHEMA_SET_ID, IDENTIFIER, CONTINUITY_ID, REG_STATUS, WORKING_COPY, ").append(
                        "WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID ");

        if (nameAttrId > 0) {
            sql.append(",ATTRIBUTE.VALUE as NAME ");
        }

        sql.append("from T_SCHEMA_SET ");
        Map<String, Object> params = new HashMap<String, Object>();

        if (nameAttrId > 0) {

            sql.append("left outer join ATTRIBUTE on ")
                    .append("(T_SCHEMA_SET.SCHEMA_SET_ID=ATTRIBUTE.DATAELEM_ID and ATTRIBUTE.PARENT_TYPE=:attrParentType ")
                    .append("and ATTRIBUTE.M_ATTRIBUTE_ID=:nameAttrId) ");

            params.put("attrParentType", DElemAttribute.ParentType.SCHEMA_SET.toString());
            params.put("nameAttrId", nameAttrId);
        }

        sql.append("where ");

        if (StringUtils.isBlank(userName)) {
            sql.append("WORKING_COPY=FALSE ");
            // sql.append("(WORKING_COPY=FALSE and REG_STATUS = :regStatus) ");
            // params.put("regStatus", SchemaSet.RegStatus.RELEASED.toString());
        } else {
            sql.append("(WORKING_COPY=FALSE or WORKING_USER=:workingUser) ");
            params.put("workingUser", userName);
        }

        // Working copy is added to "order by" so that a working copy always comes after the original when the result list is
        // displeyd to the user.
        sql.append("order by ifnull(NAME,IDENTIFIER), SCHEMA_SET_ID");

        // Execute the main SQL, build result list.

        List<SchemaSet> items = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                String name = rs.getString("NAME");
                if (StringUtils.isNotBlank(name)) {
                    ss.setAttributeValues(Collections.singletonMap("Name", Collections.singletonList(name)));
                }
                return ss;
            }
        });

        return items;
    }

    @Override
    public List<SchemaSet> getSchemaSets(List<Integer> ids) {
        String sql = "SELECT * FROM T_SCHEMA_SET WHERE SCHEMA_SET_ID IN (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        List<SchemaSet> items = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
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
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });
        return result;
    }

    @Override
    public SchemaSet getSchemaSet(String identifier, boolean workingCopy) {
        String sql = "select * from T_SCHEMA_SET where IDENTIFIER = :identifier and WORKING_COPY = :workingCopy";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", identifier);
        parameters.put("workingCopy", workingCopy);

        SchemaSet result = getNamedParameterJdbcTemplate().queryForObject(sql, parameters, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
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
        parameters.put("checkedOutCopyId", schemaSet.getCheckedOutCopyId() <= 0 ? null : schemaSet.getCheckedOutCopyId());

        getNamedParameterJdbcTemplate().update(insertSql, parameters);
        return getLastInsertId();
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

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#checkIn(int, java.lang.String, java.lang.String)
     */
    @Override
    public void checkIn(int schemaSetId, String username, String comment) {

        String sql =
                "update T_SCHEMA_SET set WORKING_USER = NULL, WORKING_COPY = 0, DATE_MODIFIED = now(), USER_MODIFIED = :username, "
                        + "COMMENT = :comment, CHECKEDOUT_COPY_ID=NULL where SCHEMA_SET_ID = :id";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", schemaSetId);
        parameters.put("username", username);
        parameters.put("comment", comment);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getSchemaMappings(int, int)
     */
    @Override
    public Map<Integer, Integer> getSchemaMappings(int schemaSetId1, int schemaSetId2) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("schemaSetId1", schemaSetId1);
        params.put("schemaSetId2", schemaSetId2);

        final HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
        getNamedParameterJdbcTemplate().query(GET_SCHEMA_MAPPINGS_SQL, params, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                result.put(rs.getInt("ID1"), rs.getInt("ID2"));
            }
        });

        return result;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#replaceId(int, int)
     */
    @Override
    public void replaceId(int replacedId, int substituteId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("replacedId", replacedId);
        params.put("substituteId", substituteId);

        getNamedParameterJdbcTemplate().update(REPLACE_ID_SQL, params);
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getWorkingCopyOfSchemaSet(int)
     */
    @Override
    public SchemaSet getWorkingCopyOfSchemaSet(int checkedOutCopyId) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("checkedOutCopyId", checkedOutCopyId);

        SchemaSet result =
                getNamedParameterJdbcTemplate().queryForObject(GET_WORKING_COPY_OF_SQL, parameters, new RowMapper<SchemaSet>() {
                    public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SchemaSet ss = new SchemaSet();
                        ss.setId(rs.getInt("SCHEMA_SET_ID"));
                        ss.setIdentifier(rs.getString("IDENTIFIER"));
                        ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                        ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                        ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                        ss.setWorkingUser(rs.getString("WORKING_USER"));
                        ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                        ss.setUserModified(rs.getString("USER_MODIFIED"));
                        ss.setComment(rs.getString("COMMENT"));
                        ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                        return ss;
                    }
                });
        return result;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getWorkingCopiesOf(java.lang.String)
     */
    @Override
    public List<SchemaSet> getWorkingCopiesOf(String userName) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userName", userName);

        List<SchemaSet> resultList =
                getNamedParameterJdbcTemplate().query(GET_WORKING_COPIES_SQL, parameters, new RowMapper<SchemaSet>() {
                    public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SchemaSet ss = new SchemaSet();
                        ss.setId(rs.getInt("SCHEMA_SET_ID"));
                        ss.setIdentifier(rs.getString("IDENTIFIER"));
                        ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                        ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                        ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                        ss.setWorkingUser(rs.getString("WORKING_USER"));
                        ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                        ss.setUserModified(rs.getString("USER_MODIFIED"));
                        ss.setComment(rs.getString("COMMENT"));
                        ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                        return ss;
                    }
                });

        return resultList;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getSchemaFileNames(java.lang.String)
     */
    @Override
    public List<String> getSchemaFileNames(String schemaSetIdentifier) {

        List<String> resultList = null;
        boolean isRootLevelSchema = StringUtils.isBlank(schemaSetIdentifier);

        RowMapper<String> rowMapper = new RowMapper<String>() {
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString(1);
            }
        };

        if (isRootLevelSchema) {
            resultList =
                    getJdbcTemplate().query(
                            "select distinct FILENAME from T_SCHEMA where SCHEMA_SET_ID is null or SCHEMA_SET_ID <= 0", rowMapper);
        } else {
            String sql =
                    "select distinct FILENAME from T_SCHEMA, T_SCHEMA_SET "
                            + "where T_SCHEMA.SCHEMA_SET_ID = T_SCHEMA_SET.SCHEMA_SET_ID and T_SCHEMA_SET.IDENTIFIER = :schemaSetIdentifier";

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("schemaSetIdentifier", schemaSetIdentifier);
            resultList = getNamedParameterJdbcTemplate().query(sql, parameters, rowMapper);
        }

        return resultList;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#exists(java.lang.String)
     */
    @Override
    public boolean exists(String schemaSetIdentifier) {

        String sql = "select count(*) from T_SCHEMA_SET where IDENTIFIER = :identifier";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", schemaSetIdentifier);

        int count = getNamedParameterJdbcTemplate().queryForInt(sql, parameters);
        return count > 0;
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#setWorkingUser(int, java.lang.String)
     */
    @Override
    public void setWorkingUser(int schemaSetId, String userName) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("schemaSetId", schemaSetId);
        params.put("userName", userName);

        getNamedParameterJdbcTemplate().update(SET_WORKING_USER_SQL, params);
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#copySchemaSetRow(int, java.lang.String, java.lang.String)
     */
    @Override
    public int copySchemaSetRow(int schemaSetId, String userName, String newIdentifier) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters = new HashMap<String, Object>();
        parameters.put("schemaSetId", schemaSetId);
        parameters.put("userName", userName);
        parameters.put("identifier", newIdentifier);
        parameters.put("checkedOutCopyId", newIdentifier == null ? schemaSetId : null);

        getNamedParameterJdbcTemplate().update(COPY_SCHEMA_SET_ROW, parameters);
        return getLastInsertId();
    }

    /**
     * @see eionet.meta.dao.ISchemaSetDAO#getSchemaSetVersions(String, java.lang.String, int...)
     */
    @Override
    public List<SchemaSet> getSchemaSetVersions(String userName, String continuityId, int... excludeIds) {

        if (StringUtils.isBlank(continuityId)) {
            throw new IllegalArgumentException("Continuity id must not be blank!");
        }

        String sql = "select * from T_SCHEMA_SET where WORKING_COPY=false and CONTINUITY_ID=:continuityId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("continuityId", continuityId);

        if (StringUtils.isBlank(userName)) {
            sql += " and REG_STATUS=:regStatus";
            params.put("regStatus", SchemaSet.RegStatus.RELEASED.toString());
        }

        if (excludeIds != null && excludeIds.length > 0) {
            sql += " and SCHEMA_SET_ID not in (:excludeIds)";
            params.put("excludeIds", Arrays.asList(ArrayUtils.toObject(excludeIds)));
        }
        sql += " order by SCHEMA_SET_ID desc";

        List<SchemaSet> resultList = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<SchemaSet>() {
            public SchemaSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                SchemaSet ss = new SchemaSet();
                ss.setId(rs.getInt("SCHEMA_SET_ID"));
                ss.setIdentifier(rs.getString("IDENTIFIER"));
                ss.setContinuityId(rs.getString("CONTINUITY_ID"));
                ss.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                ss.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                ss.setWorkingUser(rs.getString("WORKING_USER"));
                ss.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
                ss.setUserModified(rs.getString("USER_MODIFIED"));
                ss.setComment(rs.getString("COMMENT"));
                ss.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return ss;
            }
        });

        return resultList;
    }

}
