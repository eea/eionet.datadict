package eionet.meta.service;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.service.impl.FixedValuesServiceImpl;
import org.apache.commons.lang.builder.EqualsBuilder;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class FixedValueServiceTest {
    
    private FixedValuesService fixedValueService;
    
    @Mock
    private IFixedValueDAO fixedValueDao;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.fixedValueService = new FixedValuesServiceImpl(this.fixedValueDao);
    }
    
    @Test
    public void testGetFixedValueOfAttribute() throws FixedValueNotFoundException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        SimpleAttribute owner = this.createOwnerAttribute(ownerId);
        FixedValue expected = this.createFixedValue(ownerType, ownerId, value);
        when(fixedValueDao.getByValue(ownerType, ownerId, value)).thenReturn(expected);
        FixedValue actual = this.fixedValueService.getFixedValue(owner, value);
        assertEquals(expected, actual);
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testGetNonExistingFixedValueOfAttribute() throws FixedValueNotFoundException {
        SimpleAttribute owner = this.createOwnerAttribute(1);
        when(fixedValueDao.getByValue(any(FixedValue.OwnerType.class), any(Integer.class), any(String.class))).thenReturn(null);
        this.fixedValueService.getFixedValue(owner, "any value");
    }
    
    @Test
    public void testGetFixedValueOfDataElement() throws FixedValueNotFoundException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue expected = this.createFixedValue(ownerType, ownerId, value);
        when(fixedValueDao.getByValue(ownerType, ownerId, value)).thenReturn(expected);
        FixedValue actual = this.fixedValueService.getFixedValue(owner, value);
        assertEquals(expected, actual);
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testGetNonExistingFixedValueOfDataElement() throws FixedValueNotFoundException {
        DataElement owner = this.createOwnerDataElement(1);
        when(fixedValueDao.getByValue(any(FixedValue.OwnerType.class), any(Integer.class), any(String.class))).thenReturn(null);
        this.fixedValueService.getFixedValue(owner, "any value");
    }
    
    @Test
    public void testCreateFixedValueOfAttribute() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        SimpleAttribute owner = this.createOwnerAttribute(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue toBeInserted = toInsertCaptor.getValue();
        this.setNonEditableProperties(inValue, 0, ownerType.toString(), ownerId);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeInserted));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateFixedValueOfDataElement() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue toBeInserted = toInsertCaptor.getValue();
        this.setNonEditableProperties(inValue, 0, ownerType.toString(), ownerId);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeInserted));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateDefaultFixedValueOfAttribute() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        SimpleAttribute owner = this.createOwnerAttribute(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", true);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue toBeInserted = toInsertCaptor.getValue();
        this.setNonEditableProperties(inValue, 0, ownerType.toString(), ownerId);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeInserted));
        verify(fixedValueDao, times(1)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateDefaultFixedValueOfDataElement() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue toBeInserted = toInsertCaptor.getValue();
        this.setNonEditableProperties(inValue, 0, ownerType.toString(), ownerId);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeInserted, new String[] {"defaultValue"}));
        /*
         * As of the time being, Data Elements do not make use of default value functionality. 
         * Thus, fixed values for elements should not be marked as default, even if data input
         * suggests the opposite.
         */
        assertFalse(toBeInserted.isDefaultValue());
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test (expected = EmptyValueException.class)
    public void testFailToSaveFixedValueBecauseOfEmptyValue() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        DataElement owner = this.createOwnerDataElement(1);
        FixedValue inValue = this.createFixedValue(null, "def", "desc", false);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
    }
    
    @Test (expected = DuplicateResourceException.class)
    public void testFailToCreateFixedValueBecauseOfDuplicate() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        SimpleAttribute owner = this.createOwnerAttribute(ownerId);
        FixedValue inValue = this.createFixedValue(value, null, null, false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(true);
        this.fixedValueService.saveFixedValue(owner, null, inValue);
    }
    
    @Test
    public void testUpdateFixedValue() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 5;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        String originalValue = value;
        FixedValue original = this.createFixedValue(1, ownerType, ownerId, originalValue, "orig def", "orig desc", false);
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(original);
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
        ArgumentCaptor<FixedValue> toUpdateCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).update(toUpdateCaptor.capture());
        FixedValue toBeUpdated = toUpdateCaptor.getValue();
        this.mergeNonEditableValues(original, inValue);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeUpdated));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testUpdateFixedValueWithChangedValue() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 5;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        String originalValue = "original_val";
        FixedValue original = this.createFixedValue(1, ownerType, ownerId, originalValue, "orig def", "orig desc", false);
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(original);
        when(fixedValueDao.getByValue(ownerType, ownerId, value)).thenReturn(null);
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
        ArgumentCaptor<FixedValue> toUpdateCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).update(toUpdateCaptor.capture());
        FixedValue toBeUpdated = toUpdateCaptor.getValue();
        this.mergeNonEditableValues(original, inValue);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeUpdated));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testUpdateDefaultFixedValueOfAttribute() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 5;
        String value = "val";
        SimpleAttribute owner = this.createOwnerAttribute(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", true);
        String originalValue = value;
        FixedValue original = this.createFixedValue(1, ownerType, ownerId, originalValue, "orig def", "orig desc", false);
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(original);
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
        ArgumentCaptor<FixedValue> toUpdateCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).update(toUpdateCaptor.capture());
        FixedValue toBeUpdated = toUpdateCaptor.getValue();
        this.mergeNonEditableValues(original, inValue);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeUpdated));
        verify(fixedValueDao, times(1)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testUpdateDefaultFixedValueOfDataElement() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 5;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", true);
        String originalValue = value;
        FixedValue original = this.createFixedValue(1, ownerType, ownerId, originalValue, "orig def", "orig desc", false);
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(original);
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
        ArgumentCaptor<FixedValue> toUpdateCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).update(toUpdateCaptor.capture());
        FixedValue toBeUpdated = toUpdateCaptor.getValue();
        this.mergeNonEditableValues(original, inValue);
        assertTrue(EqualsBuilder.reflectionEquals(inValue, toBeUpdated));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToUpdateBecausePreviousNotExists() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 5;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        String originalValue = value;
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(null);
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToUpdateBecauseOfDuplicate() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 5;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue inValue = this.createFixedValue(value, "definition", "short description", false);
        String originalValue = "original_val";
        FixedValue original = this.createFixedValue(1, ownerType, ownerId, originalValue, "orig def", "orig desc", false);
        when(fixedValueDao.getByValue(ownerType, ownerId, originalValue)).thenReturn(original);
        when(fixedValueDao.getByValue(ownerType, ownerId, value)).thenReturn(this.createFixedValue(100, ownerType, 10, value, null, null, false));
        this.fixedValueService.saveFixedValue(owner, originalValue, inValue);
    }
    
    private FixedValue createFixedValue(FixedValue.OwnerType ownerType, int ownerId, String value) {
        FixedValue fxv = new FixedValue();
        fxv.setOwnerType(ownerType == null ? null : ownerType.toString());
        fxv.setOwnerId(ownerId);
        fxv.setValue(value);
        
        return fxv;
    }
    
    private FixedValue createFixedValue(String value, String definition, String shortDescription, boolean defaultValue) {
        FixedValue fxv = new FixedValue();
        fxv.setValue(value);
        fxv.setDefinition(definition);
        fxv.setShortDescription(shortDescription);
        fxv.setDefaultValue(defaultValue);
        
        return fxv;
    }
    
    private FixedValue createFixedValue(int id, FixedValue.OwnerType ownerType, int ownerId, String value, String definition, String shortDescription, boolean defaultValue) {
        FixedValue fxv = this.createFixedValue(value, definition, shortDescription, defaultValue);
        fxv.setId(id);
        fxv.setOwnerType(ownerType == null ? null : ownerType.toString());
        fxv.setOwnerId(ownerId);
        
        return fxv;
    }
    
    private SimpleAttribute createOwnerAttribute(int ownerId) {
        SimpleAttribute owner = new SimpleAttribute();
        owner.setAttributeId(ownerId);
        
        return owner;
    }
    
    private DataElement createOwnerDataElement(int ownerId) {
        DataElement owner = new DataElement();
        owner.setId(ownerId);
        
        return owner;
    }
    
    private void mergeNonEditableValues(FixedValue source, FixedValue target) {
        this.setNonEditableProperties(target, source.getId(), source.getOwnerType(), source.getOwnerId());
    }
    
    private void setNonEditableProperties(FixedValue target, int id, String ownerType, int ownerId) {
        target.setId(id);
        target.setOwnerType(ownerType);
        target.setOwnerId(ownerId);
    }
}
