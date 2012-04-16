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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.ISchemaDAO;
import eionet.meta.dao.domain.Schema;
import eionet.meta.dao.domain.SchemaSet.RegStatus;
import eionet.util.Util;

/**
 * SchemaSet DAO implementation.
 * 
 * @author Jaanus Heinlaid
 */
@Repository
public class SchemaDAOImpl extends GeneralDAOImpl implements ISchemaDAO {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SchemaDAOImpl.class);

    /** */
    private static final String REPLACE_ID_SQL = "update T_SCHEMA set SCHEMA_ID=:substituteId where SCHEMA_ID=:replacedId";

    /** */
    private static final String INSERT_SQL =
        "insert into T_SCHEMA (FILENAME, SCHEMA_SET_ID, CONTINUITY_ID, REG_STATUS, "
        + "WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, COMMENT, CHECKEDOUT_COPY_ID) "
        + "values (:filename,:schemaSetId,:continuityId,:regStatus,:workingCopy,:workingUser,now(),:userModified,:comment,:checkedOutCopyId)";

    /** */
    private static final String LIST_FOR_SCHEMA_SET = "select * from T_SCHEMA where SCHEMA_SET_ID=:schemaSetId";

    /** */
    private static final String COPY_TO_SCHEMA_SET_SQL =
        "insert into T_SCHEMA (FILENAME, SCHEMA_SET_ID, DATE_MODIFIED, USER_MODIFIED) "
        + "select ifnull(:newFileName,FILENAME), ifnull(:schemaSetId,SCHEMA_SET_ID), now(), :userName from T_SCHEMA where SCHEMA_ID=:schemaId";

    /**
     * @see eionet.meta.dao.ISchemaDAO#createSchema(eionet.meta.dao.domain.Schema)
     */
    @Override
    public int createSchema(Schema schema) {

        String continuityId = schema.getContinuityId();
        // If continuity id not set, but the schema is a root-level schema, we need to generate and set it.
        if (StringUtils.isBlank(continuityId) && schema.getSchemaSetId() <= 0) {
            continuityId = Util.generateContinuityId(schema);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("filename", schema.getFileName());
        params.put("schemaSetId", schema.getSchemaSetId() <= 0 ? null : schema.getSchemaSetId());
        params.put("continuityId", continuityId);
        params.put("regStatus", schema.getRegStatus().toString());
        params.put("workingCopy", schema.isWorkingCopy());
        params.put("workingUser", schema.getWorkingUser());
        params.put("userModified", schema.getUserModified());
        params.put("comment", schema.getComment());
        params.put("checkedOutCopyId", schema.getCheckedOutCopyId() <= 0 ? null : schema.getCheckedOutCopyId());

        getNamedParameterJdbcTemplate().update(INSERT_SQL, params);

        return getLastInsertId();
    }

    /**
     * @see eionet.meta.dao.ISchemaDAO#listForSchemaSet(int)
     */
    @Override
    public List<Schema> listForSchemaSet(int schemaSetId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("schemaSetId", schemaSetId);

        List<Schema> resultList = getNamedParameterJdbcTemplate().query(LIST_FOR_SCHEMA_SET, params, new RowMapper<Schema>() {
            public Schema mapRow(ResultSet rs, int rowNum) throws SQLException {
                Schema schema = new Schema();
                schema.setId(rs.getInt("SCHEMA_ID"));
                schema.setFileName(rs.getString("FILENAME"));
                schema.setContinuityId(rs.getString("CONTINUITY_ID"));
                schema.setRegStatus(RegStatus.fromString(rs.getString("REG_STATUS")));
                schema.setWorkingCopy(rs.getBoolean("WORKING_COPY"));
                schema.setWorkingUser(rs.getString("WORKING_USER"));
                schema.setDateModified(rs.getDate("DATE_MODIFIED"));
                schema.setUserModified(rs.getString("USER_MODIFIED"));
                schema.setComment(rs.getString("COMMENT"));
                schema.setCheckedOutCopyId(rs.getInt("CHECKEDOUT_COPY_ID"));
                return schema;
            }
        });

        return resultList;
    }

    /**
     * @see eionet.meta.dao.ISchemaDAO#copyToSchemaSet(int, int, String, String)
     */
    @Override
    public int copyToSchemaSet(int schemaId, int schemaSetId, String fileName, String userName) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newFileName", fileName);
        params.put("schemaSetId", schemaSetId <= 0 ? null : schemaSetId);
        params.put("userName", userName);
        params.put("schemaId", schemaId);

        getNamedParameterJdbcTemplate().update(COPY_TO_SCHEMA_SET_SQL, params);
        return getLastInsertId();
    }

    /**
     * @see eionet.meta.dao.ISchemaDAO#replaceId(int, int)
     */
    @Override
    public void replaceId(int replacedId, int substituteId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("replacedId", replacedId);
        params.put("substituteId", substituteId);

        getNamedParameterJdbcTemplate().update(REPLACE_ID_SQL, params);
    }
}
