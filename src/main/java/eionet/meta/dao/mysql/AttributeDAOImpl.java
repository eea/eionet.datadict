package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DElemAttribute.ParentType;
import eionet.meta.dao.DAOException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.ComplexAttribute;
import eionet.meta.dao.domain.ComplexAttributeField;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.dao.domain.VocabularyConceptAttribute;
import eionet.util.Pair;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@Repository
public class AttributeDAOImpl extends GeneralDAOImpl implements IAttributeDAO {

    /** */
    private static final String COPY_SIMPLE_ATTRIBUTES_SQL =
            "insert into ATTRIBUTE (DATAELEM_ID,PARENT_TYPE,M_ATTRIBUTE_ID,VALUE) "
                    + "select :newParentId, PARENT_TYPE, M_ATTRIBUTE_ID, VALUE from ATTRIBUTE where DATAELEM_ID=:parentId and PARENT_TYPE=:parentType";

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

        // Delete simple attributes

        String sql = "DELETE FROM ATTRIBUTE WHERE DATAELEM_ID IN (:ids) AND PARENT_TYPE = :parentType";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", parentIds);
        parameters.put("parentType", parentType);

        getNamedParameterJdbcTemplate().update(sql, parameters);

        // Delete complex attributes

        sql = "select ROW_ID from COMPLEX_ATTR_ROW where PARENT_ID IN (:ids) and PARENT_TYPE=:parentType";

