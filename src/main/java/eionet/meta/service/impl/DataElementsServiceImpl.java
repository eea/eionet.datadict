package eionet.meta.service.impl;

import eionet.datadict.errors.ConflictException;
import eionet.meta.application.AppContextProvider;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.service.DataElementsService;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public DataElement getDataElement(int elementId) throws ResourceNotFoundException {
        if (!this.dataElementDao.dataElementExists(elementId)) {
            String msg = String.format("Data element with internal id %d was not found.", elementId);
            throw new ResourceNotFoundException(msg);
        }
        
        return this.dataElementDao.getDataElement(elementId);
    }
    
    @Override
    public DataElement getEditableDataElement(AppContextProvider contextProvider, int dataElementId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        DataElement ownerElement = this.getDataElement(dataElementId);
        this.checkEditability(contextProvider, ownerElement);
        
        return ownerElement;
    }

    private void checkEditability(AppContextProvider contextProvider, DataElement dataElement)
            throws ConflictException, UserAuthorizationException {
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
            String msg = String.format("Data element with internal id %d is not a working copy.", dataElement.getId());
            throw new ConflictException(msg);
        }
        
        if (!ObjectUtils.equals(workingUser, contextProvider.getUserName())) {
            throw new UserAuthorizationException();
        }
    }

    @Override
    @Transactional
    public void deleteVocabularyConceptDataElementValues(int vocabularyConceptId, int dataElementId) {
        dataElementDao.deleteVocabularyConceptDataElementValues(vocabularyConceptId, dataElementId);
    }
    
}
