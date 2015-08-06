package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.controllers.impl.AttributeFixedValuesControllerImpl;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
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
    public void testGetOwnerAttribute() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        Attribute expected = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expected);
        Attribute actual = this.controller.getOwnerAttribute(contextProvider, Integer.toString(ownerId));
        assertEquals(expected, actual);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetOwnerAttributeBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getOwnerAttribute(contextProvider, "1");
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetOwnerAttributeBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.getOwnerAttribute(contextProvider, "a");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetOwnerAttributeBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.getOwnerAttribute(contextProvider, Integer.toString(ownerId));
    }
    
    @Test
    public void testGetAllValuesModel() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        Attribute expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(attributeDao.getFixedValues(ownerId)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId));
        Attribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetAllValuesModelBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getAllValuesModel(contextProvider, "1");
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetAllValuesModelBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.getAllValuesModel(contextProvider, "a");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId));
    }
    
    @Test
    public void testDeleteFixedValues() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        Attribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
        verify(fixedValueDao, times(1)).deleteAll(FixedValue.OwnerType.ATTRIBUTE, ownerId);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.deleteFixedValues(contextProvider, "1");
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToDeleteFixedValuesBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.deleteFixedValues(contextProvider, "a");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test
    public void testGetSingleValueModel() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        final String fixedValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(4, expectedOwner, fixedValue);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValue)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), fixedValue);
        Attribute actualOwner = result.get(AttributeFixedValuesController.PROPERTY_OWNER_ATTRIBUTE);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(AttributeFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetSingleValueModelBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.getSingleValueModel(contextProvider, "1", "val");
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetSingleValueModelBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.getSingleValueModel(contextProvider, "a", "val");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfValueNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        final String fixedValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValue)).thenThrow(FixedValueNotFoundException.class);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), fixedValue);
    }
    
    @Test
    public void testDeleteFixedValue() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        final String fixedValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(4, expectedOwner, fixedValue);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValue)).thenReturn(expectedValue);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), fixedValue);
        verify(fixedValueDao, times(1)).deleteById(expectedValue.getId());
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.deleteFixedValue(contextProvider, "1", "val");
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToDeleteFixedValueBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.deleteFixedValue(contextProvider, "a", "val");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfValueNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        final int ownerId = 5;
        final String fixedValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, fixedValue)).thenThrow(FixedValueNotFoundException.class);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), fixedValue);
    }
    
    @Test
    public void testSaveFixedValue() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        final int ownerId = 5;
        final String originalValue = "val";
        final FixedValue fxv = this.createSaveInput(originalValue);
        Attribute expectedOwner = this.createOwner(ownerId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, fxv);
        verify(fixedValuesService, times(1)).saveFixedValue(expectedOwner, originalValue, fxv);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.controller.saveFixedValue(contextProvider, "3", "val", this.createSaveInput("val"));
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToSaveFixedValueBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        this.controller.saveFixedValue(contextProvider, "a", "val", this.createSaveInput("val"));
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        final int ownerId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(false);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), "val", this.createSaveInput("val"));
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfValueNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        final int ownerId = 5;
        final String originalValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(originalValue);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(FixedValueNotFoundException.class).when(fixedValuesService).saveFixedValue(expectedOwner, originalValue, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, inValue);
    }
    
    @Test(expected = EmptyValueException.class)
    public void testFailToSaveFixedValueBecauseOfEmptyValue() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        final int ownerId = 5;
        final String originalValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(null);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(EmptyValueException.class).when(fixedValuesService).saveFixedValue(expectedOwner, originalValue, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, inValue);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToSaveFixedValueBecauseOfDuplicateValue() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueNotFoundException, EmptyValueException, DuplicateResourceException {
        final int ownerId = 5;
        final String originalValue = "val";
        Attribute expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput("duplicate");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(attributeDao.exists(ownerId)).thenReturn(true);
        when(attributeDao.getById(ownerId)).thenReturn(expectedOwner);
        doThrow(DuplicateResourceException.class).when(fixedValuesService).saveFixedValue(expectedOwner, originalValue, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, inValue);
    }
    
    private Attribute createOwner(int id) {
        Attribute owner = new Attribute();
        owner.setId(id);
        
        return owner;
    }
    
    private FixedValue createFixedValue(int id, Attribute owner, String value) {
        FixedValue fxv = new FixedValue();
        fxv.setId(id);
        fxv.setOwnerType(FixedValue.OwnerType.ATTRIBUTE.toString());
        fxv.setOwnerId(owner.getId());
        fxv.setValue(value);
        
        return fxv;
    }
    
    private FixedValue createSaveInput(String value) {
        FixedValue payload = new FixedValue();
        payload.setValue(value);
        
        return payload;
    }
}
