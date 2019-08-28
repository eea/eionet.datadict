package eionet.meta;

import eionet.acl.AccessController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;


/**
 * Integration test to verify that the eionet.acl module works correctly.
 * As long as DD doesn't configure where to load the acl module's configuration
 * this test shows that the fallback to properties file works.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class AclModuleTestIT {

    @Test
    public void topLevelAclMustExist() throws Exception {
        boolean top = AccessController.aclExists("/");
        assertTrue("There must be a toplevel ACL", top);
    }

}
