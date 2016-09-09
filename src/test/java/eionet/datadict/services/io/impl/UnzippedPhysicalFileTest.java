package eionet.datadict.services.io.impl;

import eionet.datadict.services.io.UnzippedPhysicalFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import org.junit.Test;

public class UnzippedPhysicalFileTest {
    
    @Test
    public void test() throws IOException, URISyntaxException {
        final File file = new File(this.getClass().getClassLoader().getResource("mock-spring-context.xml").toURI());
        final String name = "mock-spring-context.xml";
        UnzippedPhysicalFile upf = new UnzippedPhysicalFile(file, name);
        
        assertThat(upf.getFile(), is(equalTo(file)));
        assertThat(upf.getName(), is(equalTo(name)));
        assertThat("File not found", upf.getFile().exists(), is(equalTo(true)));
        
        InputStream in = null;
        
        try {
            in = upf.getInputStream();
            assertThat("File should not be empty", in.available(), is(greaterThan(0)));
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    @Test
    public void testDispose() throws IOException {
        final File file = File.createTempFile("temp-test-file.txt", ".tmp");
        UnzippedPhysicalFile upf = new UnzippedPhysicalFile(file, "temp-test-file.txt");
        upf.dispose();
        
        assertThat("Physical file should have been deleted", file.exists(), is(equalTo(false)));
    }
    
}
