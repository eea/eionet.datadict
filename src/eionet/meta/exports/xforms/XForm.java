package eionet.meta.exports.xforms;

import eionet.meta.*;
import java.io.*;
import java.net.*;
import java.util.*;
import eionet.util.Util;

public abstract class XForm implements XFormIF {
	
	protected DDSearchEngine searchEngine = null;
	protected PrintWriter writer = null;
	protected String appContext = "";
	
	private String instance = null;
	private String controlsLabel = "";
	protected Vector binds    = new Vector();
	private Vector controls = new Vector();
	
	private String instanceDOM = null;
	
	/*
	 * 
	 */
	public XForm(DDSearchEngine searchEngine, PrintWriter writer){
		this.searchEngine = searchEngine;
		this.writer = writer;
	}

	public void setAppContext(String appContext){
		if (appContext != null){
			if (!appContext.endsWith("/"))
				appContext = appContext + "/";
			this.appContext = appContext;
		}
	}

	protected void setInstance(String id){
		this.instance = appContext + "GetXmlInstance?id=" + id;
	}

	protected void setControlsLabel(String label){
		this.controlsLabel = label;
	}

	protected void addBind(Hashtable bind){
		binds.add(bind);
	}

	protected void addControl(Hashtable control){
		controls.add(control);
	}
	
	/**
	* Flush the written content into the writer.
	*/
	public void flush(String template) throws Exception{
		
		if (template==null)
			throw new Exception("Template path cannot be null!");
		
		URL url = new URL(template);
		InputStream in = url.openStream();

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		while ((line=reader.readLine())!=null)
			writeLine(line);
			
		in.close();
		reader.close();
	}
	
	private void writeLine(String line) throws Exception{
		
		if (line==null) return;
		
		if (line.trim().startsWith("<f:model")){
			writer.println(line);
			String lead = extractLead(line) + "\t";
			writeInstance(lead);
			writeBinds(lead);
		}
		else if (line.trim().startsWith("<f:group id=\"controls\"")){
			writer.println(line);
			writeControlsLabel(extractLead(line) + "\t");
		}
		else if (line.trim().startsWith("<f:repeat")){
			writeRepeat(line);
			writeControls(extractLead(line) + "\t");
		}
		else if (line.trim().startsWith("<f:insert")){
			writeInsert(line);
		}
		else if (line.trim().startsWith("<f:delete")){
			writeDelete(line);
		}
		else		
			writer.println(line);
	}
	
