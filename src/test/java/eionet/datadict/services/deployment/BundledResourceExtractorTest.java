package eionet.datadict.services.deployment;

import eionet.datadict.services.io.impl.ClassPathResourceFileProviderImpl;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
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
        doReturn(false).when(this.bundledResourceExtractor).dirExists(any(File.class));
        doReturn(true).when(this.bundledResourceExtractor).mkdirs(any(File.class));
        doNothing().when(this.bundledResourceExtractor).copyDirectory(any(File.class), any(File.class));
        doNothing().when(this.bundledResourceExtractor).copyFileToDirectory(any(File.class), any(File.class));
        doNothing().when(this.bundledResourceExtractor).copyFileToDirectoryIfNotExists(any(File.class), any(File.class));
        
        this.bundledResourceExtractor.extractResources();
        
        verify(this.bundledResourceExtractor, times(1)).extractAclFiles();
        verify(this.bundledResourceExtractor, times(1)).extractMsAccessFiles();
        verify(this.bundledResourceExtractor, times(1)).extractOpenDocFiles();
        verify(this.bundledResourceExtractor, times(1)).createTempFolder();
        
        verify(this.bundledResourceExtractor, times(16)).copyFileToDirectoryIfNotExists(any(File.class), any(File.class));
        verify(this.bundledResourceExtractor, times(3)).copyFileToDirectory(any(File.class), any(File.class));
        verify(this.bundledResourceExtractor, times(1)).copyDirectory(any(File.class), any(File.class));
        
        verify(this.bundledResourceExtractor, times(1)).dirExists(any(File.class));
        verify(this.bundledResourceExtractor, times(2)).mkdirs(any(File.class));
    }
    
}
