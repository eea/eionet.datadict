package eionet.meta.service;

import java.io.Reader;

import eionet.meta.dao.domain.VocabularyFolder;

/**
 * This interface contains methods to import csv contents to bulk edit a vocabulary.
 *
 */
public interface ICSVVocabularyImportService {


    /**
     *
     * @param contents
     * @param vocabularyFolder
     * @param concepts
     * @throws ServiceException
     */
    void importCsvIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder,  boolean purgeVocabularyData) throws ServiceException;

}// end of interface ICSVVocabularyImport
