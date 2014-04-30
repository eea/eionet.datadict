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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eionet.meta.dao.IAttributeDAO;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IFolderDAO;
import eionet.meta.dao.IRdfNamespaceDAO;
import eionet.meta.dao.ISiteCodeDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.IVocabularyFolderDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Service implementation to match references in vocabularies.
 *
 * @author enver
 */
@Service
public class VocabularyReferenceMatchServiceImpl implements IVocabularyReferenceMatchService {

    /**
     * Vocabulary folder DAO.
     */
    @Autowired
    private IVocabularyFolderDAO vocabularyFolderDAO;

    /**
     * Vocabulary concept DAO.
     */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDAO;

    /**
     * Site Code DAO.
     */
    @Autowired
    private ISiteCodeDAO siteCodeDAO;

    /**
     * Attribute DAO.
     */
    @Autowired
    private IAttributeDAO attributeDAO;

    /**
     * Folder DAO.
     */
    @Autowired
    private IFolderDAO folderDAO;

    /**
     * Data element DAO.
     */
    @Autowired
    private IDataElementDAO dataElementDAO;

    /**
     * Rdf namespace DAO.
     */
    @Autowired
    private IRdfNamespaceDAO rdfNamespaceDAO;

    /**
     * {@inheritDoc}
     */
    // @Transactional(rollbackFor = ServiceException.class)
    @Override
    public List<String> matchReferences() throws ServiceException {
        int numberOfMatchedElementValuesToIds = 0;
        List<String> logs = new ArrayList<String>();
        List<DataElement> elements = dataElementDAO.getPotentialReferringVocabularyConceptsElements();
        for (DataElement elem : elements) {
            String elemValue = elem.getAttributeValue();
            String baseUri = elem.getRelatedConceptBaseURI();
            String conceptIdentifier = StringUtils.trimToEmpty(StringUtils.replace(elemValue, baseUri, ""));
            if (StringUtils.isNotEmpty(conceptIdentifier) && !StringUtils.contains(conceptIdentifier, "/")) {
                // now hope we have a valid concept here, search for it.
                VocabularyConceptFilter filter = new VocabularyConceptFilter();
                // !!! ATTENTION: this is really dependent getPotentialReferringVocabularyConceptsElements method implementation
                filter.setVocabularyFolderId(elem.getRelatedConceptId());
                filter.setIdentifier(conceptIdentifier);
                filter.setUsePaging(false);
                VocabularyConceptResult vocabularyConceptResult = this.vocabularyConceptDAO.searchVocabularyConcepts(filter);
                // continue if and only if one item found
                List<VocabularyConcept> concepts = vocabularyConceptResult.getList();
                if (concepts.size() == 1) {
                    VocabularyConcept concept = vocabularyConceptResult.getList().get(0);
                    elem.setRelatedConceptId(concept.getId());
                    elem.setAttributeValue(null);
                    // now update it
                    this.vocabularyFolderDAO.updateRelatedConceptValueToId(elem);
                    numberOfMatchedElementValuesToIds++;
                }
            }
        }
        if (numberOfMatchedElementValuesToIds > 0) {
            logs.add("Number of vocabulary concept elements updated to have related concept id from element value: "
                    + numberOfMatchedElementValuesToIds);
        }

        return logs;
    } // end of method matchReferences
} // end of class RDFVocabularyImportServiceImpl
