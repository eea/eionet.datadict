
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;
import eionet.meta.savers.Parameters;

import java.util.*;
import java.io.*;
import java.awt.Color;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public abstract class PdfHandout implements PdfHandoutIF {
	
	public static final int PORTRAIT = 0;
	public static final int LANDSCAPE = 1;
    
    protected DDSearchEngine searchEngine = null;
    protected OutputStream os = null;
    
    private Vector docElements = new Vector();
    
	protected HeaderFooter header = null;
	protected HeaderFooter footer = null;
    
    private Vector pageBreaks = new Vector();
	private Vector rotates = new Vector();
	private Vector landscapes = new Vector();
	private Vector portraits = new Vector();
    
    protected String logo = null;
    
	protected String vsPath = null;
	
	protected Parameters params = null;
	
	protected Sectioning sect = new Sectioning();
	
	protected Vector showedAttrs = new Vector();
	
    /// methods
    ////////////
    
    public abstract void write(String objID) throws Exception;
    
    public void flush() throws Exception {
        
        Document document  = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, os);
        
		//MyPageEvents events = new MyPageEvents();
		//writer.setPageEvent(events);

		// header and footer
		if (header != null)
			document.setHeader(header);
		if (footer != null)
			document.setFooter(footer);
        
        if (titlePageNeeded()){
            // title page first, without header/footer
            document.open();
            addTitlePage(document);
            document.newPage();
        }

        if (!document.isOpen())
            document.open();
        
        // process index page (if any)
        Vector idxPageElems = getIndexPage();
        if (idxPageElems!=null && idxPageElems.size()>0){
        	for (int i=0; i<idxPageElems.size(); i++)
				document.add((Element)idxPageElems.get(i));
			document.newPage();
        }
        
        int tblCounter = 0;
        // add elements to the document
        for (int i=0; docElements!=null && i<docElements.size(); i++){

            Element elm = (Element)docElements.get(i);
			//System.out.println("===> " + elm.toString());            
            
            if (pageBreaks.contains(new Integer(i))){
            	if (landscapes.contains(new Integer(i)))
            		document.setPageSize(PageSize.A4.rotate());
            	else if (portraits.contains(new Integer(i)))
					document.setPageSize(PageSize.A4);
                document.newPage();
            }
            else if (keepOnOnePage(i)){
                if (elm.getClass().getName().endsWith(".PdfPTable")){
                    if (!writer.fitsPage((PdfPTable)elm)){
                        document.newPage();
                    }
                }
            }
            /*else if (elm.getClass().getName().endsWith(".PdfPTable")){
				PdfPTable pdfpTbl = (PdfPTable) elm;
				tblCounter++;
				float pageHeight = document.getPageSize().height();
				boolean fitsPage = writer.fitsPage(pdfpTbl);
				float tableHeight = pdfpTbl.getTotalHeight();
				tableHeight = tableHeight + pdfpTbl.getHeaderHeight();
				if (!writer.fitsPage(pdfpTbl)){
					if (tableHeight <= pageHeight/2)
						document.newPage();
				}
				
            	System.out.println("===> Table#" + tblCounter);
				System.out.println("===> fits=" + fitsPage);
				System.out.println("===> height=" + tableHeight);
				System.out.println("===> page_height=" + pageHeight);
				System.out.println("===>");
            }*/
            
            document.add(elm);
        }
        
        document.close();
    }
    
    protected int addElement(Element elm){
        
        if (elm != null)
            docElements.add(elm);
        
        return docElements.size();
    }
    
    protected void insertPageBreak(){
        pageBreaks.add(new Integer(docElements.size()));
    }
    
	/*protected void rotatePage(){
		rotates.add(new Integer(docElements.size()));
		insertPageBreak();
	}*/
	
	protected void pageToLandscape(){
		landscapes.add(new Integer(docElements.size()));
		insertPageBreak();
	}

	protected void pageToPortrait(){
		portraits.add(new Integer(docElements.size()));
		insertPageBreak();
	}
    
    // default implementation of keepOnOnePage()
    protected boolean keepOnOnePage(int index){
        return false;
    }
    
    protected void setHeader(String title) throws Exception {
        
        StringBuffer buf = new StringBuffer("European Environment Agency");
        
        Phrase phr = new Phrase();
        phr.add(new Chunk(buf.toString(), Fonts.get(Fonts.DOC_HEADER_BLACK)));
        
        if (!Util.voidStr(logo)){
            Image img = Image.getInstance(logo);
            img.setAlignment(Image.LEFT);
                    
            phr.add(new Chunk(img, 0, 0));
        }
        
        buf = new StringBuffer("   ");
        buf.append(title);
        buf.append(", created ");
        buf.append(getCurrentDate());
        
        phr.add(new Chunk(buf.toString(), Fonts.get(Fonts.DOC_HEADER)));
        
        this.header = new HeaderFooter(phr, false);
        header.setBorder(com.lowagie.text.Rectangle.BOTTOM);
    }
    
    protected void setFooter() throws Exception {
    	
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
		font.setColor(Color.gray);
		
		Phrase phr = new Phrase();
		phr.add(new Chunk("European Environment Agency  *  ", font));

		font = FontFactory.getFont(FontFactory.HELVETICA, 9);
		font.setColor(Color.lightGray);
		phr.add(new Chunk("http://www.eea.eu.int   ", font));
		
		/*font = FontFactory.getFont(FontFactory.HELVETICA, 9);
		font.setColor(Color.white);
		phr.add(new
		Chunk("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", font));*/

		footer = new HeaderFooter(phr, true);
		//footer.setAlignment(Element.ALIGN_LEFT);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorder(com.lowagie.text.Rectangle.TOP);
    }
    
    private String getCurrentDate(){
        
        java.util.Date date = new java.util.Date();
        StringBuffer buf = new StringBuffer();
        buf.append(date.getDate());
        buf.append("/");
        buf.append(date.getMonth() + 1);
        buf.append("/");
        buf.append(1900 + date.getYear());
        
        return buf.toString();
    }
    
    /**
    * Default implementation of the method for adding a title page
    */
    protected void addTitlePage(Document document) throws Exception {
    }
    
    /**
    * Default implementation of the method indicating if title page needed
    */
    protected boolean titlePageNeeded(){
        return false;
    }
    
    /**
    * Sets the full path to the EEA logo image
    */
    public void setLogo(String logo){
        this.logo = logo;
    }

	/**
	 * Sets full path to datadict images directory
	 */
	public void setVsPath(String s){
        
		vsPath = s;
        
		if (!Util.voidStr(vsPath)){
			if (!vsPath.endsWith(File.separator))
				vsPath = vsPath + File.separator;
		}
		else
			vsPath = System.getProperty("user.dir") + File.separator;
	}
	
	/**
	 * Sets whatever additional parameters that the handouts might need
	 */
	public void setParameters(Parameters params){
		this.params = params;
	}
	
	/**
	 * 
	 */
	protected String getParameter(String parName){
		if (params!=null)
			return params.getParameter(parName);
		else
			return null;
	}

	/**
	 * 
	 */
	protected void addParameter(String name, String value){
		
		if (params==null)
			params = new Parameters();
		
		params.addParameterValue(name, value);
	}
	
	/*
	 * 
	 */
	protected Sectioning getSectioning(){
		return sect;
	}
	
	/**
	 * Default implementation for adding index based on sectioning
	 */
	public Vector getIndexPage() throws Exception{
		return null;
	}
	
	protected void setShowedAttributes(){
		
		showedAttrs.add("Name");
		showedAttrs.add("ShortDescription");
		showedAttrs.add("Definition");
		showedAttrs.add("Methodology");
		showedAttrs.add("Datatype");
		showedAttrs.add("MinSize");
		showedAttrs.add("MaxSize");
		showedAttrs.add("DecimalPrecision");
		showedAttrs.add("Unit");
		showedAttrs.add("MinValue");
		showedAttrs.add("MaxValue");
		showedAttrs.add("PublicOrInternal");
		showedAttrs.add("PlannedUpdFreq");
		showedAttrs.add("ETCVersion");
		showedAttrs.add("Descriptive_image");
		showedAttrs.add("SubmitOrganisation");
		showedAttrs.add("RespOrganisation");
	}
	
	protected Vector getShowedAttributes(){
		return showedAttrs;
	}
	
	public boolean canShowAttr(String shn){
		return shn!=null && showedAttrs.contains(shn);
	}
}

