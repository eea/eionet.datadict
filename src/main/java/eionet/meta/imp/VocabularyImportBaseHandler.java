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

import java.sql.Date;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;
import eionet.meta.service.ServiceException;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.meta.service.data.VocabularyFilter;
import eionet.meta.service.data.VocabularyResult;
import eionet.meta.spring.SpringApplicationContext;
import eionet.util.Pair;
import eionet.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base abstract class used for vocabulary import handling from different sources (RDF or CSV).
 *
 * @author enver
 */
public abstract class VocabularyImportBaseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyImportBaseHandler.class);

    /**
     * log message list.
     */
    protected List<String> logMessages = null;

    /**
     * Concepts of folder.
     */
    protected List<VocabularyConcept> concepts = null;

    /**
     * Generated concept beans.
     */
    protected List<VocabularyConcept> toBeUpdatedConcepts = null;

    /**
     * Not seen concepts yet.
     */
    protected List<VocabularyConcept> notSeenConceptsYet = null;

    /**
     * To be automatically bound elements.
     */
    protected List<DataElement> newBoundElement;

    /**
     * value with folderContextRoot.
     */
    protected String folderContextRoot = null;

    /**
     * Bound elements ids.
     */
    protected Map<String, Integer> boundElementsIds = null;

    /**
     * This map is used for found related concepts which are not created yet.
     */
    protected Map<Integer, Set<DataElement>> elementsRelatedToNotCreatedConcepts = null;

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
    private IVocabularyService vocabularyService;

    /**
     * Data elements service.
     */
    private IDataService dataService;
    
    public IDataService getDataService() {
        if (this.dataService == null) {
            return SpringApplicationContext.getContext().getBean(IDataService.class); 
        }
        return this.dataService;
    }

    public IVocabularyService getVocabularyService() {
        if (this.vocabularyService == null) {
            return SpringApplicationContext.getContext().getBean(IVocabularyService.class); 
        }
        return this.vocabularyService;
    }
    
    /**
     * An object can only be instantiated from this package.
     *
     * @param folderContextRoot
     *            Folder base URI
     * @param concepts
     *            Concepts of folder
     * @param boundElementsIds
     *            Bound elements to Folder
     */
    protected VocabularyImportBaseHandler(String folderContextRoot, List<VocabularyConcept> concepts,
            Map<String, Integer> boundElementsIds) {
        this.folderContextRoot = folderContextRoot;
        this.concepts = concepts;
        this.boundElementsIds = boundElementsIds;
        this.logMessages = new ArrayList<String>();
        this.toBeUpdatedConcepts = new ArrayList<VocabularyConcept>();
        this.newBoundElement = new ArrayList<DataElement>();
        this.relatedConceptCache = new HashMap<String, VocabularyConcept>();
        this.notSeenConceptsYet = new ArrayList<VocabularyConcept>();
        this.elementsRelatedToNotCreatedConcepts = new HashMap<Integer, Set<DataElement>>();
    }

    public List<String> getLogMessages() {
        return this.logMessages;
    }

    public List<VocabularyConcept> getToBeUpdatedConcepts() {
        return toBeUpdatedConcepts;
    }

    public List<DataElement> getNewBoundElement() {
        return newBoundElement;
    }

    public Map<Integer, Set<DataElement>> getElementsRelatedToNotCreatedConcepts() {
        return elementsRelatedToNotCreatedConcepts;
    }

    /**
     * Utility method to searches concepts (both touched and untouched, and returns found if any, null otherwise).
     *
     * @param conceptIdentifier
     *            identifier of concept to search for
     * @return found concept with a flag if it is found in seen concepts
     */
    protected Pair<VocabularyConcept, Boolean> findOrCreateConcept(String conceptIdentifier) {
        int j = getPositionIn(this.concepts, conceptIdentifier);

        // concept found
        if (j < this.concepts.size()) {
            return new Pair<VocabularyConcept, Boolean>(this.concepts.remove(j), false);
        }

        // concept may already be created, check it first
        j = getPositionIn(this.notSeenConceptsYet, conceptIdentifier);
        if (j < this.notSeenConceptsYet.size()) {
            return new Pair<VocabularyConcept, Boolean>(this.notSeenConceptsYet.remove(j), false);
        }

        j = getPositionIn(this.toBeUpdatedConcepts, conceptIdentifier);
        // concept found in to be updated concepts
        if (j < this.toBeUpdatedConcepts.size()) {
            return new Pair<VocabularyConcept, Boolean>(this.toBeUpdatedConcepts.get(j), true);
        }

        if (j == this.toBeUpdatedConcepts.size()) {
            // if there is already such a concept, ignore that line. if not, add a new concept with params.
            VocabularyConcept lastFoundConcept = new VocabularyConcept();
            lastFoundConcept.setId(--this.numberOfCreatedConcepts);
            lastFoundConcept.setIdentifier(conceptIdentifier);
            lastFoundConcept.setStatus(StandardGenericStatus.VALID);
            lastFoundConcept.setStatusModified(new Date(System.currentTimeMillis()));
            lastFoundConcept.setAcceptedDate(new Date(System.currentTimeMillis()));
            List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
            lastFoundConcept.setElementAttributes(newConceptElementAttributes);
            return new Pair<VocabularyConcept, Boolean>(lastFoundConcept, false);
        }

        return null;
    } // end of method findOrCreateConcept

    /**
     * This method searches for a related concept in database or in cache.
     *
     * @param relatedConceptUri
     *            uri of related concept
     * @return found concept or null
     */
    protected VocabularyConcept findRelatedConcept(String relatedConceptUri) {
        VocabularyConcept foundRelatedConcept = null;
        String relatedConceptIdentifier;

        if (StringUtils.startsWith(relatedConceptUri, this.folderContextRoot)) {
            // it is a self reference to a concept in this vocabulary.
            // if it is in found concept then we are lucky. but it may be not created yet... and also wont be created at all...
            relatedConceptIdentifier = relatedConceptUri.replace(this.folderContextRoot, "");
            if (StringUtils.contains(relatedConceptIdentifier, "/") || !Util.isValidIdentifier(relatedConceptIdentifier)) {
                return null;
            }
            int index = getPositionIn(this.concepts, relatedConceptIdentifier);
            if (index < this.concepts.size()) {
                return this.concepts.get(index);
            }

            index = getPositionIn(this.toBeUpdatedConcepts, relatedConceptIdentifier);
            if (index < this.toBeUpdatedConcepts.size()) {
                return this.toBeUpdatedConcepts.get(index);
            }

            // concept not seen yet.
            index = getPositionIn(this.notSeenConceptsYet, relatedConceptIdentifier);
            if (index < this.notSeenConceptsYet.size()) {
                return this.notSeenConceptsYet.get(index);
            }

            // so create it
            foundRelatedConcept = new VocabularyConcept();
            foundRelatedConcept.setId(--this.numberOfCreatedConcepts);
            foundRelatedConcept.setIdentifier(relatedConceptIdentifier);
            foundRelatedConcept.setStatus(StandardGenericStatus.VALID);
            foundRelatedConcept.setStatusModified(new Date(System.currentTimeMillis()));
            foundRelatedConcept.setAcceptedDate(new Date(System.currentTimeMillis()));
            List<List<DataElement>> newConceptElementAttributes = new ArrayList<List<DataElement>>();
            foundRelatedConcept.setElementAttributes(newConceptElementAttributes);
            this.notSeenConceptsYet.add(foundRelatedConcept);
            return foundRelatedConcept;
        }

        try {
            // extract related concept base uri and related concept identifier
            int lastDelimiterIndex =
                    Math.max(relatedConceptUri.lastIndexOf("/"),
                            Math.max(relatedConceptUri.lastIndexOf("#"), relatedConceptUri.lastIndexOf(":"))) + 1;
            String relatedConceptBaseUri = relatedConceptUri.substring(0, lastDelimiterIndex);
            relatedConceptIdentifier = relatedConceptUri.substring(lastDelimiterIndex);
            if (StringUtils.isNotEmpty(relatedConceptBaseUri) && StringUtils.isNotEmpty(relatedConceptIdentifier)) {
                // check cache first
                foundRelatedConcept = this.relatedConceptCache.get(relatedConceptUri);
                // && !this.notFoundRelatedConceptCache.contains(relatedConceptUri)
                if (foundRelatedConcept == null) {
                    // not found in cache search in database
                    // search for vocabularies with base uri.
                    VocabularyFolder foundVocabularyFolder = null;
                    VocabularyFilter vocabularyFilter = new VocabularyFilter();
                    vocabularyFilter.setWorkingCopy(false);
                    vocabularyFilter.setUsePaging(false);
                    vocabularyFilter.setBaseUri(relatedConceptBaseUri);
                    // first search for vocabularies, to find correct concept and to make searching faster for concepts
                    VocabularyResult vocabularyResult = getVocabularyService().searchVocabularies(vocabularyFilter);
                    if (vocabularyResult != null && vocabularyResult.getTotalItems() > 0) {
                        // get the first found item, since base uri kinda unique
                        foundVocabularyFolder = vocabularyResult.getList().get(0);
                        // folder found, so go on for concept search
                        VocabularyConceptFilter filter = new VocabularyConceptFilter();
                        filter.setUsePaging(false);
                        filter.setIdentifier(relatedConceptIdentifier);
                        filter.setVocabularyFolderId(foundVocabularyFolder.getId());
                        // search for concepts now
                        VocabularyConceptResult results = getVocabularyService().searchVocabularyConcepts(filter);
                        // if found more than one, how can system detect which one is searched for!
                        if (results != null && results.getFullListSize() == 1) {
                            foundRelatedConcept = results.getList().get(0);
                            this.relatedConceptCache.put(relatedConceptUri, foundRelatedConcept);
                        }
                    }
                }
            }
        } catch (ServiceException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return foundRelatedConcept;
    } // end of method findRelatedConcept

    /**
     * This method includes common code to update elementsRelatedToNotCreatedConcepts.
     *
     * @param conceptId
     *            concept id which is not created (it should be less than 0)
     * @param element
     *            data elem to be added set with this concept id
     */
    protected void addToElementsReferringNotCreatedConcepts(int conceptId, DataElement element) {
        Set<DataElement> elementsReferringThisConcept = this.elementsRelatedToNotCreatedConcepts.get(conceptId);
        if (elementsReferringThisConcept == null) {
            elementsReferringThisConcept = new HashSet<DataElement>();
            this.elementsRelatedToNotCreatedConcepts.put(conceptId, elementsReferringThisConcept);
        }
        elementsReferringThisConcept.add(element);
    } // end of method addToElementsReferringNotCreatedConcepts

    /**
     * If a newly created concept does not have a label field, then remove it from list and add it ${this.notSeenConceptsYet}. This
     * is necessary not to receive a null constraint fail exception when updating table. This error won't be received from CSV
     * import, since it has more strict controls. This method should be called before processUnseenConceptsForRelatedElements.
     *
     * @return log message
     */
    protected List<String> processNewlyCreatedConceptsForNullCheck() {
        int numberOfRemovedConcepts = 0;
        StringBuffer messageBuffer = new StringBuffer("--> Identifiers of not created concepts are as following: ");
        Iterator<VocabularyConcept> conceptIterator = this.toBeUpdatedConcepts.iterator();
        while (conceptIterator.hasNext()) {
            VocabularyConcept concept = conceptIterator.next();
            if (concept.getId() < 0 && StringUtils.isEmpty(concept.getLabel())) {
                conceptIterator.remove();
                messageBuffer.append(concept.getIdentifier()).append(", ");
                numberOfRemovedConcepts++;
                this.notSeenConceptsYet.add(concept);
            }
        }
        if (numberOfRemovedConcepts > 0) {
            // delete last two characters from message buffer because they are no necessary.
            messageBuffer.deleteCharAt(messageBuffer.length() - 1);
            messageBuffer.deleteCharAt(messageBuffer.length() - 1);
            List<String> messageBufferList = new ArrayList<String>();
            messageBufferList.add(numberOfRemovedConcepts + " concepts not created because of empty label specification.");
            messageBufferList.add(messageBuffer.toString());
            return messageBufferList;
        }

        return null;
    } // end of method processNewlyCreatedConceptsForNullCheck

    /**
     * If a concept is not seen after all file is processed then just set related concept id to null. And set the value of dataelem.
     */
    protected void processUnseenConceptsForRelatedElements() {
        for (VocabularyConcept concept : this.notSeenConceptsYet) {
            Set<DataElement> elements = this.elementsRelatedToNotCreatedConcepts.remove(concept.getId());
            if (elements != null) {
                for (DataElement elem : elements) {
                    String relatedConceptIdentifier = elem.getRelatedConceptIdentifier();
                    elem.setAttributeValue(this.folderContextRoot + relatedConceptIdentifier);
                    elem.setRelatedConceptId(null);
                    elem.setRelatedConceptIdentifier(null);
                }
            }
        }
    } // end of method processUnseenConceptsForRelatedElements

    /**
     * Utility method to search a concept in a list of concepts with identifier.
     *
     * @param listOfConcepts
     *            haystack
     * @param conceptIdentifier
     *            needle
     * @return found index, or size of list if not found
     */
    public static int getPositionIn(List<VocabularyConcept> listOfConcepts, String conceptIdentifier) {
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
     * Finds list of data element values by name. Returns to reference to a list in parameter elems.
     *
     * @param elemName
     *            element name to be looked for
     * @param dataElements
     *            list containing element definitions with values
     * @return list of dataelement objects containing values
     */
    public static List<DataElement> getDataElementValuesByName(String elemName, List<List<DataElement>> dataElements) {
        return VocabularyOutputHelper.getDataElementValuesByName(elemName, dataElements);
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
    public static List<DataElement> getDataElementValuesByNameAndLang(String elemName, String lang,
            List<List<DataElement>> dataElements) {
        return VocabularyOutputHelper.getDataElementValuesByNameAndLang(elemName, lang, dataElements);
    } // end of method getDataElementValuesByNameAndLang

} // end of class VocabularyImportBaseHandler
