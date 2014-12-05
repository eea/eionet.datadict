package eionet.util;

import eionet.meta.DDUser;
import eionet.meta.service.DBUnitHelper;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.CollectionUtils;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SpringApplicationContext("mock-spring-context.xml")
public class SecurityUtilTest extends UnitilsJUnit4  {


    /**
     * Load seed data file.
     * @throws Exception if loading fails
     */
    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-acldata.xml");
    }

    /**
     * Delete helper data.
     * @throws Exception if delete fails
     */
    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-acldata.xml");
    }

    @Test
    public void testGetUserCountryFromRoles() {
        DDUser dduser = EasyMock.createMock(DDUser.class);
        String[] roles =
        { "eionet", "eionet-nrc", "eionet-nrc-energy", "eea", "eionet-nfp", "eionet-nfp-mc-ee", "eea-epa", "eea-epa-members",
                "eionet-nrc-soil", "eionet-nrc-energy-mc", "eionet-nrc-soil-mc, eionet-nrc-soil-mc-ee", "eionet-nrc-biodivdata-mc-ee" };
        EasyMock.expect(dduser.getUserRoles()).andReturn(roles).anyTimes();
        EasyMock.replay(dduser);

        String[] parentRoles = {"eionet-nrc-biodivdata-mc", "eionet-nfp-mc"};
        String[] countries = {"EE"};
        assertEquals(SecurityUtil.getUserCountriesFromRoles(dduser, parentRoles), CollectionUtils.arrayToList(countries));

        EasyMock.reset(dduser);
        String[] roles2 =
        { "eionet", "eionet-nrc", "eionet-nrc-energy", "eea", "eionet-nfp", "eionet-nfp-mc-ee", "eea-epa", "eea-epa-members",
                "eionet-nrc-soil", "eionet-nrc-energy-mc", "eionet-nrc-soil-mc", "eionet-nrc-soil-mc-ee", "eionet-nrc-biodivdata-mc-fi" };
        EasyMock.expect(dduser.getUserRoles()).andReturn(roles2).anyTimes();
        EasyMock.replay(dduser);

        String[] countries2 = {"EE", "FI"};
        assertEquals(SecurityUtil.getUserCountriesFromRoles(dduser, parentRoles), CollectionUtils.arrayToList(countries2));

        assertNull(SecurityUtil.getUserCountriesFromRoles(dduser, null));
        assertNull(SecurityUtil.getUserCountriesFromRoles(null, parentRoles));

    }


    @Test
    public void testUserPermission() throws Exception {
        String aclPath = "/testacl";

        assertTrue("heinlja should have 'i' permission.", SecurityUtil.hasPerm("heinlja", aclPath, "i"));
        assertTrue("heinlja should have 'er' permission.",!SecurityUtil.hasPerm("heinlja", aclPath, "er"));
        assertTrue("heinlja should NOT have 'u' permission as overriden by entry ACL.", !SecurityUtil.hasPerm("heinlja", aclPath, "u"));

    }

    @Test
    public void testDBUserPermission() throws Exception {
        String aclPath1 = "/testacl/first";
        String aclPath2 = "/testacl/second";
        String aclPath3 = "/testacl/third";

        //anonymous has 'amsa' in /testacl/first, in /testacl/amsa - authenticated
        assertTrue("anonymous should have 'amsa' in first", SecurityUtil.hasPerm(null, aclPath1, "amsa"));
        assertTrue("anonymous should not have 'amsa' in second ", !SecurityUtil.hasPerm(null, aclPath2, "amsa"));

        assertTrue("authenticated should have 'amsa' in first", SecurityUtil.hasPerm("heinlja", aclPath1, "amsa"));
        assertTrue("authenticated should have 'amsa' in second ", SecurityUtil.hasPerm("heinlja", aclPath2, "amsa"));
        assertTrue("authenticated should have 'amsa' in first", SecurityUtil.hasPerm("whoever", aclPath1, "amsa"));

        assertTrue("authenticated should not have 'amsa' in third", !SecurityUtil.hasPerm("whoever", aclPath3, "amsa"));

    }
}
