package eionet.meta.outservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import eionet.meta.DDSearchEngine;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

/**
 *
 */
public class OutService {

    /** */
    Connection conn = null;

    /**
     *
     */
    public OutService() {
    }

    /**
     *
     * @param raID
     * @return
     * @throws Exception
     */
    public Vector getParametersByActivityID(String raID) throws Exception {

        try {
            if (conn==null) getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            return searchEngine.getParametersByActivityID(raID);
        }
        finally {
            closeConnection();
        }
    }

    /**
     * This method returns the IDs and titles of all ogligations that have
     * a released dataset definition present in DD.
     *
     * @return
     * @throws Exception
     */
    public Vector getObligationsWithDatasets() throws Exception {

        try {
            if (conn==null) getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            return searchEngine.getObligationsWithDatasets();
        }
        finally {
            closeConnection();
        }
    }

    /**
     * Created by Dusko Kolundzija(ED).
     * Modified by Jaanus Heinlaid (<a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>)
     *
     * Returns all tables of all released datasets, including historic versions.
     * Return type is s a Vector of Hashtables where each Hashtable represents one table and has the following keys:
     * - tblId          the table's numeric identifier
     * - identifier     the table's alphanumeric (i.e. logical) identifier in DD database
     * - shortName      the table's short name
     * - dataSet        the short name of the dataset where this table belongs to
     * - dateReleased   the release date of the dataset where this table belongs to
     *
     * The caller should know that each of the above keys may be missing.
     *
     * @return Vector of Hashtables
     * @throws Exception
     */
    public Vector getDSTables() throws Exception {

        try {
            if (conn==null) getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn);

            HashSet dstStatuses = new HashSet();
            dstStatuses.add("Released");
            dstStatuses.add("Recorded");
            Vector result = searchEngine.getDatasetTables(null, null, null, null, null, null, dstStatuses, false);

            Vector ret = new Vector();
            for (int i=0; i<result.size(); i++) {
                DsTable table = (DsTable)result.get(i);

                String table_id = table.getID();
                String table_name = table.getShortName();
                String ds_id = table.getDatasetID();
                String ds_name = table.getDatasetName();
                String dsNs = table.getParentNs();

                Hashtable hash = new Hashtable();
                hash.put("tblId", table.getID());
                hash.put("identifier", table.getIdentifier());
                hash.put("shortName", table.getShortName());
                hash.put("dataSet", table.getDatasetName());

                if (table.getDstStatus()!=null && table.getDstStatus().equals("Released") && table.getDstDate()!=null) {
                    String dateFormatted = (new SimpleDateFormat("ddMMyy")).format(new Date(Long.parseLong(table.getDstDate())));
                    hash.put("dateReleased", dateFormatted);
                }
                ret.add(hash);

            }
            return ret;

        }
        finally {
            closeConnection();
        }

    }


    /**
     *
     * @throws Exception
     */
    private void getConnection() throws Exception {
        conn = ConnectionUtil.getSimpleConnection();
    }

    /**
     *
     */
    private void closeConnection() {
        try { if (conn!=null) conn.close(); } catch (SQLException e) {}
    }

    /**
     *
     * @param objType
     * @param objId
     * @return
     * @throws Exception
     */
    public Hashtable getDatasetWithReleaseInfo(String objType, String objId) throws Exception {

        // validate objType
        if (objType==null || (!objType.equals("dst") && !objType.equals("tbl"))) {
            throw new IllegalArgumentException("Missing or invalid objType!");
        }

        // validate objId
        if (objId==null || objId.trim().length()==0 || !Util.isNumericID(objId)) {
            throw new IllegalArgumentException("Missing or invalid objId!");
        }

        ReplyGetDataset reply = new ReplyGetDataset();
        try {
            // initiate the connection
            if (conn==null) {
                getConnection();
            }

            // get the dataset object
            DDSearchEngine searchEngine = new DDSearchEngine(conn);
            Dataset dst = null;
            if (objType.equals("dst")) {
                dst = searchEngine.getDataset(objId);
            }
            else {
                DsTable tbl = searchEngine.getDatasetTable(objId);
                if (tbl!=null) {
                    String dstId = tbl.getDatasetID();
                    if (dstId!=null) {
                        dst = searchEngine.getDataset(dstId);
                    }
                }
            }

            // if dataset object found, prepare the reply
            if (dst!=null) {

                // set basics
                reply.setId(dst.getID());
                reply.setIdentifier(dst.getIdentifier());
                reply.setShortname(dst.getShortName());
                reply.setStatus(dst.getStatus());
                reply.setDate(dst.getDate());
                reply.setVersion(dst.getVersion());

                // set table ids
                Vector tables = searchEngine.getDatasetTables(dst.getID(), false);
                for (int i=0; i<tables.size(); i++) {
                    reply.addTableId(((DsTable)tables.get(i)).getID());
                }

                // see if this is the latest released dataset, if no then find the latest and set its id+date
                Vector statuses = new Vector();
                statuses.add("Released");
                String latestReleasedDatasetId = searchEngine.getLatestDstID(dst.getIdentifier(), statuses);
                if (latestReleasedDatasetId!=null) {

                    if (latestReleasedDatasetId.equals(dst.getID())) {
                        reply.setIsLatestReleased(true);
                    }
                    else {
                        reply.setIsLatestReleased(false);
                        Dataset latestReleasedDst = searchEngine.getDataset(latestReleasedDatasetId);
                        if (latestReleasedDst!=null) {
                            reply.setIdOfLatestReleased(latestReleasedDst.getID());
                            reply.setDateOfLatestReleased(latestReleasedDst.getDate());
                        }
                    }
                }
            }
        }
        finally {
            closeConnection();
        }

        return reply.getHashTable();
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        OutService outService = new OutService();
        System.out.println(outService.getDatasetWithReleaseInfo("dst", "2807"));
    }
}

