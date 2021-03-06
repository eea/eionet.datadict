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
 *        Raptis Dimos
 */
package eionet.meta.dao.mysql;

import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.mysql.valueconverters.BooleanToYesNoConverter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *  Fixed Value DAO Implementation
 */
@Repository
public class FixedValueDAOImpl extends GeneralDAOImpl implements IFixedValueDAO {

    @Override
    public void create(FixedValue fixedValue){
        StringBuilder sql = new StringBuilder();
        sql.append("insert into FXV (OWNER_ID, OWNER_TYPE, VALUE, IS_DEFAULT, DEFINITION, SHORT_DESC) values (:ownerId, :ownerType, :value, :isDefault, :definition, :shortDesc)");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", fixedValue.getOwnerId());
        params.put("ownerType", fixedValue.getOwnerType());
        params.put("value", fixedValue.getValue());
        params.put("isDefault", new BooleanToYesNoConverter().convert(fixedValue.isDefaultValue()));
        params.put("definition", (fixedValue.getDefinition() == null) ? "" : fixedValue.getDefinition());
        params.put("shortDesc", (fixedValue.getShortDescription() == null) ? "" : fixedValue.getShortDescription() );

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public void deleteById(int id){
        StringBuilder sql = new StringBuilder();
        sql.append("delete from FXV where FXV_ID = :id");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public void deleteAll(FixedValue.OwnerType ownerType, int ownerId) {
        String sql = "delete from FXV where OWNER_TYPE = :ownerType and OWNER_ID = :ownerId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerType", ownerType.toString());
        params.put("ownerId", ownerId);
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int delete(FixedValue.OwnerType ownerType, List<Integer> ownerIds) {
        String sql = "delete from FXV where OWNER_TYPE = :ownerType and OWNER_ID in (:ownerIds)";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerType", ownerType.toString());
        params.put("ownerIds", ownerIds);

        return this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void update(FixedValue fixedValue){
        StringBuilder sql = new StringBuilder();
        sql.append("update FXV ");
        sql.append("SET OWNER_ID = :ownerId, OWNER_TYPE = :ownerType, VALUE = :value, IS_DEFAULT = :isDefault, DEFINITION = :definition, SHORT_DESC = :shortDesc ");
        sql.append("WHERE FXV_ID = :id");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", fixedValue.getOwnerId());
        params.put("ownerType", fixedValue.getOwnerType());
        params.put("value", fixedValue.getValue());
        params.put("isDefault", new BooleanToYesNoConverter().convert(fixedValue.isDefaultValue()));
        params.put("definition", (fixedValue.getDefinition() == null) ? "" : fixedValue.getDefinition());
        params.put("shortDesc", (fixedValue.getShortDescription() == null) ? "" : fixedValue.getShortDescription());
        params.put("id", fixedValue.getId());

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
    }

    @Override
    public FixedValue getById(int id){
        String sql = "select * from FXV where FXV_ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        List<FixedValue> fixedValues = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FixedValue>() {

            @Override
            public FixedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                return createFromSimpleSelectStatement(rs);
            }
        });

        return fixedValues.isEmpty() ? null : fixedValues.get(0);
    }

    @Override
    public FixedValue getByValue(FixedValue.OwnerType ownerType, int ownerId, String value) {
        String sql = "select * from FXV where OWNER_TYPE = :ownerType and OWNER_ID = :ownerId and VALUE = :value";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerType", ownerType.toString());
        params.put("ownerId", ownerId);
        params.put("value", value);

        List<FixedValue> fixedValues = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FixedValue>() {

            @Override
            public FixedValue mapRow(ResultSet rs, int i) throws SQLException {
                return createFromSimpleSelectStatement(rs);
            }

        });

        return fixedValues.isEmpty() ? null : fixedValues.get(0);
    }

    @Override
    public boolean exists(int id){
        String sql = "select count(*) from FXV where FXV_ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        int count = getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);

        return (count > 0);
    }

    @Override
    public boolean exists(FixedValue.OwnerType ownerType, int ownerId, String value){
        String sql = "select count(*) from FXV where OWNER_ID = :ownerId AND OWNER_TYPE = :ownerType AND value = :value";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", ownerId);
        params.put("ownerType", ownerType.toString());
        params.put("value", value);

        int count = getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);

        return (count > 0);
    }

    @Override
    public void updateDefaultValue(FixedValue.OwnerType ownerType, int ownerId, String value) {
        String sql = "update FXV\n"
                + "set IS_DEFAULT = case when VALUE = :value then 'Y' else 'N' end\n"
                + "where OWNER_TYPE = :ownerType and OWNER_ID = :ownerId";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", ownerId);
        params.put("ownerType", ownerType.toString());
        params.put("value", value);

        super.getNamedParameterJdbcTemplate().update(sql, params);
    }

    private FixedValue createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        FixedValue fixedValue = new FixedValue();
        fixedValue.setId(rs.getInt("FXV_ID"));
        fixedValue.setOwnerId(rs.getInt("OWNER_ID"));
        fixedValue.setOwnerType(rs.getString("OWNER_TYPE"));
        fixedValue.setValue(rs.getString("VALUE"));
        fixedValue.setDefaultValue(new BooleanToYesNoConverter().convertBack(rs.getString("IS_DEFAULT")));
        fixedValue.setDefinition(rs.getString("DEFINITION"));
        fixedValue.setShortDescription(rs.getString("SHORT_DESC"));

        return fixedValue;
    }

    @Override
    public List<FixedValue> getValueByOwner(FixedValue.OwnerType ownerType, int ownerId) {
        String sql = "select FXV_ID, VALUE "
                + "from FXV "
                + "where OWNER_TYPE = :ownerType and OWNER_ID = :ownerId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", ownerId);
        params.put("ownerType", ownerType.toString());

        List<FixedValue> fixedValues = super.getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<FixedValue>() {
            @Override
            public FixedValue mapRow(ResultSet rs, int i) throws SQLException {
                FixedValue fixedValue = new FixedValue();
                fixedValue.setId(rs.getInt("FXV_ID"));
                fixedValue.setValue(rs.getString("VALUE"));
                return fixedValue;
            }
        });

        return fixedValues;
    }

}
