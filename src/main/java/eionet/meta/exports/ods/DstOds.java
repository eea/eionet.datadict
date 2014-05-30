/*
 * Created on 3.05.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.exports.ods;

import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.Dataset;
import eionet.meta.DsTable;

/**
 *
 * @author jaanus
 *
 *         To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class DstOds extends Ods {

    /** */
    private String dstID = null;

    /**
     * Constructor.
     *
     * @param searchEngine
     *            data dictionary search engine
     * @param dstID
     *            dataset id
     * @throws java.lang.Exception
     *             if operation fails
     */
    public DstOds(DDSearchEngine searchEngine, String dstID) throws Exception {
        this.searchEngine = searchEngine;
        this.dstID = dstID;
        prepare();
    }

    /**
     * Prepares content for dataset.
     *
     * @throws java.lang.Exception
     *             if operation fails
     */
    private void prepare() throws Exception {

        Dataset dst = searchEngine.getDataset(dstID);
        if (dst == null)
            throw new Exception("Dataset not found: " + dstID);

        finalFileName = dst.getIdentifier() + "." + ODS_EXTENSION;
        schemaURLTrailer = "DST" + dst.getID();

        Vector tbls = searchEngine.getDatasetTables(dstID, true);
        for (int i = 0; tbls != null && i < tbls.size(); i++)
            prepareTbl((DsTable) tbls.get(i));
    }
}
