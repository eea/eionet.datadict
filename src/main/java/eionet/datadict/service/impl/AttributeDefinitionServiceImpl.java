package eionet.datadict.service.impl;

import eionet.datadict.dal.AttributeDefinitionDAO;
import eionet.datadict.dal.VocabularyDAO;
import eionet.datadict.model.AttributeDefinition;
import eionet.datadict.service.AttributeDefinitionService;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.VocabularyFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author exorx-alk
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
