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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DDUser;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.ISiteCodeDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;
import eionet.util.SecurityUtil;

/**
 * Site code service implementation.
 *
 * @author Juhan Voolaid
 */
@Service
@Transactional
public class SiteCodeServiceImpl implements ISiteCodeService {

    private static final String SITE_CODE_IDENTIFIER = "CountryCode";

    /** List of roles which is used for calculating users permissions on country level */
    private static final String[] COUNTRY_USER_ROLES = {"eionet-nfp-cc", "eionet-nfp-mc", "eionet-nrc-nature-cc",
            "eionet-nrc-nature-mc"};

    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDao;

    /** Site Code DAO. */
    @Autowired
    private ISiteCodeDAO siteCodeDao;

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

        if (user != null) {
            List<FixedValue> allCountries = getAllCountries();
            if (user.hasPermission("/sitecodes", "u")) {
                return allCountries;
            } else {
                List<String> userCountries = SecurityUtil.getUserCountriesFromRoles(user, COUNTRY_USER_ROLES);
                if (userCountries != null) {
                    List<FixedValue> userCountryFixedValues = new ArrayList<FixedValue>();
                    for (FixedValue countryFxv : allCountries) {
                        if (userCountries.contains(countryFxv.getValue())) {
                            userCountryFixedValues.add(countryFxv);
                        }
                    }
                    return userCountryFixedValues;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(String countryCode, int amount, String userName) throws ServiceException {
        allocateSiteCodes(countryCode, new String[amount], userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(String countryCode, String[] siteNames, String userName) throws ServiceException {

        int amount = siteNames.length;
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setPageNumber(1);
        siteCodeFilter.setPageSize(amount);
        siteCodeFilter.setStatus(SiteCodeStatus.NEW);
        try {
            SiteCodeResult freeSiteCodes = siteCodeDao.searchSiteCodes(siteCodeFilter);

            if (freeSiteCodes.getFullListSize() != amount) {
                throw new ServiceException("Did not find enough free site codes for allocating " + amount + " sites!");
            }
            siteCodeDao.allocateSiteCodes(freeSiteCodes.getList(), countryCode, userName, siteNames);

            //TODO return the list of site codes to be allocated
        } catch (Exception e) {
            throw new ServiceException("Failed to allocate site codes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ServiceException {
        try {
            return siteCodeDao.searchSiteCodes(filter);
        } catch (Exception e) {
            throw new ServiceException("Failed to search site codes: " + e.getMessage(), e);
        }
    }
}
