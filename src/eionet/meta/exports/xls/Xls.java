package eionet.meta.exports.xls;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

import eionet.meta.*;
import eionet.meta.exports.*;
import eionet.meta.exports.pdf.PdfUtil;
import eionet.util.*;
import com.tee.util.SQLGenerator;

import org.apache.poi.hssf.usermodel.*;

public abstract class Xls implements XlsIF{
	
	protected static final String FILE_EXT = ".xls";
	
	protected DDSearchEngine searchEngine = null;
	protected OutputStream os = null;
	protected Connection conn = null;

	protected HSSFWorkbook wb = null;
	protected HSSFSheet sheet = null;
	protected HSSFRow row = null;

	protected Hashtable styles    = new Hashtable();
	protected Hashtable cellTypes = new Hashtable();

	protected String cachePath = null;
	protected String cacheFileName = null;
	
	protected String fileName = "xls.xls";
	
	protected void setSchemaUrl(String id) throws Exception{
		
		// first make sure we have the schema url
		String schemaUrl = Props.getProperty(PropsIF.XLS_SCHEMA_URL);
		if (Util.voidStr(schemaUrl))
			throw new Exception("Missing " + PropsIF.XLS_SCHEMA_URL + " property!");
		
		// create the sheet
		sheet = wb.createSheet(Props.getProperty(PropsIF.XLS_SCHEMA_URL_SHEET));
		
		// create the warning rows
		row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short)0);
		cell.setCellValue("Please do not delete or modify this sheet!!!");
		cell.setCellStyle(getStyle(WarningStyle.class));
		
		row = sheet.createRow(1);
		cell = row.createCell((short)0);
		cell.setCellValue("It is used for converting this file back to XML!");
		cell.setCellStyle(getStyle(WarningStyle.class));
		
		row = sheet.createRow(2);
		cell = row.createCell((short)0);
		cell.setCellValue("Without this possibility your work cannot be used!");
		cell.setCellStyle(getStyle(WarningStyle.class));
		
		
		// create the row with the schema url
		row = sheet.createRow(3);
		cell = row.createCell((short)0);
		cell.setCellValue(schemaUrl + id);
	}

	protected HSSFCellStyle getStyle(Class styleClass) throws Exception {
		Short index = (Short)styles.get(styleClass.getName());
		if (index==null){
			Class[] argTypes = {wb.getClass()};
			Method method = styleClass.getMethod("create", argTypes);
			Object[] args = {wb}; 
			index = (Short)method.invoke(null, args);
			styles.put(styleClass.getName(), index);
		}
		
		return wb.getCellStyleAt(index.shortValue());
	}
	
	protected void setCellTypes(){
		
		cellTypes.put("string",  new Integer(HSSFCell.CELL_TYPE_STRING));
		cellTypes.put("boolean", new Integer(HSSFCell.CELL_TYPE_BOOLEAN));
		cellTypes.put("float",   new Integer(HSSFCell.CELL_TYPE_NUMERIC));
		cellTypes.put("integer", new Integer(HSSFCell.CELL_TYPE_NUMERIC));
		cellTypes.put("date",    new Integer(HSSFCell.CELL_TYPE_STRING));
	}
}
