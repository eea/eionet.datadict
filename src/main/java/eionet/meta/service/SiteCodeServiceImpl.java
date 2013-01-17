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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eionet.meta.DDUser;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.ISiteCodeDAO;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.service.data.AllocationResult;
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
    public AllocationResult allocateSiteCodes(String countryCode, int amount, String userName) throws ServiceException {
        return allocateSiteCodes(countryCode, new String[amount], userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllocationResult allocateSiteCodes(String countryCode, String[] siteNames, String userName) throws ServiceException {

        // TODO: validate user right to allocate given country code

        int amount = siteNames.length;
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setPageNumber(1);
        siteCodeFilter.setPageSize(amount);
        siteCodeFilter.setStatus(SiteCodeStatus.NEW);
        try {
            SiteCodeResult freeSiteCodes = siteCodeDao.searchSiteCodes(siteCodeFilter);

            Calendar c = Calendar.getInstance();
            c.set(Calendar.MILLISECOND, 0);
            Date allocationTime = c.getTime();

            if (freeSiteCodes.getList().size() != amount) {
                throw new ServiceException("Did not find enough free site codes for allocating " + amount + " sites!");
            }
            siteCodeDao.allocateSiteCodes(freeSiteCodes.getList(), countryCode, userName, siteNames, allocationTime);

            AllocationResult result = new AllocationResult();
            result.setAmount(amount);
            result.setUserName(userName);
            result.setAllocationTime(allocationTime);
            return result;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSiteCodeVocabularyFolderId() throws ServiceException {
        try {
            return siteCodeDao.getSiteCodeVocabularyFolderId();
        } catch (Exception e) {
            throw new ServiceException("Failed to get site code folder id: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFeeSiteCodeAmount() throws ServiceException {
        try {
            return siteCodeDao.getFeeSiteCodeAmount();
        } catch (Exception e) {
            throw new ServiceException("Failed to get unallocated site coudes amount: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> getCountryAllocations(List<FixedValue> countries) throws ServiceException {
        try {
            Map<String, Integer> result = new LinkedHashMap<String, Integer>();
            for (FixedValue fv : countries) {
                int amount = siteCodeDao.getCountryAllocations(fv.getValue());
                result.put(fv.getDefinition(), amount);
            }

            return result;
        } catch (Exception e) {
            throw new ServiceException("Failed to get country allocations: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportSiteCodes(SiteCodeFilter filter, OutputStream os) throws ServiceException {
        try {
            siteCodeDao.exportSiteCodes(filter, os);
        } catch (Exception e) {
            throw new ServiceException("Failed to get export site codes CSV: " + e.getMessage(), e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
