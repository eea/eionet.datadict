package eionet.meta;

import java.sql.*;
import java.util.*;

import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;

import eionet.meta.savers.*;

public class Restorer {
	
	private Connection conn = null;
	private DDSearchEngine searchEngine = null;
	private AppUserIF user = null;
	
	private Vector dsts = new Vector();
	private Vector tbls = new Vector();
	private Vector elms = new Vector();
	private Vector nss = new Vector();
	
	/**
	 * 
	 */
	public Restorer(Connection conn){
		this.conn = conn;
		this.searchEngine = new DDSearchEngine(conn);  
	}
	
	public void setUser(AppUserIF user){
		this.user = user;
	}
	
	public void restoreDst(String dstID) throws Exception{
		try{
			_restoreDst(dstID);
		}
		catch (Exception e){
			try{cleanup();} catch (Exception ee){}
			throw e;
		}
	}
	
	public String restoreTbl(String tblID) throws Exception{
		try{
			return _restoreTbl(tblID);
		}
		catch (Exception e){
			try{cleanup();} catch (Exception ee){}
			throw e;
		}
	}
	
	public String restoreElm(String elmID) throws Exception{
		try{
			return _restoreElm(elmID);
		}
		catch (Exception e){
			try{cleanup();} catch (Exception ee){}
			throw e;
		}
	}

	private void _restoreDst(String dstID) throws Exception{
	}
	
	private String _restoreTbl(String tblID) throws Exception{
		
		if (false) return tblID;
		
		// make sure the table has been deleted
		if (!searchEngine.isTblDeleted(tblID))
			throw new Exception("No point in restoring a non-deleted table!");
        
		// get the table's latest dataset
		String latestDstID = getLatestDst(tblID);
		if (Util.nullString(latestDstID))
			throw new Exception("Could not find the latest dataset!");
        
		// LOCK THE PARENT NAMESPACE
		lockParentNs(tblID);
        
		// copy the latest dataset
		String newDstID = copyDst(latestDstID);

		// copy the table
		String newTblID = copyTbl(tblID);
		
		// add the new elm to the new tbl
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DST2TBL");
		gen.setFieldExpr("TABLE_ID", newTblID);
		gen.setFieldExpr("DATASET_ID", newDstID); 
		conn.createStatement().executeUpdate(gen.insertStatement());
		
		// UNLOCK THE TOP NAMESPACE
		unlockNss();

		// all fine, return
		return newTblID;
	}
	
	private String _restoreElm(String elmID) throws Exception{
		
		if (false) return elmID;
		
		// make sure the element has been deleted
		if (!searchEngine.isElmDeleted(elmID))
			throw new Exception("No point in restoring a non-deleted element!");
        
		// get the element's latest table
		String latestTblID = getLatestTbl(elmID);
		if (Util.nullString(latestTblID))
			throw new Exception("Could not find the latest table!");
        
		// make sure the element's latest table has not been deleted
		if (searchEngine.isTblDeleted(latestTblID))
			throw new Exception("This element's parent table has been deleted!"+
									" You must restore it first!");
        
        // We are now sure that the latest dataset contains the latest table.
        // And we always restore into the latest non-deleted parent
        
		// LOCK THE TOP NAMESPACE
		lockTopNs(elmID);
        
		// copy the latest dataset
		String newDstID = copyDst(getLatestDst(latestTblID));

		// copy the latest table
		// this will also insert the new tbl into the new dataset
		// (because the previous table is already there by copyDst())  
		String newTblID = copyTbl(latestTblID);
		
		// remove the previous table from the new dataset
		// (because it was put there by copyDst()) 
		conn.createStatement().executeUpdate("delete from DST2TBL where " +
			"DATASET_ID=" + newDstID + " and TABLE_ID=" + latestTblID);
		
		// copy the element
		String newElmID = copyElm(elmID);
		
		// add the new elm to the new tbl
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("TBL2ELEM");
		gen.setFieldExpr("DATAELEM_ID", newElmID);
		gen.setFieldExpr("TABLE_ID", newTblID); 
		conn.createStatement().executeUpdate(gen.insertStatement());
		
		// UNLOCK THE TOP NAMESPACE
		unlockNss();

		// all fine, return
		return newElmID;
	}
	
	private String copyDst(String dstID) throws Exception {
		
		if (Util.nullString(dstID))
			throw new Exception("Failed to find the dataset!");

		// get the maximum version of such dataset
		String maxVersion = maxDstVersion(dstID);
		if (Util.nullString(maxVersion))
			throw new Exception("Failed to get the dataset's maximum version!"); 
		
		// now copy the dataset	
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newDstID = copyHandler.copyDst(dstID, false, true, false);
		if (Util.nullString(newDstID))
			throw new Exception("Failed to copy the dataset!");
		else
			this.dsts.add(newDstID);

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		gen.setFieldExpr("VERSION", maxVersion + "+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		conn.createStatement().executeUpdate(gen.updateStatement() + 
									" where DATASET_ID=" + newDstID);
		
		return newDstID;
	}
	
