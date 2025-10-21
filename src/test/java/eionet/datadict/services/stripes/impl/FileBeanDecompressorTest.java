package eionet.datadict.services.stripes.impl;

import eionet.datadict.errors.BadFormatException;
import eionet.datadict.services.io.UnzippedPhysicalFile;
import eionet.datadict.services.io.ZippedFileDecompressor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import net.sourceforge.stripes.action.FileBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileBeanDecompressorTest {
    
    @Mock
    private ZippedFileDecompressor zippedFileDecompressor;
    
    @InjectMocks
    private FileBeanDecompressorImpl fileBeanDecompressor;
    
    @Test
    public void testExtraction() throws IOException, BadFormatException, URISyntaxException {
        final File file = new File(this.getClass().getClassLoader().getResource("zip/file1.txt").toURI());
        final UnzippedPhysicalFile upf = new UnzippedPhysicalFile(file, "some_file.txt");
        final FileBean fileBean = Mockito.mock(FileBean.class);
        when(this.zippedFileDecompressor.unzip(any(InputStream.class))).thenReturn(upf);
        
        FileBean extractedFileBean = this.fileBeanDecompressor.unzip(fileBean);
        
        assertThat(extractedFileBean.getFileName(), is(equalTo(upf.getName())));
        assertThat(StringUtils.trim(this.readFileBeanContent(extractedFileBean)), is(equalTo("text")));
    }
    
    @Test
    public void testExtractionFailure() throws IOException, BadFormatException {
        final FileBean fileBean = Mockito.mock(FileBean.class);
        when(this.zippedFileDecompressor.unzip(any(InputStream.class))).thenThrow(BadFormatException.class);
        
        try {
            this.fileBeanDecompressor.unzip(fileBean);
            fail("Should have thrown BadFormatException");
        }
        catch (BadFormatException ex) { }
    }
    
    @Test
    public void testIOFailure() throws IOException, BadFormatException {
        final FileBean fileBean = Mockito.mock(FileBean.class);
        when(this.zippedFileDecompressor.unzip(any(InputStream.class))).thenThrow(IOException.class);
        
        try {
            this.fileBeanDecompressor.unzip(fileBean);
            fail("Should have thrown IOException");
        }
        catch (IOException ex) { }
    }
    
    private String readFileBeanContent(FileBean fileBean) throws IOException {
        InputStream in = null;
        
        try {
            in = fileBean.getInputStream();
            
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
}
