package eionet.meta.controllers;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.meta.application.AppContextProvider;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.controllers.impl.DataElementFixedValuesControllerImpl;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.DataElementsService;
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
public class DataElementFixedValuesControllerTest {

    private DataElementFixedValuesController controller;
    
    @Mock
    private DataElementsService dataElementsService;
    
    @Mock
    private FixedValuesService fixedValuesService;
    
    @Mock
    private IDataElementDAO dataElementDao;
    
    @Mock
    private IFixedValueDAO fixedValueDao;
    
    @Mock
    private AppContextProvider contextProvider;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.controller = new DataElementFixedValuesControllerImpl(dataElementsService, fixedValuesService, dataElementDao, fixedValueDao);
    }
    
    @Test
    public void testGetOwnerDataElement() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createOwner(ownerId);
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expected);
        DataElement actual = this.controller.getOwnerDataElement(ownerId);
        assertEquals(expected, actual);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnerNotFound() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(dataElementsService.getDataElement(ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getOwnerDataElement(ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnership() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expected);
        this.controller.getOwnerDataElement(ownerId);
    }
    
    @Test
    public void testGetEditableOwnerDataElement() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        DataElement actual = this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
        assertEquals(expected, actual);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetOwnerDataElementBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableOwnerDataElementBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableOwnerDataElementBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetEditableOwnerDataElementBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableOwnerDataElementBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.getEditableOwnerDataElement(contextProvider, ownerId);
    }
    
    @Test
    public void testGetAllValuesModel() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        DataElement expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expectedOwner);
        when(dataElementDao.getFixedValues(ownerId, false)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getAllValuesModel(ownerId);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotFound() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(dataElementsService.getDataElement(ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getAllValuesModel(ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnership() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expected);
        this.controller.getAllValuesModel(ownerId);
    }
    
    @Test
    public void testGetEditableAllValuesModel() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        when(dataElementDao.getFixedValues(ownerId, false)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getEditableAllValuesModel(contextProvider, ownerId);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableAllValuesModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.getEditableAllValuesModel(contextProvider, ownerId);
    }
    
    @Test
    public void testDeleteFixedValues() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        this.controller.deleteFixedValues(contextProvider, ownerId);
        verify(fixedValueDao, times(1)).deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerId);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.deleteFixedValues(contextProvider, ownerId);
    }
    
    @Test
    public void testGetSingleValueModel() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(23, expectedOwner, "val");
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, valueId)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getSingleValueModel(ownerId, valueId);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnerNotFound() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        when(dataElementsService.getDataElement(ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getSingleValueModel(ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnership() 
            throws ResourceNotFoundException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(ownerId)).thenReturn(expected);
        this.controller.getSingleValueModel(ownerId, 10);
    }
    
    @Test
    public void testGetEditableSingleValueModel() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(23, expectedOwner, "val");
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, valueId)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getEditableSingleValueModel(contextProvider, ownerId, valueId);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToGetEditableSingleValueModelBecauseOfValueNotFound()
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, valueId)).thenThrow(ResourceNotFoundException.class);
        this.controller.getEditableSingleValueModel(contextProvider, ownerId, valueId);
    }
    
    @Test
    public void testDeleteFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(23, expectedOwner, "val");
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, valueId)).thenReturn(expectedValue);
        this.controller.deleteFixedValue(contextProvider, ownerId, valueId);
        verify(fixedValueDao, times(1)).deleteById(expectedValue.getId());
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.deleteFixedValue(contextProvider, ownerId, 10);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfValueNotFound()
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, valueId)).thenThrow(ResourceNotFoundException.class);
        this.controller.deleteFixedValue(contextProvider, ownerId, valueId);
    }
    
    @Test
    public void testSaveFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException,
                   DuplicateResourceException, EmptyParameterException {
        final int ownerId = 5;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(10, "val");
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
        verify(fixedValuesService, times(1)).saveFixedValue(expectedOwner, inValue);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, 
                    DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthenticationException.class);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ResourceNotFoundException.class);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(ConflictException.class);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenThrow(UserAuthorizationException.class);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ConflictException.class)
    public void testFailToSaveFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, ConflictException,
                   DuplicateResourceException, EmptyParameterException {
        final int ownerId = 5;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expected);
        this.controller.saveFixedValue(contextProvider, ownerId, this.createSaveInput(10, "val"));
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfValueNotFound()
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        final int valueId = 10;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(valueId, "val");
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        doThrow(ResourceNotFoundException.class).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToSaveFixedValueBecauseOfDuplicate()
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        final int valueId = 10;
        final String newValue = "other_val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(valueId, newValue);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        doThrow(new DuplicateResourceException(newValue)).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    @Test(expected = EmptyParameterException.class)
    public void testFailToSaveFixedValueBecauseOfEmptyValue()
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException,
                   DuplicateResourceException, ConflictException, EmptyParameterException {
        final int ownerId = 5;
        final int valueId = 10;
        final String newValue = null;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(valueId, newValue);
        when(dataElementsService.getEditableDataElement(contextProvider, ownerId)).thenReturn(expectedOwner);
        doThrow(EmptyParameterException.class).when(fixedValuesService).saveFixedValue(expectedOwner, inValue);
        this.controller.saveFixedValue(contextProvider, ownerId, inValue);
    }
    
    private DataElement createOwner(int id) {
        DataElement owner = new DataElement();
        owner.setId(id);
        owner.setType(DataElement.DataElementValueType.FIXED.toString());
        
        return owner;
    }
    
    private DataElement createNonOwner(int id) {
        DataElement nonOwner = new DataElement();
        nonOwner.setId(id);
        nonOwner.setType(DataElement.DataElementValueType.VOCABULARY.toString());
        
        return nonOwner;
    }
    
    private FixedValue createFixedValue(int id, DataElement owner, String value) {
        FixedValue fxv = new FixedValue();
        fxv.setId(id);
        fxv.setOwnerType(FixedValue.OwnerType.DATA_ELEMENT.toString());
        fxv.setOwnerId(owner.getId());
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
