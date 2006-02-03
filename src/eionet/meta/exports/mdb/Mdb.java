/*
 * Created on Feb 2, 2006
 */
package eionet.meta.exports.mdb;

import java.io.*;
import java.sql.*;
import java.util.*;

import eionet.meta.DDSearchEngine;
import eionet.meta.exports.CachableIF;
import eionet.util.*;

import com.healthmarketscience.jackcess.DataTypes;

/**
 * @author jaanus
 */
public class Mdb implements CachableIF {
	
	/** */
	public static final String PROP_EXECUTABLE = "mdb.executable";
	
	/** */
	private static Hashtable mdbTypeMappings = null;
	private static byte DEFAULT_MDB_TYPE = DataTypes.TEXT;
	
	/** */
	private String cachePath = null;

	/* (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
	 */
	public void updateCache(String id) throws Exception {
		// XXX Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#clearCache(java.lang.String)
	 */
	public void clearCache(String id) throws Exception {
		// XXX Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
	 */
	public void setCachePath(String path) throws Exception {
		cachePath = path;
		if (cachePath!=null && !cachePath.endsWith(File.separator))
			cachePath = cachePath + File.separator;
	}

	/* (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
	 */
	public boolean isCached(String id) throws Exception {
		throw new Exception("This method is not implemented");
	}
	
	/*
	 * 
	 */
	public static File getCached(Connection conn, String dstID, String cachePath) throws Exception{
		
		if (conn==null) throw new MdbException("SQL connection not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		if (cachePath==null) throw new MdbException("Cache path not given");
		
		return getCached(new DDSearchEngine(conn), dstID, cachePath);
	}

	/*
	 * 
	 */
	public static File getCached(DDSearchEngine searchEngine, String dstID, String cachePath)
																				throws Exception{
		if (searchEngine==null) throw new MdbException("DDSearchEngine not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		if (cachePath==null) throw new MdbException("Cache path not given");
		
		if (!cachePath.endsWith(File.separator))
			cachePath = cachePath + File.separator;
		
		String filename = searchEngine.getCacheFileName(dstID, "dst", "mdb");
		if (filename==null || filename.length()==0)
			return null;
		else{
			File file = new File(cachePath + filename);
			if (!file.exists()){
				// FIXME- clear cahce if the file is really not exsiting any more
				return null;
			}
			else
				return file;
		}
	}
	
	/*
	 * 
	 */
	public static File getNew(Connection conn, String dstID, String fullPath) throws Exception{

		if (conn==null) throw new MdbException("SQL connection not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		if (fullPath==null) throw new MdbException("Full file path not given");
		
		boolean createInBackground = false;
		String jdkVersion = System.getProperty("java.version");
		if (jdkVersion!=null && jdkVersion.startsWith("1.") && jdkVersion.length()>=3){
			try{
				int i = Integer.parseInt(jdkVersion.substring(2,3));
				if (i<5) createInBackground = true;
			}
			catch (NumberFormatException nfe){}
		}
		
		File file = null;
		if (!createInBackground)
			file = MdbFile.create(conn, dstID, fullPath);
		else{
			file = new File(fullPath);
			createInBackground(dstID, fullPath);
		}
		
		if (file!=null && !file.exists())
			return null;
		else
			return file;
	}
	
	/*
	 * 
	 */
	private static void createInBackground(String dstID, String fullPath) throws Exception{
		
		String executable = Props.getProperty(PROP_EXECUTABLE);
		if (executable==null) throw new MdbException("Could not get property: " + PROP_EXECUTABLE);

		if (dstID==null) throw new MdbException("Dataset ID not given");
		if (fullPath==null) throw new MdbException("Full file path not given");
		
		String[] command = new String[3];
		command[0] = executable;
		command[1] = dstID;
		command[2] = fullPath;
		
		Process process = Runtime.getRuntime().exec(command);
		
		int exitValue = -1;
		int counter = 0;
		boolean done = false;		
		while (done==false && counter<=30){
			counter++;
			try{
				exitValue = process.exitValue();
				done = true;
			}
			catch (IllegalThreadStateException itse){
				Thread.sleep(2000);
			}
		}
		
		if (done==false)
			throw new MdbTimeoutException("Background creation was timed out");

		// log the background process standard output
		String line;
		BufferedReader rdr = null;
		try{
			rdr = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = rdr.readLine()) != null) {
				System.out.println(line);
			}
		}
		catch (Throwable e){
		}
		finally{
			try{
				if (rdr!=null) rdr.close();
			}
			catch (IOException ioe){}
		}
		
		// if the background process exited without problems, return
		if (exitValue==0)
			return;

		// the background process exited with error, get its error output and throw exception
		StringBuffer errorOutput = new StringBuffer();
		try{
			rdr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = rdr.readLine()) != null) {
				errorOutput.append(line);
				errorOutput.append("\n");
			}
		}
		catch (Exception e){
		}
		finally{
			try{
				if (rdr!=null) rdr.close();
			}
			catch (IOException ioe){}
		}
		
		if (errorOutput.length()>0)
			throw new MdbException("Background creation failed:\n" + errorOutput.toString());
		else
			throw new MdbException("Background creation failed, could not get any error output");
	}
	
	/*
	 * 
	 */
	public static String getFileNameFor(Connection conn, String dstID) throws Exception{
		
		if (conn==null) throw new MdbException("SQL connection not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		
		return getFileNameFor(new DDSearchEngine(conn), dstID);
	}

	/*
	 * 
	 */
	public static String getFileNameFor(DDSearchEngine searchEgnine, String dstID) throws Exception{
		
		if (searchEgnine==null) throw new MdbException("DDSearchEngine not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		
		StringBuffer result = new StringBuffer();
		String dstIdf = searchEgnine.getDatasetIdentifier(dstID);
		if (dstIdf==null || dstIdf.length()==0)
			result.append(dstID);
		else
			result.append(dstIdf);
		
		result.append(".mdb");
		return result.toString();
	}

	/*
	 * 
	 */
	public static byte getMdbType(String elmDataType){
		
		if (elmDataType==null) return DEFAULT_MDB_TYPE;
		if (mdbTypeMappings==null) initElmTypeMappings();
		
		Byte b = (Byte)mdbTypeMappings.get(elmDataType);
		if (b!=null)
			return b.byteValue();
		else
			return DEFAULT_MDB_TYPE;
	}
	
	/*
	 * 
	 */
	private static void initElmTypeMappings(){
		
		mdbTypeMappings = new Hashtable();
		
		mdbTypeMappings.put("string", new Byte(DataTypes.TEXT));
		mdbTypeMappings.put("boolean", new Byte(DataTypes.BOOLEAN));
		mdbTypeMappings.put("integer", new Byte(DataTypes.INT));
		mdbTypeMappings.put("date", new Byte(DataTypes.TEXT));
		mdbTypeMappings.put("float", new Byte(DataTypes.FLOAT));
	}
}
