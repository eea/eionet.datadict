package eionet.meta.dao;

import eionet.meta.service.DBUnitHelper;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

@SpringApplicationContext("mock-spring-context.xml")
public class DataElementDAOTestIT  extends UnitilsJUnit4 {

    @SpringBeanByType
    private IDataElementDAO dataElementDAO;

    /* Test case: There are no records for the given identifiers */
    @Test
    public void testGetMultipleCommonDataElementIdsNoRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
        List<String> identifiers = new ArrayList<>();
        identifiers.add("NiD_testK");
        identifiers.add("NiD_testL");
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(identifiers);
        Assert.assertThat(elementMap.size(), is(0));
    }

    /* Test case: There are records for the given identifiers */
    @Test
    public void testGetMultipleCommonDataElementIdsRecordsExist() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
        List<String> identifiers = new ArrayList<>();
        identifiers.add("V1");
        identifiers.add("E1");
        identifiers.add("ch0031");
        identifiers.add("E2");
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(identifiers);
        Assert.assertThat(elementMap.size(), is(4));
        Assert.assertThat(elementMap.get("V1"), is(101));
        Assert.assertThat(elementMap.get("E1"), is(1));
        Assert.assertThat(elementMap.get("ch0031"), is(301));
        Assert.assertThat(elementMap.get("E2"), is(2));
    }

    /* Test case: Some of the elements do not have Released status */
    @Test
    public void testGetMultipleCommonDataElementIdsNotReleased() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
        List<String> identifiers = new ArrayList<>();
        identifiers.add("V1");
        identifiers.add("E1");
        identifiers.add("E4");  //This element has status Incomplete
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(identifiers);
        Assert.assertThat(elementMap.size(), is(2));
        Assert.assertThat(elementMap.get("V1"), is(101));
        Assert.assertThat(elementMap.get("E1"), is(1));
    }
}
