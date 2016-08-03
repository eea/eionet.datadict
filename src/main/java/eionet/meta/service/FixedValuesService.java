package eionet.meta.service;

import eionet.datadict.errors.DuplicateResourceException;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SimpleAttribute;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface FixedValuesService {
    
    FixedValue getFixedValue(DataElement owner, int valueId) throws ResourceNotFoundException;
    
    FixedValue getFixedValue(SimpleAttribute owner, int valueId) throws ResourceNotFoundException;
    
    void saveFixedValue(DataElement owner, FixedValue fixedValue) throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException;
    
    void saveFixedValue(SimpleAttribute owner, FixedValue fixedValue) throws EmptyParameterException, ResourceNotFoundException, DuplicateResourceException;
    
}
