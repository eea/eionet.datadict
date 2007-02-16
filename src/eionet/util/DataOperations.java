/*
 * Created on 30.10.2006
 */
package eionet.util;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.SQLGenerator;
import com.tee.xmlserver.DBPoolIF;
import com.tee.xmlserver.XDBApplication;

import eionet.meta.savers.CopyHandler;
import eionet.meta.savers.DataElementHandler;

/**
 * 
 * @author jaanus
 */
public class DataOperations{
	
	/** */
	private static final int IDX_TBL = 0;
	private static final int IDX_POS = 1;
	private static final String DELETE = "delete";
	private static final String PRINT = "print";
	
	public static final String PARAM_ACTION = "action";
	public static final String ACTION_CLEANUP = "CLEANUP";
	public static final String ACTION_CREATE = "CREATE";
	public static final String ACTION_CLEANUP_CREATE = "CLEANUP_CREATE";
	public static final String ACTION_BOOLEAN_VALUES = "BOOLEAN_VALUES";
	
	/** */
	private Connection conn = null;
	private PrintWriter outputWriter = null;
	
	/**
	 * 
	 * @param ctx
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public DataOperations(Connection conn, PrintWriter outputWriter) throws IOException{
		this.conn = conn;
		this.outputWriter = outputWriter;
	}
	
	/**
	 * @throws SQLException 
	 */
	public void createTables() throws Exception{

		// get existing datasets and tables
		HashSet existingDatasets = new HashSet();
		HashMap existingTables = new HashMap();
		Statement stmt = null;
		ResultSet rs = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select DATASET_ID from DATASET");
			while (rs.next())
				existingDatasets.add(rs.getString(1));
			rs.close();
			rs = stmt.executeQuery("select TABLE_ID from DS_TABLE");
			while (rs.next())
				existingTables.put(rs.getString(1), null);
			rs.close();
			
			// get each table's datasets, skip non-existing ones (ie datasets whose ID is in DST2TBL but do not actually exist in DATASET)
			int countTblDatasets = 0;
			rs = stmt.executeQuery("select TABLE_ID, DATASET_ID from DST2TBL order by DATASET_ID");
			while (rs.next()){
				String tblID = rs.getString(1);
				String dstID = rs.getString(2);
				if (existingDatasets.contains(dstID) && existingTables.containsKey(tblID)){
					Vector tblDatasets = (Vector)existingTables.get(tblID);
					if (tblDatasets==null)
						tblDatasets = new Vector();
					tblDatasets.add(dstID);
					countTblDatasets++;
					existingTables.put(tblID, tblDatasets);
				}
			}
			outputWriteln("");
			outputWriteln("Found " + existingDatasets.size() + " existing datasets");
			outputWriteln("Found " + existingTables.size() + " existing tables");
			outputWrite("Creating " + (countTblDatasets-existingTables.size()) + " new tables...");

			// go through each table's datasets, create table copy for every dataset						
			int countTablesCreated = 0;
			int outputRefreshStep = 90;
			int countOutputRefreshSteps = 1;
			StringBuffer buf = null; 
			long timeStart = System.currentTimeMillis();
			if (!existingTables.isEmpty()){
				Iterator iterTables = existingTables.keySet().iterator();
				while (iterTables!=null && iterTables.hasNext()){
					String tblID = (String)iterTables.next();
					Vector tblDatasets = (Vector)existingTables.get(tblID);
					SQLGenerator gen = new SQLGenerator(); 
					// skip first dataset since for one dataset the table copy already exists
					for (int i=1; tblDatasets!=null && i<tblDatasets.size(); i++){
						String dstID = (String)tblDatasets.get(i);
						// delete the table's relation with this dataset,
						// since that relation will now be created with the new table copy
						buf = new StringBuffer();
						buf.append("delete from DST2TBL where TABLE_ID=").append(tblID).append(" and DATASET_ID=").append(dstID);
						stmt.executeUpdate(buf.toString());
						// create new table copy, relate it to this dataset
						String newTblID = copyTbl(tblID);
						if (newTblID!=null && newTblID.length()>0){
							countTablesCreated++;			
							gen.clear();
							gen.setTable("DST2TBL");
							gen.setFieldExpr("DATASET_ID", dstID);
							gen.setFieldExpr("TABLE_ID", newTblID);
							stmt.executeUpdate(gen.insertStatement());
							
							if (countTablesCreated==(outputRefreshStep*countOutputRefreshSteps)){
								outputWrite(".");
								countOutputRefreshSteps++;
							}
						}
					}
				}
			}

			long timeEnd = System.currentTimeMillis();
			long durationMillis = timeEnd - timeStart;
			outputWriteln("");
			outputWriteln(countTablesCreated + " new tables created, time spent: " + durationMillis/1000 + " sec");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * @throws SQLException 
	 */
	public void createNonCommonElements() throws SQLException{

		// get existing tables and elements
		HashSet existingTables = new HashSet();
		HashMap existingNonCommonElements = new HashMap();
		Statement stmt = null;
		ResultSet rs = null;
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select TABLE_ID from DS_TABLE");
			while (rs.next())
				existingTables.add(rs.getString(1));
			rs.close();
			rs = stmt.executeQuery("select DATAELEM_ID from DATAELEM where PARENT_NS is not null");
			while (rs.next())
				existingNonCommonElements.put(rs.getString(1), null);
			rs.close();
			
			// get each non-common element's tables, skip non-existing ones
			int countElmTables = 0;
			rs = stmt.executeQuery("select * from TBL2ELEM order by TABLE_ID");
			while (rs.next()){
				String tblID  = rs.getString("TABLE_ID");
				String elmID  = rs.getString("DATAELEM_ID");
				String elmPos = rs.getString("POSITION");
				if (existingTables.contains(tblID) && existingNonCommonElements.containsKey(elmID)){
					Vector elmTables = (Vector)existingNonCommonElements.get(elmID);
					if (elmTables==null)
						elmTables = new Vector();
					String[] elmTable = new String[2];
					elmTable[IDX_TBL] = tblID;
					elmTable[IDX_POS] = elmPos;
					elmTables.add(elmTable);
					countElmTables++;
					existingNonCommonElements.put(elmID, elmTables);
				}
			}
			outputWriteln("");
			outputWriteln("Found " + existingTables.size() + " existing tables");
			outputWriteln("Found " + existingNonCommonElements.size() + " existing non-common elements");
			outputWrite("Creating " + (countElmTables-existingNonCommonElements.size()) +
					" new non-common elements...");

			// go through each non-common element's tables, create element copy for every table						
			int countElmsCreated = 0;
			int outputRefreshStep = 100;
			int countOutputRefreshSteps = 1;
			StringBuffer buf = null;
			long timeStart = System.currentTimeMillis();
			if (!existingNonCommonElements.isEmpty()){
				Iterator iterElements = existingNonCommonElements.keySet().iterator();
				while (iterElements!=null && iterElements.hasNext()){
					String elmID = (String)iterElements.next();
					Vector elmTables = (Vector)existingNonCommonElements.get(elmID);
					SQLGenerator gen = new SQLGenerator(); 
					// skip first table since for one table the element copy already exists
					for (int i=1; elmTables!=null && i<elmTables.size(); i++){
						String[] table = (String[])elmTables.get(i);
						String tblID = table[IDX_TBL];
						String tblPos = table[IDX_POS];
						// delete the element's relation with this table,
						// since that relation will now be created with the new element copy
						buf = new StringBuffer();
						buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(elmID).append(" and TABLE_ID=").append(tblID);
						stmt.executeUpdate(buf.toString());
						// create new element copy, relate it to this table
						String newElmID = copyElm(elmID);
						if (newElmID!=null && newElmID.length()>0){
							countElmsCreated++;
							gen.clear();
							gen.setTable("TBL2ELEM");
							gen.setFieldExpr("TABLE_ID", tblID);
							gen.setFieldExpr("DATAELEM_ID", newElmID);
							stmt.executeUpdate(gen.insertStatement());
							
							if (countElmsCreated==(outputRefreshStep*countOutputRefreshSteps)){
								outputWrite(".");
								countOutputRefreshSteps++;
							}
						}
					}
				}
			}

			long timeEnd = System.currentTimeMillis();
			long durationMillis = timeEnd - timeStart;
			outputWriteln("");
			outputWriteln(countElmsCreated + " new non-common elements created, time spent: " + durationMillis/1000 + " sec");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * @throws SQLException 
	 */
	public void createBooleanFixedValues() throws SQLException{
		
		Statement stmt = null;
		ResultSet rs = null;
		try{
			outputWriteln("");
			outputWriteln("Creating fixed values for boolean data elements that have no fixed values yet...");
			
			// get distinct boolean data elements with no fixed values
			HashSet hashSet = new HashSet();
			StringBuffer buf = new StringBuffer();
			buf.
			append("select distinct ATTRIBUTE.DATAELEM_ID from ATTRIBUTE ").
			append("left outer join FXV on (ATTRIBUTE.DATAELEM_ID=FXV.OWNER_ID and FXV.OWNER_TYPE='elem') ").
			append("where ATTRIBUTE.PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and ATTRIBUTE.VALUE='boolean' and ").
			append("FXV.FXV_ID is null");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				hashSet.add(rs.getString(1));
			}
			rs.close();
			
			outputWriteln(hashSet.size() + " such boolean data elements found...");
			
			// auto-create fixed values for the above found elements
			int count = 0;
			for (Iterator i=hashSet.iterator(); !hashSet.isEmpty() && i.hasNext(); count++){
				DataElementHandler.autoCreateBooleanFixedValues(stmt, (String)i.next()); 
			}
			outputWriteln("Created fixed values for " + count + " boolean data elements");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 * @param tblID
	 * @return
	 * @throws SQLException 
	 */
	private String copyTbl(String tblID) throws SQLException{
		
        // copy row in DS_TABLE table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("TABLE_ID", "");
        CopyHandler copyHandler = new CopyHandler(conn, null, null);
        String newID = copyHandler.copy(gen, "TABLE_ID=" + tblID, false);
        
        if (newID!=null){
		    // copy simple attributes
		    gen.clear();
		    gen.setTable("ATTRIBUTE");
		    gen.setField("DATAELEM_ID", newID);
		    copyHandler.copy(gen, "DATAELEM_ID=" + tblID + " and PARENT_TYPE='T'");
		    
		    // copy complex attributes
		    copyHandler.copyComplexAttrs(newID, tblID, "T");

		    // copy TBL2ELEM rows
		    gen.clear();
		    gen.setTable("TBL2ELEM");
		    gen.setField("TABLE_ID", newID);
		    copyHandler.copy(gen, "TABLE_ID=" + tblID);

		    return newID;
        }
        
        return null;
	}
	
	/**
	 * 
	 * @param elmID
	 * @return
	 * @throws SQLException
	 */
	private String copyElm(String elmID) throws SQLException{
		
        // copy row in DATAELEM table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("DATAELEM_ID", "");
        CopyHandler copyHandler = new CopyHandler(conn, null, null);
        String newID = copyHandler.copy(gen, "DATAELEM_ID=" + elmID, false);
        
        if (newID!=null){
        	
			// copy simple attributes
			gen.clear();
			gen.setTable("ATTRIBUTE");
			gen.setField("DATAELEM_ID", newID);
			copyHandler.copy(gen, "DATAELEM_ID=" + elmID + " and PARENT_TYPE='E'");
			
	        // copy complex attributes
			copyHandler.copyComplexAttrs(newID, elmID, "E");
	        
	        // copy fixed values
			copyHandler.copyFxv(newID, elmID, "elem");
			
	        // copy fk relations
			gen.clear();
			gen.setTable("FK_RELATION");
			gen.setField("REL_ID", "");
			gen.setField("A_ID", newID);
			copyHandler.copy(gen, "A_ID=" + elmID, false);
			gen.clear();
			gen.setTable("FK_RELATION");
			gen.setField("REL_ID", "");
			gen.setField("B_ID", newID);
			copyHandler.copy(gen, "B_ID=" + elmID);
			
			return newID;
        }
		
        return null;
	}
	
	/**
	 * @throws Exception 
	 * 
	 *
	 */
	public void cleanupTables() throws Exception{
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			outputWriteln("");
					
			// delete DST2TBL relations where the dataset or the table does not actually exist
			outputWriteln("deleting DST2TBL relations where the dataset or the table does not actually exist...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select DST2TBL.* from DST2TBL ").
		    append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID "). 
			append("left outer join DS_TABLE on DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID ").
			append("where DATASET.DATASET_ID is null or DS_TABLE.TABLE_ID is null");
			int count = 0;
			HashSet hashSet = new HashSet();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				Hashtable hash = new Hashtable();
				hash.put("DATASET_ID", rs.getString("DATASET_ID"));
				hash.put("TABLE_ID", rs.getString("TABLE_ID"));
				if (!hashSet.contains(hash)){
					hashSet.add(hash);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such relations found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from DST2TBL where DATASET_ID=").append(hash.get("DATASET_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");
			
			// delete DST2TBL relations where the table has a newer version in the same dataset
			outputWriteln("deleting DST2TBL relations where the table has a newer version in the same dataset...");
			buf = new StringBuffer();
			buf.
			append("select distinct DST2TBL.DATASET_ID, DS_TABLE.IDENTIFIER, DS_TABLE.TABLE_ID ").
			append("from DS_TABLE left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
			append("order by DST2TBL.DATASET_ID asc, DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");
			count = 0;
			hashSet = new HashSet();
			Hashtable prevOne = null;
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				String dstID = rs.getString("DST2TBL.DATASET_ID");
				Hashtable thisOne = new Hashtable();				
				thisOne.put("DST2TBL.DATASET_ID", dstID);
				thisOne.put("DS_TABLE.IDENTIFIER", rs.getString("DS_TABLE.IDENTIFIER"));
				if (prevOne!=null && prevOne.equals(thisOne)){					
					Hashtable hash = new Hashtable();
					hash.put("DATASET_ID", dstID);
					hash.put("TABLE_ID", rs.getString("DS_TABLE.TABLE_ID"));
					if (!hashSet.contains(hash)){
						hashSet.add(hash);
						count++;
					}
				}
				else
					prevOne = thisOne;
			}
			rs.close();
			outputWriteln(count + " such relations found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from DST2TBL where DATASET_ID=").append(hash.get("DATASET_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");
			
			// delete tables with no parent dataset
			outputWriteln("deleting tables with no parent dataset...");
			buf = new StringBuffer();
			buf.append("select DS_TABLE.TABLE_ID from DS_TABLE left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
			append("where DST2TBL.TABLE_ID is null");
			count = 0;
			hashSet = new HashSet();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				String tblID = rs.getString("TABLE_ID");
				if (!hashSet.contains(tblID)){
					hashSet.add(tblID);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such tables found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				deleteTbl((String)i.next());
				count++;
			}
			outputWriteln(count + " deleted");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void cleanupElements() throws Exception{
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			outputWriteln("");
			
			// delete TBL2ELEM relations where the table or the element does not actually exist
			outputWriteln("deleting TBL2ELEM relations where the table or the element does not actually exist...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select TBL2ELEM.* from TBL2ELEM ").
		    append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID "). 
			append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ").
			append("where DS_TABLE.TABLE_ID is null or DATAELEM.DATAELEM_ID is null");
			int count = 0;
			HashSet hashSet = new HashSet();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				Hashtable hash = new Hashtable();
				hash.put("DATAELEM_ID", rs.getString("DATAELEM_ID"));
				hash.put("TABLE_ID", rs.getString("TABLE_ID"));
				if (!hashSet.contains(hash)){
					hashSet.add(hash);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such relations found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(hash.get("DATAELEM_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");
			
			// delete TBL2ELEM relations where the non-common element has a newer version in the same table
			outputWriteln("deleting TBL2ELEM relations where the non-common element has a newer version in the same table...");
			buf = new StringBuffer();
			buf.
			append("select distinct TBL2ELEM.TABLE_ID, DATAELEM.IDENTIFIER, DATAELEM.DATAELEM_ID ").
			append("from DATAELEM left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
			append("where DATAELEM.PARENT_NS is not null ").
			append("order by TBL2ELEM.TABLE_ID asc, DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");
			count = 0;
			hashSet = new HashSet();
			Hashtable prevOne = null;
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){				
				String tblID = rs.getString("TBL2ELEM.TABLE_ID");
				Hashtable thisOne = new Hashtable();
				thisOne.put("TBL2ELEM.TABLE_ID", tblID);
				thisOne.put("DATAELEM.IDENTIFIER", rs.getString("DATAELEM.IDENTIFIER"));
				if (prevOne!=null && prevOne.equals(thisOne)){
					Hashtable hash = new Hashtable();
					hash.put("TABLE_ID", tblID);
					hash.put("DATAELEM_ID", rs.getString("DATAELEM.DATAELEM_ID"));
					if (!hashSet.contains(hash)){
						hashSet.add(hash);
						count++;
					}
				}
				else
					prevOne = thisOne;
			}
			rs.close();
			outputWriteln(count + " such relations found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(hash.get("DATAELEM_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");

			// delete non-common elements with no parent table
			outputWriteln("deleting non-common elements with no parent table...");
			buf = new StringBuffer();
			buf.append("select DATAELEM.DATAELEM_ID from DATAELEM left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
			append("where DATAELEM.PARENT_NS is not null and TBL2ELEM.DATAELEM_ID is null");
			count = 0;
			hashSet = new HashSet();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				String elmID = rs.getString("DATAELEM_ID");
				if (!hashSet.contains(elmID)){
					hashSet.add(elmID);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such non-common elements found...");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				deleteElm((String)i.next());
				count++;
			}
			outputWriteln(count + " deleted");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param tblID
	 */
	private void deleteTbl(String tblID) throws Exception{
		
		if (tblID==null)
			return;
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			// delete entries in ATTRIBUTE
			StringBuffer buf = new StringBuffer();
			buf.append("delete from ATTRIBUTE where PARENT_TYPE='T' and DATAELEM_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD
			HashSet hashSet = new HashSet();
			buf = new StringBuffer();
			buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_TYPE='T' and PARENT_ID=").append(tblID);
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				hashSet.add(rs.getString(1));
			}
			rs.close();
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				buf = new StringBuffer();
				buf.append("delete from COMPLEX_ATTR_FIELD where ROW_ID=").append(i.next());
				stmt.executeUpdate(buf.toString());
				buf = new StringBuffer();
				buf.append("delete from COMPLEX_ATTR_ROW where ROW_ID=").append(i.next());
				stmt.executeUpdate(buf.toString());
			}

			// delete entries in DST2TBL
			buf = new StringBuffer();
			buf.append("delete from DST2TBL where TABLE_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in TBL2ELEM
			buf = new StringBuffer();
			buf.append("delete from TBL2ELEM where TABLE_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in CACHE
			buf = new StringBuffer();
			buf.append("delete from CACHE where OBJ_TYPE='tbl' and OBJ_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in DOC
			buf = new StringBuffer();
			buf.append("delete from DOC where OWNER_TYPE='tbl' and OWNER_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in DS_TABLE
			buf = new StringBuffer();
			buf.append("delete from DS_TABLE where TABLE_ID=").append(tblID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @param elmID
	 */
	private void deleteElm(String elmID) throws Exception{
		
		if (elmID==null)
			return;
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			// delete entries in ATTRIBUTE
			StringBuffer buf = new StringBuffer();
			buf.append("delete from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD
			HashSet hashSet = new HashSet();
			buf = new StringBuffer();
			buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_TYPE='E' and PARENT_ID=").append(elmID);
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				hashSet.add(rs.getString(1));
			}
			rs.close();
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				buf = new StringBuffer();
				buf.append("delete from COMPLEX_ATTR_FIELD where ROW_ID=").append(i.next());
				stmt.executeUpdate(buf.toString());
				buf = new StringBuffer();
				buf.append("delete from COMPLEX_ATTR_ROW where ROW_ID=").append(i.next());
				stmt.executeUpdate(buf.toString());
			}

			// delete entries in TBL2ELEM
			buf = new StringBuffer();
			buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in CACHE
			buf = new StringBuffer();
			buf.append("delete from CACHE where OBJ_TYPE='elm' and OBJ_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in DOC
			buf = new StringBuffer();
			buf.append("delete from DOC where OWNER_TYPE='elm' and OWNER_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());
			
			// delete entries in FK_RELATION
			buf = new StringBuffer();
			buf.append("delete from FK_RELATION where A_ID=").append(elmID).append(" or B_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in FXV
			buf = new StringBuffer();
			buf.append("delete from FXV where OWNER_TYPE='elem' and OWNER_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());

			// delete entries in DATAELEM
			buf = new StringBuffer();
			buf.append("delete from DATAELEM where DATAELEM_ID=").append(elmID);
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void cleanupNamespaces() throws Exception{
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			outputWriteln("");
			
			// delete NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table
			outputWriteln("deleting NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select NAMESPACE.NAMESPACE_ID ").
			append("from NAMESPACE left outer join DATASET on NAMESPACE.NAMESPACE_ID=DATASET.CORRESP_NS ").
			append("left outer join DS_TABLE on NAMESPACE.NAMESPACE_ID=DS_TABLE.CORRESP_NS where ").
			append("DATASET.CORRESP_NS is null and DS_TABLE.CORRESP_NS is null");
			int count = 0;
			String nsID = null;
			HashSet hashSet = new HashSet();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				nsID = rs.getString(1);
				if (!hashSet.contains(nsID)){
					hashSet.add(nsID);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such namespaces found");
			count = 0;			
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				buf = new StringBuffer();
				buf.append("delete from NAMESPACE where NAMESPACE_ID=").append(i.next());
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void cleanupAcls() throws Exception{
		
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			outputWriteln("");
			
			// delete object ACLs of objects that do not actually exist
			outputWriteln("deleting object ACLs of objects that do not actually exist...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select ACL_ID from ACLS left outer join DATASET on ACLS.ACL_NAME=DATASET.IDENTIFIER ").
			append("where ACLS.PARENT_NAME='/datasets' and DATASET.IDENTIFIER is null");
			int count = 0;
			String aclID = null;
			HashSet hashSet = new HashSet();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				aclID = rs.getString(1);
				if (!hashSet.contains(aclID)){
					hashSet.add(aclID);
					count++;
				}
			}
			rs.close();
			buf = new StringBuffer();
			buf.
			append("select ACL_ID from ACLS left outer join DATAELEM on ACLS.ACL_NAME=DATAELEM.IDENTIFIER ").
			append("where ACLS.PARENT_NAME='/elements' and DATAELEM.PARENT_NS is null and DATAELEM.IDENTIFIER is null");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(buf.toString());
			while (rs!=null && rs.next()){
				aclID = rs.getString(1);
				if (!hashSet.contains(aclID)){
					hashSet.add(aclID);
					count++;
				}
			}
			rs.close();
			outputWriteln(count + " such ACLs found");
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				aclID = (String)i.next();
				buf = new StringBuffer();
				buf.append("delete from ACL_ROWS where ACL_ID=").append(aclID);
				stmt.executeUpdate(buf.toString());
				buf = new StringBuffer();
				buf.append("delete from ACLS where ACL_ID=").append(aclID);
				stmt.executeUpdate(buf.toString());
				count++;
			}
			outputWriteln(count + " deleted");
		}
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
	}

	/**
	 * @throws Exception 
	 */
	public void create() throws Exception{
		createTables();
		createNonCommonElements();
		createBooleanFixedValues();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void cleanup() throws Exception{
		cleanupElements();
		cleanupTables();
		cleanupNamespaces();
		cleanupAcls();
	}

	/**
	 * 
	 * @param message
	 */
	public void outputWrite(String message){
		if (outputWriter!=null && message!=null){
			outputWriter.print(message);
			outputWriter.flush();
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void outputWriteln(String message){
		if (outputWriter!=null && message!=null){
			outputWriter.println(message);
			outputWriter.flush();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getTestConnection() throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(
			"jdbc:mysql://192.168.10.15:3306/jaanusdd?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&emptyStringsConvertToZero=false&jdbcCompliantTruncation=false", "dduser", "xxx");		
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
            DataOperations script = new DataOperations(DataOperations.getTestConnection(), new PrintWriter(System.out));
            script.cleanup();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
        }
		finally{
			try{
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}			
		}
	}
}