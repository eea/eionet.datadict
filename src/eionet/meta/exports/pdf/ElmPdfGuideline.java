
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import com.lowagie.text.*;

public class ElmPdfGuideline {
    
    private DDSearchEngine searchEngine = null;
    private Section parentSection = null;
    private Section section = null;
    
    private Vector docElements = new Vector();
    
    public ElmPdfGuideline(DDSearchEngine searchEngine, Section parentSection)
        throws Exception {
            
        if (parentSection==null)
            throw new Exception("parentSection cannot be null!");
        if (searchEngine==null)
            throw new Exception("searchEngine cannot be null!");
            
        this.searchEngine = searchEngine;
        this.parentSection = parentSection;
    }
    
	public void write(String elemID) throws Exception {
		write(elemID, null);
	}
    
    protected void write(String elemID, String tblID) throws Exception {
        
        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID, tblID);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E"));
        
        write(elem);
    }
    
    /**
    * Write a factsheet for a data element given by object.
    */
    private void write(DataElement elem) throws Exception {
        
        if (elem==null)
            throw new Exception("Element object was null!");
        
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(elem.getShortName(), Fonts.get(Fonts.HEADING_3_ITALIC)));
        prg.add(new Chunk(" data element", Fonts.get(Fonts.HEADING_3)));
        
        section = parentSection.addSection(prg, 3);
        
        // see if this guideline is part of a table, get the
        // latter's information.
        
        String tableID = elem.getTableID();
        if (Util.voidStr(tableID)){
                
            String msg =
            "\nWarning! This guideline does not fully reflect the " +
            "table and dataset where this data element belongs to!\n\n";
            
            addElement(new Phrase(msg, Fonts.get(Fonts.WARNING)));
        }
        
        // write simple attributes
        addElement(new Paragraph("\n"));
        
        Hashtable hash = null;
        Vector v = elem.getAttributes();
        
        // dataset name, table name
        /* JH151003 - not needed, cause elm gdln is always part of a tbl gdln
        if (!Util.voidStr(tableID)){
            DsTable dsTable = searchEngine.getDatasetTable(tableID);
            if (dsTable != null){
                Dataset ds = searchEngine.getDataset(dsTable.getDatasetID());
                if (ds != null){
                    hash = new Hashtable();
                    hash.put("name", "Dataset");
                    hash.put("value", ds.getShortName());
                    v.add(0, hash);
                }
                
                hash = new Hashtable();
                hash.put("name", "Table");
                hash.put("value", dsTable.getShortName());
                v.add(0, hash);
            }
        }
        */
        
        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", elem.getShortName());
        v.add(0, hash);
        
        addElement(PdfUtil.simpleAttributesTable(v));
        addElement(new Phrase("\n"));
        
		// write foreign key reltaions if any exist
		Vector fks = searchEngine.getFKRelationsElm(elem.getID());
		if (fks!=null && fks.size()>0){
			addElement(PdfUtil.foreignKeys(fks));
			addElement(new Phrase("\n"));
		}
			 
        // write complex attributes, one table for each
        v = elem.getComplexAttributes();
        if (v!=null && v.size()>0){
            
            DElemAttribute attr = null;
            for (int i=0; i<v.size(); i++){
                attr = (DElemAttribute)v.get(i);
                attr.setFields(searchEngine.getAttrFields(attr.getID()));
            }
            
            for (int i=0; i<v.size(); i++){
                
                addElement(PdfUtil.complexAttributeTable((DElemAttribute)v.get(i)));
                addElement(new Phrase("\n"));
            }
        }
        
        // write allowable values (for a factsheet levelling not needed I guess)
        v = searchEngine.getAllFixedValues(elem.getID(), "elem");
        if (v!=null && v.size()>0){
            addElement(new Phrase("! This data element may only have the " +
                                "following fixed values:\n", Fonts.get(Fonts.HEADING_0)));
            addElement(PdfUtil.fixedValuesTable(v, false));
        }
        
        // write aggregate structure
        // ... not implemented, as aggregates are currently out of focus
    }
    
    private void addElement(Element elm){
        
        if (elm != null)
            section.add(elm);
        
        //return docElements.size();
    }
    
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            //DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\elm_test_guideline.pdf";
            
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            Section chapter = (Section)new Chapter("test", 1);
            ElmPdfGuideline guideline = new ElmPdfGuideline(searchEngine, chapter);
            guideline.write("4518");
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}