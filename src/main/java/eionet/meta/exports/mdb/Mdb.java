/*
 * Created on Feb 2, 2006
 */
package eionet.meta.exports.mdb;

import java.io.File;
import java.sql.Connection;
import java.sql.Types;
import java.util.Hashtable;

import eionet.meta.DDSearchEngine;
import eionet.meta.exports.CachableIF;

/**
 * @author jaanus
 */
public class Mdb implements CachableIF {

    /** Types of the generated Access file. */
    @SuppressWarnings("rawtypes")
    private static Hashtable mdbTypeMappings = null;

    /** Default type of the generated Access files. */
    public static int DEFAULT_MDB_TYPE = Types.VARCHAR;

    /** Path to the cache directory of the generated Access files. */
    private String cachePath = null;

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
     */
    @Override
    public void updateCache(String id) throws Exception {
        // Empty implementation.
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
     */
    @Override
    public void setCachePath(String path) throws Exception {

        cachePath = path;
        if (cachePath != null && !cachePath.endsWith(File.separator)) {
            cachePath = cachePath + File.separator;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
     */
    @Override
    public boolean isCached(String id) throws Exception {
        throw new Exception("This method is not implemented");
    }

    /**
     * Get cached generated Access file.
     *
     * @param conn SQL connection to use.
     * @param dstID Id of the dataset whose cached file we're looking for.
     * @param cachePath Path to the cache.
     * @return The cached file. Null if not found.
     * @throws Exception If any sort of error happens.
     */
    public static File getCached(Connection conn, String dstID, String cachePath) throws Exception {

        if (conn == null) {
            throw new MdbException("SQL connection not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }
        if (cachePath == null) {
            throw new MdbException("Cache path not given");
        }

        return getCached(new DDSearchEngine(conn), dstID, cachePath);
    }

    /**
     * Does same as {@link #getCached(Connection, String, String)} but uses given {@link DDSearchEngine} instead of SQL connection.
     *
     * @param searchEngine Search engine connection to use.
     * @param dstID Id of the dataset whose cached file we're looking for.
     * @param cachePath Path to the cache.
     * @return The cached file. Null if not found.
     * @throws Exception If any sort of error happens.
     */
    public static File getCached(DDSearchEngine searchEngine, String dstID, String cachePath) throws Exception {
        if (searchEngine == null) {
            throw new MdbException("DDSearchEngine not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }
        if (cachePath == null) {
            throw new MdbException("Cache path not given");
        }

        if (!cachePath.endsWith(File.separator)) {
            cachePath = cachePath + File.separator;
        }

        String filename = searchEngine.getCacheFileName(dstID, "dst", "mdb");
        if (filename == null || filename.length() == 0) {
            return null;
        } else {
            File file = new File(cachePath + filename);
            if (!file.exists()) {
                // FIXME- clear cahce if the file is really not exsiting any more
                return null;
            } else {
                return file;
            }
        }
    }

    /**
     * Generates a new (i.e. un-cached) MS Access file for a dataset by the given id.
     *
     * @param conn SQL connection to use.
     * @param dstID The ID of the dataset.
     * @param fullPath Full path to the generated file.
     * @param vmdOnly If true, then only validation metadata will be generated.
     * @return The file.
     * @throws Exception If any sort of error happens.
     */
    public static File getNew(Connection conn, String dstID, String fullPath, boolean vmdOnly) throws Exception {

        if (conn == null) {
            throw new MdbException("SQL connection not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }
        if (fullPath == null) {
            throw new MdbException("Full file path not given");
        }

        File file = MdbFile.create(conn, dstID, fullPath, vmdOnly);
        if (file != null && !file.exists()) {
            return null;
        } else {
            return file;
        }
    }

    /**
     * Calls {@link #getFileNameFor(DDSearchEngine, String, boolean)} by constructing a {@link DDSearchEngine} with the given
     * SQL connection and using the rest of inputs as they are.
     *
     * @param conn SQL connection to use.
     * @param dstID Given dataset id.
     * @param vmdOnly If looking for validation metadata only.
     * @return The file name.
     * @throws Exception If any sort of errro happens.
     */
    public static String getFileNameFor(Connection conn, String dstID, boolean vmdOnly) throws Exception {
        if (conn == null) {
            throw new MdbException("SQL connection not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }

        return getFileNameFor(new DDSearchEngine(conn), dstID, vmdOnly);
    }

    /**
     * Get generated MS Access file name for the given dataset.
     *
     * @param searchEgnine The search engine to use.
     * @param dstID Given dataset id.
     * @param vmdOnly If looking for validation metadata only.
     * @return The file name.
     * @throws Exception If any sort of error happens.
     */
    public static String getFileNameFor(DDSearchEngine searchEgnine, String dstID, boolean vmdOnly) throws Exception {
        if (searchEgnine == null) {
            throw new MdbException("DDSearchEngine not given");
        }
        if (dstID == null) {
            throw new MdbException("Dataset ID not given");
        }

        StringBuffer result = new StringBuffer();
        String dstIdf = searchEgnine.getDatasetIdentifierById(dstID);
        if (dstIdf == null || dstIdf.length() == 0) {
            result.append(dstID);
        } else {
            result.append(dstIdf);
        }

        if (vmdOnly) {
            result.append(" - VALIDATION METADATA");
        }

        result.append(".mdb");
        return result.toString();
    }

    /**
     * Get the type of the given validation metadata column.
     *
     * @param vmdColumnName Given column.
     * @return The type.
     */
    public static int getVmdColumnType(String vmdColumnName) {
        return Types.LONGVARCHAR;
    }

    /**
     * Gets the MS Access data type for the given DD element data type.
     *
     * @param elmDataType The given element data type.
     * @return MS Access file type.
     */
    public static int getMdbType(String elmDataType) {

        if (elmDataType == null) {
            return DEFAULT_MDB_TYPE;
        }
        if (mdbTypeMappings == null) {
            initElmTypeMappings();
        }

        Integer integer = (Integer) mdbTypeMappings.get(elmDataType);
        if (integer != null) {
            return integer.intValue();
        } else {
            return DEFAULT_MDB_TYPE;
        }
    }

    /**
     * Initialize mappings between the data types of DD and MS Access.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void initElmTypeMappings() {

        mdbTypeMappings = new Hashtable();

        mdbTypeMappings.put("string", new Integer(Types.VARCHAR));
        mdbTypeMappings.put("boolean", new Integer(Types.BOOLEAN));
        mdbTypeMappings.put("integer", new Integer(Types.INTEGER));
        mdbTypeMappings.put("date", new Integer(Types.DATE));
        mdbTypeMappings.put("float", new Integer(Types.FLOAT));
        mdbTypeMappings.put("double", new Integer(Types.DOUBLE));
        mdbTypeMappings.put("decimal", new Integer(Types.DECIMAL));
    }
}
