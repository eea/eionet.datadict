package eionet.meta.exports.codelist;

import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement.DataElementValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@Component
public class CodeValueHandlerProvider {

    private final IDataElementDAO elementDAO;    
    private final IVocabularyConceptDAO vocabularyConceptDAO;

    @Autowired
    public CodeValueHandlerProvider(IDataElementDAO elementDAO, IVocabularyConceptDAO vocabularyConceptDAO) {
        this.elementDAO = elementDAO;
        this.vocabularyConceptDAO = vocabularyConceptDAO;
    }

    public CodeValueHandler get(DataElementValueType type) {
        switch (type) {
            case VOCABULARY: {
                return new VocabularyCodeValueHandler(vocabularyConceptDAO, elementDAO);
            }
            case QUANTITIVE:
            case FIXED:
            default: {
                return new FixedValueHandler(elementDAO);
            }
        }        
    }

}
