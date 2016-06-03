package eionet.datadict.services.data;

import eionet.datadict.model.Attribute;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataDictEntity;
import eionet.meta.dao.domain.FixedValue;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aliki Kopaneli
 */
public interface AttributeDataService {

    Attribute getAttribute(int id) throws ResourceNotFoundException;
    
    boolean exists(int id);
    
    int createAttribute(Attribute attribute);
    
    void updateAttribute(Attribute attribute);
    
    void deleteAttributeById(int attributeId);
    
    void deleteVocabularyBinding(int attributeId);
    
    void deleteRelatedFixedValues(int attributeId);
    
    int countAttributeValues(int attributeId);
    
    List<FixedValue> getFixedValues(int attributeId);
    
    /**
     * Fetches information about all DD concepts for which the attribute of a given id has attribute values.
     * 
     * @param attributeId the id of the attribute.
     * @return a {@link java.util.Map} containing {@link DataDictEntity$Entity} keys and Integer values.
     */
    Map<DataDictEntity.Entity, Integer> getDistinctTypesWithAttributeValues(int attributeId);
    
    Attribute setNewVocabularyToAttributeObject(Attribute attribute, int vocabularyId) throws ResourceNotFoundException;
    
}
