package eionet.meta.exports.xmlinst;

import eionet.meta.*;
import java.io.*;
import java.util.*;

public abstract class XmlInst implements XmlInstIF {
	
	protected DDSearchEngine searchEngine = null;
	private PrintWriter writer = null;
	
	protected String appContext = "";
	protected String lineTerminator = "\n";
	private String docElement = null;
	private Hashtable leads = null;
	private String curLead = "";

	private Vector content = new Vector();
	private Vector namespaces = new Vector();
	
	private String schemaLocation = "";
	
	/*
	 * 
	 */
	public XmlInst(DDSearchEngine searchEngine, PrintWriter writer){
		this.searchEngine = searchEngine;
		this.writer = writer;
		addFixedNamespaces();
	}

	public void setAppContext(String appContext){
		if (appContext != null){
			if (!appContext.endsWith("/"))
				appContext = appContext + "/";
			this.appContext = appContext;
		}
	}

	protected void addString(String s){
		content.add(s);
	}
    
	protected void newLine(){
		content.add(lineTerminator);
	}

	protected String getNamespacePrefix(Namespace ns){
		return ns.getPrefix();
	}
    
	protected void addNamespace(Namespace ns){
        addNamespace(getNamespacePrefix(ns), appContext + "namespace.jsp?ns_id=" + ns.getID());
	}

	protected void addNamespace(String prefix, String url){
		
		StringBuffer buf =
		new StringBuffer("xmlns:").append(prefix).append("=\"").append(url).append("\"");
		
		if (!namespaces.contains(buf.toString())) namespaces.add(buf.toString());
	}
	
	protected void setDocElement(String docElement){
		this.docElement = docElement;
	}

	private void startDocElement(){
        
		writer.print("<" + docElement);        
		writeNamespaces();
		writeSchemaLocation();
		writer.print(">");
		writer.print(lineTerminator);
	}
    
	private void endDocElement(){
		writer.print("</" + docElement + ">");
	}
	
	private void writeNamespaces(){
		
		Iterator iter = namespaces.iterator();
		while (iter.hasNext()){
			writer.print(" ");
			writer.print(iter.next());			
		}
	}

	private void writeSchemaLocation(){
		writer.print(" xsi:noNamespaceSchemaLocation=\"" + schemaLocation + "\"");
	}
	
	private void writeHeader(){
		writer.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		writer.print(lineTerminator);
	}

	/**
	* Flush the written content into the writer.
	*/
	public void flush() throws Exception{
		
		writeHeader();
		
		if (this.docElement==null) throw new Exception("Missing document element!");
		startDocElement();
        
		// write content
		for (int i=0; i<content.size(); i++){
			writer.print((String)content.get(i));
		}
        
		endDocElement();
	}

	protected String escape(String s){
        
		if (s == null) return null;
        
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if (c == '<')
				buf.append("&lt;");
			else if (c == '>')
				buf.append("&gt;");
			else if (c == '&')
				buf.append("&amp;");
			else
				buf.append(c);
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

	protected String getLead(String leadName){
		
		if (leads==null || leads.size()==0){
			leads = new Hashtable();
			leads.put("row", "\t");
			leads.put("elm", "\t\t");
		}
		
		String lead = (String)leads.get(leadName);
		if (lead==null)
			lead = curLead;
		else
			curLead = lead;
		
		return lead;
	}

	private void addFixedNamespaces(){
		
		addNamespace("xsi", XSI_NS);
	}

	protected void setSchemaLocation(String schemaLocation){
		this.schemaLocation = schemaLocation;
	}
	
	/*
	 * 
	 */	
	public abstract void write(String objID) throws Exception;
}
