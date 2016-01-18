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

import eionet.meta.dao.domain.StandardGenericStatus;

/**
 * This interface is the base interface for vocabulary import services (used to bulk edit a vocabulary).
 *
 * @author enver
 */
public interface IVocabularyImportService {

    // Enum Definitions

    /**
     * Before actions for file upload operations. Can be extended in the future for other file operations as well.
     */
    public static enum UploadActionBefore {
        keep, remove
    }

    /**
     * Actions for file upload operations. Can be extended in the future for other file operations as well.
     */
    public static enum UploadAction {
        add, delete, add_and_purge_per_predicate_basis
    }

    /**
     * Missing concept actions for file upload operations. Can be extended in the future for other file operations.
     */
    public static enum MissingConceptsAction {
        keep, remove, invalid, deprecated, retired, superseded
    }

    /**
     * A mapper method get status for missing concept action.
     *
     * @param missingConceptsAction given missing concept action.
     * @return mapping status value or null.
     */
    public StandardGenericStatus getStatusForMissingConceptAction(MissingConceptsAction missingConceptsAction);

} // end of interface IVocabularyImportService
