
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import com.lowagie.text.*;

public class TblPdfGuideline {
    
    private DDSearchEngine searchEngine = null;
    private Section parentSection = null;
    private Section section = null;
    
    private Vector docElements = new Vector();
    
    public TblPdfGuideline(DDSearchEngine searchEngine, Section parentSection)
        throws Exception {
            
        if (parentSection==null)
            throw new Exception("parentSection cannot be null!");
        if (searchEngine==null)
            throw new Exception("searchEngine cannot be null!");
            
        this.searchEngine = searchEngine;
        this.parentSection = parentSection;
    }
    
	public void write(String tblID) throws Exception {
		write(tblID, null);
	}
    
    protected void write(String tblID, String dstID) throws Exception {
        
        if (Util.voidStr(tblID))
            throw new Exception("Table ID not specified");
        
        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID, dstID);
        if (dsTable == null)
            throw new Exception("Table not found!");
            
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);
        
        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);
        
        // get the dataset basic info
        /* JH151003 - not needed, cause tbl gdln is always part of a dst gdln
        Dataset ds = null;
        if (!Util.voidStr(dsTable.getDatasetID())){
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        */
        
        write(dsTable);
    }
    
    /**
    * Write a full guideline for a dataset table given by table object.
    */
    private void write(DsTable dsTable) throws Exception {
        
        if (dsTable==null)
            throw new Exception("Table object was null!");
        
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(dsTable.getShortName(), Fonts.get(Fonts.HEADING_2_ITALIC)));
        prg.add(new Chunk(" table", Fonts.get(Fonts.HEADING_2)));
        
        section = parentSection.addSection(prg, 2);
        
        // write simple attributes
        
        Hashtable hash = null;
        Vector v = dsTable.getSimpleAttributes();
        
        // dataset short name
        /* JH151003 - not needed, cause tbl gldn is always part of a dst gldn
        if (ds != null){
            hash = new Hashtable();
            hash.put("name", "Dataset");
            hash.put("value", ds.getShortName());
            v.add(0, hash);
        }
        */
        
        // type
        String type = dsTable.getType();
        if (!Util.voidStr(type)){
            hash = new Hashtable();
            hash.put("name", "Type");
            hash.put("value", type);
            v.add(0, hash);
        }
        
        // definition
        String definition = dsTable.getDefinition();
        if (!Util.voidStr(definition)){
            hash = new Hashtable();
            hash.put("name", "Definition");
            hash.put("value", definition);
            v.add(0, hash);
        }
        
        // definition
        String name = dsTable.getName();
        if (!Util.voidStr(name)){
            hash = new Hashtable();
            hash.put("name", "Name");
            hash.put("value", name);
            v.add(0, hash);
        }
        
        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", dsTable.getShortName());
        v.add(0, hash);
        
        addElement(PdfUtil.simpleAttributesTable(v));
        addElement(new Phrase("\n"));
        
        // write table elements factlist, but first get fixed values & fk rels        
        v = dsTable.getElements();
        if (v==null || v.size()==0)
            return;
        
        DataElement elem = null;
        for (int i=0; i<v.size(); i++){
            elem = (DataElement)v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);
			Vector fks = searchEngine.getFKRelationsElm(elem.getID());
			elem.setFKRelations(fks);
        }
        
        addElement(new Paragraph("Elements in this table:\n", Fonts.get(Fonts.HEADING_0)));
        addElement(PdfUtil.tableElements(v));
        
        // write data element full guidelines, each into a separate chapter
        for (int i=0; v!=null && i<v.size(); i++){
            elem = (DataElement)v.get(i);
            addElement(new Paragraph("\n"));
            ElmPdfGuideline elmGuideln = new ElmPdfGuideline(searchEngine, section);
            elmGuideln.write(elem.getID(), dsTable.getID());
        }
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

            String fileName = "x:\\projects\\datadict\\tmp\\tbl_test_guideline.pdf";
            
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            Section chapter = (Section)new Chapter("test", 1);
            TblPdfGuideline guideline = new TblPdfGuideline(searchEngine, chapter);
            guideline.write("657");
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}