package eionet.meta.exports.msaccess.columns;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public enum ElmDefinitionColumn {

    TBL_IDENTIFIER("tbl_Identifier"),

    ELM_ORDER("e_order"), ELM_IDENTIFIER("e_Identifier"), ELM_URL("e_URL"), ELM_COMMON("e_Common"), ELM_TYPE("e_Type"), ELM_SHORTNAME(
            "e_ShortName"), ELM_NAME("e_Name"), ELM_DEFINITION("e_Definition"), ELM_METHODOLOGY("e_Methodology"), ELM_DATATYPE(
            "e_Datatype"), ELM_MINSIZE("e_MinSize"), ELM_MAXSIZE("e_MaxSize"), ELM_DECIMALPRECISION("e_DecimalPrecission"), ELM_UNIT(
            "e_Unit"), ELM_MININCLUSIVE("e_MinInclusiveValue"), ELM_MAXINCLUSIVE("e_MaxInclusiveValue"), ELM_MINEXCLUSIVE(
            "e_MinExclusiveValue"), ELM_MAXEXCLUSIVE("e_MaxExclusiveValue"), ELM_PUBLICORINTERNAL("e_PublicOrInternal"), ELM_MANDATORY(
            "e_Mandatory"), ELM_MULTIVALUEDELIM("e_MultiValueDelimiter"), ELM_ID("e_guid"), ELM_DATASERVICE_DEFINITION(
            "e_DataserviceDefinition"), ELM_DATASERVICE_NOTE("e_DataserviceNote");

    /** */
    private String columnName;

    /**
     *
     * @param columnName
     */
    private ElmDefinitionColumn(String columnName) {
        this.columnName = columnName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    public String toString() {
        return columnName;
    }

}