class MyPageEvents extends PdfPageEventHelper {
    
	// This is the contentbyte object of the writer
	PdfContentByte cb;
    
	// we will put the final number of pages in a template
	PdfTemplate template;
    
	// this is the BaseFont we are going to use for the header / footer
	BaseFont bf = null;
    
	// this is the current act of the play
	String act = "";
    
	// we override the onOpenDocument method
	public void onOpenDocument(PdfWriter writer, Document document) {
		try {
			bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			cb = writer.getDirectContent();
			template = cb.createTemplate(50, 50);
		}
		catch(DocumentException de) {
		}
		catch(IOException ioe) {
		}
	}
    
	// we override the onEndPage method
	public void onEndPage(PdfWriter writer, Document document) {
		int pageN = writer.getPageNumber();
		String text = "Page " + pageN + " of ";
		float len = bf.getWidthPoint(text, 8);
		cb.beginText();
		cb.setFontAndSize(bf, 8);
		cb.setTextMatrix(280, 30);
		cb.showText(text);
		cb.endText();
		cb.addTemplate(template, 280 + len, 30);
	}
    
	// we override the onCloseDocument method
	public void onCloseDocument(PdfWriter writer, Document document) {
		template.beginText();
		template.setFontAndSize(bf, 8);
		template.showText(String.valueOf(writer.getPageNumber() - 1));
		template.endText();
	}
}