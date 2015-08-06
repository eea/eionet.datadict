package eionet.meta.service.impl;

import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.DataElementsService;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class DataElementsServiceImpl implements DataElementsService {

    private final IDataElementDAO dataElementDao;
    
    @Autowired
    public DataElementsServiceImpl(IDataElementDAO dataElementDao) {
        this.dataElementDao = dataElementDao;
    }
    
    @Override
    public DataElement getDataElement(AppContextProvider contextProvider, int dataElementId, boolean readOnly) 
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        DataElement ownerElement = this.getDataElement(dataElementId);
        
        if (!readOnly) {
            this.checkEditability(contextProvider, ownerElement);
        }
        
        return ownerElement;
    }
    
    private DataElement getDataElement(int elementId) throws ResourceNotFoundException {
        if (!this.dataElementDao.dataElementExists(elementId)) {
            throw new ResourceNotFoundException(elementId);
        }
        
        return this.dataElementDao.getDataElement(elementId);
    }
    
    private void checkEditability(AppContextProvider contextProvider, DataElement dataElement) 
            throws NotAWorkingCopyException, UserAuthorizationException {
        boolean workingCopy;
        String workingUser;
        
        if (dataElement.isCommonElement()) {
            workingCopy = dataElement.isWorkingCopy();
            workingUser = dataElement.getWorkingUser();
        }
        else {
            DataSet parentDataSet = this.dataElementDao.getParentDataSet(dataElement.getId());
            
            if (parentDataSet == null) {
                throw new IllegalStateException();
            }
            
            workingCopy = parentDataSet.isWorkingCopy();
            workingUser = parentDataSet.getWorkingUser();
        }
        
        if (!workingCopy) {
            throw new NotAWorkingCopyException();
        }
        
        if (!ObjectUtils.equals(workingUser, contextProvider.getUserName())) {
            throw new UserAuthorizationException();
        }
    }
    
}
