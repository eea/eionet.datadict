package eionet.datadict.services.io.impl;

import java.io.File;
import java.net.URL;
import org.springframework.stereotype.Service;
import eionet.datadict.services.io.ClassPathResourceFileProvider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ClassPathResoureFileProviderImpl implements ClassPathResourceFileProvider {

    @Override
    public File[] loadAllFilesFromFolder(String folderName) throws IOException {
        URL sourceURL = this.getClass().getClassLoader().getResource(folderName);
        
        if (sourceURL == null) {
            throw new FileNotFoundException("wrong or missing folder in classpath");
        }

        File[] files;

        try {
            File sourceFolder = new File(sourceURL.toURI());
            files = sourceFolder.listFiles();
        } catch (URISyntaxException e) {
            throw new IOException("Error Loading Resources From Classpath", e);
        }
        
        return files;
    }

    @Override
    public File loadFileFromFolder(String folderName, String fileName) throws FileNotFoundException {
        URL sourceURL = this.getClass().getClassLoader().getResource(folderName + "/" + fileName);
        if (sourceURL == null) {
            throw new FileNotFoundException("wrong or missing folder or file  in classpath");
        }
        return new File(sourceURL.getFile());
    }

    @Override
    public File loadFileFromRootClasspathDirectory(String fileName) throws FileNotFoundException {
        URL sourceURL = this.getClass().getClassLoader().getResource(fileName);
        if (sourceURL == null) {
            throw new FileNotFoundException("wrong or missing file in classpath");
        }
        return new File(sourceURL.getFile());
    }
}
