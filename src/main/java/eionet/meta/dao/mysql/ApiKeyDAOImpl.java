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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.meta.dao.mysql;

import eionet.meta.dao.IApiKeyDAO;
import eionet.meta.dao.domain.DDApiKey;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO implementation for API-Key.
 *
 * @author enver
 */
@Repository
public class ApiKeyDAOImpl extends GeneralDAOImpl implements IApiKeyDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public DDApiKey getApiKey(String key) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key", key);

        StringBuilder sql = new StringBuilder();
        sql.append("select dak.IDENTIFIER, dak.SCOPE, dak.KEY, dak.REMOTE_ADDR, dak.EXPIRES, dak.NOTES ");
        sql.append("from API_KEY as dak ");
        sql.append("where dak.KEY=:key");

        DDApiKey result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new ApiKeyRowMapper());

        return result;
    }//end of method getApiKey

    /**
     * Inner class to be re-used, when row mapping.
     */
    private static class ApiKeyRowMapper implements RowMapper<DDApiKey> {

        @Override
        public DDApiKey mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            DDApiKey apiKey = new DDApiKey();
            apiKey.setIdentifier(resultSet.getString("IDENTIFIER"));
            apiKey.setScope(resultSet.getString("SCOPE"));
            apiKey.setKey(resultSet.getString("KEY"));
            apiKey.setRemoteAddr(resultSet.getString("REMOTE_ADDR"));
            apiKey.setExpires(resultSet.getDate("EXPIRES"));
            apiKey.setNotes(resultSet.getString("NOTES"));
            return apiKey;
        }//end of method mapRow
    }//end of inner class ApiKeyRowMapper
}//end of class ApiKeyDAOImpl
