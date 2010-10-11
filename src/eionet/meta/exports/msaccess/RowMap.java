package eionet.meta.exports.msaccess;

import java.util.HashMap;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class RowMap extends HashMap<String,Object>{
	
	/**
	 * 
	 */
	public RowMap(Enum[] columns){
		
		super();
		if (columns==null || columns.length==0){
			throw new IllegalArgumentException("Columns array must be null or empty");
		}
		else{
			for (Enum column : columns){
				put(column.toString(), null);
			}
		}
	}

	/**
	 * 
	 * @param column
	 * @param value
	 */
	public void put(Enum column, Object value){
		put(column.toString(), value);
	}
}
