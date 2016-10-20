package eionet.datadict.dal;

import eionet.datadict.model.Vocabulary;
import java.util.List;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public interface VocabularyDao {

    boolean exists(Integer vocabularySetId, String vocabularyIdentifier);
    
    boolean exists(String vocabularySetIdentifier, String vocabularyIdentifier);
    
    List<Vocabulary> getValueListCodesOfDataElementsInTable(int tableId);
    
}
