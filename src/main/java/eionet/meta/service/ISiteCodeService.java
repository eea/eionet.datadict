package eionet.meta.service;

import eionet.meta.DDUser;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.AllocationResult;
import eionet.meta.service.data.CountryAllocations;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

import java.util.List;

/**
 * Services for site codes.
 *
 * @author Juhan Voolaid
 */
public interface ISiteCodeService {

    /**
     * Returns all site code countries.
     *
     * @return
     * @throws ServiceException
     */
    List<FixedValue> getAllCountries() throws ServiceException;

    /**
     * Returns countries that are allocatable by user. If user has update permissions on site codes, then all countries will be
     * returned.
     *
     * @return
     * @throws ServiceException
     */
    List<FixedValue> getUserCountries(DDUser user) throws ServiceException;

    /**
     * Allocates available site codes to given country.
     *
     * @param country
     * @param amount
     * @param userName
     * @throws ServiceException
     */
    AllocationResult allocateSiteCodes(String country, int amount, String userName) throws ServiceException;

    /**
     * Allocates available site codes to given country.
     *
     * @param country
     * @param siteCodeNames
     * @param userName
     * @throws ServiceException
     */
    AllocationResult allocateSiteCodes(String country, String[] siteCodeNames, String userName) throws ServiceException;

    /**
     * Searches site codes.
     *
     * @param filter
     * @return
     * @throws ServiceException
     */
    SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ServiceException;

    /**
     * Returns the first vocabulary folder Id where type is SITE_CODE.
     *
     * @return
     * @throws ServiceException
     */
    int getSiteCodeVocabularyFolderId() throws ServiceException;

    /**
     * Returns number of available free unallocated site codes.
     *
     * @return
     * @throws ServiceException
     */
    int getFeeSiteCodeAmount() throws ServiceException;

    /**
     * Returns number of allocated site codes per country.
     *
     * @param countries
     * @return
     * @throws ServiceException
     */
    List<CountryAllocations> getCountryAllocations(List<FixedValue> countries) throws ServiceException;

}
