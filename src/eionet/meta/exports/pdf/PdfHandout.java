
package eionet.meta.exports.pdf;

import eionet.meta.*;
import eionet.util.Util;

import java.util.*;
import java.io.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public abstract class PdfHandout implements PdfHandoutIF {
    
    protected DDSearchEngine searchEngine = null;
    protected OutputStream os = null;
    
    private Vector docElements = new Vector();
    
    private HeaderFooter header = null;
    private HeaderFooter footer = null;
    
    private Vector pageBreaks = new Vector();
    
    protected String logo = null;
    
    public abstract void write(String objID) throws Exception;
    
    public void flush() throws Exception {
        
        Document document  = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, os);
        
        if (titlePageNeeded()){
            // title page first, without header/footer
            document.open();
            addTitlePage(document);
        }

        // header and footer
        if (header != null)
            document.setHeader(header);
        
        footer = new HeaderFooter(new Phrase("", Fonts.get(Fonts.DOC_HEADER)), true);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setBorder(com.lowagie.text.Rectangle.TOP);
        document.setFooter(footer);
        
        if (!document.isOpen())
            document.open();
        
        // add elements to the document
        for (int i=0; docElements!=null && i<docElements.size(); i++){

            Element elm = (Element)docElements.get(i);
            
            if (pageBreaks.contains(new Integer(i)))
                document.newPage();
            else if (keepOnOnePage(i)){
                if (elm.getClass().getName().endsWith(".PdfPTable")){
                    if (!writer.fitsPage((PdfPTable)elm)){
                        document.newPage();
                    }
                }
            }
            
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
}