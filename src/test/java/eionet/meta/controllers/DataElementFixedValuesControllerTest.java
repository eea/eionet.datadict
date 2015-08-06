package eionet.meta.controllers;

import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
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
import static org.mockito.Mockito.when;
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
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, 
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
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
        this.controller.getOwnerDataElement(contextProvider, "a", false);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetOwnerDataElementBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToGetOwnerDataElementBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetOwnerDataElementBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.getOwnerDataElement(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test
    public void testGetAllValuesModel() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException, 
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
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException {
        this.controller.getAllValuesModel(contextProvider, "a", false);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailToGetAllValuesModelBecauseOfAuthentication() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException,
                   DataElementFixedValuesController.FixedValueOwnerNotEditableException, UserAuthorizationException, NotAWorkingCopyException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthenticationException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = FixedValueOwnerNotFoundException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotFound() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = false;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(new ResourceNotFoundException(ownerId));
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = DataElementFixedValuesController.FixedValueOwnerNotEditableException.class)
    public void testFailToGetAllValuesModelBecauseOfOwnerNotEditable() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(NotAWorkingCopyException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailToGetAllValuesModelBecauseOfAuthorization() 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ResourceNotFoundException, 
                   NotAWorkingCopyException, UserAuthorizationException, DataElementFixedValuesController.FixedValueOwnerNotEditableException {
        final int ownerId = 5;
        final boolean edit = true;
        when(dataElementsService.getDataElement(contextProvider, ownerId, !edit)).thenThrow(UserAuthorizationException.class);
        this.controller.getAllValuesModel(contextProvider, Integer.toString(ownerId), edit);
    }
    
    private DataElement createOwner(int id) {
        DataElement owner = new DataElement();
        owner.setId(id);
        
        return owner;
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
