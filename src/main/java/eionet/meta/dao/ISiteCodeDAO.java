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
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.meta.dao;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.SiteCode;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site code DAO interface.
 *
 * @author Enriko Käsper
 */
public interface ISiteCodeDAO {

    /**
     * Search site codes from database using SiteCodeFilter.
     *
     * @param filter
     * @return SiteCodeResult object with found rows.
     */
    SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ParseException;

    /**
     * @param vocabularyConcepts
     * @param userName
     */
    void insertUserAndDateCreatedForSiteCodes(List<VocabularyConcept> vocabularyConcepts, String userName) throws Exception;

    /**
     * Allocates the given site codes for country. If Site names are provided, then this information is stored as for information.
     *
     * @param freeSiteCodes
     *            The list of free site code objects that will be allocated.
     * @param countryCode
     *            The allocating country.
     * @param userName
     *            User who started the allocation.
     * @param siteNames
     *            Optional list of site names.
     *
     * @param allocationTime
     *            allocation time
     */
    void allocateSiteCodes(List<SiteCode> freeSiteCodes, String countryCode, String userName, String[] siteNames,
                           Date allocationTime);

    /**
     * Returns the first vocabulary folder Id where type is SITE_CODE.
     *
     * @return
     */
    int getSiteCodeVocabularyFolderId();

    /**
     * Returns number of available free unallocated site codes.
     *
     * @return
     */
    int getFeeSiteCodeAmount();

    /**
     * Returns number of site codes in status: allocated.
     *
     * @param countryCode
     * @param withoutInitialName
     * @return
     */
    int getCountryUnusedAllocations(String countryCode, boolean withoutInitialName);

    /**
     * Returns number of site codes in status: assigned, deleted, disappeared.
     *
     * @param countryCode
     * @return
     */
    int getCountryUsedAllocations(String countryCode);

    /**
     * True, if there already is existing vocabulary folder with type SITE_CODE;
     *
     * @return
     */
    boolean siteCodeFolderExists();

    /**
     * Returns a list of SiteCode objects which contains information for the codes.
     *
     * @param vcId the site code id
     * @param dataElementIds
     * @return a hashmap with key the element's id and value, the element's value
     */
    Map<Integer, String> getBoundElementIdAndValue(Integer vcId,List<Integer> dataElementIds);

    /**
     * Creates the query for site codes and retrieves the site code list
     *
     * @param filter filtering
     * @param elementMap map for elements' identifier and id
     * @return a list of site codes
     */
    List<SiteCode> createQueryAndRetrieveSiteCodes(SiteCodeFilter filter, Map<String, Integer> elementMap);

    /**
     * Executes a query and returns a site code list
     *
     * @param query the sql query
     * @param params the parameters for the query
     * @param elementMap map for elements' identifier and id
     * @return a list of site codes
     */
    List<SiteCode> getSiteCodeList(String query, Map<String, Object> params, Map<String, Integer> elementMap);
}