        List<String> rowIds = getNamedParameterJdbcTemplate().query(sql, parameters, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {

                return rs.getString(1);
            }
        });

        if (rowIds != null && !rowIds.isEmpty()) {
            parameters = new HashMap<String, Object>();
            parameters.put("rowIds", rowIds);
            getNamedParameterJdbcTemplate().update("delete from COMPLEX_ATTR_ROW where ROW_ID in (:rowIds)", parameters);
            getNamedParameterJdbcTemplate().update("delete from COMPLEX_ATTR_FIELD where ROW_ID in (:rowIds)", parameters);
        }
    }

    /** */
    private static final String REPLACE_SIMPLE_ATTR_PARENT_ID_SQL = "update ATTRIBUTE set DATAELEM_ID=:substituteId "
            + "where DATAELEM_ID=:replacedId and PARENT_TYPE=:parentType";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_PARENT_ID_SQL = "update COMPLEX_ATTR_ROW set PARENT_ID=:substituteId "
            + "where PARENT_ID=:replacedId and PARENT_TYPE=:parentType";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_ROW_ID_SQL = "update COMPLEX_ATTR_ROW set ROW_ID=:substituteId "
            + "where ROW_ID=:replacedId";
    /** */
    private static final String REPLACE_COMPLEX_ATTR_FIELD_ROW_ID_SQL = "update COMPLEX_ATTR_FIELD set ROW_ID=:substituteId "
            + "where ROW_ID=:replacedId";

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

        String sql =
                "select M_COMPLEX_ATTR_ID, POSITION, ROW_ID from COMPLEX_ATTR_ROW "
                        + "where PARENT_ID=:parentId and PARENT_TYPE=:parentType order by ROW_ID";

        prms = new HashMap<String, Object>();
        prms.put("parentId", replacedId);
        prms.put("parentType", parentType.toString());

        List<Pair<String, String>> pairs = getNamedParameterJdbcTemplate().query(sql, prms, new RowMapper<Pair<String, String>>() {
            @Override
            public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {

                String oldRowId = rs.getString("ROW_ID");
                String newRowId = substituteId + parentType.toString() + rs.getString("M_COMPLEX_ATTR_ID") + rs.getInt("POSITION");
                return new Pair<String, String>(oldRowId, newRowId);
            }
        });

        prms = new HashMap<String, Object>();
        prms.put("replacedId", replacedId);
        prms.put("substituteId", substituteId);
        prms.put("parentType", parentType.toString());
        getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_PARENT_ID_SQL, prms);

        for (Pair<String, String> pair : pairs) {

            prms = new HashMap<String, Object>();
            prms.put("replacedId", pair.getLeft());
            prms.put("substituteId", pair.getRight());
            getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_ROW_ID_SQL, prms);
            getNamedParameterJdbcTemplate().update(REPLACE_COMPLEX_ATTR_FIELD_ROW_ID_SQL, prms);
        }
    }

    /**
     * @see eionet.meta.dao.IAttributeDAO#getAttributes(eionet.meta.DElemAttribute.ParentType, java.lang.String)
     */
    @Override
    public List<Attribute> getAttributes(DElemAttribute.ParentType parentType, String attributeType) throws DAOException {
        List<Attribute> result = new ArrayList<Attribute>();
        DDSearchEngine searchEngine = new DDSearchEngine(getConnection());

        LinkedHashMap<Integer, DElemAttribute> attributes = searchEngine.getObjectAttributes(0, parentType, attributeType);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplexAttribute getComplexAttributeByName(String complexAttrName) {

        String sql =
                "select * from M_COMPLEX_ATTR as a, M_COMPLEX_ATTR_FIELD as f "
                        + "where a.M_COMPLEX_ATTR_ID = f.M_COMPLEX_ATTR_ID and a.NAME= :attrName";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attrName", complexAttrName);

        ComplexAttribute complexAttribute =
                getNamedParameterJdbcTemplate().query(sql, params, new ResultSetExtractor<ComplexAttribute>() {

                    @Override
                    public ComplexAttribute extractData(ResultSet rs) throws DataAccessException, SQLException {
                        ComplexAttribute complexAttribute = null;
                        while (rs.next()) {
                            if (complexAttribute == null) {
                                complexAttribute = new ComplexAttribute(rs.getInt("a.M_COMPLEX_ATTR_ID"), rs.getString("a.NAME"));
                            }
                            ComplexAttributeField field =
                                    new ComplexAttributeField(rs.getInt("f.M_COMPLEX_ATTR_FIELD_ID"), rs.getString("f.NAME"));
                            complexAttribute.addField(field);
                        }
                        return complexAttribute;
                    }
                });
        return complexAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyComplexAttributes(int parentId, final String parentType, final int newParentId) {

        String sqlQuery =
                "select M_COMPLEX_ATTR_ID, COMPLEX_ATTR_ROW.ROW_ID, POSITION, HARV_ATTR_ID, M_COMPLEX_ATTR_FIELD_ID, VALUE "
                        + "from COMPLEX_ATTR_ROW, COMPLEX_ATTR_FIELD where PARENT_ID=:parentId and PARENT_TYPE=:parentType "
                        + "and COMPLEX_ATTR_ROW.ROW_ID=COMPLEX_ATTR_FIELD.ROW_ID "
                        + "order by COMPLEX_ATTR_ROW.ROW_ID, M_COMPLEX_ATTR_FIELD_ID";

        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("parentId", parentId);
        queryParams.put("parentType", parentType);

        final String sqlInsertRow =
                "insert into COMPLEX_ATTR_ROW " + "(PARENT_ID, PARENT_TYPE, M_COMPLEX_ATTR_ID, POSITION, HARV_ATTR_ID, ROW_ID) "
                        + "values (:parentId, :parentType, :attrId, :position, :harvAttrId, :rowId)";

        final Map<String, Object> insertRowParams = new HashMap<String, Object>();
        insertRowParams.put("parentId", newParentId);
        insertRowParams.put("parentType", parentType);

        final String sqlInsertField =
                "insert into COMPLEX_ATTR_FIELD (ROW_ID, M_COMPLEX_ATTR_FIELD_ID, VALUE) " + "values (:rowId, :fieldId, :value)";

        final Map<String, Object> insertFieldParams = new HashMap<String, Object>();

        getNamedParameterJdbcTemplate().query(sqlQuery, queryParams, new RowCallbackHandler() {

            String previousRowId = "";
            String newRowId = null;

            @Override
            public void processRow(ResultSet rs) throws SQLException {

                int attrId = rs.getInt("M_COMPLEX_ATTR_ID");
                String rowId = rs.getString("COMPLEX_ATTR_ROW.ROW_ID");
                int fieldId = rs.getInt("M_COMPLEX_ATTR_FIELD_ID");
                String value = rs.getString("VALUE");
                int position = rs.getInt("POSITION");
                int harvAttrId = rs.getInt("HARV_ATTR_ID");

                if (!rowId.equals(previousRowId)) {

                    insertRowParams.put("attrId", attrId);
                    insertRowParams.put("position", position);
                    insertRowParams.put("harvAttrId", harvAttrId);

                    String md5Input = newParentId + parentType + attrId + position;
                    newRowId = DigestUtils.md5Hex(md5Input);
                    insertRowParams.put("rowId", newRowId);

                    getNamedParameterJdbcTemplate().update(sqlInsertRow, insertRowParams);
                    previousRowId = rowId;
                }

                insertFieldParams.put("rowId", newRowId);
                insertFieldParams.put("fieldId", fieldId);
                insertFieldParams.put("value", value);

                getNamedParameterJdbcTemplate().update(sqlInsertField, insertFieldParams);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<List<VocabularyConceptAttribute>> getVocabularyConceptAttributes(int vocabularyConceptId, boolean emptyAttributes) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("attributeWeight", DElemAttribute.typeWeights.get("VCO"));
        params.put("vocabularyConceptId", vocabularyConceptId);

        StringBuilder sql = new StringBuilder();
        sql.append("select * from T_VOCABULARY_CONCEPT_ATTRIBUTE v ");
        if (emptyAttributes) {
            sql.append("RIGHT OUTER JOIN M_ATTRIBUTE m ");
        } else {
            sql.append("LEFT JOIN M_ATTRIBUTE m ");
        }
        sql.append("ON (v.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID and v.VOCABULARY_CONCEPT_ID = :vocabularyConceptId) ");
        sql.append("LEFT JOIN T_VOCABULARY_CONCEPT c ON v.RELATED_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID ");
        sql.append("WHERE FLOOR(m.DISP_WHEN / :attributeWeight) %2 != 0 ");
        sql.append("order by m.DISP_ORDER, v.LANGUAGE, v.ATTR_VALUE");

        final List<List<VocabularyConceptAttribute>> result = new ArrayList<List<VocabularyConceptAttribute>>();

        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {

            List<VocabularyConceptAttribute> attributes = null;
            String previousShortName = null;

            @Override
            public void processRow(ResultSet rs) throws SQLException {

                if (attributes == null) {
                    attributes = new ArrayList<VocabularyConceptAttribute>();
                    previousShortName = rs.getString("m.SHORT_NAME");
                }

                VocabularyConceptAttribute atr = new VocabularyConceptAttribute();
                atr.setId(rs.getInt("v.ID"));
                atr.setVocabularyConceptId(rs.getInt("v.VOCABULARY_CONCEPT_ID"));
                atr.setValue(rs.getString("v.ATTR_VALUE"));
                atr.setLanguage(rs.getString("v.LANGUAGE"));
                atr.setLanguageUsed(rs.getBoolean("m.LANGUAGE_USED"));
                atr.setAttributeId(rs.getInt("m.M_ATTRIBUTE_ID"));
                atr.setDataType(rs.getString("m.DATA_TYPE"));
                atr.setInputType(rs.getString("m.DISP_TYPE"));
                atr.setLabel(rs.getString("m.NAME"));
                atr.setHeight(rs.getInt("m.DISP_HEIGHT"));
                atr.setWidth(rs.getInt("m.DISP_WIDTH"));
                atr.setMultiValue(rs.getBoolean("m.DISP_MULTIPLE"));
                atr.setRdfProperty(rs.getString("m.RDF_PROPERTY"));
                atr.setIdentifier(rs.getString("m.SHORT_NAME"));
                atr.setLinkText(rs.getString("v.LINK_TEXT"));
                atr.setRelatedId(rs.getInt("v.RELATED_CONCEPT_ID"));
                atr.setRelatedIdentifier(rs.getString("c.IDENTIFIER"));

                if (!StringUtils.equals(previousShortName, rs.getString("m.SHORT_NAME"))) {
                    result.add(attributes);
                    attributes = new ArrayList<VocabularyConceptAttribute>();
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
        sql.append("order by m.DISP_ORDER, a.VALUE");

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
    public void createVocabularyConceptAttributes(List<VocabularyConceptAttribute> attributes) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into T_VOCABULARY_CONCEPT_ATTRIBUTE (M_ATTRIBUTE_ID, VOCABULARY_CONCEPT_ID, ATTR_VALUE, RELATED_CONCEPT_ID, LANGUAGE, LINK_TEXT) ");
        sql.append("values (:attributeId, :vocabularyConceptId, :value, :relatedId, :language, :linkText)");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[attributes.size()];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("attributeId", attributes.get(i).getAttributeId());
            params.put("vocabularyConceptId", attributes.get(i).getVocabularyConceptId());
            params.put("value", attributes.get(i).getValue());
            params.put("language", attributes.get(i).getLanguage());
            params.put("relatedId", attributes.get(i).getRelatedId());
            params.put("linkText", attributes.get(i).getLinkText());
            batchValues[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConceptAttributes(List<VocabularyConceptAttribute> attributes) {
        StringBuilder sql = new StringBuilder();
        sql.append("update T_VOCABULARY_CONCEPT_ATTRIBUTE set ATTR_VALUE=:value, RELATED_CONCEPT_ID=:relatedId, LANGUAGE=:language, LINK_TEXT=:linkText ");
        sql.append("where ID = :id");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[attributes.size()];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", attributes.get(i).getId());
            params.put("value", attributes.get(i).getValue());
            params.put("language", attributes.get(i).getLanguage());
            params.put("relatedId", attributes.get(i).getRelatedId());
            params.put("linkText", attributes.get(i).getLinkText());
            batchValues[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyConceptAttributes(List<Integer> excludedIds, int vocabularyConceptId) {
        String sql = null;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyConceptId", vocabularyConceptId);

        if (excludedIds.size() == 0) {
            sql = "delete from T_VOCABULARY_CONCEPT_ATTRIBUTE where VOCABULARY_CONCEPT_ID = :vocabularyConceptId";
        } else {
            sql =
                    "delete from T_VOCABULARY_CONCEPT_ATTRIBUTE where VOCABULARY_CONCEPT_ID = :vocabularyConceptId and ID not in (:excludedIds)";
            parameters.put("excludedIds", excludedIds);
        }

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyConceptAttribute> getDeletedConceptAttributes(List<Integer> excludedIds, int vocabularyConceptId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyConceptId", vocabularyConceptId);

        StringBuilder sql = new StringBuilder();
        if (excludedIds.size() == 0) {
            sql.append("select * from T_VOCABULARY_CONCEPT_ATTRIBUTE a LEFT JOIN M_ATTRIBUTE m ON a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID ");
            sql.append("where a.VOCABULARY_CONCEPT_ID = :vocabularyConceptId");
        } else {
            sql.append("select * from T_VOCABULARY_CONCEPT_ATTRIBUTE a LEFT JOIN M_ATTRIBUTE m ON a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID ");
            sql.append("where a.VOCABULARY_CONCEPT_ID = :vocabularyConceptId and a.ID not in (:excludedIds)");
            parameters.put("excludedIds", excludedIds);
        }

        List<VocabularyConceptAttribute> result =
                getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<VocabularyConceptAttribute>() {
                    @Override
                    public VocabularyConceptAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConceptAttribute atr = new VocabularyConceptAttribute();
                        atr.setId(rs.getInt("a.ID"));
                        atr.setAttributeId(rs.getInt("a.M_ATTRIBUTE_ID"));
                        atr.setIdentifier(rs.getString("m.SHORT_NAME"));
                        atr.setVocabularyConceptId(rs.getInt("a.VOCABULARY_CONCEPT_ID"));
                        atr.setValue(rs.getString("a.ATTR_VALUE"));
                        atr.setLanguage(rs.getString("a.LANGUAGE"));
                        atr.setLinkText(rs.getString("a.LINK_TEXT"));
                        atr.setRelatedId(rs.getInt("a.RELATED_CONCEPT_ID"));

                        return atr;
                    }
                });

        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAndDeleteConceptAttribute(int conceptId, int relatedConceptId, String identifier) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("conceptId", conceptId);
        parameters.put("relatedConceptId", relatedConceptId);
        parameters.put("identifier", identifier);

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE a FROM T_VOCABULARY_CONCEPT_ATTRIBUTE a LEFT JOIN M_ATTRIBUTE m ON a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID WHERE ");
        sql.append("a.VOCABULARY_CONCEPT_ID = :conceptId AND a.RELATED_CONCEPT_ID = :relatedConceptId AND m.SHORT_NAME = :identifier ");

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAndAddConceptAttribute(int conceptId, int relatedConceptId, String identifier) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("conceptId", conceptId);
        parameters.put("relatedConceptId", relatedConceptId);
        parameters.put("identifier", identifier);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO T_VOCABULARY_CONCEPT_ATTRIBUTE (M_ATTRIBUTE_ID, VOCABULARY_CONCEPT_ID, RELATED_CONCEPT_ID) ");
        sql.append("SELECT DISTINCT m.M_ATTRIBUTE_ID, :conceptId, :relatedConceptId FROM T_VOCABULARY_CONCEPT_ATTRIBUTE a ");
        sql.append("RIGHT JOIN M_ATTRIBUTE m ON a.M_ATTRIBUTE_ID = m.M_ATTRIBUTE_ID ");
        sql.append("WHERE m.SHORT_NAME = :identifier AND NOT EXISTS ");
        sql.append(" (SELECT a2.ID FROM T_VOCABULARY_CONCEPT_ATTRIBUTE a2 LEFT JOIN M_ATTRIBUTE m2 ON a2.M_ATTRIBUTE_ID = m2.M_ATTRIBUTE_ID WHERE ");
        sql.append("  a2.VOCABULARY_CONCEPT_ID = :conceptId AND a2.RELATED_CONCEPT_ID = :relatedConceptId AND m2.SHORT_NAME = :identifier)");

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
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

}
