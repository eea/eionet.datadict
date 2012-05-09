package eionet.meta.schemas;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eionet.meta.DDRuntimeException;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author Jaanus Heinlaid
 */
@Component
public class SchemaRepository {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SchemaRepository.class);

    /** */
    public static final String WORKING_COPY_SUFFIX = ".workingCopy";
    public static final String WORKING_COPY_DIR = "workingCopies";

    /** */
    public static final String REPO_PATH = Props.getRequiredProperty(PropsIF.SCHEMA_REPO_LOCATION);

    /**
     *
     * @return
     * @throws IOException
     */
    public File addSchema(FileBean fileBean, String schemaSetIdentifier, boolean overwrite) throws IOException {

        if (fileBean == null) {
            throw new IllegalArgumentException("File bean must not be null!");
        }

        File repoDirectory = new File(REPO_PATH);
        if (!repoDirectory.exists() || !repoDirectory.isDirectory()) {
            repoDirectory.mkdir();
        }

        // If schema set identifier is blank, consider it a root-level schema.
        File fileDirectory = StringUtils.isBlank(schemaSetIdentifier) ? repoDirectory : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            fileDirectory.mkdir();
        }

        File workingCopyDirectory = new File(fileDirectory, WORKING_COPY_DIR);
        if (!workingCopyDirectory.exists() || !workingCopyDirectory.isDirectory()) {
            workingCopyDirectory.mkdir();
        }

        File schemaFile = new File(workingCopyDirectory, fileBean.getFileName());
        if (schemaFile.exists() && schemaFile.isFile()) {
            if (overwrite == false) {
                throw new DDRuntimeException("File already exists, but overwrite not requested!");
            } else {
                schemaFile.delete();
            }
        }

        fileBean.save(schemaFile);
        if (schemaFile.exists() && schemaFile.isFile()) {
            return schemaFile;
        } else {
            throw new DDRuntimeException("Schema file creation threw no exceptions, yet the file does not exist!");
        }
    }

    /**
     *
     * @param fileName
     * @param schemaSetIdentifier
     * @param fileBean
     * @return
     * @throws IOException
     */
    public File reuploadSchema(String fileName, String schemaSetIdentifier, FileBean fileBean) throws IOException {

        if (fileBean == null) {
            throw new IllegalArgumentException("File bean must not be null!");
        }

        File repoDirectory = new File(REPO_PATH);
        if (!repoDirectory.exists() || !repoDirectory.isDirectory()) {
            repoDirectory.mkdir();
        }

        // If schema set identifier is blank, consider it a root-level schema.
        File fileDirectory = StringUtils.isBlank(schemaSetIdentifier) ? repoDirectory : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            fileDirectory.mkdir();
        }

        File workingCopyDirectory = new File(fileDirectory, WORKING_COPY_DIR);
        if (!workingCopyDirectory.exists() || !workingCopyDirectory.isDirectory()) {
            workingCopyDirectory.mkdir();
        }

        File schemaFile = new File(workingCopyDirectory, fileName);
        if (schemaFile.exists() && schemaFile.isFile()) {
            schemaFile.delete();
        }
        fileBean.save(schemaFile);

        return schemaFile;
    }

    /**
     *
     * @param relativePath
     * @return
     */
    public File getSchemaFile(String relativePath) {

        File file = new File(REPO_PATH, relativePath);
        if (file.exists() && file.isFile()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     *
     * @param fileName
     * @param schemaSetIdentifier
     * @return
     * @throws IOException
     */
    public boolean existsSchema(String fileName, String schemaSetIdentifier) throws IOException {

        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("File name must not be blank!");
        }

        boolean isRootLevel = StringUtils.isBlank(schemaSetIdentifier);
        File fileDirectory = isRootLevel ? new File(REPO_PATH) : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            return false;
        }

        File schemaFile = new File(fileDirectory, fileName);
        return schemaFile.exists() && schemaFile.isFile();
    }

    /**
     * Deletes the given schema file in the given schema set.
     *
     * @param fileName
     * @param schemaSetIdentifier
     */
    public void deleteSchema(String fileName, String schemaSetIdentifier) {

        boolean isRootLevel = StringUtils.isBlank(schemaSetIdentifier);
        File fileDirectory = isRootLevel ? new File(REPO_PATH) : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            return;
        }

        File schemaFile = new File(fileDirectory, fileName);
        if (schemaFile.exists() && schemaFile.isFile()) {
            LOGGER.debug("Deleting " + schemaFile);
            schemaFile.delete();
        }
    }

    /**
     * Delete whole schema set directory, including all schema files in it. So use with caution!
     *
     * @param fileName
     * @param schemaSetIdentifier
     * @throws IOException
     */
    public void deleteSchemaSet(String schemaSetIdentifier) throws IOException {

        File schemaSetDirectory = new File(REPO_PATH, schemaSetIdentifier);
        if (schemaSetDirectory.exists() && schemaSetDirectory.isDirectory()) {
            LOGGER.debug("Deleting " + schemaSetDirectory);
            FileUtils.deleteDirectory(schemaSetDirectory);
        }
    }

    /**
     *
     * @param schemaFileName
     * @param schemaSetIdentifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void cleanupCheckIn(String schemaFileName, String schemaSetIdentifier, List<String> schemasInDatabase)
    throws IOException {

        boolean isRootLevel = StringUtils.isBlank(schemaSetIdentifier);
        File fileDirectory = isRootLevel ? new File(REPO_PATH) : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            return;
        }

        File workingCopyDirectory = new File(fileDirectory, WORKING_COPY_DIR);
        if (!workingCopyDirectory.exists() || !workingCopyDirectory.isDirectory()) {
            workingCopyDirectory.mkdir();
        }

        File[] workingCopyFiles = workingCopyDirectory.listFiles();
        for (File workingCopyFile : workingCopyFiles) {

            if (StringUtils.isBlank(schemaFileName) || workingCopyFile.getName().equals(schemaFileName)){
                File originalFile = new File(fileDirectory, workingCopyFile.getName());
                if (originalFile.exists() && originalFile.isFile()){
                    LOGGER.debug("Deleting " + originalFile);
                    originalFile.delete();
                }
                LOGGER.debug("Moving " + workingCopyFile + " to " + originalFile);
                FileUtils.moveFile(workingCopyFile, originalFile);
            }
        }

        // If check-in of a schema set, delete whole working directory
        if (StringUtils.isBlank(schemaFileName)){
            FileUtils.deleteDirectory(workingCopyDirectory);
        }

        // Delete files not present in the database.
        File[] schemaFiles = fileDirectory.listFiles();
        for (File schemaFile : schemaFiles) {
            if (schemaFile.isFile() && !schemasInDatabase.contains(schemaFile.getName())) {
                LOGGER.debug("Deleting " + schemaFile);
                schemaFile.delete();
            }
        }
    }

    /**
     *
     * @param schemaFileName
     * @param schemaSetIdentifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void cleanupUndoCheckout(String schemaFileName, String schemaSetIdentifier, List<String> schemasInDatabase) throws IOException {

        boolean isRootLevel = StringUtils.isBlank(schemaSetIdentifier);
        File fileDirectory = isRootLevel ? new File(REPO_PATH) : new File(REPO_PATH, schemaSetIdentifier);
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            return;
        }

        File workingCopyDirectory = new File(fileDirectory, WORKING_COPY_DIR);
        if (workingCopyDirectory.exists() && workingCopyDirectory.isDirectory()) {

            // If undo-checkout of a schema set, delete whole working directory
            if (StringUtils.isBlank(schemaFileName)){
                LOGGER.debug("Deleting " + workingCopyDirectory);
                FileUtils.deleteDirectory(workingCopyDirectory);
            }
            else{
                File[] workingCopyFiles = workingCopyDirectory.listFiles();
                for (File workingCopyFile : workingCopyFiles) {
                    if (workingCopyFile.getName().equals(schemaFileName)){
                        LOGGER.debug("Deleting " + workingCopyFile);
                        workingCopyFile.delete();
                    }
                }
            }

        }

        // Delete files not present in the database.
        File[] schemaFiles = fileDirectory.listFiles();
        for (File schemaFile : schemaFiles) {
            if (schemaFile.isFile() && !schemasInDatabase.contains(schemaFile.getName())) {
                LOGGER.debug("Deleting " + schemaFile);
                schemaFile.delete();
            }
        }
    }

    /**
     *
     * @param file
     */
    public static void deleteQuietly(File file) {
        if (file != null) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
