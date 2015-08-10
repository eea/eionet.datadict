package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.application.errors.fixedvalues.NotAFixedValueOwnerException;
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
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expected = this.createOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        DataElement actual = this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
        assertEquals(expected, actual);
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetOwnerDataElementBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
        this.controller.getOwnerDataElement(contextProvider, "a", false);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetOwnerDataElementBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetOwnerDataElementBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test
    public void testGetAllValuesModel() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expectedOwner = this.createOwner(ownerId);
        List<FixedValue> expectedValues = new ArrayList<FixedValue>();
        expectedValues.add(this.createFixedValue(12, expectedOwner, "val"));
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        when(dataElementDao.getFixedValues(ownerId)).thenReturn(expectedValues);
        CompoundDataObject result = this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        List<FixedValue> actualValues = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUES);
        assertEquals(expectedValues, actualValues);
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetAllValuesModelBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
        this.controller.getAllValuesModel(contextProvider, "a", false);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetAllValuesModelBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetAllValuesModelBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test
    public void testDeleteFixedValues() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
        verify(fixedValueDao, times(1)).deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerId);
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToDeleteFixedValuesBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
        this.controller.deleteFixedValues(contextProvider, "a");
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToDeleteFixedValuesBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToDeleteFixedValuesBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.deleteFixedValues(contextProvider, Integer.toString(ownerId));
    }
    
    @Test
    public void testGetSingleValueModel() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(23, expectedOwner, value);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, value)).thenReturn(expectedValue);
        CompoundDataObject result = this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), value, edit);
        DataElement actualOwner = result.get(DataElementFixedValuesController.PROPERTY_OWNER_DATA_ELEMENT);
        assertEquals(expectedOwner, actualOwner);
        FixedValue actualValue = result.get(DataElementFixedValuesController.PROPERTY_FIXED_VALUE);
        assertEquals(expectedValue, actualValue);
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToGetSingleValueModelBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        this.controller.getSingleValueModel(contextProvider, "a", "val", false);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetSingleValueModelBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val", edit);
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val", edit);
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val", edit);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetSingleValueModelBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val", edit);
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToGetSingleValueModelBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), "val", edit);
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToGetSingleValueModelBecauseOfValueNotFound()
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, value)).thenThrow(new FixedValueNotFoundException(value));
        this.controller.getSingleValueModel(contextProvider, Integer.toString(ownerId), value, edit);
    }
    
    @Test
    public void testDeleteFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue expectedValue = this.createFixedValue(23, expectedOwner, value);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, value)).thenReturn(expectedValue);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), value);
        verify(fixedValueDao, times(1)).deleteById(expectedValue.getId());
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToDeleteFixedValueBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        this.controller.deleteFixedValue(contextProvider, "a", "val");
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToDeleteFixedValueBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToDeleteFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), "val");
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToDeleteFixedValueBecauseOfValueNotFound()
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, NotAFixedValueOwnerException,
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        when(fixedValuesService.getFixedValue(expectedOwner, value)).thenThrow(new FixedValueNotFoundException(value));
        this.controller.deleteFixedValue(contextProvider, Integer.toString(ownerId), value);
    }
    
    @Test
    public void testSaveFixedValue() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(value);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, inValue);
        verify(fixedValuesService, times(1)).saveFixedValue(expectedOwner, value, inValue);
    }
    
    @Test(expected = MalformedIdentifierException.class)
    public void testFailToSaveFixedValueBecauseOfMalformedId() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, NotAFixedValueOwnerException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException,
                   DuplicateResourceException, EmptyValueException {
        final String value = "val";
        this.controller.saveFixedValue(contextProvider, "a", value, this.createSaveInput(value));
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, this.createSaveInput(value));
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, this.createSaveInput(value));
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToSaveFixedValueBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, this.createSaveInput(value));
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToSaveFixedValueBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, this.createSaveInput(value));
    }
    
    @Test(expected = NotAFixedValueOwnerException.class)
    public void testFailToSaveFixedValueBecauseOfOwnership() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, NotAFixedValueOwnerException,
                   MalformedIdentifierException, FixedValueOwnerNotFoundException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        DataElement expected = this.createNonOwner(ownerId);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expected);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, this.createSaveInput(value));
    }
    
    @Test(expected = FixedValueNotFoundException.class)
    public void testFailToSaveFixedValueBecauseOfValueNotFound()
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String value = "val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(value);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        doThrow(new FixedValueNotFoundException(value)).when(fixedValuesService).saveFixedValue(expectedOwner, value, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), value, inValue);
    }
    
    @Test(expected = DuplicateResourceException.class)
    public void testFailToSaveFixedValueBecauseOfDuplicate()
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String originalValue = "val";
        final String newValue = "other_val";
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(newValue);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        doThrow(new DuplicateResourceException(newValue)).when(fixedValuesService).saveFixedValue(expectedOwner, originalValue, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, inValue);
    }
    
    @Test(expected = EmptyValueException.class)
    public void testFailToSaveFixedValueBecauseOfEmptyValue()
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException,
                   DuplicateResourceException, NotAFixedValueOwnerException, EmptyValueException {
        final int ownerId = 5;
        final boolean edit = true;
        final String originalValue = "val";
        final String newValue = null;
        DataElement expectedOwner = this.createOwner(ownerId);
        FixedValue inValue = this.createSaveInput(newValue);
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenReturn(expectedOwner);
        doThrow(EmptyValueException.class).when(fixedValuesService).saveFixedValue(expectedOwner, originalValue, inValue);
        this.controller.saveFixedValue(contextProvider, Integer.toString(ownerId), originalValue, inValue);
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
    
    private FixedValue createSaveInput(String value) {
        FixedValue payload = new FixedValue();
        payload.setValue(value);
        
        return payload;
    }
    
}