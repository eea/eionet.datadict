
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import java.io.*;

import com.lowagie.text.*;

public class ElmPdfFactsheet extends PdfHandout {
    
    public ElmPdfFactsheet(Connection conn, OutputStream os){
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
    }
    
    public void write(String elemID) throws Exception {
        
        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E",null,elem.getTableID(),elem.getDatasetID()));
        
        write(elem);
    }
    
    /**
    * Write a factsheet for a data element given by object.
    */
    private void write(DataElement elem) throws Exception {
        
        String tableID = elem.getTableID();
        if (!Util.voidStr(tableID)){
            
            String msg =
            "\nWarning! This factsheet does not fully reflect the " +
            "table and dataset where this data element belongs to!\n\n";
            
            addElement(new Phrase(msg, Fonts.get(Fonts.WARNING)));
        }
        
        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
        
        // write simple attributes
        
        Hashtable hash = null;
        Vector attrs = elem.getAttributes();
        
        // dataset name, table name
        if (!Util.voidStr(tableID)){
            DsTable dsTable = searchEngine.getDatasetTable(tableID);
            if (dsTable != null){
                Dataset ds = searchEngine.getDataset(dsTable.getDatasetID());
                if (ds != null){
                    hash = new Hashtable();
                    hash.put("name", "Dataset");
                    hash.put("value", ds.getShortName());
                    attrs.add(0, hash);
                }
                
                hash = new Hashtable();
                hash.put("name", "Table");
                hash.put("value", dsTable.getShortName());
                attrs.add(0, hash);
            }
        }
        
        /* extends
        String extID = elem.getExtension();
        if (!Util.voidStr(extID)){
            DataElement extElem = searchEngine.getDataElement(elemID);
            if (extElem != null){
                hash = new Hashtable();
                hash.put("name", "Extends");
                hash.put("value", extElem.getShortName());
                v.add(0, hash);
            }
        }*/
        
        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", elem.getShortName());
        attrs.add(0, hash);
        
        addElement(PdfUtil.simpleAttributesTable(attrs));
        addElement(new Phrase("\n"));
        
        // write foreign key reltaions if any exist
        String dstID = getParameter("dstID");
        Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
        if (fks!=null && fks.size()>0){
			addElement(PdfUtil.foreignKeys(fks));
			addElement(new Phrase("\n"));
        }
        
        // write complex attributes, one table for each
        Vector v = elem.getComplexAttributes();
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
        addElement(PdfUtil.fixedValuesTable(v, false));
        
        // write image attributes
        Vector imgs = PdfUtil.imgAttributes(elem.getAttributes(), vsPath);
		for (int u=0; u<imgs.size(); u++){
			com.lowagie.text.Image img = (com.lowagie.text.Image)imgs.get(u); 
			addElement(img);
		}
        //addElement(PdfUtil.imgAttributes(elem.getAttributes(), vsPath));
        
        /* write image attributes
        Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath);
        if (imgAttrs!=null){
			addElement(new Phrase("\n"));
			addElement(imgAttrs);
        }*/
					
        // write aggregate structure
        // ... not implemented, as aggregates are currently out of focus
        
        // set the factsheet header
        setHeader("data element factsheet");
    }
    
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            //DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\elm_test.pdf";
            ElmPdfFactsheet factsheet = new ElmPdfFactsheet(conn, new FileOutputStream(fileName));
            factsheet.setLogo("x:\\projects\\datadict\\images\\pdf_logo_small.png");
            factsheet.write("4733");
            factsheet.flush();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}