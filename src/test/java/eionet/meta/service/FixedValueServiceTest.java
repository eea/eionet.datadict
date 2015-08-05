package eionet.meta.service;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.impl.FixedValuesServiceImpl;
import org.apache.commons.lang.builder.EqualsBuilder;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.unitils.UnitilsJUnit4;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class FixedValueServiceTest extends UnitilsJUnit4 {
    
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
        Attribute owner = this.createOwnerAttribute(ownerId);
        FixedValue expected = this.createFixedValue(ownerType, ownerId, value);
        when(fixedValueDao.getByValue(ownerType, ownerId, value)).thenReturn(expected);
        FixedValue actual = this.fixedValueService.getFixedValue(owner, value);
        assertEquals(expected, actual);
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testGetNonExistingFixedValueOfAttribute() throws FixedValueNotFoundException {
        Attribute owner = this.createOwnerAttribute(1);
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
        Attribute owner = this.createOwnerAttribute(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, toCreate);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue inserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(toCreate, inserted));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateDefaultFixedValueOfAttribute() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        Attribute owner = this.createOwnerAttribute(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value, "definition", "short description", true);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, toCreate);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue inserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(toCreate, inserted));
        verify(fixedValueDao, times(1)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateFixedValueOfDataElement() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, toCreate);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue inserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(toCreate, inserted));
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test
    public void testCreateDefaultFixedValueOfDataElement() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value, "definition", "short description", false);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(false);
        this.fixedValueService.saveFixedValue(owner, toCreate);
        ArgumentCaptor<FixedValue> toInsertCaptor = ArgumentCaptor.forClass(FixedValue.class);
        verify(fixedValueDao, times(1)).create(toInsertCaptor.capture());
        FixedValue inserted = toInsertCaptor.getValue();
        assertTrue(EqualsBuilder.reflectionEquals(toCreate, inserted, new String[] {"defaultValue"}));
        assertFalse(inserted.isDefaultValue());
        verify(fixedValueDao, times(0)).updateDefaultValue(ownerType, ownerId, value);
    }
    
    @Test (expected = EmptyValueException.class)
    public void testFailToSaveAttributeFixedValueBecauseOfEmptyValue() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        Attribute owner = this.createOwnerAttribute(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, null);
        this.fixedValueService.saveFixedValue(owner, toCreate);
    }
    
    @Test (expected = EmptyValueException.class)
    public void testFailToSaveDataElementFixedValueBecauseOfEmptyValue() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, null);
        this.fixedValueService.saveFixedValue(owner, toCreate);
    }
    
    @Test (expected = DuplicateResourceException.class)
    public void testFailToCreateAttributeFixedValueBecauseOfDuplicate() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.ATTRIBUTE;
        int ownerId = 1;
        String value = "val";
        Attribute owner = this.createOwnerAttribute(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(true);
        this.fixedValueService.saveFixedValue(owner, toCreate);
    }
    
    @Test (expected = DuplicateResourceException.class)
    public void testFailToCreateDataElementFixedValueBecauseOfDuplicate() throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.DATA_ELEMENT;
        int ownerId = 1;
        String value = "val";
        DataElement owner = this.createOwnerDataElement(ownerId);
        FixedValue toCreate = this.createFixedValue(ownerType, ownerId, value);
        when(fixedValueDao.exists(ownerType, ownerId, value)).thenReturn(true);
        this.fixedValueService.saveFixedValue(owner, toCreate);
    }
    
    private FixedValue createFixedValue(FixedValue.OwnerType ownerType, int ownerId, String value) {
        FixedValue fxv = new FixedValue();
        fxv.setOwnerType(ownerType.toString());
        fxv.setOwnerId(ownerId);
        fxv.setValue(value);
        
        return fxv;
    }
    
    private FixedValue createFixedValue(FixedValue.OwnerType ownerType, int ownerId, String value, String definition, String shortDescription, boolean defaultValue) {
        FixedValue fxv = this.createFixedValue(ownerType, ownerId, value);
        fxv.setDefinition(definition);
        fxv.setShortDescription(shortDescription);
        fxv.setDefaultValue(defaultValue);
        
        return fxv;
    }
    
    private Attribute createOwnerAttribute(int ownerId) {
        Attribute owner = new Attribute();
        owner.setId(ownerId);
        
        return owner;
    }
    
    private DataElement createOwnerDataElement(int ownerId) {
        DataElement owner = new DataElement();
        owner.setId(ownerId);
        
        return owner;
    }
}
