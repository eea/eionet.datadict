
package eionet.meta.exports.pdf;

import java.util.Hashtable;
import java.util.Vector;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.meta.DataElement;
import eionet.meta.savers.Parameters;
import eionet.util.Util;

public class ElmPdfGuideline {

    private DDSearchEngine searchEngine = null;
    //private Section parentSection = null;
    //private Section section = null;

    //private Vector docElements = new Vector();

    private String vsPath = null;

    private Parameters params = null;

    private TblPdfGuideline owner = null;

    // methods
    ///////////

    public ElmPdfGuideline(DDSearchEngine searchEngine, TblPdfGuideline owner) //Section parentSection)
    throws Exception {

        //if (parentSection == null) throw new Exception("parentSection cannot be null!");
        if (searchEngine == null) {
            throw new Exception("searchEngine cannot be null!");
        }

        this.searchEngine = searchEngine;
        //this.parentSection = parentSection;
        this.owner = owner;
    }

    public void write(String elemID) throws Exception {
        write(elemID, null);
    }

    protected void write(String elemID, String tblID) throws Exception {

        if (Util.isEmpty(elemID)) {
            throw new Exception("Data element ID not specified!");
        }

        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elemID, tblID, false);
        if (elem == null) {
            throw new Exception("Data element not found!");
        }

        // get and set the element's complex attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E"));

        write(elem);
    }

    /**
     * Write a factsheet for a data element given by object.
     */
    private void write(DataElement elem) throws Exception {

        if (elem == null) {
            throw new Exception("Element object was null!");
        }

        String nr = "";
        Sectioning sect = null;
        if (owner != null) {
            sect = owner.getSectioning();
        }
        if (sect != null) {
            nr = sect.level(elem.getShortName() + " element", 3);
        }
        nr = nr == null ? "" : nr + " ";

        Paragraph prg = new Paragraph();
        prg.add(new Chunk(nr + elem.getShortName(), Fonts.getUnicode(12, Font.BOLDITALIC)));
        prg.add(new Chunk(" data element", Fonts.getUnicode(12, Font.BOLD)));

        //section = parentSection.addSection(prg, 3);
        addElement(prg);

        // see if this guideline is part of a table, get the
        // latter's information.

        String tableID = elem.getTableID();
        if (Util.isEmpty(tableID)) {

            String msg =
                "\nWarning! This guideline does not fully reflect the " +
                "table and dataset where this data element belongs to!\n\n";

            addElement(new Phrase(msg, Fonts.get(Fonts.WARNING)));
        }

        // write simple attributes
        addElement(new Paragraph("\n"));

        Hashtable hash = null;
        Vector attrs = elem.getAttributes();

        // short name
        hash = new Hashtable();
        hash.put("name", "Short name");
        hash.put("value", elem.getShortName());
        attrs.add(0, hash);

        addElement(PdfUtil.simpleAttributesTable(attrs));
        addElement(new Phrase("\n"));

        // write foreign key reltaions if any exist
        String dstID = params == null ? null : params.getParameter("dstID");
        Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
        if (fks != null && fks.size()>0) {
            addElement(PdfUtil.foreignKeys(fks));
            addElement(new Phrase("\n"));
        }

        // write complex attributes, one table for each
        Vector v = elem.getComplexAttributes();
        if (v != null && v.size()>0) {

            DElemAttribute attr = null;
            for (int i = 0; i < v.size(); i++) {
                attr = (DElemAttribute) v.get(i);
                attr.setFields(searchEngine.getAttrFields(attr.getID()));
            }

            for (int i = 0; i < v.size(); i++) {

                addElement(PdfUtil.complexAttributeTable((DElemAttribute) v.get(i)));
                addElement(new Phrase("\n"));
            }
        }

        // write allowable values (for a factsheet levelling not needed I guess)
        v = searchEngine.getFixedValues(elem.getID(), "elem");
        if (v != null && v.size()>0) {
            addElement(new Phrase("! This data element may only have the " +
                    "following fixed values:\n", Fonts.get(Fonts.HEADING_0)));
            addElement(PdfUtil.fixedValuesTable(v, false));
        }

        /*/ write image attributes
        Element imgAttrs = PdfUtil.imgAttributes(attrs, vsPath);
        if (imgAttrs != null) {
            addElement(new Phrase("\n"));
            addElement(imgAttrs);
        }*/
    }

    private void addElement(Element elm) {

        if (owner != null) {
            owner.addElement(elm);
        }

        //if (elm != null) section.add(elm);
        //return docElements.size();
    }

    public void setVsPath(String vsPath) {
        this.vsPath = vsPath;
    }

    public void setParameters(Parameters params) {
        this.params = params;
    }
}
