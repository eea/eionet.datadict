
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import java.io.*;

import com.lowagie.text.*;

public class TblPdfFactsheet extends PdfHandout {
    
    public TblPdfFactsheet(Connection conn, OutputStream os){
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
    }
    
    public void write(String tblID) throws Exception {
        
        if (Util.voidStr(tblID))
            throw new Exception("Table ID not specified");
        
        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable == null)
            throw new Exception("Table not found!");
            
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T", null, dsTable.getDatasetID());
        dsTable.setSimpleAttributes(v);

        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);
        
        // get the dataset basic info
        Dataset ds = null;
        if (!Util.voidStr(dsTable.getDatasetID())){
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        
        write(dsTable, ds);
    }
    
    private void write(DsTable dsTable, Dataset ds) throws Exception {
        
        if (dsTable==null)
            throw new Exception("Table object was null!");
        
        // add simple attributes
        
        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
        
        Hashtable hash = null;
        Vector v = dsTable.getSimpleAttributes();
        
        // dataset short name
        if (ds != null){
            hash = new Hashtable();
            hash.put("name", "Dataset");
            hash.put("value", ds.getShortName());
            v.add(0, hash);
        }
        
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
        
        /* write image attributes
		Element imgAttrs = PdfUtil.imgAttributes(v, vsPath);
		if (imgAttrs!=null){
			addElement(new Phrase("\n"));
			addElement(imgAttrs);
		}*/
        
        // write table elements, but 1st get their fixed values & FK relations
        
        v = dsTable.getElements();
        if (v==null || v.size()==0)
            return;
        
        DataElement elem = null;
        for (int i=0; i<v.size(); i++){
            elem = (DataElement)v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);
            
			String dstID = getParameter("dstID");
			Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
			elem.setFKRelations(fks);
        }
        
        addElement(new Paragraph("Elements in this table:\n", Fonts.get(Fonts.HEADING_0)));
        addElement(PdfUtil.tableElements(v));
        
        // set the factsheet header
        setHeader("dataset table factsheet");
    }
    
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            //DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\tbl_test.pdf";
            TblPdfFactsheet factsheet = new TblPdfFactsheet(conn, new FileOutputStream(fileName));
            factsheet.setLogo("x:\\projects\\datadict\\images\\pdf_logo_small.png");
            factsheet.write("657");
            factsheet.flush();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}