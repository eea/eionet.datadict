
package eionet.meta.exports.schema;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

public class DstSchema extends Schema {
    
    public DstSchema(DDSearchEngine searchEngine, PrintWriter writer){
        super(searchEngine, writer);
    }
    
    /**
    * Write a schema for an object given by ID.
    */
    public void write(String dsID) throws Exception{
        
        if (Util.voidStr(dsID))
            throw new Exception("Dataset ID not specified!");
        
        Dataset ds = searchEngine.getDataset(dsID);
        if (ds == null)
            throw new Exception("Dataset not found!");
        
        Vector v = searchEngine.getSimpleAttributes(dsID, "DS");
        ds.setSimpleAttributes(v);
        v = searchEngine.getComplexAttributes(dsID, "DS");
        ds.setComplexAttributes(v);
        v = searchEngine.getDatasetTables(dsID);
        ds.setTables(v);
        
        write(ds);
    }
    
    /**
    * Write a schema for a given object.
    */
    private void write(Dataset ds) throws Exception{
        
        Namespace ns = null;
        String nsID = ds.getNamespaceID();
        if (!Util.voidStr(nsID))
            ns = searchEngine.getNamespace(nsID);
        if (ns == null)
            ns = searchEngine.getNamespace("1");
        
        if (ns != null){
            // add to namespaces
            addNamespace(ns);
                
            // set target namespace url
            setTargetNsUrl(ns.getID());
        }
        
        writeElemStart(ds.getShortName());
        writeAnnotation(ds.getSimpleAttributes(), ds.getComplexAttributes());
        writeContent(ds);
        writeElemEnd();
    }
    
    private void writeContent(Dataset ds) throws Exception {
        
        addString("\t<xs:complexType name=\"type" + ds.getShortName() + "\">");
        newLine();
        
        String tab = "\t\t";
        
        writeSequence(ds.getTables(), tab, null, null);
        
        addString("\t");
        addString("</xs:complexType>");
        newLine();
    }
    
    public static void main(String[] args){
        
        Connection conn = null;
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
            FileOutputStream os = new FileOutputStream("x:\\temp\\test_ds.xsd");
            PrintWriter writer = new PrintWriter(os);
            DstSchema dstSchema = new DstSchema(searchEngine, writer);
            dstSchema.setIdentitation("\t");
            dstSchema.setAppContext("http://127.0.0.1:8080/datadict/public");
            dstSchema.write("631");
            dstSchema.flush();
            
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