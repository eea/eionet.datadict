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
 *        Kaido Laine
 */


package eionet.meta.service.data;

import java.util.List;


/**
 * Result for saerching concepts in Vocabulary page.
 *
 * @author Kaido Laine
 */
public class ConceptSearchResult extends PagedResult<VocabularyConceptData> {

    /**
     * Class constructor.
     *
     * @param items
     *            found items array
     * @param totalItems
     *            count of found items
     * @param pagedRequest
     *            paged request the result is passed to
     */
    public ConceptSearchResult(List<VocabularyConceptData> items, int totalItems, PagedRequest pagedRequest) {
        super(items, totalItems, pagedRequest);
    }
}
