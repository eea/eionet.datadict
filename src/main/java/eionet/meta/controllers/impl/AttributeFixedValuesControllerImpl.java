package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.controllers.AppContextProvider;
import eionet.util.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
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
    public Attribute getOwnerAttribute(AppContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        int attributeId = this.convertStringIdentifier(ownerAttributeId);
        Attribute ownerAttribute = this.getAttribute(attributeId);
        
        return ownerAttribute;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(AppContextProvider contextProvider, String ownerAttributeId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException {
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        FixedValue value = this.fixedValuesService.getFixedValue(ownerAttribute, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(AppContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException {
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        Collection<FixedValue> fixedValues = this.attributeDao.getFixedValues(ownerAttribute.getId());
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
        
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
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
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, ownerAttribute.getId());
    }
    
    private int convertStringIdentifier(String id) throws MalformedIdentifierException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex) {
            throw new MalformedIdentifierException(id, ex);
        }
    }
    
    private Attribute getAttribute(int attributeId) throws FixedValueOwnerNotFoundException {
        if (!this.attributeDao.exists(attributeId)) {
            throw new FixedValueOwnerNotFoundException(attributeId);
        }
        
        return this.attributeDao.getById(attributeId);
    }
    
}
