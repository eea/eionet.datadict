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

package eionet.meta.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DDUser;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site code service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class SiteCodeServiceImpl implements ISiteCodeService {

    private static final String SITE_CODE_IDENTIFIER = "CountryCode";

    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FixedValue> getAllCountries() throws ServiceException {
        try {
            int countryElementId = dataElementDao.getDataElementId(SITE_CODE_IDENTIFIER);
            return dataElementDao.getFixedValues(countryElementId);
        } catch (Exception e) {
            throw new ServiceException("Failed to get countries: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FixedValue> getUserCountries(DDUser user) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(String country, int amount, String userName) throws ServiceException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(String country, String[] siteCodeNames, String userName) throws ServiceException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }
}
