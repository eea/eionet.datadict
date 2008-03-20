package eionet.meta.exports.codelist;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

public class CodelistCSV extends Codelist{
	
	/** */
	private String delim = ",";
	
	/**
	 * 
	 * @param conn
	 * @param writer
	 * @param delim
	 */
	public CodelistCSV(Connection conn, PrintWriter writer, String delim){
		
		this.writer = writer;
		if (conn!=null)
			searchEngine = new DDSearchEngine(conn);
		if (delim!=null && delim.trim().length()>0)
			this.delim = delim; 
	}

	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.exports.codelist.Codelist#write(java.lang.String, java.lang.String)
	 */
	public void write(String objID, String objType) throws Exception{
		
		Vector elms = new Vector();
		if (objType.equalsIgnoreCase(ELM)){
			DataElement elm = searchEngine.getDataElement(objID);
			if (elm!=null)
				elms.add(elm);
		}
		else if (objType.equalsIgnoreCase(TBL))
			elms = searchEngine.getDataElements(null, "CH1", null, null, objID, null);
		else if (objType.equalsIgnoreCase(DST))
			elms = searchEngine.getDataElements(null, "CH1", null, null, null, objID);
		else
			throw new Exception("Unknown object type: " + objType);
		
		write(elms);
	}

	/**
	 * 
	 * @param elms
	 * @throws Exception
	 */
	private void write(Vector elms) throws Exception{
		
		for (int i=0; elms!=null && i<elms.size(); i++){
			
			DataElement elm = (DataElement)elms.get(i);
			if (!elm.getType().equals("CH1")) continue;
			Vector fxvs = searchEngine.getFixedValues(elm.getID());
			if (fxvs==null || fxvs.size()==0) continue;
			
			StringBuffer line = new StringBuffer();
			if (elm.isCommon()==false){			
				String dstName = elm.getDstShortName();
				if (Util.voidStr(dstName))
					throw new Exception("failed to get dataset name");
				String tblName = elm.getTblShortName();
				if (Util.voidStr(tblName))
					throw new Exception("failed to get table name");
			
				line.append(dstName).append("/").
				append(tblName).append("/").
				append(elm.getShortName()).
				append(":");
			}
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
}
