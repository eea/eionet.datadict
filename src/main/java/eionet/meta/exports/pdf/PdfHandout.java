package eionet.meta.exports.pdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.ImgRaw;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataDictEntity;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.savers.Parameters;
import eionet.util.Util;
import java.util.Iterator;
import java.util.List;

public abstract class PdfHandout implements PdfHandoutIF {

    private static final String GO_TO_SUFFIX = "_LocalDestination";

    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;

    private static final int YPOS_START = 780;
    private static final int IMG_FIT_RATIO = 4;
    private static final int IMG_FIT_RESERVE = 100;

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

    protected String visualsPath = null;

    protected Parameters params = null;

    protected Sectioning sect = new Sectioning();

    protected Vector showedAttrs = new Vector();

    protected String fileName = "DataDictionary.pdf";

    // / methods
    // //////////

    @Override
    public abstract void write(String objID) throws Exception;

    @Override
    public void flush() throws Exception {

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, os);

        PageOutline pageEvent = new PageOutline();
        writer.setPageEvent(pageEvent);

        // header and footer
        if (header != null) {
            document.setHeader(header);
        }
        if (footer != null) {
            document.setFooter(footer);
        }

        if (titlePageNeeded()) {
            // title page first, without header/footer
            document.open();
            addTitlePage(document);
            document.newPage();
        }

        if (!document.isOpen()) {
            document.open();
        }

        // process index page (if any)
        Vector idxPageElems = getIndexPage();
        if (idxPageElems != null && idxPageElems.size() > 0) {
            for (int i = 0; i < idxPageElems.size(); i++) {
                document.add((Element) idxPageElems.get(i));
            }
            document.newPage();
        }

        int tblCounter = 0;
        float yPos = YPOS_START;
        // add elements to the document
        for (int i = 0; docElements != null && i < docElements.size(); i++) {

            Element elm = (Element) docElements.get(i);

            if (pageBreaks.contains(new Integer(i))) {
                if (landscapes.contains(new Integer(i))) {
                    document.setPageSize(PageSize.A4.rotate());
                } else if (portraits.contains(new Integer(i))) {
                    document.setPageSize(PageSize.A4);
                }
                document.newPage();
            }

            if (elm instanceof ImgRaw) {

                ImgRaw img = (ImgRaw) elm;
                float h = img.getScaledHeight();
                if (yPos < h + IMG_FIT_RESERVE) {
                    if (yPos < (h - h / IMG_FIT_RATIO) + IMG_FIT_RESERVE) {
                        document.newPage();
                    } else {
                        float w = img.getScaledWidth();
                        img.scaleToFit(w, h - h / IMG_FIT_RATIO);
                    }
                }
                elm = img;
            }

            document.add(elm);
            yPos = pageEvent.getPosition();
        }

