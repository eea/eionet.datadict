
package eionet.meta.exports.pdf;

import java.io.OutputStream;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Vector;

import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Util;

public class TblPdfFactsheet extends PdfHandout {

    public TblPdfFactsheet(Connection conn, OutputStream os) {
        searchEngine = new DDSearchEngine(conn);
        this.os = os;
    }

    public void write(String tblID) throws Exception {

        if (Util.isEmpty(tblID))
            throw new Exception("Table ID not specified");

        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable == null)
            throw new Exception("Table not found!");

        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T", null, dsTable.getDatasetID());
        dsTable.setSimpleAttributes(v);

        // get data elements (this will set all the simple attributes,
        // but no fixed values required by writer!)
        v = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(v);

        // get the dataset basic info
        Dataset ds = null;
        if (!Util.isEmpty(dsTable.getDatasetID())) {
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }

        write(dsTable, ds);
    }

    private void write(DsTable dsTable, Dataset ds) throws Exception {

        if (dsTable == null)
            throw new Exception("Table object was null!");

        // add simple attributes

        addElement(new Paragraph("Basic metadata:\n", Fonts.get(Fonts.HEADING_0)));

        Hashtable hash = null;
        Vector v = dsTable.getSimpleAttributes();

        // dataset short name
        if (ds != null) {
            hash = new Hashtable();
            hash.put("name", "Dataset");
            hash.put("value", ds.getShortName());
            v.add(0, hash);
        }

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

        addElement(PdfUtil.simpleAttributesTable(v));
        addElement(new Phrase("\n"));

        /* write image attributes
        Element imgAttrs = PdfUtil.imgAttributes(v, vsPath);
        if (imgAttrs != null) {
            addElement(new Phrase("\n"));
            addElement(imgAttrs);
        }*/

        // write table elements, but 1st get their fixed values & FK relations

        v = dsTable.getElements();
        if (v == null || v.size() == 0)
            return;

        DataElement elem = null;
        for (int i = 0; i < v.size(); i++) {
            elem = (DataElement)v.get(i);
            Vector fxValues = searchEngine.getFixedValues(elem.getID(), "elem");
            elem.setFixedValues(fxValues);

            String dstID = getParameter("dstID");
            Vector fks = searchEngine.getFKRelationsElm(elem.getID(), dstID);
            elem.setFKRelations(fks);
        }

        addElement(new Paragraph("Elements in this table:\n", Fonts.get(Fonts.HEADING_0)));
        addElement(PdfUtil.tableElements(v));

        // set the factsheet header
        setHeader("dataset table factsheet");
    }
}
