package eionet.datadict.services.impl.io;

import eionet.datadict.services.io.UnzippedPhysicalFile;
import eionet.datadict.errors.BadFormatException;
import eionet.datadict.services.io.UnzippedFile;
import eionet.datadict.services.io.ZippedFileDecompressor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class ZippedFileDecompressorImpl implements ZippedFileDecompressor {

    @Override
    public UnzippedFile unzip(InputStream in) throws IOException, BadFormatException {
        ZipInputStream zin = null;
        
        try {
            zin = new ZipInputStream(in);
            ZipEntry entry = zin.getNextEntry();
            
            if (entry == null) {
                throw new BadFormatException("Could not extract compressed file. Archive was either empty, or malformed.");
            }
            
            if (entry.isDirectory()) {
                throw new BadFormatException("Unexpected archive structure: directories are not supported.");
            }
            
            File destFile = File.createTempFile(entry.getName(), ".tmp");
            FileUtils.copyInputStreamToFile(zin, destFile);
            
            return new UnzippedPhysicalFile(destFile, entry.getName());
        }
        finally {
            if (zin != null) {
                zin.close();
            }
        }
    }
    
    
    
}
