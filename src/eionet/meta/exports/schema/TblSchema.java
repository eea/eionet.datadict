
package eionet.meta.exports.schema;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;

public class TblSchema extends Schema {
	
	private String ROW_NS = "1";
    
    public TblSchema(DDSearchEngine searchEngine, PrintWriter writer){
        super(searchEngine, writer);
    }
    
    /**
    * Write a schema for an object given by ID.
    */
    public void write(String tblID) throws Exception{
        
        if (Util.voidStr(tblID))
            throw new Exception("Dataset table ID not specified!");
        
        // Get the dataset table object. This will also give us the
        // element's simple attributes + tableID
        
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable == null)
            throw new Exception("Dataset table not found!");
        
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T", null, dsTable.getDatasetID());
        dsTable.setSimpleAttributes(v);
        
        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);
        
        write(dsTable);
    }
    
    /**
    * Write a schema for a given object.
    */
    private void write(DsTable dsTable) throws Exception{

		// set target namespace (being the parent dataset's namespace)
		String parentNsID = dsTable.getParentNs(); 
		if (parentNsID!=null) setTargetNsUrl(parentNsID);

        // set the table's corresponding namespace (referred namespace for children)
        String nsID = dsTable.getNamespace();
        if (!Util.voidStr(nsID)){
            Namespace ns = searchEngine.getNamespace(nsID);
            if (ns != null){
                addNamespace(ns);
				setRefferedNs(ns);
            }
        }
        
        // add the namespace declaration for ns1, used by Row element
		/*Namespace ns = searchEngine.getNamespace(ROW_NS);
		if (ns != null){
			ROW_NS = ROW_NS + ":";
			addNamespace(ns);
		}
		else
			ROW_NS = "";*/
        
        //writeElemStart(dsTable.getShortName());
		writeElemStart(dsTable.getIdentifier());
        writeAnnotation(dsTable.getSimpleAttributes(), dsTable.getComplexAttributes());
        writeContent(dsTable);
        writeElemEnd();
    }
    
    private void writeContent(DsTable dsTable) throws Exception {
    	
        //addString("\t<xs:complexType name=\"type" + dsTable.getShortName() + "\">");
		addString("\t<xs:complexType name=\"type" + dsTable.getIdentifier() + "\">");
        newLine();
        addString("\t\t<xs:sequence>");
        newLine();
        addString("\t\t\t<xs:element name=\"Row\" minOccurs=\"1\" maxOccurs=\"unbounded\">");
		//addString("\t\t\t<xs:element ref=\"" + this.referredNsPrefix + ":Row\" minOccurs=\"1\" maxOccurs=\"unbounded\">");
		newLine();
        addString("\t\t\t\t<xs:complexType>");
		newLine();
		addString("\t\t\t\t\t<xs:attribute name=\"status\" type=\"xs:string\" use=\"optional\"/>");
        newLine();
        
        String tab = "\t\t\t\t\t";
        
        writeSequence(dsTable.getElements(), tab, null, null);
        
        addString("\t\t\t\t</xs:complexType>");
        newLine();
        addString("\t\t\t</xs:element>");
        newLine();
        addString("\t\t</xs:sequence>");
        newLine();
        addString("\t</xs:complexType>");
        newLine();
    }
    
    public static void main(String[] args){
        
        Connection conn = null;
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            
            FileOutputStream os = new FileOutputStream("x:\\temp\\test_table.xsd");
            PrintWriter writer = new PrintWriter(os);
            TblSchema tblSchema = new TblSchema(searchEngine, writer);
            tblSchema.setIdentitation("\t");
            tblSchema.setAppContext("http://127.0.0.1:8080/datadict/public");
            tblSchema.write("653");
            tblSchema.flush();
            
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