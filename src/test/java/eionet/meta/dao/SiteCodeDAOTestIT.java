package eionet.meta.dao;

import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.DBUnitHelper;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

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
        Assert.assertThat(true, is(siteCodeDAO.siteCodeFolderExists()));
    }

    /* Test case: The site code vocabulary does not exist */
    @Test
    public void testSiteCodeFolderExistsFalse() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(false, is(siteCodeDAO.siteCodeFolderExists()));
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
    @Test(expected = NullPointerException.class)
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
        Assert.assertThat(elementMap.get(2), is("Allocated"));
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

}


