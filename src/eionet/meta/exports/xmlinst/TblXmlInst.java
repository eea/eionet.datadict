package eionet.meta.exports.xmlinst;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

import com.tee.xmlserver.AppUserIF;

public class TblXmlInst extends XmlInst {
	
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
		tbl.setElements(searchEngine.getDataElements(null, null, null, null, tblID));
        
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
				setSchemaLocation(getSchemaLocation(nsID, tbl.getID()));
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
		
		setDocElement(dstNsPrefix + ":" + tbl.getIdentifier());
		
		writeRows(tbl.getElements());
	}
	
	protected String getSchemaLocation(String nsID, String id){
		StringBuffer buf = new StringBuffer().
		append(this.appContext).append("GetSchema?id=TBL").append(id);
		
		return buf.toString();
	}

	protected void setLeads(){
		leads = new Hashtable();
		leads.put("row", "\t");
		leads.put("elm", "\t\t");
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
