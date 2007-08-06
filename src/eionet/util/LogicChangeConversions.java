package eionet.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.tee.util.SQLGenerator;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class LogicChangeConversions extends DataManipulations {
	
	/**
	 * 
	 * @param conn
	 * @param outputWriter
	 * @throws IOException
	 */
	public LogicChangeConversions(Connection conn, PrintWriter outputWriter) throws IOException{
		super(conn, outputWriter);
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
	public void createNonCommonElements() throws Exception{

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
	 * Deletes DST2TBL relations where the table has a newer version in the same dataset.
	 * @throws Exception
	 */
	public void deleteExpiredDstToTblRelations() throws Exception{
		ResultSet rs = null;
		Statement stmt = null;		
		try{
			// delete DST2TBL relations where the table has a newer version in the same dataset
			outputWriteln("searching DST2TBL relations where the table has a newer version in the same dataset...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select distinct DST2TBL.DATASET_ID, DS_TABLE.IDENTIFIER, DS_TABLE.TABLE_ID ").
			append("from DS_TABLE left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
			append("order by DST2TBL.DATASET_ID asc, DS_TABLE.IDENTIFIER asc, DS_TABLE.TABLE_ID desc");
			int count = 0;
			HashSet hashSet = new HashSet();
			Hashtable prevOne = null;
			stmt = conn.createStatement();
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
			
			boolean attemptingDelete = false;
			if (count>0){
				attemptingDelete = true;
				outputWriteln(count + " such relations found, now deleting them...");
			}
			else
				outputWriteln(count + " such relations found");
			
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from DST2TBL where DATASET_ID=").append(hash.get("DATASET_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			
			if (attemptingDelete)
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
	 * Cleans up the database for migration from the old versioning logic to the new one.
	 * <b>Do not use this method if the database contents already follow the new versioning logic!</b>
	 * @throws Exception
	 */
	public void cleanup() throws Exception{
		
		// cleanup elements
		deleteBrokenTblToElmRelations();
		deleteExpiredTblToElmRelations();
		deleteOrphanNonCommonElements();
		
		// cleanup tables
		deleteBrokenDstToTblRelations();
		deleteExpiredDstToTblRelations();
		deleteOrphanTables();
		
		// cleanup namespaces
		deleteOrphanNamespaces();
		
		// cleanup acls
		deleteOrphanAcls();
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
	 * Deletes TBL2ELEM relations where the non-common element has a newer version in the same table.
	 * @throws Exception
	 */
	public void deleteExpiredTblToElmRelations() throws Exception{
		
		ResultSet rs = null;
		Statement stmt = null;
		try{
			// delete TBL2ELEM relations where the non-common element has a newer version in the same table
			outputWriteln("searching TBL2ELEM relations where the non-common element has a newer version in the same table...");
			StringBuffer buf = new StringBuffer();
			buf.
			append("select distinct TBL2ELEM.TABLE_ID, DATAELEM.IDENTIFIER, DATAELEM.DATAELEM_ID ").
			append("from DATAELEM left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
			append("where DATAELEM.PARENT_NS is not null ").
			append("order by TBL2ELEM.TABLE_ID asc, DATAELEM.IDENTIFIER asc, DATAELEM.DATAELEM_ID desc");
			int count = 0;
			HashSet hashSet = new HashSet();
			Hashtable prevOne = null;
			stmt = conn.createStatement();
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
			
			boolean attemptingDelete = false;
			if (count>0){
				attemptingDelete = true;
				outputWriteln(count + " such relations found, now deleting them...");
			}
			else
				outputWriteln(count + " such relations found");
			
			count = 0;
			for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
				Hashtable hash = (Hashtable)i.next();
				buf = new StringBuffer();
				buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(hash.get("DATAELEM_ID")).
				append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
				stmt.executeUpdate(buf.toString());
				count++;
			}
			
			if (attemptingDelete)
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
}
