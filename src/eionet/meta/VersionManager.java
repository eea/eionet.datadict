
package eionet.meta;

import java.sql.*;
import java.util.*;
import javax.servlet.ServletContext;

import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;

import eionet.meta.savers.*;

import com.tee.uit.security.*;
import eionet.util.SecurityUtil;

public class VersionManager{
    
    private Connection conn = null;
    private DDSearchEngine searchEngine = null;
    private AppUserIF user = null;
    private boolean upwardsVersioning = true;
    
    /** possible registration statuses*/
    private Hashtable regStatuses = new Hashtable();
    private Vector regStatusesOrdered = new Vector();
    
    /** version-effecting attributes */
    private HashSet compAttrs = new HashSet();
    
	/** servlet context object if instatiated from servlet environment*/
	private ServletContext ctx = null;
    
    /**
    *
    */
    public VersionManager(Connection conn, AppUserIF user){
        this(conn, new DDSearchEngine(conn), user);
    }
    
    /**
    *
    */
    public VersionManager(Connection conn, DDSearchEngine searchEngine,
                                AppUserIF user){
        this();
        this.conn = conn;
        this.user = user;
        this.searchEngine = searchEngine;
    }
    
    /**
    *
    */
    public VersionManager(){
        
        // init compAttrs
        compAttrs.add("ShortDescription");
        compAttrs.add("Definition");
        compAttrs.add("Descriptipon of Use");
        compAttrs.add("Methodology");
        compAttrs.add("Datatype");
        compAttrs.add("MinSize");
        compAttrs.add("MaxSize");
        compAttrs.add("Decimal precision");
        compAttrs.add("Unit");
        compAttrs.add("MinValue");
        compAttrs.add("MaxValue");
        compAttrs.add("Planned Upd Frequency");
        
		// init registration statuses vector
		regStatusesOrdered.add("Incomplete");
		regStatusesOrdered.add("Candidate");
		regStatusesOrdered.add("Recorded");
		regStatusesOrdered.add("Qualified");
		regStatusesOrdered.add("Released");
		
        // init registration statuses hashtable
        regStatuses.put("Incomplete", "true");
        regStatuses.put("Candidate", "false");
        regStatuses.put("Recorded", "true");
        regStatuses.put("Qualified", "true");
        regStatuses.put("Released", "true");
    }
    
    public void setUpwardsVersioning(boolean f){
    	this.upwardsVersioning = f;
    }
    
    /**
     * See if the specified object type has the specified short name
     * in the specified namespace (or dataset if object type is "tbl")
     * checked out.
     * If yes then return the name of the user who checked it out.
     * Otherwise return null.
     * 
     * @param   type    object type ("elm", "tbl" or "dst")
     * @param   ctxID    namespace id if type=="elm",
     *                   dataset id if type=="tbl",
     *                   ignored if type=="dst"
     * @param   shortName
     *
     * @return  the name of the working user or null if it's missing
     * @exception   SQLException
     */
    public String getWorkingUser(String ctxID, String shortName, String type)
        throws SQLException {
        
        String tblName = null;
        if (type.equals("elm"))
            tblName = "DATAELEM";
        else if (type.equals("tbl"))
            tblName = "DS_TABLE";
        else if (type.equals("dst"))
            tblName = "DATASET";
        else
            throw new SQLException("Unknown type");
        
        String ctxField = null;
        if (type.equals("elm") || type.equals("tbl"))
            ctxField = "PARENT_NS";
        
        String qry = "select distinct WORKING_USER, VERSION from " + tblName +
        " where SHORT_NAME='" + shortName + "'";
        if (ctxField!=null)
            qry = qry + " and " + ctxField + "=" + ctxID;
        
        qry = qry + " and " + tblName + ".WORKING_COPY='Y'";
        qry = qry + " order by VERSION desc";

        ResultSet rs = conn.createStatement().executeQuery(qry);
        if (rs.next())
            return rs.getString("WORKING_USER");
        else
            return null;
    }
    
    /**
    *
    */
    public String getWorkingUser(String nsID) throws SQLException {
        
        if (nsID==null)
            return null;
        
        String s =
        "select WORKING_USER from NAMESPACE where NAMESPACE_ID=" + nsID;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        if (rs.next())
            return rs.getString(1);
        else
            return null;
    }
    
