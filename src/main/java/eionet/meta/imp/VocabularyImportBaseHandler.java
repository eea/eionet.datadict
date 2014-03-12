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
package eionet.meta.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.util.VocabularyCSVOutputHelper;

/**
 * Base abstract class used for vocabulary import handling from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportBaseHandler {

    /**
     * log message list.
     */
    protected List<String> logMessages = null;

    /**
     * Generated concept beans.
     */
    protected Set<VocabularyConcept> toBeUpdatedConcepts = null;

    /**
     * To be automatically binded elements.
     */
    protected List<DataElement> newBindedElement;

    /**
     * value with folderContextRoot.
     */
    protected String folderContextRoot = null;

    /**
     * Concepts of folder.
     */
    protected List<VocabularyConcept> concepts = null;

    /**
     * Binded elements ids.
     */
    protected Map<String, Integer> bindedElementsIds = null;

    /**
     * Temporary map to hold found related concepts for caching.
     */
    protected Map<String, VocabularyConcept> relatedConceptCache = null;

    /**
     * Newly created concepts.
     */
    protected int numberOfCreatedConcepts = 0;

    /**
     * Vocabulary service.
     */
    @Autowired
    protected IVocabularyService vocabularyService;

    /**
     * Data elements service.
     */
    @Autowired
    protected IDataService dataService;

    /**
     * Default constructor. An object can only be instantiated from this package.
     *
     * @param folderContextRoot
     *            Folder base URI
     * @param concepts
     *            Concepts of folder
     * @param bindedElementsIds
     *            Binded elements to Folder
     */
    protected VocabularyImportBaseHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> bindedElementsIds) {
        this.folderContextRoot = folderContextRoot;
        this.concepts = concepts;
        this.bindedElementsIds = bindedElementsIds;
        this.logMessages = new ArrayList<String>();
        this.toBeUpdatedConcepts = new HashSet<VocabularyConcept>();
        this.newBindedElement = new ArrayList<DataElement>();
        this.relatedConceptCache = new HashMap<String, VocabularyConcept>();
    }

    public List<String> getLogMessages() {
        return this.logMessages;
    }

    public Set<VocabularyConcept> getToBeUpdatedConcepts() {
        return toBeUpdatedConcepts;
    }

    public List<DataElement> getNewBindedElement() {
        return newBindedElement;
    }

    /**
     * Utility method to search a concept in a list of concepts with identifier.
     *
     * @param listOfConcepts
     *            haystack
     * @param conceptIdentifier
     *            needle
     * @return found index, or size of list if not found
     */
    protected int getPositionIn(Collection<VocabularyConcept> listOfConcepts, String conceptIdentifier) {

        int j = 0;
        for (VocabularyConcept vc : listOfConcepts) {
            if (StringUtils.equals(conceptIdentifier, vc.getIdentifier())) {
                break;
            }
            j++;
        }
        return j;
    } // end of method getPositionIn

    /**
     * Utility method to searches concepts (both touched and untouched, and returns found if any, null otherwise).
     *
     * @param conceptIdentifier
     *            identifier of concept to search for
     * @return found concept or null
     */
    protected VocabularyConcept findOrCreateConcept(String conceptIdentifier) {
        int j = getPositionIn(this.concepts, conceptIdentifier);

        VocabularyConcept lastFoundConcept = null;
        // concept found
        if (j < this.concepts.size()) {
            lastFoundConcept = this.concepts.remove(j);
        } else {
            j = getPositionIn(this.toBeUpdatedConcepts, conceptIdentifier);
            if (j == this.toBeUpdatedConcepts.size()) {
                // if there is already such a concept, ignore that line. if not, add a new concept with params.
                lastFoundConcept = new VocabularyConcept();
                lastFoundConcept.setId(--this.numberOfCreatedConcepts);
                lastFoundConcept.setIdentifier(conceptIdentifier);
                List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
                lastFoundConcept.setElementAttributes(newConceptElementAttributes);
            }
        }

        return lastFoundConcept;
    } // end of method findOrCreateConcept

    /**
     * Finds list of data element values by name. Returns to reference to a list in parameter elems.
     *
     * @param elemName
     *            element name to be looked for
     * @param dataElements
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    protected List<DataElement> getDataElementValuesByName(String elemName, List<List<DataElement>> dataElements) {
        return VocabularyCSVOutputHelper.getDataElementValuesByName(elemName, dataElements);
    } // end of method getDataElementValuesByName

    /**
     * finds list of data element values by name and language. It creates a new list and returns it.
     *
     * @param elemName
     *            element name to be looked for
     * @param lang
     *            element lang to be looked for
     * @param dataElements
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    protected List<DataElement> getDataElementValuesByNameAndLang(String elemName, String lang,
            List<List<DataElement>> dataElements) {
        return VocabularyCSVOutputHelper.getDataElementValuesByNameAndLang(elemName, lang, dataElements);
    } // end of method getDataElementValuesByNameAndLang

    /**
     * This method searches for a related concept in database or in cache.
     *
     * @param relatedConceptUri
     *            uri of related concept
     * @return found concept or null
     */
    protected VocabularyConcept findRelatedConcept(String relatedConceptUri) {
        VocabularyConcept foundRelatedConcept = null;
        String relatedConceptVocabularyIdentifier = null;

        int lastSlashIndex = relatedConceptUri.lastIndexOf("/") + 1;
        String relatedConceptIdentifier = relatedConceptUri.substring(lastSlashIndex);
        String relatedConceptBaseUri = relatedConceptUri.substring(0, lastSlashIndex);
        if (StringUtils.isNotEmpty(relatedConceptBaseUri) && StringUtils.isNotEmpty(relatedConceptIdentifier)) {
            // check cache first
            foundRelatedConcept = this.relatedConceptCache.get(relatedConceptUri);
            // && !this.notFoundRelatedConceptCache.contains(relatedConceptUri)
            if (foundRelatedConcept == null) {
                // not found in cache search in database
                String temp = relatedConceptBaseUri.substring(0, relatedConceptBaseUri.length() - 1);
                relatedConceptVocabularyIdentifier = temp.substring(temp.lastIndexOf("/") + 1);
                if (StringUtils.isNotEmpty(relatedConceptVocabularyIdentifier)) {
                    try {

                        VocabularyFolder foundVocabularyFolder = null;
                        // create vocabulary filter
                        VocabularyFilter vocabularyFilter = new VocabularyFilter();
                        vocabularyFilter.setIdentifier(relatedConceptVocabularyIdentifier);
                        vocabularyFilter.setWorkingCopy(false);
                        // first search for vocabularies, to find correct concept and to make searching faster for concepts
                        VocabularyResult vocabularyResult = this.vocabularyService.searchVocabularies(vocabularyFilter);
                        if (vocabularyResult != null) {
                            for (VocabularyFolder vocabularyFolder : vocabularyResult.getList()) {
                                // if it matches with base uri then we found it! this is an costly operation
                                // but to satisfy consistency we need it.
                                if (StringUtils.equals(relatedConceptBaseUri, VocabularyFolder.getBaseUri(vocabularyFolder))) {
                                    foundVocabularyFolder = vocabularyFolder;
                                    break;
                                }
                            }
                        }
                        // if a vocabulary not found don't go on!
                        if (foundVocabularyFolder != null) {
                            VocabularyConceptFilter filter = new VocabularyConceptFilter();
                            filter.setIdentifier(relatedConceptIdentifier);
                            filter.setVocabularyFolderId(foundVocabularyFolder.getId());
                            // search for concepts now
                            VocabularyConceptResult results = this.vocabularyService.searchVocabularyConcepts(filter);
                            // if found more than one, how can system detect which one is searched for!
                            if (results != null && results.getFullListSize() == 1) {
                                foundRelatedConcept = results.getList().get(0);
                                this.relatedConceptCache.put(relatedConceptUri, foundRelatedConcept);
                            }
                        }
                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return foundRelatedConcept;
    } // end of method findRelatedConcept

} // end of class VocabularyImportBaseHandler
