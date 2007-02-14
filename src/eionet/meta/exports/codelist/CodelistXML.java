/*
 * Created on 14.02.2007
 */
package eionet.meta.exports.codelist;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;
import java.util.Hashtable;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.FixedValue;
import eionet.meta.exports.mdb.MdbException;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * 
 * @author jaanus
 */
public class CodelistXML extends Codelist {
	
	/** */
	public static final String DD_NAMESPACE = "dd";
	public static final String TAG_VALUE_LISTS = "value-lists";
	public static final String TAG_VALUE_LIST = "value-list";
	public static final String TAG_VALUE = "value";
	public static final String ATTR_ELEMENT = "element";
	public static final String ATTR_TABLE = "table";
	public static final String ATTR_DATASET = "dataset";
	public static final String ATTR_FIXED = "fixed";
	
	/** */
	private static final String KEY_NS_ID = "ns-id";
	private static final String KEY_NS_URL = "ns-url";
	
	/** */
	private Vector namespaces = null;

	/**
	 * 
	 * @param conn
	 * @param writer
	 * @param delim
	 */
	public CodelistXML(Connection conn, PrintWriter writer){
		
		this.writer = writer;
		if (conn!=null)
			searchEngine = new DDSearchEngine(conn);		
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
			elms = searchEngine.getDataElements(null, null, null, null, objID, null);
		else if (objType.equalsIgnoreCase(DST))
			elms = searchEngine.getDataElements(null, null, null, null, null, objID);
		else
			throw new Exception("Unknown object type: " + objType);
		
		initNamespaces();
		writeHeader();
		write(elms);
		writeFooter();
	}
	
	/**
	 * 
	 *
	 */
	private void writeHeader(){		
		lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		StringBuffer line = new StringBuffer("<");
		line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LISTS);
		
		for (int i=0; namespaces!=null && i<namespaces.size(); i++){
			Hashtable ns = (Hashtable)namespaces.get(i);
			String nsID = (String)ns.get(KEY_NS_ID);
			String nsURL = (String)ns.get(KEY_NS_URL);
			line.append(" xmlns:").append(nsID).append("=\"").append(nsURL).append("\"");			
		}		
		line.append(">");
		lines.add(line);
	}

	/**
	 * 
	 *
	 */
	private void writeFooter(){		
		StringBuffer line = new StringBuffer("</");
		line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LISTS).append(">");
		lines.add(line);
	}

	/**
	 * 
	 * @param elms
	 * @throws Exception
	 */
	private void write(Vector elms) throws Exception {
		
		for (int i=0; elms!=null && i<elms.size(); i++){
			
			DataElement elm = (DataElement)elms.get(i);
			Vector fxvs = searchEngine.getFixedValues(elm.getID());
			if (fxvs==null || fxvs.size()==0)
				continue;
			
			// start value-list tag
			StringBuffer line = new StringBuffer("\t<");
			line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LIST).append(" ").
			append(ATTR_ELEMENT).append("=\"").append(elm.getIdentifier()).append("\"");
			if (elm.isCommon()==false){
				line.append(" ").append(ATTR_TABLE).append("=\"").append(elm.getTblIdentifier()).append("\" ").
				append(ATTR_DATASET).append("=\"").append(elm.getDstIdentifier()).append("\"");
			}
			if (elm.getType()!=null && elm.getType().equals("CH1"))
				line.append(" ").append(ATTR_FIXED).append("=\"").append(String.valueOf(true)).append("\"");
			line.append(">");
			lines.add(line);
			
			// value tags
			for (int j=0; j<fxvs.size(); j++){
				FixedValue fxv = (FixedValue)fxvs.get(j);
				line = new StringBuffer("\t\t<");
				line.append(DD_NAMESPACE).append(":").append(TAG_VALUE).append(">");
				line.append(fxv.getValue());
				line.append("</").append(DD_NAMESPACE).append(":").append(TAG_VALUE).append(">");
				lines.add(line);
			}
			
			// end value-list tag
			line = new StringBuffer("\t</");
			line.append(DD_NAMESPACE).append(":").append(TAG_VALUE_LIST).append(">");
			lines.add(line);
		}
	}

	/**
	 * 
	 *
	 */
	private void initNamespaces() throws Exception{
		namespaces = new Vector();
		Hashtable ns = new Hashtable();
		ns.put(KEY_NS_ID, "xsi");
		ns.put(KEY_NS_URL, "http://www.w3.org/2001/XMLSchema-instance");
		namespaces.add(ns);
		
		String jspURLPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
		if (jspURLPrefix==null || jspURLPrefix.length()==0)
			throw new Exception("Missing " + PropsIF.JSP_URL_PREFIX + " property!");
		if (jspURLPrefix.endsWith("/"))
			jspURLPrefix = jspURLPrefix.substring(0, jspURLPrefix.length()-1);
		
		ns = new Hashtable();
		ns.put(KEY_NS_ID, "dd");
		ns.put(KEY_NS_URL, jspURLPrefix);
		namespaces.add(ns);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn =
				DriverManager.getConnection(
			"jdbc:mysql://192.168.10.15:3306/jaanusdd", "dduser", "xxx");
			
			PrintWriter pw = new PrintWriter(System.out);
			CodelistXML codelist = new CodelistXML(conn, pw);
			codelist.write("16820", ELM);
			codelist.flush();
			pw.flush();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
		}
	}
}
