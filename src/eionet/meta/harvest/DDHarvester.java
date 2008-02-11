/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is "EINRC-6 / Data Dictionary Project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2002 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Jaanus Heinlaid (TietoEnator)
 */

package eionet.meta.harvest;

import java.sql.*;
import java.util.*;
import com.tee.util.*;

import eionet.util.*;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public abstract class DDHarvester implements HarvesterIF{
	
	private String harvesterID = null;
	private Connection conn = null;
	private long harvestingTime = 0;
	
	protected LogServiceIF log = null;
	
	protected DDHarvester(String harvesterID){		
		this.harvesterID = harvesterID;
		log = new Log4jLoggerImpl(Props.getProperty(PropsIF.HRV_LOG));		
	}

	/**
	 * 
	 * @param hash
	 * @param id
	 * @throws Exception
	 */
	protected void store(Hashtable hash, String id) throws Exception{
		
		if (hash == null) return;
		Enumeration flds = hash.keys();
		if (flds==null || !flds.hasMoreElements()) return;
		
		if (harvesterID==null || harvestingTime==0)
			throw new Exception("Failed to find the harvesting ID and time!");
		
		getConnection();
		if (conn==null || conn.isClosed())
			throw new Exception("Failed to get the DB connection!");
		
		PreparedStatement stmt = null;
		try{
			// store in HARV_ATTR
			String[] ids = new String[3];
			ids[0] = id;
			ids[1] = harvesterID;
			ids[2] = String.valueOf(harvestingTime);
			String md5key = getMD5(ids);
			
			ids = new String[2];
			ids[0] = id;
			ids[1] = harvesterID;
			String logID = getMD5(ids);
			
			INParameters inParams = new INParameters();
			LinkedHashMap map = new LinkedHashMap();
			map.put("HARV_ATTR_ID", inParams.add(id));
			map.put("HARVESTER_ID", inParams.add(harvesterID));
			map.put("HARVESTED", inParams.add(String.valueOf(harvestingTime), Types.BIGINT));
			map.put("MD5KEY", inParams.add(md5key));
			map.put("LOGICAL_ID", inParams.add(logID));
			
			stmt = SQL.preparedStatement(SQL.insertStatement("HARV_ATTR", map), inParams, conn);
			stmt.executeUpdate();
			stmt.close();
			
			// store in HARV_ATTR_FIELD (using a do-while cause hasMoreElements() has been called already)
			do{
				String fldName  = (String)flds.nextElement();
				HashSet fldValues = new HashSet();
							
				Object o = hash.get(fldName);
				if (o==null) o = "";
				
				if (o.getClass().getName().endsWith("Vector")){
					for (int i=0; i<((Vector)o).size(); i++)
						fldValues.add(((Vector)o).get(i));						
				}
				else if (o.getClass().getName().endsWith("String"))
					fldValues.add(o);
				else
					continue; //FIX ME! should through an exception
				
				Iterator iter = fldValues.iterator();
				while (iter.hasNext()){
					inParams = new INParameters();
					map = new LinkedHashMap();
					map.put("HARV_ATTR_MD5", inParams.add(md5key));
					map.put("FLD_NAME", inParams.add(fldName));
					map.put("FLD_VALUE", inParams.add((String)iter.next()));
					stmt = SQL.preparedStatement(SQL.insertStatement("HARV_ATTR_FIELD", map), inParams, conn);
					stmt.executeUpdate();
				}
							
			}
			while (flds.hasMoreElements());
		}
		finally{
			try{
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			}
			catch (SQLException e){}
		}
	}
	
	/**
	 * 
	 */
	public void harvest() throws Exception{
		this.harvestingTime = System.currentTimeMillis();
		doHarvest();
		cleanup();
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected abstract void doHarvest() throws Exception;
	
	/**
	 * 
	 */
	public void cleanup(){
		
		if (conn==null)
			return;

		PreparedStatement stmt = null;
		try{
			INParameters inParams = new INParameters();
			StringBuffer buf = new StringBuffer("delete from HARV_ATTR where HARVESTER_ID=");
			buf.append(inParams.add(harvesterID)).append(" and HARVESTED<").append(inParams.add(String.valueOf(harvestingTime), Types.BIGINT));
			stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
			stmt.executeUpdate();
			
			rmvDeleted(conn);
		}
		catch (Exception e){
			log.fatal("Failed to delete old harvested attributes", e);
		}
		finally{
			try {
				if (stmt!=null) stmt.close();
				if (conn!=null) conn.close();
			}
			catch (SQLException e) {}
		}
	}
	
	public LogServiceIF getLog(){
		return this.log;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	protected static void rmvDeleted(Connection conn) throws Exception{
		
		String q = "select distinct ROW_ID from COMPLEX_ATTR_ROW " +
					"left outer join HARV_ATTR on " +
					"COMPLEX_ATTR_ROW.HARV_ATTR_ID=HARV_ATTR.LOGICAL_ID " +
					"where COMPLEX_ATTR_ROW.HARV_ATTR_ID is not null and " +
					"HARV_ATTR.LOGICAL_ID is null";

		Vector v = new Vector();
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try{
			stmt = conn.prepareStatement(q);
			rs = stmt.executeQuery();
			while (rs.next()){
				v.add(rs.getString(1));
			}
						
			for (int i=0; i<v.size(); i++){
				INParameters inParams = new INParameters();
				StringBuffer buf = new StringBuffer("delete from COMPLEX_ATTR_ROW where ROW_ID=");
				buf.append(inParams.add((String)v.get(i)));
				stmt.close();
				stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
				stmt.executeUpdate();
			}
		}
		finally{
			try{
				if (rs!=null) rs.close();
			}
			catch(SQLException e){}
		}
	}
	
	private void getConnection() throws Exception{
		if (conn==null || conn.isClosed()){			
			Class.forName(Props.getProperty(PropsIF.DBDRV));
			this.conn = DriverManager.getConnection(
				Props.getProperty(PropsIF.DBURL),
				Props.getProperty(PropsIF.DBUSR),
				Props.getProperty(PropsIF.DBPSW));
		}
	}

	private void closeConnection(){
		try{
			if (conn!=null) conn.close();
		}
		catch (SQLException e){}
	}
	
	private String getMD5(String[] flds){
		StringBuffer buf = new StringBuffer("md5('");
		for (int i=0; i<flds.length; i++)
			buf.append(flds[i]);
		return buf.append("')").toString();
	}
}
