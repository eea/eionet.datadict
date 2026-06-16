package eionet.datadict.services.deployment;

import eionet.util.Props;
import eionet.util.PropsIF;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
public class BundledResourceExtractorRegressionTestIT {

    @Test
    public void testExtraction() {
        File appHomeDir = new File(Props.getRequiredProperty(PropsIF.APP_HOME));
        assertThat("App home directory not found: " + appHomeDir.getAbsolutePath(), appHomeDir.isDirectory(), is(true));

        File aclDir = FileUtils.getFile(appHomeDir, "acl");
        assertThat("Acl directory not found: " + aclDir.getAbsolutePath(), aclDir.isDirectory(), is(true));
        assertThat("Acl item count", aclDir.listFiles().length, is(19));

        File tmpDir = FileUtils.getFile(appHomeDir, "tmp");
        assertThat("Temp directory not found: " + tmpDir.getAbsolutePath(), tmpDir.isDirectory(), is(true));
    }

}
