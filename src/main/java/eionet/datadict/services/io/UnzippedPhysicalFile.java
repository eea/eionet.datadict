package eionet.datadict.services.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UnzippedPhysicalFile implements UnzippedFile {

    private final File file;
    private final String name;

    public UnzippedPhysicalFile(File file, String name) {
        this.file = file;
        this.name = name;
    }

    public File getFile() {
        return this.file;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public void dispose() {
        this.file.delete();
    }
    
}
