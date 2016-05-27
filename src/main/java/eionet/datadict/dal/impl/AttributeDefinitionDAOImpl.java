package eionet.datadict.dal.impl;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.enums.Enumerations.AttributeDataType;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayType;
import eionet.datadict.model.enums.Enumerations.Inherit;
import eionet.datadict.model.enums.Enumerations.Obligation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.resources.AttributeDefinitionIdInfo;
import eionet.datadict.resources.ResourceType;
import eionet.meta.dao.domain.VocabularyFolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 *
 * @author Aliki Kopaneli
 */
@Repository
public class AttributeDefinitionDAOImpl extends JdbcRepositoryBase implements AttributeDefinitionDAO {

    private static final String GET_BY_ID = "select "
            + "M_ATTRIBUTE.*, NAMESPACE.*, M_ATTRIBUTE_VOCABULARY.VOCABULARY_ID AS VOCABULARY_ID, T_RDF_NAMESPACE.ID AS RDF_ID, T_RDF_NAMESPACE.URI AS RDF_URI "
            + "FROM "
            + "M_ATTRIBUTE "
            + "LEFT JOIN T_RDF_NAMESPACE ON T_RDF_NAMESPACE.ID = M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID "
            + "LEFT JOIN NAMESPACE ON NAMESPACE.NAMESPACE_ID = M_ATTRIBUTE.NAMESPACE_ID "
            + "LEFT JOIN M_ATTRIBUTE_VOCABULARY ON M_ATTRIBUTE_VOCABULARY.M_ATTRIBUTE_ID =  M_ATTRIBUTE.M_ATTRIBUTE_ID "
            + "WHERE M_ATTRIBUTE.M_ATTRIBUTE_ID = :id";

    private static final String UPDATE = "update M_ATTRIBUTE set "
            + "NAME = :name, OBLIGATION = :obligation, DEFINITION = :definition, DISP_TYPE = :dispType, DISP_ORDER = :dispOrder, "
            + "DISP_WIDTH = :dispWidth, DISP_HEIGHT = :dispHeight, DISP_MULTIPLE = :dispMultiple, INHERIT = :inherit, NAMESPACE_ID = :namespaceId, "
            + "DISP_WHEN = :dispWhen, RDF_PROPERTY_NAMESPACE_ID = :rdfPropertyNamespaceId, RDF_PROPERTY_NAME = :rdfPropertyName "
            + "where M_ATTRIBUTE_ID = :id";

    private static final String UPDATE_VOC ="insert into M_ATTRIBUTE_VOCABULARY "
            + "(M_ATTRIBUTE_ID, VOCABULARY_ID) "
            + "values (:attrDefId, :vocId) "
            + "on duplicate key update "
            + "VOCABULARY_ID=VALUES(VOCABULARY_ID)";
    
    private static final String DELETE_VOC ="delete from M_ATTRIBUTE_VOCABULARY "
            + "where M_ATTRIBUTE_ID = :id";
    
    private static final String DELETE = "delete from M_ATTRIBUTE "
            + "where M_ATTRIBUTE_ID = :id";

    private static final String ADD = "insert into M_ATTRIBUTE "
            + "(NAME , SHORT_NAME, OBLIGATION, DEFINITION, DISP_TYPE, DISP_ORDER, "
            + "DISP_WIDTH, DISP_HEIGHT, DISP_MULTIPLE, INHERIT, NAMESPACE_ID, "
            + "DISP_WHEN, RDF_PROPERTY_NAMESPACE_ID, RDF_PROPERTY_NAME) "
            + "values (:name, :shortName, :obligation, :definition, :dispType, :dispOrder, "
            + ":dispWidth, :dispHeight, :dispMultiple, :inherit, :namespaceId, "
            + ":dispWhen, :rdfPropertyNamespaceId, :rdfPropertyName)";

