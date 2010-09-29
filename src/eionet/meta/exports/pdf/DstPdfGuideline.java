package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.meta.exports.*;
import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.awt.Color;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import com.tee.util.SQLGenerator;

public class DstPdfGuideline extends PdfHandout implements CachableIF {
	
	private static final String FILE_EXT = ".pdf";
	
    private int vsTableIndex = -1;
    private int elmCount = 0;
    
    private String dsName = "";
	private String dsVersion = "";
	private Hashtable tblElms = new Hashtable();
	private Hashtable tblNames = new Hashtable();
	private Hashtable submitOrg = new Hashtable();
	private Hashtable respOrg = new Hashtable();
	private boolean hasGisTables = false;
	
	private String cachePath = null;
	private String cacheFileName = null;
	
	private Connection conn = null; // for storing cache entries
	
	//	private Chapter chapter = null;
    
	public DstPdfGuideline(Connection conn){
		this.conn = conn;
		searchEngine = new DDSearchEngine(conn);
		setShowedAttributes();
	}
	
    public DstPdfGuideline(Connection conn, OutputStream os){
    	this(conn);
        this.os = os;
    }
    
	private void cache(String dsID) throws Exception {
		write(dsID, true);
	}
    
	public void write(String dsID) throws Exception {
		write(dsID, false);
	}
    
