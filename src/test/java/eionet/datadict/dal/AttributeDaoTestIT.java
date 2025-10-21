package eionet.datadict.dal;

import eionet.config.ApplicationTestContext;
import eionet.datadict.model.Attribute;
import eionet.datadict.model.Attribute.DisplayType;
import eionet.datadict.model.Attribute.ObligationType;
import eionet.datadict.model.Attribute.TargetEntity;
import eionet.datadict.model.Attribute.ValueInheritanceMode;
import eionet.datadict.model.DataDictEntity;
import eionet.datadict.model.Namespace;
import eionet.datadict.model.RdfNamespace;
import eionet.meta.service.DBUnitHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class
   })

public class AttributeDaoTestIT {

    @Autowired
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
    public void testGetAll() {
        List<Attribute> attributes = this.attributeDao.getAll();
        assertEquals(attributes.size(), 7);
        List<Integer> attributeIds = attributes.stream().map(Attribute::getId).collect(Collectors.toList());
        assertTrue(attributeIds.containsAll(Arrays.asList(80, 81, 82, 37, 39, 40, 23)));
    }

    @Test
    public void testGetByIdForTargetEntities() {
        Attribute attribute37 = this.attributeDao.getById(37);
        EnumSet<Attribute.TargetEntity> targetEntities = attribute37.getTargetEntities();
        assertNotNull(targetEntities);
        assertTrue(targetEntities.size() == 4);
        assertTrue(targetEntities.containsAll(
                Arrays.asList(
                        Attribute.TargetEntity.CH1,
                        Attribute.TargetEntity.CH2,
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
        assertTrue(attributeDao.exists(37));
    }

    @Test
    public void testCreate() {

        Namespace namespace = new Namespace();
        namespace.setId(3);

        RdfNamespace rdfNamespace = new RdfNamespace();
        rdfNamespace.setId(1);

        EnumSet<TargetEntity> entities = EnumSet.noneOf(TargetEntity.class);
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
        assertEquals(6, count);
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

    @Test
    public void testGetVocabularyBinding() {
        Integer expectedVocabularyID = 1;
        Integer actualVocabularyID = this.attributeDao.getVocabularyBinding(37);
        assertEquals(expectedVocabularyID, actualVocabularyID);
    }

    @Test
    public void testGetCombinedDatasetAndDataTableAttributes() {
        List<Attribute> actualAttributes = this.attributeDao.getCombinedDataSetAndDataTableAttributes(1692, 738);
        assertEquals(2, actualAttributes.size());
    }

    @Test
    public void testGetAttributesOfDataTable() {
        List<Attribute> actualAttributes = this.attributeDao.getCombinedDataSetAndDataTableAttributes(1692, 738);
        assertEquals(2, actualAttributes.size());
    }

    @Test
    public void testGetByDatadictDataSetEntity() {
        List<Attribute> actualAttributes = this.attributeDao.getByDataDictEntity(new DataDictEntity(740, DataDictEntity.Entity.DS));
        assertEquals(2, actualAttributes.size());
    }

    @Test
    public void testGetByDatadictDataSetTableEntity() {
        List<Attribute> actualAttributes = this.attributeDao.getByDataDictEntity(new DataDictEntity(750, DataDictEntity.Entity.T));
        assertEquals(2, actualAttributes.size());
    }
    
    
    @Test
    public void testAttributeRowMapperMapRow() {
        Attribute attribute37 = this.attributeDao.getById(37);
        performDaoAssertions(attribute37, 37, "EEA Issue", "EEAissue", "An issue form the EEA environmental issues list.",
                6, DISPLAY_HEIGHT_DEFAULT, DISPLAY_WIDTH_DEFAULT, true, ValueInheritanceMode.PARENT_WITH_EXTEND, 3, null, Attribute.DisplayType.SELECT, Attribute.ObligationType.OPTIONAL);
    }

    @Test
    public void testCombinedAttributeRowMapperMapRow() {
        List<Attribute> actualAttributes = this.attributeDao.getByDataDictEntity(new DataDictEntity(741, DataDictEntity.Entity.T));
        assertEquals(1, actualAttributes.size());
        performDaoAssertions(actualAttributes.get(0), 23, "Descriptive image", "Descriptive_image", "DEF",
                null, DISPLAY_HEIGHT_DEFAULT, DISPLAY_WIDTH_DEFAULT, true, ValueInheritanceMode.PARENT_WITH_EXTEND, 3, null, DisplayType.IMAGE, ObligationType.OPTIONAL);
    }

    private Attribute createAttributeObject(
            String name, String shortName, String definition, Integer displayOrder,
            Integer height, Integer width, boolean displayMultiple, ValueInheritanceMode valueInheritanceMode,
            Namespace namespace, RdfNamespace rdfNamespace, DisplayType displayType,
            ObligationType obligationType, EnumSet<TargetEntity> targetEntities) {

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
            Integer namespaceId, Integer rdfNamespaceId, DisplayType displayType,
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
        if (rdfNamespaceId == null) {
            assertEquals(null, attribute.getRdfNamespace());
        } else {
            assertEquals(rdfNamespaceId, attribute.getRdfNamespace().getId());
        }
        assertEquals(displayType, attribute.getDisplayType());
        assertEquals(obligationType, attribute.getObligationType());
    }

}
