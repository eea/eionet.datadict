package eionet.meta;

import eionet.acl.AccessController;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


/**
 * Integration test to verify that the eionet.acl module works correctly.
 * As long as DD doesn't configure where to load the acl module's configuration
 * this test shows that the fallback to properties file works.
 */
public class AclModuleTest {

    @Test
    public void topLevelAclMustExist() throws Exception {
        boolean top = AccessController.aclExists("/");
        assertTrue("There must be a toplevel ACL", top);
    }

}
