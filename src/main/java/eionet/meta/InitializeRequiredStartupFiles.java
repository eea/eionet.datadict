package eionet.meta;

import eionet.datadict.errors.ClassPathLoadResourceException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import eionet.datadict.services.ClassPathResourcesLoadService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.nio.file.Files;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class InitializeRequiredStartupFiles {

    private static final String ACL_FOLDER_NAME = "acl";
    private static final String MS_ACCESS_FOLDER_HOME = "msaccess";
    private static final String OPENDOC_FOLDER_HOME = "opendoc";
    private static final String VERSION_FILE = "VERSION.txt";
    private static final String TEMP_FOLDER = "tmp";
    
    private ClassPathResourcesLoadService classPathResourcesLoadService = null;
    private String appHomeDirectory;

    @Autowired
    public InitializeRequiredStartupFiles(ClassPathResourcesLoadService classPathResourcesLoadService) {
        this.classPathResourcesLoadService = classPathResourcesLoadService;
    }

    public void initialize() throws RuntimeException {
        try {
            appHomeDirectory = Props.getRequiredProperty(PropsIF.APP_HOME);
        } catch (Exception e) {
            throw new BeanInitializationException("app.home property not found in properties file ", e);
        }

        try {
            initializeAclFiles();
            initializeOpenDocFiles();
            initializeMsAccessFiles();
            overwriteVersionFile();
            createTMPFolder();
        } catch (ClassPathLoadResourceException ex) {
            Logger.getLogger(InitializeRequiredStartupFiles.class.getName()).log(Level.ALL, ex.getMessage(), ex);
            throw new BeanInitializationException(ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(InitializeRequiredStartupFiles.class.getName()).log(Level.ALL, ex.getMessage(), ex);
            throw new BeanInitializationException(ex.getMessage(), ex);
        }
    }

    private void initializeAclFiles() throws ClassPathLoadResourceException, IOException {
        List<String> subdirectories = new ArrayList<String>();
        subdirectories.add(appHomeDirectory);
        subdirectories.add(ACL_FOLDER_NAME);
        File directory = buildFileDirectory(subdirectories);
        File[] sourceFiles = classPathResourcesLoadService.loadAllFilesFromFolder(ACL_FOLDER_NAME + "/");
        for (File sourceFile : sourceFiles) {
            if (sourceFile.getName().contains(".prms") || sourceFile.getName().contains(".permissions")) {
                File destFile = new File(sourceFile.getName());
                FileUtils.copyFile(sourceFile, destFile);
            }
            FileUtils.copyFileToDirectory(sourceFile, directory);
        }
    }

    private void initializeMsAccessFiles() throws ClassPathLoadResourceException, IOException {
        List<String> subdirectories = new ArrayList<String>();
        subdirectories.add(appHomeDirectory);
        subdirectories.add(MS_ACCESS_FOLDER_HOME);
        File directory = buildFileDirectory(subdirectories);
        File[] files = classPathResourcesLoadService.loadAllFilesFromFolder(MS_ACCESS_FOLDER_HOME + "/");
        for (File file : files) {
            FileUtils.copyFileToDirectory(file, directory);
        }
    }

    private void initializeOpenDocFiles() throws ClassPathLoadResourceException, IOException {
        List<String> subdirectories = new ArrayList<String>();
        subdirectories.add(appHomeDirectory);
        subdirectories.add(OPENDOC_FOLDER_HOME);
        subdirectories.add("/ods");//OpenDoc has inside it a folder named ods , so we need to maintain original directory structure 
        File directory = buildFileDirectory(subdirectories);
        File[] files = classPathResourcesLoadService.loadAllFilesFromFolder(OPENDOC_FOLDER_HOME + "/");
        for (File file : files) {
            FileUtils.copyDirectory(file, directory);
        }
    }

    private void overwriteVersionFile() throws ClassPathLoadResourceException, IOException {
        List<String> subdirectories = new ArrayList<String>();
        subdirectories.add(appHomeDirectory);
        File directory = buildFileDirectory(subdirectories);
        File oldFile = classPathResourcesLoadService.loadFileFromRootClasspathDirectory(VERSION_FILE);
        FileUtils.copyFileToDirectory(oldFile, directory);
    }

    public void createTMPFolder() throws IOException {
        List<String> subdirectories = new ArrayList<String>();
        subdirectories.add(appHomeDirectory);
        subdirectories.add(TEMP_FOLDER);
        File directory = buildFileDirectory(subdirectories);
        if (Files.exists(directory.toPath())) {
            return;
        }
        if (directory.exists() && !directory.isDirectory()) {
            return;
        }
        directory.setWritable(true);
        directory.setReadable(true);
        boolean successfullyCreated = directory.mkdirs();
        if (!successfullyCreated) {
            throw new IOException("tmp directory could not be created");
        }
    }

    private File buildFileDirectory(List<String> subdirectories) {
        StringBuilder sb = new StringBuilder();
        for (String subdirectory : subdirectories) {
            sb.append(subdirectory).append("/");
        }
        File fileDirectory = new File(sb.toString());
        return fileDirectory;
    }

}
