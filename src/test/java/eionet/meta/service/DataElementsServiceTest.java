package eionet.meta.service;

import eionet.meta.application.AppContextProvider;
import eionet.datadict.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.impl.DataElementsServiceImpl;
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
public class DataElementsServiceTest {

    private DataElementsService service;
    
    @Mock
    private IDataElementDAO dataElementDao;
    
    @Mock
    private AppContextProvider contextProvider;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.service = new DataElementsServiceImpl(dataElementDao);
    }
    
    @Test
    public void testGetDataElement() throws ResourceNotFoundException {
        final int elmId = 5;
        DataElement expected = this.createDataElement(elmId);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        DataElement actual = this.service.getDataElement(elmId);
        assertEquals(expected, actual);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailGetDataElementBecauseOfNotFound() throws ResourceNotFoundException {
        final int elmId = 5;
        when(dataElementDao.dataElementExists(elmId)).thenReturn(false);
        this.service.getDataElement(elmId);
    }
    
    @Test
    public void testGetWorkingCommonDataElement() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        final String user = "user";
        DataElement expected = this.createWorkingCommon(elmId, user);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(contextProvider.getUserName()).thenReturn(user);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        DataElement actual = this.service.getEditableDataElement(contextProvider, elmId);
        assertEquals(expected, actual);
        verify(dataElementDao, times(0)).getParentDataSet(elmId);
    }
    
    @Test
    public void testGetWorkingNonCommonDataElement() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        final String user = "user";
        DataElement expected = this.createNonCommon(elmId);
        DataSet parentDataSet = this.createWorkingDataSet(20, user);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(contextProvider.getUserName()).thenReturn(user);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        when(dataElementDao.getParentDataSet(elmId)).thenReturn(parentDataSet);
        DataElement actual = this.service.getEditableDataElement(contextProvider, elmId);
        assertEquals(expected, actual);
        verify(dataElementDao, times(1)).getParentDataSet(elmId);
    }
    
    @Test(expected = UserAuthenticationException.class)
    public void testFailGetEditableDataElementBecauseOfAuthentication() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        when(contextProvider.isUserAuthenticated()).thenReturn(false);
        this.service.getEditableDataElement(contextProvider, 5);
    }
    
    @Test(expected = ResourceNotFoundException.class)
    public void testFailGetEditableDataElementBecauseOfNotFound() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(false);
        this.service.getDataElement(elmId);
    }
    
    @Test(expected = NotAWorkingCopyException.class)
    public void testFailGetCommonDataElementBecauseOfNonWorking() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        DataElement expected = this.createCommon(elmId);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        this.service.getEditableDataElement(contextProvider, elmId);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailGetCommonDataElementBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        DataElement expected = this.createWorkingCommon(elmId, "workuser");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(contextProvider.getUserName()).thenReturn("user");
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        this.service.getEditableDataElement(contextProvider, elmId);
    }
    
    @Test(expected = NotAWorkingCopyException.class)
    public void testFailGetNonCommonDataElementBecauseOfNonWorking() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        DataElement expected = this.createNonCommon(elmId);
        DataSet parentDataSet = this.createDataSet(20);
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        when(dataElementDao.getParentDataSet(elmId)).thenReturn(parentDataSet);
        this.service.getEditableDataElement(contextProvider, elmId);
    }
    
    @Test(expected = UserAuthorizationException.class)
    public void testFailGetNonCommonDataElementBecauseOfAuthorization() 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        final int elmId = 5;
        DataElement expected = this.createNonCommon(elmId);
        DataSet parentDataSet = this.createWorkingDataSet(20, "workuser");
        when(contextProvider.isUserAuthenticated()).thenReturn(true);
        when(contextProvider.getUserName()).thenReturn("user");
        when(dataElementDao.dataElementExists(elmId)).thenReturn(true);
        when(dataElementDao.getDataElement(elmId)).thenReturn(expected);
        when(dataElementDao.getParentDataSet(elmId)).thenReturn(parentDataSet);
        this.service.getEditableDataElement(contextProvider, elmId);
    }
    
    private DataElement createDataElement(int id) {
        DataElement elm = new DataElement();
        elm.setId(id);
        
        return elm;
    }
    
    private DataElement createCommon(int id) {
        return this.createDataElement(id);
    }
    
    private DataElement createNonCommon(int id) {
        DataElement elm = this.createDataElement(id);
        elm.setParentNamespace(19);
        
        return elm;
    }
    
    private DataElement createWorkingCommon(int id, String workingUser) {
        DataElement elm = this.createCommon(id);
        elm.setWorkingCopy(true);
        elm.setWorkingUser(workingUser);
        
        return elm;
    }
    
    private DataSet createDataSet(int id) {
        DataSet ds = new DataSet();
        ds.setId(id);
        
        return ds;
    }
    
    private DataSet createWorkingDataSet(int id, String workingUser) {
        DataSet ds = this.createDataSet(id);
        ds.setWorkingCopy(true);
        ds.setWorkingUser(workingUser);
        
        return ds;
    }
}
