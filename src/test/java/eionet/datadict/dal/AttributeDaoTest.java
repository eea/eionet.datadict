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
import org.junit.AfterClass;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

@SpringApplicationContext("mock-spring-context.xml")
public class AttributeDaoTest extends UnitilsJUnit4{
    
    @SpringBeanByType
    AttributeDao attributeDao;
    
    public static final int DISPLAY_WIDTH_DEFAULT = 20;
    public static final int DISPLAY_HEIGHT_DEFAULT = 1;
    public static final int DISPLAY_ORDER_DEFAULT = 999;
    public static final int NAMESPACE_ID_DEFAULT = 3;
    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-attribute.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-attribute.xml");
    }
    
    @Test
    public void testGetByIdForSimpleFields() {
        Attribute attribute37 = this.attributeDao.getById(37);
        performDaoAssertions(attribute37, 37, "EEA Issue", "EEAissue", "An issue form the EEA environmental issues list.", 
                6, DISPLAY_HEIGHT_DEFAULT, DISPLAY_WIDTH_DEFAULT, true, ValueInheritanceMode.PARENT_WITH_EXTEND, 3, null, Attribute.DisplayType.SELECT, Attribute.ObligationType.OPTIONAL);       
    }
    
    @Test
    public void testGetByIdForTargetEntities() {
        Attribute attribute37 = this.attributeDao.getById(37);
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
        Attribute attribute40 = this.attributeDao.getById(40);
        assertEquals(null, attribute40.getDisplayOrder());
    }
    
    
    @Test
    public void testGetByIdForVocabularyBinding() {
        Attribute attribute40 = this.attributeDao.getById(40);
        assertNotNull(attribute40.getVocabulary());
        assertEquals(1, attribute40.getVocabulary().getId());
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
        assertEquals("shortName2", this.attributeDao.getById(id).getShortName());
    }
    
    @Test
    public void testUpdate() {
        Attribute attribute80 = this.attributeDao.getById(80);
        
        attribute80.setName("testName");
        attribute80.setDisplayOrder(null);
        attribute80.setObligationType(ObligationType.CONDITIONAL);
        attribute80.setDisplayHeight(null);
        attribute80.setDisplayWidth(null);
        attribute80.setNamespace(null);
        attribute80.setRdfNamespace(null);
        
        
        this.attributeDao.update(attribute80);
        attribute80 = this.attributeDao.getById(80);
        
        assertEquals("testName", attribute80.getName());
        assertEquals(null, attribute80.getDisplayOrder());
        assertEquals(ObligationType.CONDITIONAL, attribute80.getObligationType());
        assertEquals(new Integer(DISPLAY_HEIGHT_DEFAULT), attribute80.getDisplayHeight());
        assertEquals(new Integer(DISPLAY_WIDTH_DEFAULT), attribute80.getDisplayWidth());
        assertNotNull(attribute80.getNamespace());
        assertEquals(new Integer(NAMESPACE_ID_DEFAULT), attribute80.getNamespace().getId());
        assertNull(attribute80.getRdfNamespace());
    }
    
    @Test
    public void testUpdateVocabularyBindingSimple() {
        this.attributeDao.updateVocabularyBinding(37, 1);
        Attribute attribute37 = this.attributeDao.getById(37);
        
        assertNotNull(attribute37.getVocabulary());
        assertEquals(1, attribute37.getVocabulary().getId());
    }
    
    @Test
    public void testUpdateVocabularyBindingDuplicateKey() {
        this.attributeDao.updateVocabularyBinding(40, 2);
        Attribute attribute40 = this.attributeDao.getById(40);
        
        assertNotNull(attribute40.getVocabulary());
        assertEquals(2, attribute40.getVocabulary().getId());
    }
    
    @Test 
    public void testCountAttributeValues() {
        int count = this.attributeDao.countAttributeValues(37);
        assertEquals(5, count); 
    }
    
   
    @Test
    public void testDelete() {
        this.attributeDao.delete(80);
        assertFalse(this.attributeDao.exists(80));
    }
   
    @Test
    public void testDeleteVocabularyBinding() {
        this.attributeDao.deleteVocabularyBinding(40);
        Attribute attribute40 = this.attributeDao.getById(40);
        assertNull(attribute40.getVocabulary());
    }
    
    @Test
    public void testDeleteValues() {
        this.attributeDao.deleteValues(37);
        assertEquals(0, this.attributeDao.countAttributeValues(37));
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
        
        assertEquals(id, attribute.getId());
        assertEquals(name, attribute.getName());
        assertEquals(shortName, attribute.getShortName());
        assertEquals(definition, attribute.getDefinition());
        assertEquals(displayOrder, attribute.getDisplayOrder());
        assertEquals(height, attribute.getDisplayHeight());
        assertEquals(width, attribute.getDisplayWidth());
        assertEquals(displayMultiple, attribute.isDisplayMultiple());
        assertEquals(valueInheritanceMode, attribute.getValueInheritanceMode());
        assertEquals(namespaceId, attribute.getNamespace().getId());
        if (rdfNamespaceId==null) {
            assertEquals(null, attribute.getRdfNamespace());
        } else {
            assertEquals(rdfNamespaceId, attribute.getRdfNamespace().getId());
        }
        assertEquals(displayType, attribute.getDisplayType());
        assertEquals(obligationType, attribute.getObligationType());
    }
    
    
}