    private void write(String dsID, boolean caching) throws Exception {
        
        if (Util.voidStr(dsID))
            throw new Exception("Dataset ID not specified");
        
        // See if this output has been cached.
        // If so, write from cache and exit
        if (!caching && isCached(dsID)){
        	writeFromCache();
        	return;
        }
        
        Dataset ds = searchEngine.getDataset(dsID);
        if (ds == null)
            throw new Exception("Dataset not found!");
        
        fileName = ds.getIdentifier() + FILE_EXT;
        
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
		prg.add(new Chunk(nr + "General information for ", Fonts.getUnicode(16)));
		prg.add(new Chunk(dsName, Fonts.getUnicode(16, Font.BOLD)));
        prg.add(new Chunk(" dataset", Fonts.getUnicode(16)));
        
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
		prg.add(new Chunk(nr, Fonts.getUnicode(16, Font.BOLD)));
		prg.add(new Chunk("Overview of ", Fonts.getUnicode(16)));
		prg.add(new Chunk(dsName, Fonts.getUnicode(16, Font.BOLD)));
		prg.add(new Chunk(" dataset tables", Fonts.getUnicode(16)));
		addElement(prg);
        
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
					prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
					prg.add(new Chunk("Codelists for ", Fonts.getUnicode(14)));
					prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
					prg.add(new Chunk(" table", Fonts.getUnicode(14)));
					
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
				prg.add(new Chunk(nr + elmName, Fonts.getUnicode(14, Font.BOLD)));
				prg.add(new Chunk(" codelist", Fonts.getUnicode(14)));

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
				prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
				prg.add(new Chunk("Illustrations for ", Fonts.getUnicode(14)));
				prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
				prg.add(new Chunk(" table", Fonts.getUnicode(14)));

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
					prg.add(new Chunk(nr, Fonts.getUnicode(14, Font.BOLD)));
					prg.add(new Chunk("Illustrations for ", Fonts.getUnicode(14)));
					prg.add(new Chunk(tblName, Fonts.getUnicode(14, Font.BOLD)));
					prg.add(new Chunk(" table", Fonts.getUnicode(14)));
					
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
				prg.add(new Chunk(nr + elmName, Fonts.getUnicode(14, Font.BOLD)));
				prg.add(new Chunk(" illustrations", Fonts.getUnicode(14)));

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

	/*
	 * (non-Javadoc)
	 * @see eionet.meta.exports.pdf.PdfHandout#keepOnOnePage(int)
	 */
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
        font = Fonts.getUnicode(26, Font.BOLD);
        prg = new Paragraph(dsName, font);
		prg.setAlignment(Element.ALIGN_CENTER);
		doc.add(prg);
		
		// dataset word
        font = Fonts.getUnicode(14);
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
		
		font = Fonts.getUnicode(9);
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
		
		Vector toc = sect.getTOCformatted("    ");
		if (toc==null || toc.size()==0)
			return elems;
		
		elems.add(new Paragraph("\n\n\n"));
		
		font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		prg = new Paragraph("Index", font);
		elems.add(prg);
		
		elems.add(new Paragraph("\n"));
		
		font = Fonts.getUnicode(10);
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
			font = Fonts.getUnicode(9, Font.BOLD);
			font.setColor(Color.gray);
			phr.add(new Chunk(submOrgName, font));
	
			String submOrgUrl = (String)submitOrg.get("url");
			if (!Util.voidStr(submOrgUrl)){
				font = Fonts.getUnicode(9);
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
				font = Fonts.getUnicode(9, Font.BOLD);
				font.setColor(Color.gray);
				phr.add(new Chunk("\n" + respOrgName, font));
				
				String respOrgUrl = (String)respOrg.get("url");
				if (!Util.voidStr(respOrgUrl)){
					font = Fonts.getUnicode(9);
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
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#updateCache(java.lang.String)
	 */
	public void updateCache(String id) throws Exception{
		
		cache(id);
		if (cachePath!=null && fileName!=null){
			String fn = cachePath + fileName;
			try{
				os = new FileOutputStream(fn);
				flush();
				os.flush();
				storeCacheEntry(id, fileName, conn);
			}
			catch (Exception e){
				try{
					File file = new File(fn);
					if (file.exists()) file.delete();
				}
				catch (Exception ee){}
			}
			finally{
				if (os != null) os.close();
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#clearCache(java.lang.String)
	 */
	public void clearCache(String id) throws Exception{
		
		String fn = deleteCacheEntry(id, conn);
		File file = new File(cachePath + fn);
		if (file.exists() && file.isFile())
			file.delete();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#setCachePath(java.lang.String)
	 */
	public void setCachePath(String path) throws Exception{
		cachePath = path;
		if (cachePath!=null){
			cachePath.trim();
			if (!cachePath.endsWith(File.separator))
				cachePath = cachePath + File.separator;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see eionet.meta.exports.CachableIF#isCached(java.lang.String)
	 */
	public boolean isCached(String id) throws Exception{
		if (searchEngine == null)
			throw new Exception("DstPdfGuideline.isCached(): missing searchEngine!");
		
		cacheFileName = searchEngine.getCacheFileName(id, "dst", "pdf");
		if (Util.voidStr(cacheFileName)) return false;
		
		// if the file is referenced in CACHE table, but does not actually exist, we say false
		File file = new File(cachePath + cacheFileName);
		if (!file.exists()){
			cacheFileName = null;
			return false;
		}
		
		return true;
	}
	
	/*
	 * Called when the output is present in cache.
	 * Writes the cached document into the output stream.
	 */
	public void writeFromCache() throws Exception{
		
		if (Util.voidStr(cachePath)) throw new Exception("Cache path is missing!");
		if (Util.voidStr(cacheFileName)) throw new Exception("Cache file name is missing!");
		
		fileName = cacheFileName;
		
		String fullName = cachePath + cacheFileName;
		File file = new File(fullName);
		if (!file.exists()) throw new Exception("Cache file <" + fullName + "> does not exist!");

		int i = 0;
		byte[] buf = new byte[1024];		
		FileInputStream in = null;
		try{
			in = new FileInputStream(file);
			while ((i=in.read(buf, 0, buf.length)) != -1)
				os.write(buf, 0, i);
		}
		finally{
			if (in!=null){
				in.close();
			}
		}
	}
	
	/*
	 * Overriding flush() to check if content has been written from cache
	 */
	public void flush() throws Exception {
		if (cacheFileName!=null)
			os.flush();
		else
			super.flush();
	}
	
	/**
	 * 
	 * @param id
	 * @param fn
	 * @throws SQLException
	 */
	protected static int storeCacheEntry(String id, String fn, Connection conn) throws SQLException{
		
		if (id==null || fn==null || conn==null)
			return -1;

		INParameters inParams = new INParameters();
		
		PreparedStatement stmt = null;
		try{
			// first delete the old entry
			StringBuffer buf = new StringBuffer().
			append("delete from CACHE where OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));
			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
			stmt.executeUpdate();
			stmt.close();

			// now create the new entry
			inParams = new INParameters();
			LinkedHashMap map = new LinkedHashMap();
			map.put("OBJ_ID", inParams.add(id, Types.INTEGER));
			map.put("OBJ_TYPE", SQL.encloseWithApos("dst"));
			map.put("ARTICLE", SQL.encloseWithApos("pdf"));
			map.put("FILENAME", SQL.encloseWithApos(fn));
			map.put("CREATED", inParams.add(String.valueOf(System.currentTimeMillis()), Types.BIGINT));			
			
			stmt = SQL.preparedStatement(SQL.insertStatement("CACHE", map), inParams, conn);
			return stmt.executeUpdate();
		}
		finally{
			try{
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	protected static String deleteCacheEntry(String id, Connection conn) throws SQLException{
		
		if (id==null || conn==null)
			return null;
		
		INParameters inParams = new INParameters();
		StringBuffer buf = new StringBuffer("select FILENAME from CACHE where ").
		append("OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));
		
		String fn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
			rs = stmt.executeQuery();
			if (rs.next()){
				fn = rs.getString(1);
				inParams = new INParameters();
				buf = new StringBuffer("delete from CACHE where ").
				append("OBJ_TYPE='dst' and ARTICLE='pdf' and OBJ_ID=").append(inParams.add(id, Types.INTEGER));
				stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
				stmt.executeUpdate();
			}
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}			
		}
		
		return fn;
	}
}