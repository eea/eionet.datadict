package eionet.meta.dao;

import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.DBUnitHelper;
import eionet.meta.service.data.SiteCode;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;
import eionet.util.Pair;
import org.displaytag.properties.SortOrderEnum;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import eionet.datadict.model.enums.Enumerations.SiteCodeBoundElementIdentifiers;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringApplicationContext("mock-spring-context.xml")
public class SiteCodeDAOTestIT extends UnitilsJUnit4 {

    @SpringBeanByType
    private ISiteCodeDAO siteCodeDAO;

    @SpringBeanByType
    private IVocabularyConceptDAO vocabularyConceptDAO;

    /* Test case: The site code vocabulary exists */
    @Test
    public void testSiteCodeFolderExistsTrue() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-exists.xml");
        assertThat(siteCodeDAO.siteCodeFolderExists(), is(true));
    }

    /* Test case: The site code vocabulary does not exist */
    @Test
    public void testSiteCodeFolderExistsFalse() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        assertThat(siteCodeDAO.siteCodeFolderExists(), is(false));
    }

    /* Test case: There are no allocations for a specific country */
    @Test
    public void testGetCountryUsedAllocationsNoneAllocated() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(0));
    }

    /* Test case: There are no records for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(0));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test(expected = Exception.class)
    public void testGetCountryUsedAllocationsCountryNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        siteCodeDAO.getCountryUsedAllocations("testCountryCode");
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(2));
    }

    /* Test case: There are no allocations for a specific country */
    @Test
    public void testGetCountryUnusedAllocationsNoneAllocated() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(0));
    }

    /* Test case: There are no records for a specific country */
    @Test
    public void testGetCountryUnusedAllocationsCountryNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(0));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test(expected = Exception.class)
    public void testGetCountryUnusedAllocationsCountryNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false);
    }

    /* Test case: Search for pairs with country code: otherCountryCode, status: allocated but the site name element must be empty or null (there are no rows for otherCountryCode with site name element)*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsNotExistWithoutInitialName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        assertThat(siteCodeDAO.getCountryUnusedAllocations("otherCountryCode", true), is(1));
    }

    /* Test case: Search for pairs with country code: testCountryCode, status: allocated but the site name element must be empty or null*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsExistWithoutInitialName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", true), is(2));
    }

    /* Test case: Search for pairs with country code: testCountryCode, status: allocated and do not take into consideration if there are initial site names elements*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsExistWithInitialName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(3));
    }

    /* Test case: Search for site codes with status available when no such records exist*/
    @Test
    public void testGetFeeSiteCodeAmountNoRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: Search for site codes with status available when site code folder does not exist*/
    @Test
    public void testGetFeeSiteCodeAmountNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: Search for site codes with status available when records exist*/
    @Test
    public void testGetFeeSiteCodeAmountRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(2));
    }

    /* Test case: The vocabulary folder for site codes doesn't exist*/
    @Test
    public void testGetSiteCodeVocabularyFolderIdNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: The vocabulary folder for site codes exists*/
    @Test
    public void testGetSiteCodeVocabularyFolderIdExistss() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(2));
    }

    /* Test case: The vocabulary concepts and the data elements exist*/
    @Test
    public void testInsertAvailableSiteCodesConceptsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> conceptIds = new ArrayList<>();
        conceptIds.add(1);
        conceptIds.add(4);
        conceptIds.add(5);
        List<VocabularyConcept> vocabularyConcepts = vocabularyConceptDAO.getVocabularyConcepts(conceptIds);
        String testUser = "test_user";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        String testDateStr = formatter.format(new Date());
        siteCodeDAO.insertAvailableSiteCodes(vocabularyConcepts, testUser);

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(4);
        boundElementIds.add(5);
        for (Integer conceptId: conceptIds){
            Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(conceptId, boundElementIds);
            assertThat(elementMap.size(), is(2));
            //   assertThat(elementMap.get(4), is(testDateStr));    //this may need to be commented because there could be differences in the seconds.
            assertThat(elementMap.get(5), is(testUser));
        }
    }

    /* Test case: The dataElements don't exist*/
    @Test(expected = Exception.class)
    public void testInsertAvailableSiteCodesDataElementsDontExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        List<Integer> conceptIds = new ArrayList<>();
        conceptIds.add(1);
        conceptIds.add(2);
        conceptIds.add(4);
        List<VocabularyConcept> vocabularyConcepts = vocabularyConceptDAO.getVocabularyConcepts(conceptIds);
        String testUser = "test_user";
        siteCodeDAO.insertAvailableSiteCodes(vocabularyConcepts, testUser);
    }

    /* Test case: The data elements and the concept exist*/
    @Test
    public void testGetBoundElementIdAndValueValuesExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(1);
        boundElementIds.add(2);
        boundElementIds.add(3);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(6, boundElementIds);
        assertThat(elementMap.size(), is(3));
        assertThat(elementMap.get(1), is("http://dd.eionet.europa.eu/vocabulary/common/countries/testCountryCode"));
        assertThat(elementMap.get(2), is("ALLOCATED"));
        assertThat(elementMap.get(3), is(nullValue()));
    }

    /* Test case: The concept doesn't exist*/
    @Test
    public void testGetBoundElementIdAndValueConceptNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(1);
        boundElementIds.add(2);
        boundElementIds.add(3);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(10, boundElementIds);
        assertThat(elementMap.size(), is(0));
    }

    /* Test case: The data elements exist but are not set for the concept*/
    @Test
    public void testGetBoundElementIdAndValueDataElementWithoutValue() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(4);
        boundElementIds.add(5);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        assertThat(elementMap.size(), is(0));
    }

    /* Test case: The data elements don't exist */
    @Test
    public void testGetBoundElementIdAndValueDataElementsDontExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(7);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        assertThat(elementMap.size(), is(0));
    }

    /* Test case: The element map is null or empty */
    @Test
    public void testGetSiteCodeListNullOrEmptyElementMap() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");

        String query = "select vc.* from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID where v.VOCABULARY_TYPE = :siteCodeType" +
                "  order by  vc.VOCABULARY_CONCEPT_ID limit 3 ";
        Map<String, Object> params = new HashMap<>();
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        List<SiteCode> scListElemNull =  siteCodeDAO.getSiteCodeList(query, params,null);
        assertThat(scListElemNull.size(), is(3));
        assertThat(scListElemNull.get(0).getId(), is(1));
        assertThat(scListElemNull.get(0).getIdentifier(), is("C"));
        assertThat(scListElemNull.get(0).getLabel(), is("test1"));
        assertThat(scListElemNull.get(0).getDefinition(), is("test1"));
        assertThat(scListElemNull.get(0).getNotation(), is("test1"));
        assertThat(scListElemNull.get(0).getCountryCode(), is(nullValue()));
        assertThat(scListElemNull.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemNull.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemNull.get(0).getDateAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(0).getDateCreated(), is(nullValue()));
        assertThat(scListElemNull.get(0).getUserAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(0).getUserCreated(), is(nullValue()));
        assertThat(scListElemNull.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemNull.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemNull.get(0).getStatus(), is(nullValue()));

        assertThat(scListElemNull.get(1).getId(), is(2));
        assertThat(scListElemNull.get(1).getIdentifier(), is("L"));
        assertThat(scListElemNull.get(1).getLabel(), is("test2"));
        assertThat(scListElemNull.get(1).getDefinition(), is("test2"));
        assertThat(scListElemNull.get(1).getNotation(), is("test2"));
        assertThat(scListElemNull.get(1).getCountryCode(), is(nullValue()));
        assertThat(scListElemNull.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemNull.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemNull.get(1).getDateAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(1).getDateCreated(), is(nullValue()));
        assertThat(scListElemNull.get(1).getUserAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(1).getUserCreated(), is(nullValue()));
        assertThat(scListElemNull.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemNull.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemNull.get(1).getStatus(), is(nullValue()));

        assertThat(scListElemNull.get(2).getId(), is(3));
        assertThat(scListElemNull.get(2).getIdentifier(), is("R"));
        assertThat(scListElemNull.get(2).getLabel(), is("test3"));
        assertThat(scListElemNull.get(2).getDefinition(), is("test3"));
        assertThat(scListElemNull.get(2).getNotation(), is("test3"));
        assertThat(scListElemNull.get(2).getCountryCode(), is(nullValue()));
        assertThat(scListElemNull.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemNull.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemNull.get(2).getDateAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(2).getDateCreated(), is(nullValue()));
        assertThat(scListElemNull.get(2).getUserAllocated(), is(nullValue()));
        assertThat(scListElemNull.get(2).getUserCreated(), is(nullValue()));
        assertThat(scListElemNull.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemNull.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemNull.get(2).getStatus(), is(nullValue()));


        Map<String, Integer> boundElementsMap = new HashMap<>();
        List<SiteCode> scListElemEmpty =  siteCodeDAO.getSiteCodeList(query, params, boundElementsMap);
        assertThat(scListElemEmpty.size(), is(3));
        assertThat(scListElemEmpty.get(0).getId(), is(1));
        assertThat(scListElemEmpty.get(0).getIdentifier(), is("C"));
        assertThat(scListElemEmpty.get(0).getLabel(), is("test1"));
        assertThat(scListElemEmpty.get(0).getDefinition(), is("test1"));
        assertThat(scListElemEmpty.get(0).getNotation(), is("test1"));
        assertThat(scListElemEmpty.get(0).getCountryCode(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemEmpty.get(0).getDateAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getDateCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getUserAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getUserCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemEmpty.get(0).getStatus(), is(nullValue()));

        assertThat(scListElemEmpty.get(1).getId(), is(2));
        assertThat(scListElemEmpty.get(1).getIdentifier(), is("L"));
        assertThat(scListElemEmpty.get(1).getLabel(), is("test2"));
        assertThat(scListElemEmpty.get(1).getDefinition(), is("test2"));
        assertThat(scListElemEmpty.get(1).getNotation(), is("test2"));
        assertThat(scListElemEmpty.get(1).getCountryCode(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemEmpty.get(1).getDateAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getDateCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getUserAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getUserCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemEmpty.get(1).getStatus(), is(nullValue()));

        assertThat(scListElemEmpty.get(2).getId(), is(3));
        assertThat(scListElemEmpty.get(2).getIdentifier(), is("R"));
        assertThat(scListElemEmpty.get(2).getLabel(), is("test3"));
        assertThat(scListElemEmpty.get(2).getDefinition(), is("test3"));
        assertThat(scListElemEmpty.get(2).getNotation(), is("test3"));
        assertThat(scListElemEmpty.get(2).getCountryCode(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(scListElemEmpty.get(2).getDateAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getDateCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getUserAllocated(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getUserCreated(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(scListElemEmpty.get(2).getStatus(), is(nullValue()));
    }

    /* Test case: There are concepts and elements in the map */
    @Test
    public void testGetSiteCodeListConceptsAndElementsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");

        Map<String, Integer> boundElementsMap = new HashMap<>();
        boundElementsMap.put(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier(), 1);
        boundElementsMap.put(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier(), 2);

        String query = "select vc.* from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID " +
                "  inner join VOCABULARY_CONCEPT_ELEMENT vce4 on vc.VOCABULARY_CONCEPT_ID=vce4.VOCABULARY_CONCEPT_ID " +
                " inner join VOCABULARY_CONCEPT_ELEMENT vce3 on vc.VOCABULARY_CONCEPT_ID=vce3.VOCABULARY_CONCEPT_ID " +
                " where v.VOCABULARY_TYPE = :siteCodeType " +
                " and vce4.DATAELEM_ID = :countryCodeElemId and vce4.ELEMENT_VALUE = :countryCode " +
                " and vce3.DATAELEM_ID = :statusElemId and vce3.ELEMENT_VALUE = :status " +
                " order by vc.VOCABULARY_CONCEPT_ID limit 3 ";
        Map<String, Object> params = new HashMap<>();
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("countryCode", "http://dd.eionet.europa.eu/vocabulary/common/countries/testCountryCode");
        params.put("status", "ALLOCATED");
        params.put("countryCodeElemId", 1);
        params.put("statusElemId", 2);

        List<SiteCode> scList =  siteCodeDAO.getSiteCodeList(query, params, boundElementsMap);
        assertThat(scList.size(), is(3));
        assertThat(scList.get(0).getId(), is(1));
        assertThat(scList.get(0).getIdentifier(), is("C"));
        assertThat(scList.get(0).getLabel(), is("test1"));
        assertThat(scList.get(0).getDefinition(), is("test1"));
        assertThat(scList.get(0).getNotation(), is("test1"));
        assertThat(scList.get(0).getCountryCode(), is("testCountryCode"));
        assertThat(scList.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(scList.get(0).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        assertThat(scList.get(0).getDateAllocated(), is(nullValue()));
        assertThat(scList.get(0).getDateCreated(), is(nullValue()));
        assertThat(scList.get(0).getUserAllocated(), is(nullValue()));
        assertThat(scList.get(0).getUserCreated(), is(nullValue()));
        assertThat(scList.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(scList.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(scList.get(0).getStatus(), is(nullValue()));

        assertThat(scList.get(1).getId(), is(2));
        assertThat(scList.get(1).getIdentifier(), is("L"));
        assertThat(scList.get(1).getLabel(), is("test2"));
        assertThat(scList.get(1).getDefinition(), is("test2"));
        assertThat(scList.get(1).getNotation(), is("test2"));
        assertThat(scList.get(1).getCountryCode(), is("testCountryCode"));
        assertThat(scList.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(scList.get(1).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        assertThat(scList.get(1).getDateAllocated(), is(nullValue()));
        assertThat(scList.get(1).getDateCreated(), is(nullValue()));
        assertThat(scList.get(1).getUserAllocated(), is(nullValue()));
        assertThat(scList.get(1).getUserCreated(), is(nullValue()));
        assertThat(scList.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(scList.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(scList.get(1).getStatus(), is(nullValue()));

        assertThat(scList.get(2).getId(), is(6));
        assertThat(scList.get(2).getIdentifier(), is("Q"));
        assertThat(scList.get(2).getLabel(), is("test6"));
        assertThat(scList.get(2).getDefinition(), is("test6"));
        assertThat(scList.get(2).getNotation(), is("test6"));
        assertThat(scList.get(2).getCountryCode(), is("testCountryCode"));
        assertThat(scList.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(scList.get(2).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        assertThat(scList.get(2).getDateAllocated(), is(nullValue()));
        assertThat(scList.get(2).getDateCreated(), is(nullValue()));
        assertThat(scList.get(2).getUserAllocated(), is(nullValue()));
        assertThat(scList.get(2).getUserCreated(), is(nullValue()));
        assertThat(scList.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(scList.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(scList.get(2).getStatus(), is(nullValue()));

    }

    /* Test case: the site code list is null */
    @Test(expected = Exception.class)
    public void testAllocateSiteCodesSiteCodesDontExist() throws Exception {
        String username = "test_user";
        String countryCode = "testCountryCode";
        String[] siteNames = new String[3];
        siteNames[0] = "a";
        siteNames[1] = "b";
        siteNames[2] = "c";
        Date allocationTime = new Date();
        siteCodeDAO.allocateSiteCodes(null, countryCode, username, siteNames, allocationTime);
    }

    /* Test case: the site code list is empty*/
    @Test(expected = Exception.class)
    public void testAllocateSiteCodesEmptySiteCodes() throws Exception {
        String username = "test_user";
        String countryCode = "testCountryCode";
        String[] siteNames = new String[3];
        siteNames[0] = "a";
        siteNames[1] = "b";
        siteNames[2] = "c";
        Date allocationTime = new Date();
        List<SiteCode> scList = new ArrayList<>();
        siteCodeDAO.allocateSiteCodes(scList, countryCode, username, siteNames, allocationTime);
    }

    /* Test case: Data elements don't exist*/
    @Test(expected = Exception.class)
    public void testAllocateSiteCodesDataElementDoesntExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        String username = "test_user";
        String countryCode = "testCountryCode";
        String[] siteNames = new String[3];
        siteNames[0] = "a";
        siteNames[1] = "b";
        siteNames[2] = "c";
        Date allocationTime = new Date();
        siteCodeDAO.allocateSiteCodes(null, countryCode, username, siteNames, allocationTime);
    }


    /* Test case: successful allocation */
    @Test
    public void testAllocateSiteCodesSiteCodesesAndElementsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        String username = "test_user";
        String countryCode = "testCountryCode";
        String countryBaseUrl = "http://dd.eionet.europa.eu/vocabulary/common/countries/";
        String[] siteNames = new String[3];
        siteNames[0] = "a";
        siteNames[1] = "b";
        siteNames[2] = "c";
        Date allocationTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        String expectedTime = formatter.format(allocationTime);

        List<SiteCode> scList = new ArrayList<>();
        SiteCode sc1 = new SiteCode();
        sc1.setId(1);
        SiteCode sc2 = new SiteCode();
        sc2.setId(2);
        SiteCode sc3 = new SiteCode();
        sc3.setId(5);
        scList.add(sc1);
        scList.add(sc2);
        scList.add(sc3);
        siteCodeDAO.allocateSiteCodes(scList, countryCode, username, siteNames, allocationTime);

        //expected time may need to be commented because there could be differences in the seconds.

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(1);
        boundElementIds.add(2);
        boundElementIds.add(3);
        boundElementIds.add(4);
        boundElementIds.add(5);
        boundElementIds.add(6);
        boundElementIds.add(7);

        Map<Integer, String> elementMap1 = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        assertThat(elementMap1.size(), is(5));
        assertThat(elementMap1.get(1), is(nullValue()));
        assertThat(elementMap1.get(2), is(SiteCodeStatus.ALLOCATED.name()));
        assertThat(elementMap1.get(3), is("a"));
        //assertThat(elementMap1.get(6), is(expectedTime));
        assertThat(elementMap1.get(7), is(username));

        Map<Integer, String> elementMap2 = siteCodeDAO.getBoundElementIdAndValue(2, boundElementIds);
        assertThat(elementMap2.size(), is(4));
        assertThat(elementMap2.get(1), is(nullValue()));
        assertThat(elementMap2.get(3), is("b"));
        //assertThat(elementMap2.get(6), is(expectedTime));
        assertThat(elementMap2.get(7), is(username));

        Map<Integer, String> elementMap3 = siteCodeDAO.getBoundElementIdAndValue(5, boundElementIds);
        assertThat(elementMap3.size(), is(4));
        assertThat(elementMap3.get(1), is(nullValue()));
        assertThat(elementMap3.get(3), is("c"));
       //assertThat(elementMap3.get(6), is(expectedTime));
        assertThat(elementMap3.get(7), is(username));

    }

    /* Test case: successful allocation but with only one site name*/
    @Test
    public void testAllocateSiteCodesOneSiteName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        String username = "test_user";
        String countryCode = "testCountryCode";
        String countryBaseUrl = "http://dd.eionet.europa.eu/vocabulary/common/countries/";
        String[] siteNames = new String[1];
        siteNames[0] = "a";
        Date allocationTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        //expectedTime may need to be commented because there could be differences in the seconds.
        String expectedTime = formatter.format(allocationTime);

        List<SiteCode> scList = new ArrayList<>();
        SiteCode sc1 = new SiteCode();
        sc1.setId(1);
        SiteCode sc2 = new SiteCode();
        sc2.setId(2);
        SiteCode sc3 = new SiteCode();
        sc3.setId(5);
        scList.add(sc1);
        scList.add(sc2);
        scList.add(sc3);
        siteCodeDAO.allocateSiteCodes(scList, countryCode, username, siteNames, allocationTime);

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(1);
        boundElementIds.add(2);
        boundElementIds.add(3);
        boundElementIds.add(4);
        boundElementIds.add(5);
        boundElementIds.add(6);
        boundElementIds.add(7);

        Map<Integer, String> elementMap1 = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        assertThat(elementMap1.size(), is(5));
        assertThat(elementMap1.get(1), is(nullValue()));
        assertThat(elementMap1.get(2), is(SiteCodeStatus.ALLOCATED.name()));
        assertThat(elementMap1.get(3), is("a"));
        //assertThat(elementMap1.get(6), is(expectedTime));
        assertThat(elementMap1.get(7), is(username));

        Map<Integer, String> elementMap2 = siteCodeDAO.getBoundElementIdAndValue(2, boundElementIds);
        assertThat(elementMap2.size(), is(4));
        assertThat(elementMap2.get(1), is(nullValue()));
        assertThat(elementMap2.get(3), is(""));
        //assertThat(elementMap2.get(6), is(expectedTime));
        assertThat(elementMap2.get(7), is(username));

        Map<Integer, String> elementMap3 = siteCodeDAO.getBoundElementIdAndValue(5, boundElementIds);
        assertThat(elementMap3.size(), is(4));
        assertThat(elementMap3.get(1), is(nullValue()));
        assertThat(elementMap3.get(3), is(""));
        //assertThat(elementMap3.get(6), is(expectedTime));
        assertThat(elementMap3.get(7), is(username));

    }

    /* Test case: the filter is null*/
    @Test(expected = Exception.class)
    public void testSearchSiteCodesNullFilter() throws Exception {
        siteCodeDAO.searchSiteCodes(null);
    }

    /* Test case: search for available site codes*/
    @Test
    public void testSearchSiteCodesForAllocation() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-search.xml");
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setPageNumber(1);
        siteCodeFilter.setUsePaging(false);
        siteCodeFilter.setStatus(SiteCodeStatus.AVAILABLE);
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");

        SiteCodeResult result = siteCodeDAO.searchSiteCodes(siteCodeFilter);
        assertThat(result.getTotalItems(), is(3));
        assertThat(result.getList().size(), is(3));

        assertThat(result.getList().get(0).getId(), is(2));
        assertThat(result.getList().get(0).getIdentifier(), is("L"));
        assertThat(result.getList().get(0).getLabel(), is("test2"));
        assertThat(result.getList().get(0).getDefinition(), is("test2"));
        assertThat(result.getList().get(0).getNotation(), is("test2"));
        assertThat(result.getList().get(0).getCountryCode(), is("testCountryCode"));
        assertThat(result.getList().get(0).getInitialSiteName(), is("site name"));
        assertThat(result.getList().get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.getList().get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(0).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(0).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(0).getStatus(), is(nullValue()));

        assertThat(result.getList().get(1).getId(), is(6));
        assertThat(result.getList().get(1).getIdentifier(), is("Q"));
        assertThat(result.getList().get(1).getLabel(), is("test6"));
        assertThat(result.getList().get(1).getDefinition(), is("test6"));
        assertThat(result.getList().get(1).getNotation(), is("test6"));
        assertThat(result.getList().get(1).getCountryCode(), is("testCountryCode"));
        assertThat(result.getList().get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.getList().get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(1).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(1).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(1).getStatus(), is(nullValue()));

        assertThat(result.getList().get(2).getId(), is(7));
        assertThat(result.getList().get(2).getIdentifier(), is("K"));
        assertThat(result.getList().get(2).getLabel(), is("test7"));
        assertThat(result.getList().get(2).getDefinition(), is("test7"));
        assertThat(result.getList().get(2).getNotation(), is("test7"));
        assertThat(result.getList().get(2).getCountryCode(), is(nullValue()));
        assertThat(result.getList().get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.getList().get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(2).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(2).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(2).getStatus(), is(nullValue()));

    }

    /* Test case: retrieve all site codes*/
    @Test
    public void testSearchSiteCodesGetAll() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");

        SiteCodeResult result = siteCodeDAO.searchSiteCodes(siteCodeFilter);
        assertThat(result.getTotalItems(), is(4));
        assertThat(result.getList().size(), is(4));

        assertThat(result.getList().get(0).getId(), is(1));
        assertThat(result.getList().get(0).getIdentifier(), is("C"));
        assertThat(result.getList().get(0).getLabel(), is("test"));
        assertThat(result.getList().get(0).getDefinition(), is("test"));
        assertThat(result.getList().get(0).getNotation(), is("test"));
        assertThat(result.getList().get(0).getCountryCode(), is(nullValue()));
        assertThat(result.getList().get(0).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.getList().get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(0).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(0).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(0).getStatus(), is(nullValue()));

        assertThat(result.getList().get(1).getId(), is(2));
        assertThat(result.getList().get(1).getIdentifier(), is("L"));
        assertThat(result.getList().get(1).getLabel(), is("test"));
        assertThat(result.getList().get(1).getDefinition(), is("test"));
        assertThat(result.getList().get(1).getNotation(), is("test"));
        assertThat(result.getList().get(1).getCountryCode(), is("country1"));
        assertThat(result.getList().get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(1).getSiteCodeStatus(), is(nullValue()));
        assertThat(result.getList().get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(1).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(1).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(1).getStatus(), is(nullValue()));

        assertThat(result.getList().get(2).getId(), is(3));
        assertThat(result.getList().get(2).getIdentifier(), is("R"));
        assertThat(result.getList().get(2).getLabel(), is("test"));
        assertThat(result.getList().get(2).getDefinition(), is("test"));
        assertThat(result.getList().get(2).getNotation(), is("test"));
        assertThat(result.getList().get(2).getCountryCode(), is("country2"));
        assertThat(result.getList().get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(2).getSiteCodeStatus(), is(SiteCodeStatus.ASSIGNED));
        assertThat(result.getList().get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(2).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(2).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(2).getStatus(), is(nullValue()));

        assertThat(result.getList().get(3).getId(), is(4));
        assertThat(result.getList().get(3).getIdentifier(), is("T"));
        assertThat(result.getList().get(3).getLabel(), is("test"));
        assertThat(result.getList().get(3).getDefinition(), is("test"));
        assertThat(result.getList().get(3).getNotation(), is("test"));
        assertThat(result.getList().get(3).getCountryCode(), is("country3"));
        assertThat(result.getList().get(3).getInitialSiteName(), is(nullValue()));
        assertThat(result.getList().get(3).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.getList().get(3).getDateAllocated(), is(nullValue()));
        assertThat(result.getList().get(3).getDateCreated(), is(nullValue()));
        assertThat(result.getList().get(3).getUserAllocated(), is(nullValue()));
        assertThat(result.getList().get(3).getUserCreated(), is(nullValue()));
        assertThat(result.getList().get(3).getYearsDeleted(), is(nullValue()));
        assertThat(result.getList().get(3).getYearsDisappeared(), is(nullValue()));
        assertThat(result.getList().get(3).getStatus(), is(nullValue()));
    }

    /* Test case: the filter is null*/
    @Test(expected = Exception.class)
    public void testCreateQueryAndRetrieveSiteCodesNullFilter() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-search.xml");
        Map<String, Integer> elementMap = new HashMap<>();
        elementMap.put("sitecodes_CC_ISO2", 1);
        elementMap.put("sitecodes_STATUS", 2);
        elementMap.put("sitecodes_INITIAL_SITE_NAME", 3);
        elementMap.put("sitecodes_DATE_CREATED", 4);
        elementMap.put("sitecodes_USER_CREATED", 5);
        siteCodeDAO.createQueryAndRetrieveSiteCodes(null, elementMap);
    }

    /* Test case: the element map is null*/
    @Test
    public void testCreateQueryAndRetrieveSiteCodesNullHashMap() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");
        Pair<Integer, List<SiteCode>> scPair = siteCodeDAO.createQueryAndRetrieveSiteCodes(siteCodeFilter, null);

        List<SiteCode> result = scPair.getRight();

        assertThat(result.size(), is(4));
        assertThat(scPair.getLeft(), is(4));

        assertThat(result.get(0).getId(), is(1));
        assertThat(result.get(0).getIdentifier(), is("C"));
        assertThat(result.get(0).getLabel(), is("test"));
        assertThat(result.get(0).getDefinition(), is("test"));
        assertThat(result.get(0).getNotation(), is("test"));
        assertThat(result.get(0).getCountryCode(), is(nullValue()));
        assertThat(result.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.get(0).getDateCreated(), is(nullValue()));
        assertThat(result.get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.get(0).getUserCreated(), is(nullValue()));
        assertThat(result.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(0).getStatus(), is(nullValue()));

        assertThat(result.get(1).getId(), is(2));
        assertThat(result.get(1).getIdentifier(), is("L"));
        assertThat(result.get(1).getLabel(), is("test"));
        assertThat(result.get(1).getDefinition(), is("test"));
        assertThat(result.get(1).getNotation(), is("test"));
        assertThat(result.get(1).getCountryCode(), is(nullValue()));
        assertThat(result.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.get(1).getDateCreated(), is(nullValue()));
        assertThat(result.get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.get(1).getUserCreated(), is(nullValue()));
        assertThat(result.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(1).getStatus(), is(nullValue()));

        assertThat(result.get(2).getId(), is(3));
        assertThat(result.get(2).getIdentifier(), is("R"));
        assertThat(result.get(2).getLabel(), is("test"));
        assertThat(result.get(2).getDefinition(), is("test"));
        assertThat(result.get(2).getNotation(), is("test"));
        assertThat(result.get(2).getCountryCode(), is(nullValue()));
        assertThat(result.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.get(2).getDateCreated(), is(nullValue()));
        assertThat(result.get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.get(2).getUserCreated(), is(nullValue()));
        assertThat(result.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(2).getStatus(), is(nullValue()));

        assertThat(result.get(3).getId(), is(4));
        assertThat(result.get(3).getIdentifier(), is("T"));
        assertThat(result.get(3).getLabel(), is("test"));
        assertThat(result.get(3).getDefinition(), is("test"));
        assertThat(result.get(3).getNotation(), is("test"));
        assertThat(result.get(3).getCountryCode(), is(nullValue()));
        assertThat(result.get(3).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(3).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(3).getDateAllocated(), is(nullValue()));
        assertThat(result.get(3).getDateCreated(), is(nullValue()));
        assertThat(result.get(3).getUserAllocated(), is(nullValue()));
        assertThat(result.get(3).getUserCreated(), is(nullValue()));
        assertThat(result.get(3).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(3).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(3).getStatus(), is(nullValue()));
    }

    /* Test case: the element map is empty but filter is not used for other than sorting*/
    @Test
    public void testCreateQueryAndRetrieveSiteCodesEmptyHashMap() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        Map<String, Integer> elementMap = new HashMap<>();
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");
        Pair<Integer, List<SiteCode>> scPair = siteCodeDAO.createQueryAndRetrieveSiteCodes(siteCodeFilter, elementMap);

        List<SiteCode> result = scPair.getRight();

        assertThat(result.size(), is(4));
        assertThat(scPair.getLeft(), is(4));

        assertThat(result.get(0).getId(), is(1));
        assertThat(result.get(0).getIdentifier(), is("C"));
        assertThat(result.get(0).getLabel(), is("test"));
        assertThat(result.get(0).getDefinition(), is("test"));
        assertThat(result.get(0).getNotation(), is("test"));
        assertThat(result.get(0).getCountryCode(), is(nullValue()));
        assertThat(result.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.get(0).getDateCreated(), is(nullValue()));
        assertThat(result.get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.get(0).getUserCreated(), is(nullValue()));
        assertThat(result.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(0).getStatus(), is(nullValue()));

        assertThat(result.get(1).getId(), is(2));
        assertThat(result.get(1).getIdentifier(), is("L"));
        assertThat(result.get(1).getLabel(), is("test"));
        assertThat(result.get(1).getDefinition(), is("test"));
        assertThat(result.get(1).getNotation(), is("test"));
        assertThat(result.get(1).getCountryCode(), is(nullValue()));
        assertThat(result.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.get(1).getDateCreated(), is(nullValue()));
        assertThat(result.get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.get(1).getUserCreated(), is(nullValue()));
        assertThat(result.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(1).getStatus(), is(nullValue()));

        assertThat(result.get(2).getId(), is(3));
        assertThat(result.get(2).getIdentifier(), is("R"));
        assertThat(result.get(2).getLabel(), is("test"));
        assertThat(result.get(2).getDefinition(), is("test"));
        assertThat(result.get(2).getNotation(), is("test"));
        assertThat(result.get(2).getCountryCode(), is(nullValue()));
        assertThat(result.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.get(2).getDateCreated(), is(nullValue()));
        assertThat(result.get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.get(2).getUserCreated(), is(nullValue()));
        assertThat(result.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(2).getStatus(), is(nullValue()));

        assertThat(result.get(3).getId(), is(4));
        assertThat(result.get(3).getIdentifier(), is("T"));
        assertThat(result.get(3).getLabel(), is("test"));
        assertThat(result.get(3).getDefinition(), is("test"));
        assertThat(result.get(3).getNotation(), is("test"));
        assertThat(result.get(3).getCountryCode(), is(nullValue()));
        assertThat(result.get(3).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(3).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(3).getDateAllocated(), is(nullValue()));
        assertThat(result.get(3).getDateCreated(), is(nullValue()));
        assertThat(result.get(3).getUserAllocated(), is(nullValue()));
        assertThat(result.get(3).getUserCreated(), is(nullValue()));
        assertThat(result.get(3).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(3).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(3).getStatus(), is(nullValue()));
    }

    /* Test case: the element map is empty and the filter is used*/
    @Test(expected = Exception.class)
    public void testCreateQueryAndRetrieveSiteCodesEmptyHashMapUsedFilter() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        Map<String, Integer> elementMap = new HashMap<>();
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");
        siteCodeFilter.setUserAllocated("test_user");
        siteCodeDAO.createQueryAndRetrieveSiteCodes(siteCodeFilter, elementMap);

    }

    /* Test case: retrieve available site codes*/
    @Test
    public void testCreateQueryAndRetrieveSiteCodesAvailable() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-search.xml");
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setPageNumber(1);
        siteCodeFilter.setUsePaging(false);
        siteCodeFilter.setStatus(SiteCodeStatus.AVAILABLE);
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");

        Map<String, Integer> elementMap = new HashMap<>();
        elementMap.put("sitecodes_CC_ISO2", 1);
        elementMap.put("sitecodes_STATUS", 2);
        elementMap.put("sitecodes_INITIAL_SITE_NAME", 3);
        elementMap.put("sitecodes_DATE_CREATED", 4);
        elementMap.put("sitecodes_USER_CREATED", 5);

        Pair<Integer, List<SiteCode>> scPair = siteCodeDAO.createQueryAndRetrieveSiteCodes(siteCodeFilter, elementMap);
        List<SiteCode> result = scPair.getRight();

        assertThat(result.size(), is(3));
        assertThat(scPair.getLeft(), is(3));

        assertThat(result.get(0).getId(), is(2));
        assertThat(result.get(0).getIdentifier(), is("L"));
        assertThat(result.get(0).getLabel(), is("test2"));
        assertThat(result.get(0).getDefinition(), is("test2"));
        assertThat(result.get(0).getNotation(), is("test2"));
        assertThat(result.get(0).getCountryCode(), is("testCountryCode"));
        assertThat(result.get(0).getInitialSiteName(), is("site name"));
        assertThat(result.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.get(0).getDateCreated(), is(nullValue()));
        assertThat(result.get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.get(0).getUserCreated(), is(nullValue()));
        assertThat(result.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(0).getStatus(), is(nullValue()));

        assertThat(result.get(1).getId(), is(6));
        assertThat(result.get(1).getIdentifier(), is("Q"));
        assertThat(result.get(1).getLabel(), is("test6"));
        assertThat(result.get(1).getDefinition(), is("test6"));
        assertThat(result.get(1).getNotation(), is("test6"));
        assertThat(result.get(1).getCountryCode(), is("testCountryCode"));
        assertThat(result.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.get(1).getDateCreated(), is(nullValue()));
        assertThat(result.get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.get(1).getUserCreated(), is(nullValue()));
        assertThat(result.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(1).getStatus(), is(nullValue()));

        assertThat(result.get(2).getId(), is(7));
        assertThat(result.get(2).getIdentifier(), is("K"));
        assertThat(result.get(2).getLabel(), is("test7"));
        assertThat(result.get(2).getDefinition(), is("test7"));
        assertThat(result.get(2).getNotation(), is("test7"));
        assertThat(result.get(2).getCountryCode(), is(nullValue()));
        assertThat(result.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.get(2).getDateCreated(), is(nullValue()));
        assertThat(result.get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.get(2).getUserCreated(), is(nullValue()));
        assertThat(result.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(2).getStatus(), is(nullValue()));

    }

    /* Test case: retrieve all site codes*/
    @Test
    public void testCreateQueryAndRetrieveSiteCodesGetAll() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        SiteCodeFilter siteCodeFilter = new SiteCodeFilter();
        siteCodeFilter.setSortOrder(SortOrderEnum.ASCENDING);
        siteCodeFilter.setSortProperty("vc.VOCABULARY_CONCEPT_ID");

        Map<String, Integer> elementMap = new HashMap<>();
        elementMap.put("sitecodes_CC_ISO2", 1);
        elementMap.put("sitecodes_STATUS", 2);
        elementMap.put("sitecodes_INITIAL_SITE_NAME", 6);
        elementMap.put("sitecodes_DATE_CREATED", 4);
        elementMap.put("sitecodes_USER_CREATED", 5);

        Pair<Integer, List<SiteCode>> scPair = siteCodeDAO.createQueryAndRetrieveSiteCodes(siteCodeFilter, elementMap);
        List<SiteCode> result = scPair.getRight();

        assertThat(result.size(), is(4));
        assertThat(scPair.getLeft(), is(4));

        assertThat(result.get(0).getId(), is(1));
        assertThat(result.get(0).getIdentifier(), is("C"));
        assertThat(result.get(0).getLabel(), is("test"));
        assertThat(result.get(0).getDefinition(), is("test"));
        assertThat(result.get(0).getNotation(), is("test"));
        assertThat(result.get(0).getCountryCode(), is(nullValue()));
        assertThat(result.get(0).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(0).getDateAllocated(), is(nullValue()));
        assertThat(result.get(0).getDateCreated(), is(nullValue()));
        assertThat(result.get(0).getUserAllocated(), is(nullValue()));
        assertThat(result.get(0).getUserCreated(), is(nullValue()));
        assertThat(result.get(0).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(0).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(0).getStatus(), is(nullValue()));

        assertThat(result.get(1).getId(), is(2));
        assertThat(result.get(1).getIdentifier(), is("L"));
        assertThat(result.get(1).getLabel(), is("test"));
        assertThat(result.get(1).getDefinition(), is("test"));
        assertThat(result.get(1).getNotation(), is("test"));
        assertThat(result.get(1).getCountryCode(), is("country1"));
        assertThat(result.get(1).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(1).getSiteCodeStatus(), is(nullValue()));
        assertThat(result.get(1).getDateAllocated(), is(nullValue()));
        assertThat(result.get(1).getDateCreated(), is(nullValue()));
        assertThat(result.get(1).getUserAllocated(), is(nullValue()));
        assertThat(result.get(1).getUserCreated(), is(nullValue()));
        assertThat(result.get(1).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(1).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(1).getStatus(), is(nullValue()));

        assertThat(result.get(2).getId(), is(3));
        assertThat(result.get(2).getIdentifier(), is("R"));
        assertThat(result.get(2).getLabel(), is("test"));
        assertThat(result.get(2).getDefinition(), is("test"));
        assertThat(result.get(2).getNotation(), is("test"));
        assertThat(result.get(2).getCountryCode(), is("country2"));
        assertThat(result.get(2).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(2).getSiteCodeStatus(), is(SiteCodeStatus.ASSIGNED));
        assertThat(result.get(2).getDateAllocated(), is(nullValue()));
        assertThat(result.get(2).getDateCreated(), is(nullValue()));
        assertThat(result.get(2).getUserAllocated(), is(nullValue()));
        assertThat(result.get(2).getUserCreated(), is(nullValue()));
        assertThat(result.get(2).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(2).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(2).getStatus(), is(nullValue()));

        assertThat(result.get(3).getId(), is(4));
        assertThat(result.get(3).getIdentifier(), is("T"));
        assertThat(result.get(3).getLabel(), is("test"));
        assertThat(result.get(3).getDefinition(), is("test"));
        assertThat(result.get(3).getNotation(), is("test"));
        assertThat(result.get(3).getCountryCode(), is("country3"));
        assertThat(result.get(3).getInitialSiteName(), is(nullValue()));
        assertThat(result.get(3).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        assertThat(result.get(3).getDateAllocated(), is(nullValue()));
        assertThat(result.get(3).getDateCreated(), is(nullValue()));
        assertThat(result.get(3).getUserAllocated(), is(nullValue()));
        assertThat(result.get(3).getUserCreated(), is(nullValue()));
        assertThat(result.get(3).getYearsDeleted(), is(nullValue()));
        assertThat(result.get(3).getYearsDisappeared(), is(nullValue()));
        assertThat(result.get(3).getStatus(), is(nullValue()));
    }

    /* Test case: vocabulary concept id list is null*/
    @Test(expected = Exception.class)
    public void testUpdateSiteCodeStatusNullList() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        siteCodeDAO.updateSiteCodeStatus(null, 2, "TestStatus");
    }

    /* Test case: vocabulary concept id list is empty*/
    @Test(expected = Exception.class)
    public void testUpdateSiteCodeStatusEmptyList() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        List<Integer> vcIds = new ArrayList<>();
        siteCodeDAO.updateSiteCodeStatus(vcIds, 2, "TestStatus");
    }

    /* Test case: the concepts do not have status*/
    @Test
    public void testUpdateSiteCodeStatusNoStatus() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        List<Integer> vcIds = new ArrayList<>();
        vcIds.add(3);
        siteCodeDAO.updateSiteCodeStatus(vcIds, 2, "TestStatus");

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(2);

        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(3, boundElementIds);
        assertThat(elementMap.size(), is(0));
    }

    /* Test case: status is successfully updated */
    @Test
    public void testUpdateSiteCodeStatusSuccessful() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-allocate.xml");
        List<Integer> vcIds = new ArrayList<>();
        vcIds.add(1);
        String expectedStatus = "TestStatus";
        siteCodeDAO.updateSiteCodeStatus(vcIds, 2, expectedStatus);

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(2);

        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        assertThat(elementMap.size(), is(1));
        assertThat(elementMap.get(2), is(expectedStatus));
    }

}


