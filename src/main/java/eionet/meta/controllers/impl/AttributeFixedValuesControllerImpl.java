package eionet.meta.controllers.impl;

import eionet.datadict.errors.ConflictException;
import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.datadict.errors.UserAuthenticationException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.service.FixedValuesService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class AttributeFixedValuesControllerImpl implements AttributeFixedValuesController {

    private final FixedValuesService fixedValuesService;
    private final IAttributeDAO attributeDao;
    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public AttributeFixedValuesControllerImpl(FixedValuesService fixedValuesService, IAttributeDAO attributeDao, IFixedValueDAO fixedValueDao) {
        this.fixedValuesService = fixedValuesService;
        this.attributeDao = attributeDao;
        this.fixedValueDao = fixedValueDao;
    }

    @Override
    public SimpleAttribute getOwnerAttribute(int ownerAttributeId) throws ResourceNotFoundException, ConflictException {
        SimpleAttribute attr = this.attributeDao.getById(ownerAttributeId);
        
        if (attr == null) {
            String msg = String.format("Attribute with internal id %d was not found.", ownerAttributeId);
            throw new ResourceNotFoundException(msg);
        }
        
        if (!SimpleAttribute.DisplayType.SELECT_BOX.isMatch(attr.getInputType())) {
            String msg = String.format("Attibute with internal id %d does not support fixed values.", ownerAttributeId);
            throw new ConflictException(msg);
        }
        
        return this.attributeDao.getById(ownerAttributeId);
    }
    
    @Override
    public SimpleAttribute getEditableOwnerAttribute(AppContextProvider contextProvider, int ownerAttributeId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(ownerAttributeId);
        
        return ownerAttribute;
    }

    @Override
    public CompoundDataObject getSingleValueModel(int ownerAttributeId, int fixedValueId) 
            throws ResourceNotFoundException, ConflictException {
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(ownerAttributeId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerAttribute, fixedValueId);
        
        return this.packageResults(ownerAttribute, value);
    }
    
    @Override
    public CompoundDataObject getEditableSingleValueModel(AppContextProvider contextProvider, int ownerAttributeId, int fixedValueId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        SimpleAttribute ownerAttribute = this.getEditableOwnerAttribute(contextProvider, ownerAttributeId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerAttribute, fixedValueId);
        
        return this.packageResults(ownerAttribute, value);
    }

    @Override
    public CompoundDataObject getAllValuesModel(int ownerAttributeId) throws ResourceNotFoundException, ConflictException {
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(ownerAttributeId);
        List<FixedValue> fixedValues = this.attributeDao.getFixedValues(ownerAttribute.getAttributeId());
        
        return this.packageResults(ownerAttribute, fixedValues);
    }
    
    @Override
    public CompoundDataObject getEditableAllValuesModel(AppContextProvider contextProvider, int ownerAttributeId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        SimpleAttribute ownerAttribute = this.getEditableOwnerAttribute(contextProvider, ownerAttributeId);
        List<FixedValue> fixedValues = this.attributeDao.getFixedValues(ownerAttribute.getAttributeId());
        
        return this.packageResults(ownerAttribute, fixedValues);
    }

    @Override
    public void saveFixedValue(AppContextProvider contextProvider, int ownerAttributeId, FixedValue fixedValue) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException, EmptyParameterException, DuplicateResourceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        SimpleAttribute ownerAttribute = this.getEditableOwnerAttribute(contextProvider, ownerAttributeId);
        this.fixedValuesService.saveFixedValue(ownerAttribute, fixedValue);
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, int ownerAttributeId, int fixedValueId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        CompoundDataObject result = this.getEditableSingleValueModel(contextProvider, ownerAttributeId, fixedValueId);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.fixedValueDao.deleteById(fxv.getId());
    }
    
    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, int ownerAttributeId) 
            throws UserAuthenticationException, ResourceNotFoundException, ConflictException {
        SimpleAttribute ownerAttribute = this.getEditableOwnerAttribute(contextProvider, ownerAttributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, ownerAttribute.getAttributeId());
    }
    
    private CompoundDataObject packageResults(SimpleAttribute ownerAttribute, FixedValue value) {
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }
    
    private CompoundDataObject packageResults(SimpleAttribute ownerAttribute, List<FixedValue> fixedValues) {
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }
    
}
