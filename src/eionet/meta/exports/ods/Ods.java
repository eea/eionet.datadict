/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.exports.ods.tags.NumberStyle;
import eionet.meta.exports.ods.tags.Style;
import eionet.meta.exports.ods.tags.Table;

/**
 * 
 * @author jaanus
 */
public abstract class Ods {
	
	/** */
	public static final String DOS_EXTENSION = "ods";
	
	public static final String INTEGER_STYLE_NAME = "ce2";
	public static final String DEFAULT_STYLE_NAME = "Default";
	
	public static final String ODS_FILE_NAME = "template.ods";
	public static final String CONTENT_FILE_NAME = "content.xml";
	public static final String META_FILE_NAME = "meta.xml";
	public static final int BUF_SIZE = 1024;
	
	/** */	
	protected DDSearchEngine searchEngine = null;
	protected String finalFileName = null;
	protected String schemaURLTrailer = null;	
	
	protected Vector numberStyles = null;
	protected Vector styles = null;
	protected Vector tables = null;
	
	private String workingFolderPath = null;
	private String schemaURLBase = null;
	
	/**
	 * 
	 * @return
	 */
	protected final int getTableCount(){
		return tables==null ? 0 : tables.size();
	}
	
	/**
	 * 
	 * @return
	 */
	protected final int getTotalColumnCount(){
		
		int count=0;
		for (int i=0; tables!=null && i<tables.size(); i++){
			Table table = (Table)tables.get(i);
			Vector cols = table.getTableColumns();
			if (cols!=null)
				count = count + cols.size();
		}
		
		return count;
	}

	/*
	 * 
	 */
	protected void addNumberStyle(NumberStyle numberStyle){
		
		if (numberStyles==null)
			numberStyles = new Vector();
		
		numberStyles.add(numberStyle);
	}

	/*
	 * 
	 */
	protected void addStyle(Style style){
		
		if (styles==null)
			styles = new Vector();
		
		styles.add(style);
	}

	/*
	 * 
	 */
	protected void addTable(Table table){
		
		if (tables==null)
			tables = new Vector();
		
		tables.add(table);
	}

	/*
	 * 
	 */
	protected void prepareTbl(DsTable tbl) throws Exception{
		
		Table tableTag = new Table();
		tableTag.setTableName(tbl.getIdentifier());
		tableTag.setSchemaURLTrailer("TBL" + tbl.getID());
		
		Vector elms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
		for (int i=0; elms!=null && i<elms.size(); i++){
			DataElement elm = (DataElement)elms.get(i);
			String defaultCellStyleName = getDefaultCellStyleName(elm);
			tableTag.addTableColumn(defaultCellStyleName);
			tableTag.addColumnHeader(elm.getIdentifier());
		}
		
		addTable(tableTag);
	}

	/*
	 * 
	 */
	protected String getDefaultCellStyleName(DataElement elm){
		
		String defaultCellStyleName = DEFAULT_STYLE_NAME;
		String elmDataType = elm.getAttributeValueByShortName("Datatype");			
		if (elmDataType!=null){
			if (elmDataType.equals("integer"))
				defaultCellStyleName = INTEGER_STYLE_NAME;
			else if (elmDataType.equals("float") || elmDataType.equals("double")){
				String decimalPrecision = elm.getAttributeValueByShortName("DecimalPrecision");
				if (decimalPrecision!=null && decimalPrecision.length()>0){
					try{
						Integer.parseInt(decimalPrecision);
						
						int count = numberStyles==null ? 0 : numberStyles.size();
						String numberStyleName = "N" + String.valueOf(2 + count); 
						NumberStyle numberStyle = new NumberStyle();
						numberStyle.setStyleName(numberStyleName);
						numberStyle.setMinIntegerDigits("1");
						numberStyle.setDecimalPlaces(decimalPrecision);
						
						count = styles==null ? 0 : styles.size();
						defaultCellStyleName = "ce" + String.valueOf(3 + count);
						Style style = new Style();
						style.setStyleName(defaultCellStyleName);
						style.setDataStyleName(numberStyleName);
						
						addNumberStyle(numberStyle);
						addStyle(style);
					}
					catch (NumberFormatException nfe){						
					}
				}
			}
		}
		
		return defaultCellStyleName;
	}

