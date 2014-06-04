/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods;

import eionet.meta.DDSearchEngine;
import eionet.meta.DsTable;

/**
 *
 * @author jaanus
 */
public class TblOds extends Ods {

    /** */
    private String tblID = null;

    /**
     * Constructor.
     *
     * @param searchEngine
     *            data dictionary search engine
     * @param tblID
     *            table id
     * @throws java.lang.Exception
     *             if operation fails
     */
    public TblOds(DDSearchEngine searchEngine, String tblID) throws Exception {
        this.searchEngine = searchEngine;
        this.tblID = tblID;
        prepare();
    }

    /**
     * Prepares content for table.
     *
     * @throws java.lang.Exception if operation fails.
     */
    private void prepare() throws Exception {

        DsTable tbl = searchEngine.getDatasetTable(tblID);
        if (tbl == null)
            throw new Exception("Table not found: " + tblID);

        // set the final file name
        String dstIdfier = tbl.getDstIdentifier();
        if (dstIdfier != null)
            finalFileName = dstIdfier + "_";
        finalFileName = finalFileName + tbl.getIdentifier() + "." + ODS_EXTENSION;
        schemaURLTrailer = "TBL" + tbl.getID();

        prepareTbl(tbl);
    }
}
