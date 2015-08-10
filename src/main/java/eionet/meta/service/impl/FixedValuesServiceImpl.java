package eionet.meta.service.impl;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
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
    public FixedValue getFixedValue(DataElement owner, String value) throws FixedValueNotFoundException {
        return this.getFixedValue(FixedValue.OwnerType.DATA_ELEMENT, owner.getId(), value);
    }

    @Override
    public FixedValue getFixedValue(SimpleAttribute owner, String value) throws FixedValueNotFoundException {
        return this.getFixedValue(FixedValue.OwnerType.ATTRIBUTE, owner.getAttributeId(), value);
    }
    
    private FixedValue getFixedValue(FixedValue.OwnerType ownerType, int ownerId, String value) throws FixedValueNotFoundException {
        FixedValue fxv = this.fixedValueDao.getByValue(ownerType, ownerId, value);
        
        if (fxv == null) {
            throw new FixedValueNotFoundException(value);
        }
        
        return fxv;
    }

    @Override
    @Transactional
    public void saveFixedValue(DataElement owner, String originalValue, FixedValue fixedValue) throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        fixedValue.setDefaultValue(false);
        this.saveFixedValue(FixedValue.OwnerType.DATA_ELEMENT, owner.getId(), originalValue, fixedValue);
    }

    @Override
    @Transactional
    public void saveFixedValue(SimpleAttribute owner, String originalValue, FixedValue fixedValue) throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        this.saveFixedValue(FixedValue.OwnerType.ATTRIBUTE, owner.getAttributeId(), originalValue, fixedValue);
    }
    
    private void saveFixedValue(FixedValue.OwnerType ownerType, int ownerId, String originalValue, FixedValue fixedValue) 
            throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException {
        if (StringUtils.isBlank(fixedValue.getValue())) {
            throw new EmptyValueException();
        }
        
        if (StringUtils.isBlank(originalValue)) {
            this.insertFixedValue(ownerType, ownerId, fixedValue);
        }
        else {
            this.updateFixedValue(ownerType, ownerId, originalValue, fixedValue);
        }
    }
    
    private void insertFixedValue(FixedValue.OwnerType ownerType, int ownerId, FixedValue fixedValue) throws DuplicateResourceException {
        if (this.fixedValueDao.exists(ownerType, ownerId, fixedValue.getValue())) {
            throw new DuplicateResourceException(fixedValue.getValue());
        }
        
        FixedValue toInsert = new FixedValue();
        this.prepareFixedValueForUpdate(ownerType, ownerId, fixedValue, toInsert);
        this.fixedValueDao.create(toInsert);
        this.updateDefaultStatus(toInsert);
    }
    
    private void updateFixedValue(FixedValue.OwnerType ownerType, int ownerId, String originalValue, FixedValue fixedValue) 
            throws FixedValueNotFoundException, DuplicateResourceException {
        FixedValue toUpdate = this.fixedValueDao.getByValue(ownerType, ownerId, originalValue);
        
        if (toUpdate == null) {
            throw new FixedValueNotFoundException(originalValue);
        }
        
        if (!fixedValue.getValue().equals(toUpdate.getValue())) {
            FixedValue fxvByValue = this.fixedValueDao.getByValue(ownerType, ownerId, fixedValue.getValue());

            if (fxvByValue != null) {
                throw new DuplicateResourceException(fixedValue.getValue());
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