	private String copyTbl(String tblID) throws Exception{
		
		if (Util.nullString(tblID))
			throw new Exception("Failed to find the table!");

		// get the table's maximum version
		String maxVersion = maxTblVersion(tblID);
		if (Util.nullString(maxVersion))
			throw new Exception("Failed to get the table's maximum version!");
		
		// now copy the table
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newTblID = copyHandler.copyTbl(tblID, false, true);
		if (Util.nullString(newTblID))
			throw new Exception("Failed to copy the table!");
		else
			this.tbls.add(newTblID);

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DS_TABLE");
		gen.setFieldExpr("VERSION", maxVersion + "+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		conn.createStatement().executeUpdate(gen.updateStatement() + 
									" where TABLE_ID=" + newTblID);
		
		return newTblID;
	}
	
	private String copyElm(String elmID) throws Exception{
		
		if (Util.nullString(elmID))
			throw new Exception("Failed to find the element!");

		// get the element's maximum version
		String maxVersion = maxElmVersion(elmID);
		if (Util.nullString(maxVersion))
			throw new Exception("Failed to get the element's maximum version!");

		// now copy the element
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newElmID = copyHandler.copyElem(elmID, true);
		if (Util.nullString(newElmID))
			throw new Exception("Failed to copy the element!");
		else
			this.elms.add(newElmID);

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATAELEM");
		gen.setFieldExpr("VERSION", maxVersion + "+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		conn.createStatement().executeUpdate(gen.updateStatement() + 
									" where DATAELEM_ID=" + newElmID);
		return newElmID;
	}
	
	private String lockTopNs(String elmID) throws Exception{
		
		String q = "select TOP_NS from DATAELEM where DATAELEM_ID=" + elmID;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		if (rs.next())
			return lockNamespace(rs.getString(1));
		else
			return null;
	}

	private String lockParentNs(String tblID) throws Exception{
		String q = "select PARENT_NS from DS_TABLE where TABLE_ID=" + tblID;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		if (rs.next())
			return lockNamespace(rs.getString(1));
		else
			return null;
	}
	
	private String lockNamespace(String nsID) throws Exception{
		
		if (nsID==null) return null;
		
		if (user==null)
			throw new Exception("User not specified!");
					
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("NAMESPACE");
		gen.setField("WORKING_USER", user.getUserName());
		conn.createStatement().executeUpdate(gen.updateStatement() +
						" where NAMESPACE_ID=" + nsID);
		nss.add(nsID);
		return nsID;
	}

	private void unlockNss() throws Exception{
		
		if (nss.size()==0) return;
		
		Statement stmt = conn.createStatement();
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("NAMESPACE");
		gen.setFieldExpr("WORKING_USER", "NULL");
		for (int i=0; i<nss.size(); i++){
			stmt.executeUpdate(gen.updateStatement() +
					" where NAMESPACE_ID=" + (String)nss.get(i)); 
		}
	}
	
	private void cleanup() throws Exception {
		unlockNss();
		cleanDsts();
		cleanTbls();
		cleanElms();
	}
	
	private void cleanDsts() throws Exception {
		
		Parameters pars = new Parameters();
		pars.addParameterValue("mode", "delete");
		pars.addParameterValue("complete", "true");		
		for (int i=0; i<dsts.size(); i++)
			pars.addParameterValue("ds_id", (String)dsts.get(i));
		
		DatasetHandler handler = new DatasetHandler(conn, pars, null);
		handler.setUser(user);
		handler.setVersioning(false);
		handler.execute();
	}

	private void cleanTbls() throws Exception {
		
		Parameters pars = new Parameters();
		pars.addParameterValue("mode", "delete");
		pars.addParameterValue("complete", "true");		
		for (int i=0; i<tbls.size(); i++)
			pars.addParameterValue("del_id", (String)tbls.get(i));
		
		DatasetHandler handler = new DatasetHandler(conn, pars, null);
		handler.setUser(user);
		handler.setVersioning(false);
		handler.execute();
	}

	private void cleanElms() throws Exception {
		
		Parameters pars = new Parameters();
		pars.addParameterValue("mode", "delete");
		pars.addParameterValue("complete", "true");		
		for (int i=0; i<elms.size(); i++)
			pars.addParameterValue("delem_id", (String)elms.get(i));
		
		DataElementHandler handler = new DataElementHandler(conn, pars, null);
		handler.setUser(user);
		handler.setVersioning(false);
		handler.execute();
	}
	
