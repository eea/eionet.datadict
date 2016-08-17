package eionet.datadict.services.impl.data;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.VocabularyDAO;
import eionet.datadict.model.Attribute;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataDictEntity;
import eionet.meta.dao.IFixedValueDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.VocabularyFolder;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Aliki Kopaneli
 */
@Service
public class AttributeDataServiceImpl implements AttributeDataService {

    private final AttributeDao attributeDao;
    private final VocabularyDAO vocabularyDao;
    private final IFixedValueDAO fixedValueDao;
    
    @Autowired
    public AttributeDataServiceImpl(AttributeDao attributeDao, VocabularyDAO vocabularyDao, IFixedValueDAO fixedValueDao) {
        this.attributeDao = attributeDao;
        this.vocabularyDao = vocabularyDao;
        this.fixedValueDao = fixedValueDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Attribute getAttribute(int id) throws ResourceNotFoundException {
        Attribute attribute = attributeDao.getById(id);
        
        if (attribute == null) {
            throw new ResourceNotFoundException("Attribute with id: "+id+" does not exist.");
        }
        
        if (attribute.getVocabulary() != null) {
            VocabularyFolder voc = attribute.getVocabulary();
            voc = vocabularyDao.getPlainVocabularyById(voc.getId());
            attribute.setVocabulary(voc);
        }
        
        return attribute;
    }
    
    @Override
    public boolean exists(int id) {
        return this.attributeDao.exists(id);
    }
    
    @Override
    public int createAttribute(Attribute attribute) {
        return this.attributeDao.create(attribute);
    }

    @Override
    @Transactional
    public void updateAttribute(Attribute attribute) {
        this.attributeDao.update(attribute);
        if (attribute.getVocabulary() != null) {
            this.attributeDao.updateVocabularyBinding(attribute.getId(), attribute.getVocabulary().getId());
        }
        else {
            this.attributeDao.deleteVocabularyBinding(attribute.getId());
        }
    }
    
    @Override
    @Transactional
    public void deleteAttributeById(int attributeId) {
        this.attributeDao.deleteValues(attributeId);
        this.attributeDao.deleteVocabularyBinding(attributeId);
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, attributeId);
        this.attributeDao.delete(attributeId);
    }
    
    @Override
    public int countAttributeValues(int attributeId) {
        return this.attributeDao.countAttributeValues(attributeId);
    }
     
    @Override
    public Attribute setNewVocabularyToAttributeObject(Attribute attribute, int vocabularyId) throws ResourceNotFoundException {
        attribute.setVocabulary(this.vocabularyDao.getPlainVocabularyById(vocabularyId));
        return attribute;
    }

    @Override
    public Map<DataDictEntity.Entity, Integer> getDistinctTypesWithAttributeValues(int attributeId) {
        return this.attributeDao.getConceptsWithAttributeValues(attributeId);
    }
    
    @Override
    public void deleteVocabularyBinding(int attributeId){
        this.attributeDao.deleteVocabularyBinding(attributeId);
    }

    @Override
    public void deleteRelatedFixedValues(int attributeId) {
        this.fixedValueDao.deleteAll(FixedValue.OwnerType.ATTRIBUTE, attributeId);
    }
    
    @Override
    public List<FixedValue> getFixedValues(int attributeId) {
        return this.fixedValueDao.getValueByOwner(FixedValue.OwnerType.ATTRIBUTE, attributeId);
    }
}
