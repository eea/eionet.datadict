package eionet.meta.doc.io;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
public class DocumentationFileStorageInfoProviderTest {
    
    @Autowired
    private DocumentationFileStorageInfoProvider storageInfoProvider;
    
    @Test
    public void testStoragePath() {
        String storagePath = storageInfoProvider.getFileStoragePath();
        assertTrue(storagePath != null && storagePath.trim().length() > 0);
    }
}
