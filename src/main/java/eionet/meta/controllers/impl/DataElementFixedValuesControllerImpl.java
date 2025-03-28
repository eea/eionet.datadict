package eionet.meta.controllers.impl;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.DataElementFixedValuesController;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.datadict.errors.UserAuthorizationException;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.DataElementsService;
import eionet.meta.service.FixedValuesService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class DataElementFixedValuesControllerImpl implements DataElementFixedValuesController {
    
    private final DataElementsService dataElementsService;
    private final FixedValuesService fixedValuesService;
    private final IDataElementDAO dataElementDao;
    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public DataElementFixedValuesControllerImpl(
            DataElementsService dataElementsService, FixedValuesService fixedValuesService, 
            IDataElementDAO dataElementDao, IFixedValueDAO fixedValueDao) {
        this.dataElementsService = dataElementsService;
        this.fixedValuesService = fixedValuesService;
        this.dataElementDao = dataElementDao;
        this.fixedValueDao = fixedValueDao;
    }

    @Override
    public DataElement getOwnerDataElement(int ownerDataElementId) 
            throws ResourceNotFoundException, ConflictException {
        DataElement owner = this.dataElementsService.getDataElement(ownerDataElementId);
        this.ensureFixedValueOwnership(owner);
        
        return owner;
    }

    @Override
    public DataElement getEditableOwnerDataElement(AppContextProvider contextProvider, int ownerDataElementId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        DataElement owner = this.dataElementsService.getEditableDataElement(contextProvider, ownerDataElementId);
        this.ensureFixedValueOwnership(owner);
        
        return owner;
    }

    @Override
    public CompoundDataObject getSingleValueModel(int ownerDataElementId, int fixedValueId) 
            throws ResourceNotFoundException, ConflictException {
        DataElement ownerElement = this.getOwnerDataElement(ownerDataElementId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerElement, fixedValueId);
        
        return this.packageDataResult(ownerElement, value);
    }

    @Override
    public CompoundDataObject getEditableSingleValueModel(AppContextProvider contextProvider, int ownerDataElementId, int fixedValueId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        DataElement ownerElement = this.getEditableOwnerDataElement(contextProvider, ownerDataElementId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerElement, fixedValueId);
        
        return this.packageDataResult(ownerElement, value);
    }

    @Override
    public CompoundDataObject getAllValuesModel(int ownerDataElementId) 
            throws ResourceNotFoundException, ConflictException {
        DataElement ownerElement = this.getOwnerDataElement(ownerDataElementId);
        List<FixedValue> fixedValues = this.dataElementDao.getFixedValues(ownerElement.getId(), false);
        
        return this.packageDataResult(ownerElement, fixedValues);
    }

    @Override
    public CompoundDataObject getEditableAllValuesModel(AppContextProvider contextProvider, int ownerDataElementId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        DataElement ownerElement = this.getEditableOwnerDataElement(contextProvider, ownerDataElementId);
        List<FixedValue> fixedValues = this.dataElementDao.getFixedValues(ownerElement.getId(), false);
        
        return this.packageDataResult(ownerElement, fixedValues);
    }
    
    @Override
    public void saveFixedValue(AppContextProvider contextProvider, int ownerDataElementId, FixedValue fixedValue) 
            throws UserAuthenticationException, ResourceNotFoundException, UserAuthorizationException, 
                   ConflictException, DuplicateResourceException, EmptyParameterException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        DataElement ownerElement = this.getEditableOwnerDataElement(contextProvider, ownerDataElementId);
        this.fixedValuesService.saveFixedValue(ownerElement, fixedValue);
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, int ownerDataElementId, int fixedValueId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        CompoundDataObject result = this.getEditableSingleValueModel(contextProvider, ownerDataElementId, fixedValueId);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.fixedValueDao.deleteById(fxv.getId());
    }

    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, int ownerDataElementId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, UserAuthorizationException {
        DataElement ownerElement = this.getEditableOwnerDataElement(contextProvider, ownerDataElementId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.DATA_ELEMENT, ownerElement.getId());
    }
    
    private void ensureFixedValueOwnership(DataElement owner) throws ConflictException {
        DataElement.DataElementValueType valueType = DataElement.DataElementValueType.parse(owner.getType());
        
        if (valueType != DataElement.DataElementValueType.FIXED && valueType != DataElement.DataElementValueType.QUANTITIVE) {
            String msg = String.format("Data elemenent with internal id %d does not support fixed values.", owner.getId());
            throw new ConflictException(msg);
        }
    }
    
    private CompoundDataObject packageDataResult(DataElement ownerElement, FixedValue value) {
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }
    
    private CompoundDataObject packageDataResult(DataElement ownerElement, List<FixedValue> fixedValues) {
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_DATA_ELEMENT, ownerElement);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }
    
}
