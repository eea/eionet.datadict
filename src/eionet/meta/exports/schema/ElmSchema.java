
package eionet.meta.exports.schema;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

public class ElmSchema extends Schema {
    
    public ElmSchema(DDSearchEngine searchEngine, PrintWriter writer){
        super(searchEngine, writer);
    }
    
    /**
    * Write a schema for an object given by ID.
    */
    public void write(String elemID) throws Exception{
        
        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E"));
        
        if (elem.getType().equalsIgnoreCase("AGG")){
	            
	        String choiceID = elem.getChoice();
	        String sequenceID = elem.getSequence();
	            
	        if (choiceID != null && sequenceID != null)
	            throw new Exception("An aggregate cannot have both a sequence and a choice!");
        		
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
	            }
        	        
	            elem.setSubElements(subElements);
	        }
        }
        
        if (elem.getType().equalsIgnoreCase("CH1")){
        		
		    Vector fixedValues =
		        searchEngine.getAllFixedValues(elem.getID(), "elem");
		    elem.setFixedValues(fixedValues);
        }
        
        write(elem);
    }
    
    /**
    * Write a schema for a given object.
    */
    private void write(DataElement elem) throws Exception{

        Namespace ns = elem.getNamespace();
        if (ns != null){
            // add to namespaces
            addNamespace(ns);
            
            // set target namespace url
            setTargetNsUrl(ns.getID());
        }
        
        writeElemStart(elem.getShortName());
        writeAnnotation(elem.getAttributes(), elem.getComplexAttributes());
        writeContent(elem);
        writeElemEnd();
    }
    
    private void writeContent(DataElement elem) throws Exception {
        
        String type = elem.getType();
        if (type.equalsIgnoreCase("AGG"))
            writeComplexContent(elem);
        else
            writeSimpleContent(elem);
    }
    
    protected void writeComplexContent(DataElement elem) throws Exception {
        
        addString("\t<xs:complexType name=\"type" + elem.getShortName() + "\">");
        newLine();
        
        String tab = "\t\t";
        
        String extendsID = elem.getExtension();
        if (extendsID != null && searchEngine != null){
            DataElement ext = searchEngine.getDataElement(extendsID);
            Namespace extNs = ext.getNamespace();
            
            addString(tab);
            addString("<xs:complexContent>");
            newLine();
            
            addString(tab + "\t");
            addString("<xs:extension base=\"");
            //addString(extNs.getShortName() + ":" + ext.getShortName());
            addString("ns" + extNs.getID() + ":" + ext.getShortName());
            addString("\">");
            newLine();
            
            tab = tab + "\t\t";
        }
        
        Vector subElements = elem.getSubElements();
        
        if (elem.getChoice()!=null)
            writeChoice(subElements, tab, null, null);
        else if (elem.getSequence()!=null)
            writeSequence(subElements, tab, null, null);
        
        if (extendsID != null && searchEngine != null){
            addString("\t\t\t");
            addString("</xs:extension>");
            newLine();
            addString("\t\t");
            addString("</xs:complexContent>");
            newLine();
        }
        
        addString("\t");
        addString("</xs:complexType>");
        newLine();
    }
    
    private void writeSimpleContent(DataElement elem) throws Exception {
        
        String dataType = (String)nonAnnotationAttributes.get("Datatype");
        String minSize = (String)nonAnnotationAttributes.get("MinSize");
        String maxSize = (String)nonAnnotationAttributes.get("MaxSize");
        
        addString("\t");
        addString("<xs:simpleType>");
        newLine();
        
        if (dataType != null){
            
            addString("\t\t");
            addString("<xs:restriction base=\"xs:");
            addString(dataType);
            addString("\">");
            newLine();
            
            if (!Util.voidStr(minSize)){
                addString("\t\t\t");
                addString("<xs:minLength value=\"");
                addString(minSize);
                addString("\"/>");
                newLine();
            }
            
            if (!Util.voidStr(maxSize)){
                addString("\t\t\t");
                addString("<xs:maxLength value=\"");
                addString(maxSize);
                addString("\"/>");
                newLine();
            }
            
            Vector fixedValues = elem.getFixedValues();
            for (int k=0; fixedValues!= null && k<fixedValues.size(); k++){
                
                FixedValue fxv = (FixedValue)fixedValues.get(k);
            
                addString("\t\t\t");
                addString("<xs:enumeration value=\"");
                addString(escape(fxv.getValue()));
                addString("\"/>");
                newLine();
            }
            
            addString("\t\t");
            addString("</xs:restriction>");
            newLine();
        }
        
        addString("\t");
        addString("</xs:simpleType>");
        newLine();
    }
    
    public static void main(String[] args){
        
        Connection conn = null;
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            //conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            conn = DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
            FileOutputStream os = new FileOutputStream("x:\\temp\\test.xsd");
            PrintWriter writer = new PrintWriter(os);
            ElmSchema elmSchema = new ElmSchema(searchEngine, writer);
            elmSchema.setIdentitation("\t");
            elmSchema.setAppContext("http://localhost:8080/datadict/public");
            //elmSchema.write("104");
            //elmSchema.write("4593");
            elmSchema.write("111");
            elmSchema.flush();
            
            writer.flush();
            writer.close();
            os.flush();
            os.close();
        }
        catch (Exception e){
            e.printStackTrace();
            //System.out.println(e.toString());
        }
        finally{
            if (conn != null){
                try{ conn.close(); }
                catch (Exception e) {}
            }
        }
    }
}