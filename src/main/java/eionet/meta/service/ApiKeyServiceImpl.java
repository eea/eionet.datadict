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

import eionet.meta.dao.IApiKeyDAO;
import eionet.meta.dao.domain.DDApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for API-Key.
 *
 * @author enver
 */
@Service
public class ApiKeyServiceImpl implements IApiKeyService {
    /**
     * API Key table DAO.
     */
    @Autowired
    private IApiKeyDAO apiKeyDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public DDApiKey getApiKey(String key) throws ServiceException {
        try {
            return apiKeyDAO.getApiKey(key);
        } catch (Exception e) {
            throw new ServiceException("Failed to get api key: " + e.getMessage(), e);
        }
    }//end of method getApiKey
}//end of class ApiKeyServiceImpl
