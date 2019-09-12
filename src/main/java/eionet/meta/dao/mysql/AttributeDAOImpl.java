package eionet.meta.dao.mysql;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.mysql.valueconverters.BooleanToYesNoConverter;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@Repository
public class AttributeDAOImpl extends GeneralDAOImpl implements IAttributeDAO {

    /**
     * update an attribute record.
     */
    private static final String UPDATE_ATTRIBUTE_SQL = "update ATTRIBUTE SET VALUE = :value WHERE M_ATTRIBUTE_ID = (SELECT M_ATTRIBUTE_ID FROM M_ATTRIBUTE "
            + "WHERE NAME = :attrName) AND DATAELEM_ID = :dataElemId and PARENT_TYPE = :parentType";
    /** */
    private static final String COPY_SIMPLE_ATTRIBUTES_SQL =
            "insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) "
                    + "select :newParentId, PARENT_TYPE, M_ATTRIBUTE_ID, VALUE from ATTRIBUTE where DATAELEM_ID=:parentId and PARENT_TYPE=:parentType ";

    /**
     * @see eionet.meta.dao.IAttributeDAO#copySimpleAttributes(int, java.lang.String, int)
     */
    @Override
    public void copySimpleAttributes(int parentId, String parentType, int newParentId) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        params.put("newParentId", newParentId);
        params.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(COPY_SIMPLE_ATTRIBUTES_SQL, params);
    }

    @Override
    public void deleteAttributes(List<Integer> parentIds, String parentType) {
        String sql = "DELETE FROM ATTRIBUTE WHERE DATAELEM_ID IN (:ids) AND PARENT_TYPE = :parentType";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", parentIds);
        parameters.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    private static final String REPLACE_SIMPLE_ATTR_PARENT_ID_SQL = "update ATTRIBUTE set DATAELEM_ID=:substituteId "
            + "where DATAELEM_ID=:replacedId and PARENT_TYPE=:parentType";

    /**
     * @see eionet.meta.dao.IAttributeDAO#replaceParentId(int, int, eionet.meta.DElemAttribute.ParentType)
     */
    @Override
    public void replaceParentId(int replacedId, final int substituteId, final ParentType parentType) {

        Map<String, Object> prms = new HashMap<String, Object>();
        prms.put("replacedId", replacedId);
        prms.put("substituteId", substituteId);
        prms.put("parentType", parentType.toString());

        getNamedParameterJdbcTemplate().update(REPLACE_SIMPLE_ATTR_PARENT_ID_SQL, prms);
    }

    /**
     * @see eionet.meta.dao.IAttributeDAO#getAttributes(eionet.meta.DElemAttribute.ParentType)
     */
    @Override
    public List<Attribute> getAttributes(DElemAttribute.ParentType parentType) throws DAOException {
        List<Attribute> result = new ArrayList<Attribute>();
        DDSearchEngine searchEngine = new DDSearchEngine(getConnection());

        LinkedHashMap<Integer, DElemAttribute> attributes = searchEngine.getObjectAttributes(0, parentType);

        for (DElemAttribute dea : attributes.values()) {
            Attribute a = new Attribute();
            a.setId(Integer.parseInt(dea.getID()));
            a.setName(dea.getName());
            a.setShortName(dea.getShortName());
            result.add(a);
        }

        return result;
    }

    /**
     * @see eionet.meta.dao.IAttributeDAO#getAttributeValues(int, java.lang.String)
     */
    @Override
    public Map<String, List<String>> getAttributeValues(int parentId, String parentType) {

        String sql =
                "select SHORT_NAME, VALUE from ATTRIBUTE, M_ATTRIBUTE"
                        + " where DATAELEM_ID=:parentId and PARENT_TYPE=:parentType and ATTRIBUTE.M_ATTRIBUTE_ID=M_ATTRIBUTE.M_ATTRIBUTE_ID"
                        + " order by SHORT_NAME, VALUE";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        params.put("parentType", parentType);

        final HashMap<String, List<String>> resultMap = new HashMap<String, List<String>>();
        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String shortName = rs.getString("SHORT_NAME");
                String value = rs.getString("VALUE");
                List<String> values = resultMap.get(shortName);
                if (values == null) {
                    values = new ArrayList<String>();
                    resultMap.put(shortName, values);
                }
                values.add(value);
            }
        });

        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttributeByName(String shortName) {
        String sql = "select * from M_ATTRIBUTE where SHORT_NAME=:shortName";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shortName", shortName);

        Attribute result = getNamedParameterJdbcTemplate().queryForObject(sql, params, new RowMapper<Attribute>() {

            @Override
            public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                Attribute attribute = new Attribute();
                attribute.setId(rs.getInt("M_ATTRIBUTE_ID"));
                attribute.setName(rs.getString("NAME"));
                attribute.setShortName(rs.getString("SHORT_NAME"));
                return attribute;
            }

        });
        return result;
    }

    @Override
    public List<List<SimpleAttribute>> getVocabularyFolderAttributes(int vocabularyFolderId, boolean emptyAttributes) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeWeight", DElemAttribute.typeWeights.get("VCF"));
        params.put("vocabularyFolderId", vocabularyFolderId);
        params.put("parentType", DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ATTRIBUTE a ");
        if (emptyAttributes) {
            sql.append("RIGHT OUTER JOIN M_ATTRIBUTE m ");
        } else {
            sql.append("LEFT JOIN M_ATTRIBUTE m ");
        }
        sql.append("ON (a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID and a.DATAELEM_ID = :vocabularyFolderId and a.PARENT_TYPE = :parentType) ");
        sql.append("WHERE FLOOR(m.DISP_WHEN / :attributeWeight) %2 != 0 ");
        sql.append("order by m.DISP_ORDER");

        final List<List<SimpleAttribute>> result = new ArrayList<List<SimpleAttribute>>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {

            List<SimpleAttribute> attributes = null;
            String previousShortName = null;

            @Override
            public void processRow(ResultSet rs) throws SQLException {

                if (attributes == null) {
                    attributes = new ArrayList<SimpleAttribute>();
                    previousShortName = rs.getString("m.SHORT_NAME");
                }

                SimpleAttribute atr = new SimpleAttribute();
                atr.setObjectId(rs.getInt("a.DATAELEM_ID"));
                atr.setValue(rs.getString("a.VALUE"));
                atr.setAttributeId(rs.getInt("m.M_ATTRIBUTE_ID"));
                atr.setDataType(rs.getString("m.DATA_TYPE"));
                atr.setInputType(rs.getString("m.DISP_TYPE"));
                atr.setLabel(rs.getString("m.NAME"));
                atr.setHeight(rs.getInt("m.DISP_HEIGHT"));
                atr.setWidth(rs.getInt("m.DISP_WIDTH"));
                atr.setMultiValue(rs.getBoolean("m.DISP_MULTIPLE"));
                atr.setIdentifier(rs.getString("m.SHORT_NAME"));
                atr.setMandatory("M".equals(rs.getString("m.OBLIGATION")));

                if (!StringUtils.equals(previousShortName, rs.getString("m.SHORT_NAME"))) {
                    result.add(attributes);
                    attributes = new ArrayList<SimpleAttribute>();
                }

                attributes.add(atr);
                previousShortName = rs.getString("m.SHORT_NAME");

                if (rs.isLast()) {
                    result.add(attributes);
                }
            }
        });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleAttribute> getAttributesMetadata(int typeWeight) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeWeight", typeWeight);

        StringBuilder sql = new StringBuilder();
        sql.append("select * from M_ATTRIBUTE m ");
        sql.append("WHERE FLOOR(m.DISP_WHEN / :attributeWeight) %2 != 0 ");
        sql.append("order by m.DISP_ORDER");

        List<SimpleAttribute> result =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<SimpleAttribute>() {

                    @Override
                    public SimpleAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SimpleAttribute atr = new SimpleAttribute();
                        atr.setAttributeId(rs.getInt("m.M_ATTRIBUTE_ID"));
                        atr.setDataType(rs.getString("m.DATA_TYPE"));
                        atr.setInputType(rs.getString("m.DISP_TYPE"));
                        atr.setLabel(rs.getString("m.NAME"));
                        atr.setHeight(rs.getInt("m.DISP_HEIGHT"));
                        atr.setWidth(rs.getInt("m.DISP_WIDTH"));
                        atr.setMultiValue(rs.getBoolean("m.DISP_MULTIPLE"));
                        atr.setIdentifier(rs.getString("m.SHORT_NAME"));
                        atr.setMandatory("M".equals(rs.getString("m.OBLIGATION")));

                        return atr;
                    }
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSimpleAttributes(int objectId, String parentType, List<List<SimpleAttribute>> attributes) {
        if (attributes == null) {
            return;
        }

        // Delete first
        String deleteSql = "delete from ATTRIBUTE where DATAELEM_ID = :elementId and PARENT_TYPE = :parentType";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("elementId", objectId);
        parameters.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(deleteSql, parameters);

        // Insert new ones
        String insertSql =
                "insert into ATTRIBUTE (M_ATTRIBUTE_ID, DATAELEM_ID, PARENT_TYPE, VALUE) values (:attributeId,:elementId,:parentType,:value)";

        List<SimpleAttribute> batchAttrs = new ArrayList<SimpleAttribute>();
        for (List<SimpleAttribute> attrs : attributes) {
            if (attrs != null) {
                for (SimpleAttribute attr : attrs) {
                    if (attr != null && StringUtils.isNotEmpty(attr.getValue())) {
                        batchAttrs.add(attr);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchParams = new HashMap[batchAttrs.size()];

        for (int i = 0; i < batchAttrs.size(); i++) {
            SimpleAttribute sa = batchAttrs.get(i);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("attributeId", sa.getAttributeId());
            params.put("elementId", objectId);
            params.put("parentType", parentType);
            params.put("value", sa.getValue());
            batchParams[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(insertSql, batchParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RdfNamespace> getRdfNamespaces() {
        Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("select * from T_RDF_NAMESPACE order by URI");

        List<RdfNamespace> items = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<RdfNamespace>() {
            @Override
            public RdfNamespace mapRow(ResultSet rs, int rowNum) throws SQLException {
                RdfNamespace rn = new RdfNamespace();
                rn.setId(rs.getInt("ID"));
                rn.setUri(rs.getString("URI"));
                rn.setPrefix(rs.getString("NAME_PREFIX"));
                return rn;
            }
        });

        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleAttribute> getSimpleAttributeValues(int parentId, String parentType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentId", parentId);
        params.put("parentType", parentType);

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ATTRIBUTE a ");
        sql.append("LEFT JOIN M_ATTRIBUTE m ");
        sql.append("ON (a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID and a.DATAELEM_ID = :parentId and a.PARENT_TYPE = :parentType) ");
        sql.append("LEFT JOIN T_RDF_NAMESPACE r ON m.RDF_PROPERTY_NAMESPACE_ID = r.ID ");
        sql.append("where a.DATAELEM_ID = :parentId and a.PARENT_TYPE = :parentType ");
        sql.append("order by m.DISP_ORDER, a.VALUE");

        List<SimpleAttribute> items =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<SimpleAttribute>() {

                    @Override
                    public SimpleAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SimpleAttribute atr = new SimpleAttribute();
                        atr.setObjectId(rs.getInt("a.DATAELEM_ID"));
                        atr.setValue(rs.getString("a.VALUE"));
                        atr.setAttributeId(rs.getInt("m.M_ATTRIBUTE_ID"));
                        atr.setDataType(rs.getString("m.DATA_TYPE"));
                        atr.setInputType(rs.getString("m.DISP_TYPE"));
                        atr.setLabel(rs.getString("m.NAME"));
                        atr.setHeight(rs.getInt("m.DISP_HEIGHT"));
                        atr.setWidth(rs.getInt("m.DISP_WIDTH"));
                        atr.setMultiValue(rs.getBoolean("m.DISP_MULTIPLE"));
                        atr.setIdentifier(rs.getString("m.SHORT_NAME"));
                        atr.setMandatory("M".equals(rs.getString("m.OBLIGATION")));
                        atr.setRdfPropertyName(rs.getString("m.RDF_PROPERTY_NAME"));
                        atr.setRdfPropertyPrefix(rs.getString("r.NAME_PREFIX"));
                        atr.setRdfPropertyUri(rs.getString("r.URI"));

                        return atr;
                    }
                });

        return items;
    }
    
    @Override
    public SimpleAttribute getById(int id){
        String sql = "select * from M_ATTRIBUTE where M_ATTRIBUTE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        List<SimpleAttribute> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<SimpleAttribute>() {
            
            @Override
            public SimpleAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                SimpleAttribute attr = new SimpleAttribute();
                attr.setAttributeId(rs.getInt("M_ATTRIBUTE_ID"));
                attr.setIdentifier(rs.getString("SHORT_NAME"));
                attr.setLabel(rs.getString("NAME"));
                attr.setInputType(rs.getString("DISP_TYPE"));
                
                return attr;
            }
        });
        
        return result.isEmpty() ? null : result.get(0);
    }
    
    @Override
    public List<FixedValue> getFixedValues(int attributeId) {
        StringBuilder sql = new StringBuilder("select * from FXV");
        sql.append(" where OWNER_ID = :ownerId ");
        sql.append(" and OWNER_TYPE=:ownerType ");
        sql.append(" order by FXV_ID");
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ownerId", attributeId);
        params.put("ownerType", "attr");
        
        List<FixedValue> result = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<FixedValue>() {
            @Override
            public FixedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                FixedValue fv = new FixedValue();
                fv.setId(rs.getInt("FXV_ID"));
                fv.setOwnerId(rs.getInt("OWNER_ID"));
                fv.setOwnerType(rs.getString("OWNER_TYPE"));
                fv.setValue(rs.getString("VALUE"));
                fv.setDefaultValue(new BooleanToYesNoConverter().convertBack(rs.getString("IS_DEFAULT")));
                fv.setDefinition(rs.getString("DEFINITION"));
                fv.setShortDescription(rs.getString("SHORT_DESC"));
                return fv;
            }
        });
        
        return result;
    }

    @Override
    public void updateSimpleAttributeValue(String attrName, int dataElemId , String parentType, String value) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attrName", attrName);
        params.put("dataElemId", dataElemId);
        params.put("parentType", parentType);
        params.put("value", value);

        getNamedParameterJdbcTemplate().update(UPDATE_ATTRIBUTE_SQL, params);
    }

}
