package eionet.datadict.services.deployment;

import eionet.util.Props;
import eionet.util.PropsIF;
import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class BundledResourceExtractorRegressionTest {
    
    @Test
    public void testExtraction() {
        File appHomeDir = new File(Props.getRequiredProperty(PropsIF.APP_HOME));
        assertThat("App home directory not found: " + appHomeDir.getAbsolutePath(), appHomeDir.isDirectory(), is(true));
        
        File aclDir = FileUtils.getFile(appHomeDir, "acl");
        assertThat("Acl directory not found: " + aclDir.getAbsolutePath(), aclDir.isDirectory(), is(true));
        assertThat("Acl item count", aclDir.listFiles().length, is(16));
        
        File msAccessDir = FileUtils.getFile(appHomeDir, "msaccess");
        assertThat("MS Access directory not found: " + msAccessDir.getAbsolutePath(), msAccessDir.isDirectory(), is(true));
        assertThat("MS Access item count", msAccessDir.listFiles().length, is(1));
        
        File opendocDir = FileUtils.getFile(appHomeDir, "opendoc");
        assertThat("MS Access directory not found: " + opendocDir.getAbsolutePath(), opendocDir.isDirectory(), is(true));
        assertThat("OpenDoc item count", opendocDir.listFiles().length, is(1));
        
        File tmpDir = FileUtils.getFile(appHomeDir, "tmp");
        assertThat("Temp directory not found: " + tmpDir.getAbsolutePath(), tmpDir.isDirectory(), is(true));
        
        File versionFile = FileUtils.getFile(appHomeDir, "VERSION.txt");
        assertThat("Version file not found: " + versionFile.getAbsolutePath(), versionFile.isFile(), is(true));
        
    }
    
}
