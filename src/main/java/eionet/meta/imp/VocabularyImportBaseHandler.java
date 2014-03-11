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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.IDataService;
import eionet.meta.service.IVocabularyService;

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

} // end of class VocabularyImportBaseHandler
