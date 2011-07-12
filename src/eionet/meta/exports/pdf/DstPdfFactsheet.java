package eionet.meta.exports.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Util;

public class DstPdfFactsheet extends PdfHandout {

    private int vsTableIndex = -1;

    public DstPdfFactsheet(Connection conn, OutputStream os) {
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
        v = searchEngine.getDatasetTables(dsID, true);
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
        if (!Util.voidStr(version)) {
            hash = new Hashtable();
            hash.put("name", "Version");
            hash.put("value", version);
            attrs.add(1, hash);
        }

        String regStatus = ds.getStatus();
        if (!Util.voidStr(regStatus)) {
            hash = new Hashtable();
            hash.put("name", "Registration status");
            hash.put("value", regStatus);
            attrs.add(2, hash);
        }

        addElement(PdfUtil.simpleAttributesTable(attrs));
        addElement(new Phrase("\n"));

        // write complex attributes, one table for each

        Vector v = ds.getComplexAttributes();
        if (v != null && v.size() > 0) {

            DElemAttribute attr = null;
            for (int i = 0; i < v.size(); i++) {
                attr = (DElemAttribute) v.get(i);
                attr.setFields(searchEngine.getAttrFields(attr.getID()));
            }

            for (int i = 0; i < v.size(); i++) {

                addElement(PdfUtil.complexAttributeTable((DElemAttribute) v.get(i)));
            }
        }

        /*
         * write image attributes Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath); if (imgAttrs!=null){ addElement(new
         * Phrase("\n")); addElement(imgAttrs); }
         */

        // write tables of tables in dataset

        addElement(new Phrase("Tables in this dataset:\n", Fonts.get(Fonts.HEADING_0)));

        Vector tables = ds.getTables();
        for (int i = 0; tables != null && i < tables.size(); i++) {

            // write caption (short name + name)
            DsTable dsTable = (DsTable) tables.get(i);
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
        if (ds.getVisual() != null) {
            String fullPath = visualsPath + ds.getVisual();
            File file = new File(fullPath);
            if (file.exists()) {

                try {
                    PdfPTable table = PdfUtil.vsTable(fullPath, "Dataset visual structure");
                    if (table != null) {
                        // insertPageBreak();
                        int size = addElement(table);
                        vsTableIndex = size - 1;
                    }
                } catch (IOException e) {

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

    /**
     *
     */
    protected boolean keepOnOnePage(int index) {
        if (index == vsTableIndex)
            return true;
        else
            return false;
    }
}
