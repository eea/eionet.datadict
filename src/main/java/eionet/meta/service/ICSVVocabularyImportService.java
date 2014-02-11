package eionet.meta.service;

import java.io.Reader;
import java.util.List;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;

/**
 * This interface contains methods to import csv contents to bulk edit a vocabulary.
 *
 */
public interface ICSVVocabularyImportService {

    //TODO i will update this part, it should work line by line basis
    /**
     *
     * @param contents
     * @param vocabularyFolder
     * @param concepts
     * @throws ServiceException
     */
    void importCsvIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder, List<VocabularyConcept> concepts) throws ServiceException;

}// end of interface ICSVVocabularyImport
