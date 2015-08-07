package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.service.FixedValuesService;
import java.util.Collection;
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
    public SimpleAttribute getOwnerAttribute(AppContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        int attributeId = this.convertStringIdentifier(ownerAttributeId);
        SimpleAttribute ownerAttribute = this.getAttribute(attributeId);
        
        return ownerAttribute;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerAttribute, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        Collection<FixedValue> fixedValues = this.attributeDao.getFixedValues(ownerAttribute.getAttributeId());
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }

    @Override
    public void saveFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String originalValue, FixedValue fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                   EmptyValueException, DuplicateResourceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        this.fixedValuesService.saveFixedValue(ownerAttribute, originalValue, fixedValue);
    }

    @Override
    public void deleteFixedValue(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        CompoundDataObject result = this.getSingleValueModel(contextProvider, ownerAttributeId, fixedValue);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.fixedValueDao.deleteById(fxv.getId());
    }
    
    @Override
    public void deleteFixedValues(AppContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        SimpleAttribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, ownerAttribute.getAttributeId());
    }
    
    private int convertStringIdentifier(String id) throws MalformedIdentifierException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex) {
            throw new MalformedIdentifierException(id, ex);
        }
    }
    
    private SimpleAttribute getAttribute(int attributeId) throws FixedValueOwnerNotFoundException {
        SimpleAttribute attr = this.attributeDao.getById(attributeId);
        
        if (attr == null) {
            throw new FixedValueOwnerNotFoundException(attributeId);
        }
        
        return this.attributeDao.getById(attributeId);
    }
    
}
