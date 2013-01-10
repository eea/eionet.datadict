package eionet.meta.service;

import java.util.List;

import eionet.meta.DDUser;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

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
     * Returns countries that are allocatable by user. If user has update permissions on site codes,
     * then all countries will be returned.
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
    void allocateSiteCodes(String country, int amount, String userName) throws ServiceException;

    /**
     * Allocates available site codes to given country.
     *
     * @param country
     * @param siteCodeNames
     * @param userName
     * @throws ServiceException
     */
    void allocateSiteCodes(String country, String[] siteCodeNames, String userName) throws ServiceException;

    /**
     * Searches site codes.
     *
     * @param filter
     * @return
     * @throws ServiceException
     */
    SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ServiceException;
}
