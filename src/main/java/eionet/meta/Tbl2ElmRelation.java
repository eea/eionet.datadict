package eionet.meta;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class Tbl2ElmRelation {

    /** */
    private int tblId;
    private int elmId;
    private int elmPosition;
    private boolean isElmMandatory;
    private String elmMultivalueDelimiter;
    private boolean isElmCommon;

    /**
     * @return the tblId
     */
    public int getTblId() {
        return tblId;
    }
    /**
     * @param tblId the tblId to set
     */
    public void setTblId(int tblId) {
        this.tblId = tblId;
    }
    /**
     * @return the elmId
     */
    public int getElmId() {
        return elmId;
    }
    /**
     * @param elmId the elmId to set
     */
    public void setElmId(int elmId) {
        this.elmId = elmId;
    }
    /**
     * @return the elmPosition
     */
    public int getElmPosition() {
        return elmPosition;
    }
    /**
     * @param elmPosition the elmPosition to set
     */
    public void setElmPosition(int elmPosition) {
        this.elmPosition = elmPosition;
    }
    /**
     * @return the isElmMandatory
     */
    public boolean isElmMandatory() {
        return isElmMandatory;
    }
    /**
     * @param isElmMandatory the isElmMandatory to set
     */
    public void setElmMandatory(boolean isElmMandatory) {
        this.isElmMandatory = isElmMandatory;
    }
    /**
     * @return the elmMultivalueDelimiter
     */
    public String getElmMultivalueDelimiter() {
        return elmMultivalueDelimiter;
    }
    /**
     * @param elmMultivalueDelimiter the elmMultivalueDelimiter to set
     */
    public void setElmMultivalueDelimiter(String elmMultivalueDelimiter) {
        this.elmMultivalueDelimiter = elmMultivalueDelimiter;
    }
    /**
     * @return the isElmCommon
     */
    public boolean isElmCommon() {
        return isElmCommon;
    }
    /**
     * @param isElmCommon the isElmCommon to set
     */
    public void setElmCommon(boolean isElmCommon) {
        this.isElmCommon = isElmCommon;
    }
}
