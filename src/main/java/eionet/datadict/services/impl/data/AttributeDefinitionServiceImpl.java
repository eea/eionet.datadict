package eionet.datadict.services.impl.data;

import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.dal.VocabularyDAO;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.services.data.AttributeDefinitionService;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.VocabularyFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Aliki Kopaneli
 */
@Transactional
@Component
public class AttributeDefinitionServiceImpl implements AttributeDefinitionService {

    @Autowired
    private AttributeDefinitionDAO ddAttributeDefinitionDAOImpl;
    
    @Autowired
    private VocabularyDAO vocabularyDAOImpl;
            
            
    @Override
    public AttributeDefinition getAttributeDefinitionById(int id) throws ResourceNotFoundException {
        AttributeDefinition attrDef = ddAttributeDefinitionDAOImpl.getAttributeDefinitionById(id);
        if (attrDef.getVocabulary()!= null) {
            VocabularyFolder voc = attrDef.getVocabulary();
            voc = vocabularyDAOImpl.getPlainVocabularyById(voc.getId());
            attrDef.setVocabulary(voc);
        }
        return attrDef;
    }
    
}
