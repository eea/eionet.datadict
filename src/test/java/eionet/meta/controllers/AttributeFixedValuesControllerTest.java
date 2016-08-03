package eionet.meta.controllers;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.application.AppContextProvider;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.controllers.impl.AttributeFixedValuesControllerImpl;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.service.FixedValuesService;
import eionet.util.CompoundDataObject;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class AttributeFixedValuesControllerTest {
    
    private AttributeFixedValuesController controller;
    
    @Mock
    private FixedValuesService fixedValuesService;
    
    @Mock
    private IAttributeDAO attributeDao;
    
    @Mock
    private IFixedValueDAO fixedValueDao;
    
    @Mock
    private AppContextProvider contextProvider;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.controller = new AttributeFixedValuesControllerImpl(this.fixedValuesService, this.attributeDao, this.fixedValueDao);
    }
    
    @Test
    public void testGetOwnerAttribute() throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createOwner(ownerId);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        SimpleAttribute actual = this.controller.getOwnerAttribute(ownerId);
        assertEquals(expected, actual);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetOwnerAttributeBecauseOfOwnerNotFound() throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getOwnerAttribute(ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetOwnerAttributeBecauseOfOwnership() 
             throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getOwnerAttribute(ownerId);
    }
    
    @Test
    public void testGetEditableOwnerAttribute() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        SimpleAttribute actual = this.controller.getEditableOwnerAttribute(contextProvider, ownerId);
        assertEquals(expected, actual);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetEditableOwnerAttributeBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getEditableOwnerAttribute(contextProvider, 1);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableOwnerAttributeBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getEditableOwnerAttribute(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableOwnerAttributeBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getEditableOwnerAttribute(contextProvider, ownerId);
    }
    
    @Test
    public void testGetAllValuesModel() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(attributeDao.getFixedValues(ownerId)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getAllValuesModel(ownerId);
        SimpleAttribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotFound() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getAllValuesModel(ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnership() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getAllValuesModel(ownerId);
    }
    
    @Test
    public void testGetEditableAllValuesModel() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(attributeDao.getFixedValues(ownerId)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getEditableAllValuesModel(contextProvider, ownerId);
        SimpleAttribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getEditableAllValuesModel(contextProvider, 1);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test
    public void testDeleteFixedValues() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        this.controller.deleteFixedValues(contextProvider, ownerId);
        verify(fixedValueDao, times(1)).deleteAll(FixedValue.OwnerType.ATTRIBUTE, ownerId);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.deleteFixedValues(contextProvider, 1);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test
    public void testGetSingleValueModel() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(4, expectedOwner, "val");
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValueId)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getSingleValueModel(ownerId, fixedValueId);
        SimpleAttribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getSingleValueModel(ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getSingleValueModel(ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfValueNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValueId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getSingleValueModel(ownerId, fixedValueId);
    }
    
    @Test
    public void testGetEditableSingleValueModel() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(4, expectedOwner, "val");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValueId)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getEditableSingleValueModel(contextProvider, ownerId, fixedValueId);
        SimpleAttribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getEditableSingleValueModel(contextProvider, 1, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfValueNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValueId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, fixedValueId);
    }
    
    @Test
    public void testDeleteFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValue = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(4, expectedOwner, "val");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValue)).thenReturn(expectedValue);
        this.controller.deleteFixedValue(contextProvider, ownerId, fixedValue);
        verify(fixedValueDao, times(1)).deleteById(expectedValue.getId());
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.deleteFixedValue(contextProvider, 1, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfValueNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int fixedValueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValueId)).thenThrow(ResourceNotFoundException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, fixedValueId);
    }
    
    @Test
    public void testSaveFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        final FixedValue fxv = this.createSaveInput(10, "val");
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        this.controller.saveFixedValue(contextProvider, ownerId, fxv);
        verify(fixedValuesService, times(1)).saveFixedValue(expectedOwner, fxv);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.saveFixedValue(contextProvider, 3, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(null);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToSaveFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, 
                   EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        SimpleAttribute expected = this.createNonOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfValueNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        final int valueId = 10;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(valueId, "val");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(ResourceNotFoundException.class).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    @Test(expected = EmptyParameterException.class)
    public void testFailToSaveFixedValueBecauseOfEmptyValue() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(10, null);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(EmptyParameterException.class).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToSaveFixedValueBecauseOfDuplicateValue() 
            throws UserAuthenticationException, ResourceNotFoundException, 
                   ConflictException, EmptyParameterException, DuplicateResourceException {
        final int ownerId = 5;
        SimpleAttribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(10, "duplicate");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(new DuplicateResourceException(inValue.getValue())).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    private SimpleAttribute createOwner(int id) {
        SimpleAttribute owner = new SimpleAttribute();
        owner.setAttributeId(id);
        owner.setInputType(SimpleAttribute.DisplayType.SELECT_BOX.toString());
        
        return owner;
    }
    
    private SimpleAttribute createNonOwner(int id) {
        SimpleAttribute owner = new SimpleAttribute();
        owner.setAttributeId(id);
        
        return owner;
    }
    
    private FixedValue createFixedValue(int id, SimpleAttribute owner, String value) {
        FixedValue fxv = new FixedValue();
        fxv.setId(id);
        fxv.setOwnerType(FixedValue.OwnerType.ATTRIBUTE.toString());
        fxv.setOwnerId(owner.getAttributeId());
        fxv.setValue(value);
        
        return fxv;
    }
    
    private FixedValue createSaveInput(int id, String value) {
        FixedValue payload = new FixedValue();
        payload.setId(id);
        payload.setValue(value);
        
        return payload;
    }
}
