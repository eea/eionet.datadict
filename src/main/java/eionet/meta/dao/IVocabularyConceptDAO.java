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

package eionet.meta.dao;

import java.util.List;

import eionet.meta.dao.domain.VocabularyConcept;

/**
 * Vocabulary concept DAO interface.
 *
 * @author Juhan Voolaid
 */
public interface IVocabularyConceptDAO {

    /**
     * Returns the vocabulary folder's concepts.
     *
     * @param vocabularyFolderId
     * @return
     */
    List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId);

    /**
     * Creates new vocabulary concept into database.
     *
     * @param vocabularyFolderId
     * @param vocabularyConcept
     * @return
     */
    int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept);

    /**
     * Updates vocabulary concept.
     *
     * @param vocabularyConcept
     */
    void updateVocabularyConcept(VocabularyConcept vocabularyConcept);
}