	private String getLatestTbl(String elmID) throws Exception{
		
		StringBuffer buf = new StringBuffer("select distinct DS_TABLE.TABLE_ID ");
		buf.append("from DATAELEM, DS_TABLE ");
		buf.append("left outer join DST2TBL on ");
		buf.append("DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ");
		buf.append("left outer join DATASET on ");
		buf.append("DST2TBL.DATASET_ID=DATASET.DATASET_ID ");
		buf.append("where DATAELEM.PARENT_NS=DS_TABLE.CORRESP_NS and ");
		buf.append("DATASET.DELETED is null and ");
		buf.append("DATAELEM.DATAELEM_ID=");
		buf.append(elmID);
		buf.append(" order by DS_TABLE.VERSION desc");
		
		ResultSet rs = conn.createStatement().executeQuery(buf.toString());
		if (rs.next())
			return rs.getString(1);
		
		return null;
	}
	
	private String getLatestDst(String tblID) throws Exception{
		
		StringBuffer buf = new StringBuffer("select distinct DATASET.DATASET_ID ");
		buf.append("from DS_TABLE, DATASET ");
		buf.append("where DS_TABLE.PARENT_NS=DATASET.CORRESP_NS and ");
		buf.append("DS_TABLE.TABLE_ID=");
		buf.append(tblID);
		buf.append(" and DATASET.DELETED is null ");
		buf.append(" order by DATASET.VERSION desc");

		ResultSet rs = conn.createStatement().executeQuery(buf.toString());
		if (rs.next())
			return rs.getString(1);
					
		return null;
	}
	
	private String getElmTopNs(String elmID) throws Exception{
		
		String q = "select TOP_NS from DATAELEM where DATAELEM_ID=" + elmID;
		ResultSet rs = conn.createStatement().executeQuery(q);
		if (rs.next())
			return rs.getString(1);

		return null;
	}

	private String getTblTopNs(String tblID) throws Exception{
		
		String q = "select PARENT_NS from DS_TABLE where TABLE_ID=" + tblID;
		ResultSet rs = conn.createStatement().executeQuery(q);
		if (rs.next())
			return rs.getString(1);

		return null;
	}
	
	private String maxDstVersion(String dstID) throws SQLException{
		
		if (Util.nullString(dstID)) return null;
		
		String correspNs = null;
		Statement stmt = conn.createStatement();
		String q = "select CORRESP_NS from DATASET where DATASET_ID=" + dstID;
		ResultSet rs = stmt.executeQuery(q);		
		if (rs.next())
			correspNs = rs.getString(1);
		
		if (Util.nullString(correspNs)) return null;
		
		q = "select max(VERSION) from DATASET where CORRESP_NS=" + correspNs;		
		rs = stmt.executeQuery(q);
		if (rs.next())
			return rs.getString(1);
		else
			return null;
	}
	
	private String maxTblVersion(String tblID) throws SQLException{
	
		if (Util.nullString(tblID)) return null;
		
		String correspNs = null;
		Statement stmt = conn.createStatement();
		String q = "select CORRESP_NS from DS_TABLE where TABLE_ID=" + tblID;
		ResultSet rs = stmt.executeQuery(q);		
		if (rs.next())
			correspNs = rs.getString(1);
		
		if (Util.nullString(correspNs)) return null;
		
		q = "select max(VERSION) from DS_TABLE where CORRESP_NS=" + correspNs;		
		rs = stmt.executeQuery(q);
		if (rs.next())
			return rs.getString(1);
		else
			return null;
	}
	
	private String maxElmVersion(String elmID) throws SQLException{
		
		if (Util.nullString(elmID)) return null;
		
		String idfier = null;
		String parentNs = null;
		Statement stmt = conn.createStatement();
		String q = "select IDENTIFIER, PARENT_NS from DATAELEM where " +
					"DATAELEM_ID=" + elmID;
		ResultSet rs = stmt.executeQuery(q);		
		if (rs.next()){
			idfier = rs.getString("IDENTIFIER");
			parentNs  = rs.getString("PARENT_NS");
		}
		
		if (Util.nullString(idfier) || Util.nullString(parentNs)) return null;
		
		q = "select max(VERSION) from DATAELEM where IDENTIFIER="
				+ Util.strLiteral(idfier) + " and PARENT_NS=" + parentNs;		
		rs = stmt.executeQuery(q);
		if (rs.next())
			return rs.getString(1);
		else
			return null;
	}
	
	public static void main(String[] args){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(
				"jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

			AppUserIF testUser = new TestUser();
			testUser.authenticate("jaanus", "jaanus");
			
			Restorer restorer = new Restorer(conn);
			restorer.setUser(testUser);
			//String s1 = restorer.maxDstVersion("1433");
			String s2 = restorer.maxElmVersion("11786");
			//String s3 = restorer.maxElmVersion("12165");
			System.out.println(s2 + "+1");
			//restorer.restoreElm("12150");
			//restorer.restoreTbl("2263");
		}
		catch (Exception e){
			System.out.println(e.toString());
		}
	}
}
