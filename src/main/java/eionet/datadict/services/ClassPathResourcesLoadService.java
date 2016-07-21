package eionet.datadict.services;

import eionet.datadict.errors.ClassPathLoadResourceException;
import java.io.File;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ClassPathResourcesLoadService {

    public File[] loadAllFilesFromFolder(String folderName) throws ClassPathLoadResourceException;
    
    public File loadFileFromFolder(String folderName, String fileName) throws ClassPathLoadResourceException;
    
    public File loadFileFromRootClasspathDirectory(String fileName) throws ClassPathLoadResourceException;
}
