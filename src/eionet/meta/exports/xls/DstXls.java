package eionet.meta.exports.xls;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

import eionet.meta.*;
import eionet.meta.exports.pdf.PdfUtil;

import org.apache.poi.hssf.usermodel.*;

public class DstXls implements XlsIF{
	
	private DDSearchEngine searchEngine = null;
	private OutputStream os = null;
	
	private HSSFWorkbook wb = null;
	private HSSFSheet sheet = null;
	private HSSFRow row = null;
	
	private Hashtable styles    = new Hashtable();
	private Hashtable cellTypes = new Hashtable();
	
	private String dstXlsName = "dataset";
	
	public DstXls(DDSearchEngine searchEngine, OutputStream os){
		this.searchEngine = searchEngine;
		this.os = os;
		wb = new HSSFWorkbook();
	}

	public void create(String dstID) throws Exception{
		addTables(dstID);
	}
		
	public void write() throws Exception{
		wb.write(os);
	}
	
	private void addTables(String dstID) throws Exception{
		
		Dataset dst = searchEngine.getDataset(dstID);
		if (dst==null) throw new Exception("Dataset " + dstID + " not found!");
		this.dstXlsName = dst.getShortName();
		Vector tables = searchEngine.getDatasetTables(dstID);
		for (int i=0; tables!=null && i<tables.size(); i++){
			addTable((DsTable)tables.get(i));
		}
	}
	
	private void addTable(DsTable tbl) throws Exception{
		
		tbl.setGIS(searchEngine.hasGIS(tbl.getID()));
		sheet = wb.createSheet(tbl.getShortName());
		row = sheet.createRow(0);
		addElements(tbl);
	}
	
	private void addElements(DsTable tbl) throws Exception{
		
		Vector elems = searchEngine.getDataElements(null, null, null, null, tbl.getID());
		if (elems==null || elems.size()==0) return;
		
		int done = 0;
		for (int i=0; i<elems.size(); i++){
			if (tbl.hasGIS()){
				DataElement elm = (DataElement)elems.get(i);
				if (elm.getGIS()!=null){
					addElement((DataElement)elems.get(i), (short)done);
					done++;
				}
			}
			else{
				addElement((DataElement)elems.get(i), (short)done);
				done++;
			}
		}
		
		if (done<elems.size()){
			sheet = wb.createSheet(tbl.getShortName() + "-meta");
			row = sheet.createRow(0);
			done = 0;
			for (int i=0; i<elems.size(); i++){
				DataElement elm = (DataElement)elems.get(i);
				if (elm.getGIS()==null){
					addElement((DataElement)elems.get(i), (short)done);
					done++;
				}
			}
		}
	}

	private void addElement(DataElement elm, short index) throws Exception{
		
		HSSFCell cell = row.createCell(index);
		String title = elm.getShortName();
		title = PdfUtil.processUnicode(title);
		setColWidth(title, index);
		cell.setCellValue(title);
		cell.setCellStyle(getStyle(ElmStyle.class));
		
		/*String elmDataType = "";
		Integer cellType = (Integer)cellTypes.get(elmDataType);
		cellType = cellType==null ? new Integer(HSSFCell.CELL_TYPE_STRING) : cellType;
		cell.setCellType(cellType.intValue());*/
	}
	
	private void setColWidth(String title, short index){
		short width = (short)(title.length() * ElmStyle.FONT_HEIGHT * 50);
		sheet.setColumnWidth(index, width);
	}
	
	private HSSFCellStyle getStyle(Class styleClass) throws Exception {
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
	
	private void setCellTypes(){
		
		cellTypes.put("string",  new Integer(HSSFCell.CELL_TYPE_STRING));
		cellTypes.put("boolean", new Integer(HSSFCell.CELL_TYPE_BOOLEAN));
		cellTypes.put("float",   new Integer(HSSFCell.CELL_TYPE_NUMERIC));
		cellTypes.put("integer", new Integer(HSSFCell.CELL_TYPE_NUMERIC));
		cellTypes.put("date",    new Integer(HSSFCell.CELL_TYPE_STRING));
	}
	
	public String getName(){
		return this.dstXlsName;
	}
	
	public static void main(String[] args) {
		
		Connection conn = null;
		
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			conn = DriverManager.getConnection(
							"jdbc:mysql://195.250.186.33:3306/dd", "dduser", "xxx");
							
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", null);
			FileOutputStream fos = new FileOutputStream("d:\\tmp\\workbook.xls");
			DstXls xls = new DstXls(searchEngine, fos);
			
			xls.create("1327");
			xls.write();
			
			fos.close();
			conn.close();
		}
		catch (Exception e){
			if (conn!=null) try{ conn.close(); } catch (SQLException sqle) {}
			e.printStackTrace(System.out);
		}
	}
}
