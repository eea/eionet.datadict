
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import java.io.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class DstPdfGuideline extends PdfHandout {
    
    private String vsPath = null;
    private int vsTableIndex = -1;
    
    private int elmCount = 0;
    
    private Chapter chapter = null;
    
    private String dsName = "";
    
    public DstPdfGuideline(Connection conn, OutputStream os){
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
    }
    
    public void setVsPath(String s){
        
        vsPath = s;
        
        if (!Util.voidStr(vsPath)){
            if (!vsPath.endsWith(File.separator))
                vsPath = vsPath + File.separator;
        }
        else
            vsPath = System.getProperty("user.dir") + File.separator;
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
        
        String s = ds.getAttributeValueByShortName("Name");
        dsName = Util.voidStr(s) ? ds.getShortName() : s;
            
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(ds.getShortName(), Fonts.get(Fonts.HEADING_1_ITALIC)));
        prg.add(new Chunk(" dataset", Fonts.get(Fonts.HEADING_1)));
        
        chapter = new Chapter(prg, 1);
        
        // add the dataset chapter to the document
        elmCount = super.addElement(chapter);
        
        addElement(new Paragraph("\n"));
        
        // write simple attributes
        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
        
        Vector v = ds.getSimpleAttributes();

        Hashtable hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", ds.getShortName());
        v.add(0, hash);
        
        String version = ds.getVersion();
        if (!Util.voidStr(version)){
            hash = new Hashtable();
            hash.put("name", "Version");
            hash.put("value", version);
            v.add(1, hash);
        }
            
        addElement(PdfUtil.simpleAttributesTable(v));
        addElement(new Phrase("\n"));
        
        // write complex attributes, one table for each
        
        v = ds.getComplexAttributes();
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
        
        // write tables list
        
        addElement(new Phrase("Tables in this dataset:\n", Fonts.get(Fonts.HEADING_0)));
        
        Vector tables = ds.getTables();
        addElement(PdfUtil.tablesList(tables));
        addElement(new Phrase("\n"));
        
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
        
        // add full guidlines of tables
        for (int i=0; tables!=null && i<tables.size(); i++){
            DsTable dsTable = (DsTable)tables.get(i);
            // the tables guidelines will be added to the currnet chapter
            addElement(new Paragraph("\n"));
            TblPdfGuideline tblGuideln = new TblPdfGuideline(searchEngine, (Section)chapter);
            tblGuideln.write(dsTable.getID());
        }

        // set the factsheet header
        setHeader("dataset full definition");
    }
    
    protected int addElement(Element elm){
        
        if (elm == null || chapter == null)
            return elmCount;
        
        chapter.add(elm);
        elmCount = elmCount + 1;
        return elmCount;
    }
    
    protected boolean keepOnOnePage(int index){
        if (index == vsTableIndex)
            return true;
        else
            return false;
    }
    
    /**
    * Override of the method for adding a title page
    */
    protected void addTitlePage(Document doc) throws Exception {
        
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26);
        
        // reportnet
        Paragraph prg = new Paragraph("Reportnet", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        // data dictionary
        font = FontFactory.getFont(FontFactory.TIMES_BOLD, 26);
        prg = new Paragraph("Data Dictionary", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n"));
        
        // full definition
        font = FontFactory.getFont(FontFactory.COURIER, 14);
        prg = new Paragraph("Full definition of", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        // dataset name
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 24);
        prg = new Paragraph(dsName, font);
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        prg.add(new Chunk(" dataset", font));
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
        doc.add(new Paragraph("\n\n\n\n\n\n"));
        
        // European Environment Agency
        font = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        prg = new Paragraph("European Environment Agency", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        
        if (!Util.voidStr(logo)){
            Image img = Image.getInstance(logo);
            img.setAlignment(Image.LEFT);
                    
            prg.add(new Chunk(img, 0, 0));
        }
        
        doc.add(prg);
    }
    
    /**
    * Override of the method indicating if title page needed
    */
    protected boolean titlePageNeeded(){
        return true;
    }
    
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            //DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            String fileName = "x:\\projects\\datadict\\tmp\\ds_test_guideline.pdf";
            DstPdfGuideline guideline = new DstPdfGuideline(conn, new FileOutputStream(fileName));
            guideline.setVsPath("x:\\projects\\datadict\\visuals");
            guideline.setLogo("x:\\projects\\datadict\\images\\pdf_logo_small.png");
            guideline.write("637");
            guideline.flush();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}