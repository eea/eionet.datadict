
package eionet.meta.exports.schema;

import java.io.PrintWriter;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.Dataset;
import eionet.meta.Namespace;
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
        if (ds != null){
        
	        Vector v = searchEngine.getSimpleAttributes(dsID, "DS");
	        ds.setSimpleAttributes(v);
	        v = searchEngine.getComplexAttributes(dsID, "DS");
	        ds.setComplexAttributes(v);
	        v = searchEngine.getDatasetTables(dsID, true);
	        ds.setTables(v);
	        
	        write(ds);
        }
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
    
    protected void writeContent(Dataset ds) throws Exception {
        
        //addString("\t<xs:complexType name=\"type" + ds.getShortName() + "\">");
		//addString("\t<xs:complexType name=\"type" + ds.getIdentifier() + "\">");
		addString("\t<xs:complexType>");
        newLine();
        
        String tab = "\t\t";
        
        writeSequence(ds.getTables(), tab, null, null);
        
        addString("\t");
        addString("</xs:complexType>");
        newLine();
    }
    
}
