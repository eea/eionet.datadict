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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 */

package eionet.datadict.dal;

import eionet.datadict.model.SiteCode;
import java.util.List;

/**
 *
 * @author nta@eworx.gr
 */
public interface SiteCodeDao {
    
    /**
     * Updates the VOCABULARY_CONCEPT_ID column of T_SITE_CODE table
     *
     * @return
     */
    void updateVocabularyConceptId();

    /**
     * Retrieves all rows from T_SITE_CODE table
     *
     * @return
     */
    List<SiteCode> getAllSiteCodes();
}
