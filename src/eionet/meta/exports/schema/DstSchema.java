
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
        
		// set target namespace (being the so-called "datasets" namespace) 
		setTargetNsUrl(NSID_DATASETS);
		
		// set the dataset corresponding namespace
		String nsID = ds.getNamespaceID();
		if (!Util.voidStr(nsID)){
			Namespace ns = searchEngine.getNamespace(nsID);
			if (ns != null){
				addNamespace(ns);
				setRefferedNs(ns);
			}
		}
        
        //writeElemStart(ds.getShortName());
		writeElemStart(ds.getIdentifier());
        writeAnnotation(ds.getSimpleAttributes(), ds.getComplexAttributes());
        writeContent(ds);
        writeElemEnd();
    }
    
    private void writeContent(Dataset ds) throws Exception {
        
        //addString("\t<xs:complexType name=\"type" + ds.getShortName() + "\">");
		addString("\t<xs:complexType name=\"type" + ds.getIdentifier() + "\">");
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