    @Autowired
    public AttributeDefinitionDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void delete(int id) {
        String sql = DELETE;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public int add(AttributeDefinition attrDef) {
        String sql = ADD;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("shortName", attrDef.getShortName());
        params.put("name", attrDef.getName());
        params.put("obligation", attrDef.getObligationLevel().name());
        params.put("definition", attrDef.getDefinition());
        params.put("dispOrder", attrDef.getDisplayOrder());
        params.put("dispWidth", attrDef.getDisplayWidth());
        params.put("dispHeight", attrDef.getDisplayHeight());
        params.put("dispWhen", attrDef.getDisplayWhen());
        if (attrDef.getDisplayMultiple()) {
            params.put("dispMultiple", "1");
        } else {
            params.put("dispMultiple", "0");
        }
        params.put("inherit", attrDef.getInherit().getValue());
        params.put("rdfPropertyName", attrDef.getRdfPropertyName());
        params.put("namespaceId", attrDef.getNamespace().getNamespaceID());
        if (attrDef.getDisplayType() != null) {
            params.put("dispType", attrDef.getDisplayType().getValue());
        } else {
            params.put("dispType", null);
        }
        if (attrDef.getRdfNamespace() != null) {
            params.put("rdfPropertyNamespaceId", attrDef.getRdfNamespace().getId());
        } else {
            params.put("rdfPropertyNamespaceId", null);
        }
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(sql, parameterMap, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public void update(AttributeDefinition attrDef) {
        String sql = UPDATE;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", attrDef.getId());
        params.put("name", attrDef.getName());
        params.put("obligation", attrDef.getObligationLevel().name());
        params.put("definition", attrDef.getDefinition());
        params.put("dispOrder", attrDef.getDisplayOrder());
        params.put("dispWidth", attrDef.getDisplayWidth());
        params.put("dispHeight", attrDef.getDisplayHeight());
        params.put("dispWhen", attrDef.getDisplayWhen());
        if (attrDef.getDisplayType() != null) {
            params.put("dispType", attrDef.getDisplayType().getValue());
        } else {
            params.put("dispType", null);
        }
        if (attrDef.getDisplayMultiple()) {
            params.put("dispMultiple", "1");
        } else {
            params.put("dispMultiple", "0");
        }
        params.put("inherit", attrDef.getInherit().getValue());
        params.put("rdfPropertyName", attrDef.getRdfPropertyName());
        params.put("namespaceId", attrDef.getNamespace().getNamespaceID());
        if (attrDef.getRdfNamespace() != null) {
            params.put("rdfPropertyNamespaceId", attrDef.getRdfNamespace().getId());
        } else {
            params.put("rdfPropertyNamespaceId", null);
        }
        MapSqlParameterSource parameterMap = new MapSqlParameterSource(params);
        getNamedParameterJdbcTemplate().update(sql, parameterMap);
    }

    @Override
    public void updateAttributeDefinitionVocabulary (int attrDefId, int vocId ) {
        String sql = UPDATE_VOC;
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("attrDefId", attrDefId);
        params.put("vocId", vocId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    @Override
    public void removeAttributeDefinitionVocabulary(int attrDefId) {
        String sql = DELETE_VOC;
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("id", attrDefId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }
 
    @Override
    public AttributeDefinition getAttributeDefinitionById(int id) throws ResourceNotFoundException {
        try {
            String sql = GET_BY_ID;

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);

            AttributeDefinition attrDef
                    = getNamedParameterJdbcTemplate()
                    .queryForObject(sql, params, new RowMapper<AttributeDefinition>() {
                        @Override
                        public AttributeDefinition mapRow(ResultSet rs, int i) throws SQLException {
                            return createFromSelectWithReferences(rs);
                        }

                    });
            return attrDef;
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(ResourceType.ATTRIBUTE_DEFINITION, new AttributeDefinitionIdInfo(String.valueOf(id)));
        }
    }

    private AttributeDefinition createFromSelectWithReferences(ResultSet rs) throws SQLException {
        AttributeDefinition attrDef = createFromSimpleSelectStatement(rs);
        attrDef = addRdfNamespace(rs, attrDef);
        attrDef = addNamespace(rs, attrDef);
        attrDef = addVocabulary(rs, attrDef);
        return attrDef;
    }

    private AttributeDefinition addVocabulary(ResultSet rs, AttributeDefinition attrDefToBeExtended) throws SQLException {
        if (rs.getObject("VOCABULARY_ID")!=null) {
            VocabularyFolder vocFolder = new VocabularyFolder();
            vocFolder.setId(rs.getInt("VOCABULARY_ID"));
            attrDefToBeExtended.setVocabulary(vocFolder);
        }
        return attrDefToBeExtended;
    }
    
    private AttributeDefinition addRdfNamespace(ResultSet rs, AttributeDefinition attrDefToBeExtended) throws SQLException {
        RdfNamespace rdfNamespace = new RdfNamespace();
        if (rs.getObject("RDF_ID") != null) {
            rdfNamespace.setId(rs.getInt("RDF_ID"));
            rdfNamespace.setUri(rs.getString("RDF_URI"));
            attrDefToBeExtended.setRdfNameSpace(rdfNamespace);
        }
        return attrDefToBeExtended;
    }

    private AttributeDefinition addNamespace(ResultSet rs, AttributeDefinition attrDefToBeExtended) throws SQLException {
        String namespacePrefix = "NAMESPACE.";
        if (rs.getObject(namespacePrefix + "NAMESPACE_ID") != null) {
            Namespace namespace = new Namespace();
            namespace.setNamespaceID(rs.getInt(namespacePrefix + "NAMESPACE_ID"));
            namespace.setShortName(rs.getString(namespacePrefix + "SHORT_NAME"));
            namespace.setFullName(rs.getString(namespacePrefix + "FULL_NAME"));
            namespace.setDefinition(rs.getString(namespacePrefix + "DEFINITION"));
            namespace.setParentNS(rs.getInt(namespacePrefix + "PARENT_NS"));
            namespace.setWorkingUser(rs.getString(namespacePrefix + "WORKING_USER"));
            attrDefToBeExtended.setNamespace(namespace);
        }
        return attrDefToBeExtended;
    }

    private AttributeDefinition createFromSimpleSelectStatement(ResultSet rs) throws SQLException {
        AttributeDefinition attrDef = new AttributeDefinition();
        attrDef.setId(rs.getInt("M_ATTRIBUTE_ID"));
        attrDef.setDisplayOrder(rs.getInt("DISP_ORDER"));
        attrDef.setDisplayWhen(rs.getInt("DISP_WHEN"));
        attrDef.setDisplayWidth(rs.getInt("DISP_WIDTH"));
        attrDef.setDisplayHeight(rs.getInt("DISP_HEIGHT"));
        attrDef.setLanguageUsed(rs.getBoolean("INHERIT"));
        attrDef.setName(rs.getString("NAME"));
        attrDef.setDefinition(rs.getString("DEFINITION"));
        attrDef.setShortName(rs.getString("SHORT_NAME"));
        attrDef.setDisplayMultiple(rs.getBoolean("DISP_MULTIPLE"));

        attrDef.setRdfPropertyName(rs.getString("RDF_PROPERTY_NAME"));
        attrDef.setObligationLevel(
                Obligation.valueOf(rs.getString("OBLIGATION")));
        attrDef.setDisplayType(AttributeDisplayType
                .getEnum(rs.getString("DISP_TYPE")));
        attrDef.setInherit(Inherit
                .getEnum(rs.getString("INHERIT")));
        attrDef.setDataType(AttributeDataType
                .getEnum(rs.getString("DATA_TYPE")));

        attrDef.setUnknownNames();

        return attrDef;
    }

   
}
