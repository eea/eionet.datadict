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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.meta.service.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eionet.meta.dao.domain.Folder;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularySetSearchItem;

/**
 * Vocabulary concept search result.
 *
 * @author Juhan Voolaid
 */
public class VocabularyConceptResult extends PagedResult<VocabularyConcept> {
    /**
     * distinct list of vocabulary sets.
     */
    private List<Folder> vocabularySets;
    /**
     * Static comparator instance for Vocabulary sets. Used in sorting.
     */
    private static VocabularySetComparator comparator = new VocabularySetComparator();

    /**
     * Class constructor.
     *
     * @param items
     *            list of concepts
     * @param totalItems
     *            total found items
     * @param pagedRequest
     *            if it is paged request or not
     */
    public VocabularyConceptResult(List<VocabularyConcept> items, int totalItems, PagedRequest pagedRequest) {
        super(items, totalItems, pagedRequest);

        vocabularySets = new ArrayList<Folder>();
        for (VocabularyConcept concept : items) {
            Folder vocSet = new VocabularySetSearchItem();
            vocSet.setId(concept.getVocabularySetId());
            vocSet.setLabel(concept.getVocabularySetLabel());

            // FIXME - contains is wrong
            if (!vocabularySets.contains(vocSet)) {
                vocabularySets.add(vocSet);
            }
        }
        Collections.sort(vocabularySets, comparator);
    }

    public List<Folder> getVocabularySets() {
        return vocabularySets;
    }

    /**
     * helper class for sorting vocabulary sets.
     */
    public static class VocabularySetComparator implements Comparator<Folder> {

        @Override
        public int compare(Folder f1, Folder f2) {
            return f1.getLabel().compareTo(f2.getLabel());
        }

    }
}
