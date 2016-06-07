package eionet.datadict.dal.impl;

import eionet.datadict.model.Attribute;
import eionet.datadict.model.enums.Enumerations.AttributeDataType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.impl.converters.BooleanToMySqlEnumConverter;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.util.data.DataConverter;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 *
 * @author Aliki Kopaneli
 */
@Repository
public class AttributeDaoImpl extends JdbcRepositoryBase implements AttributeDao {

    @Autowired
    public AttributeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Attribute getById(int id) {
        String sql = "select "
                + "M_ATTRIBUTE.*, "
                + "NAMESPACE.*, "
                + "M_ATTRIBUTE_VOCABULARY.VOCABULARY_ID, T_RDF_NAMESPACE.ID AS RDF_ID, T_RDF_NAMESPACE.URI AS RDF_URI, T_RDF_NAMESPACE.NAME_PREFIX as RDF_PREFIX "
                + "FROM "
                + "M_ATTRIBUTE "
                + "LEFT JOIN T_RDF_NAMESPACE ON T_RDF_NAMESPACE.ID = M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID "
                + "LEFT JOIN NAMESPACE ON NAMESPACE.NAMESPACE_ID = M_ATTRIBUTE.NAMESPACE_ID "
                + "LEFT JOIN M_ATTRIBUTE_VOCABULARY ON M_ATTRIBUTE_VOCABULARY.M_ATTRIBUTE_ID =  M_ATTRIBUTE.M_ATTRIBUTE_ID "
                + "WHERE M_ATTRIBUTE.M_ATTRIBUTE_ID = :id";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        try {
            return this.getNamedParameterJdbcTemplate().queryForObject(sql, params, new AttributeRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public boolean exists(int id) {
        String sql = "select count(M_ATTRIBUTE_ID) from M_ATTRIBUTE where M_ATTRIBUTE_ID=:id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);

        int count = getNamedParameterJdbcTemplate().queryForInt(sql, params);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int create(Attribute attribute) {
        String sql = "insert into M_ATTRIBUTE "
                + "(NAME , SHORT_NAME, OBLIGATION, DEFINITION, DISP_TYPE, DISP_ORDER, "
                + "DISP_WIDTH, DISP_HEIGHT, DISP_MULTIPLE, INHERIT, NAMESPACE_ID, "
                + "DISP_WHEN, RDF_PROPERTY_NAMESPACE_ID, RDF_PROPERTY_NAME) "
                + "values (:name, :shortName, :obligation, :definition, :dispType, :dispOrder, "
                + ":dispWidth, :dispHeight, :dispMultiple, :inherit, :namespaceId, "
                + ":dispWhen, :rdfPropertyNamespaceId, :rdfPropertyName)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shortName", attribute.getShortName());
        params.put("name", attribute.getName());
        params.put("obligation", new ObligationTypeConverter().convert(attribute.getObligationType()));
        params.put("definition", attribute.getDefinition() ==  null ? "" : attribute.getDefinition());
        params.put("dispOrder", attribute.getDisplayOrder() == null ? DISPLAY_ORDER_DEFAULT : attribute.getDisplayOrder());
        params.put("dispWidth", attribute.getDisplayWidth() == null ? DISPLAY_WIDTH_DEFAULT : attribute.getDisplayWidth());
        params.put("dispHeight", attribute.getDisplayHeight() == null ? DISPLAY_HEIGHT_DEFAULT : attribute.getDisplayHeight());
        params.put("dispWhen", new TargetEntityConverter().convert(attribute.getTargetEntities()));
        params.put("dispMultiple", new BooleanToMySqlEnumConverter().convert(attribute.isDisplayMultiple()));
        params.put("inherit", new ValueInheritanceConverter().convert(attribute.getValueInheritanceMode()));
        params.put("rdfPropertyName", attribute.getRdfPropertyName());
        params.put("namespaceId", attribute.getNamespace() == null ? NAMESPACE_ID_DEFAULT : attribute.getNamespace().getId());
        params.put("dispType", new DisplayTypeConverter().convert(attribute.getDisplayType()));
        params.put("rdfPropertyNamespaceId", attribute.getRdfNamespace() == null ? null : attribute.getRdfNamespace().getId());
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(sql, parameterMap, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public void update(Attribute attribute) {
        String sql = "update M_ATTRIBUTE set "
                + "NAME = :name, OBLIGATION = :obligation, DEFINITION = :definition, DISP_TYPE = :dispType, DISP_ORDER = :dispOrder, "
                + "DISP_WIDTH = :dispWidth, DISP_HEIGHT = :dispHeight, DISP_MULTIPLE = :dispMultiple, INHERIT = :inherit, NAMESPACE_ID = :namespaceId, "
                + "DISP_WHEN = :dispWhen, RDF_PROPERTY_NAMESPACE_ID = :rdfPropertyNamespaceId, RDF_PROPERTY_NAME = :rdfPropertyName "
                + "where M_ATTRIBUTE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", attribute.getId());
        params.put("name", attribute.getName());
        params.put("obligation", new ObligationTypeConverter().convert(attribute.getObligationType()));
        params.put("definition", attribute.getDefinition());
        params.put("dispOrder", attribute.getDisplayOrder() == null ? DISPLAY_ORDER_DEFAULT : attribute.getDisplayOrder());
        params.put("dispWidth", attribute.getDisplayWidth());
        params.put("dispHeight", attribute.getDisplayHeight());
        params.put("dispWhen", new TargetEntityConverter().convert(attribute.getTargetEntities()));
        params.put("dispType", new DisplayTypeConverter().convert(attribute.getDisplayType()));
        params.put("dispMultiple", new BooleanToMySqlEnumConverter().convert(attribute.isDisplayMultiple()));
        params.put("inherit", new ValueInheritanceConverter().convert(attribute.getValueInheritanceMode()));
        params.put("rdfPropertyName", attribute.getRdfPropertyName());
        params.put("namespaceId", attribute.getNamespace() == null ? NAMESPACE_ID_DEFAULT : attribute.getNamespace().getId());
        params.put("rdfPropertyNamespaceId", attribute.getRdfNamespace() == null ? null : attribute.getRdfNamespace().getId());
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        getNamedParameterJdbcTemplate().update(sql, parameterMap);
    }

    @Override
    public void updateVocabularyBinding(int attributeId, int vocabularyId) {
        String sql = "insert into M_ATTRIBUTE_VOCABULARY "
                + "(M_ATTRIBUTE_ID, VOCABULARY_ID) "
                + "values (:attrDefId, :vocId) "
                + "on duplicate key update "
                + "VOCABULARY_ID=VALUES(VOCABULARY_ID)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("attrDefId", attributeId);
        params.put("vocId", vocabularyId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int countAttributeValues(int attributeId) {
        String sql = "select count(Distinct(PARENT_TYPE), DATAELEM_ID) from ATTRIBUTE where M_ATTRIBUTE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", attributeId);
        return getNamedParameterJdbcTemplate().queryForInt(sql, params);
    }

    @Override
    public void delete(int id) {
        String sql = "delete from M_ATTRIBUTE where M_ATTRIBUTE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteVocabularyBinding(int attributeId) {
        String sql = "delete from M_ATTRIBUTE_VOCABULARY where M_ATTRIBUTE_ID = :id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", attributeId);
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteValues(int attributeId) {
        String sql = "delete from ATTRIBUTE where M_ATTRIBUTE_ID = :attributeId";
        Map<String, Object> params = this.createParameterMap();
        params.put("attributeId", attributeId);
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public Map<DataDictEntity.Entity, Integer> getConceptsWithAttributeValues(int attributeId) {
        String sql = "select PARENT_TYPE, count(PARENT_TYPE) as COUNT_RES from ATTRIBUTE where M_ATTRIBUTE_ID = :id group by PARENT_TYPE";
        Map<String, Object> params = this.createParameterMap();
        params.put("id", attributeId);
        List<Map<DataDictEntity.Entity, Integer>> listOfMaps = getNamedParameterJdbcTemplate().query(sql, params, new MapRowMapper());
        Map<DataDictEntity.Entity, Integer> map = new HashMap();
        for (Map<DataDictEntity.Entity, Integer> singleLineMap : listOfMaps) {
            for (DataDictEntity.Entity key : singleLineMap.keySet()) {
                map.put(key, singleLineMap.get(key));
            }
        }
        return map;
    }

    protected static class MapRowMapper implements RowMapper<Map<DataDictEntity.Entity, Integer>> {

        @Override
        public Map<DataDictEntity.Entity, Integer> mapRow(ResultSet rs, int i) throws SQLException {
            Map<DataDictEntity.Entity, Integer> map = new HashMap<DataDictEntity.Entity, Integer>();
            map.put(new DataDictEntityConverter().convertBack(rs.getString("PARENT_TYPE")), rs.getInt("COUNT_RES"));
            return map;
        }

    }

    protected static class DataDictEntityRowMapper implements RowMapper<DataDictEntity> {

        @Override
        public DataDictEntity mapRow(ResultSet rs, int i) throws SQLException {
            DataDictEntity datadictEntity = new DataDictEntity();
            datadictEntity.setId(rs.getInt("DATAELEM_ID"));
            datadictEntity.setType(DataDictEntity.Entity.E);
            return datadictEntity;
        }
    }

    protected static class AttributeRowMapper implements RowMapper<Attribute> {

        @Override
        public Attribute mapRow(ResultSet rs, int i) throws SQLException {
            Attribute attribute = new Attribute();
            attribute.setId(rs.getInt("M_ATTRIBUTE.M_ATTRIBUTE_ID"));
            attribute.setDisplayOrder(rs.getInt("M_ATTRIBUTE.DISP_ORDER") == 999 ? null : rs.getInt("M_ATTRIBUTE.DISP_ORDER"));
            attribute.setTargetEntities(new TargetEntityConverter().convertBack(rs.getInt("M_ATTRIBUTE.DISP_WHEN")));
            attribute.setDisplayWidth(rs.getInt("M_ATTRIBUTE.DISP_WIDTH"));
            attribute.setDisplayHeight(rs.getInt("M_ATTRIBUTE.DISP_HEIGHT"));
            attribute.setLanguageUsed(rs.getBoolean("M_ATTRIBUTE.LANGUAGE_USED"));
            attribute.setName(rs.getString("M_ATTRIBUTE.NAME"));
            attribute.setDefinition(rs.getString("M_ATTRIBUTE.DEFINITION"));
            attribute.setShortName(rs.getString("M_ATTRIBUTE.SHORT_NAME"));
            attribute.setDisplayMultiple(rs.getBoolean("M_ATTRIBUTE.DISP_MULTIPLE"));
            attribute.setRdfPropertyName(rs.getString("M_ATTRIBUTE.RDF_PROPERTY_NAME"));
            attribute.setObligationType(new ObligationTypeConverter().convertBack(rs.getString("M_ATTRIBUTE.OBLIGATION")));
            attribute.setDisplayType(new DisplayTypeConverter().convertBack(rs.getString("M_ATTRIBUTE.DISP_TYPE")));
            attribute.setValueInheritanceMode(new ValueInheritanceConverter().convertBack(rs.getString("M_ATTRIBUTE.INHERIT")));
            attribute.setDataType(AttributeDataType.getEnum(rs.getString("M_ATTRIBUTE.DATA_TYPE")));

            int namespaceId = rs.getInt("M_ATTRIBUTE.NAMESPACE_ID");

            if (!rs.wasNull()) {
                Namespace ns = new Namespace();
                ns.setId(namespaceId);
                attribute.setNamespace(ns);
                this.readNamespace(rs, attribute);
            }

            int rdfNamespaceId = rs.getInt("M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID");

            if (!rs.wasNull()) {
                RdfNamespace rdfNamespace = new RdfNamespace();
                rdfNamespace.setId(rdfNamespaceId);
                attribute.setRdfNamespace(rdfNamespace);
                this.readRdfNamespace(rs, attribute);
            }

            int vocabularyId = rs.getInt("VOCABULARY_ID");

            if (!rs.wasNull()) {
                VocabularyFolder vocabulary = new VocabularyFolder();
                vocabulary.setId(vocabularyId);
                attribute.setVocabulary(vocabulary);
            }

            return attribute;
        }

        protected void readNamespace(ResultSet rs, Attribute attribute) throws SQLException {
            rs.getInt("NAMESPACE.NAMESPACE_ID");

            if (rs.wasNull()) {
                return;
            }

            attribute.getNamespace().setShortName(rs.getString("NAMESPACE.SHORT_NAME"));
            attribute.getNamespace().setFullName(rs.getString("NAMESPACE.FULL_NAME"));
            attribute.getNamespace().setDefinition(rs.getString("NAMESPACE.DEFINITION"));
            attribute.getNamespace().setWorkingUser(rs.getString("NAMESPACE.WORKING_USER"));
        }

        protected void readRdfNamespace(ResultSet rs, Attribute attribute) throws SQLException {
            rs.getInt("RDF_ID");

            if (rs.wasNull()) {
                return;
            }

            attribute.getRdfNamespace().setPrefix(rs.getString("RDF_PREFIX"));
            attribute.getRdfNamespace().setUri(rs.getString("RDF_URI"));
        }

    }

    protected static class DataDictEntityConverter implements DataConverter<DataDictEntity.Entity, String> {

        @Override
        public String convert(DataDictEntity.Entity value) {
            return value.name();
        }

        @Override
        public DataDictEntity.Entity convertBack(String value) {
            for (DataDictEntity.Entity entity : DataDictEntity.Entity.values()) {
                if (entity.name().equals(value)) {
                    return entity;
                }
            }
            return null;
        }
    }

    protected static class TargetEntityConverter implements DataConverter<Set<Attribute.TargetEntity>, Integer> {

        @Override
        public Integer convert(Set<Attribute.TargetEntity> value) {
            int result = 0;

            if (value == null || value.isEmpty()) {
                return result;
            }

            for (Attribute.TargetEntity item : value) {
                result = result | item.getValue();
            }

            return result;
        }

        @Override
        public Set<Attribute.TargetEntity> convertBack(Integer value) {
            Set<Attribute.TargetEntity> result = new HashSet<Attribute.TargetEntity>();

            if (value == 0) {
                return result;
            }

            for (Attribute.TargetEntity item : Attribute.TargetEntity.values()) {
                if ((item.getValue() & value) != 0) {
                    result.add(item);
                }
            }

            return result;
        }

    }

    protected static class ObligationTypeConverter implements DataConverter<Attribute.ObligationType, String> {

        @Override
        public String convert(Attribute.ObligationType value) {
            switch (value) {
                case MANDATORY:
                    return "M";
                case OPTIONAL:
                    return "O";
                case CONDITIONAL:
                    return "C";
                default:
                    throw new IllegalArgumentException(String.format("Unable to convert obligation type to string: %s", value));
            }
        }

        @Override
        public Attribute.ObligationType convertBack(String value) {
            if (StringUtils.equalsIgnoreCase(value, "M")) {
                return Attribute.ObligationType.MANDATORY;
            }

            if (StringUtils.equalsIgnoreCase(value, "O")) {
                return Attribute.ObligationType.OPTIONAL;
            }

            if (StringUtils.equalsIgnoreCase(value, "C")) {
                return Attribute.ObligationType.CONDITIONAL;
            }

            throw new IllegalArgumentException(String.format("Unable to convert stirng to obligation type: %s", value));
        }

    }

    protected static class DisplayTypeConverter implements DataConverter<Attribute.DisplayType, String> {

        @Override
        public String convert(Attribute.DisplayType value) {
            if (value == null) {
                return null;
            }

            return value.toString().toLowerCase();
        }

        @Override
        public Attribute.DisplayType convertBack(String value) {
            if (value == null) {
                return null;
            }

            for (Attribute.DisplayType displayType : Attribute.DisplayType.values()) {
                if (StringUtils.endsWithIgnoreCase(displayType.toString(), value)) {
                    return displayType;
                }
            }

            throw new IllegalArgumentException(String.format("Unable to convert stirng to display type: %s", value));
        }

    }

    protected static class ValueInheritanceConverter implements DataConverter<Attribute.ValueInheritanceMode, String> {

        @Override
        public String convert(Attribute.ValueInheritanceMode value) {
            switch (value) {
                case NONE:
                    return "0";
                case PARENT_WITH_EXTEND:
                    return "1";
                case PARENT_WITH_OVERRIDE:
                    return "2";
                default:
                    throw new IllegalArgumentException(String.format("Unable to convert value inheritance mode to string: %s", value));
            }
        }

        @Override
        public Attribute.ValueInheritanceMode convertBack(String value) {
            if (StringUtils.equals(value, "0")) {
                return Attribute.ValueInheritanceMode.NONE;
            }

            if (StringUtils.equals(value, "1")) {
                return Attribute.ValueInheritanceMode.PARENT_WITH_EXTEND;
            }

            if (StringUtils.equals(value, "2")) {
                return Attribute.ValueInheritanceMode.PARENT_WITH_OVERRIDE;
            }

            throw new IllegalArgumentException(String.format("Unable to convert stirng to value inheritance mode: %s", value));
        }

    }

    public static final int DISPLAY_WIDTH_DEFAULT = 20;
    public static final int DISPLAY_HEIGHT_DEFAULT = 1;
    public static final int DISPLAY_ORDER_DEFAULT = 999;
    public static final int NAMESPACE_ID_DEFAULT = 3;

}