        document.close();
    }

    /**
     * 
     * @param elm
     * @return
     */
    protected int addElement(Element elm) {

        if (elm != null) {
            docElements.add(elm);
        }

        return docElements.size();
    }

    protected void insertPageBreak() {
        pageBreaks.add(new Integer(docElements.size()));
    }

    /*
     * protected void rotatePage() { rotates.add(new Integer(docElements.size())); insertPageBreak(); }
     */

    protected void pageToLandscape() {
        landscapes.add(new Integer(docElements.size()));
        insertPageBreak();
    }

    protected void pageToPortrait() {
        portraits.add(new Integer(docElements.size()));
        insertPageBreak();
    }

    /*
     *
     */
    protected boolean keepOnOnePage(int index) {
        return false;
    }

    protected void setHeader(String title) throws Exception {

        StringBuffer buf = new StringBuffer("European Environment Agency");

        Phrase phr = new Phrase();
        phr.add(new Chunk(buf.toString(), Fonts.get(Fonts.DOC_HEADER_BLACK)));

        if (!Util.isEmpty(logo)) {
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

        footer = new HeaderFooter(phr, true);
        // footer.setAlignment(Element.ALIGN_LEFT);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setBorder(com.lowagie.text.Rectangle.TOP);
    }

    protected String getCurrentDate() {

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
    protected boolean titlePageNeeded() {
        return false;
    }

    /**
     * Sets the full path to the EEA logo image
     */
    @Override
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * Sets full path to datadict images directory
     */
    @Override
    public void setVisualsPath(String visualsPath) {

        this.visualsPath = visualsPath;

        if (!Util.isEmpty(this.visualsPath)) {
            if (!this.visualsPath.endsWith(File.separator)) {
                this.visualsPath = this.visualsPath + File.separator;
            }
        } else {
            this.visualsPath = System.getProperty("user.dir") + File.separator;
        }
    }

    /**
     * Sets whatever additional parameters that the handouts might need
     */
    @Override
    public void setParameters(Parameters params) {
        this.params = params;
    }

    /**
     *
     */
    protected String getParameter(String parName) {
        if (params != null) {
            return params.getParameter(parName);
        } else {
            return null;
        }
    }

    /**
     *
     */
    protected void addParameter(String name, String value) {

        if (params == null) {
            params = new Parameters();
        }

        params.addParameterValue(name, value);
    }

    /*
     *
     */
    protected Sectioning getSectioning() {
        return sect;
    }

    /**
     * Default implementation for adding index based on sectioning
     */
    public Vector getIndexPage() throws Exception {
        return null;
    }

    protected void setShowedAttributes() {

        showedAttrs.add("Name");
        showedAttrs.add("ShortDescription");
        showedAttrs.add("Definition");
        showedAttrs.add("Datatype");
        showedAttrs.add("MinSize");
        showedAttrs.add("MaxSize");
        showedAttrs.add("DecimalPrecision");
        showedAttrs.add("Unit");
        showedAttrs.add("MinInclusiveValue");
        showedAttrs.add("MaxInclusiveValue");
        showedAttrs.add("MinExclusiveValue");
        showedAttrs.add("MaxExclusiveValue");
        showedAttrs.add("PublicOrInternal");
        showedAttrs.add("PlannedUpdFreq");
        showedAttrs.add("ETCVersion");
        showedAttrs.add("Descriptive_image");
        showedAttrs.add("Methodology");
    }

    protected Vector getShowedAttributes() {
        return showedAttrs;
    }

    public boolean canShowAttr(String shn) {
        return shn != null && showedAttrs.contains(shn);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * utility method to produce address with a given string. address is generated removing whitespaces and adding suffix
     * 
     * @param address
     *            input for address
     * @return produced address
     */
    public static String getLocalDestinationAddressFor(String address) {
        return (address.replaceAll("\\s+", "") + PdfHandout.GO_TO_SUFFIX);
    }// end of static method getLocalDestinationAddressFor

    public void populateVocabularyAttributes(Vector<DElemAttribute> attributes, Integer entityId, DataDictEntity.Entity entityType) 
            throws ResourceNotFoundException, EmptyParameterException {
        populateVocabularyAttributes(attributes, entityId, entityType, false);
    }

    public void populateVocabularyAttributes(Vector<DElemAttribute> attributes, Integer entityId, DataDictEntity.Entity entityType, boolean isCommonElement) 
            throws ResourceNotFoundException, EmptyParameterException {
        for (Iterator<DElemAttribute> it = attributes.iterator(); it.hasNext();) {
            DElemAttribute attribute = it.next();
            String displayType = attribute.getDisplayType();
            if (displayType != null && displayType.equals("vocabulary")) {
                DataDictEntity ddEntity = new DataDictEntity(entityId, entityType);
                List<VocabularyConcept> vocabularyConcepts = searchEngine.getAttributeVocabularyConcepts(Integer.parseInt(attribute.getID()), ddEntity, isCommonElement ? "0" : attribute.getInheritable());
                if (vocabularyConcepts == null || vocabularyConcepts.isEmpty()) {
                    it.remove();
                } else {
                    attribute.setVocabularyBinding(searchEngine.getVocabulary(vocabularyConcepts.get(0).getVocabularyId()));
                    attribute.setVocabularyConcepts(vocabularyConcepts);
                }
            }
        }
    }

}// end of class PdfHandout

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
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        try {
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb = writer.getDirectContent();
            template = cb.createTemplate(50, 50);
        } catch (DocumentException de) {
        } catch (IOException ioe) {
        }
    }

    // we override the onEndPage method
    @Override
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
    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        template.beginText();
        template.setFontAndSize(bf, 8);
        template.showText(String.valueOf(writer.getPageNumber() - 1));
        template.endText();
    }
}

class PageOutline extends PdfPageEventHelper {

    // the paragraph number
    private float pos = 0;

    // we override only the onParagraph method
    @Override
    public void onParagraphEnd(PdfWriter writer, Document document, float position) {
        pos = position;
    }

    public float getPosition() {
        return pos;
    }

}
