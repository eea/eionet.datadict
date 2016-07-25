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
    
    protected static final String ACL_FOLDER_NAME = "acl";
    protected static final String MS_ACCESS_FOLDER_HOME = "msaccess";
    protected static final String OPENDOC_FOLDER_HOME = "opendoc";
    protected static final String VERSION_FILE = "VERSION.txt";
    protected static final String TEMP_FOLDER = "tmp";
    
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
    
    protected String getAppHomeDirectory() {
        return this.appHomeDirectory;
    }

    protected void initializeAclFiles() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, ACL_FOLDER_NAME);
        this.mkdirs(directory);
        File[] sourceFiles = classPathResourceProvider.getDirectoryFiles(ACL_FOLDER_NAME);
        
        for (File sourceFile : sourceFiles) {
            if (sourceFile.getName().endsWith(".prms") || sourceFile.getName().endsWith(".permissions")) {
                // Always overwrite .prms files.
                this.copyFileToDirectory(sourceFile, directory);
                continue;
            }
            
            // Other files must be copied only if they do not exist in destination folder.
            this.copyFileToDirectoryIfNotExists(sourceFile, directory);
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
        
        if (this.dirExists(directory)) {
            return;
        }
        
        directory.setWritable(true);
        directory.setReadable(true);
        
        if (!this.mkdirs(directory)) {
            throw new IOException("tmp directory could not be created");
        }
    }

    protected boolean mkdirs(File f) {
        return f.mkdirs();
    }
    
    protected boolean dirExists(File dir) {
        return dir.isDirectory();
    }
    
    protected void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        FileUtils.copyFileToDirectory(srcFile, destDir);
    }
    
    protected void copyFileToDirectoryIfNotExists(File srcFile, File destDir) throws IOException {
        File destFile = FileUtils.getFile(destDir, srcFile.getName());
        
        if (!FileUtils.directoryContains(destDir, destFile)) {
            FileUtils.copyFileToDirectory(srcFile, destDir);
        }
    }
    
    protected void copyDirectory(File dir, File destDir) throws IOException {
        FileUtils.copyDirectory(dir, destDir);
    }
    
}
