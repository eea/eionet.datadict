
package eionet.util;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.awt.Color;

import eionet.meta.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PdfGenerator {
    
    public static final String CELL_HEADER  = "header";
    public static final String CELL_VALUE   = "value";
    public static final String CELL_CAPTION = "caption";
    public static final String CELL_WIDTH   = "width"; // percentage
    public static final String CELL_COLSPAN = "colspan";
    
    public static final String S_ATTR_NAME = "name";
    public static final String S_ATTR_VALUE = "value";
    
    public static final float  MAX_IMG_LENGTH = 520;
    
    private static final int EVEN = 0;
    private static final int ODD = 1;
    private static final int NONE = 2;
    
    private static final int LEVELLED = 0;
    private static final int NON_LEVELLED = 1;
    
    private Connection conn = null;
    private Document doc = null;
    private PdfWriter pdfWriter = null;
    private DDSearchEngine searchEngine = null;
    
    private String imagesRealPath = null;
    private String imagesWebPath = null;
    
    public PdfGenerator(String fileName, Connection conn) throws Exception {
        this(new File(fileName), conn);
    }
    
    public PdfGenerator(File file, Connection conn) throws Exception {
        this(new FileOutputStream(file), conn);
    }
    
    public PdfGenerator(OutputStream os, Connection conn) throws Exception {
        this.conn = conn;
        this.doc = new Document();
        pdfWriter = PdfWriter.getInstance(doc, os);
    }
    
    public void openDoc() throws Exception {
        
        doc.open();
    }
    
    public void closeDoc(){
        doc.close();
    }
    
    public void setDocumentHeader(String text) throws Exception {
        
        Font font = FontFactory.getFont(FontFactory.COURIER, 10);
        font.setColor(new Color(0,100,200));
        
        Phrase phr = new Phrase(text, font);
        HeaderFooter header = new HeaderFooter(phr, false);
        header.setBorder(Rectangle.BOTTOM);
        doc.setHeader(header);
    }
    
    public void setImagesPaths(String realPath, String webPath){
        
        imagesRealPath = realPath;
        if (imagesRealPath!=null && !imagesRealPath.endsWith(File.separator))
            imagesRealPath = imagesRealPath + File.separator;
        
        imagesWebPath = webPath;
        if (imagesWebPath!=null && !imagesWebPath.endsWith("/"))
            imagesWebPath = imagesWebPath + "/";
    }
    
    public void writeSimple() throws Exception {
        doc.add(new Phrase("KALA"));
    }

    /**
    *   Write dataset factsheet by dataset ID
    */
    public void writeDataset(String dsID) throws Exception {
        
        if (Util.voidStr(dsID))
            throw new Exception("Dataset ID not specified");
        
        if (searchEngine == null)
            createSearchEngine();
        
        Dataset ds = searchEngine.getDataset(dsID);
        if (ds == null)
            throw new Exception("Dataset not found!");
            
        Vector v = searchEngine.getSimpleAttributes(dsID, "DS");
        ds.setSimpleAttributes(v);
        v = searchEngine.getComplexAttributes(dsID, "DS");
        ds.setComplexAttributes(v);
        v = searchEngine.getDatasetTables(dsID);
        ds.setTables(v);
        
        writeDataset(ds);
    }
    
    /**
    *   Write dataset factsheet by dataset object
    */
    private void writeDataset(Dataset ds) throws Exception {
        
        if (ds == null)
            throw new Exception("Dataset object is null!");

        // add simple attributes
        
        doc.add(new Paragraph("Basic metadata:\n"));
        
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
            
        writeSimpleAttributesTable(v);
        
        doc.add(new Phrase("\n"));
        
        // add complex attributes, one table for each
        v = ds.getComplexAttributes();
        for (int i=0; v!=null && i<v.size(); i++){
            DElemAttribute attr = (DElemAttribute)v.get(i);
            attr.setFields(searchEngine.getAttrFields(attr.getID()));
            writeComplexAttributeTable(attr);
            doc.add(new Phrase("\n"));
        }        
        
        // add tables' tables
        doc.add(new Phrase("\nTables in this dataset:\n"));
        Vector tables = ds.getTables();
        for (int i=0; tables!=null && i<tables.size(); i++){
            writeTableTable((DsTable)tables.get(i));
            doc.add(new Phrase("\n"));
        }
        
        // add dataset visual structure image
        if (ds.getVisual() != null){
            
            String filePath = imagesRealPath;
            if (filePath == null)
                filePath = System.getProperty("user.dir") + File.separator;
            filePath = filePath + ds.getVisual();
        
            File file = new File(filePath);
            if (file.exists()){
                if (Util.voidStr(imagesWebPath))
                    addImage(filePath,null);
                else
                    addImage(filePath, imagesWebPath + ds.getVisual());
            }
        }
    }
    
    /**
    *   Write table factsheet by table ID
    */
    public void writeDsTable(String tblID) throws Exception {
        
        if (Util.voidStr(tblID))
            throw new Exception("Table ID not specified");
        
        if (searchEngine == null)
            createSearchEngine();
        
        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable == null)
            throw new Exception("Table not found!");
            
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);
        
        // get data elements
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);
        
        // get the dataset basic info
        Dataset ds = null;
        if (!Util.voidStr(dsTable.getDatasetID())){
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        
        writeDsTable(dsTable, ds);
    }
    
    /**
    *   Write table factsheet by table object
    */
    private void writeDsTable(DsTable dsTable, Dataset ds) throws Exception {
        
        if (dsTable==null)
            throw new Exception("Table object was null!");
        
        if (searchEngine == null)
            createSearchEngine();
        
        // add simple attributes
        doc.add(new Paragraph("Basic metadata:\n"));
        
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
        
        writeSimpleAttributesTable(v);
        
        doc.add(new Phrase("\n"));
        
        writeTableTable(dsTable, true);
        
        /*Vector tblElems = dsTable.getElements();
        for (int i=0; tblElems!=null && i<tblElems.size(); i++){
            DataElement elem = (DataElement)tblElems.get(i);
            writeDataElement(elem);
        }*/
    }
    
    /**
    *   Write data element factsheet by data element ID
    */
    public void writeDataElement(String elemID) throws Exception {
        
        if (Util.voidStr(elemID))
            throw new Exception("Data element ID not specified!");
        
        if (searchEngine == null)
            createSearchEngine();
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E"));
        
        writeDataElement(elem);
    }
    
    /**
    *   Write data element factsheet by data element object
    */
    private void writeDataElement(DataElement elem) throws Exception {
        
        String tableID = elem.getTableID();
        if (!Util.voidStr(tableID))
            writeDataElementWarning();
        
        doc.add(new Paragraph("Basic metadata:\n"));
        
        // write simple attributes
        
        Hashtable hash = null;
        Vector v = elem.getAttributes();
        
        // dataset name, table name
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
        v.add(0, hash);
        
        writeSimpleAttributesTable(v);
        
        doc.add(new Paragraph("\n"));
        
        // write complex attributes, one table for each
        v = elem.getComplexAttributes();
        for (int i=0; v!=null && i<v.size(); i++){
            DElemAttribute attr = (DElemAttribute)v.get(i);
            attr.setFields(searchEngine.getAttrFields(attr.getID()));
            writeComplexAttributeTable(attr);
            doc.add(new Phrase("\n"));
        }
        
        // write allowable values (for a factsheet levelling not needed I guess)
        v = searchEngine.getAllFixedValues(elem.getID(), "elem");
        writeFixedValues(v);
        
        // write aggregate structure
    }
    
    /**
    *   Write the table of identifier fields and simple attributes
    */
    private void writeSimpleAttributesTable(Vector attrs) throws Exception {
        
        if (doc == null)
            throw new Exception("Document not created!");
            
        // set up the table
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        int headerwidths[] = {25, 75}; // percentage
        table.setWidths(headerwidths);
        table.setWidthPercentage(100); // percentage
        
        // set up the fonts
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        
        // start adding rows and cells
        int rowCount = 0;
        PdfPCell nameCell = null;
        PdfPCell valueCell = null;
        
        for (int i=0; attrs!=null && i<attrs.size(); i++){
            
            String name = null;
            String value = null;
            
            Object o = attrs.get(i);
            if (o.getClass().getName().endsWith("Hashtable")){
                name = (String)((Hashtable)o).get("name");
                value = (String)((Hashtable)o).get("value");
            }
            else if (o.getClass().getName().endsWith("DElemAttribute")){
                name = ((DElemAttribute)o).getShortName();
                value = ((DElemAttribute)o).getValue();
            }
            
            if (Util.voidStr(value) || Util.voidStr(name))
                continue;
                
            nameCell = new PdfPCell(new Phrase(name, nameFont));
            valueCell = new PdfPCell(new Phrase(value, valueFont));
            
            nameCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            nameCell.setPaddingRight(5);
            nameCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setBorder(Rectangle.NO_BORDER);
            
            if (rowCount % 2 != 1){
                nameCell.setGrayFill(0.9f);
                valueCell.setGrayFill(0.9f);
            }
                
            table.addCell(nameCell);
            table.addCell(valueCell);
            
            rowCount++;
        }
        
        if (table.size() != 0)            
            doc.add(table);
    }
    
    /**
    *   Write the table of complex attributes
    */
    private void writeComplexAttributeTable(DElemAttribute attr) throws Exception {
        
        if (doc == null)
            throw new Exception("Document not created!");
        
        // get the attribute fields
        Vector fields = attr.getFields();
        if (fields==null || fields.size()==0)
            return;
        
        int fieldCount = fields.size();
        
        // set up the table
        PdfPTable table = new PdfPTable(fieldCount);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // set the column widths
        float headerwidths[] = new float[fieldCount];
        for (int i=0; i<fieldCount; i++){
            headerwidths[i] = 100 / fieldCount; // percentage
        }
        
        table.setWidths(headerwidths);
        table.setWidthPercentage(100); // percentage
        
        // prepare fonts
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font captionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        headerFont.setColor(new Color(255,255,255));
        
        // start adding rows and cells
        int rowCount = 0;
        
        // add caption row
        PdfPCell cell =
            new PdfPCell(new Phrase(attr.getName()==null ?
                                    attr.getShortName() :
                                    attr.getName(),
                                    captionFont));                                    
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setColspan(fieldCount);
        cell.setBorder(Rectangle.NO_BORDER);
        
        table.addCell(cell);
        
        // add header row
        for (int i=0; i<fields.size(); i++){
            
            Hashtable field = (Hashtable)fields.get(i);
			String fieldName = (String)field.get("name");
			
            cell = new PdfPCell(new Phrase(fieldName, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setGrayFill(0.4f);
            
            table.addCell(cell);
        }
        
        // add the attribute value rows
        Vector valueRows = attr.getRows();
        for (int i=0; valueRows!=null && i<valueRows.size(); i++){
            
            Hashtable rowHash = (Hashtable)valueRows.get(i);
            
            for (int t=0; t<fieldCount; t++){
                Hashtable fieldHash = (Hashtable)fields.get(t);
				String fieldID = (String)fieldHash.get("id");
				String fieldValue = fieldID==null ? " " : (String)rowHash.get(fieldID);
				
				cell = new PdfPCell(new Phrase(fieldValue, valueFont));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setPaddingLeft(5);
                cell.setBorder(Rectangle.NO_BORDER);
                
                if (i % 2 == 1)
                    cell.setGrayFill(0.9f);
                    
                table.addCell(cell);
            }
        }
        
        if (table.size() != 0)
            doc.add(table);
    }
    
    /**
    *   Write the list of tables in a dataset
    */
    private void writeTablesList(Vector tables) throws Exception {
        
        if (doc == null)
            throw new Exception("Document not created!");
        
        // get the attribute fields
        if (tables==null || tables.size()==0)
            return;
        
        // set up the table
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // set the column widths
        float headerwidths[] = {40, 50, 10}; // percentage
        table.setWidths(headerwidths);
        table.setWidthPercentage(100); // percentage
        
        // prepare fonts
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        headerFont.setColor(new Color(255,255,255));
        
        // start adding rows and cells
        int rowCount = 0;
        
        // add header row
        
        // short name
        PdfPCell cell = new PdfPCell(new Phrase("Short name", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // full name
        cell = new PdfPCell(new Phrase("Full name", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // type
        cell = new PdfPCell(new Phrase("Type", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // add the the rest of rows
        for (int i=0; tables!=null && i<tables.size(); i++){
            
            DsTable dsTable = (DsTable)tables.get(i);
            
            // add table short name
            cell = new PdfPCell(new Phrase(dsTable.getShortName(), valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
                    
            table.addCell(cell);
            
            // add table name
            String name = dsTable.getName()==null ? " " : dsTable.getName();
            cell = new PdfPCell(new Phrase(name, valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
                    
            table.addCell(cell);
            
            // add table type
            String type = dsTable.getType()==null ? " " : dsTable.getType();
            cell = new PdfPCell(new Phrase(type, valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
                    
            table.addCell(cell);
        }
        
        if (table.size() != 0)
            doc.add(table);
    }
    private void writeTableTable(DsTable dsTable) throws Exception {
        writeTableTable(dsTable, false);
    }
    
    private void writeTableTable(DsTable dsTable, boolean tableFactsheet) throws Exception {
        
        if (doc == null)
            throw new Exception("Document not created!");
            
        if (dsTable==null)
            throw new Exception("Table object was null!");
        
        if (searchEngine == null)
            createSearchEngine();
        
        // get the data elements in this table
        Vector tblElems = searchEngine.getDataElements(null, null, null, null, dsTable.getID());
        if (tblElems == null || tblElems.size()==0)
            return;
        dsTable.setElements(tblElems);
        
        // set up the PDF table
        PdfPTable table = new PdfPTable(3);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // set the column widths
        float headerwidths[] = {20, 30, 50}; // percentage
        table.setWidths(headerwidths);
        table.setWidthPercentage(100); // percentage
        
        // prepare fonts
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font captionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        headerFont.setColor(new Color(255,255,255));
        
        // start adding rows and cells
        int rowCount = 0;
        PdfPCell cell = null;
        
        if (tableFactsheet){
            // add caption row
            cell = new PdfPCell(new Phrase("Elements in this table:", captionFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setColspan(3);
            cell.setBorder(Rectangle.NO_BORDER);
            
            table.addCell(cell);
        }
        else{
            // add caption rows (1st for Short name, 2nd for Full name)
            cell = new PdfPCell(new Phrase("Table - " + dsTable.getShortName(), captionFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setColspan(3);
            cell.setBorder(Rectangle.NO_BORDER);
            
            table.addCell(cell);
            
            String fullName = dsTable.getName();
            if (!Util.voidStr(fullName)){
                
                cell = new PdfPCell(new Phrase(fullName, captionFont));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setPaddingLeft(5);
                cell.setColspan(3);
                cell.setBorder(Rectangle.NO_BORDER);
                    
                table.addCell(cell);
            }
        }
        
        // add header row
        
        // element name
        cell = new PdfPCell(new Phrase("Element name", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // data type
        cell = new PdfPCell(new Phrase("Element type", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // definition
        cell = new PdfPCell(new Phrase("Element definition", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // add value rows
        for (int i=0; tblElems!=null && i<tblElems.size(); i++){
            
            DataElement elem = (DataElement)tblElems.get(i);
            
            String elemType = elem.getType();
            if (elemType.equals("AGG"))
                continue;
            
            // add short name
            cell = new PdfPCell(new Phrase(elem.getShortName(), valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            
            if (i == tblElems.size()-1)
                cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
            else
                cell.setBorder(Rectangle.LEFT);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
            
            table.addCell(cell);
            
            // add data type
            
            String datatype = elemType.equals("CH1") ?
                                "Code list" :
                                elem.getAttributeValueByShortName("Datatype");
            
            if (Util.voidStr(datatype))
                datatype = " ";
            else if (elemType.equals("CH1")){
                // list 4 first codes for example (from the highest level)
                // if no codes found, let the datatype stay simply 'Code list'
                StringBuffer buf = new StringBuffer(datatype);
                Vector fixedValues = searchEngine.getFixedValues(elem.getID(), "elem");
                if (fixedValues!=null && fixedValues.size()!=0){
                    buf.append(":");
                    int k = 0;
                    for (k=0; k<fixedValues.size() && k<4; k++){
                        FixedValue fxv = (FixedValue)fixedValues.get(k);
		                String value = fxv.getValue();
                        buf.append("\n" + value);
                    }
                    
                    if (k<fixedValues.size())
                        buf.append("\n...");
                }
                
                datatype = buf.toString();
            }
            
            cell = new PdfPCell(new Phrase(datatype, valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            
            if (i == tblElems.size()-1)
                cell.setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
            else
                cell.setBorder(Rectangle.LEFT);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
            
            table.addCell(cell);
            
            // add definition
            String definition = elem.getAttributeValueByShortName("Definition");
            if (Util.voidStr(definition)) definition = " ";
            cell = new PdfPCell(new Phrase(definition, valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            
            if (i == tblElems.size()-1)
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
            else
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
            
            table.addCell(cell);
        }
        
        if (table.size() != 0)
            doc.add(table);
    }
    
    private void writeFixedValues(Vector fxValues) throws Exception {
        writeFixedValues(fxValues, NON_LEVELLED);
    }
    
    private void writeFixedValues(Vector fxValues, int levelled) throws Exception {
        
        if (fxValues==null || fxValues.size()==0)
            return;
        
        // set up the PDF table
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // set the column widths
        float headerwidths[] = {30, 70}; // percentage
        table.setWidths(headerwidths);
        table.setWidthPercentage(100); // percentage
        
        // prepare fonts
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font captionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        headerFont.setColor(new Color(255,255,255));
        
        // start adding rows and cells
        
        PdfPCell cell = null;
        
        // add caption row
        cell = new PdfPCell(new Phrase("Allowable values:", captionFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setColspan(3);
        cell.setBorder(Rectangle.NO_BORDER);
            
        table.addCell(cell);
        
        // add header row
        
            // value
        cell = new PdfPCell(new Phrase("Value", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
            // definition
        cell = new PdfPCell(new Phrase("Definition", headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(5);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setGrayFill(0.4f);
        
        table.addCell(cell);
        
        // add value rows
        for (int i=0; i<fxValues.size(); i++){
            
            FixedValue fxv = (FixedValue)fxValues.get(i);
            
            // value
            cell = new PdfPCell(new Phrase(fxv.getValue(), valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
            
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
            
            table.addCell(cell);
            
            // definition
            String definition = fxv.getAttributeValueByShortName("Definition");
            if (Util.voidStr(definition))
                definition = " ";
            cell = new PdfPCell(new Phrase(definition, valueFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPaddingLeft(5);
            cell.setBorder(Rectangle.NO_BORDER);
            
            if (i % 2 == 1)
                cell.setGrayFill(0.9f);
            
            table.addCell(cell);
        }
        
        if (table.size() != 0)
            doc.add(table);
    }
    
    private void writeDataElementWarning() throws Exception {
        
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        font.setColor(255,0,0);
        
        String msg =
        "\nWarning! This factsheet does not fully reflect the table and dataset " +
        "where this data element belongs to!\n";
        
        doc.add(new Phrase(msg, font));
    }
    
    private void addImage(String filePath, String webPath){
        
        if (filePath==null || filePath.length()==0)
            return;
        
        try {
            Image img = Image.getInstance(filePath);
            img.setAlignment(Image.LEFT);
            float width = img.scaledWidth();
            if (width > MAX_IMG_LENGTH)
                img.scaleAbsoluteWidth(MAX_IMG_LENGTH);
            doc.add(new Phrase("\n\nThis is the dataset's visual structure:\n"));
            doc.add(img);
        }
        catch (Exception e){
            
            // Check if there was an IOException. If so, it very
            // probably means that the file was not an image. Because
            // this method assumes that the file given by filePath is
            // exsiting!
            
            if (e.getClass().getName().endsWith("IOException")){
                
                // Since we now assume that the file was not an image,
                // we nevertheless tell the user where the file can be seen.
                StringBuffer buf = new StringBuffer("\n\n");
                buf.append("Visual structure of this dataset ");
                buf.append("can be downloaded from its detailed view ");
                buf.append("in the Data Dictionary website.");
                    
                try{
                    doc.add(new Phrase(buf.toString()));
                }
                catch (Exception ee) {}
            }
        }
    }
    
    private void createSearchEngine(){
        searchEngine = new DDSearchEngine(conn);
    }
    
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            PdfGenerator pdfGen = new PdfGenerator("x:\\projects\\datadict\\tmp\\test.pdf", conn);
            
            java.util.Date date = new java.util.Date();
            StringBuffer buf = new StringBuffer();
            buf.append(date.getDate());
            buf.append("/");
            buf.append(date.getMonth() + 1);
            buf.append("/");
            buf.append(1900 + date.getYear());
            
            /*String headerText =
                "European Environment Agency,\tdataset factsheet,\tcreated " +
                buf.toString();*/
            String headerText =
                "European Environment Agency,\tdataset table factsheet,\tcreated " +
                buf.toString();
                
            //pdfGen.setDocumentHeader(headerText);
            pdfGen.openDoc();
            pdfGen.setImagesPaths("x:\\projects\\datadict\\visuals", null);
            //pdfGen.writeDataset("621");
            //pdfGen.writeDsTable("644");
            //pdfGen.writeDataElement("4288");
            pdfGen.writeSimple();
            pdfGen.closeDoc();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}