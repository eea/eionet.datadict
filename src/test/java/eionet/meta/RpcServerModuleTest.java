package eionet.meta;

import eionet.rpcserver.UITServiceRoster;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;


/**
 * Integration test to verify that the eionet.rpcserver module works correctly.
 * As long as DD doesn't configure where to load the rpcserver module's configuration
 * this test shows that the fallback to properties file works.
 */
public class RpcServerModuleTest {

    @Test
    public void servicesMustExist() throws Exception {
        HashMap services = UITServiceRoster.getServices();
        assertNotNull("No services", services);
    }

}
