package eionet.meta.service.impl;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;
import eionet.meta.service.FixedValuesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class FixedValuesServiceImpl implements FixedValuesService {

    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public FixedValuesServiceImpl(IFixedValueDAO fixedValueDao) {
        this.fixedValueDao = fixedValueDao;
    }

    @Override
    public FixedValue getFixedValue(DataElement owner, int valueId) throws ResourceNotFoundException {
        return this.getFixedValue(FixedValue.OwnerType.DATA_ELEMENT, owner.getId(), valueId);
    }

    @Override
    public FixedValue getFixedValue(SimpleAttribute owner, int valueId) throws ResourceNotFoundException {
        return this.getFixedValue(FixedValue.OwnerType.ATTRIBUTE, owner.getAttributeId(), valueId);
    }
    
    private FixedValue getFixedValue(FixedValue.OwnerType ownerType, int ownerId, int valueId) throws ResourceNotFoundException {
        FixedValue fxv = this.fixedValueDao.getById(valueId);
        
        if (fxv == null || ownerId != fxv.getOwnerId() || !ownerType.isMatch(fxv.getOwnerType())) {
            String msg = String.format("Fixed value with internal id %d was not found for owner of type %s with internal id %d.", 
                    valueId, ownerType.toString(), ownerId);
            throw new ResourceNotFoundException(msg);
        }
        
        return fxv;
    }

    @Override
    @Transactional
    public void saveFixedValue(DataElement owner, FixedValue fixedValue) throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException {
        fixedValue.setDefaultValue(false);
        this.saveFixedValue(FixedValue.OwnerType.DATA_ELEMENT, owner.getId(), fixedValue);
    }

    @Override
    @Transactional
    public void saveFixedValue(SimpleAttribute owner, FixedValue fixedValue) throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException {
        this.saveFixedValue(FixedValue.OwnerType.ATTRIBUTE, owner.getAttributeId(), fixedValue);
    }
    
    private void saveFixedValue(FixedValue.OwnerType ownerType, int ownerId, FixedValue fixedValue) 
            throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException {
        if (StringUtils.isBlank(fixedValue.getValue())) {
            throw new EmptyParameterException("value");
        }
        
        if (fixedValue.getId() == 0) {
            this.insertFixedValue(ownerType, ownerId, fixedValue);
        }
        else {
            this.updateFixedValue(ownerType, ownerId, fixedValue);
        }
    }
    
    private void insertFixedValue(FixedValue.OwnerType ownerType, int ownerId, FixedValue fixedValue) throws DuplicateResourceException {
        if (this.fixedValueDao.exists(ownerType, ownerId, fixedValue.getValue())) {
            String message = String.format("Fixed value %s already exists for owner type %s with internal id %d.", 
                    fixedValue.getValue(), ownerType.toString(), ownerId);
            throw new DuplicateResourceException(message);
        }
        
        FixedValue toInsert = new FixedValue();
        this.prepareFixedValueForUpdate(ownerType, ownerId, fixedValue, toInsert);
        this.fixedValueDao.create(toInsert);
        this.updateDefaultStatus(toInsert);
    }
    
    private void updateFixedValue(FixedValue.OwnerType ownerType, int ownerId, FixedValue fixedValue) 
            throws ResourceNotFoundException, DuplicateResourceException {
        FixedValue toUpdate = this.getFixedValue(ownerType, ownerId, fixedValue.getId());
        
        if (!fixedValue.getValue().equals(toUpdate.getValue())) {
            FixedValue fxvByValue = this.fixedValueDao.getByValue(ownerType, ownerId, fixedValue.getValue());

            if (fxvByValue != null) {
                String message = String.format("Fixed value %s already exists for owner type %s with internal id %d.", 
                    fixedValue.getValue(), ownerType.toString(), ownerId);
                throw new DuplicateResourceException(message);
            }
        }
        
        this.prepareFixedValueForUpdate(ownerType, ownerId, fixedValue, toUpdate);
        this.fixedValueDao.update(toUpdate);
        this.updateDefaultStatus(toUpdate);
    }
    
    private void prepareFixedValueForUpdate(FixedValue.OwnerType ownerType, int ownerId, FixedValue inValue, FixedValue outValue) {
        outValue.setOwnerId(ownerId);
        outValue.setOwnerType(ownerType.toString());
        outValue.setValue(inValue.getValue());
        outValue.setShortDescription(inValue.getShortDescription());
        outValue.setDefinition(inValue.getDefinition());
        outValue.setDefaultValue(inValue.isDefaultValue());
    }
    
    private void updateDefaultStatus(FixedValue fixedValue) {
        if (!fixedValue.isDefaultValue()) {
            return;
        }
        
        FixedValue.OwnerType ownerType = FixedValue.OwnerType.parse(fixedValue.getOwnerType());
        this.fixedValueDao.updateDefaultValue(ownerType, fixedValue.getOwnerId(), fixedValue.getValue());
    }
    
}
