package eionet.meta.schema;

import java.util.*;
import java.io.*;
import eionet.meta.*;

import java.sql.*;

import javax.servlet.ServletContext;

public class SchemaExp{
    
    private static final String ANNOTATION_TAB = "\t\t";
    private static final String CONTENT_TAB = "\t\t";
    
    public static final String DATASET_MODE = "DATASET";
    public static final String ELEMENT_MODE = "ELEMENT";
    
    private DataElement dataElement = null;
    private String basensPath = "";
    private ServletContext ctx = null;
    private String lineSeparator = "\n";
    
    private HashSet namespaces = new HashSet();
    private HashSet imports = new HashSet();
    private Hashtable specialAttributes = new Hashtable();
    
    private StringBuffer result = new StringBuffer();
    
    private DDSearchEngine searchEngine = null;
    
    private String mode = ELEMENT_MODE;
    
    private Vector dsElemSchemas = new Vector();
   
    public SchemaExp(DataElement dataElement){
        
        this.dataElement = dataElement;
        
        this.specialAttributes.put("iso:Datatype", "");
        this.specialAttributes.put("iso:MinSize", "");
        this.specialAttributes.put("iso:MaxSize", "");
        
        this.namespaces.add("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
    }
    
    public SchemaExp(DataElement dataElement, DDSearchEngine searchEngine){
        this(dataElement);
        this.searchEngine = searchEngine;
    }
    
    public void setMode(String mode){
        this.mode = mode;
    }
    
    public void export(PrintWriter writer) throws Exception {
        
        generate();
        if (result == null) return;
        StringReader sReader = new StringReader(result.toString());
        BufferedReader reader = new BufferedReader(sReader);
        String line = null;
        while ((line = reader.readLine()) != null){
            writer.println(line);
        }
    }
    
    public String export() throws Exception {
        
        generate();
        if (result != null)
            return result.toString();
        else
            return null;
    }
    
    private void generate() throws Exception {
        
        if (dataElement == null) return;
        if (dataElement.getShortName() == null) return;
        if (dataElement.getNamespace() == null) return;
        if (dataElement.getNamespace().getShortName() == null) return;
        if (dataElement.getNamespace().getUrl() == null) return;
        
        lineSeparator = File.separator.equals("/") ? "\r\n" : "\n";
        
        if (mode.equals(DATASET_MODE)){
            
            DataElement dsElem = dataElement;
            Vector dsElements = dsElem.getSubElements();
            
            for (int i=0; dsElements != null && i<dsElements.size(); i++){
                
                DataElement elm = (DataElement)dsElements.get(i);
                dataElement = searchEngine.getDataElement(elm.getID());
                if (dataElement.getType().equals("CH1"))
                    dataElement.setFixedValues(searchEngine.getFixedValues(elm.getID()));
                
                String annot = generateAnnotation();
                String cont  = generateContent();
                StringBuffer dsElemSchema = new StringBuffer(generateElemStart());
                dsElemSchema.append(annot);
                dsElemSchema.append(cont);
                dsElemSchema.append(generateElemEnd());
                
                dsElemSchemas.add(dsElemSchema.toString());
            }
            
            dataElement = dsElem;
        }
        
        String annotation = generateAnnotation();
        String content = generateContent();
        String header = generateSchemaHeader();
        String footer = generateSchemaFooter();
        
        
        if (annotation == null || header == null || content == null || footer == null)
            return;

        this.result = new StringBuffer();
        this.result.append(header);
        
        if (mode.equals(DATASET_MODE)){
            this.result.append(lineSeparator);
            for (int i=0; i<dsElemSchemas.size(); i++){
                this.result.append(dsElemSchemas.get(i));
                this.result.append(lineSeparator);
            }
        }
            
        this.result.append(generateElemStart());
        this.result.append(annotation);
        this.result.append(content);
        this.result.append(generateElemEnd());
        this.result.append(footer);
    }
    
    private String generateAnnotation() throws Exception{
        
        StringBuffer buf = new StringBuffer(ANNOTATION_TAB);
        buf.append("<xs:annotation>");
        buf.append(lineSeparator);
        buf.append(ANNOTATION_TAB + "\t");
        buf.append("<xs:documentation xml:lang=\"en\">");
        buf.append(lineSeparator);
        
        // simple attributes first
        Vector simpleAttrs = dataElement.getAttributes();
        for (int i=0; simpleAttrs!=null && i<simpleAttrs.size(); i++){
            
            DElemAttribute attr = (DElemAttribute) simpleAttrs.get(i);
            Namespace ns = attr.getNamespace();
            String name = ns.getShortName() + ":" + attr.getShortName();
            
            if (attr.getValue() == null) continue;
            
            if (specialAttributes.containsKey(name)){
                specialAttributes.put(name, attr.getValue());
                continue;
            }
            
            addNamespace(ns);
            
            buf.append(ANNOTATION_TAB + "\t\t");
            buf.append("<" + name + ">");
            buf.append(attr.getValue());
            buf.append("</" + name + ">");
            buf.append(lineSeparator);
        }
        
        buf.append(ANNOTATION_TAB + "\t");
        buf.append("</xs:documentation>");
        buf.append(lineSeparator);
        buf.append(ANNOTATION_TAB);
        buf.append("</xs:annotation>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateSchemaHeader() throws Exception {
        
        Namespace ns = dataElement.getNamespace();
        String nsUrl = ns.getUrl();
        if (nsUrl != null && nsUrl.startsWith("/"))
            nsUrl = basensPath + nsUrl;
        
        StringBuffer nsDeclaration = new StringBuffer("xmlns:");
        nsDeclaration.append(ns.getShortName());
        nsDeclaration.append("=\"" + nsUrl + "\"");
        if (!namespaces.contains(nsDeclaration.toString()))
            namespaces.add(nsDeclaration.toString());
        
        StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        buf.append(lineSeparator);
        buf.append("<xs:schema targetNamespace=\"");        
        buf.append(nsUrl);
        buf.append("\" ");
        
        Iterator iter = namespaces.iterator();
        while (iter.hasNext()){
            buf.append(iter.next());
            buf.append(" ");
        }
        
        buf.append("elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">");
        buf.append(lineSeparator);
        
        if (mode.equals(ELEMENT_MODE)){
            String imps = generateImports();
            if (imps != null)
                buf.append(imps);
        }
        
        return buf.toString();
    }
    
    private String generateElemStart(){
        StringBuffer buf = new StringBuffer("\t");
        buf.append("<xs:element name=\"");
        buf.append(dataElement.getShortName());
        buf.append("\">");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateElemEnd(){
        StringBuffer buf = new StringBuffer("\t");
        buf.append("</xs:element>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateSchemaFooter(){
        
        return "</xs:schema>";
    }
    
    private String generateContent() throws Exception {
        
        String type = dataElement.getType();
        if (type.equalsIgnoreCase("AGG"))
            return generateComplexContent();
        else
            return generateSimpleContent();
    }
    
    private String generateComplexContent() throws Exception {
        
        StringBuffer buf = new StringBuffer(CONTENT_TAB);
        buf.append("<xs:complexType name=\"type" + dataElement.getShortName() + "\">");
        buf.append(lineSeparator);
        
        String tab = CONTENT_TAB + "\t";
        
        String extendsID = dataElement.getExtension();
        if (extendsID != null && searchEngine != null){
            DataElement ext = searchEngine.getDataElement(extendsID);
            buf.append(tab);
            buf.append("<xs:complexContent>");
            buf.append(lineSeparator);
            buf.append(tab + "\t");
            buf.append("<xs:extension base=\"");
            buf.append(ext.getNamespace().getShortName() + ":" + ext.getShortName());
            buf.append("\">");
            buf.append(lineSeparator);
            
            tab = tab + "\t\t";
        }
        
        Vector subElements = dataElement.getSubElements();
        
        String s = "";
        if (dataElement.getChoice()!=null)
            s = generateChoice(subElements, tab, null, null);
        else if (dataElement.getSequence()!=null)
            s = generateSequence(subElements, tab, null, null);
        
        if (s == null)
            buf.append("");
        else
            buf.append(s);
        
        if (extendsID != null && searchEngine != null){
            buf.append(CONTENT_TAB + "\t\t");
            buf.append("</xs:extension>");
            buf.append(lineSeparator);
            buf.append(CONTENT_TAB + "\t");
            buf.append("</xs:complexContent>");
            buf.append(lineSeparator);
        }
        
        buf.append(CONTENT_TAB);
        buf.append("</xs:complexType>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateComplexContent_() throws Exception {
        
        StringBuffer buf = new StringBuffer(CONTENT_TAB);
        buf.append("<xs:complexType name=\"type" + dataElement.getShortName() + "\">");
        buf.append(lineSeparator);
        
        String structTagName = null;
        if (dataElement.getChoice()!=null)
            structTagName = "xs:choice";
        else if (dataElement.getSequence()!=null)
            structTagName = "xs:sequence";
        
        if (structTagName != null){

            buf.append(CONTENT_TAB + "\t");
            buf.append("<" + structTagName + ">");
            buf.append(lineSeparator);
            
            Vector subElements = dataElement.getSubElements();
            
            for (int i=0; subElements!=null && i<subElements.size(); i++){
                
                Object o = subElements.get(i);
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
                    
                    addImport(elem, ns);
                    addNamespace(ns);
                    
                    buf.append(CONTENT_TAB + "\t\t");
                    buf.append("<xs:element ref=\"");
                    buf.append(ns.getShortName() + ":" + elem.getShortName());
                    
                    if (dataElement.getSequence() != null){
                        buf.append("\" minOccurs=\"");
                        buf.append(elem.getMinOccurs());
                        buf.append("\" maxOccurs=\"");
                        buf.append(elem.getMaxOccurs());
                    }
                    
                    buf.append("\"/>");
                    buf.append(lineSeparator);
                }
                else if (child != null){
                    
                    String childType = (String)child.get("child_type");
                    String childID = (String)child.get("child_id");
                    String minOcc = (String)child.get("child_min_occ");
                    String maxOcc = (String)child.get("child_max_occ");
                    
                    if (childType != null && childID != null){
                        
                        String tagName = childType.equals("seq") ? "sequence" : "choice";
                    
                        buf.append(CONTENT_TAB + "\t\t");            
                        buf.append("<xs:" + tagName + " id=\"");
                        buf.append(childID);
                        buf.append("\"");
                        
                        if (dataElement.getSequence() != null){
                            if (minOcc != null)
                                buf.append(" minOccurs=\"" + minOcc + "\"");
                            if (maxOcc != null)
                                buf.append(" maxOccurs=\"" + maxOcc + "\"");
                        }
                        
                        buf.append("/>");
                        buf.append(lineSeparator);
                    }
                }
            }
            
            buf.append(CONTENT_TAB + "\t");
            buf.append("</" + structTagName + ">");
            buf.append(lineSeparator);
        }
        
        buf.append(CONTENT_TAB);
        buf.append("</xs:complexType>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateSequence(Vector children, String tab, String minOcc, String maxOcc) throws Exception{
        
        if (children == null || children.size()==0) return "";
        
        StringBuffer buf = new StringBuffer(tab);
        buf.append("<sequence");
        if (minOcc != null)
            buf.append(" minOccurs=\"" + minOcc + "\"");
        if (maxOcc != null)
            buf.append(" maxOccurs=\"" + maxOcc + "\"");
        buf.append(">");            
        buf.append(lineSeparator);
            
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
                    
                addImport(elem, ns);
                addNamespace(ns);
                    
                buf.append(tab + "\t");
                if (mode.equals(ELEMENT_MODE))
                    buf.append("<xs:element ref=\"");
                else
                    buf.append("<xs:element name=\"");
                buf.append(ns.getShortName() + ":" + elem.getShortName());
                    
                buf.append("\" minOccurs=\"");
                buf.append(elem.getMinOccurs());
                buf.append("\" maxOccurs=\"");
                buf.append(elem.getMaxOccurs());
                    
                buf.append("\"/>");
                buf.append(lineSeparator);
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
                
                String s = "";
                if (childType.equals("chc"))
                    s = generateChoice(v, tab + "\t", childMinOcc, childMaxOcc);
                
                if (s != null)
                    buf.append(s);
            }
        }
            
        buf.append(tab);
        buf.append("</sequence>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private String generateChoice(Vector children, String tab, String minOcc, String maxOcc) throws Exception{
        
        if (children == null || children.size()==0) return "";
        
        StringBuffer buf = new StringBuffer(tab);
        buf.append("<choice");
        if (minOcc != null)
            buf.append(" minOccurs=\"" + minOcc + "\"");
        if (maxOcc != null)
            buf.append(" maxOccurs=\"" + maxOcc + "\"");
        buf.append(">");            
        buf.append(lineSeparator);
            
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
                    
                addImport(elem, ns);
                addNamespace(ns);
                    
                buf.append(tab + "\t");
                if (mode.equals(ELEMENT_MODE))
                    buf.append("<xs:element ref=\"");
                else
                    buf.append("<xs:element name=\"");
                buf.append(ns.getShortName() + ":" + elem.getShortName());
                    
                buf.append("\"/>");
                buf.append(lineSeparator);
            }
            else if (child != null){
                
                String childID = (String)child.get("child_id");
                String childType = (String)child.get("child_type");
                
                if (childID == null || childType == null)
                    continue;
                
                Vector v = null;
                if (searchEngine != null)
                    v = searchEngine.getSubElements(childType, childID);
                
                String s = "";
                if (childType.equals("seq"))
                    s = generateSequence(v, tab + "\t", null, null);
                
                if (s != null)
                    buf.append(s);
            }
        }
            
        buf.append(tab);
        buf.append("</choice>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    private void addImport(DataElement elem, Namespace ns){
        
        StringBuffer importClause = new StringBuffer("<xs:import namespace=\"");
        String url = ns.getUrl();
        if (url.startsWith("/"))
            url = basensPath + url;
        importClause.append(url);
        importClause.append("\" schemaLocation=\"");
        
        importClause.append(elem.getDefinitionUrl());
        importClause.append("\"/>");
        
        if (!imports.contains(importClause.toString()))
            imports.add(importClause.toString());
    }
    
    private void addNamespace(Namespace ns) {
        
        StringBuffer nsDeclaration = new StringBuffer("xmlns:");
        nsDeclaration.append(ns.getShortName());
        String url = ns.getUrl();
        if (url.startsWith("/"))
            url = basensPath + url;
        nsDeclaration.append("=\"" + url + "\"");
            
        if (!namespaces.contains(nsDeclaration.toString()))
            namespaces.add(nsDeclaration.toString());
    }
    
    private String generateImports() throws Exception {
        
        StringBuffer buf = new StringBuffer();
        Iterator iter = imports.iterator();
        while (iter.hasNext()){
            buf.append("\t");
            buf.append((String)iter.next());
            buf.append(lineSeparator);
        }
        
        return buf.toString();
    }
    
    private String generateSimpleContent() throws Exception {
        
        String dataType = (String)specialAttributes.get("iso:Datatype");
        String minSize = (String)specialAttributes.get("iso:MinSize");
        String maxSize = (String)specialAttributes.get("iso:MaxSize");
        
        StringBuffer buf = new StringBuffer(CONTENT_TAB);
        buf.append("<xs:simpleType>");
        buf.append(lineSeparator);
        
        if (dataType != null){
            
            buf.append(CONTENT_TAB + "\t");
            buf.append("<xs:restriction base=\"xs:");
            buf.append(dataType);
            buf.append("\">");
            buf.append(lineSeparator);
            
            if (minSize != null){
                buf.append(CONTENT_TAB + "\t\t");
                buf.append("<xs:minLength value=\"");
                buf.append(minSize);
                buf.append("\"/>");
                buf.append(lineSeparator);
            }
            
            if (maxSize != null){
                buf.append(CONTENT_TAB + "\t\t");
                buf.append("<xs:maxLength value=\"");
                buf.append(maxSize);
                buf.append("\"/>");
                buf.append(lineSeparator);
            }
            
            Vector fixedValues = dataElement.getFixedValues();
            for (int k=0; fixedValues!= null && k<fixedValues.size(); k++){
                
                Hashtable hash = (Hashtable)fixedValues.get(k);
            
                buf.append(CONTENT_TAB + "\t\t");
                buf.append("<xs:enumeration value=\"");
                buf.append(hash.get("value"));
                buf.append("\"/>");
                buf.append(lineSeparator);
            }
            
            buf.append(CONTENT_TAB + "\t");
            buf.append("</xs:restriction>");
            buf.append(lineSeparator);
        }
        
        buf.append(CONTENT_TAB);
        buf.append("</xs:simpleType>");
        buf.append(lineSeparator);
        
        return buf.toString();
    }
    
    public void setBaseNsPath(String path){
        if (path!=null)
            basensPath = path;
    }
    
    public void setServletContext(ServletContext ctx){
        this.ctx = ctx;
    }
    
    public static void main(String[] args){
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
            String delem_id = "188";
            String basens = "http://127.0.0.1:8080/datadict/public";
            
            DataElement dataElement = searchEngine.getDataElement(delem_id);
            if (dataElement == null)
                throw new Exception("Not found");
            
            String choiceID = dataElement.getChoice();
            String sequenceID = dataElement.getSequence();
            
            if (choiceID != null && sequenceID != null){
	            throw new Exception("An aggregate cannot have both a sequence and a choice!");
	        }
        		
		    Vector subElements = null;
		    if (choiceID != null)
		        subElements = searchEngine.getChoice(choiceID);
		    else if (sequenceID != null)
		        subElements = searchEngine.getSequence(sequenceID);
	            
            if (subElements != null && subElements.size()!=0){
                
                for (int i=0; i<subElements.size(); i++){
                    Object o = subElements.get(i);
		            Class oClass = o.getClass();
		            String oClassName = oClass.getName();
                		
		            if (oClassName.endsWith("DataElement")){
			            DataElement elm = (DataElement)o;
	                    elm.setDefinitionUrl(basens + "/GetSchema?delem_id=" + elm.getID());
	                }
                }
                
                dataElement.setSubElements(subElements);
            }
            
            //Vector fixedValues = searchEngine.getFixedValues(delem_id);
            //dataElement.setFixedValues(fixedValues);
            
            //System.out.println(dataElement.toString());
            
            SchemaExp schemaExp = new SchemaExp(dataElement, searchEngine);
            schemaExp.setMode(SchemaExp.DATASET_MODE);
            schemaExp.setBaseNsPath(basens);
            String s = schemaExp.export();
            System.out.println(s);
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}