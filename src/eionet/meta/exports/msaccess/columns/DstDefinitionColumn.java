package eionet.meta.exports.msaccess.columns;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public enum DstDefinitionColumn {

	DST_IDENTIFIER ("ds_Identifier"),
	DST_SHORTNAME ("ds_ShortName"),
	DST_NAME ("ds_Name"),
	DST_DEFINITION ("ds_Definition"),
	DST_METHODOLOGY ("ds_Methodology"),
	DST_URL ("ds_URL"),
	DST_NUMBER_OF_TABLES ("ds_ntbl"),
	DST_DATE ("ds_DateTime");
	
	/** */
	private String columnName;
	
	/**
	 * 
	 * @param columnName
	 */
	private DstDefinitionColumn(String columnName){
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
