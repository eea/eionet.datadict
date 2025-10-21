package eionet.util;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DbUnitTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
        value = "classpath:seed-acldata.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL,
        value = "classpath:seed-acldata.xml")
public class SecurityUtilTestIT {

    private DDUser user1;
    private DDUser user2;

    @Before
    public void setUp() {
        user1 =  mock(DDUser.class);
        ArrayList<String> groupResults1 = new ArrayList<>();
        groupResults1.add("heinlja");
        user1.setGroupResults(groupResults1);
        when(user1.isAuthentic()).thenReturn(true);
        user2 = mock(DDUser.class);
        ArrayList<String> groupResults2 = new ArrayList<>();
        groupResults2.add("whoever");
        user2.setGroupResults(groupResults2);
        when(user2.isAuthentic()).thenReturn(true);
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
        DDUser user = new FakeUser();
        user.authenticate("heinlja", "heinlja");
        String aclPath = "/testacl";

        assertTrue("heinlja should have '78' permission.", SecurityUtil.hasPerm(user, aclPath, "i"));
        assertTrue("heinlja should have 'er' permission.",!SecurityUtil.hasPerm(user, aclPath, "er"));
        assertTrue("heinlja should NOT have 'u' permission as overriden by entry ACL.", !SecurityUtil.hasPerm(user, aclPath, "u"));

    }

    @Test
    @Ignore
    public void testDBUserPermission() throws Exception {
        String aclPath1 = "/testacl/first";
        String aclPath2 = "/testacl/second";
        String aclPath3 = "/testacl/third";
        //anonymous has 'amsa' in /testacl/first, in /testacl/amsa - authenticated
        assertTrue("anonymous should have 'amsa' in first", SecurityUtil.hasPerm(null, aclPath1, "amsa"));
        assertTrue("anonymous should not have 'amsa' in second ", !SecurityUtil.hasPerm(null, aclPath2, "amsa"));

        assertTrue("authenticated should have 'amsa' in first", SecurityUtil.hasPerm(user1, aclPath1, "amsa"));
        assertTrue("authenticated should have 'amsa' in second ", SecurityUtil.hasPerm(user1, aclPath2, "amsa"));
        assertTrue("authenticated should have 'amsa' in first", SecurityUtil.hasPerm(user2, aclPath1, "amsa"));

        assertTrue("authenticated should not have 'amsa' in third", !SecurityUtil.hasPerm(user2, aclPath3, "amsa"));

    }
}
