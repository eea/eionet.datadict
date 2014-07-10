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
 * TripleDev
 */

package eionet.meta.service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.imp.VocabularyCSVImportHandler;
import eionet.util.Util;

/**
 * Service implementation to import CSV into a Vocabulary Folder.
 *
 * @author enver
 */
@Service
public class CSVVocabularyImportServiceImpl extends VocabularyImportServiceBaseImpl implements ICSVVocabularyImportService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public List<String> importCsvIntoVocabulary(Reader content, VocabularyFolder vocabularyFolder, boolean purgeVocabularyData,
            boolean purgeBoundElements) throws ServiceException {

        final String folderContextRoot = VocabularyFolder.getBaseUri(vocabularyFolder);
		// check for valid base uri
        if (!Util.isValidUri(folderContextRoot)) {
            throw new ServiceException("Vocabulary does not have a valid base URI");
        }

        this.logMessages = new ArrayList<String>();
        List<VocabularyConcept> concepts =
                vocabularyService.getValidConceptsWithAttributes(vocabularyFolder.getId());

        List<DataElement> boundElements = vocabularyService.getVocabularyDataElements(vocabularyFolder.getId());
        if (purgeVocabularyData) {
            String message = "All concepts ";
            purgeConcepts(concepts);
            concepts = new ArrayList<VocabularyConcept>();
            if (purgeBoundElements) {
                purgeBoundElements(vocabularyFolder.getId(), boundElements);
                boundElements = new ArrayList<DataElement>();
                message += "and bound elements ";
            }
            message += "were deleted (with purge operation).";
            this.logMessages.add(message);
        }

        Map<String, Integer> elementToId = new HashMap<String, Integer>();
        for (DataElement elem : boundElements) {
            String identifier = elem.getIdentifier();
            if (StringUtils.isNotEmpty(identifier)) {
                elementToId.put(identifier, elem.getId());
            }
        }


        VocabularyCSVImportHandler handler = new VocabularyCSVImportHandler(folderContextRoot, concepts, elementToId, content);
        handler.generateUpdatedBeans();

        importIntoDb(vocabularyFolder.getId(), handler.getToBeUpdatedConcepts(), handler.getNewBoundElement(),
                handler.getElementsRelatedToNotCreatedConcepts());
        this.logMessages.addAll(handler.getLogMessages());
        this.logMessages.add("CSV imported into Database.");

        return this.logMessages;
    } // end of method importCsvIntoVocabulary

} // end of class CSVVocabularyImportServiceImpl
