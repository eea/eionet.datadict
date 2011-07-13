package eionet.meta.filestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import eionet.meta.DownloadServlet;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * This class provides services related to the DD file store. This is the store where all user-uploaded files should be stored.
 *
 * The DD file store is simply a dedicated directory in the file system. It can have any depth of sub-directories and files in them.
 *
 * To the end user, the files in file store are served by the {@link DownloadServlet}.
 *
 * This class provides services like add, get and delete files to/from the file store.
 *
 * @author Jaanus Heinlaid
 *
 */
public class FileStore {

    /** Static logger. */
    private static final LogServiceIF LOGGER = new Log4jLoggerImpl();

    /** Full path to the file store's root directory. */
    public static final String PATH = Props.getRequiredProperty(PropsIF.FILESTORE_PATH);

    /** A {@link File} pointer to the file store's root directory. */
    private File rootDir;

    /**
     *
     */
    private FileStore() {
        rootDir = new File(FileStore.PATH);
    }

    /**
     *
     * @return
     */
    public static FileStore getInstance() {
        return new FileStore();
    }

    /**
     *
     * @param fileName
     * @param overwrite
     * @param inputStream
     * @return File
     * @throws IOException
     */
    public File add(String fileName, boolean overwrite, InputStream inputStream) throws IOException {

        File filePath = prepareFileWrite(fileName, overwrite);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return filePath;
    }

    /**
     *
     * @param fileName
     * @param overwrite
     * @param reader
     * @return File
     * @throws IOException
     */
    public File add(String fileName, boolean overwrite, Reader reader) throws IOException {

        File filePath = prepareFileWrite(fileName, overwrite);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            IOUtils.copy(reader, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return null;
    }

    /**
     *
     * @param fileName
     * @param overwrite
     * @throws FileAlreadyExistsException
     */
    protected File prepareFileWrite(String fileName, boolean overwrite) throws FileAlreadyExistsException {

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            // creates the directory, including any necessary but nonexistent parent directories
            rootDir.mkdirs();
        }

        File filePath = new File(rootDir, fileName);
        if (filePath.exists() && filePath.isFile()) {

            if (overwrite == false) {
                throw new FileAlreadyExistsException("File already exists: " + fileName);
            } else {
                filePath.delete();
            }
        }

        return filePath;
    }

    /**
     *
     * @param fileName
     */
    public void delete(String fileName) {

        File file = new File(rootDir, fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     *
     * @param renamings
     */
    public void rename(Map<String, String> renamings) {

        int renamedCount = 0;
        if (renamings != null) {

            for (Map.Entry<String, String> entry : renamings.entrySet()) {

                String oldName = entry.getKey();
                String newName = entry.getValue();

                File file = new File(rootDir, oldName);
                if (file.exists() && file.isFile()) {

                    file.renameTo(new File(rootDir, newName));
                    renamedCount++;
                }
            }
        }

        LOGGER.debug("Total of " + renamedCount + " files renamed in the file store");
    }

    /**
     *
     * @param filePath
     * @return File
     */
    public File get(String filePath) {

        File file = new File(rootDir, filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        } else {
            return file;
        }
    }
}
