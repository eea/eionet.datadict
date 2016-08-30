package eionet.datadict.services.io;

import java.io.IOException;
import java.io.InputStream;

public interface UnzippedFile {

    String getName();
    
    InputStream getInputStream() throws IOException;
    
    void dispose();
    
}
