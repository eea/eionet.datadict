package eionet.meta.controllers.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.controllers.ControllerContextProvider;
import eionet.meta.controllers.CompoundDataObject;
import eionet.meta.controllers.AttributeFixedValuesController;
import eionet.meta.application.errors.MalformedIdentifierException;
import eionet.meta.application.errors.UserAuthenticationException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.application.errors.fixedvalues.FixedValueOwnerNotFoundException;
import eionet.meta.dao.domain.Attribute;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.IDataService;
import eionet.meta.service.ServiceException;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Controller
public class AttributeFixedValuesControllerImpl implements AttributeFixedValuesController {
    
    private final IDataService dataService;
    
    @Autowired
    public AttributeFixedValuesControllerImpl(IDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Attribute getOwnerAttribute(ControllerContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
        if (!contextProvider.isUserAuthenticated()) {
            throw new UserAuthenticationException();
        }
        
        int attributeId = this.convertStringIdentifier(ownerAttributeId);
        Attribute ownerAttribute = this.getAttribute(attributeId);
        
        return ownerAttribute;
    }
    
    @Override
    public CompoundDataObject getSingleValueModel(ControllerContextProvider contextProvider, String ownerAttributeId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException {
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        FixedValue value = this.getFixedValue(ownerAttribute, fixedValue);
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUE, value);
        
        return result;
    }

    @Override
    public CompoundDataObject getAllValuesModel(ControllerContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        Collection<FixedValue> fixedValues = this.getFixedValues(ownerAttribute.getId());
        CompoundDataObject result = new CompoundDataObject();
        result.put(PROPERTY_OWNER_ATTRIBUTE, ownerAttribute);
        result.put(PROPERTY_FIXED_VALUES, fixedValues);
        
        return result;
    }

    @Override
    public void saveFixedValue(ControllerContextProvider contextProvider, String ownerAttributeId, FixedValue fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, 
                   EmptyValueException, DuplicateResourceException, ServiceException {
        if (fixedValue == null) {
            throw new IllegalArgumentException();
        }
        
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        
        if (StringUtils.isBlank(fixedValue.getValue())) {
            throw new EmptyValueException();
        }
        
        if (fixedValue.getId() == 0) {
            this.insertFixedValue(ownerAttribute, fixedValue);
        }
        else {
            this.updateFixedValue(ownerAttribute, fixedValue);
        }
    }

    @Override
    public void deleteFixedValue(ControllerContextProvider contextProvider, String ownerAttributeId, String fixedValue) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, FixedValueNotFoundException, ServiceException {
        CompoundDataObject result = this.getSingleValueModel(contextProvider, ownerAttributeId, fixedValue);
        FixedValue  fxv = result.get(PROPERTY_FIXED_VALUE);
        this.dataService.deleteFixedValue(fxv);
    }
    
    @Override
    public void deleteFixedValues(ControllerContextProvider contextProvider, String ownerAttributeId) 
            throws UserAuthenticationException, MalformedIdentifierException, FixedValueOwnerNotFoundException, ServiceException {
        Attribute ownerAttribute = this.getOwnerAttribute(contextProvider, ownerAttributeId);
        this.dataService.deleteFixedValues(FixedValue.OwnerType.ATTRIBUTE, ownerAttribute.getId());
    }
    
    private int convertStringIdentifier(String id) throws MalformedIdentifierException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException ex) {
            throw new MalformedIdentifierException(id, ex);
        }
    }
    
    private Attribute getAttribute(int attributeId) throws FixedValueOwnerNotFoundException, ServiceException {
        if (!this.dataService.attributeExists(attributeId)) {
            throw new FixedValueOwnerNotFoundException(attributeId);
        }
        
        return this.dataService.getAttributeById(attributeId);
    }
    
    private FixedValue getFixedValue(Attribute owner, String fixedValue) throws FixedValueNotFoundException, ServiceException {
        FixedValue fxv = this.dataService.getFixedValue(FixedValue.OwnerType.ATTRIBUTE, owner.getId(), fixedValue);
        
        if (fxv == null) {
            throw new FixedValueNotFoundException(fixedValue);
        }
        
        return fxv;
    }
    
    private Collection<FixedValue> getFixedValues(int attributeId) throws ServiceException {
        return this.dataService.getAttributeFixedValues(attributeId);
    }
    
    private void insertFixedValue(Attribute ownerAttribute, FixedValue fixedValue) throws DuplicateResourceException, ServiceException {
        if (this.dataService.fixedValueExistsWithSameNameOwner(FixedValue.OwnerType.ATTRIBUTE, ownerAttribute.getId(), fixedValue.getValue())) {
            throw new DuplicateResourceException(fixedValue.getValue());
        }
        
        fixedValue.setOwnerId(ownerAttribute.getId());
        fixedValue.setOwnerType(FixedValue.OwnerType.ATTRIBUTE.toString());
        this.dataService.createFixedValue(fixedValue);
    }
    
    private void updateFixedValue(Attribute ownerAttribute, FixedValue fixedValue) 
            throws FixedValueNotFoundException, DuplicateResourceException, ServiceException {
        FixedValue fxv = this.dataService.getFixedValueById(fixedValue.getId());
        
        if (fxv == null) {
            throw new FixedValueNotFoundException(fixedValue.getValue());
        }
        
        if (!FixedValue.OwnerType.ATTRIBUTE.isMatch(fxv.getOwnerType()) || fxv.getOwnerId() != ownerAttribute.getId()) {
            throw new IllegalStateException();
        }
        
        FixedValue fxvByValue = this.dataService.getFixedValue(FixedValue.OwnerType.ATTRIBUTE, fxv.getOwnerId(), fixedValue.getValue());
        
        if (fxvByValue != null && fxv.getId() != fxvByValue.getId()) {
            throw new DuplicateResourceException(fxv.getValue());
        }
        
        fxv.setDefinition(fixedValue.getDefinition());
        fxv.setIsDefault(fixedValue.getIsDefault());
        fxv.setShortDescription(fixedValue.getShortDescription());
        fxv.setValue(fixedValue.getValue());
        this.dataService.updateFixedValue(fxv);
    }
}
