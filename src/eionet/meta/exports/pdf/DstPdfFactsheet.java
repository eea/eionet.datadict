
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import java.io.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class DstPdfFactsheet extends PdfHandout {
    
    private int vsTableIndex = -1;
    
    public DstPdfFactsheet(Connection conn, OutputStream os){
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
    }
    
    public void write(String dsID) throws Exception {
        
        if (Util.voidStr(dsID))
            throw new Exception("Dataset ID not specified");
        
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
    
    private void write(Dataset ds) throws Exception {
        
        if (ds == null)
            throw new Exception("Dataset object is null!");

        Paragraph prg = new Paragraph();
        prg.add(new Chunk(ds.getShortName(), Fonts.get(Fonts.HEADING_1_ITALIC)));
        prg.add(new Chunk(" dataset", Fonts.get(Fonts.HEADING_1)));
        prg.setAlignment(Element.ALIGN_CENTER);
        
        addElement(new Paragraph("\n"));
        addElement(prg);
        addElement(new Paragraph("\n", Fonts.get(Fonts.HEADING_1)));
        
        // write simple attributes
        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
        Vector attrs = ds.getSimpleAttributes();
        Hashtable hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", ds.getShortName());
        attrs.add(0, hash);
        
        String version = ds.getVersion();
        if (!Util.voidStr(version)){
            hash = new Hashtable();
            hash.put("name", "Version");
            hash.put("value", version);
            attrs.add(1, hash);
        }
        
		String regStatus = ds.getStatus();
		if (!Util.voidStr(regStatus)){
			hash = new Hashtable();
			hash.put("name", "Registration status");
			hash.put("value", regStatus);
			attrs.add(2, hash);
		}
            
        addElement(PdfUtil.simpleAttributesTable(attrs));
        addElement(new Phrase("\n"));
        
        // write complex attributes, one table for each
        
        Vector v = ds.getComplexAttributes();
        if (v!=null && v.size()>0){
            
            DElemAttribute attr = null;
            for (int i=0; i<v.size(); i++){
                attr = (DElemAttribute)v.get(i);
                attr.setFields(searchEngine.getAttrFields(attr.getID()));
            }
            
            for (int i=0; i<v.size(); i++){
                
                addElement(PdfUtil.complexAttributeTable((DElemAttribute)v.get(i)));
            }
        }

		/* write image attributes
		Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath);
		if (imgAttrs!=null){
			addElement(new Phrase("\n"));
			addElement(imgAttrs);
		}*/
        
        // write tables of tables in dataset
        
        addElement(new Phrase("Tables in this dataset:\n", Fonts.get(Fonts.HEADING_0)));
        
        Vector tables = ds.getTables();
        for (int i=0; tables!=null && i<tables.size(); i++){
            
            // write caption (short name + name)
            DsTable dsTable = (DsTable)tables.get(i);
            Vector captions = new Vector();
            captions.add(dsTable.getShortName());
            if (!Util.voidStr(dsTable.getName()))
                captions.add(dsTable.getName());
            
            // write table elements
            v = searchEngine.getDataElements(null, null, null, null, dsTable.getID());
            addElement(PdfUtil.tableElements(v, captions));
            addElement(new Phrase("\n"));
        }
        
        // add dataset visual structure image
        if (ds.getVisual() != null){
            String fullPath = vsPath + ds.getVisual();
            File file = new File(fullPath);
            if (file.exists()){
                
                try {
                    PdfPTable table = PdfUtil.vsTable(fullPath, "Dataset visual structure");
                    if (table != null){
                        //insertPageBreak();
                        int size = addElement(table);
                        vsTableIndex = size-1;
                    }
                }
                catch (IOException e){
                    
                    // If there was an IOException, it very probably means that
                    // the file was not an image. But we nevertheless tell the
                    // user where the file can be seen.
                    
                    StringBuffer buf = new StringBuffer("\n\n");
                    buf.append("Visual structure of this dataset ");
                    buf.append("can be downloaded from its detailed view ");
                    buf.append("in the Data Dictionary website.");
                            
                    addElement(new Phrase(buf.toString()));
                }
            }
        }
        
        // set the factsheet header
        setHeader("dataset factsheet");
    }
    
    protected boolean keepOnOnePage(int index){
        if (index == vsTableIndex)
            return true;
        else
            return false;
    }
    
    public static void main(String[] args){
        
        String id = "";
        
        if (args!=null && args.length > 0)
            id = args[0];
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            //DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\ds_test.pdf";
            DstPdfFactsheet factsheet = new DstPdfFactsheet(conn, new FileOutputStream(fileName));
            factsheet.setVsPath("x:\\projects\\datadict\\visuals");
            factsheet.setLogo("x:\\projects\\datadict\\images\\pdf_logo.png");
            factsheet.write(id);
            factsheet.flush();
        }
        catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
