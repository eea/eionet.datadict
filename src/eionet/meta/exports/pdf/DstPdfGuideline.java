package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.awt.Color;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class DstPdfGuideline extends PdfHandout {
	
    private int vsTableIndex = -1;
    private int elmCount = 0;
    
    private String dsName = "";
	private String dsVersion = "";
	private Hashtable tblElms = new Hashtable();
	private Hashtable tblNames = new Hashtable();
	private Hashtable submitOrg = new Hashtable();
	private Hashtable respOrg = new Hashtable();
	private boolean hasGisTables = false;
	
//	private Chapter chapter = null;
    
    public DstPdfGuideline(Connection conn, OutputStream os){
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
		setShowedAttributes();
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
        v = searchEngine.getDatasetTables(dsID, true);
        DsTable tbl = null;
        for (int i=0; v!=null && i<v.size(); i++){
        	tbl = (DsTable)v.get(i);
        	if (searchEngine.hasGIS(tbl.getID())){
        		tbl.setGIS(true);
        		this.hasGisTables = true;
        	}
        	tbl.setSimpleAttributes(
        			searchEngine.getSimpleAttributes(tbl.getID(), "T"));
        }
        ds.setTables(v);
        
        addParameter("dstID", dsID);
        
        write(ds);
    }
    
    private void write(Dataset ds) throws Exception {
        
        if (ds == null)
            throw new Exception("Dataset object is null!");
        
        String s = ds.getAttributeValueByShortName("Name");
        dsName = Util.voidStr(s) ? ds.getShortName() : s;
        
		dsVersion = ds.getAttributeValueByShortName("Version");
        
        String title = "General information for " + dsName + " dataset";
		String nr = sect.level(title, 1);
		nr = nr==null ? "" : nr + " ";
		        
        Paragraph prg = new Paragraph();
		prg.add(new Chunk(nr + "General information for ",
					FontFactory.getFont(FontFactory.HELVETICA, 16)));
		prg.add(new Chunk(dsName,
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        prg.add(new Chunk(" dataset",
        			FontFactory.getFont(FontFactory.HELVETICA, 16)));
        
        //chapter = new Chapter(prg, 1);
        
        // add the dataset chapter to the document
        // elmCount = super.addElement(chapter);
		elmCount = addElement(prg);
        
        addElement(new Paragraph("\n"));
        
        // set up complex attributes to retreive SubmitOrg, RespOrg and Contact information
		Vector cattrs = ds.getComplexAttributes();
		if (cattrs!=null && cattrs.size()>0){
			DElemAttribute attr = null;
			for (int i=0; i<cattrs.size(); i++){
				attr = (DElemAttribute)cattrs.get(i);
				attr.setFields(searchEngine.getAttrFields(attr.getID()));
			}
		}

		this.submitOrg = ds.getCAttrByShortName("SubmitOrganisation");
		this.respOrg   = ds.getCAttrByShortName("RespOrganisation");
        
        // write simple attributes
        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));
        
        Vector attrs = ds.getSimpleAttributes();

        Hashtable hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", ds.getShortName());
		attrs.add(hash);
		
        if (!Util.voidStr(dsVersion)){
            hash = new Hashtable();
            hash.put("name", "Version");
            hash.put("value", dsVersion);
			attrs.add(hash);
        }
        
        String contactInfo = getContactInfo();
        if (contactInfo!=null){
			hash = new Hashtable();
			hash.put("name", "Contact information");
			hash.put("value", contactInfo);
			attrs.add(hash);
        }

        addElement(PdfUtil.simpleAttributesTable(attrs, showedAttrs));
        addElement(new Phrase("\n"));
        
		/* write image attributes
		Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath);
		if (imgAttrs!=null){
			addElement(new Phrase("\n"));
			addElement(imgAttrs);
		}*/
        
        // write tables list
        title = "Overview of " + dsName + " dataset tables";
		nr = sect.level(title, 1);
		nr = nr==null ? "" : nr + " ";
		prg = new Paragraph();
		prg.add(new Chunk(nr,
					 FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
		prg.add(new Chunk("Overview of ",
					 FontFactory.getFont(FontFactory.HELVETICA, 16)));
		prg.add(new Chunk(dsName,
					 FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
		prg.add(new Chunk(" dataset tables",
					 FontFactory.getFont(FontFactory.HELVETICA, 16)));
		addElement(prg);
        //addElement(new Phrase("Tables in this dataset:\n", Fonts.get(Fonts.HEADING_0)));
        
        Vector tables = ds.getTables();
        addElement(PdfUtil.tablesList(tables));
        addElement(new Phrase("\n"));
        
        // add dataset visual structure image
        if (ds.getVisual() != null){
            String fullPath = vsPath + ds.getVisual();
            File file = new File(fullPath);
            if (file.exists()){
                
                try {
                    PdfPTable table = PdfUtil.vsTable(fullPath, "Datamodel for this dataset");
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
        
		pageToLandscape();
		if (tables!=null && tables.size()>0){
			title = "Tables";
			nr = sect.level(title, 1);
			nr = nr==null ? "" : nr + " ";
			prg = new Paragraph(nr + title, Fonts.get(Fonts.HEADING_1));
			addElement(prg);
		}
		
		Vector gisTables = new Vector();
		if (this.hasGisTables){
			this.sect.setRefCodelists("5");
			this.sect.setRefIllustrations("6");
		}
        
        // add full guidlines of tables
        for (int i=0; tables!=null && i<tables.size(); i++){
            DsTable dsTable = (DsTable)tables.get(i);
            
            if (dsTable.hasGIS()){
            	gisTables.add(dsTable);
            	continue;
            }
            
            // the tables guidelines will be added to the current chapter
            addElement(new Paragraph("\n"));
            TblPdfGuideline tblGuideln =
            	new TblPdfGuideline(searchEngine, this);//, (Section)chapter);
			tblGuideln.setVsPath(vsPath);
            tblGuideln.write(dsTable.getID(), ds.getID());
            insertPageBreak();
        }
        
        if (gisTables.size()>0){
			title = "GIS tables";
			nr = sect.level(title, 1);
			nr = nr==null ? "" : nr + " ";
			prg = new Paragraph(nr + title, Fonts.get(Fonts.HEADING_1));
			addElement(prg);
        }
        
		for (int i=0; i<gisTables.size(); i++){
			DsTable dsTable = (DsTable)gisTables.get(i);
			// the tables guidelines will be added to the current chapter
			addElement(new Paragraph("\n"));
			TblPdfGuideline tblGuideln =
				new TblPdfGuideline(searchEngine, this);//, (Section)chapter);
			tblGuideln.setGIS(true);
			tblGuideln.setVsPath(vsPath);
			tblGuideln.write(dsTable.getID(), ds.getID());
			insertPageBreak();
		}
        
		pageToPortrait();
        
        // add codelists
        addCodelists(tables);
		insertPageBreak();
        
        // add img attrs
        addImgAttrs(tables);
        
        // set header & footer
        setHeader("");
		setFooter();
    }
    
    private void addCodelists(Vector tables) throws Exception{
    	
		String nr = null;
		Paragraph prg = null;
		String title = null;
		String s = null;
		boolean lv1added = false;
		
		for (int i=0; tables!=null && i<tables.size(); i++){
			boolean lv2added = false;
			DsTable tbl = (DsTable)tables.get(i);
			Vector elms = (Vector)tblElms.get(tbl.getID());
			for (int j=0; elms!=null && j<elms.size(); j++){
				
				DataElement elm = (DataElement)elms.get(j);
				
				PdfPTable codelist = PdfUtil.codelist(elm.getFixedValues());
				if (codelist==null || codelist.size()==0) continue;
				
				// add 'Codelists' title
				if (!lv1added){
					nr = sect.level("Codelists", 1);
					nr = nr==null ? "" : nr + " ";
					prg = new Paragraph(nr +
							"Codelists", Fonts.get(Fonts.HEADING_1));
					addElement(prg);
					addElement(new Paragraph("\n"));
					lv1added = true;
				}
				
				// add table title
				if (!lv2added){
					s = (String)tblNames.get(tbl.getID());
					String tblName = Util.voidStr(s) ? tbl.getShortName() : s;
					title = "Codelists for " + tblName + " table";
					nr = sect.level(title, 2, false);
					nr = nr==null ? "" : nr + " ";
					
					prg = new Paragraph();
					prg.add(new Chunk(nr,
						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
					prg.add(new Chunk("Codelists for ",
						FontFactory.getFont(FontFactory.HELVETICA, 14)));
					prg.add(new Chunk(tblName,
						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
					prg.add(new Chunk(" table",
						FontFactory.getFont(FontFactory.HELVETICA, 14)));
					
					addElement(prg);
					addElement(new Paragraph("\n"));
					lv2added = true;
				}
				
				// add element title
				s = elm.getAttributeValueByShortName("Name");
				String elmName = Util.voidStr(s) ? elm.getShortName() : s;
				title = elmName + " codelist";
				nr = sect.level(title, 3, false);
				nr = nr==null ? "" : nr + " ";
				
				prg = new Paragraph();
				prg.add(new Chunk(nr + elmName,
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
				prg.add(new Chunk(" codelist",
					FontFactory.getFont(FontFactory.HELVETICA, 14)));

				addElement(prg);
				addElement(new Paragraph("\n"));
				
				// add codelist
				addElement(codelist);
				addElement(new Paragraph("\n"));
			}
		}
    }

	private void addImgAttrs(Vector tables) throws Exception{
		
		String nr = null;
		Paragraph prg = null;
		String title = null;
		String s = null;
		boolean lv1added = false;
		
		for (int i=0; tables!=null && i<tables.size(); i++){
			
			boolean lv2added = false;
			DsTable tbl = (DsTable)tables.get(i);
			Vector tblImgVector =
				PdfUtil.imgAttributes(tbl.getSimpleAttributes(), vsPath);
			if (tblImgVector!=null && tblImgVector.size()!=0){
				
				// add level 1 title
				if (!lv1added){
					nr = sect.level("Illustrations", 1);
					nr = nr==null ? "" : nr + " ";
					prg = new Paragraph(nr +
							"Illustrations", Fonts.get(Fonts.HEADING_1));
					addElement(prg);
					addElement(new Paragraph("\n"));
					lv1added = true;
				}
				
				// add level 2 title
				s = (String)tblNames.get(tbl.getID());
				String tblName = Util.voidStr(s) ? tbl.getShortName() : s;
				title = "Illustrations for " + tblName + " table";
				nr = sect.level(title, 2, false);
				nr = nr==null ? "" : nr + " ";

				prg = new Paragraph();
				prg.add(new Chunk(nr,
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
				prg.add(new Chunk("Illustrations for ",
					FontFactory.getFont(FontFactory.HELVETICA, 14)));
				prg.add(new Chunk(tblName,
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
				prg.add(new Chunk(" table",
					FontFactory.getFont(FontFactory.HELVETICA, 14)));

				addElement(prg);
				addElement(new Paragraph("\n"));
				lv2added = true;
				
				// add images vector
				for (int u=0; u<tblImgVector.size(); u++){
					com.lowagie.text.Image img =
						(com.lowagie.text.Image)tblImgVector.get(u); 
					addElement(img);
				}
				
				addElement(new Paragraph("\n"));
			}
			
			Vector elms = (Vector)tblElms.get(tbl.getID());
			for (int j=0; elms!=null && j<elms.size(); j++){
				
				DataElement elm = (DataElement)elms.get(j);
				
				Vector elmImgVector =
					PdfUtil.imgAttributes(elm.getAttributes(), vsPath);
				if (elmImgVector==null || elmImgVector.size()==0) continue; 				
				
				// add 'Images' title
				if (!lv1added){
					nr = sect.level("Illustrations", 1);
					nr = nr==null ? "" : nr + " ";
					prg = new Paragraph(nr +
							"Illustrations", Fonts.get(Fonts.HEADING_1));
					addElement(prg);
					addElement(new Paragraph("\n"));
					lv1added = true;
				}
				
				// add table title
				if (!lv2added){
					s = (String)tblNames.get(tbl.getID());
					String tblName = Util.voidStr(s) ? tbl.getShortName() : s;
					title = "Illustrations for " + tblName + " table";
					nr = sect.level(title, 2, false);
					nr = nr==null ? "" : nr + " ";
					
					prg = new Paragraph();
					prg.add(new Chunk(nr,
						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
					prg.add(new Chunk("Illustrations for ",
						FontFactory.getFont(FontFactory.HELVETICA, 14)));
					prg.add(new Chunk(tblName,
						FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
					prg.add(new Chunk(" table",
						FontFactory.getFont(FontFactory.HELVETICA, 14)));
					
					addElement(prg);
					addElement(new Paragraph("\n"));
					lv2added = true;
				}
				
				// add element title
				s = elm.getAttributeValueByShortName("Name");
				String elmName = Util.voidStr(s) ? elm.getShortName() : s;
				title = elmName + " illustrations";
				nr = sect.level(title, 3, false);
				nr = nr==null ? "" : nr + " ";
				
				prg = new Paragraph();
				prg.add(new Chunk(nr + elmName,
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
				prg.add(new Chunk(" illustrations",
					FontFactory.getFont(FontFactory.HELVETICA, 14)));

				addElement(prg);
				
				// add images
				for (int u=0; u<elmImgVector.size(); u++){
					com.lowagie.text.Image img =
						(com.lowagie.text.Image)elmImgVector.get(u); 
					addElement(img);
					//addElement(imgTable);
					//addElement(new Paragraph("\n"));
				}
			}
		}
	}
    
    /*protected int addElement(Element elm){
        
        if (elm == null || chapter == null)
		if (elm == null)
            return elmCount;
        
        chapter.add(elm);
        elmCount = elmCount + 1;
        return elmCount;
    }*/
    
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
        
		doc.add(new Paragraph("\n\n\n\n"));			
        // data dictionary
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
		Paragraph prg = new Paragraph("Data Dictionary", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n"));
        
        // full definition
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        prg = new Paragraph("Definition of", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
        // dataset name
        font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26);
        prg = new Paragraph(dsName, font);
		prg.setAlignment(Element.ALIGN_CENTER);
		doc.add(prg);
		
		// dataset word
        font = FontFactory.getFont(FontFactory.HELVETICA, 14);
		prg = new Paragraph("dataset", font);
        prg.setAlignment(Element.ALIGN_CENTER);
        doc.add(prg);
        
		doc.add(new Paragraph("\n\n"));
		
		// version
		if (dsVersion!=null){
			prg = new Paragraph();
			prg.add(new Chunk("Version: ", font));
			prg.add(new Chunk(dsVersion, font));
			prg.setAlignment(Element.ALIGN_CENTER);
			doc.add(prg);
		}
		
		// date
		//prg = new Paragraph(getTitlePageDate());
		//prg.setAlignment(Element.ALIGN_CENTER);
		//doc.add(prg);
        
        doc.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n"));
        
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
    
    private String getTitlePageDate(){
    	
    	String[] months = {"January", "February", "March", "April", "May",
    					   "June", "July", "August", "September", "October",
    					   "November", "December"};
    	
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		
		return months[month] + " " + String.valueOf(year);
    }
    
	protected void setHeader(String title) throws Exception {
        
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
		font.setColor(Color.gray);
	
		Paragraph prg = new Paragraph();		
		prg.add(new Chunk("Data Dictionary\n", font));
		prg.setLeading(10*1.2f);
		font = FontFactory.getFont(FontFactory.HELVETICA, 9);
		font.setColor(Color.lightGray);
		prg.add(new Chunk("Dataset specification for " + dsName +
								" * Version " + dsVersion, font));
		prg.add(new Chunk(" * created " + Util.pdfDate(System.currentTimeMillis()), font));

		this.header = new HeaderFooter(prg, false);
		header.setBorder(com.lowagie.text.Rectangle.BOTTOM);
	}

	/**
	 * Default implementation for adding index based on sectioning
	 */
	public Vector getIndexPage() throws Exception{
		
		Vector elems = new Vector();
		
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		Paragraph prg = new Paragraph("About this document", font);
		elems.add(prg);
		elems.add(new Paragraph("\n"));
		
		String about = 
		"This document holds the technical specifications for a dataflow " +
		"based on automatically generated output from the Data Dictionary " +
		"application. The Data Dictionary is a central service for storing " +
		"technical specifications for information requested in reporting " +
		"obligations. The purpose of this document is to support countries " +
		"in reporting good quality data. This document contains detailed " +
		"specifications in a structured format for the data requested in a " +
		"dataflow. Suggestions from users on how to improve the document " +
		"are welcome.";
		
		font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		prg = new Paragraph(about, font);
		elems.add(prg);
		
		if (sect==null)
			return elems;
		
		Vector toc = sect.getTOCformatted("\t\t\t\t");
		if (toc==null || toc.size()==0)
			return elems;
		
		elems.add(new Paragraph("\n\n\n"));
		
		font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		prg = new Paragraph("Index", font);
		elems.add(prg);
		
		elems.add(new Paragraph("\n"));
		
		font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		for (int i=0; i<toc.size(); i++){
			String line = (String)toc.get(i);
			elems.add(new Chunk(line + "\n", font));
		}
		
		return elems;
	}
	
	public void addTblElms(String tblID, Vector elms){
		tblElms.put(tblID, elms);
	}
	
	public void addTblNames(String tblID, String name){
		tblNames.put(tblID, name);
	}

	protected void setFooter() throws Exception {
    	
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
		font.setColor(Color.gray);
		
		Phrase phr = new Phrase();

		String submOrgName = (String)submitOrg.get("name");
		if (!Util.voidStr(submOrgName)){
			font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
			font.setColor(Color.gray);
			phr.add(new Chunk(submOrgName, font));
	
			String submOrgUrl = (String)submitOrg.get("url");
			if (!Util.voidStr(submOrgUrl)){
				font = FontFactory.getFont(FontFactory.HELVETICA, 9);
				font.setColor(Color.lightGray);
				phr.add(new Chunk("  *  " + submOrgUrl, font));
			}
			
			phr.add(new Chunk("     ", font));
			phr.setLeading(10*1.2f);
		}
		else
			submOrgName = "";
		
		if (respOrg!=null){
			String respOrgName = (String)respOrg.get("name");
			if (!Util.voidStr(respOrgName) && !respOrgName.equals(submOrgName)){
				font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
				font.setColor(Color.gray);
				phr.add(new Chunk("\n" + respOrgName, font));
				
				String respOrgUrl = (String)respOrg.get("url");
				if (!Util.voidStr(respOrgUrl)){
					font = FontFactory.getFont(FontFactory.HELVETICA, 9);
					font.setColor(Color.lightGray);
					phr.add(new Chunk("  *  " + respOrgUrl, font));
				} 
			}
		}
		
		phr.add(new Chunk("   ", font));
		
		footer = new HeaderFooter(phr, true);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorder(com.lowagie.text.Rectangle.TOP);
	}
	
	protected void setShowedAttributes(){
		
		showedAttrs.add("Short name");
		showedAttrs.add("Version");
		showedAttrs.add("Definition");
		showedAttrs.add("ShortDescription");
		showedAttrs.add("Contact information");
		showedAttrs.add("PlannedUpdFreq");
		showedAttrs.add("Methodology");
	}
	
	private String getContactInfo(Dataset dst){
		
		if (submitOrg==null) return null;
		
		DElemAttribute attr = dst.getAttributeByShortName("SubmitOrganisation");
		if (attr==null) return null;
		
		Vector flds = attr.getFields();
		if (flds==null || flds.size()==0)
			return getContactInfo();
		
		StringBuffer buf = null;
		for (int i=0; i<flds.size(); i++){
			Hashtable fld = (Hashtable)flds.get(i);
			String fldName  = (String)fld.get("name");
			if (fldName!=null){
				String fldValue = (String)submitOrg.get(fldName);
				if (!Util.voidStr(fldValue)){
					if (buf==null) buf = new StringBuffer();
					if (buf.length()>0) buf.append("\n");
					buf.append(fldValue);
				}
			}
		}
		
		return buf==null ? null : buf.toString();
	}
	
	private String getContactInfo(){
		
		if (submitOrg==null) return null;
		
		Vector flds = (Vector)submitOrg.get("fields");
		if (flds==null || flds.size()==0)
			return null;

		StringBuffer buf = null;
		for (int i=0; i<flds.size(); i++){
			Hashtable fld = (Hashtable)flds.get(i);
			String fldName  = (String)fld.get("name");
			if (fldName!=null){
				String fldValue = (String)submitOrg.get(fldName);
				if (!Util.voidStr(fldValue)){
					if (buf==null) buf = new StringBuffer();
					if (buf.length()>0) buf.append("\n");
					buf.append(fldValue);
				}
			}
		}
	
		return buf==null ? null : buf.toString();
	}
  
    public static void main(String[] args){
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
            //DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
            DriverManager.getConnection(
				"jdbc:mysql://195.250.186.16:3306/dd", "root", "ABr00t");

            String fileName =
				"d:\\projects\\datadict\\tmp\\ds_test_guideline.pdf";
            DstPdfGuideline guideline =
            	new DstPdfGuideline(conn, new FileOutputStream(fileName));
            guideline.setVsPath("x:\\projects\\datadict\\public\\visuals");
            guideline.setLogo(
				"d:\\projects\\datadict\\public\\images\\pdf_logo.png");
            guideline.write("1275");
            guideline.flush();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}