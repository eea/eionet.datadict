package eionet.meta.schemas;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eionet.meta.DDRuntimeException;
import eionet.meta.dao.domain.SchemaSet;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import org.springframework.context.annotation.DependsOn;

/**
 *
 * @author Jaanus Heinlaid
 */
@Component
@DependsOn ("contextAware")
public class SchemaRepository {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaRepository.class);

    /** */
    public static final String WORKING_COPY_DIR = ".workingCopy";

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

        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            repoDir.mkdir();
        }

        File workingCopyDir = null;
        boolean isRootLevelSchema = StringUtils.isBlank(schemaSetIdentifier);
        if (isRootLevelSchema) {
            workingCopyDir = new File(repoDir, WORKING_COPY_DIR);
        } else {
            workingCopyDir = new File(repoDir, schemaSetIdentifier + WORKING_COPY_DIR);
        }

        if (workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            workingCopyDir.mkdir();
        }

        File schemaFile = new File(workingCopyDir, fileBean.getFileName());
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
     * Copies one file to another location.
     *
     * @param fileName
     * @param schemaSetIdentifier
     *            may be null if it is root level schema
     * @param workingCopy
     * @param newFileName
     * @param newSchemaSetIdentifier
     * @param newWorkingCopy
     * @throws ServiceException
     */
    public void copySchema(String fileName, String schemaSetIdentifier, boolean workingCopy, String newFileName,
            String newSchemaSetIdentifier, boolean newWorkingCopy) throws ServiceException {
        try {
            File srcFile = getSchemaFile(fileName, schemaSetIdentifier, workingCopy);
            File destFile = new File(REPO_PATH, getSchemaRelativePath(newFileName, newSchemaSetIdentifier, newWorkingCopy));

            if (destFile.exists()) {
                throw new ServiceException("Failed to copy file because it already exists: " + newSchemaSetIdentifier + "/"
                        + newFileName);
            }

            FileUtils.copyFile(srcFile, destFile);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to copy file: " + e.toString(), e);
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

        boolean isRootLevelSchema = schemaSetIdentifier.equals(SchemaSet.ROOT_IDENTIFIER);
        File workingCopyDirectory = null;
        if (isRootLevelSchema) {
            workingCopyDirectory = new File(REPO_PATH, WORKING_COPY_DIR);
        } else {
            workingCopyDirectory = new File(REPO_PATH, schemaSetIdentifier + WORKING_COPY_DIR);
        }
        if (!workingCopyDirectory.exists() || !workingCopyDirectory.isDirectory()) {
            throw new SchemaRepositoryException("Working copy directory not found!");
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
     * @param workingCopy
     * @return
     * @throws ServiceException
     */
    public File getSchemaFile(String fileName, String schemaSetIdentifier, boolean workingCopy) throws ServiceException {

        String relativePath = getSchemaRelativePath(fileName, schemaSetIdentifier, workingCopy);
        return getSchemaFile(relativePath);
    }

    /**
     * Returns the content as string from the schema file.
     *
     * @param relativePath
     * @return
     * @throws ServiceException
     */
    public String getSchemaString(String fileName, String schemaSetIdentifier, boolean workingCopy) throws ServiceException {

        File schemaFile = getSchemaFile(fileName, schemaSetIdentifier, workingCopy);
        if (schemaFile != null) {
            try {
                return FileUtils.readFileToString(schemaFile);
            } catch (IOException e) {
                throw new ServiceException("Failed to get the schema contents: " + e.getMessage(), e);
            }
        } else {
            throw new ServiceException("Failed to find such a schema file!");
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
     * @throws IOException
     */
    public void checkInSchema(String schemaFileName) throws IOException {

        if (StringUtils.isBlank(schemaFileName)) {
            throw new IllegalArgumentException("Schema file name must not be blank!");
        }

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // Ensure we have a working copy directory.
        File workingCopyDir = new File(REPO_PATH, WORKING_COPY_DIR);
        if (!workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            throw new SchemaRepositoryException("Working copy directory of root-level schemas not found!");
        }

        // Ensure we have a working copy file.
        File workingFile = new File(workingCopyDir, schemaFileName);
        if (!workingFile.exists() || !workingFile.isFile()) {
            throw new SchemaRepositoryException("Working copy file not found!");
        }

        // Replace original file with the working copy.
        File originalFile = new File(REPO_PATH, schemaFileName);
        if (originalFile.exists() && originalFile.isFile()) {
            LOGGER.debug("Deleting " + originalFile);
            originalFile.delete();
        }
        FileUtils.moveFile(workingFile, originalFile);
    }

    /**
     *
     * @param schemaFileName
     * @param schemasInDatabase
     * @throws IOException
     */
    public void undoCheckOutSchema(String schemaFileName, List<String> schemasInDatabase) throws IOException {

        if (StringUtils.isBlank(schemaFileName)) {
            throw new IllegalArgumentException("Schema file name must not be blank!");
        }

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // If we don't have a working copy directory anyway, exit silently.
        File workingCopyDir = new File(REPO_PATH, WORKING_COPY_DIR);
        if (!workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            return;
        }

        // Delete working file if it exists.
        File workingFile = new File(workingCopyDir, schemaFileName);
        if (workingFile.exists() && workingFile.isFile()) {
            workingFile.delete();
        }
    }

    /**
     *
     * @param schemaSetIdentifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void checkInSchemaSet(String schemaSetIdentifier, List<String> schemasInDatabase) throws IOException {

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // If we don't have a working directory, assume the schema set is checked in without any schemas in it,
        // so exit silently.
        File workingCopyDir = new File(REPO_PATH, schemaSetIdentifier + WORKING_COPY_DIR);
        if (!workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            return;
        }

        // If we don't have the original directory, assume it's brand new schema set,
        // so simply rename the working copy directory to the original one, and exit.
        File schemaSetDir = new File(REPO_PATH, schemaSetIdentifier);
        if (!schemaSetDir.exists() || !schemaSetDir.isDirectory()) {
            FileUtils.moveDirectory(workingCopyDir, schemaSetDir);
            return;
        }

        // Copy files from working copy directory to the original directory, doing overwrite if needed.
        File[] workingFiles = workingCopyDir.listFiles();
        for (File workingFile : workingFiles) {

            File originalFile = new File(schemaSetDir, workingFile.getName());
            if (originalFile.exists() && originalFile.isFile()) {
                LOGGER.debug("Deleting " + originalFile);
                originalFile.delete();
            }
            LOGGER.debug("Moving " + workingFile + " to " + originalFile);
            FileUtils.moveFile(workingFile, originalFile);
        }

        // Delete working copy directory
        FileUtils.deleteDirectory(workingCopyDir);

        // Delete original files that are not present in the database any more (i.e. were removed from the working copy).
        File[] schemaFiles = schemaSetDir.listFiles();
        for (File schemaFile : schemaFiles) {
            if (schemaFile.isFile() && !schemasInDatabase.contains(schemaFile.getName())) {
                LOGGER.debug("Deleting " + schemaFile);
                schemaFile.delete();
            }
        }
    }

    /**
     *
     * @param schemaSetIdentifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void undoCheckOutSchemaSet(String schemaSetIdentifier, List<String> schemasInDatabase) throws IOException {

        if (StringUtils.isBlank(schemaSetIdentifier)) {
            throw new IllegalArgumentException("Schema set identifier must not be blank!");
        }

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // If we don't have a working copy directory anyway, exit silently.
        // But if we do, delete it.
        File workingCopyDir = new File(REPO_PATH, schemaSetIdentifier + WORKING_COPY_DIR);
        if (!workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            return;
        } else {
            FileUtils.deleteDirectory(workingCopyDir);
        }
    }

    /**
     * Copies the given source schema set directory to the given destination schema set directory. The latter will be appended with
     * {@link #WORKING_COPY_DIR}. Given directory names must not be blank. Throws a {@link SchemaRepositoryException} if the source
     * directory does not exist, or if a destination directory by the given name already exists.
     *
     * @param srcIdentifier
     * @param dstIdentifier
     * @throws IOException
     */
    public void copySchemaSet(String srcIdentifier, String dstIdentifier) throws IOException {

        if (StringUtils.isBlank(srcIdentifier) || StringUtils.isBlank(dstIdentifier)) {
            throw new IllegalArgumentException("Source identifier and destination identifier must not be blank!");
        }

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // If source directory not existing, assume this is a schema set without any schemas in it yet.
        // In this case simply exit silently.
        File srcDir = new File(REPO_PATH, srcIdentifier);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return;
        }

        // If destination directory exists already, throw exception.
        File dstDir = new File(REPO_PATH, dstIdentifier + WORKING_COPY_DIR);
        if (dstDir.exists()) {
            throw new SchemaRepositoryException("Destination directory already existing!");
        }

        FileUtils.copyDirectory(srcDir, dstDir);
    }

    /**
     * Calls {@link #copySchemaSet(String, String)} with both of the inputs set to the given schema set identifier. The latter must
     * not be null or blank.
     *
     * @param schemaSetIdentifier
     * @throws IOException
     */
    public void checkOutSchemaSet(String schemaSetIdentifier) throws IOException {

        if (StringUtils.isBlank(schemaSetIdentifier)) {
            throw new IllegalArgumentException("Schema set identifier must not be blank!");
        }

        copySchemaSet(schemaSetIdentifier, schemaSetIdentifier);
    }

    /**
     *
     * @param srcIdentifier
     * @param dstIdentifier
     * @throws IOException
     */
    public void newVersionSchemaSet(String srcIdentifier, String dstIdentifier) throws IOException {

        copySchemaSet(srcIdentifier, dstIdentifier + WORKING_COPY_DIR);
    }

    /**
     * Makes a copy of the given root-level schema (given by file name) into the {@link #WORKING_COPY_DIR} directory in repository
     * root.
     *
     * @param schemaFileName
     * @throws IOException
     */
    public void checkOutSchema(String schemaFileName) throws IOException {

        if (StringUtils.isBlank(schemaFileName)) {
            throw new IllegalArgumentException("Schema file name must not be blank!");
        }

        // If repository not created yet, throw exception.
        File repoDir = new File(REPO_PATH);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new SchemaRepositoryException("Repository not created yet!");
        }

        // Make sure the schema file that is being checked out, exists actually.
        File schemaFile = new File(repoDir, schemaFileName);
        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new SchemaRepositoryException("Schema file not existing: " + schemaFile);
        }

        // Ensure the directory of working copies of root-level schemas exists.
        File workingCopyDir = new File(repoDir, WORKING_COPY_DIR);
        if (!workingCopyDir.exists() || !workingCopyDir.isDirectory()) {
            workingCopyDir.mkdir();
        }

        // Make sure there is no working copy of this file yet.
        File workingFile = new File(workingCopyDir, schemaFileName);
        if (workingFile.exists() && workingFile.isFile()) {
            throw new SchemaRepositoryException("A working copy of this schema file already exists!");
        }

        FileUtils.copyFile(schemaFile, workingFile);
    }

    /**
     * Returns the relative path of the given schema. It is relative the schema repository location and will have NO slash at the
     * beginning. For example, if the schema repository location is /var/lib/schemas and there is a schema located at
     * /var/lib/schemas/water/river.xsd, then the relative path of that schema is "water/river.xsd".
     *
     * @param schemaFileName
     *            The filename of the schema (required).
     * @param schemaSetIdentifier
     *            The schema set containing the schema (can be blank, meaning a root-level schema).
     * @param isWorkingCopy
     *            Indicates if the schema or the schema set is a working copy.
     * @return The relative path.
     */
    public String getSchemaRelativePath(String schemaFileName, String schemaSetIdentifier, boolean isWorkingCopy) {

        if (StringUtils.isBlank(schemaFileName)) {
            throw new IllegalArgumentException("At least the file name must be given!");
        }

        StringBuilder relativePath = new StringBuilder();
        if (StringUtils.isNotBlank(schemaSetIdentifier)) {
            relativePath.append(schemaSetIdentifier);
        }
        if (isWorkingCopy) {
            relativePath.append(WORKING_COPY_DIR);
        }
        if (relativePath.length() > 0) {
            relativePath.append("/");
        }
        relativePath.append(schemaFileName);

        return relativePath.toString();
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
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
