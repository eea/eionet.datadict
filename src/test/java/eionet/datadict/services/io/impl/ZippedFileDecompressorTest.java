package eionet.datadict.services.io.impl;

import eionet.datadict.errors.BadFormatException;
import eionet.datadict.services.io.UnzippedFile;
import eionet.datadict.services.io.UnzippedPhysicalFile;
import eionet.datadict.services.io.ZippedFileDecompressor;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mock-spring-context.xml" })
public class ZippedFileDecompressorTest {

    @Autowired
    private ZippedFileDecompressor zippedFileDecompressor;
    
    @Test
    public void testEmptyArchive() throws IOException {
        try {
            this.extractZipResource("zip/empty_archive.zip");
            fail("Should have raised BadFormatException");
        }
        catch (BadFormatException ex) { }
    }
    
    @Test
    public void testInvalidArchiveStructure() throws IOException {
        try {
            this.extractZipResource("zip/dir_archive.zip");
            fail("Should have raised BadFormatException");
        }
        catch (BadFormatException ex) { }
    }
    
    @Test
    public void testSingleFileArchive() throws IOException, BadFormatException {
        UnzippedFile uf = this.extractZipResource("zip/single_file_archive.zip");
        
        try {
            assertThat(uf, is(instanceOf(UnzippedPhysicalFile.class)));
            assertThat(uf.getName(), is(equalTo("file1.txt")));
            assertThat(StringUtils.trim(this.readExtractedFileAsString(uf)), is(equalTo("text")));
        }
        finally {
            uf.dispose();
        }
    }
    
    @Test
    public void testMultiFileArchive() throws IOException, BadFormatException {
        UnzippedFile uf = this.extractZipResource("zip/multi_file_archive.zip");
        
        try {
            assertThat(uf, is(instanceOf(UnzippedPhysicalFile.class)));
            assertThat(uf.getName(), is(equalTo("file1.txt")));
            assertThat(StringUtils.trim(this.readExtractedFileAsString(uf)), is(equalTo("text1")));
        }
        finally {
            uf.dispose();
        }
    }
    
    private UnzippedFile extractZipResource(String resourcePath) throws IOException, BadFormatException {
        InputStream in = null;
        
        try {
            in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
            
            return this.zippedFileDecompressor.unzip(in);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private String readExtractedFileAsString(UnzippedFile uf) throws IOException {
        InputStream in = null;
        
        try {
            in = uf.getInputStream();
            
            return IOUtils.toString(in, "utf-8");
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
}
