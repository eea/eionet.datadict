package eionet.meta.exports.xforms;

import java.sql.*;
import java.util.*;
import java.io.*;
import eionet.meta.*;
import eionet.util.Util;

public class TblXForm extends XForm {
	
	private static final String REPEAT_ID = "i1";
	
	private Vector elements = null;
	private Hashtable tblBind = new Hashtable();
	private String dstNs = "";
	private String tblNs = "";
	
	public TblXForm(DDSearchEngine searchEngine, PrintWriter writer){
		super(searchEngine, writer);
	}
	
	public void write(String tblID) throws Exception{

		if (Util.voidStr(tblID))
			throw new Exception("Table ID not specified!");
        
		// Get the table object.
		DsTable tbl = searchEngine.getDatasetTable(tblID);
		if (tbl == null) throw new Exception("Table not found!");
		
		// get simple attributes
		Vector v = searchEngine.getSimpleAttributes(tblID, "T", null, tbl.getDatasetID());
		tbl.setSimpleAttributes(v);
        
		// get data elements (this will set all the simple attributes of elements)
		elements = searchEngine.getDataElements(null, null, null, null, tblID);
		
		// set namespaces
		Namespace ns = null;
		String nsID = tbl.getParentNs();
		if (!Util.voidStr(nsID)){
			ns = searchEngine.getNamespace(nsID);
			if (ns!=null) dstNs = ns.getPrefix() + ":";
		}
		nsID = tbl.getNamespace();
		if (!Util.voidStr(nsID)){
			ns = searchEngine.getNamespace(nsID);
			if (ns!=null) tblNs = ns.getPrefix() + ":";
		}
        
        //
		write(tbl);
	}

	/**
	* Write a schema for a given object.
	*/
	private void write(DsTable tbl) throws Exception{
		
		// set instance
		setInstance(tbl.getID());
		
		// set controls label
		setControlsLabel(		
		tbl.getDatasetName() + " dataset, " + tbl.getIdentifier() + " table");
		// tbl.getDatasetName() + " dataset, " + tbl.getShortName() + " table");
		
		// set binds
		setBinds(tbl);
		
		// set controls
		setControls(tbl);
	}
	
	private void setBinds(DsTable tbl){
		
		// set the table bind
		String bindID = tbl.getIdentifier();
		//String nodeset = "/" + dstNs + tbl.getShortName() + "/" + dstNs + "Row";
		String nodeset = "/" + dstNs + tbl.getIdentifier() + "/" + dstNs + "Row";
		tblBind.put(ATTR_ID, bindID);
		tblBind.put(ATTR_NODESET, nodeset);

		// set element binds
		for (int i=0; elements!=null && i<elements.size(); i++){
	
			DataElement elm = (DataElement)elements.get(i);
			bindID = elm.getIdentifier();
			String bindType = elm.getAttributeValueByShortName("Datatype");
			if (bindType==null)
				bindType = DEFAULT_DATATYPE;
			//nodeset = tblNs + elm.getShortName();
			nodeset = tblNs + elm.getIdentifier();
	
			Hashtable elmBind = new Hashtable();
			elmBind.put(ATTR_ID, bindID);
			elmBind.put(ATTR_TYPE, bindType);
			elmBind.put(ATTR_NODESET, nodeset);
			
			addBind(elmBind);
		}
	}

	private void setControls(DsTable tbl) throws Exception{
		
		for (int i=0; elements!=null && i<elements.size(); i++){
	
			DataElement elm = (DataElement)elements.get(i);
			String ctrlID = "ctrl_" + elm.getID();
			String ctrlLabel = elm.getAttributeValueByShortName("Name");
			if (ctrlLabel==null)
				ctrlLabel = elm.getShortName(); // Short name is OK to use for label!
			//String ref = tblNs + elm.getShortName();			
			String ref = tblNs + elm.getIdentifier();
			String ctrlType = DEFAULT_CTRLTYPE;
			
			Vector fxvs = null;
			String elmType = elm.getType();
			if (elmType!=null && elmType.equals("CH1")){
				fxvs = searchEngine.getFixedValues(elm.getID());
				if (fxvs!=null && fxvs.size()>0)
					ctrlType = "select1";
			}
			
	
			Hashtable control = new Hashtable();
			control.put(ATTR_ID, ctrlID);
			control.put(ATTR_REF, ref);
			control.put(CTRL_LABEL, ctrlLabel);
			control.put(CTRL_TYPE,  ctrlType);
			if (fxvs!=null) control.put(CTRL_FXVS,  fxvs);			
			addControl(control);
		}
	}

	protected void writeBinds(String lead) throws Exception{
		
		// element binds will be written into the table bind
		
		// start table bind
		String id = (String)tblBind.get(ATTR_ID);
		String nodeset = (String)tblBind.get(ATTR_NODESET);
		StringBuffer buf = new StringBuffer("<f:bind");
		if (id!=null)
			buf.append(" id=\"").append(id).append("\"");
		if (nodeset!=null)
			buf.append(" nodeset=\"").append(nodeset).append("\"");
		buf.append(">");
		
		writer.println(lead + buf.toString());
			
		// write element binds
		writeRegularBinds(lead + "\t");
		
		// end table bind
		writer.println(lead + "</f:bind>");
	}

	protected void writeRepeat(String line) throws Exception{
		line = setAttr(line, "id", REPEAT_ID);
		String tblBindID = (String)tblBind.get(ATTR_ID);
		if (tblBindID!=null) 
			line = setAttr(line, "bind", tblBindID);
		
		writer.println(line);
	}

	protected void writeTrigger(String line) throws Exception{
		line = setAttr(line, "at", "index('" + REPEAT_ID + "')");
		String tblBindNodeset = (String)tblBind.get(ATTR_NODESET);
		if (tblBindNodeset!=null) 
			line = setAttr(line, "nodeset", tblBindNodeset);

		writer.println(line);
	}

	public static void main(String args[]){
		
		Connection conn = null;
        
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://195.250.186.33:3306/DataDict", "dduser", "xxx");
			DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
			FileOutputStream os = new FileOutputStream("d:\\projects\\datadict\\tmp\\xformike.xml");
			PrintWriter writer = new PrintWriter(os);
			TblXForm tblFXorm = new TblXForm(searchEngine, writer);
			tblFXorm.setAppContext("http://127.0.0.1:8080/datadict/public");
			tblFXorm.write("1312");
			tblFXorm.flush("d:\\projects\\datadict\\tmp\\xform.xhtml");
            
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
