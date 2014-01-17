package eionet.meta.exports.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.sql.Connection;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import eionet.meta.Dataset;
import eionet.meta.savers.Parameters;
import eionet.util.Util;

/**
 * PDF guidelines generator for more than a dataset.
 */
// TODO there can be some refactorings for all pdf generation package, maybe more inheritance rather than owner or using owner more
// genericly
public class DstCombinedPdfGuideline extends DstPdfGuideline {
   
    /**
     * Constructor.
     * 
     * @param conn
     * @param os
     */
    public DstCombinedPdfGuideline(Connection conn, OutputStream os) {
        super(conn, os);
        this.levelOffset = 1;
        super.levelOffset = 1;
    }

    @Override
    public void write(String objIDs) throws Exception {
        if (Util.isEmpty(objIDs)) {
            throw new Exception("No Dataset ID specified");
        }

        String[] datasetIds = objIDs.split("[:]");
        if (Util.isEmpty(datasetIds[0])) {
            throw new Exception("No Dataset ID specified");
        }

        StringBuffer filenames = new StringBuffer();
        for (String dstId : datasetIds) {
            if (!Util.isEmpty(dstId)) {
                //add a title
                Dataset ds = searchEngine.getDataset(dstId);
                String dsName = ds.getShortName();                
                String nr = super.sect.level(dsName, 1);
                nr = nr == null ? "" : nr + " ";
                String localAddress = PdfHandout.getLocalDestinationAddressFor(nr + dsName);
                Paragraph prg = new Paragraph();
                prg.add(new Chunk(nr + dsName, Fonts.getUnicode(16, Font.BOLD)).setLocalDestination(localAddress));
                addElement(prg);

                super.write(dstId, false);
                filenames.append(ds.getIdentifier());
                filenames.append("-");
            }
        }
        filenames.deleteCharAt(filenames.length() - 1);
        filenames.append(DstPdfGuideline.FILE_EXT);
        this.fileName = filenames.toString();

        setHeader("");
        setFooter();
    }// end of method write

    @Override
    public void flush() throws Exception {
        super.flush();
    }

    @Override
    public void setLogo(String logo) {
    }

    @Override
    public void setVisualsPath(String visualsPath) {
    }

    @Override
    public void setParameters(Parameters params) {
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
    
    @Override
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

    @Override
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

}// end of class DstCombinedPdfGuideline
