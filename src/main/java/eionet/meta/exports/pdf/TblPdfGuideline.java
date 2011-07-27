
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

public class TblPdfGuideline {

    private DDSearchEngine searchEngine = null;
    //private Section parentSection = null;
    //private Section section = null;

    //private Vector docElements = new Vector();

    private String vsPath = null;
    private Parameters params = null;
    private DstPdfGuideline owner = null;
    private boolean hasGIS = false;
    protected Vector showedAttrs = new Vector();

    // methods
    /////////////

    public TblPdfGuideline(DDSearchEngine searchEngine, DstPdfGuideline owner)//, Section parentSection)
        throws Exception {

        //if (parentSection==null) throw new Exception("parentSection cannot be null!");

        if (searchEngine==null)
            throw new Exception("searchEngine cannot be null!");

        this.searchEngine = searchEngine;
        //this.parentSection = parentSection;
        this.owner = owner;
        setShowedAttributes();
    }

    public void write(String tblID) throws Exception {
        write(tblID, null);
    }

    protected void write(String tblID, String dstID) throws Exception {

        if (Util.isEmpty(tblID))
            throw new Exception("Table ID not specified");

        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID, dstID);
        if (dsTable == null)
            throw new Exception("Table not found!");

        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);

        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);

        // get the dataset basic info
        /* JH151003 - not needed, cause tbl gdln is always part of a dst gdln
        Dataset ds = null;
        if (!Util.voidStr(dsTable.getDatasetID())) {
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        */

        write(dsTable);
    }

    /**
    * Write a full guideline for a dataset table given by table object.
    */
    private void write(DsTable dsTable) throws Exception {

        if (dsTable==null)
            throw new Exception("Table object was null!");

        String s = dsTable.getAttributeValueByShortName("Name");
        String tblName = Util.isEmpty(s) ? dsTable.getShortName() : s;

        String titleTail = hasGIS ? "" : " table";

        String nr = "";
        if (owner != null)
            nr = owner.getSectioning().level(tblName + titleTail, 2);
        nr = nr==null ? "" : nr + " ";

        Paragraph prg = new Paragraph();
        prg.add(new Chunk(nr + tblName, Fonts.getUnicode(14, Font.BOLD)));
        if (titleTail.length()>0)
            prg.add(new Chunk(titleTail, Fonts.getUnicode(14)));

        //section = parentSection.addSection(prg, 2);

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

        /* write image attributes
        Element imgAttrs = PdfUtil.imgAttributes(v, vsPath);
        if (imgAttrs!=null) {
            addElement(new Phrase("\n"));
            addElement(imgAttrs);
        }*/

        // write table elements factlist,
        // but first get fixed values & fk rels
        // and split the elements vector into GIS and non-GIS

        v = dsTable.getElements();
        if (v==null || v.size()==0)
            return;

        Vector gisElms = new Vector();
        Vector nonGisElms = new Vector();

        DataElement elem = null;
        String dstID = params==null ? null : params.getParameter("dstID");
        for (int i=0; i<v.size(); i++) {
            elem = (DataElement)v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);
            Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
            elem.setFKRelations(fks);
            Vector attrs = searchEngine.getSimpleAttributes(elem.getID(), "E");
            elem.setAttributes(attrs);

            // split vector by GIS
            if (elem.getGIS()!=null)
                gisElms.add(elem);
            else
                nonGisElms.add(elem);
        }

        if (owner!=null) {
            owner.addTblElms(dsTable.getID(), v);
            owner.addTblNames(dsTable.getID(), tblName);
        }

        String nonGisTitle = " table:";
        String gisTitle = " metadata table:";

        // write non-GIS elements factlist
        prg = new Paragraph();
        prg.add(new Chunk("Columns in ",
                        FontFactory.getFont(FontFactory.HELVETICA, 12)));
        prg.add(new Chunk(tblName,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        prg.add(new Chunk(nonGisTitle,
                        FontFactory.getFont(FontFactory.HELVETICA, 12)));
        addElement(prg);

        addElement(
            PdfUtil.tableElements(nonGisElms, null, owner.getSectioning()));

        // write GIS elements factlist
        if (gisElms.size()>0) {
            addElement(new Phrase("\n"));

            prg = new Paragraph();
            prg.add(new Chunk("Columns in ",
                            FontFactory.getFont(FontFactory.HELVETICA, 12)));
            prg.add(new Chunk(tblName,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            prg.add(new Chunk(gisTitle,
                            FontFactory.getFont(FontFactory.HELVETICA, 12)));
            addElement(prg);

            addElement(
                PdfUtil.tableElements(gisElms, null, owner.getSectioning()));
        }

        /* write data element full guidelines, each into a separate chapter
        for (int i=0; v!=null && i<v.size(); i++) {
            elem = (DataElement)v.get(i);
            addElement(new Paragraph("\n"));
            ElmPdfGuideline elmGuideln = new ElmPdfGuideline(searchEngine, this); //, section);
            elmGuideln.setVsPath(this.vsPath);
            elmGuideln.write(elem.getID(), dsTable.getID());
        }*/
    }

    protected void addElement(Element elm) {

        if (owner!=null)
            owner.addElement(elm);

        //if (elm != null) section.add(elm);
        //return docElements.size();
    }

    public void setVsPath(String vsPath) {
        this.vsPath = vsPath;
    }

    public void setParameters(Parameters params) {
        this.params = params;
    }

    protected Sectioning getSectioning() {
        if (owner!=null)
            return owner.getSectioning();
        else
            return null;
    }

    public void setGIS(boolean hasGIS) {
        this.hasGIS = hasGIS;
    }

    protected void setShowedAttributes() {

        showedAttrs.add("Short name");
        showedAttrs.add("Definition");
        showedAttrs.add("ShortDescription");
        showedAttrs.add("Methodology");
    }
}
