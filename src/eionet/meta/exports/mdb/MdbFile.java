/*
 * Created on Feb 1, 2006
 */
package eionet.meta.exports.mdb;

import com.healthmarketscience.jackcess.*;

import java.sql.*;
import java.io.*;
import java.util.*;

import eionet.meta.*;
import eionet.util.*;

/**
 * @author jaanus
 */
public class MdbFile {
	
	/** */
	public static final String PROP_TMP_FILE_PATH = "mdb.tmp-file-path";
	public static final String PROP_LOG_FILE = "mdb.log-file";
	public static final String PROP_SCHEMA_URL = "mdb.vmd-schema-url";
	
	public static final String   VMD_TABLENAME = "VALIDATION_METADATA_DO_NOT_MODIFY";
	public static final String[] VMD_COLUMNS   = {"TableID", "SchemaURL", "DatasetSchemaURL"};
	
	/** */
	private static LogServiceIF log = null;
	
	/** */
	private Connection conn = null;
	private String dstID = null;
	private String fullPath = null;
	private DDSearchEngine searchEngine = null;
	
	private boolean vmdOnly = false;
	private String tblSchemaURL = null;
	private String dstSchemaURL = null;
		
	/*
	 * 
	 */
	private MdbFile(Connection conn, String dstID, String fullPath) throws MdbException{
		
		if (conn==null) throw new MdbException("SQL connection not given");
		if (dstID==null) throw new MdbException("Dataset ID not given");
		if (fullPath==null) throw new MdbException("File path not given");
		
		this.conn = conn;
		this.dstID = dstID;
		this.fullPath = fullPath;
		searchEngine = new DDSearchEngine(this.conn);
	}
	
	/*
	 * 
	 */
	public void setVmdOnly(boolean vmdOnly){
		this.vmdOnly = vmdOnly;
	}

	/*
	 * 
	 */
	public boolean getVmdOnly(){
		return vmdOnly;
	}

	/*
	 * 
	 */
	private File create() throws Exception{
		
		// if only creating metadata for automatic validation 
		if (vmdOnly) return createVmdOnly();

		Dataset dst = searchEngine.getDataset(dstID);
		if (dst==null) throw new MdbException("Dataset not found, id=" + dstID);
		
		File file = new File(fullPath);
		createDatabase(dst, file);
		return file;
	}

	/*
	 * 
	 */
	private Database createDatabase(Dataset dst, File file) throws Exception{

		Database db = null;
		try{
			db = Database.create(file);
			
			Vector tables = searchEngine.getDatasetTables(dstID);
			for (int i=0; tables!=null && i<tables.size(); i++){
				createTable((DsTable)tables.get(i), db);
			}
		}
		finally{
			if (db!=null){
				try{ db.close(); } catch (Throwable t){}
			}
		}
		
		return db;
	}
	
	/*
	 * 
	 */
	private void createTable(DsTable tbl, Database db) throws Exception{
		
		if (tbl==null) return;
		if (db==null) return;
		
		Vector gisColumns = new Vector();
		Vector nonGisColumns = new Vector();
		Vector elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());
		if (elems!=null && elems.size()>0){
			gisColumns = createGISColumns(elems);
			nonGisColumns = createNONGISColumns(elems);
		}
		
		boolean atLeastOneCreated = false;
		String tableName = tbl.getIdentifier();
		if (gisColumns!=null && gisColumns.size()>0){
			db.createTable(tableName, gisColumns);
			atLeastOneCreated = true;
		}

		if (nonGisColumns!=null && nonGisColumns.size()>0){
			if (atLeastOneCreated) tableName = tableName + "_meta";
			db.createTable(tableName, nonGisColumns);
			atLeastOneCreated = true;
		}
		
