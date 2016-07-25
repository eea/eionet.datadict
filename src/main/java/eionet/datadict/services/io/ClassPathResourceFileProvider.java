package eionet.datadict.services.io;

import eionet.datadict.errors.ClassPathLoadResourceException;
import java.io.File;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ClassPathResourceFileProvider {

    public File[] loadAllFilesFromFolder(String folderName) throws ClassPathLoadResourceException;
    
    public File loadFileFromFolder(String folderName, String fileName) throws ClassPathLoadResourceException;
    
    public File loadFileFromRootClasspathDirectory(String fileName) throws ClassPathLoadResourceException;
}