	/**
	 * 
	 * @param intoStr
	 */
	private String writeContentInto(String intoStr){
		
		String str = new String(intoStr);
		
		for (int i=0; numberStyles!=null && i<numberStyles.size(); i++){
			NumberStyle numberStyle = (NumberStyle)numberStyles.get(i);
			str = numberStyle.writeInto(str);
		}

		for (int i=0; styles!=null && i<styles.size(); i++){
			Style style = (Style)styles.get(i);
			str = style.writeInto(str);
		}

		for (int i=0; tables!=null && i<tables.size(); i++){
			Table table = (Table)tables.get(i);
			str = table.writeContentInto(str);
		}

		return str;
	}
	
	/**
	 * 
	 * @param intoStr
	 * @return
	 */
	private String writeMetaInto(String intoStr){
		
		if (intoStr==null || intoStr.length()==0)
			return intoStr;
		
		String officeMeta = new String("</office:meta>");
		int index = intoStr.indexOf(officeMeta);
		if (index<0)
			return intoStr;

		StringBuffer buf = new StringBuffer();
		buf.append(intoStr.substring(0, index));
		
		// write creation date & DublinCore date
		String date = Ods.getDate(System.currentTimeMillis());
		buf.append("<meta:creation-date>");
		buf.append(date);
		buf.append("</meta:creation-date>");
		buf.append("<dc:date>");
		buf.append(date);
		buf.append("</dc:date>");
		
		// write schemaURL of this ods
		if (schemaURLTrailer!=null && schemaURLBase!=null){
			buf.append("<meta:user-defined meta:name=\"schema-url\">");
			buf.append(schemaURLBase + schemaURLTrailer);
			buf.append("</meta:user-defined>");
		}
		
		// if this is DstOds, write schemaURL of each table
		int tableCount = getTableCount();
		if (this instanceof DstOds && tableCount>0){			
			StringBuffer buf1 = new StringBuffer();
			for (int i=0; i<tables.size(); i++){
				Table table = (Table)tables.get(i);
				String tableName = table.getTableName();
				String tableSchemaURLTrailer = table.getSchemaURLTrailer();
				if (tableName!=null && tableSchemaURLTrailer!=null && schemaURLBase!=null){
					if (i>0) buf1.append(";");
					buf1.append("tableName=");
					buf1.append(tableName);
					buf1.append(",tableSchemaURL=");
					buf1.append(schemaURLBase + tableSchemaURLTrailer);
				}
			}
			
			if (buf1.length()>0){
				buf.append("<meta:user-defined meta:name=\"table-schema-urls\">");
				buf.append(buf1.toString());
				buf.append("</meta:user-defined>");
			}
		}
		
		// write document statistics
		int totalColumnCount = getTotalColumnCount();
		if (tableCount>0 || totalColumnCount>0){
			
			buf.append("<meta:document-statistic");
			if (tableCount>0){
				buf.append(" meta:table-count=\"");
				buf.append(tableCount);
				buf.append("\"");
			}

			if (totalColumnCount>0){
				buf.append(" meta:cell-count=\"");
				buf.append(totalColumnCount);
				buf.append("\"");
			}
			buf.append("/>");
		}
		
		buf.append(intoStr.substring(index));
		
		return buf.toString();
	}

	/**
	 * 
	 * @param folderPath
	 */
	public void setWorkingFolderPath(String folderPath) {
		this.workingFolderPath = folderPath;
		if (!this.workingFolderPath.endsWith(File.separator))
			this.workingFolderPath = this.workingFolderPath + File.separator;
	}

	/**
	 * 
	 *
	 */
	private String fileToString(File file) throws Exception{
		
		String result = null;
		
		int i = 0;
		byte[] buf = new byte[BUF_SIZE];
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		
		try{
			in = new FileInputStream(file);
			out = new ByteArrayOutputStream();
			while ((i=in.read(buf, 0, buf.length)) != -1){
				out.write(buf, 0, i);
			}
			
			out.flush();
			result = out.toString();
		}
		finally{
			if (in!=null) in.close();
			if (out!=null) out.close();
		}
		
		return result;
	}
	
