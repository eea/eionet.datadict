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

package eionet.meta.dao;

import eionet.meta.dao.domain.DDApiKey;

/**
 * DAO definition for API-Key.
 *
 * @author enver
 */
public interface IApiKeyDAO {

    /**
     * Search given key in existing defined API keys in API_KEY table.
     *
     * @param key API key sent by external request.
     * @return DDApiKey object if found an object, null otherwise.
     */
    DDApiKey getApiKey(String key);

}//end of class IApiKeyDAO
