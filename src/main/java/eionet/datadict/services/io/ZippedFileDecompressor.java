package eionet.datadict.services.io;

import eionet.datadict.errors.BadFormatException;
import java.io.IOException;
import java.io.InputStream;

public interface ZippedFileDecompressor {

    UnzippedFile unzip(InputStream in) throws IOException, BadFormatException;
    
}
