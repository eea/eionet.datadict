package eionet.meta.dao;

import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.DBUnitHelper;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@SpringApplicationContext("mock-spring-context.xml")
public class FixedValueDaoTestIT extends UnitilsJUnit4 {
    
    private static final int FXV_2_ID = 2;
    private static final String FXV_2_VALUE = "element second fixed value";
    private static final int FXV_2_OWNER_ID = 1;
    private static final int FXV_3_ID = 3;
    private static final String FXV_3_VALUE = "attribute first fixed value";
    private static final String FXV_4_VALUE = "attribute second fixed value";
    private static final int FXV_8880_OWNER_ID=8880;
    private static final int FXV_3_OWNER_ID = 1;
    
    @SpringBeanByType
    private IFixedValueDAO fixedValueDao;
    
    @SpringBeanByType
    private IDataElementDAO dataElementDao;
    
    @SpringBeanByType
    private IAttributeDAO attributeDao;
    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-fixedValues.xml");
    }
    
    @Test
    public void testExistsById() {
        for (int id = 1; id < 5; ++id) {
            assertTrue(String.format("Fixed value with id %d should exist", id), this.fixedValueDao.exists(id));
        }
        
        assertFalse(this.fixedValueDao.exists(1000));
    }
    
    @Test
    public void testExistsByValue() {
        assertTrue(this.fixedValueDao.exists(FixedValue.OwnerType.ATTRIBUTE, FXV_2_OWNER_ID, FXV_3_VALUE));
        assertTrue(this.fixedValueDao.exists(FixedValue.OwnerType.ATTRIBUTE, FXV_3_OWNER_ID, FXV_4_VALUE));
        assertFalse(this.fixedValueDao.exists(FixedValue.OwnerType.DATA_ELEMENT, FXV_2_OWNER_ID, "not existing value"));
        assertFalse(this.fixedValueDao.exists(FixedValue.OwnerType.DATA_ELEMENT, 1000, "not existing value"));
    }
    
    @Test
    public void testGetById() {
        FixedValue fxv1 = this.fixedValueDao.getById(2);
        this.assertFixedValueWithId2(fxv1);
        
        FixedValue fxv2 = this.fixedValueDao.getById(3);
        this.assertFixedValueWithId3(fxv2);
        
        FixedValue fxv3 = this.fixedValueDao.getById(1000);
        assertNull(fxv3);
    }
    
    @Test
    public void testGetByValue() {
        FixedValue fxv1 = this.fixedValueDao.getByValue(FixedValue.OwnerType.DATA_ELEMENT, FXV_8880_OWNER_ID, FXV_2_VALUE);
        this.assertFixedValueWithId2(fxv1);
        
        FixedValue fxv2 = this.fixedValueDao.getByValue(FixedValue.OwnerType.ATTRIBUTE, FXV_3_OWNER_ID, FXV_3_VALUE);
        this.assertFixedValueWithId3(fxv2);
        
        FixedValue fxv3 = this.fixedValueDao.getByValue(FixedValue.OwnerType.ATTRIBUTE, FXV_3_OWNER_ID, "not existing value");
        assertNull(fxv3);
        
        FixedValue fxv4 = this.fixedValueDao.getByValue(FixedValue.OwnerType.ATTRIBUTE, 16, "not existing value");
        assertNull(fxv4);
    }
    
    @Test
    public void testDelete() {
        final int id = 2;
        this.fixedValueDao.deleteById(id);
        FixedValue fxv = this.fixedValueDao.getById(id);
        assertNull(fxv);
    }
    
    @Test
    public void testDeleteAll() {
        final int ownerId = 8880;
        
        List<FixedValue> valuesBefore = this.dataElementDao.getFixedValues(ownerId, false);
        assertFalse(valuesBefore.isEmpty());
        
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerId);
        
        List<FixedValue> valuesAfter = this.dataElementDao.getFixedValues(ownerId, false);
        assertTrue(valuesAfter.isEmpty());
    }
    
    @Test
    public void testCreate() {
        final FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        FixedValue toInsert = new FixedValue();
        toInsert.setValue("some value");
        toInsert.setOwnerId(10);
        toInsert.setOwnerType(ownerType.toString());
        toInsert.setShortDescription("short desc");
        toInsert.setDefinition("def");
        this.fixedValueDao.create(toInsert);
        FixedValue fxv = this.fixedValueDao.getByValue(ownerType, toInsert.getOwnerId(), toInsert.getValue());
        assertNotNull(fxv);
        this.assertFixedValue(toInsert, fxv, true);
    }
    
    @Test
    public void testUpdate() {
        FixedValue toUpdate = this.fixedValueDao.getById(FXV_2_ID);
        toUpdate.setValue("some other value");
        toUpdate.setDefinition("some other definition");
        toUpdate.setShortDescription("some other description");
        toUpdate.setDefaultValue(true);
        this.fixedValueDao.update(toUpdate);
        FixedValue fxv = this.fixedValueDao.getById(toUpdate.getId());
        this.assertFixedValue(toUpdate, fxv, false);
    }
    
    @Test
    public void testUpdateDefaultValue() {
        List<FixedValue> valuesBefore = this.attributeDao.getFixedValues(FXV_3_OWNER_ID);
        boolean updatedFixedValue = false;
        FixedValue previousDefault = null;
        
        for (FixedValue fxv : valuesBefore) {
            if (fxv.isDefaultValue()) {
                previousDefault = fxv;
            }
            else {
                if (!updatedFixedValue) {
                    this.fixedValueDao.updateDefaultValue(FixedValue.OwnerType.ATTRIBUTE, fxv.getOwnerId(), fxv.getValue());
                    updatedFixedValue = true;
                }
            }
        }
        
        assertTrue(updatedFixedValue);
        assertNotNull(previousDefault);
        
        List<FixedValue> valuesAfter = this.attributeDao.getFixedValues(FXV_3_OWNER_ID);
        int newDefaultCount = 0;
        
        for (FixedValue fxv : valuesAfter) {
            if (fxv.isDefaultValue()) {
                newDefaultCount++;
                assertNotEquals(fxv.getId(), previousDefault.getId());
            }
        }
        
        assertEquals(1, newDefaultCount);
    }
    
    private void assertFixedValueWithId2(FixedValue fxv) {
        assertNotNull(fxv);
        assertEquals(FXV_2_ID, fxv.getId());
        assertEquals(FXV_2_VALUE, fxv.getValue());
        assertEquals("definition 2", fxv.getDefinition());
        assertEquals("description 2", fxv.getShortDescription());
        assertFalse(fxv.isDefaultValue());
        assertEquals(FXV_8880_OWNER_ID, fxv.getOwnerId());
        assertEquals(FixedValue.OwnerType.DATA_ELEMENT.toString(), fxv.getOwnerType());
    }
    
    private void assertFixedValueWithId3(FixedValue fxv) {
        assertNotNull(fxv);
        assertEquals(FXV_3_ID, fxv.getId());
        assertEquals(FXV_3_VALUE, fxv.getValue());
        assertEquals("definition 3", fxv.getDefinition());
        assertEquals("description 3", fxv.getShortDescription());
        assertTrue(fxv.isDefaultValue());
        assertEquals(FXV_3_OWNER_ID, fxv.getOwnerId());
        assertEquals(FixedValue.OwnerType.ATTRIBUTE.toString(), fxv.getOwnerType());
    }
    
    private void assertFixedValue(FixedValue expected, FixedValue actual, boolean skipIdAssertion) {
        if (!skipIdAssertion) {
            assertEquals(expected.getId(), actual.getId());
        }
        
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getDefinition(), actual.getDefinition());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.isDefaultValue(), actual.isDefaultValue());
        assertEquals(expected.getOwnerId(), actual.getOwnerId());
        assertEquals(expected.getOwnerType(), actual.getOwnerType());
    }
}
