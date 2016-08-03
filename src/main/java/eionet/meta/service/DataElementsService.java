package eionet.meta.service;

import eionet.datadict.errors.ConflictException;
import eionet.meta.application.AppContextProvider;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.domain.DataElement;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface DataElementsService {

    DataElement getDataElement(int dataElementId) 
            throws ResourceNotFoundException;
    
    DataElement getEditableDataElement(AppContextProvider contextProvider, int dataElementId)
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException;

}
