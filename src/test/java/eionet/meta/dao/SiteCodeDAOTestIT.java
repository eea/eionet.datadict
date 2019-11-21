package eionet.meta.dao;

import eionet.meta.service.DBUnitHelper;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import static org.hamcrest.CoreMatchers.is;

@SpringApplicationContext("mock-spring-context.xml")
public class SiteCodeDAOTestIT extends UnitilsJUnit4 {

    @SpringBeanByType
    private ISiteCodeDAO siteCodeDAO;

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
        Assert.assertThat(0, is(siteCodeDAO.getCountryUsedAllocations("testCountryCode")));
    }

    /* Test case: There are no records for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryNotExists() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
       // Assert.assertThat(0, is(siteCodeDAO.getCountryUsedAllocations("testCountryCode")));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryNoSiteCodeFolder() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
        Assert.assertThat(0, is(siteCodeDAO.getCountryUsedAllocations("testCountryCode")));
    }

    /* Test case: There are allocated site codes for a specific country */
    @Test
    public void testGetCountryUsedAllocationsCountryRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-sitecode-folder-not-exists.xml");
       // Assert.assertThat(0, is(siteCodeDAO.getCountryUsedAllocations("testCountryCode")));
    }


}


