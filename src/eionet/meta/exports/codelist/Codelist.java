package eionet.meta.exports.codelist;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

public class Codelist {
	
	private static final String ELM = "ELM";
	private static final String TBL = "TBL";
	private static final String DST = "DST";
	
	private DDSearchEngine searchEngine = null;
	private PrintWriter writer = null;
	private String delim = ",";
	
	private Vector lines = new Vector();
	private String lineTerminator = "\n";
	
	public Codelist(Connection conn, PrintWriter writer, String delim){
		
		this.writer = writer;
		if (conn!=null)
			searchEngine = new DDSearchEngine(conn);
		if (delim!=null && delim.trim().length()>0)
			this.delim = delim; 
	}
	
	public void write(String objID, String objType) throws Exception{
		
		if (searchEngine==null) throw new Exception("Search engine not initialized!");
		
		Vector elms = new Vector();
		if (objType.equalsIgnoreCase(ELM)){
			DataElement elm = searchEngine.getDataElement(objID);
			if (elm!=null) elms.add(elm);
		}
		else if (objType.equalsIgnoreCase(TBL))
			elms = searchEngine.getDataElements(null, null, null, null, objID);
		else if (objType.equalsIgnoreCase(DST))
			elms = searchEngine.getDataElements(null, null, null, null, null, objID);
		else
			throw new Exception("Unknown object type: " + objType);
		
		if (elms==null || elms.size()==0)
			lines.add("No data elements with codelists found!");
		else
			write(elms);
	}
	
	public void write(Vector elms) throws Exception{
		
		for (int i=0; elms!=null && i<elms.size(); i++){
			
			DataElement elm = (DataElement)elms.get(i);
			if (!elm.getType().equals("CH1")) continue;
			Vector fxvs = searchEngine.getFixedValues(elm.getID());
			if (fxvs==null || fxvs.size()==0) continue;
			
			String dstName = elm.getDstShortName();
			if (Util.voidStr(dstName)) throw new Exception("failed to get dataset name");
			String tblName = elm.getTblShortName();
			if (Util.voidStr(tblName)) throw new Exception("failed to get table name");
			
			StringBuffer line = new StringBuffer(dstName).append("/").
			append(tblName).append("/").append(elm.getShortName()).append(":");
			
			for (int j=0; j<fxvs.size(); j++){
				FixedValue fxv = (FixedValue)fxvs.get(j);
				if (j>0) line.append(delim);
				line.append(fxv.getValue());
			}
			
			lines.add(line.toString());
		}
		
		if (lines==null || lines.size()==0)
			lines.add("No codelists found!");
	}
	
	public void flush() throws IOException{
		for (int i=0; lines!=null && i<lines.size(); i++){
			writer.write((String)lines.get(i));
			writer.write(lineTerminator);			
		}
	}
	
	public static void main(String[] args){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn =
				DriverManager.getConnection(
			"jdbc:mysql://195.250.186.33:3306/dd", "dduser", "xxx");
			
			Codelist codelist = new Codelist(conn, null, ",");
			codelist.write("2440", TBL);
			codelist.flush();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
		}
	}
}
