package eionet.meta.service;

import java.io.Reader;
import java.util.List;

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
     * @param purgeVocabularyData
     * @return
     * @throws ServiceException
     */
    List<String> importCsvIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder,  boolean purgeVocabularyData) throws ServiceException;

}// end of interface ICSVVocabularyImport