	protected String extractLead(String line){
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++){
			char c = line.charAt(i);
			if (Character.isWhitespace(c))
				buf.append(line.charAt(i));
			else
				break;
		}
		return buf.toString();	
	}
	
	private void writeInstance(String lead) throws Exception{
		writer.println(lead + "<!-- Original instance in Data Dictinary (not used anymore - use default instance value with context parameter $instance) -->");
		writer.println(lead + "<!--  <f:instance id=\"original\" src=\"" + instance + "\"/>  -->");
	}

	private void writeControlsLabel(String lead) throws Exception{
		writer.println(lead + "<f:label>" + controlsLabel + "</f:label>");
	}

	protected void writeRepeat(String line) throws Exception{
	}

	protected void writeInsert(String line) throws Exception{
	}

	protected void writeDelete(String line) throws Exception{
	}
	
	protected void writeBinds(String lead) throws Exception{
		writeRegularBinds(lead);
	}

	protected void writeRegularBinds(String lead) throws Exception{
		
		for (int i=0; i<binds.size(); i++){
			Hashtable bind = (Hashtable)binds.get(i);
			String id = (String)bind.get(ATTR_ID);
			String type = (String)bind.get(ATTR_TYPE);
			String nodeset = (String)bind.get(ATTR_NODESET);
			String constraint = getConstraint(bind);
			
			StringBuffer buf = new StringBuffer("<f:bind");
			
			if (id!=null) buf.append(" id=\"").append(id).append("\"");
			if (type!=null) buf.append(" type=\"").append(type).append("\"");
			if (nodeset!=null) buf.append(" nodeset=\"").append(nodeset).append("\"");
			if (constraint!=null) buf.append(" constraint=\"").append(constraint).append("\"");
			
			buf.append("/>");
			
			writer.println(lead + buf.toString());
		}
	}

	private void writeControls(String lead) throws Exception{
		
		for (int i=0; i<controls.size(); i++){
			Hashtable control = (Hashtable)controls.get(i);
			String bind  = (String)control.get(ATTR_BIND);
			String label = (String)control.get(CTRL_LABEL);
			String hint  = (String)control.get(CTRL_HINT);
			String type  = (String)control.get(CTRL_TYPE);
			String alert = (String)control.get(CTRL_ALERT);
	
			// start control
			StringBuffer buf = new StringBuffer("<f:").append(type);
			if (bind!=null)
				buf.append(" bind=\"").append(bind).append("\"");
			buf.append(">");
			writer.println(lead + buf.toString());
			
			// write label
			if (label!=null){
				buf = new StringBuffer("<f:label>").append(label).append("</f:label>");
				writer.println(lead + "\t" + buf.toString());
			}

			// write hint
			if (hint!=null){
				buf = new StringBuffer("<f:hint>").append(hint).append("</f:hint>");
				writer.println(lead + "\t" + buf.toString());
			}

			// write alert
			if (alert!=null){
				buf = new StringBuffer("<f:alert>").append(alert).append("</f:alert>");
				writer.println(lead + "\t" + buf.toString());
			}
			
			// write items if select
			writeSelectItems((Vector)control.get(CTRL_FXVS), lead + "\t");
			
			// end control
			writer.println(lead + "</f:" + type + ">");
		}
	}

	private void writeSelectItems(Vector fxvs, String lead) throws Exception{
		
		for (int i=0; fxvs!=null && i<fxvs.size(); i++){
			writeSelectItem((FixedValue)fxvs.get(i), lead);
		}
	}

	private void writeSelectItem(FixedValue fxv, String lead) throws Exception{
		
		String value = fxv.getValue();
		if (value==null || value.length()==0)
			return;
		else
			value = Util.escapeXML(value);
			
		String label = value;
		
		writer.println(lead + "<f:item>");
		writer.println(lead + "\t<f:label>" + label + "</f:label>");
		writer.println(lead + "\t<f:value>" + value + "</f:value>");
		writer.println(lead + "</f:item>");
	}
	
	protected String getRef(String tagName) throws Exception{
		if (instance==null)
			throw new Exception("getRef(): instance document is not set!");
		return "";
	}
	
	protected String setAttr(String line, String name, String value){
		
		StringBuffer buf = new StringBuffer(line);
		
		String attrStart = name + "=\"";
		int i = line.indexOf(attrStart);
		if (i!=-1){
			int j = line.indexOf("\"", i + attrStart.length());
			if (j!=-1)
				buf.replace(i + attrStart.length(), j, value);
		}
		
		return buf.toString();
	}

	protected String escapeCDATA(String s){
        
		if (s == null) return null;
        
		StringBuffer buf = new StringBuffer("<![CDATA[");
		for (int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if (Character.isISOControl(c)){
				if (Character.isWhitespace(c))
					buf.append(c);
			}
			else
				buf.append(c);
		}
 
		buf.append("]]>");
		return buf.toString();
	}
	
	protected String getConstraint(Hashtable bind){
		
		StringBuffer buf = new StringBuffer();
		
		String minSize = (String)bind.get(ATTR_MINSIZE);
		if (minSize!=null)
			buf.append("string-length(.)&gt;=").append(minSize);
		
		String maxSize = (String)bind.get(ATTR_MAXSIZE);
		if (maxSize!=null){
			if (buf.length()!=0) buf.append(" and ");
			buf.append("string-length(.)&lt;=").append(maxSize);
		}
		
		String minInclValue = (String)bind.get(ATTR_MIN_INCL_VALUE);
		String minExclValue = (String)bind.get(ATTR_MIN_EXCL_VALUE);
		if (minInclValue!=null){
			if (buf.length()!=0) buf.append(" and ");
			buf.append("number(.)&gt;=").append(minInclValue);
		}
		else if (minExclValue!=null){
			if (buf.length()!=0) buf.append(" and ");
			buf.append("number(.)&gt;").append(minExclValue);
		}

		String maxInclValue = (String)bind.get(ATTR_MAX_INCL_VALUE);
		String maxExclValue = (String)bind.get(ATTR_MAX_EXCL_VALUE);
		if (maxInclValue!=null){
			if (buf.length()!=0) buf.append(" and ");
			buf.append("number(.)&lt;=").append(maxInclValue);
		}
		else if (maxExclValue!=null){
			if (buf.length()!=0) buf.append(" and ");
			buf.append("number(.)&lt;").append(maxExclValue);
		}
		
		if (buf.length()!=0)
			return buf.toString();
		else
			return null;
	}

	/*
	 * 
	 */	
	public abstract void write(String objID) throws Exception;
}
