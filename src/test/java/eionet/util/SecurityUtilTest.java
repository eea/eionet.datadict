package eionet.util;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.util.CollectionUtils;

import eionet.meta.DDUser;

public class SecurityUtilTest extends TestCase {

    public void testGetUserCountryFromRoles() {
        DDUser dduser = EasyMock.createMock(DDUser.class);
        String[] roles =
        { "eionet", "eionet-nrc", "eionet-nrc-energy", "eea", "eionet-nfp", "eionet-nfp-mc-ee", "eea-epa", "eea-epa-members",
                "eionet-nrc-soil", "eionet-nrc-energy-mc", "eionet-nrc-soil-mc, eionet-nrc-soil-mc-ee", "eionet-nrc-nature-mc-ee" };
        EasyMock.expect(dduser.getUserRoles()).andReturn(roles).anyTimes();
        EasyMock.replay(dduser);

        String[] parentRoles = {"eionet-nrc-nature-mc", "eionet-nfp-mc"};
        String[] countries = {"EE"};
        assertEquals(SecurityUtil.getUserCountriesFromRoles(dduser, parentRoles), CollectionUtils.arrayToList(countries));

        EasyMock.reset(dduser);
        String[] roles2 =
        { "eionet", "eionet-nrc", "eionet-nrc-energy", "eea", "eionet-nfp", "eionet-nfp-mc-ee", "eea-epa", "eea-epa-members",
                "eionet-nrc-soil", "eionet-nrc-energy-mc", "eionet-nrc-soil-mc", "eionet-nrc-soil-mc-ee", "eionet-nrc-nature-mc-fi" };
        EasyMock.expect(dduser.getUserRoles()).andReturn(roles2).anyTimes();
        EasyMock.replay(dduser);

        String[] countries2 = {"EE", "FI"};
        assertEquals(SecurityUtil.getUserCountriesFromRoles(dduser, parentRoles), CollectionUtils.arrayToList(countries2));

        assertNull(SecurityUtil.getUserCountriesFromRoles(dduser, null));
        assertNull(SecurityUtil.getUserCountriesFromRoles(null, parentRoles));

    }
}
