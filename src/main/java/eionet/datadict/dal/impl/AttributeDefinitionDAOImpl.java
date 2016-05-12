package eionet.datadict.dal.impl;

import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.model.enums.Enumerations.AttributeDataType;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayMultiple;
import eionet.datadict.model.enums.Enumerations.AttributeDisplayType;
import eionet.datadict.model.enums.Enumerations.Inherit;
import eionet.datadict.model.enums.Enumerations.Obligation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;


/**
 *
 * @author eworx-alk
 */
@Repository
public class AttributeDefinitionDAOImpl extends JdbcRepositoryBase implements AttributeDefinitionDAO {
    
    //initially was with ORDER_BY
    private static final String GET_BY_ID = "select "
            + "M_ATTRIBUTE.*, NAMESPACE.*, T_RDF_NAMESPACE.ID AS RDF_ID, T_RDF_NAMESPACE.URI AS RDF_URI "
            + "FROM "
            + "M_ATTRIBUTE "
            + "LEFT JOIN T_RDF_NAMESPACE ON T_RDF_NAMESPACE.ID = M_ATTRIBUTE.RDF_PROPERTY_NAMESPACE_ID "
            + "LEFT JOIN NAMESPACE ON NAMESPACE.NAMESPACE_ID = M_ATTRIBUTE.NAMESPACE_ID "
            + "WHERE M_ATTRIBUTE.M_ATTRIBUTE_ID = :id"; 

    @Autowired
    public AttributeDefinitionDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<AttributeDefinition> getAttributes() {
        String sql = "Select * from M_ATTRIBUTE";
        List<AttributeDefinition> attributeDefinitions =
                getJdbcTemplate().query(sql, new RowMapper<AttributeDefinition>() {
            @Override
            public AttributeDefinition mapRow(ResultSet rs, int i) throws SQLException {
                return createFromSimpleSelectStatement(rs);
            }
        });
        return attributeDefinitions;
    }
    
    @Override
    public AttributeDefinition getAttributeDefinitionById(int id){
        String sql = GET_BY_ID;
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        
        AttributeDefinition attrDef = 
                getNamedParameterJdbcTemplate()
                        .queryForObject(sql, params, new RowMapper<AttributeDefinition>() {
            @Override
            public AttributeDefinition mapRow(ResultSet rs, int i) throws SQLException {
                return createFromSelectWithRdfNamespaceAndNamespace(rs);
            }

        });
        
        return attrDef;
    }
    
    
    
    private AttributeDefinition createFromSelectWithRdfNamespaceAndNamespace(ResultSet rs) throws SQLException {
        AttributeDefinition attrDef = createFromSimpleSelectStatement(rs);
        attrDef = addRdfNamespace(rs, attrDef);
        attrDef = addNamespace(rs, attrDef);
        return attrDef;
    }
    
    
    private AttributeDefinition addRdfNamespace (ResultSet rs, AttributeDefinition attrDefToBeExtended) throws SQLException {
        RdfNamespace rdfNamespace = new RdfNamespace();
        rdfNamespace.setId(rs.getInt("RDF_ID"));
        rdfNamespace.setUri(rs.getString("RDF_URI"));
        attrDefToBeExtended.setRdfNameSpace(rdfNamespace);
        return attrDefToBeExtended;
    }
    
    private AttributeDefinition addNamespace (ResultSet rs, AttributeDefinition attrDefToBeExtended) throws SQLException {
        String namespacePrefix = "NAMESPACE.";
        Namespace namespace = new Namespace();
        namespace.setNamespaceID(rs.getInt(namespacePrefix+"NAMESPACE_ID"));
        namespace.setShortName(rs.getString(namespacePrefix+"SHORT_NAME"));
        namespace.setFullName(rs.getString(namespacePrefix+"FULL_NAME"));
        namespace.setDefinition(rs.getString(namespacePrefix+"DEFINITION"));
        namespace.setParentNS(rs.getInt(namespacePrefix+"PARENT_NS"));
        namespace.setWorkingUser(rs.getString(namespacePrefix+"WORKING_USER"));
        attrDefToBeExtended.setNamespace(namespace);
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
                attrDef.setRdfPropertyName(rs.getString("RDF_PROPERTY_NAME"));
                attrDef.setObligationLevel(
                        Obligation.valueOf(rs.getString("OBLIGATION")));
                attrDef.setDisplayType(AttributeDisplayType
                        .getEnum(rs.getString("DISP_TYPE")));
                attrDef.setDisplayMultiple(AttributeDisplayMultiple
                        .getEnum(rs.getString("DISP_MULTIPLE")));
                attrDef.setInherit(Inherit
                        .getEnum(rs.getString("INHERIT")));
                attrDef.setDatatype(AttributeDataType
                        .getEnum(rs.getString("DATA_TYPE")));
                
                attrDef.setUnknownNames();
                
                return attrDef;
    }

}
