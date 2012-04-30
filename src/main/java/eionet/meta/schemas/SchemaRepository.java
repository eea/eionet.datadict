package eionet.meta.schemas;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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

    private static final String WORKING_COPY_SUFFIX = ".workingCopy";
    /** */
    public static final String REPO_PATH = Props.getRequiredProperty(PropsIF.SCHEMA_REPO_LOCATION);

    /**
     *
     * @return
     * @throws IOException
     */
    public File addSchema(FileBean fileBean, String schemaSetIdentifier, boolean overwrite) throws IOException {

        if (fileBean == null || StringUtils.isBlank(schemaSetIdentifier)) {
            throw new IllegalArgumentException("File bean and schema set identifier must not be null or blank!");
        }

        File repoLocation = new File(REPO_PATH);
        if (!repoLocation.exists() || !repoLocation.isDirectory()) {
            repoLocation.mkdir();
        }

        File schemaSetLocation = new File(REPO_PATH, schemaSetIdentifier);
        if (!schemaSetLocation.exists() || !schemaSetLocation.isDirectory()) {
            schemaSetLocation.mkdir();
        }

        File schemaLocation = new File(schemaSetLocation, fileBean.getFileName());
        if (schemaLocation.exists() && schemaLocation.isFile()) {
            if (overwrite == false) {
                throw new DDRuntimeException("File already exists, but overwrite not requested!");
            } else {
                schemaLocation.delete();
            }
        }

        fileBean.save(schemaLocation);
        if (schemaLocation.exists() && schemaLocation.isFile()) {
            return schemaLocation;
        } else {
            throw new DDRuntimeException("Schema file creation threw no exceptions, yet the file does not exist!");
        }
    }

    /**
     *
     * @param relativePath
     * @return
     */
    public File getSchemaFile(String relativePath){

        File file = new File(REPO_PATH, relativePath);
        if (file.exists() && file.isFile()){
            return file;
        }
        else{
            return null;
        }
    }

    /**
     *
     * @param fileBean
     * @param schemaSetIdentifier
     * @return
     * @throws IOException
     */
    public boolean existsSchema(FileBean fileBean, String schemaSetIdentifier) throws IOException {

        if (fileBean == null || StringUtils.isBlank(schemaSetIdentifier)) {
            throw new IllegalArgumentException("File bean and schema set identifier must not be null or blank!");
        }

        File repoLocation = new File(REPO_PATH);
        if (!repoLocation.exists() || !repoLocation.isDirectory()) {
            return false;
        }

        File schemaSetLocation = new File(REPO_PATH, schemaSetIdentifier);
        if (!schemaSetLocation.exists() || !schemaSetLocation.isDirectory()) {
            return false;
        }

        File schemaLocation = new File(schemaSetLocation, fileBean.getFileName());
        return schemaLocation.exists() && schemaLocation.isFile();
    }

    /**
     * Deletes the given schema file in the given schema set.
     *
     * @param fileName
     * @param schemaSetIdentifier
     */
    public void deleteSchema(String fileName, String schemaSetIdentifier) {

        File schemaSetLocation = new File(REPO_PATH, schemaSetIdentifier);
        File schemaLocation = new File(schemaSetLocation, fileName);
        if (schemaLocation.exists() && schemaLocation.isFile()){
            schemaLocation.delete();
        }
    }

    /**
     * Delete whole schema set directory, including all schema files in it.
     * So use with caution!
     *
     * @param fileName
     * @param schemaSetIdentifier
     * @throws IOException
     */
    public void deleteSchemaSet(String schemaSetIdentifier) throws IOException {

        File schemaSetLocation = new File(REPO_PATH, schemaSetIdentifier);
        if (schemaSetLocation.exists() && schemaSetLocation.isDirectory()){
            FileUtils.deleteDirectory(schemaSetLocation);
        }
    }

    /**
     *
     * @param identifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void cleanupCheckInSchemaSet(String identifier, List<String> schemasInDatabase) throws IOException{

        File schemaSetLocation = new File(REPO_PATH, identifier);
        if (schemaSetLocation==null || !schemaSetLocation.isDirectory()){
            return;
        }

        // Rename schema working copies to their original file names
        File[] schemaFiles = schemaSetLocation.listFiles();
        for (File schemaFile : schemaFiles){
            if (schemaFile.isFile() && schemaFile.getName().endsWith(WORKING_COPY_SUFFIX)){
                String originalFileName = StringUtils.substringBefore(schemaFile.getName(), WORKING_COPY_SUFFIX);
                if (StringUtils.isNotBlank(originalFileName)){
                    File originalFile = new File(schemaSetLocation, originalFileName);
                    if (originalFile.exists() && originalFile.isFile()){
                        originalFile.delete();
                        schemaFile.renameTo(originalFile);
                    }
                }
            }
        }

        // Delete files not present in the database.
        for (File schemaFile : schemaFiles){
            if (schemaFile.isFile() && !schemasInDatabase.contains(schemaFile.getName())){
                schemaFile.delete();
            }
        }
    }

    /**
     *
     * @param identifier
     * @param schemasInDatabase
     * @throws IOException
     */
    public void cleanupUndoCheckoutSchemaSet(String identifier, Set<String> schemasInDatabase) throws IOException{

        File schemaSetLocation = new File(REPO_PATH, identifier);
        if (schemaSetLocation==null || !schemaSetLocation.isDirectory()){
            return;
        }

        // Rename schema working copies to their original file names
        File[] schemaFiles = schemaSetLocation.listFiles();
        for (File schemaFile : schemaFiles){

            if (schemaFile.isFile() && schemaFile.getName().endsWith(WORKING_COPY_SUFFIX)){
                schemaFile.delete();
            }
        }

        // Delete files not present in the database.
        for (File schemaFile : schemaFiles){
            if (schemaFile.isFile() && !schemasInDatabase.contains(schemaFile.getName())){
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
