package eionet.meta.exports.msaccess.columns;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public enum TblElmRelationsColumn {
    
    TBL_IDENTIFIER ("tbl_Identifier"),
    ELM_ORDER ("e_order"),
    ELM_IDENTIFIER ("e_Identifier"),
    ELM_COMMON ("e_Common"),
    ELM_MANDATORY ("e_Mandatory"),
    ELM_MULTIVALUEDELIM ("e_MultiValueDelimiter"),
    TBL_ID ("tbl_guid"),
    ELM_ID ("e_guid");

    /** */
    private String columnName;
    
    /**
     * 
     * @param columnName
     */
    private TblElmRelationsColumn(String columnName){
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
