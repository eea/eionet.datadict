package eionet.meta.exports.xmlinst;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

import com.tee.xmlserver.AppUserIF;

public class TblXmlInst extends XmlInst {
	
	private static final int ROW_COUNT = 1;
	
	private String dstNsPrefix = "";
	private String tblNsPrefix = "";
	private Vector elements = null;
	
	public TblXmlInst(DDSearchEngine searchEngine, PrintWriter writer){
		super(searchEngine, writer);
	}
	
	public void write(String tblID) throws Exception{

		if (Util.voidStr(tblID))
			throw new Exception("Table ID not specified!");
        
		// Get the table object.
		DsTable tbl = searchEngine.getDatasetTable(tblID);
		if (tbl == null) throw new Exception("Table not found!");
        
		// get data elements (this will set all the simple attributes of elements)
		elements = searchEngine.getDataElements(null, null, null, null, tblID);
        
		write(tbl);
	}

	/**
	* Write a schema for a given object.
	*/
	private void write(DsTable tbl) throws Exception{

		// set the dataset namespace
		String nsID = tbl.getParentNs();
		if (!Util.voidStr(nsID)){
			Namespace ns = searchEngine.getNamespace(nsID);
			if (ns != null){
				addNamespace(ns);
				dstNsPrefix = getNamespacePrefix(ns);
				setSchemaLocation(getSchemaLocation(nsID, tbl));
			}
		}
		
		// set the table namespace
		nsID = tbl.getNamespace();
		if (!Util.voidStr(nsID)){
			Namespace ns = searchEngine.getNamespace(nsID);
			if (ns != null){
				addNamespace(ns);
				tblNsPrefix = getNamespacePrefix(ns);
			}
		}
		
		//setDocElement(dstNsPrefix + ":" + tbl.getShortName());
		setDocElement(dstNsPrefix + ":" + tbl.getIdentifier());
		
		writeRows();
	}
	
	private void writeRows(){		
		for (int i=0; i<ROW_COUNT; i++){
			writeRow();
		}
	}
	
	private void writeRow(){
		addString(startRow());
		newLine();
		
		for (int i=0; elements!=null && i<elements.size(); i++){
			DataElement elm = (DataElement)elements.get(i);
			//addString(elm(elm.getShortName()));
			addString(elm(elm.getIdentifier()));
			newLine(); 
		}
		
		addString(endRow());
		newLine();
	}
	
	private String startRow(){
		return getLead("row") + "<" + dstNsPrefix + ":" + "Row>";
	}

	private String endRow(){
		return getLead("row") + "</" + dstNsPrefix + ":" + "Row>";
	}

	private String elm(String name){
		String qfName = tblNsPrefix + ":" + name;
		return getLead("elm") + "<" + qfName + "></" + qfName + ">";
	}

	private String getSchemaLocation(String nsID, DsTable tbl){
		StringBuffer buf = new StringBuffer().
		/*append(this.appContext).
		append("namespace.jsp?ns_id=").
		append(nsID).
		append(" ").*/
		append(this.appContext).
		append("GetSchema?id=TBL").
		append(tbl.getID());
		
		return buf.toString();
	}
		
	public static void main(String args[]){
		
		Connection conn = null;
        
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://195.250.186.33:3306/DataDict", "dduser", "xxx");
			DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
			FileOutputStream os = new FileOutputStream("d:\\projects\\datadict\\tmp\\instance.xml");
			PrintWriter writer = new PrintWriter(os);
			TblXmlInst tblXmlInst = new TblXmlInst(searchEngine, writer);
			tblXmlInst.setAppContext("http://127.0.0.1:8080/datadict/public");
			tblXmlInst.write("1896");
			tblXmlInst.flush();
            
			writer.flush();
			writer.close();
			os.flush();
			os.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			if (conn != null){
				try{ conn.close(); }
				catch (Exception e) {}
			}
		}
	}
}