	/**
	 * 
	 *
	 */
	private String contentFileToString() throws Exception{
		
		return fileToString(new File(workingFolderPath + CONTENT_FILE_NAME));
	}

	/**
	 * 
	 *
	 */
	private String metaFileToString() throws Exception{
		
		return fileToString(new File(workingFolderPath + META_FILE_NAME));
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private void stringToFile(String str, File file) throws Exception{
		
		if (file.exists())
			file.delete();
		
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(file);
			out.write(str.getBytes());
			out.flush();
		}
		finally{
			if (out!=null) out.close();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private void stringToContentFile(String str) throws Exception{
		
		stringToFile(str, new File(workingFolderPath + CONTENT_FILE_NAME));
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private void stringToMetaFile(String str) throws Exception{
		
		stringToFile(str, new File(workingFolderPath + META_FILE_NAME));
	}

	/*
	 * 
	 */
	private void writeContentIntoFile() throws Exception{
		
		String str = contentFileToString();
		str = writeContentInto(str);
		stringToContentFile(str);
	}

	/*
	 * 
	 */
	private void writeMetaIntoFile() throws Exception{
		
		String str = metaFileToString();
		str = writeMetaInto(str);
		stringToMetaFile(str);
	}

	/**
	 * 
	 *
	 */
	private void zip(String fileToZip) throws Exception{
		
		String[] command = new String[4];
		command[0] = "zip";
		command[1] = "-j"; // this one junks the folder name of content.xml
		command[2] = workingFolderPath + ODS_FILE_NAME;
		command[3] = fileToZip;
		
		Process process = Runtime.getRuntime().exec(command);
		
		// check process exit value after every 0.5sec, maximum for 10 seconds in total  
		int exitValue = -1;
		int counter = 0;
		boolean done = false;
		while (done==false && counter<=20){
			counter++;
			try{
				exitValue = process.exitValue();
				done = true;
			}
			catch (IllegalThreadStateException itse){
				Thread.sleep(500);
			}
		}
		
		if (done==false)
			throw new Exception("Process timed out");
	}
	
	/**
	 * 
	 *
	 */
	private void zipContent() throws Exception{
		
		zip(workingFolderPath + CONTENT_FILE_NAME);
	}

	/**
	 * 
	 *
	 */
	private void zipMeta() throws Exception{
		
		zip(workingFolderPath + META_FILE_NAME);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void processContent() throws Exception{
		writeContentIntoFile();
		zipContent();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void processMeta() throws Exception{
		writeMetaIntoFile();
		zipMeta();
	}

	/**
	 * 
	 * @return
	 */
	public String getFinalFileName() {
		return finalFileName;
	}

	/**
	 * 
	 * @return
	 */
	public String getWorkingFolderPath() {
		return workingFolderPath;
	}
	
	/**
	 * 
	 * @param schemaURLBase
	 */
	public void setSchemaURLBase(String schemaURLBase) {
		this.schemaURLBase = schemaURLBase;
	}

	/**
	 * 
	 * @return
	 */
	public static String getDate(long timestamp){
		
		Date date = new Date(timestamp);
        String year = String.valueOf(1900 + date.getYear());
        String month = String.valueOf(date.getMonth() + 1);
        month = (month.length() < 2) ? ("0" + month) : month;
        String day = String.valueOf(date.getDate());
        day = (day.length() < 2) ? ("0" + day) : day;
        String hours = String.valueOf(date.getHours());
        hours = (hours.length() < 2) ? ("0" + hours) : hours;
        String minutes = String.valueOf(date.getMinutes());
        minutes = (minutes.length() < 2) ? ("0" + minutes) : minutes;
        String seconds = String.valueOf(date.getSeconds());
        seconds = (seconds.length() < 2) ? ("0" + seconds) : seconds;
        
        String time = year;
        time = time + "-" + month;
        time = time + "-" + day;
        time = time + "T" + hours;
        time = time + ":" + minutes;
        time = time + ":" + seconds;
        
        return time;
	}
}
