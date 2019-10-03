package eionet.meta.exports.msaccess.columns;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public enum CodeListsColumn {

    /** */
//  DST_IDENTIFIER ("ds_Identifier"),
    TBL_IDENTIFIER ("tb_Identifier"),
    ELM_IDENTIFIER ("e_Identifier"),
    VALUE ("lu_Value"),
    DEFINITION ("lu_Definition"),
    SHORT_DESC ("lu_ShortDescription"),
    ELM_ID ("e_guid");

    /** */
    private String columnName;

    /**
     *
     * @param columnName
     */
    private CodeListsColumn(String columnName) {
        this.columnName = columnName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    public String toString() {
        return columnName;
    }
}
