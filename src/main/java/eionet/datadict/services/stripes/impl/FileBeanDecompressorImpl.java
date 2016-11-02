package eionet.datadict.services.stripes.impl;

import eionet.datadict.errors.BadFormatException;
import eionet.datadict.services.io.UnzippedFile;
import eionet.datadict.services.io.UnzippedPhysicalFile;
import eionet.datadict.services.io.ZippedFileDecompressor;
import eionet.datadict.services.stripes.FileBeanDecompressor;
import java.io.File;
import java.io.IOException;
import net.sourceforge.stripes.action.FileBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileBeanDecompressorImpl implements FileBeanDecompressor {

    private final ZippedFileDecompressor zippedFileDecompressor;
    
    @Autowired
    public FileBeanDecompressorImpl(ZippedFileDecompressor zippedFileDecompressor) {
        this.zippedFileDecompressor = zippedFileDecompressor;
    }
    
    @Override
    public FileBean unzip(FileBean fileBean) throws IOException, BadFormatException {
        UnzippedFile unzippedFile = this.zippedFileDecompressor.unzip(fileBean.getInputStream());
        File file = ((UnzippedPhysicalFile) unzippedFile).getFile();;
        
        return new FileBean(file, null, unzippedFile.getName());
    }
    
}
