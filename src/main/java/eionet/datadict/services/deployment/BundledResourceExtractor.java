package eionet.datadict.services.deployment;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import eionet.datadict.services.io.ClassPathResourceFileProvider;
import eionet.util.Props;
import eionet.util.PropsIF;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class BundledResourceExtractor {
    
    private static final String ACL_FOLDER_NAME = "acl";
    private static final String MS_ACCESS_FOLDER_HOME = "msaccess";
    private static final String OPENDOC_FOLDER_HOME = "opendoc";
    private static final String VERSION_FILE = "VERSION.txt";
    private static final String TEMP_FOLDER = "tmp";
    
    private final ClassPathResourceFileProvider classPathResourceProvider;
    private String appHomeDirectory;

    @Autowired
    public BundledResourceExtractor(ClassPathResourceFileProvider classPathResourceProvider) {
        this.classPathResourceProvider = classPathResourceProvider;
    }

    @PostConstruct
    public void initialize() throws IOException {
        try {
            appHomeDirectory = Props.getRequiredProperty(PropsIF.APP_HOME);
        } catch (Exception ex) {
            throw new BeanInitializationException("app.home property not found in properties file.", ex);
        }

        initializeAclFiles();
        initializeOpenDocFiles();
        initializeMsAccessFiles();
        overwriteVersionFile();
        createTMPFolder();
    }

    protected void initializeAclFiles() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, ACL_FOLDER_NAME);
        directory.mkdirs();
        File[] sourceFiles = classPathResourceProvider.getDirectoryFiles(ACL_FOLDER_NAME);
        
        for (File sourceFile : sourceFiles) {
            if (sourceFile.getName().endsWith(".prms") || sourceFile.getName().endsWith(".permissions")) {
                // Always overwrite .prms files.
                this.copyFileToDirectory(sourceFile, directory);
                continue;
            }
            
            if (!this.directoryContains(directory, sourceFile.getName())) {
                // Other files must be copied only if they do not exist in destination folder.
                this.copyFileToDirectory(sourceFile, directory);
            }
        }
    }

    protected void initializeMsAccessFiles() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, MS_ACCESS_FOLDER_HOME);
        File[] files = classPathResourceProvider.getDirectoryFiles(MS_ACCESS_FOLDER_HOME);
        
        for (File file : files) {
            this.copyFileToDirectory(file, directory);
        }
    }

    protected void initializeOpenDocFiles() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, OPENDOC_FOLDER_HOME, "ods");
        File[] files = classPathResourceProvider.getDirectoryFiles(OPENDOC_FOLDER_HOME);
        
        for (File file : files) {
            this.copyDirectory(file, directory);
        }
    }

    protected void overwriteVersionFile() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory);
        File oldFile = classPathResourceProvider.getFile(VERSION_FILE);
        this.copyFileToDirectory(oldFile, directory);
    }

    protected void createTMPFolder() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, TEMP_FOLDER);
        
        if (directory.isDirectory()) {
            return;
        }
        
        directory.setWritable(true);
        directory.setReadable(true);
        
        if (!directory.mkdirs()) {
            throw new IOException("tmp directory could not be created");
        }
    }

    protected boolean directoryContains(File dir, String filename) throws IOException {
        File file = FileUtils.getFile(dir, filename);
        
        return FileUtils.directoryContains(dir, file);
    }
    
    protected void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        FileUtils.copyFileToDirectory(srcFile, destDir);
    }
    
    protected void copyDirectory(File dir, File destDir) throws IOException {
        FileUtils.copyDirectory(dir, destDir);
    }
    
}
