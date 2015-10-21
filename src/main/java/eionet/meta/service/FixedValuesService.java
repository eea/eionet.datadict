package eionet.meta.service;

import eionet.meta.application.errors.DuplicateResourceException;
import eionet.meta.application.errors.fixedvalues.EmptyValueException;
import eionet.meta.application.errors.fixedvalues.FixedValueNotFoundException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface FixedValuesService {
    
    FixedValue getFixedValue(DataElement owner, int valueId) throws FixedValueNotFoundException;
    
    FixedValue getFixedValue(SimpleAttribute owner, int valueId) throws FixedValueNotFoundException;
    
    void saveFixedValue(DataElement owner, FixedValue fixedValue) throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException;
    
    void saveFixedValue(SimpleAttribute owner, FixedValue fixedValue) throws EmptyValueException, FixedValueNotFoundException, DuplicateResourceException;
    
}
