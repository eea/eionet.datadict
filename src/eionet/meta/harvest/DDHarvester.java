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

public abstract class DDHarvester implements HarvesterIF{
	
	private String harvesterID = null;
	private Connection conn = null;
	private long harvestingTime = 0;
	
	protected LogServiceIF log = null;
	
	protected DDHarvester(String harvesterID){		
		this.harvesterID = harvesterID;
		log = new Log4jLoggerImpl(Props.getProperty(PropsIF.HRV_LOG));		
	}
	
	protected void store(Hashtable hash, String id) throws Exception{
		
		if (hash == null) return;
		Enumeration flds = hash.keys();
		if (flds==null || !flds.hasMoreElements()) return;
		
		getConnection();
		if (conn==null || conn.isClosed())
			throw new Exception("Failed to get the DB connection!");
		
		if (harvesterID==null || harvestingTime==0)
			throw new Exception("Failed to find the harvesting ID and time!");
		
		// store in HARV_ATTR
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("HARV_ATTR");
		
		String[] ids = new String[3];
		ids[0] = id;
		ids[1] = harvesterID;
		ids[2] = String.valueOf(harvestingTime);
		String md5key = getMD5(ids);
		
		ids = new String[2];
		ids[0] = id;
		ids[1] = harvesterID;
		String logID = getMD5(ids);
		
		gen.setField("HARV_ATTR_ID", id);
		gen.setField("HARVESTER_ID", harvesterID);
		gen.setField("HARVESTED", String.valueOf(harvestingTime));
		gen.setFieldExpr("MD5KEY", md5key);
		gen.setFieldExpr("LOGICAL_ID", logID);
		conn.createStatement().executeUpdate(gen.insertStatement());
		
		// store in HARV_ATTR_FIELD
		
		do{ // using a do-while cause hasMoreElements() has been called already
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
				gen.clear();
				gen.setTable("HARV_ATTR_FIELD");
				gen.setFieldExpr("HARV_ATTR_MD5", md5key);						
				gen.setField("FLD_NAME", fldName);
				gen.setField("FLD_VALUE", (String)iter.next());
				conn.createStatement().executeUpdate(gen.insertStatement());
			}
						
		} while (flds.hasMoreElements());
	}
	
	public void harvest() throws Exception{
		this.harvestingTime = System.currentTimeMillis();
		doHarvest();
		cleanup();
	}
	
	protected abstract void doHarvest() throws Exception;
	
	public void cleanup(){
		
		if (conn == null) return;
		
		try{
			String s = "delete from HARV_ATTR where HARVESTER_ID='" +
						this.harvesterID + "' and HARVESTED<" +
						String.valueOf(this.harvestingTime);
			conn.createStatement().executeUpdate(s);
			rmvDeleted();
		} catch (Exception e){
			log.fatal("Failed to delete old harvested attributes", e);
			//System.out.println(e.toString());
		}
		finally{
			try { conn.close(); } catch (SQLException e) {}
		}
	}
	
	public LogServiceIF getLog(){
		return this.log;
	}
	
	private void rmvDeleted() throws Exception{
		
		String q = "select distinct ROW_ID from COMPLEX_ATTR_ROW " +
					"left outer join HARV_ATTR on " +
					"COMPLEX_ATTR_ROW.HARV_ATTR_ID=HARV_ATTR.LOGICAL_ID " +
					"where COMPLEX_ATTR_ROW.HARV_ATTR_ID is not null and " +
					"HARV_ATTR.LOGICAL_ID is null";

		Vector v = new Vector();
		ResultSet rs = conn.createStatement().executeQuery(q);
		while (rs.next()) v.add(rs.getString(1));
		for (int i=0; i<v.size(); i++)
			conn.createStatement().executeUpdate("delete from " +
				"COMPLEX_ATTR_ROW where ROW_ID='" + (String)v.get(i) + "'");
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
	
	private String getMD5(String[] flds){
		StringBuffer buf = new StringBuffer("md5('");
		for (int i=0; i<flds.length; i++)
			buf.append(flds[i]);
		return buf.append("')").toString();
	}
	
	/*
	protected static String getMyName(String className){
		
		String myName = null;
		
		String harvesters = Props.getProperty(PropsIF.HARVESTERS);
		StringTokenizer st = new StringTokenizer(harvesters, ",");
		while (st.hasMoreTokens()) {
			String hrvName = st.nextToken().trim();
			if (hrvName!=null && hrvName.length()!=0){
				String hrvClass = Props.getProperty(PropsIF.HRV_PREFIX +
									hrvName + PropsIF.HRV_CLASS);
				if (hrvClass!=null && hrvClass.equals(className)){
					myName = hrvName;
					break;					
				}
			}
		}

		return myName;
	}*/

	public static void main(String[] args) {
		
		//HarvesterIF harvester = new OrgHarvester();
		DDHarvester harvester = new OrgHarvester();
		
		try{
			//harvester.harvest();
			harvester.getConnection();
			harvester.rmvDeleted();
		}
		catch (Exception e){
			LogServiceIF log = harvester.getLog();
			log.fatal("", e);
			e.printStackTrace(System.out);
			harvester.cleanup();
		}
	}
}
