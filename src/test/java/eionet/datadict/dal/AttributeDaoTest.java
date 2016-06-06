package eionet.datadict.dal;

import eionet.datadict.model.Attribute;
import eionet.datadict.model.Attribute.DisplayType;
import eionet.datadict.model.Attribute.ObligationType;
import eionet.datadict.model.Attribute.TargetEntity;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.meta.service.DBUnitHelper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringApplicationContext("mock-spring-context.xml")
public class AttributeDaoTest extends UnitilsJUnit4{
    
    @SpringBeanByType
    AttributeDao attributeDao;
    
    private Attribute attribute37;
    private Attribute attribute40;
    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-attributes.xml");
        attribute37 = this.attributeDao.getById(37);
        attribute40 = this.attributeDao.getById(40);
    }

    @Test
    public void testGetByIdForSimpleFields() {
        performDaoAssertions(attribute37, 37, "EEA Issue", "EEAissue", "An issue form the EEA environmental issues list.", 6, 1, 20, true, ValueInheritanceMode.PARENT_WITH_EXTEND, 3, 1, Attribute.DisplayType.SELECT, Attribute.ObligationType.OPTIONAL);       
    }
    
    @Test
    public void testGetByIdForTargetEntities() {
        
        Set<Attribute.TargetEntity> targetEntities = attribute37.getTargetEntities();
        assertNotNull(targetEntities);
        assertTrue(targetEntities.size()==5);
        assertTrue(targetEntities.containsAll(
                Arrays.asList(
                        Attribute.TargetEntity.CH1, 
                        Attribute.TargetEntity.CH2, 
                        Attribute.TargetEntity.CH3, 
                        Attribute.TargetEntity.TBL, 
                        Attribute.TargetEntity.DST
                )));
    }
    
    @Test
    public void testGetByIdForDisplayOrder() {
        assertEquals(attribute40.getDisplayOrder(), null);
    }
    
    @Test
    public void testExists() {
        assertTrue (attributeDao.exists(37));
    }
    
    @Test
    public void testCreate() {
        
        Namespace namespace = new Namespace();
        namespace.setId(3);
        
        RdfNamespace rdfNamespace = new RdfNamespace();
        rdfNamespace.setId(1);
        
        Set<TargetEntity> entities = new HashSet();
        entities.add(TargetEntity.CH1);
        entities.add(TargetEntity.DST);
        
        Attribute attributeToPersist = this.createAttributeObject("name", "shortName", "definition", 6, 1, 2, true, ValueInheritanceMode.PARENT_WITH_EXTEND, namespace, rdfNamespace, DisplayType.SELECT, ObligationType.CONDITIONAL, entities);
        
        int id = this.attributeDao.create(attributeToPersist);
        
        assertTrue(this.attributeDao.exists(id));
        
        attributeToPersist = this.attributeDao.getById(id);
        performDaoAssertions(attributeToPersist, id, "name", "shortName", "definition", 6, 1, 2, true, ValueInheritanceMode.PARENT_WITH_EXTEND, namespace.getId(), rdfNamespace.getId(), DisplayType.SELECT, ObligationType.CONDITIONAL);
    }
    
    @Test
    public void testCreateWithNullValues() {
        Attribute attributeToPersist = this.createAttributeObject("name2", "shortName2", null, null, null, null, true, ValueInheritanceMode.PARENT_WITH_EXTEND, null, null, null, ObligationType.CONDITIONAL, null);
        int id = this.attributeDao.create(attributeToPersist);
        assertTrue(this.attributeDao.exists(id));
        assertEquals(this.attributeDao.getById(id).getShortName(), "shortName2");
    }
    
    private Attribute createAttributeObject(
            String name, String shortName, String definition, Integer displayOrder,
            Integer height, Integer width, boolean displayMultiple, ValueInheritanceMode valueInheritanceMode,
            Namespace namespace, RdfNamespace rdfNamespace,  DisplayType displayType, 
            ObligationType obligationType, Set<TargetEntity> targetEntities){
        
        Attribute attribute = new Attribute();
        
        attribute.setName(name);
        attribute.setShortName(shortName);
        attribute.setDefinition(definition);
        attribute.setDisplayOrder(displayOrder);
        attribute.setDisplayHeight(height);
        attribute.setDisplayWidth(width);
        attribute.setValueInheritanceMode(valueInheritanceMode);
        attribute.setNamespace(namespace);
        attribute.setRdfNamespace(rdfNamespace);
        attribute.setTargetEntities(targetEntities);
        attribute.setDisplayMultiple(displayMultiple);
        attribute.setDisplayType(displayType);
        attribute.setObligationType(obligationType);
        
        return attribute;
    }
    
    private void performDaoAssertions(Attribute attribute, Integer id, String name, String shortName, String definition,
            Integer displayOrder, Integer height, Integer width, boolean displayMultiple, ValueInheritanceMode valueInheritanceMode,
            Integer namespaceId, Integer rdfNamespaceId,  DisplayType displayType, 
            ObligationType obligationType) {
        
        assertEquals(attribute.getId(), id);
        assertEquals(attribute.getName(), name);
        assertEquals(attribute.getObligationType(), obligationType);
        assertEquals(attribute.getDefinition(), definition);
        assertEquals(attribute.getShortName(), shortName);
        assertEquals(attribute.getValueInheritanceMode(), valueInheritanceMode);
        assertEquals(attribute.getNamespace().getId(), namespaceId);
        assertEquals(attribute.getRdfNamespace().getId(), rdfNamespaceId);
        assertEquals(attribute.getDisplayType(), displayType);
        assertEquals(attribute.getDisplayOrder(), displayOrder);
        assertEquals(attribute.getDisplayHeight(), height);
        assertEquals(attribute.getDisplayWidth(), width);
        assertEquals(attribute.isDisplayMultiple(), displayMultiple);
    }
    
    
}
