package eionet.meta.exports.codelist;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Lena KARGIOTI eka@eworx.gr
 */
public class VocabularyCodeValueHandler extends CodeValueHandler {

    private static final Logger LOGGER = Logger.getLogger(VocabularyCodeValueHandler.class);

    private final IVocabularyConceptDAO vocabularyConceptDAO;
    private List<String> relationshipNames;

    public VocabularyCodeValueHandler(IVocabularyConceptDAO vocabularyConceptDAO, IDataElementDAO elementDAO) {
        super(elementDAO);
        this.vocabularyConceptDAO = vocabularyConceptDAO;
    }

    @Override
    List<CodeItem> getCodeItemList() {
        if (this.element == null) {
            throw new UnsupportedOperationException("Data element is not set");
        }

        Integer vocabularyID = this.element.getVocabularyId();
        if (vocabularyID == null) {
            LOGGER.info("Vocabulary Element identified by '" + this.element.getIdentifier() + "' is not bound to any vocabulary. Skipping...");
            return Collections.emptyList();
        }

        List<VocabularyConcept> concepts = this.vocabularyConceptDAO.getConceptsWithAttributeValues(vocabularyID, StandardGenericStatus.VALID);
        List<CodeItem> items = new ArrayList<CodeItem>();
        
        for (VocabularyConcept concept : concepts) {
            String code = concept.getNotation();
            String label = concept.getLabel();
            String definition = concept.getDefinition();

            CodeItem item = new CodeItem(code, label, definition);
            items.add(item);
            
            for (List<DataElement> conceptAttributeValues : concept.getElementAttributes()) {
                List<CodeItem> relatedCodeItems = new ArrayList<CodeItem>();
                
                for (DataElement conceptAttributeValue : conceptAttributeValues) {
                    if (conceptAttributeValue.getRelatedConceptId() != null) {
                        CodeItem relatedCodeItem = new CodeItem(conceptAttributeValue.getRelatedConceptNotation(), 
                                conceptAttributeValue.getRelatedConceptLabel(), conceptAttributeValue.getRelatedConceptDefinition());
                        relatedCodeItems.add(relatedCodeItem);
                    }
                }
                
                if (!relatedCodeItems.isEmpty()) {
                    DataElement conceptAttribute = conceptAttributeValues.get(0);
                    String relationshipName = conceptAttribute.getName();
                    this.addRelationshipName(relationshipName);
                    RelationshipInfo info = new RelationshipInfo(relationshipName, conceptAttribute.getRelatedConceptVocabularyLabel(), 
                            conceptAttribute.getRelatedConceptVocSetLabel(), relatedCodeItems);
                    item.addRelationship(info);
                }
            }
        }
        
        return items;
    }

    private void addRelationshipName(String relationshipName) {
        if (relationshipName == null)
            return;
        if (relationshipNames == null) {
            relationshipNames = new ArrayList<String>();
        }
        if (relationshipNames.contains(relationshipName))
            return;
        relationshipNames.add(relationshipName);
    }

    @Override
    public List<String> getRelationshipNames() {
        return relationshipNames;
    }

}
