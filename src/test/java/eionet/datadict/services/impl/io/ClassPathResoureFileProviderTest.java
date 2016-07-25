package eionet.datadict.services.impl.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class ClassPathResoureFileProviderTest {
    
    private ClassPathResoureFileProviderImpl classPathResourceProvider;
    
    @Before
    public void setUp() {
        this.classPathResourceProvider = new ClassPathResoureFileProviderImpl();
    }
    
    @Test
    public void testGetDirectoryFiles() throws IOException {
        File[] packageFiles = this.classPathResourceProvider.getDirectoryFiles("eionet", "datadict", "services", "impl", "io");
        assertThat(packageFiles, is(notNullValue()));
        assertThat(Arrays.asList(packageFiles), not(empty()));
        assertThat("Test class not found in classpath.", CollectionUtils.find(Arrays.asList(packageFiles), new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                File f = (File) o;
                
                return StringUtils.equals(f.getName(), "ClassPathResoureFileProviderTest.class");
            }
        }), is(notNullValue()));
        
        try {
            final String fakePackageName = "io222";
            this.classPathResourceProvider.getDirectoryFiles("eionet", "datadict", "services", "impl", fakePackageName);
            fail("Sub package " + fakePackageName + " should not exist.");
        }
        catch(IOException ex) {
            assertThat(ex.getClass(), is(equalTo((Class) FileNotFoundException.class)));
        }
    }
    
    @Test
    public void testGetFile() throws FileNotFoundException {
        final String testClassFileName = "ClassPathResoureFileProviderTest.class";
        File testClassFile = this.classPathResourceProvider.getFile("eionet", "datadict", "services", "impl", "io", testClassFileName);
        assertThat(testClassFile, is(notNullValue()));
        assertThat(testClassFile.getName(), is(equalTo(testClassFileName)));
        
        try {
            final String fakeClassName = "testClassFileName + \"1234\"";
            this.classPathResourceProvider.getFile("eionet", "datadict", "services", "impl", "io", fakeClassName);
            fail("Test class " + fakeClassName + " should not exist.");
        }
        catch (FileNotFoundException ex) { }
    }
    
}
