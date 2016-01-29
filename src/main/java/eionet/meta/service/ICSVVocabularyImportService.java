/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */
package eionet.meta.service;

import eionet.meta.dao.domain.VocabularyFolder;

import java.io.Reader;
import java.util.List;

/**
 * This interface contains methods to import csv contents to bulk edit a vocabulary.
 *
 * @author enver
 */
public interface ICSVVocabularyImportService {

    /**
     * A Transactional method to import CSV file contents into a vocabulary folder. User can request purging data first and then
     * inserting from scracth.
     *
     * @param contents            Reader object to read file content
     * @param vocabularyFolder    Vocabulary folder under bulk edit mode
     * @param purgeVocabularyData Purge all vocabulary concepts of folder
     * @param purgeBoundElements  Purge all bound elements of folder
     * @return List of log messages
     * @throws ServiceException Error if input is not valid
     */
    List<String> importCsvIntoVocabulary(Reader contents, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData,
                                         boolean purgeBoundElements) throws ServiceException;

} // end of interface ICSVVocabularyImport
