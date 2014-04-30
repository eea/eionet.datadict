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

import java.util.List;

/**
 * This interface contains methods to match references in vocabulary.
 */
public interface IVocabularyReferenceMatchService {

    /**
     * A Transactional method to match references. Expected to be called from a background task. *
     *
     * @return List of log messages
     * @throws eionet.meta.service.ServiceException
     *             Error if operation fails for any reason.
     */
    List<String> matchReferences() throws ServiceException;

} // end of interface IVocabularyReferenceMatchService
