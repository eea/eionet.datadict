package eionet.datadict.services.io.impl;

import eionet.datadict.errors.ClassPathLoadResourceException;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import eionet.datadict.services.io.ClassPathResourceFileProvider;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ClassPathResoureFileProviderImpl implements ClassPathResourceFileProvider {

    @Override
    public File[] loadAllFilesFromFolder(String folderName) throws ClassPathLoadResourceException {

        URL sourceURL = this.getClass().getClassLoader().getResource(folderName);
        if (sourceURL == null) {
            throw new ClassPathLoadResourceException("wrong or missing folder in classpath");
        }

        File[] files;
        File sourceFolder;

        try {
            sourceFolder = new File(sourceURL.toURI());
            files = sourceFolder.listFiles();
        } catch (Exception e) {
            Logger.getLogger(ClassPathResoureFileProviderImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new ClassPathLoadResourceException("Error Loading Resources From Classpath", e);
        }
        return files;
    }

    @Override
    public File loadFileFromFolder(String folderName, String fileName) throws ClassPathLoadResourceException {
        URL sourceURL = this.getClass().getClassLoader().getResource(folderName + "/" + fileName);
        if (sourceURL == null) {
            throw new ClassPathLoadResourceException("wrong or missing folder or file  in classpath");
        }
        return new File(sourceURL.getFile());
    }

    @Override
    public File loadFileFromRootClasspathDirectory(String fileName) throws ClassPathLoadResourceException {
        URL sourceURL = this.getClass().getClassLoader().getResource(fileName);
        if (sourceURL == null) {
            throw new ClassPathLoadResourceException("wrong or missing file in classpath");
        }
        return new File(sourceURL.getFile());
    }
}
