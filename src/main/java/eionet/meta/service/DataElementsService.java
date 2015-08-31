package eionet.meta.service;

import eionet.meta.application.AppContextProvider;
import eionet.meta.application.errors.NotAWorkingCopyException;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.dao.domain.DataElement;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface DataElementsService {

    DataElement getDataElement(int dataElementId) 
            throws ResourceNotFoundException;
    
    DataElement getEditableDataElement(AppContextProvider contextProvider, int dataElementId)
            throws UserAuthenticationException, ResourceNotFoundException, NotAWorkingCopyException, UserAuthorizationException;
    
}
