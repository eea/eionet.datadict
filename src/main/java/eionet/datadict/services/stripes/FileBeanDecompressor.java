package eionet.datadict.services.stripes;

import eionet.datadict.errors.BadFormatException;
import java.io.IOException;
import net.sourceforge.stripes.action.FileBean;

public interface FileBeanDecompressor {

    FileBean unzip(FileBean fileBean) throws IOException, BadFormatException;
    
}