    /**
    *
    */
    public String getTblWorkingUser(String tblName, String parentNs)
        throws SQLException {
            
        String q =
        "select distinct DS_TABLE.WORKING_USER from DS_TABLE " +
        "where SHORT_NAME=" + com.tee.util.Util.strLiteral(tblName) +
        " and WORKING_COPY='Y'";
        
        if (parentNs!=null)
            q = q + " and PARENT_NS=" + parentNs;
        else
            q = q + " and PARENT_NS is null";
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next())
            return rs.getString("WORKING_USER");
        else
            return null;
    }
    
    /**
    *
    */
    public String getDstWorkingUser(String shortName)
        throws SQLException {
        
        String q = "select distinct DATASET.WORKING_USER from DATASET " +
        "where DATASET.SHORT_NAME='" + shortName + "' and " +
        "DATASET.WORKING_COPY='Y'";
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next())
            return rs.getString("WORKING_USER");
        else
            return null;
    }
    
    /**
    *
    */
    public String getWorkingCopyID(DataElement elm) throws SQLException {
        String q =
        "select DATAELEM_ID from DATAELEM where WORKING_COPY='Y' and " +
        "PARENT_NS=" + elm.getNamespace().getID() + " and " +
        "SHORT_NAME='" + elm.getShortName() + "'";
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next()){
            return rs.getString(1);
        }
        
        return null;
    }
    
    /**
    *
    */
    public String getWorkingCopyID(DsTable tbl) throws SQLException {
        
        String q =
        "select distinct TABLE_ID from DS_TABLE " +
        "where WORKING_COPY='Y' and " +
        "SHORT_NAME='" + tbl.getShortName() + "' and " +
        "PARENT_NS=" + tbl.getParentNs();
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next()){
            return rs.getString(1);
        }
        
        return null;
    }
    
    /**
    *
    */
    public String getWorkingCopyID(Dataset dst) throws SQLException {
        String q =
        "select distinct DATASET_ID from DATASET " +
        "where WORKING_COPY='Y' and " +
        "SHORT_NAME='" + dst.getShortName() + "'";
        
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next()){
            return rs.getString(1);
        }
        
        return null;
    }
    
    /**
     * Check out the specified object. Meaning a working copy of the
     * object will be created.
     * 
     * @param   id    object id.
     * @param   type  object type (one of "elm", "tbl" or "dst")
     * @return  id of the working copy
     * @exception   Exception
     */
    public String checkOut(String id, String type) throws Exception{
    	
        if (id==null || type==null)
            throw
            new Exception("Unable to locate the object to check out!");
            
        if (type.equals("elm"))
            return checkOutElm(id);
        else if (type.equals("tbl"))
            return checkOutTbl(id);
        else if (type.equals("dst"))
            return checkOutDst(id);
        else
            throw new Exception("Unknown object type!");
    }
    
    /**
     * Check out the specified data element.
     * 
     * @param   elmID    element id.
     * @return  id of the working copy
     * @exception   Exception
     */
    private String checkOutElm(String elmID) throws Exception{
        
        if (user==null || !user.isAuthentic())
            throw new Exception("Check-out attempt by an unauthorized user!");
        
        String userName = user.getUserName();
        
        // set the original's WORKING_USER
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("WORKING_USER", userName);
        String q = gen.updateStatement() + " where DATAELEM_ID=" + elmID;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(q);
        
        // set the WORKING_USER of top namespace
        q = "select TOP_NS from DATAELEM where DATAELEM_ID=" + elmID;
        ResultSet rs = stmt.executeQuery(q);
        String topNS = rs.next() ? rs.getString(1) : null;
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setField("WORKING_USER", userName);
            q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
            stmt.executeUpdate(q);
        }
        
		// copy element
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newID = copyHandler.copyElem(elmID, true, true);
        
        return newID;
    }
    
    /**
     * Check out the specified table.
     * 
     * @param   tblID    table id.
     * @return  id of the working copy
     * @exception   Exception
     */
    private String checkOutTbl(String tblID) throws Exception{
        
        if (user==null || !user.isAuthentic())
            throw new Exception("Check-out attempt by an unauthorized user!");
        
        // set the original's WORKING_USER
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("WORKING_USER", user.getUserName());
        String q = gen.updateStatement() + " where TABLE_ID=" + tblID;
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(q);
        
        // set the WORKING_USER of top namespace
        q = "select PARENT_NS from DS_TABLE where TABLE_ID=" + tblID;
        ResultSet rs = stmt.executeQuery(q);
        String topNS = rs.next() ? rs.getString(1) : null;
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setField("WORKING_USER", user.getUserName());
            q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
            stmt.executeUpdate(q);
        }
        
        // copy table
        CopyHandler copyHandler = new CopyHandler(conn);
        copyHandler.setUser(user);
        String newID = copyHandler.copyTbl(tblID, true, true);
        
        return newID;
    }
    
    /**
     * Check out the specified dataset.
     * 
     * @param   dstID    dataset id.
     * @return  id of the working copy
     * @exception   Exception
     */
    private String checkOutDst(String dstID) throws Exception{
    	
    	if (dstID==null) throw new Exception("Dataset ID missing!"); 
    	
		// set the working user of the original
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		gen.setField("WORKING_USER", user.getUserName());
		conn.createStatement().executeUpdate(gen.updateStatement() +
					" where DATASET_ID=" + dstID);
		
		// set the WORKING_USER of top namespace
        String q = "select CORRESP_NS from DATASET where DATASET_ID=" + dstID;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        String topNS = rs.next() ? rs.getString(1) : null;
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setField("WORKING_USER", user.getUserName());
            q = gen.updateStatement() + " where NAMESPACE_ID=" + topNS;
            stmt.executeUpdate(q);
        }
        
		// copy the dataset
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newID = copyHandler.copyDst(dstID, true, true, false);
		
		return newID;
    }
    
    /**
     * Check in the specified object.
     *
     * @param   objID    object id
     * @param   objType    object type ("elm", "tbl" or "dst")
     * @param   status  registration status where the user wants
     *                  the checked-in object to be. The method
     *                  checks if the user has filled all the
     *                  requirements of that status. If not, an
     *                  exception with a proper message is thrown.
     * @exception   Exception
     */
    public boolean checkIn(String objID, String objType, String status)
        throws Exception {
            
        if (objID==null || objType==null)
            throw
            new Exception("Unable to locate the object to check out!");

		// JH 031203
		// check if this is really a working copy.
		// it might not be if the user has tried to cancel a previous
		// check-in with the browser's STOP button
		
		if (!searchEngine.isWorkingCopy(objID, objType))
			throw new Exception("Trying to check in a non-working copy!");
		
        if (objType.equals("elm"))
            return checkInElm(objID, status);
        else if (objType.equals("tbl"))
            return checkInTbl(objID, status);
        else if (objType.equals("dst"))
            return checkInDst(objID, status);
        else
            throw new Exception("Unknown object type!");
    }
    
    /**
     * Check in the specified element.
     */
    private boolean checkInElm(String elmID, String status) throws Exception{
        
        // load the element we need to check in
        DataElement elm = loadElm(elmID);
        
        // check if the status satisfies the requirements
        checkStatusRequirements(elm, status);
        
        String newVersion = null;
        DataElement latestElm = null;
        
		String latestID = getLatestElmID(elm);
		if (latestID==null || latestID.equals(elm.getID()))
			newVersion = composeNewVersion(null);
		else
			latestElm = loadElm(latestID);
        
        if (newVersion==null){
			if (requiresVersioning(status) ||
				(latestElm!=null &&
				!latestElm.getStatus().equals(elm.getStatus())))
					newVersion = newVersion(elm, latestElm);
			else
				newVersion = elm.getVersion();
        }
        
        // if the table was empty before this element's check-in,
        // there will be no upwards versiongin done
        boolean tblEmpty = tblEmpty(elm.getTableID(), true);
        
        // update from working copy to acting copy
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("WORKING_COPY", "N");
        gen.setFieldExpr("WORKING_USER", "NULL");
        gen.setField("VERSION", newVersion);
        
        // set the fields for keeping history info
        gen.setField("USER", user.getUserName());
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.updateStatement() +
                                " where DATAELEM_ID=" + elmID);
        
        // If the version wasn't updated (meaning non-effective attributes were
        // changed), the original has to be deleted, otherwise set
        // to WORKING_USER=NULL.
        // This is no problem in a situation when the user updates status from
        // no-version-requiring to version-requiring, because status is not
        // part of unique ID, but the version however is. It is the new version
        // that creates a new element. In other cases the previous latest has
        // to be deleted. Status only effects if version is updated at all.
        
        if (latestElm!=null){
            if (newVersion.equals(elm.getVersion())){
                // delete the previous copy                
                Parameters params = new Parameters();
                params.addParameterValue("mode", "delete");
                params.addParameterValue("delem_id", latestElm.getID());
                DataElementHandler delemHandler =
                    new DataElementHandler(conn, params, ctx);
                delemHandler.setUser(user);
                delemHandler.setVersioning(false);
                delemHandler.execute();
            }
            else{
                gen.clear();
                gen.setTable("DATAELEM");
                gen.setFieldExpr("WORKING_USER", "NULL");
                stmt.executeUpdate(gen.updateStatement() +
                        " where DATAELEM_ID=" + latestElm.getID());

                // update the parent table
                versionUpwards(elm, latestElm.getID());
            }
        }
        else
            versionUpwards(elm, null);
        
        // release the top namespace
        String topNS = elm.getTopNs();
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setFieldExpr("WORKING_USER", "NULL");
            stmt.executeUpdate(gen.updateStatement() +
                    " where NAMESPACE_ID=" + topNS);
        }
        
        return true;
    }
    
    /**
     * Check in the specified table.
     */
    private boolean checkInTbl(String tblID, String status) throws Exception{
        
        // load the table we need to check in
        DsTable tbl = loadTbl(tblID);
        
        // check if the status satisfies the requirements
        checkStatusRequirements(tbl, status);
        
        String newVersion = null;
        DsTable latestTbl = null;
        
		String latestID = getLatestTblID(tbl);
		if (latestID==null || latestID.equals(tbl.getID()))
			newVersion = composeNewVersion(null);
		else
			latestTbl = loadTbl(latestID);
        
		if (newVersion==null){
			if (requiresVersioning(status) ||
				(latestTbl!=null &&
				!latestTbl.getStatus().equals(tbl.getStatus())))
					newVersion = newVersion(tbl, latestTbl);
			else
				newVersion = tbl.getVersion();
		}
        
        // if the dataset was empty before this table's check-in,
        // there will be no upwards versiongin done
        boolean dstEmpty = dstEmpty(tbl.getDatasetID(), true);
        
        // update from working copy to acting copy
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("WORKING_COPY", "N");
        gen.setFieldExpr("WORKING_USER", "NULL");
        gen.setField("VERSION", newVersion);
        
        // set the fields for keeping history info
        gen.setField("USER", user.getUserName());
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.updateStatement() +
                                " where TABLE_ID=" + tblID);
        
        // If the version wasn't updated (meaning non-effective attributes were
        // changed), the original has to be deleted, otherwise set
        // to WORKING_USER=NULL.
        // This is no problem in a situation when the user updates status from
        // no-version-requiring to version-requiring, because status is not
        // part of unique ID, but the version however is. It is the new version
        // that creates a new element. In other cases the previous latest has
        // to be deleted. Status only effects if version is updated at all.
        
        if (latestTbl!=null){
            if (newVersion.equals(tbl.getVersion())){
                // delete the previous copy                
                Parameters params = new Parameters();
                params.addParameterValue("mode", "delete");
                params.addParameterValue("del_id", latestTbl.getID());
                DsTableHandler tblHandler =
                    new DsTableHandler(conn, params, ctx);
                tblHandler.setUser(user);
                tblHandler.setVersioning(false);
                tblHandler.execute();
            }
            else{
                gen.clear();
                gen.setTable("DS_TABLE");
                gen.setFieldExpr("WORKING_USER", "NULL");
                stmt.executeUpdate(gen.updateStatement() +
                        " where TABLE_ID=" + latestTbl.getID());

                // update the parent dataset
                versionUpwards(tbl, latestTbl.getID());
            }
        }
        else
            versionUpwards(tbl, null);
        
        // release the top namespace
        String topNS = tbl.getParentNs();
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setFieldExpr("WORKING_USER", "NULL");
            stmt.executeUpdate(gen.updateStatement() +
                    " where NAMESPACE_ID=" + topNS);
        }
        
        return true;
    }
    
    /**
     * Check in the specified dataset.
     */
    private boolean checkInDst(String dstID, String status) throws Exception{
    	
		// load the table we need to check in
		Dataset dst = loadDst(dstID);
        
		// check if the status satisfies the requirements
		checkStatusRequirements(dst, status);
        
		String newVersion = null;
		Dataset latestDst = null;
        
		String latestID = getLatestDstID(dst);
		if (latestID==null || latestID.equals(dst.getID()))
			newVersion = composeNewVersion(null);
		else
			latestDst = loadDst(latestID);
        
		if (newVersion==null){
			if (requiresVersioning(status) ||
				(latestDst!=null &&
				!latestDst.getStatus().equals(dst.getStatus())))
					newVersion = newVersion(dst, latestDst);
			else
				newVersion = dst.getVersion();
		}
        
		// update from working copy to acting copy
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		gen.setField("WORKING_COPY", "N");
		gen.setFieldExpr("WORKING_USER", "NULL");
		gen.setField("VERSION", newVersion);
        
		// set the fields for keeping history info
		gen.setField("USER", user.getUserName());
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(gen.updateStatement() +
								" where DATASET_ID=" + dstID);
        
		// If the version wasn't updated (meaning non-effective attributes were
		// changed), the original has to be deleted, otherwise set
		// to WORKING_USER=NULL.
		// This is no problem in a situation when the user updates status from
		// no-version-requiring to version-requiring, because status is not
		// part of unique ID, but the version however is. It is the new version
		// that creates a new dataset. In other cases the previous latest has
		// to be deleted. Status only effects if version is updated at all.
        
		if (latestDst!=null){
			
			if (newVersion.equals(dst.getVersion())){
				// delete the previous copy                
				Parameters params = new Parameters();
				params.addParameterValue("mode", "delete");
				params.addParameterValue("complete", "true");
				params.addParameterValue("ds_id", latestDst.getID());
				DatasetHandler dstHandler =
					new DatasetHandler(conn, params, ctx);
				dstHandler.setUser(user);
				dstHandler.setVersioning(false);
				dstHandler.execute();
			}
			else{
				gen.clear();
				gen.setTable("DATASET");
				gen.setFieldExpr("WORKING_USER", "NULL");
				stmt.executeUpdate(gen.updateStatement() +
						" where DATASET_ID=" + latestDst.getID());
			}
		}
		
		// release the top namespace
        String topNS = dst.getNamespaceID();
        if (topNS!=null){
            gen.clear();
            gen.setTable("NAMESPACE");
            gen.setFieldExpr("WORKING_USER", "NULL");
            stmt.executeUpdate(gen.updateStatement() +
                    " where NAMESPACE_ID=" + topNS);
        }
		
		return true;
    }
    
    /**
     * Check status requirements, based on given object id and type.
     */
    private void checkStatusRequirements(String objID, String objType,
        String status) throws Exception{
        
        if (objID==null || objType==null)
            throw new Exception("Check-in error: object id or type null!");
        
        if (status==null)
            throw new Exception("Status null!");
        
        if (objType.equals("elm")){
            checkStatusRequirements(loadElm(objID), status);
        }
        else if (objType.equals("tbl")){
        }
        else if (objType.equals("dst")){
        }
        else
            throw new Exception("Unknown object type: " + objType);
    }
    
    /**
     * Check status requirements of the specified data element
     */
    private void checkStatusRequirements(DataElement elm, String status)
                                                    throws Exception{        
        // check Name
        String name = elm.getAttributeValueByShortName("Name");
        if (Util.nullString(name))
            throw new Exception("Status '" + status + "' requires Name!");
            
        // check Submitting Org
        
        DElemAttribute submOrgAttr =
            elm.getAttributeByShortName("SubmitOrganisation");
        if (submOrgAttr==null)
            throw new Exception("For '" +status+ "' status you must specify " +
                                "SubmitOrganisation complex attribute!");
                                
        submOrgAttr.setFields(searchEngine.getAttrFields(submOrgAttr.getID()));
        
        //check name
        if (submOrgAttr.getFieldValueByName("name")==null)
            throw new Exception("For '"+status+"' status you must specify " +
                "'name' field in SubmitOrganisation complex attribute!");
                
        //check PhoneNr
        if (submOrgAttr.getFieldValueByName("PhoneNr")==null)
            throw new Exception("For '"+status+"' status you must specify " +
                "'PhoneNr' field in SubmitOrganisation complex attribute!");
        
        if (!status.equals("Recorded"))
            return;
        
        // 'Recorded' status requires all mandatory attributes
        Vector attrs =
            searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
        for (int i=0; attrs!=null && i<attrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)attrs.get(i);
            String oblig = attr.getObligation();
            if (oblig!=null && oblig.equals("M")){
                if (attr.displayFor(elm.getType())){
                    String attrName = attr.getShortName();
                    String value = elm.getAttributeValueByShortName(attrName);
                    if (Util.nullString(value)){
                        throw new Exception("Status '" + status +
                                                "' requires " + attrName);
                    }
                }
            }
        }
    }
    
    /**
     * Check status requirements of the specified table
     */
    private void checkStatusRequirements(DsTable tbl, String status)
                                                    throws Exception{        
        // check Name
        String name = tbl.getAttributeValueByShortName("Name");
        if (Util.nullString(name))
            throw new Exception("Status '" + status + "' requires Name!");
            
        // check Submitting Org
        DElemAttribute submOrgAttr =
            tbl.getAttributeByShortName("SubmitOrganisation");
        if (submOrgAttr==null)
            throw new Exception("For '" +status+ "' status you must specify " +
                                "SubmitOrganisation complex attribute!");
                                
        submOrgAttr.setFields(searchEngine.getAttrFields(submOrgAttr.getID()));
            //check name
        if (submOrgAttr.getFieldValueByName("name")==null)
            throw new Exception("For '"+status+"' status you must specify " +
                "'name' field in SubmitOrganisation complex attribute!");
                
            //check PhoneNr
        if (submOrgAttr.getFieldValueByName("PhoneNr")==null)
            throw new Exception("For '"+status+"' status you must specify " +
                "'PhoneNr' field in SubmitOrganisation complex attribute!");
        
        if (!status.equals("Recorded"))
            return;
        
        // 'Recorded' status requires all mandatory attributes
        Vector attrs =
            searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
        for (int i=0; attrs!=null && i<attrs.size(); i++){
            DElemAttribute attr = (DElemAttribute)attrs.get(i);
            String oblig = attr.getObligation();
            if (oblig!=null && oblig.equals("M")){
                if (attr.displayFor("TBL")){
                    String attrName = attr.getShortName();
                    String value = tbl.getAttributeValueByShortName(attrName);
                    if (Util.nullString(value)){
                        throw new Exception("Status '" + status +
                                                "' requires " + attrName);
                    }
                }
            }
        }
    }
    
	/**
	 * Check status requirements of the specified table
	 */
	private void checkStatusRequirements(Dataset dst, String status)
													throws Exception{        
		// check Name
		String name = dst.getAttributeValueByShortName("Name");
		if (Util.nullString(name))
			throw new Exception("Status '" + status + "' requires Name!");
        
		// check Submitting Org
		DElemAttribute submOrgAttr =
			dst.getAttributeByShortName("SubmitOrganisation");
		if (submOrgAttr==null)
			throw new Exception("For '"+status+"' status you must specify " +
                "SubmitOrganisation complex attribute!");
                
		submOrgAttr.setFields(searchEngine.getAttrFields(submOrgAttr.getID()));
			//check name
		if (submOrgAttr.getFieldValueByName("name")==null)
			throw new Exception("For '"+status+"' status you must specify " +
                "'name' field in SubmitOrganisation complex attribute!");
                
			//check PhoneNr
		if (submOrgAttr.getFieldValueByName("PhoneNr")==null)
			throw new Exception("For '"+status+"' status you must specify " +
                "'PhoneNr' field in SubmitOrganisation complex attribute!");
    
		if (!status.equals("Recorded"))
			return;
    
		// 'Recorded' status requires all mandatory attributes
		Vector attrs =
			searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
		for (int i=0; attrs!=null && i<attrs.size(); i++){
			DElemAttribute attr = (DElemAttribute)attrs.get(i);
			String oblig = attr.getObligation();
			if (oblig!=null && oblig.equals("M")){
				if (attr.displayFor("DST")){
					String attrName = attr.getShortName();
					String value = dst.getAttributeValueByShortName(attrName);
					if (Util.nullString(value)){
						throw new Exception("Status '" + status +
												"' requires " + attrName);
					}
				}
			}
		}
	}
    
    /**
     * Update version of the specified object.
     */
    private String updateVersion(String objID, String objType,
        String status) throws Exception{
            
        return null;
    }
    
    /**
     * Check if the given status requires versioning.
     */
    public boolean requiresVersioning(String status) throws Exception{
        
        if (status==null)
            throw new Exception("Unknown status: null");
        
        if (!regStatuses.containsKey(status))
            throw new Exception("Unknown status!");
        
        return Boolean.valueOf((String)regStatuses.get(status)).booleanValue();
    }
    
    /**
    *
    */
    private String newVersion(String copyID, String type, String latestID)
        throws Exception{
        
        if (Util.nullString(copyID) || Util.nullString(type))
            throw new Exception("Unable to locate the object!");
        
        if (type.equals("elm"))
            return newVersion(loadElm(copyID), loadElm(latestID));
        else if (type.equals("tbl"))
            return newVersion(loadTbl(copyID));
        else if (type.equals("dst"))
            return newVersion(loadDst(copyID));
        else
            throw new Exception("Unknown type!");
    }
    
    /**
    *
    */
    private DataElement loadElm(String elmID) throws Exception{

        if (Util.nullString(elmID))
            throw new Exception("Data element ID not specified!");
        
        // Get the data element object. This will also give us the
        // element's simple attributes + tableID
        DataElement elem = searchEngine.getDataElement(elmID);
        if (elem == null)
            throw new Exception("Data element not found!");
        
        // get and set the element's complex attributes
        //elem.setComplexAttributes(searchEngine.getComplexAttributes(elmID, "E",null,elem.getTableID(),elem.getDatasetID()));
        //EK get also iinherited attributes
        elem.setComplexAttributes(searchEngine.getComplexAttributes(elmID, "E",null,elem.getTableID(),elem.getDatasetID()));

        // write allowable values (levelling not needed here)
        elem.setFixedValues(searchEngine.getAllFixedValues(elmID, "elem"));
        
        return elem;
    }
    
    /**
    *
    */
    private DsTable loadTbl(String tblID) throws Exception{
        
        if (Util.nullString(tblID))
            throw new Exception("Table ID not specified!");
        
        // get the table
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable == null)
            throw new Exception("Table not found!");
            
        // get simple attributes
        Vector v = searchEngine.getSimpleAttributes(tblID, "T");
        dsTable.setSimpleAttributes(v);
        
        // get complex attributes
        //dsTable.setComplexAttributes(searchEngine.getComplexAttributes(tblID, "T"));
       //EK get also iinherited attributes
        dsTable.setComplexAttributes(searchEngine.getComplexAttributes(tblID, "T", null, null,dsTable.getDatasetID()));

        // get data elements (this will set all the simple attributes,
        // but no fixed values)
        Vector vv = searchEngine.getDataElements(null, null, null, null, tblID);
        dsTable.setElements(vv);
        
        // get the dataset basic info
        Dataset ds = null;
        if (!Util.nullString(dsTable.getDatasetID())){
            ds = searchEngine.getDataset(dsTable.getDatasetID());
        }
        
        return dsTable;
    }
    
    /**
    *
    */
    private Dataset loadDst(String dstID) throws Exception{
        
        if (Util.nullString(dstID))
            throw new Exception("Dataset ID not specified!");
        
        Dataset ds = searchEngine.getDataset(dstID);
        if (ds == null)
            throw new Exception("Dataset not found!");
            
        Vector v = searchEngine.getSimpleAttributes(dstID, "DS");
        ds.setSimpleAttributes(v);
        
        v = searchEngine.getComplexAttributes(dstID, "DS");
        ds.setComplexAttributes(v);
        
        v = searchEngine.getDatasetTables(dstID);
        ds.setTables(v);
        
        return ds;
    }

    
    /**
    *
    */
    private String newVersion(DataElement srcElm, DataElement latestElm)
        throws Exception{
        	
        // compare statuses
        if (!srcElm.getStatus().equals(latestElm.getStatus()))
			return composeNewVersion(latestElm.getVersion());
        
        // compare the attributes
        if (!equalWithoutOrder(latestElm.getVersioningAttributes(),
                                    srcElm.getVersioningAttributes()))
            return composeNewVersion(latestElm.getVersion());
            
        // compare the value domain
        if (!equalWithoutOrder(latestElm.getFixedValues(),
                                    srcElm.getFixedValues()))
            return composeNewVersion(latestElm.getVersion());
        
        // elements must have been equal
        return latestElm.getVersion();
    }
    
    /**
    *
    */
    private String newVersion(DsTable srcTbl, DsTable latestTbl)
        throws Exception{
        
		// compare statuses
		if (!srcTbl.getStatus().equals(latestTbl.getStatus()))
			return composeNewVersion(latestTbl.getVersion());

        // compare the attributes
        if (!equalWithoutOrder(latestTbl.getVersioningAttributes(),
                                    srcTbl.getVersioningAttributes()))
            return composeNewVersion(latestTbl.getVersion());
            
        // elements we don't compare, because table structure is versioned
        // by versioning its single elements
        
        // tables must have been equal
        return latestTbl.getVersion();
    }
    
    /**
    *
    */
    private String newVersion(Dataset srcDst, Dataset latestDst)
        throws Exception{
        	
		// compare statuses
		if (!srcDst.getStatus().equals(latestDst.getStatus()))
			return composeNewVersion(latestDst.getVersion());

        
        // compare the attributes
        if (!equalWithoutOrder(latestDst.getVersioningAttributes(),
                                    srcDst.getVersioningAttributes()))
            return composeNewVersion(latestDst.getVersion());
            
        // elements we don't compare, because table structure is versioned
        // by versioning its single elements
        
        // tables must have been equal
        return latestDst.getVersion();
    }
    
    /**
    *
    */
    private void versionUpwards(DataElement elm, String latestID)
                throws Exception{
        
        if (!upwardsVersioning)
        	return;
        
        // create new version of parent table by copying
        // everything from the old version and substituting
        // latestElm with the new elm
        
        String oldTblID = elm.getTableID();
        if (Util.nullString(oldTblID))
            return;
        
        // copy table
        CopyHandler copyHandler = new CopyHandler(conn);
        copyHandler.setUser(user);
        String newTblID = copyHandler.copyTbl(oldTblID, false, true);
        if (Util.nullString(newTblID))
            return;

        // set the new tables credentials for history
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        if (user==null)
            gen.setFieldExpr("USER", "NULL");
        else
            gen.setField("USER", user.getUserName());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.updateStatement() +
                " where TABLE_ID=" + newTblID);

		// check if this is an element restore and if so,
		// add element with latestID to the new table
        if (elm.getID()==null){
        	if (latestID!=null){
				gen = new SQLGenerator();
				gen.setTable("TBL2ELEM");
				gen.setFieldExpr("DATAELEM_ID", latestID);
				gen.setFieldExpr("TABLE_ID", newTblID);
				stmt.executeUpdate(gen.insertStatement());
        	}
        	
        	DsTable tbl = loadTbl(newTblID);
        	if (tbl!=null){
        		String dstID = tbl.getDatasetID();
        		tbl = new DsTable(null, dstID, null);
        		//versionUpwards(tbl,"");
        	}
        	
        	return;
        }
        
        // remove new element from table old version
        stmt.executeUpdate("delete from TBL2ELEM where TABLE_ID=" + oldTblID +
                                " and DATAELEM_ID=" + elm.getID());
        
        // remove old element from table new version
        // (that is if there ever was an old element, i.e.
        // it's a completely new element)
        if (latestID!=null)
            stmt.executeUpdate("delete from TBL2ELEM where TABLE_ID=" +
                                newTblID + " and DATAELEM_ID=" + latestID);
                                
        // update the parent dataset
        versionUpwards(loadTbl(newTblID), oldTblID);
    }
    
    /**
    *
    */
    private void versionUpwards(DsTable tbl, String latestID)
                                                throws Exception {
		if (!upwardsVersioning)
			return;
        
        // create new version of parent dataset by copying
        // everything from the old version and substituting
        // old table in the new dataset with new table
        
        String oldDstID = tbl.getDatasetID();
        if (Util.nullString(oldDstID))
            return;
            
        // copy dataset
        CopyHandler copyHandler = new CopyHandler(conn);
        copyHandler.setUser(user);
        String newDstID = copyHandler.copyDst(oldDstID, false, true, false);
        if (Util.nullString(newDstID))
            return;

        // set the new dataset's credentials for history
        SQLGenerator gen = new SQLGenerator();
        gen.clear();
        gen.setTable("DATASET");
        gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        if (user==null)
            gen.setFieldExpr("USER", "NULL");
        else
            gen.setField("USER", user.getUserName());
        
        Statement stmt = conn.createStatement();
        String sql = gen.updateStatement() + " where DATASET_ID=" + newDstID;
        stmt.executeUpdate(sql);
        
        // remove new table from the dataset's old version
        stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" + oldDstID +
                                " and TABLE_ID=" + tbl.getID());
        
        // remove old table from the dataset's new version
        // (that is if there ever was an old table, i.e.
        // it's a completely new table)
        if (latestID!=null)
            stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" + newDstID +
                                " and TABLE_ID=" + latestID);
    }
    
    /**
    *
    */
    private String newVersion(DsTable srcTbl)
        throws Exception{
        
        return null;
    }
    
    /**
    *
    */
    private String newVersion(Dataset srcDst)
        throws Exception{
        
        return null;
    }
    
    /**
    *
    */
    public boolean tblEmpty(String tblID, boolean exclWC) throws SQLException {
        
        String s = null;
        if (exclWC){
            s = "select count(TBL2ELEM.DATAELEM_ID) from TBL2ELEM " +
            "left outer join " +
            "DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID " +
            "where DATAELEM.WORKING_COPY='N' and TABLE_ID=" + tblID;
        }
        else
            s = "select count(*) from TBL2ELEM where TABLE_ID=" + tblID;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        boolean f = true;
        if (rs.next()){
            if (rs.getInt(1) > 0)
                f = false;
        }
        else
            f = false;
        
        return f;
    }
    
    /**
    *
    */
    public boolean dstEmpty(String dstID, boolean exclWC) throws SQLException {
        
        String s = null;
        if (exclWC){
            s = "select count(DST2TBL.TABLE_ID) from DST2TBL " +
            "left outer join " +
            "DS_TABLE on DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID " +
            "where DS_TABLE.WORKING_COPY='N' and DST2TBL.DATASET_ID=" + dstID;
        }
        else
            s = "select count(*) from DST2TBL where DATASET_ID=" + dstID;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        boolean f = true;
        if (rs.next()){
            if (rs.getInt(1) > 0)
                f = false;
        }
        else
            f = false;
        
        return f;
    }
    
	/**
	*
	*/
	public String restoreElm(String elmID) throws Exception{
        
		// make sure the element has been deleted
		if (!searchEngine.isElmDeleted(elmID))
			throw new Exception("No point in restoring a non-deleted element!");
        
        // get the element's latest table
        DataElement elm = loadElm(elmID);
        String latestTblID = elm.getTableID();
        if (Util.nullString(latestTblID))
			throw new Exception("Could not find the latest table!");
        
        // make sure the element's latest table has not been deleted
		if (!searchEngine.isTblDeleted(latestTblID))
			throw new Exception("This element's parent table has been deleted!"+
									" You must restore it first!");
        
        // LOCK THE TOP NAMESPACE
        
        // copy the latest dataset
        String latestDstID = elm.getDatasetID();
        if (Util.nullString(latestDstID))
			throw new Exception("Could not find the latest dataset!");
		
		CopyHandler copyHandler = new CopyHandler(conn);
		copyHandler.setUser(user);
		String newDstID = copyHandler.copyDst(latestDstID, false, true, false);
		if (Util.nullString(newDstID))
			throw new Exception("Failed to copy the dataset!");

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		gen.setFieldExpr("VERSION", "VERSION+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(gen.updateStatement() + 
									" where DATASET_ID=" + newDstID);

		// copy the latest table
		String newTblID = copyHandler.copyTbl(latestTblID, false, true);
		if (Util.nullString(newTblID))
			throw new Exception("Failed to copy the table!");

		gen = new SQLGenerator();
		gen.setTable("DS_TABLE");
		gen.setFieldExpr("VERSION", "VERSION+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		stmt.executeUpdate(gen.updateStatement() + 
									" where TABLE_ID=" + newTblID);
		
		// add the table copy into the dataset copy
		gen.setTable("DST2TBL");
		gen.setFieldExpr("DATASET_ID", newDstID);
		gen.setFieldExpr("TABLE_ID", newTblID);
		stmt.executeUpdate(gen.insertStatement());
		
		// copy the element
		String newElmID = copyHandler.copyElem(elmID, true);
		if (Util.nullString(newElmID))
			throw new Exception("Failed to copy the element!");

		gen = new SQLGenerator();
		gen.setTable("DATAELEM");
		gen.setFieldExpr("VERSION", "VERSION+1");
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
		if (user==null)
			gen.setFieldExpr("USER", "NULL");
		else
			gen.setField("USER", user.getUserName());
        
		stmt.executeUpdate(gen.updateStatement() + 
									" where DATAELEM_ID=" + newElmID);
		
		// add the element copy into the table copy
		gen.setTable("TBL2ELEM");
		gen.setFieldExpr("DATAELEM_ID", newElmID);
		gen.setFieldExpr("TABLE_ID", newTblID);
		stmt.executeUpdate(gen.insertStatement());
		
		// UNLOCK THE TOP NAMESPACE

		// all fine, return
		return newElmID;
	}
    
    /**
    *
    */
    public String deleteElm(String elmID) throws Exception{
        
        DataElement elm = loadElm(elmID);
        if (elm==null)
            return null;
        
        // make sure that this is the latest version of an element
        // with such logical ID
        String latestID = getLatestElmID(elm);
        if (latestID!=null && !latestID.equals(elmID))
            throw new Exception("Cannot delete an intermediate version!");
        
        // deleting the latest version means creating a new
        // parent table where this element is missing (if it had
        // a table in the first place)
        
        String oldTblID = elm.getTableID();
        if (Util.nullString(oldTblID))
            return null;
        
        // copy old table
        CopyHandler copyHandler = new CopyHandler(conn);
        copyHandler.setUser(user);
        String newTblID = copyHandler.copyTbl(oldTblID, false, true);
        if (Util.nullString(newTblID))
            return null;

        // set the new table's credentials for history
        SQLGenerator gen = new SQLGenerator();
        gen.clear();
        gen.setTable("DS_TABLE");
        gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        if (user==null)
            gen.setFieldExpr("USER", "NULL");
        else
            gen.setField("USER", user.getUserName());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.updateStatement() + " where TABLE_ID=" +
                                                                newTblID);
        
        // remove this element from the new table
        stmt.executeUpdate("delete from TBL2ELEM where TABLE_ID=" +
                newTblID + " and DATAELEM_ID=" + elmID);
        
        // update the parent dataset
        versionUpwards(loadTbl(newTblID), oldTblID);
        
        return newTblID;
    }
    
    /**
    *
    */
    public String deleteTbl(String tblID) throws Exception{
        
        DsTable tbl = loadTbl(tblID);
        if (tbl==null)
            return null;
        
        // make sure that this is the latest version of a table
        // with such logical ID
        String latestID = getLatestTblID(tbl);
        if (latestID!=null && !latestID.equals(tblID))
            throw new Exception("Cannot delete an intermediate version- " +
                    "tblID=" + tblID + ", latestID=" + latestID);
        
        // deleting the latest version means creating a new
        // parent dataset where this table is missing
        
        String oldDstID = tbl.getDatasetID();
        if (Util.nullString(oldDstID))
            return null;
        
        // we need to copy the latest parent dataset and its table relations
        CopyHandler copyHandler = new CopyHandler(conn);
        copyHandler.setUser(user);
        String newDstID = copyHandler.copyDst(oldDstID, false,true,false);
        if (Util.nullString(newDstID))
            return null;

        // we need to update the new dataset's version
        SQLGenerator gen = new SQLGenerator();
        gen.clear();
        gen.setTable("DATASET");
        gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        if (user==null)
            gen.setFieldExpr("USER", "NULL");
        else
            gen.setField("USER", user.getUserName());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.updateStatement() +
                " where DATASET_ID=" + newDstID);
        
        // remove this table from the new dataset
        stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" +
                newDstID + " and TABLE_ID=" + tblID);
		
		return newDstID;
    }
    
    /**
    *
    */
    public String getLatestElmID(DataElement copyElm) throws SQLException{
        
        String q =
        "select * from DATAELEM " +
		"left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
		"left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
        "left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
		"left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
        "where DATAELEM.WORKING_COPY='N' and " +
		"DATASET.DELETED is null and " +
        "DATAELEM.PARENT_NS=" + copyElm.getNamespace().getID() + " and " +
        "DATAELEM.SHORT_NAME='" + copyElm.getShortName() + "' " +
        "order by DATAELEM.VERSION desc";
        
		ResultSet rs = conn.createStatement().executeQuery(q);
		if (rs.next())
			return rs.getString("DATAELEM_ID");
		else        
			return null;
    }
    
    /**
    *
    */
    public String getLatestTblID(DsTable tbl) throws SQLException{
    	
        String q =
		"select * from DS_TABLE " +
		"left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
		"left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
		"where DS_TABLE.WORKING_COPY='N' and " +
		"DATASET.DELETED is null and " +
		"DS_TABLE.PARENT_NS=" + tbl.getParentNs() + " and " +
		"DS_TABLE.SHORT_NAME='" + tbl.getShortName() + "' " +
		"order by DS_TABLE.VERSION desc";
				
        ResultSet rs = conn.createStatement().executeQuery(q);
        if (rs.next())
            return rs.getString("TABLE_ID");
        else        
            return null;
    }
    
	/**
	*
	*/
	public String getLatestDstID(Dataset dst) throws SQLException{
    
		String q =
		"select * from DATASET where WORKING_COPY='N' and " +
		"DELETED is null and " +
		"SHORT_NAME='" + dst.getShortName() + "' order by VERSION desc";

		ResultSet rs = conn.createStatement().executeQuery(q);
		if (rs.next())
			return rs.getString("DATASET_ID");
		else        
			return null;
	}
    
    /**
    *
    */
    private String composeNewVersion(String oldVersion){
        if (oldVersion==null) oldVersion = "0";
        int oldVer = Integer.parseInt(oldVersion);
        return String.valueOf(oldVer+1);
    }
    
    /**
    *
    */
    public Hashtable getRegStatuses(){
        return this.regStatuses;
    }

	/**
	*
	*/
	public Vector getRegStatusesOrdered(){
		return this.regStatusesOrdered;
	}
    
    /**
    *
    */
    public boolean equalWithoutOrder(Vector v1, Vector v2){
        
        if (v1==null){
            if (v2==null)
                return true;
        }
        else if (v2==null)
            return false;
                
        if (v1.size() != v2.size())
            return false;
            
        for (int i=0; i<v1.size(); i++)
            if (!v2.contains(v1.get(i)))
                return false;
        
        return true;
    }
    
    /**
    *
    */
    public boolean isLastElm(String id, String shortName, String parentNS)
                                                        throws SQLException{
        
        String s =
		"select count(*) from DATAELEM " +
		"left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
		"left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
		"left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
		"left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +        
		"where DATAELEM.SHORT_NAME='" + shortName +
		"' and DATASET.DELETED is null and " +
        "DATAELEM.DATAELEM_ID<>" + id + " and ";
        
        if (parentNS==null)
            s = s + "DATAELEM.PARENT_NS is null";
        else
            s = s + "DATAELEM.PARENT_NS=" + parentNS;
            
        boolean f = false;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        if (rs.next()){
            if (rs.getInt(1) == 0)
                f = true;
        }
        
        return f;
    }
    
    /**
    *
    */
    public boolean isLastTbl(String id, String shortName, String parentNS)
                                                        throws SQLException{
        
        String s =
		"select count(*) from DS_TABLE " +
		"left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
		"left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
        "where DS_TABLE.SHORT_NAME='" + shortName +
		"' and DATASET.DELETED is null and " +
        "DS_TABLE.TABLE_ID<>" + id + " and ";
        
        if (parentNS==null)
            s = s + "DS_TABLE.PARENT_NS is null";
        else
            s = s + "DS_TABLE.PARENT_NS=" + parentNS;
            
        boolean f = false;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        if (rs.next()){
            if (rs.getInt(1) == 0)
                f = true;
        }
        
        return f;
    }
    
    /**
    *
    */
    public boolean isLastDst(String id, String shortName)
                                                        throws SQLException{
        
        String s = "select count(*) from DATASET " +
        "where SHORT_NAME='" + shortName + "' and DELETED is null and " +
        "DATASET_ID<>" + id;
            
        boolean f = false;
        
        ResultSet rs = conn.createStatement().executeQuery(s);
        if (rs.next()){
            if (rs.getInt(1) == 0)
                f = true;
        }
        
        return f;
    }

	/**
	*
	*/
	public void setContext(ServletContext ctx){
		this.ctx = ctx;
	}
    
    /**
    * main for testing
    */
    public static void main(String[] args){
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            /*Connection conn =
                DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");*/
                
            AppUserIF testUser = new TestUser();
            testUser.authenticate("jaanus", "jaanus");
            
            VersionManager verMan = new VersionManager(conn, testUser);
            DsTable tbl = new DsTable("2226", "", "CDDA");
            tbl.setParentNs("265");
            String id = verMan.getLatestTblID(tbl);
            //boolean f = verMan.checkInElm("11536", "Card");
      }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
