package eionet.meta.dao;

import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.DBUnitHelper;
import eionet.meta.service.data.SiteCode;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.SiteCodeBoundElementIdentifiers;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

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
        Assert.assertThat(siteCodeDAO.siteCodeFolderExists(), is(true));
    }

    /* Test case: The site code vocabulary does not exist */
    @Test
    public void testSiteCodeFolderExistsFalse() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(siteCodeDAO.siteCodeFolderExists(), is(false));
    }

    /* Test case: There are no allocations for a specific country */
    @Test
    public void testGetCountryUsedAllocationsNoneAllocated() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(0));
    }

    /* Test case: There are no records for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(0));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(0));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        Assert.assertThat(siteCodeDAO.getCountryUsedAllocations("testCountryCode"), is(2));
    }

    /* Test case: There are no allocations for a specific country */
    @Test
    public void testGetCountryUnusedAllocationsNoneAllocated() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(0));
    }

    /* Test case: There are no records for a specific country */
    @Test
    public void testGetCountryUnusedAllocationsCountryNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-countryCode-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(0));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUnusedAllocationsCountryNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(0));
    }

    /* Test case: Search for pairs with country code: otherCountryCode, status: allocated but the site name element must be empty or null (there are no rows for otherCountryCode with site name element)*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsNotExistWithoutInitialName() throws Exception {
         DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
         Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("otherCountryCode", true), is(1));
    }

    /* Test case: Search for pairs with country code: testCountryCode, status: allocated but the site name element must be empty or null*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsExistWithoutInitialName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", true), is(2));
    }

    /* Test case: Search for pairs with country code: testCountryCode, status: allocated and do not take into consideration if there are initial site names elements*/
    @Test
    public void testGetCountryUnusedAllocationsCountryRecordsExistWithInitialName() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        Assert.assertThat(siteCodeDAO.getCountryUnusedAllocations("testCountryCode", false), is(3));
    }

    /* Test case: Search for site codes with status available when no such records exist*/
    @Test
    public void testGetFeeSiteCodeAmountNoRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        Assert.assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: Search for site codes with status available when site code folder does not exist*/
    @Test
    public void testGetFeeSiteCodeAmountNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: Search for site codes with status available when records exist*/
    @Test
    public void testGetFeeSiteCodeAmountRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        Assert.assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(2));
    }

    /* Test case: The vocabulary folder for site codes doesn't exist*/
    @Test
    public void testGetSiteCodeVocabularyFolderIdNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(0));
    }

    /* Test case: The vocabulary folder for site codes exists*/
    @Test
    public void testGetSiteCodeVocabularyFolderIdExistss() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        Assert.assertThat(siteCodeDAO.getFeeSiteCodeAmount(), is(2));
    }

    /* Test case: The vocabulary concepts and the data elements exist*/
    @Test
    public void testInsertUserAndDateCreatedForSiteCodesConceptsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> conceptIds = new ArrayList<>();
        conceptIds.add(1);
        conceptIds.add(4);
        conceptIds.add(5);
        List<VocabularyConcept> vocabularyConcepts = vocabularyConceptDAO.getVocabularyConcepts(conceptIds);
        String testUser = "test_user";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String testDateStr = formatter.format(new Date());
        siteCodeDAO.insertUserAndDateCreatedForSiteCodes(vocabularyConcepts, testUser);

        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(4);
        boundElementIds.add(5);
        for (Integer conceptId: conceptIds){
            Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(conceptId, boundElementIds);
            Assert.assertThat(elementMap.size(), is(2));
            Assert.assertThat(elementMap.get(4), is(testDateStr));    //this may need to be commented because there could be differences in the seconds.
            Assert.assertThat(elementMap.get(5), is(testUser));
        }
    }

    /* Test case: The dataElements don't exist*/
    @Test(expected = Exception.class)
    public void testInsertUserAndDateCreatedForSiteCodesDataElementsDontExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-none-allocated-countryCode-exists.xml");
        List<Integer> conceptIds = new ArrayList<>();
        conceptIds.add(1);
        conceptIds.add(2);
        conceptIds.add(4);
        List<VocabularyConcept> vocabularyConcepts = vocabularyConceptDAO.getVocabularyConcepts(conceptIds);
        String testUser = "test_user";
        siteCodeDAO.insertUserAndDateCreatedForSiteCodes(vocabularyConcepts, testUser);
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
        Assert.assertThat(elementMap.size(), is(3));
        Assert.assertThat(elementMap.get(1), is("testCountryCode"));
        Assert.assertThat(elementMap.get(2), is("ALLOCATED"));
        Assert.assertThat(elementMap.get(3), is(nullValue()));
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
        Assert.assertThat(elementMap.size(), is(0));
    }

    /* Test case: The data elements exist but are not set for the concept*/
    @Test
    public void testGetBoundElementIdAndValueDataElementWithoutValue() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(4);
        boundElementIds.add(5);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        Assert.assertThat(elementMap.size(), is(0));
    }

    /* Test case: The data elements don't exist */
    @Test
    public void testGetBoundElementIdAndValueDataElementsDontExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-records-exist.xml");
        List<Integer> boundElementIds = new ArrayList<>();
        boundElementIds.add(7);
        Map<Integer, String> elementMap = siteCodeDAO.getBoundElementIdAndValue(1, boundElementIds);
        Assert.assertThat(elementMap.size(), is(0));
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
        Assert.assertThat(scListElemNull.size(), is(3));
        Assert.assertThat(scListElemNull.get(0).getId(), is(1));
        Assert.assertThat(scListElemNull.get(0).getIdentifier(), is("C"));
        Assert.assertThat(scListElemNull.get(0).getLabel(), is("test1"));
        Assert.assertThat(scListElemNull.get(0).getDefinition(), is("test1"));
        Assert.assertThat(scListElemNull.get(0).getNotation(), is("test1"));
        Assert.assertThat(scListElemNull.get(0).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemNull.get(0).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(0).getStatus(), is(nullValue()));

        Assert.assertThat(scListElemNull.get(1).getId(), is(2));
        Assert.assertThat(scListElemNull.get(1).getIdentifier(), is("L"));
        Assert.assertThat(scListElemNull.get(1).getLabel(), is("test2"));
        Assert.assertThat(scListElemNull.get(1).getDefinition(), is("test2"));
        Assert.assertThat(scListElemNull.get(1).getNotation(), is("test2"));
        Assert.assertThat(scListElemNull.get(1).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemNull.get(1).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(1).getStatus(), is(nullValue()));

        Assert.assertThat(scListElemNull.get(2).getId(), is(3));
        Assert.assertThat(scListElemNull.get(2).getIdentifier(), is("R"));
        Assert.assertThat(scListElemNull.get(2).getLabel(), is("test3"));
        Assert.assertThat(scListElemNull.get(2).getDefinition(), is("test3"));
        Assert.assertThat(scListElemNull.get(2).getNotation(), is("test3"));
        Assert.assertThat(scListElemNull.get(2).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemNull.get(2).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemNull.get(2).getStatus(), is(nullValue()));


        Map<String, Integer> boundElementsMap = new HashMap<>();
        List<SiteCode> scListElemEmpty =  siteCodeDAO.getSiteCodeList(query, params, boundElementsMap);
        Assert.assertThat(scListElemEmpty.size(), is(3));
        Assert.assertThat(scListElemEmpty.get(0).getId(), is(1));
        Assert.assertThat(scListElemEmpty.get(0).getIdentifier(), is("C"));
        Assert.assertThat(scListElemEmpty.get(0).getLabel(), is("test1"));
        Assert.assertThat(scListElemEmpty.get(0).getDefinition(), is("test1"));
        Assert.assertThat(scListElemEmpty.get(0).getNotation(), is("test1"));
        Assert.assertThat(scListElemEmpty.get(0).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemEmpty.get(0).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(0).getStatus(), is(nullValue()));

        Assert.assertThat(scListElemEmpty.get(1).getId(), is(2));
        Assert.assertThat(scListElemEmpty.get(1).getIdentifier(), is("L"));
        Assert.assertThat(scListElemEmpty.get(1).getLabel(), is("test2"));
        Assert.assertThat(scListElemEmpty.get(1).getDefinition(), is("test2"));
        Assert.assertThat(scListElemEmpty.get(1).getNotation(), is("test2"));
        Assert.assertThat(scListElemEmpty.get(1).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemEmpty.get(1).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(1).getStatus(), is(nullValue()));

        Assert.assertThat(scListElemEmpty.get(2).getId(), is(3));
        Assert.assertThat(scListElemEmpty.get(2).getIdentifier(), is("R"));
        Assert.assertThat(scListElemEmpty.get(2).getLabel(), is("test3"));
        Assert.assertThat(scListElemEmpty.get(2).getDefinition(), is("test3"));
        Assert.assertThat(scListElemEmpty.get(2).getNotation(), is("test3"));
        Assert.assertThat(scListElemEmpty.get(2).getCountryCode(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getSiteCodeStatus(), is(SiteCodeStatus.AVAILABLE));
        Assert.assertThat(scListElemEmpty.get(2).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getDateCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getUserCreated(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scListElemEmpty.get(2).getStatus(), is(nullValue()));
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
        params.put("countryCode", "testCountryCode");
        params.put("status", "ALLOCATED");
        params.put("countryCodeElemId", 1);
        params.put("statusElemId", 2);

        List<SiteCode> scList =  siteCodeDAO.getSiteCodeList(query, params, boundElementsMap);
        Assert.assertThat(scList.size(), is(3));
        Assert.assertThat(scList.get(0).getId(), is(1));
        Assert.assertThat(scList.get(0).getIdentifier(), is("C"));
        Assert.assertThat(scList.get(0).getLabel(), is("test1"));
        Assert.assertThat(scList.get(0).getDefinition(), is("test1"));
        Assert.assertThat(scList.get(0).getNotation(), is("test1"));
        Assert.assertThat(scList.get(0).getCountryCode(), is("testCountryCode"));
        Assert.assertThat(scList.get(0).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scList.get(0).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        Assert.assertThat(scList.get(0).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(0).getDateCreated(), is(nullValue()));
        Assert.assertThat(scList.get(0).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(0).getUserCreated(), is(nullValue()));
        Assert.assertThat(scList.get(0).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scList.get(0).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scList.get(0).getStatus(), is(nullValue()));

        Assert.assertThat(scList.get(1).getId(), is(2));
        Assert.assertThat(scList.get(1).getIdentifier(), is("L"));
        Assert.assertThat(scList.get(1).getLabel(), is("test2"));
        Assert.assertThat(scList.get(1).getDefinition(), is("test2"));
        Assert.assertThat(scList.get(1).getNotation(), is("test2"));
        Assert.assertThat(scList.get(1).getCountryCode(), is("testCountryCode"));
        Assert.assertThat(scList.get(1).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scList.get(1).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        Assert.assertThat(scList.get(1).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(1).getDateCreated(), is(nullValue()));
        Assert.assertThat(scList.get(1).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(1).getUserCreated(), is(nullValue()));
        Assert.assertThat(scList.get(1).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scList.get(1).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scList.get(1).getStatus(), is(nullValue()));

        Assert.assertThat(scList.get(2).getId(), is(6));
        Assert.assertThat(scList.get(2).getIdentifier(), is("Q"));
        Assert.assertThat(scList.get(2).getLabel(), is("test6"));
        Assert.assertThat(scList.get(2).getDefinition(), is("test6"));
        Assert.assertThat(scList.get(2).getNotation(), is("test6"));
        Assert.assertThat(scList.get(2).getCountryCode(), is("testCountryCode"));
        Assert.assertThat(scList.get(2).getInitialSiteName(), is(nullValue()));
        Assert.assertThat(scList.get(2).getSiteCodeStatus(), is(SiteCodeStatus.ALLOCATED));
        Assert.assertThat(scList.get(2).getDateAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(2).getDateCreated(), is(nullValue()));
        Assert.assertThat(scList.get(2).getUserAllocated(), is(nullValue()));
        Assert.assertThat(scList.get(2).getUserCreated(), is(nullValue()));
        Assert.assertThat(scList.get(2).getYearsDeleted(), is(nullValue()));
        Assert.assertThat(scList.get(2).getYearsDisappeared(), is(nullValue()));
        Assert.assertThat(scList.get(2).getStatus(), is(nullValue()));

    }

    /* Test case: The vocabulary concept list is null or empty */
    @Test
    public void testSearchSiteCodes() throws Exception {
     //   Assert.assertThat(true, is(false));
    }

}


