package eionet.meta.exports.dbf;

import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import eionet.meta.DDSearchEngine;
import eionet.meta.DataElement;
import eionet.meta.DsTable;
import eionet.meta.exports.mdb.MdbException;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Dbf {
	
	/** */
	private static final int C_MAX_LENGTH = 254;
	private static final int F_FIX_LENGTH = 20;
	private static final int L_FIX_LENGTH = 1;
	private static final int MAX_DEC_COUNT = 15;

	/** */
	private String tblID;
	
	/** */
	private Connection conn;

	/** */
	private DDSearchEngine searchEngine;

	/** */
	private DBFWriter dbfWriter;

	/** */
	private String fileName;
	
	/** */
	private static HashMap mapDataTypes;
	private static HashMap mapMaxLengths;
	
	/**
	 * @throws Exception 
	 * 
	 *
	 */
	public Dbf(String tblID, Connection conn) throws Exception{
		
		this.tblID = tblID;
		this.conn = conn;
		this.searchEngine = new DDSearchEngine(this.conn);
		
		dbfWriter = null;
		fileName = null;
		create();
	}
	
	/**
	 * 
	 * @return
	 */
	private void create() throws Exception{
		
		// get DD table
		DsTable tbl = searchEngine.getDatasetTable(tblID);
		if (tbl==null)
			throw new Exception("Table not found, id=" + tblID);
		
		// init list of DBF fields, get DD elements, loop over them
		ArrayList fields = new ArrayList();
		Vector elms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
		for (int i=0; elms!=null && i<elms.size(); i++){

			// get data element, skip those where GIS type is not null
			DataElement elm = (DataElement)elms.get(i);
			if (elm.getGIS()!=null)
				continue;
			
			// construct a DBF field whose name is DD element's Identifier
			DBFField fld = new DBFField();
			String elmIdentifier = elm.getIdentifier();
			fld.setName(elmIdentifier.length()>10 ? elmIdentifier.substring(0,10) : elmIdentifier);
			
			// set DBF field's data type
			Dbf.setDataType(fld, elm);
			
			// set DBF field's length
			Dbf.setFieldLength(fld, elm);
			
			// set DBF field's decimal count
			Dbf.setDecimalCount(fld, elm);
			
			// add constructed field into fields list 
			fields.add(fld);
		}

		// construct fields array
		DBFField[] dbff = new DBFField[fields.size()];
		for (int i=0; i<fields.size(); i++)
			dbff[i] = (DBFField)fields.get(i);
		
		// create the DBFWriter
		dbfWriter = new DBFWriter();
		dbfWriter.setFields(dbff);
		fileName = tbl.getIdentifier();
	}
	
	/**
	 * 
	 * @param out
	 * @throws DBFException 
	 */
	public void write(OutputStream out) throws DBFException{
		if (dbfWriter!=null)
			dbfWriter.write(out);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFileName(){
		return fileName;
	}

	/**
	 * 
	 * @param fld
	 * @param elm
	 */
	private static void setDataType(DBFField fld, DataElement elm){
		
		if (Dbf.mapDataTypes==null){
			Dbf.mapDataTypes = new HashMap();
			Dbf.mapDataTypes.put("string", new Byte(DBFField.FIELD_TYPE_C));
			Dbf.mapDataTypes.put("date", new Byte(DBFField.FIELD_TYPE_D));
			Dbf.mapDataTypes.put("integer", new Byte(DBFField.FIELD_TYPE_N));
			Dbf.mapDataTypes.put("float", new Byte(DBFField.FIELD_TYPE_F));
			Dbf.mapDataTypes.put("double", new Byte(DBFField.FIELD_TYPE_F));
			Dbf.mapDataTypes.put("boolean", new Byte(DBFField.FIELD_TYPE_L));
		}
		
		fld.setDataType(((Byte)Dbf.mapDataTypes.get(elm.getAttributeValueByShortName("Datatype"))).byteValue());
	}

	/**
	 * 
	 * @param fld
	 * @param elm
	 * @throws Exception 
	 */
	private static void setFieldLength(DBFField fld, DataElement elm) throws Exception{

		// if 'date' type, return right away because DBFField throws exception when setting field length for 'date'
		byte dataType = fld.getDataType();
		if (dataType==DBFField.FIELD_TYPE_D)
			return; // 
		
		// get maximum length (getter ensures it's not null)
		Integer maxLength = Dbf.getMaxLength(dataType);

		// determine field length by DD's "MaxSize" attribute
		int fldLength = 0;
		String strMaxSize = elm.getAttributeValueByShortName("MaxSize");
		if (strMaxSize!=null){
			try{
				fldLength = Integer.parseInt(strMaxSize);
			}
			catch (NumberFormatException nfe){fldLength = 0;}
		}

		// if the determined field length is <=0, use maxLength.
		// if the determined field length is bigger than maxLength, then in case of 'character' use 'memo', otherwise use maxLength
		// if 0<fieldLength<=maxLength then use fieldLength 
		if (fldLength<=0){
			fld.setFieldLength(maxLength.intValue());
		}
		else if (fldLength > maxLength.intValue()){
			if (dataType==DBFField.FIELD_TYPE_C){
				fld.setDataType(DBFField.FIELD_TYPE_M);
				fld.setFieldLength(Dbf.getMaxLength(DBFField.FIELD_TYPE_M));
			}
			else
				fld.setFieldLength(maxLength);
		}
		else
			fld.setFieldLength(fldLength);
	}

	/**
	 * 
	 * @param elm
	 */
	private static void setDecimalCount(DBFField fld, DataElement elm){
		
		String decPrecision = elm.getAttributeValueByShortName("DecimalPrecision");
		
		int decCount = 0;
		if (decPrecision!=null){
			try{
				decCount = Integer.parseInt(decPrecision);
			}
			catch (NumberFormatException nfe){decCount = 0;}
		}
		
		if (decCount>0){
			int fldLength = fld.getFieldLength();
			if (decCount > fldLength)
				decCount = fldLength;
			
			fld.setDecimalCount(decCount>Dbf.MAX_DEC_COUNT ? MAX_DEC_COUNT : decCount);
		}
	}
	
	/**
	 * 
	 * @param dataType
	 * @return
	 * @throws Exception 
	 */
	private static Integer getMaxLength(byte dataType) throws Exception{

		if (Dbf.mapMaxLengths==null){
			Dbf.mapMaxLengths = new HashMap();
			Dbf.mapMaxLengths.put(new Byte(DBFField.FIELD_TYPE_C), new Integer(253));
			Dbf.mapMaxLengths.put(new Byte(DBFField.FIELD_TYPE_N), new Integer(17));
			Dbf.mapMaxLengths.put(new Byte(DBFField.FIELD_TYPE_F), new Integer(20));
			Dbf.mapMaxLengths.put(new Byte(DBFField.FIELD_TYPE_L), new Integer(1));
			Dbf.mapMaxLengths.put(new Byte(DBFField.FIELD_TYPE_M), new Integer(10));
		}
		Integer i = (Integer)Dbf.mapMaxLengths.get(new Byte(dataType));
		
		// knowing the maximum length is a must, because eventually field length must be set to some >0 integer
		if (i==null || i.intValue()<=0)
			throw new Exception("Missing or invalid maximum length for data type " + dataType);
		
		return i;
	}
}
