package eionet.datadict.services.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface ClassPathResourceFileProvider {

    public File[] getDirectoryFiles(String... names) throws IOException;
    
    public File getFile(String... names) throws FileNotFoundException;
    
}
