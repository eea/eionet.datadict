package eionet.meta.exports.pdf;

import java.util.Hashtable;
import java.util.Vector;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.savers.Parameters;
import eionet.util.Util;

/**
 * The generator of the PDF guidelines for a table.
 */
public class TblPdfGuideline {

    /**  */
    private DDSearchEngine searchEngine = null;
    /**  */
    private Parameters params = null;
    /**  */
    private DstPdfGuideline owner = null;
    /**  */
    @SuppressWarnings("rawtypes")
    protected Vector showedAttrs = new Vector();

    /**
     * Default constructor.
     * 
     * @param searchEngine
     * @param owner
     */
    public TblPdfGuideline(DDSearchEngine searchEngine, DstPdfGuideline owner) {

        if (searchEngine == null) {
            throw new IllegalArgumentException("Search engine must not be null!");
        }

        this.searchEngine = searchEngine;
        this.owner = owner;
        setShowedAttributes();
    }

    /**
     * @param tblID
     * @throws Exception
     */
    public void write(String tblID) throws Exception {
        write(tblID, null);
    }

    /**
     * @param tblID
     * @param dstID
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    protected void write(String tblID, String dstID) throws Exception {

        if (Util.isEmpty(tblID)) {
            throw new Exception("Table ID not specified");
        }

        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID, dstID);
        if (dsTable == null) {
            throw new Exception("Table not found!");
        }

        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);

        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);

        write(dsTable);
    }

    /**
     * Write a full PDF guideline for the given table.
     * 
     * @param dsTable
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void write(DsTable dsTable) throws Exception {

        if (dsTable == null) {
            throw new Exception("Table object was null!");
        }

        String s = dsTable.getAttributeValueByShortName("Name");
        String tblName = Util.isEmpty(s) ? dsTable.getShortName() : s;

        String titleTail = " table";

        String nr = "";
        if (owner != null) {
            nr = owner.getSectioning().level(tblName + titleTail, owner.getLevelFor(2));
        }
        nr = nr == null ? "" : nr + " ";

        String localAddress = PdfHandout.getLocalDestinationAddressFor(nr + tblName + titleTail);
        Paragraph prg = new Paragraph();
        prg.add(new Chunk(nr + tblName, Fonts.getUnicode(14, Font.BOLD)).setLocalDestination(localAddress));
        if (titleTail.length() > 0) {
            prg.add(new Chunk(titleTail, Fonts.getUnicode(14)));
        }

        // section = parentSection.addSection(prg, 2);

        addElement(prg);

        // write simple attributes

        Hashtable hash = null;
        Vector v = dsTable.getSimpleAttributes();

        // name
        String name = dsTable.getName();
        if (!Util.isEmpty(name)) {
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

        addElement(PdfUtil.simpleAttributesTable(v, showedAttrs));
        addElement(new Phrase("\n"));

        // Write table elements fact-list, but first get fixed values and foreign-key relations.

        v = dsTable.getElements();
        if (v == null || v.size() == 0) {
            return;
        }

        Vector elms = new Vector();

        DataElement elem = null;
        String dstID = params == null ? null : params.getParameter("dstID");
        for (int i = 0; i < v.size(); i++) {
            elem = (DataElement) v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);
            Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
            elem.setFKRelations(fks);
            Vector attrs = searchEngine.getSimpleAttributes(elem.getID(), "E");
            elem.setAttributes(attrs);

            elms.add(elem);
        }

        if (owner != null) {
            owner.addTblElms(dsTable.getID(), v);
            owner.addTblNames(dsTable.getID(), tblName);
        }

        // Write the fact-list.
        prg = new Paragraph();
        prg.add(new Chunk("Columns in ", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        prg.add(new Chunk(tblName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        prg.add(new Chunk(" table:", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        addElement(prg);

        addElement(PdfUtil.tableElements(elms, null, owner.getSectioning(), owner.getLevelFor(3)));
    }

    /**
     * 
     * @param elm
     */
    protected void addElement(Element elm) {

        if (owner != null) {
            owner.addElement(elm);
        }
    }

    /**
     * @param params
     */
    public void setParameters(Parameters params) {
        this.params = params;
    }

    /**
     * @return
     */
    protected Sectioning getSectioning() {
        if (owner != null) {
            return owner.getSectioning();
        } else {
            return null;
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    protected void setShowedAttributes() {

        showedAttrs.add("Short name");
        showedAttrs.add("Definition");
        showedAttrs.add("ShortDescription");
        showedAttrs.add("Methodology");
    }
}
