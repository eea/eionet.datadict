package eionet.meta.outservices;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 */
public class ReplyGetDataset{

    /** */
    private static final String ID = "id";
    private static final String IDENTIFIER = "identifier";
    private static final String SHORTNAME = "shortname";
    private static final String STATUS = "status";
    private static final String VERSION = "version";
    private static final String DATE = "date";
    private static final String TABLE_IDS = "tableIds";

    /** */
    private static final String IS_LATEST_RELEASED = "isLatestReleased";
    private static final String ID_OF_LATEST_RELEASED = "idOfLatestReleased";
    private static final String DATE_OF_LATEST_RELEASED = "dateOfLatestReleased";

    /** */
    private Hashtable hashTable = new Hashtable();

    /**
     *
     */
    public ReplyGetDataset() {
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        hashTable.put(ID, id);
    }

    /**
     *
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        hashTable.put(IDENTIFIER, identifier);
    }

    /**
     *
     * @param shortname
     */
    public void setShortname(String shortname) {
        hashTable.put(SHORTNAME, shortname);
    }

    /**
     *
     * @param status
     */
    public void setStatus(String status) {
        hashTable.put(STATUS, status);
    }

    /**
     *
     * @param version
     */
    public void setVersion(String version) {
        hashTable.put(VERSION, version);
    }

    /**
     *
     * @param dateReleased
     */
    public void setDate(String date) {
        hashTable.put(DATE, date);
    }

    /**
     *
     * @param f
     */
    public void setIsLatestReleased(boolean f) {
        hashTable.put(IS_LATEST_RELEASED, String.valueOf(f));
    }

    /**
     *
     * @param tableId
     */
    public void addTableId(String tableId) {

        if (tableId!=null && tableId.trim().length()>0) {

            Vector tableIds = (Vector)hashTable.get(TABLE_IDS);
            if (tableIds==null) {
                tableIds = new Vector();
                hashTable.put(TABLE_IDS, tableIds);
            }

            tableIds.add(tableId);
        }
    }

    /**
     *
     * @param idOfLatestReleased
     */
    public void setIdOfLatestReleased(String idOfLatestReleased) {
        hashTable.put(ID_OF_LATEST_RELEASED, idOfLatestReleased);
    }

    /**
     *
     * @param dateOfLatestReleased
     */
    public void setDateOfLatestReleased(String dateOfLatestReleased) {
        hashTable.put(DATE_OF_LATEST_RELEASED, dateOfLatestReleased);
    }

    /**
     * @return the hashTable
     */
    public Hashtable getHashTable() {
        return hashTable;
    }
}
