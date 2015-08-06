package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.UserAuthorizationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.DataSet;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.FixedValuesService;
import java.util.Collection;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class DataElementFixedValuesControllerImpl implements DataElementFixedValuesController {
    
    private final FixedValuesService fixedValuesService;
    private final IDataElementDAO dataElementDao;
    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public DataElementFixedValuesControllerImpl(FixedValuesService fixedValuesService, IDataElementDAO dataElementDao, IFixedValueDAO fixedValueDao) {
        this.fixedValuesService = fixedValuesService;
        this.dataElementDao = dataElementDao;
        this.fixedValueDao = fixedValueDao;
    }

    @Override
    public DataElement getOwnerDataElement(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, 
                   FixedValueOwnerNotEditableException, UserAuthorizationException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        int elementId = this.convertStringIdentifier(ownerDataElementId);
        DataElement ownerElement = this.getDataElement(elementId);
        
        if (isEditRequest) {
            this.checkEditability(contextProvider, ownerElement);
        }
        
        return ownerElement;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerElement, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerDataElementId, boolean isEditRequest) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, isEditRequest);
        Collection<FixedValue> fixedValues = this.dataElementDao.getFixedValues(ownerElement.getId());
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }

    @Override
    public void saveFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String originalValue, FixedValue fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, EmptyValueException, DuplicateResourceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        this.fixedValuesService.saveFixedValue(ownerElement, originalValue, fixedValue);
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, String ownerDataElementId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException,
                   FixedValueOwnerNotEditableException, UserAuthorizationException, FixedValueNotFoundException {
        CompoundDataObject result = this.getSingleValueModel(contextProvider, ownerDataElementId, fixedValue, true);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.fixedValueDao.deleteById(fxv.getId());
    }

    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, String ownerDataElementId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueOwnerNotEditableException, 
                   UserAuthorizationException {
        DataElement ownerElement = this.getOwnerDataElement(contextProvider, ownerDataElementId, true);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerElement.getId());
    }
    
    private int convertStringIdentifier(String id) throws MalformedIdentifierException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex) {
            throw new MalformedIdentifierException(id, ex);
        }
    }
    
    private DataElement getDataElement(int elementId) throws FixedValueOwnerNotFoundException {
        if (!this.dataElementDao.dataElementExists(elementId)) {
            throw new FixedValueOwnerNotFoundException(elementId);
        }
        
        return this.dataElementDao.getDataElement(elementId);
    }
    
    private void checkEditability(AppContextProvider contextProvider, DataElement dataElement) 
            throws FixedValueOwnerNotEditableException, UserAuthorizationException {
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
            throw new FixedValueOwnerNotEditableException();
        }
        
        if (!ObjectUtils.equals(workingUser, contextProvider.getUserName())) {
            throw new UserAuthorizationException();
        }
    }
    
}
