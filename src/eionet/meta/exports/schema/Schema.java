
package eionet.meta.exports.schema;

import java.io.*;
import java.util.*;
import eionet.meta.*;
import eionet.util.Util;

public abstract class Schema implements SchemaIF{
    
    protected DDSearchEngine searchEngine = null;
    private PrintWriter writer = null;
    
    private Vector content = new Vector();
    private Vector namespaces = new Vector();
    private Vector imports = new Vector();
    
    private String identitation = "";
    //protected String basensPath = "";
    protected String appContext = "";
    
    protected String lineTerminator = "\n";
    
	protected String targetNsUrl = "";
	protected String referredNsPrefix = "";
	protected String referredNsID = "";
    
    protected Hashtable nonAnnotationAttributes = new Hashtable();
    
    public Schema(DDSearchEngine searchEngine, PrintWriter writer){
        
        this.searchEngine = searchEngine;
        this.writer = writer;
        this.lineTerminator = File.separator.equals("/") ? "\r\n" : "\n";
        
        this.namespaces.add("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
        
        this.nonAnnotationAttributes.put("Datatype", "");
        this.nonAnnotationAttributes.put("MinSize", "");
        this.nonAnnotationAttributes.put("MaxSize", "");
    }
    
    public void setIdentitation(String identitation){
        if (identitation != null)
            this.identitation = identitation;
    }
    
    /*protected void setBasensPath(String basensPath){
        if (basensPath != null)
            this.basensPath = basensPath;
    }*/
    
    public void setAppContext(String appContext){
        if (appContext != null){
            if (!appContext.endsWith("/"))
                appContext = appContext + "/";
            this.appContext = appContext;
        }
    }
    
    protected void addString(String s){
        if (content.size()==0)
            content.add(identitation + s);
        else
            content.add(s);
    }
    
    protected void newLine(){
        content.add(lineTerminator + identitation);
    }

    protected void setTargetNsUrl(String nsID){
        this.targetNsUrl = appContext + "namespace.jsp?ns_id=" + nsID;
    }

	protected void setRefferedNs(Namespace ns){
		this.referredNsID = ns.getID();
		this.referredNsPrefix = getNamespacePrefix(ns);		
	}
	
	protected String getNamespacePrefix(Namespace ns){
		return "dd" + ns.getID();
	}
    
    protected void addNamespace(Namespace ns){
        
        StringBuffer nsDeclaration = new StringBuffer("xmlns:");
        nsDeclaration.append(getNamespacePrefix(ns));
        String url = appContext + "namespace.jsp?ns_id=" + ns.getID();
        nsDeclaration.append("=\"" + url + "\"");
            
        if (!namespaces.contains(nsDeclaration.toString()))
            namespaces.add(nsDeclaration.toString());
    }
    
    protected void addImport(String compID, String compType){
        
        StringBuffer importClause = new StringBuffer("<xs:import namespace=\"");
        //String url = appContext + "namespace.jsp?ns_id=" + ns.getID();
		String url = appContext + "namespace.jsp?ns_id=" + this.referredNsID;
        importClause.append(url);
        importClause.append("\" schemaLocation=\"");
        
        importClause.append(appContext + "GetSchema?id=" + compType + compID);
        
        importClause.append("\"/>");
        
        if (!imports.contains(importClause.toString()))
            imports.add(importClause.toString());
    }
    
    /**
    * Write a schema for an object given by ID.
    */
    public abstract void write(String objID) throws Exception;
    
    /**
    * Flush the written content into the writer.
    */
    public void flush() throws Exception{
        
        // write schema header
        writeHeader();
        // write imports
        writeImports();
        
        // write content
        for (int i=0; i<content.size(); i++){
            writer.print((String)content.get(i));
        }
        
        // write schema footer
        writeFooter();
    }
    
    protected void writeElemStart(String shortName){
        
        addString("<xs:element name=\"");
        addString(shortName);
        addString("\">");
        newLine();
    }
    
    protected void writeElemEnd(){
        addString("</xs:element>");
    }
    
    protected void writeAnnotation(Vector simpleAttrs, Vector complexAttrs) throws Exception{
        
        addString("\t<xs:annotation>");
        newLine();
        addString("\t\t<xs:documentation xml:lang=\"en\">");
        newLine();
        
        // simple attributes first
        for (int i=0; simpleAttrs!=null && i<simpleAttrs.size(); i++){
            
            // get attribute
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            Namespace ns = attr.getNamespace();
            String name = this.getNamespacePrefix(ns) + ":" + attr.getShortName();
            
            // put attributes value or values into a vector
            String dispMultiple = attr.getDisplayMultiple();
            if (Util.voidStr(dispMultiple))
                dispMultiple = "0";
            
            Vector values = null;
            if (dispMultiple.equals("1")){
                values = attr.getValues();
            }
            else{
                String _value = attr.getValue();
                if (!Util.voidStr(_value)){
                    values = new Vector();
                    values.add(_value);
                }
            }
            
            if (values == null || values.size()==0) continue;
            
            // handle  nonAnnotationAttributes
            if (nonAnnotationAttributes.containsKey(attr.getShortName())){
                nonAnnotationAttributes.put(attr.getShortName(), values.get(0));
                continue;
            }
            
            // add namespace
            addNamespace(ns);
            
            // add attr values to annotation
            for (int j=0; j<values.size(); j++){
                addString("\t\t\t");
                addString("<" + name + ">");
                addString(escapeCDATA((String)values.get(j)));
                addString("</" + name + ">");
                newLine();
            }
        }
        
        addString("\t\t</xs:documentation>");
        newLine();
        addString("\t</xs:annotation>");
        newLine();
    }
    
    protected void writeChoice(Vector children, String tab, String minOcc, String maxOcc) throws Exception{
        
        if (children == null || children.size()==0) return;
        
        addString(tab);
        addString("<xs:choice");
        if (minOcc != null)
            addString(" minOccurs=\"" + minOcc + "\"");
        if (maxOcc != null)
            addString(" maxOccurs=\"" + maxOcc + "\"");
        addString(">");            
        newLine();
            
        for (int i=0; children!=null && i<children.size(); i++){
                
            Object o = children.get(i);
		    Class oClass = o.getClass();
		    String oClassName = oClass.getName();
        		
		    DataElement elem = null;
		    Hashtable child = null;
		    if (oClassName.endsWith("DataElement"))
			    elem = (DataElement)o;
		    else if (oClassName.endsWith("Hashtable"))
			    child = (Hashtable)o;
		    else
			    continue;
                
            if (elem != null){
                    
                Namespace ns = elem.getNamespace();
                    
                addImport(elem.getID(), GetSchema.ELM);
				// addNamespace(ns); - substituted with parent's namespace, i.e. referredNs
				// which is added already in parent's write() method
                    
                addString(tab + "\t");
                addString("<xs:element ref=\"");
                //addString(ns.getShortName() + ":" + elem.getShortName());
                //addString("ns" + ns.getID() + ":" + elem.getShortName());
				addString("ns" + ns.getID() + ":" + elem.getIdentifier());
                    
                addString("\"/>");
                newLine();
            }
            else if (child != null){
                
                String childID = (String)child.get("child_id");
                String childType = (String)child.get("child_type");
                
                if (childID == null || childType == null)
                    continue;
                
                Vector v = null;
                if (searchEngine != null)
                    v = searchEngine.getSubElements(childType, childID);
                
                if (childType.equals("seq"))
                    writeSequence(v, tab + "\t", null, null);
            }
        }
            
        addString(tab);
        addString("</xs:choice>");
        newLine();
    }
    
    protected void writeSequence(Vector children, String tab, String minOcc, String maxOcc)
    																		throws Exception{
        
        if (children == null || children.size()==0) return;
        
        addString(tab);
        addString("<xs:sequence");
        
        if (minOcc != null)
            addString(" minOccurs=\"" + minOcc + "\"");
        if (maxOcc != null)
            addString(" maxOccurs=\"" + maxOcc + "\"");
        addString(">");            
        newLine();
            
        for (int i=0; children!=null && i<children.size(); i++){
                
            Object o = children.get(i);
		    Class oClass = o.getClass();
		    String oClassName = oClass.getName();
        		
		    DataElement elem = null;
		    Hashtable child = null;
		    DsTable dsTable = null;
		    
		    if (oClassName.endsWith("DataElement"))
			    elem = (DataElement)o;
			else if (oClassName.endsWith("DsTable"))
			    dsTable = (DsTable)o;
		    else if (oClassName.endsWith("Hashtable"))
			    child = (Hashtable)o;
		    else
			    continue;
                
            if (elem != null){
                    
                Namespace ns = elem.getNamespace();
                
                addImport(elem.getID(), GetSchema.ELM);
                // addNamespace(ns); - substituted with parent's namespace, i.e. referredNs
                // which is added already in parent's write() method
                    
                addString(tab + "\t");
                addString("<xs:element ref=\"");
				addString(referredNsPrefix + ":" + elem.getIdentifier());
                //addString(referredNsPrefix + ":" + elem.getShortName());
                
                String minOccs = elem.getMinOccurs();
                String maxOccs = elem.getMaxOccurs();
                if (this.getClass().getName().endsWith("TblSchema")){
                    minOccs = "1";
                    maxOccs = "1";
                }
                
                addString("\" minOccurs=\"");
                addString(minOccs);
                addString("\" maxOccurs=\"");
                addString(maxOccs);
                    
                addString("\"/>");
                newLine();
            }
            else if (dsTable != null){

                Namespace ns = null;
                String nsID = dsTable.getNamespace();
                if (!Util.voidStr(nsID))
                    ns = searchEngine.getNamespace(nsID);
                
                if (ns == null)
                    ns = searchEngine.getNamespace("1");
                
                addImport(dsTable.getID(), GetSchema.TBL);
				// addNamespace(ns); - substituted with parent's namespace, i.e. referredNs
				// which is added already in parent's write() method
            
                addString(tab + "\t");
                addString("<xs:element ref=\"");                
				addString(referredNsPrefix + ":" + dsTable.getIdentifier());
//				addString(referredNsPrefix + ":" + dsTable.getShortName());
                    
                addString("\" minOccurs=\"");
                addString("1");
                addString("\" maxOccurs=\"");
                addString("1");
                    
                addString("\"/>");
                newLine();
            }
            else if (child != null){
                
                String childID = (String)child.get("child_id");
                String childType = (String)child.get("child_type");
                String childMinOcc = (String)child.get("child_min_occ");
                String childMaxOcc = (String)child.get("child_max_occ");
                
                if (childID == null || childType == null)
                    continue;
                
                Vector v = null;
                if (searchEngine != null)
                    v = searchEngine.getSubElements(childType, childID);
                
                if (childType.equals("chc"))
                    writeChoice(v, tab + "\t", childMinOcc, childMaxOcc);
            }
        }
            
        addString(tab);
        addString("</xs:sequence>");
        newLine();
    }
    
    private void writeHeader(){
        
         writer.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        //writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.print(lineTerminator);
        writer.print("<xs:schema targetNamespace=\"");        
        writer.print(targetNsUrl);
        writer.print("\" ");
        
        Iterator iter = namespaces.iterator();
        while (iter.hasNext()){
            writer.print(iter.next());
            writer.print(" ");
        }
        
        writer.print("elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
        writer.print(lineTerminator);
    }
    
    private void writeImports(){
        
        Iterator iter = imports.iterator();
        while (iter.hasNext()){
            writer.print("\t");
            writer.print((String)iter.next());
            writer.print(lineTerminator);
        }
    }
    
    private void writeFooter(){
        writer.print(lineTerminator);
        writer.print("</xs:schema>");
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
}