		if (!atLeastOneCreated){
			db.createTable(tableName, new Vector());
		}
	}

	/*
	 * 
	 */
	private Vector createGISColumns(Vector elems) throws Exception{
		
		Vector result = new Vector();
		
		if (elems==null || elems.size()==0) return result;
		
		int done = 0;
		for (int i=0; i<elems.size(); i++){
			
			Column col = null;
			DataElement elm = (DataElement)elems.get(i);
			if (elm.getGIS()!=null){ // we want only GIS elements here!
				col = createColumn((DataElement)elems.get(i));
				if (col!=null) result.add(col);
			}
		}
		
		return result;
	}

	/*
	 * 
	 */
	private Vector createNONGISColumns(Vector elems) throws Exception{

		Vector result = new Vector();
		
		if (elems==null || elems.size()==0) return result;
		
		int done = 0;
		for (int i=0; i<elems.size(); i++){
			
			Column col = null;
			DataElement elm = (DataElement)elems.get(i);
			if (elm.getGIS()==null){ // we want only NON-GIS elements here!
				col = createColumn((DataElement)elems.get(i));
				if (col!=null) result.add(col);
			}
		}
		
		return result;
	}
	
	/*
	 * 
	 */
	private Column createColumn(DataElement elm) throws SQLException{
		
		if (elm==null) return null;
		String colName = elm.getIdentifier();
		String elmDataType = elm.getAttributeValueByShortName("Datatype");
		if (colName==null || elmDataType==null) return null;
		
		int colType = Mdb.getMdbType(elmDataType);
		
		Column col = new Column();
		col.setName(colName);
		col.setSQLType(colType);
		
		return col;
	}

	/*
	 * 
	 */
	private File createVmdOnly() throws Exception{

		File file = new File(fullPath);
		Database db = null;
		try{
			db = Database.create(file);
			
			List cols = getVmdColumns();
			if (cols==null || cols.size()==0)
				throw new MdbException("No columns were added for validation metadata");

			db.createTable(VMD_TABLENAME, cols);
			Table vmdTable = db.getTable(VMD_TABLENAME);
			if (vmdTable==null) throw new NullPointerException();
			
			List rows = createVmdRows();
			if (rows==null || rows.size()==0)
				throw new MdbException("No rows were added for validation metadata");
				
			vmdTable.addRows(rows);
		}
		finally{
			if (db!=null){
				try{ db.close(); } catch (Throwable t){}
			}			
		}
		
		return file;
	}
	
	/*
	 * 
	 */
	private List createVmdRows() throws SQLException, IOException, MdbException{

		Vector ddTables = searchEngine.getDatasetTables(dstID);
		if (ddTables==null || ddTables.size()==0) return null;
		
		initSchemaURLs();
		
		Vector rows = new Vector();
		for (int i=0; ddTables!=null && i<ddTables.size(); i++){
			
			DsTable ddTable = (DsTable)ddTables.get(i);
				
			Object[] row = new Object[VMD_COLUMNS.length];
			row[0] = ddTable.getIdentifier();
			row[1] = tblSchemaURL + ddTable.getID();
			row[2] = dstSchemaURL + dstID;
			
			rows.add(row);
		}
		
		return (List)rows;
	}
	
	/*
	 * 
	 */
	private static List getVmdColumns() throws SQLException{
		Vector cols = new Vector();
		for (int i=0; i<VMD_COLUMNS.length; i++){
			Column col = new Column();
			String colName = VMD_COLUMNS[i];
			int colType = Mdb.getVmdColumnType(colName);
			col.setName(colName);
			col.setSQLType(colType);
			cols.add(col);
		}
		return cols;
	}
	
	/*
	 * 
	 */
	private void initSchemaURLs() throws MdbException{

		String schemaUrlBase = Props.getProperty(PROP_SCHEMA_URL);
		if (schemaUrlBase==null || schemaUrlBase.length()==0)
			throw new MdbException("Missing " + PROP_SCHEMA_URL + " property!");
		
		this.tblSchemaURL = schemaUrlBase + "TBL";
		this.dstSchemaURL = schemaUrlBase + "DST";
	}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	/*
	 * 
	 */
	public static File create(Connection conn, String dstID, String fullPath) throws Exception{
		
		return MdbFile.create(conn, dstID, fullPath, false);
	}

	/*
	 * 
	 */
	public static File create(Connection conn, String dstID, String fullPath, boolean vmdOnly)
																					throws Exception{		
		MdbFile mdbFile = new MdbFile(conn, dstID, fullPath);
		mdbFile.setVmdOnly(vmdOnly);
		return mdbFile.create();
	}
	
	/*
	 * 
	 */
	public static void main(String args[]){
		
		String dstID = (args!=null && args.length > 0) ? args[0] : null;		
		String fileFullPath = (args!=null && args.length > 1) ? args[1] : null;
		String vmdOnly = (args!=null && args.length > 2) ? args[2] : null;
		
		System.out.println("entered " + MdbFile.class.getName() + ".main() with " + args);
		
		try{
			if (dstID==null)
				throw new MdbException("Missing command line argument for dataset id");
			if (fileFullPath==null)
				throw new MdbException("Missing command line argument for file full path");
			
			Properties props = MdbFile.getProperties();
			
			Class.forName(props.getProperty(PropsIF.DBDRV));
			Connection conn = DriverManager.getConnection(
				props.getProperty(PropsIF.DBURL),
				props.getProperty(PropsIF.DBUSR),
				props.getProperty(PropsIF.DBPSW));
			
			if (vmdOnly==null)
				MdbFile.create(conn, dstID, fileFullPath);
			else
				MdbFile.create(conn, dstID, fileFullPath, Boolean.valueOf(vmdOnly).booleanValue());
		}
		catch (Throwable t){
			try{
				System.err.println("============>");
				System.err.println(t.getMessage());
				t.printStackTrace(System.err);
				System.err.println("============>");
			}
			finally{
				System.exit(1);
			}
		}
		
		System.exit(0);
	}
	
	/*
	 * 
	 */
	private static Properties getProperties() throws Throwable{
		
		Vector v = new Vector();
		v.add(PropsIF.DBDRV);
		v.add(PropsIF.DBURL);
		v.add(PropsIF.DBUSR);
		v.add(PropsIF.DBPSW);
		
		Properties props = new Properties();
		
		for (int i=0; i<v.size(); i++){
			String propName  = (String)v.get(i);
			String propValue = Props.getProperty(propName);
			if (propValue==null || propValue.length()==0)
				throw new MdbException("Could not find property: " + propName);
			else
				props.setProperty(propName, propValue);
		}
		
		return props; 
	}
	
	/*
	 * 
	 */
	private static void initLog(){
		
		String logFile = null;
		try{
			logFile = Props.getProperty(PROP_LOG_FILE);
		}
		catch (Throwable t){
		}
		
		if (logFile!=null && logFile.trim().length()>0)
			log = new Log4jLoggerImpl(logFile.trim());
		else
			log = null;
	}

	/*
	 * 
	 */
	private static void log(String msg){
		
		if (log!=null)
			log.debug(msg);
		else
			System.out.println(msg);
	}

	/*
	 * 
	 */
	private static void log(String msg, Throwable t){
		
		if (log!=null){
			log.debug(msg);
			log.debug(Util.getStack(t));
		}
		else{
			System.out.println(msg);
			System.out.println(Util.getStack(t));
		}
	}

	/*
	 * 
	 */
	private static void log(Throwable t){
		
		if (log!=null)
			log.debug(Util.getStack(t));
		else
			System.out.println(Util.getStack(t));
	}

	/*
	 * 
	 */
	private static void createTest() throws Exception{
		
		File file = null;
		if (File.separator.equals("/"))
			file = new File("/home/jaanus/test.mdb");
		else
			file = new File("D:\\projects\\datadict\\doc\\test.mdb");
		
		Database db = Database.create(file);
		
		Column a = new Column();
		a.setName("a");
		a.setSQLType(Types.INTEGER);
		Column b = new Column();
		b.setName("b");
		b.setSQLType(Types.VARCHAR);
		
		Vector v = new Vector();
		v.add(a);
		v.add(b);
		
		db.createTable("NewTable", v);
		
		db.close();

	}
}
