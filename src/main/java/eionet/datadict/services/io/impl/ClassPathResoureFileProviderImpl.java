package eionet.datadict.services.io.impl;

import java.io.File;
import java.net.URL;
import org.springframework.stereotype.Service;
import eionet.datadict.services.io.ClassPathResourceFileProvider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import liquibase.util.StringUtils;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ClassPathResoureFileProviderImpl implements ClassPathResourceFileProvider {

    @Override
    public File[] getDirectoryFiles(String... names) throws IOException {
        URL sourceUrl = this.getClassPathResourceUrl(names);
        
        File sourceFolder;

        try {
            sourceFolder = new File(sourceUrl.toURI());
        } catch (URISyntaxException ex) {
            throw new IOException("Error loading resources from classpath.", ex);
        }
        
        return sourceFolder.listFiles();
    }
    
    @Override
    public File getFile(String... names) throws FileNotFoundException {
        URL sourceURL = this.getClassPathResourceUrl(names);
        
        return new File(sourceURL.getFile());
    }
    
    protected URL getClassPathResourceUrl(String... names) throws FileNotFoundException {
        String resourcePath = this.getPath(names);
        URL resourceUrl = this.getClass().getClassLoader().getResource(resourcePath);
        
        if (resourceUrl == null) {
            throw new FileNotFoundException("Could not find classpath resource: " + resourcePath);
        }
        
        return resourceUrl;
    }
    
    protected String getPath(String... names) {
        return StringUtils.join(names, File.separator);
    }
    
}
