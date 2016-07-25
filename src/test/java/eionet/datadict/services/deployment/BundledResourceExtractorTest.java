package eionet.datadict.services.deployment;

import eionet.datadict.services.impl.io.ClassPathResourceFileProviderImpl;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class BundledResourceExtractorTest {

    @Spy
    @InjectMocks
    private BundledResourceExtractor bundledResourceExtractor;
    
    @Spy
    private ClassPathResourceFileProviderImpl classPathResourceFileProvider;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void test() throws IOException {
        this.bundledResourceExtractor.initialize();
        verify(this.bundledResourceExtractor, times(15)).copyFileToDirectoryIfNotExists(any(File.class), any(File.class));
        verify(this.bundledResourceExtractor, times(3)).copyFileToDirectory(any(File.class), any(File.class));
        verify(this.bundledResourceExtractor, times(1)).copyDirectory(any(File.class), any(File.class));
    }
    
}
