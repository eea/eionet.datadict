package eionet.datadict.services.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ClassPathResourceFileProvider {

    public File[] loadAllFilesFromFolder(String folderName) throws IOException;
    
    public File loadFileFromRootClasspathDirectory(String fileName) throws FileNotFoundException;
}
