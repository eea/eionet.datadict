package eionet.datadict.services.impl.data;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.VocabularyDAO;
import eionet.datadict.model.Attribute;
import eionet.datadict.services.data.AttributeDataService;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.resources.ResourceDbIdInfo;
import eionet.datadict.resources.ResourceType;
import eionet.meta.dao.domain.VocabularyFolder;
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
    
    @Autowired
    public AttributeDataServiceImpl(AttributeDao attributeDao, VocabularyDAO vocabularyDao) {
        this.attributeDao = attributeDao;
        this.vocabularyDao = vocabularyDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Attribute getAttribute(int id) throws ResourceNotFoundException {
        Attribute attribute = attributeDao.getById(id);
        
        if (attribute == null) {
            throw new ResourceNotFoundException(ResourceType.ATTRIBUTE, new ResourceDbIdInfo(id));
        }
        
        if (attribute.getVocabulary() != null) {
            VocabularyFolder voc = attribute.getVocabulary();
            voc = vocabularyDao.getPlainVocabularyById(voc.getId());
            attribute.setVocabulary(voc);
        }
        
        return attribute;
    }
    
    @Override
    public int createAttribute(Attribute attribute) {
        return this.attributeDao.create(attribute);
    }

    @Override
    public void updateAttribute(Attribute attribute) {
        this.attributeDao.update(attribute);
    }
    
}
