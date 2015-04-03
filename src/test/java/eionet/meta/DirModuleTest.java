package eionet.meta;

import eionet.directory.modules.FileServiceImpl;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;


/**
 * Integration test to verify that the eionet.directory module works correctly.
 * As long as DD doesn't configure where to load the directory module's configuration
 * this test shows that the fallback to properties file works.
 */
public class DirModuleTest {

    @Test
    public void fileService() throws Exception {
        FileServiceImpl fs = new FileServiceImpl();
        assertNotNull("File service must instantiate", fs);
    }

}
