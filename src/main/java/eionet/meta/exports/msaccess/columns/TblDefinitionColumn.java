package eionet.meta.exports.msaccess.columns;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public enum TblDefinitionColumn {

    DST_IDENTIFIER ("ds_Identifier"),
    TBL_IDENTIFIER ("tbl_Identifier"),
    TBL_URL ("tbl_URL"),
    TBL_SHORTNAME ("tbl_ShortName"),
    TBL_NAME ("tbl_Name"),
    TBL_DEFINITION ("tbl_Definition"),
    TBL_METHODOLOGY ("tbl_Methodology"),
    TBL_NUMBER_OF_ELEMENTS ("tbl_ne"),
    TBL_ID ("tbl_guid"),
    TBL_SHORTDESC ("tbl_ShortDescription");


    /** */
    private String columnName;

    /**
     *
     * @param columnName
     */
    private TblDefinitionColumn(String columnName){
        this.columnName = columnName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    public String toString(){
        return columnName;
    }

}
