package eionet.meta.savers;

import java.util.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.util.*;
import com.tee.xmlserver.*;

import eionet.meta.TestUser;
import eionet.meta.VersionManager;
import eionet.meta.Dataset;

import com.tee.uit.security.*;
import eionet.util.SecurityUtil;

public class DatasetHandler extends BaseHandler {

    public static String ATTR_PREFIX = "attr_";
    public static String ATTR_MULT_PREFIX = "attr_mult_";
    
    private String mode = null;
    private String ds_id = null;
    private String[] ds_ids = null;
    private String ds_name = null;
    private String idfier = null;
    private String lastInsertID = null;
    
    private boolean versioning = true;
	private boolean importMode = false;
    
	/** indicates if top namespace needs to be released after an exception*/
	private boolean doCleanup = false;
	
	/** hashes for remembering originals and top namespaces for cleanup */
	HashSet origs = new HashSet();
	HashSet nss = new HashSet();
    
    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DatasetHandler(Connection conn, Parameters req, ServletContext ctx){
    	
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        
        this.mode = req.getParameter("mode");
        this.ds_id = req.getParameter("ds_id");
        this.ds_ids = req.getParameterValues("ds_id");
        this.ds_name = req.getParameter("ds_name");
		this.idfier = req.getParameter("idfier");
        
        if (ctx!=null){
			String _versioning = ctx.getInitParameter("versioning");
			if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
				setVersioning(false);
        }        
    }
    
    public DatasetHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void setUser(AppUserIF user){
        this.user = user;
    }
    
	public void setImport(boolean importMode){
		this.importMode = importMode;
	}
    
    public void setVersioning(boolean f){
        this.versioning = f;
    }
    
    public boolean getVersioning(){
        return this.versioning;
    }
    
    /**
     * 
     * @throws Exception
     */
	public void cleanup() throws Exception{
		
		if (!doCleanup) return;
		
		processOriginals(origs);
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("NAMESPACE");
		gen.setFieldExpr("WORKING_USER", "NULL");
		for (Iterator i=nss.iterator(); i.hasNext(); ){
		 conn.createStatement().executeUpdate(gen.updateStatement() +
					 " where NAMESPACE_ID=" + (String)i.next());
		}
	}
    
    public void execute() throws Exception {
    	
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
						  !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("restore") &&
						  !mode.equalsIgnoreCase("force_status") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("DatasetHandler mode unspecified!");
            
        if (mode.equalsIgnoreCase("add")){
            insert();
            ds_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
		else if (mode.equalsIgnoreCase("restore"))
			restore();
		else if (mode.equalsIgnoreCase("force_status"))
			forceStatus();
        else{
            delete();
            cleanVisuals();
        }
    }
    
    private void insert() throws Exception {
    	
        if (this.idfier == null)
            throw new SQLException("Identifier must be specified!");
        
        if (exists()) throw new SQLException(
					"A dataset with this identifier already exists!");
            
        if (Util.nullString(ds_name))
        	ds_name = idfier;
        	
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATASET");
        gen.setField("IDENTIFIER", idfier);
		gen.setField("SHORT_NAME", ds_name);
        
        // new datasets we treat as working copies until checked in
        if (versioning){
            gen.setField("WORKING_COPY", "Y");
            if (user!=null && user.isAuthentic())
                gen.setField("WORKING_USER", user.getUserName());
            gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        }
        
        // set the status
        String status = req.getParameter("reg_status");
        if (!Util.nullString(status))
            gen.setField("REG_STATUS", status);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        setLastInsertID();
        
        // add acl
		if (user!=null){
			String aclPath = "/datasets/" + idfier;
			HashMap acls = AccessController.getAcls();
			if (!acls.containsKey(aclPath)){
				String aclDesc = "Identifier: " + idfier;
				AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
			}
		}
        
        // create the corresponding namespace
        // (this also sets the WORKING_USER)
        String correspNS = createNamespace(idfier);
        if (correspNS!=null){
            gen.clear();
            gen.setTable("DATASET");
            gen.setField("CORRESP_NS", correspNS);
            stmt.executeUpdate(gen.updateStatement() + 
                        " where DATASET_ID=" + lastInsertID);
        }
        
        stmt.close();
        
        // process dataset attributes
        processAttributes();
    }
    
    private void update() throws Exception {
    	
    	if (ds_id==null) throw new Exception("Dataset ID missing!");

		// see if it's just an unlock
		String unlock = req.getParameter("unlock");
		if (unlock!=null && !unlock.equals("false")){
			
			// check if the user has the right to do this operation
			// ...
			
			unlockNamespace(unlock);
			return;
		}
		
        lastInsertID = ds_id;

		// handle the update of data model
        String dsVisual = req.getParameter("visual");
        if (!Util.nullString(dsVisual)){
            
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("DATASET");
        
            String strType = req.getParameter("str_type");
            String fldName = strType.equals("simple") ? "VISUAL" :
                                                        "DETAILED_VISUAL";
            
            if (dsVisual.equalsIgnoreCase("NULL"))
                gen.setFieldExpr(fldName, dsVisual);
            else
                gen.setField(fldName, dsVisual);
            
            StringBuffer buf = new StringBuffer(gen.updateStatement());
            buf.append(" where DATASET_ID=");
            buf.append(ds_id);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            stmt.close();
            
            return; // we only changed the 'visual'. no need to deal with attrs
        }

        // set the status
        String status = req.getParameter("reg_status");
        if (!Util.nullString(status)){
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("DATASET");
            gen.setField("REG_STATUS", status);
            conn.createStatement().executeUpdate(gen.updateStatement() +
                                    " where DATASET_ID=" + ds_id);
        }
        
        // short name
		if (!Util.nullString(ds_name)){
			SQLGenerator gen = new SQLGenerator();
			gen.setTable("DATASET");
			gen.setField("SHORT_NAME", ds_name);
			conn.createStatement().executeUpdate(gen.updateStatement() +
									" where DATASET_ID=" + ds_id);
		}
        
        // if check-in, do the action and exit
        String checkIn = req.getParameter("check_in");
        if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
            
            VersionManager verMan = new VersionManager(conn, user);
			verMan.setContext(ctx);
			
			String updVer = req.getParameter("upd_version");
			if (updVer!=null && updVer.equalsIgnoreCase("true"))
				verMan.updateVersion();
							
            verMan.checkIn(ds_id, "dst",
                                    req.getParameter("reg_status"));
            return;
        }
        
        deleteAttributes();
        processAttributes();
    }
    
    /**
	 * @param frcStatus
	 */
	private void forceStatus() throws SQLException{
		
		if (ds_id==null) return;
		String frcStatus = req.getParameter("force_status");
		if (frcStatus==null) return;
		
		// get all tables and data elements in dataset
		HashSet tbls = new HashSet();
		HashSet elms = new HashSet();
		StringBuffer buf = new StringBuffer().
		append("select distinct DST2TBL.TABLE_ID, TBL2ELEM.DATAELEM_ID ").
		append("from DST2TBL left outer join TBL2ELEM on ").
		append("DST2TBL.TABLE_ID=TBL2ELEM.TABLE_ID where DST2TBL.DATASET_ID=").
		append(ds_id);
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			tbls.add(rs.getString("DST2TBL.TABLE_ID"));
			elms.add(rs.getString("TBL2ELEM.DATAELEM_ID"));
		} 
		
		// force status to tables
		String s = "update DS_TABLE set REG_STATUS='" +
					frcStatus + "' where TABLE_ID=";
		Iterator iter = tbls.iterator();
		while (iter.hasNext())
			stmt.executeUpdate(s + (String)iter.next());
			
		// force status to elements
		s = "update DATAELEM set REG_STATUS='" +
					frcStatus + "' where DATAELEM_ID=";
		iter = elms.iterator();
		while (iter.hasNext())
			stmt.executeUpdate(s + (String)iter.next());
		
	}

	private void restore() throws Exception {
    	
		if (ds_ids==null || ds_ids.length==0)
			return;
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATASET");
		gen.setFieldExpr("DELETED", "NULL");
				
		Statement stmt = conn.createStatement();
		for (int i=0; i<ds_ids.length; i++)
			stmt.executeUpdate(gen.updateStatement() +
						" where DATASET_ID=" + ds_ids[i]);
		
		stmt.close();
    }
    
    private void delete() throws Exception {
        
        if (ds_ids==null || ds_ids.length==0)
            return;

        // find out which of ds_ids are not working copies
        // and if they're not the latest versions, throw
        // an exception
        
        StringBuffer buf = new StringBuffer("select * from DATASET where ");    
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        Vector  legal     = new Vector();
        HashSet delns     = new HashSet();
		HashSet wrkCopies = new HashSet();
		Hashtable identifiers = new Hashtable();
        VersionManager verMan = new VersionManager(conn, user);
		verMan.setContext(ctx);
        while (rs.next()){
            
            String thisID = rs.getString("DATASET_ID");
			String idfier = rs.getString("IDENTIFIER");
            String wrkCopy   = rs.getString("WORKING_COPY");
            
			identifiers.put(thisID, idfier);
            
            if (wrkCopy.equals("Y")){
                nss.add(rs.getString("CORRESP_NS"));
				wrkCopies.add(thisID);
            }            	
            
            if (verMan.isLastDst(rs.getString("DATASET_ID"),
                                     rs.getString("IDENTIFIER"))){
                delns.add(rs.getString("CORRESP_NS"));
            }
                
            // we require non-working copies to be the latest only if
            // in versioning mode
            if (wrkCopy.equals("N") && versioning){
                String latestID = verMan.getLatestDstID(
                        new Dataset(null, rs.getString("IDENTIFIER"), null));
                if (latestID!=null && !latestID.equals(thisID))
                    throw new Exception("DatasetHandler: Cannot delete an " +
                                                "intermediate version!");
            }
            else if (versioning)
                origs.add(idfier);
            
            legal.add(thisID);
        }
        
        if (legal.size()==0) return;
        
        // now reform ds_ids of those that are legsal for deletion
        ds_ids = new String[legal.size()];
        for (int i=0; i<legal.size(); i++)
            ds_ids[i] = (String)legal.get(i);
        
        // JH101003
        // if not a complete deletion, set the deleted flag
        String complete = req.getParameter("complete");
        if (complete==null || complete.equals("false")){
        	SQLGenerator gen = new SQLGenerator();
        	gen.setTable("DATASET");
        	gen.setField("DELETED", user.getUserName());
        	for (int i=0; i<ds_ids.length; i++)
        		stmt.executeUpdate(gen.updateStatement() +
								" where DATASET_ID=" + ds_ids[i]);			
        	return;
        }
        
		// delete dataset dependencies
		deleteAttributes();
		deleteComplexAttributes();
		deleteRodLinks();
		deleteTablesElems();
		//deleteNamespaces();
	
		// delete namespaces of last such datasets
		for (Iterator i=delns.iterator(); i.hasNext(); ){
		 stmt.executeUpdate("delete from NAMESPACE " +
					 " where NAMESPACE_ID=" + (String)i.next());
		}
	
		// we've passed the critical point, set cleanup is needed
		// in case an exception happens now
		doCleanup = true;
				
        // delete the datasets themselves
		buf = new StringBuffer("delete from DATASET where ");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATASET_ID=");
            buf.append(ds_ids[i]);
        }
        stmt.executeUpdate(buf.toString());
        
        // remove acls
		for (int i=0; i<ds_ids.length; i++){
			String identifier = (String)identifiers.get(ds_ids[i]);
			if (identifier==null) continue; 
			rs = stmt.executeQuery("select count(*) from DATASET " +
							"where IDENTIFIER='" + identifier + "'");
			if (rs.next() && rs.getInt(1)>0) continue;
			try{
				AccessController.removeAcl("/datasets/" + identifier);
			}
			catch (Exception e){}
		}
        
        stmt.close();
        
        // release the originals and namespaces
        cleanup();
    }
    
    /**
    *
    */
    private String createNamespace(String idfier) throws Exception{
        
        String shortName  = idfier + "_dst";
        String fullName   = idfier + " dataset";
        String definition = "The namespace of " + fullName;
        
        Parameters pars = new Parameters();        
        pars.addParameterValue("mode", "add");
        pars.addParameterValue("short_name", shortName);
        pars.addParameterValue("fullName", fullName);
        pars.addParameterValue("description", definition);
        
        if (versioning && user!=null && user.isAuthentic()){
            pars.addParameterValue("wrk_user", user.getUserName());
        }
        
        NamespaceHandler nsHandler = new NamespaceHandler(conn, pars, ctx);
        nsHandler.execute();
        
        return nsHandler.getLastInsertID();
    }
    
    private void deleteAttributes() throws SQLException {

		// find out image attributes, so to skip them later
		StringBuffer buf = new StringBuffer("select M_ATTRIBUTE_ID ");
		buf.append("from M_ATTRIBUTE where DISP_TYPE='image'");
		
		Vector imgAttrs = new Vector();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next())
			imgAttrs.add(rs.getString(1));

        buf = new StringBuffer("delete from ATTRIBUTE where (");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(ds_ids[i]);
        }
        
        buf.append(") and PARENT_TYPE='DS'");

		// skip image attributes        
		for (int i=0; i<imgAttrs.size(); i++)
			buf.append(" and M_ATTRIBUTE_ID<>").append((String)imgAttrs.get(i));
       
        log(buf.toString());
        
        stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteComplexAttributes() throws SQLException {
        
        for (int i=0; ds_ids!=null && i<ds_ids.length; i++){
            
            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("legal_delete", "true");
            params.addParameterValue("parent_id", ds_ids[i]);
            params.addParameterValue("parent_type", "DS");
            
            AttrFieldsHandler attrFieldsHandler =
                        new AttrFieldsHandler(conn, params, ctx);
            //attrFieldsHandler.setVersioning(this.versioning);
            attrFieldsHandler.setVersioning(false);
            try{
                attrFieldsHandler.execute();
            }
            catch (Exception e){
                throw new SQLException(e.toString());
            }
        }
    }

	private void deleteRodLinks() throws SQLException {

		StringBuffer buf = new StringBuffer("delete from DST2ROD where ");
		for (int i=0; i<ds_ids.length; i++){
			if (i>0) buf.append(" or ");
			buf.append("DATASET_ID=").append(ds_ids[i]);
		}
	
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			stmt.executeUpdate(buf.toString());
		}
		catch (SQLException sqle){
			try{if (stmt != null) stmt.close();}catch (SQLException sqlee){}
		}
	}

    private void processAttributes() throws SQLException {
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()){
            String parName = (String)parNames.nextElement();

            if (!parName.startsWith(ATTR_PREFIX))
                continue;
            if (parName.startsWith(ATTR_MULT_PREFIX)){
              String[] attrValues = req.getParameterValues(parName);
              if (attrValues == null || attrValues.length == 0) continue;
              String attrID = parName.substring(ATTR_MULT_PREFIX.length());
              for (int i=0; i<attrValues.length; i++){
                  insertAttribute(attrID, attrValues[i]);
              }
            }
            else{
              String attrValue = req.getParameter(parName);
              if (attrValue.length()==0)
                  continue;
              String attrID = parName.substring(ATTR_PREFIX.length());
              insertAttribute(attrID, attrValue);
            }
        }
    }
    
    private void insertAttribute(String attrId, String value) throws SQLException {
    	
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        
        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setField("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "DS");
        
        String sql = gen.insertStatement();
        log(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void deleteTablesElems() throws Exception {
        
        // we can only delete the tables that exist ONLY within those
        // datasets found legal for deletion (and residing in ds_ids)
        
        Statement stmt = conn.createStatement();
        
        // do it dataset by dataset
        for (int i=0; i<ds_ids.length; i++){
            
            // get the tables in this dataset
            String qry =
            "select distinct TABLE_ID from DST2TBL where DATASET_ID=" +
                                                                ds_ids[i];
            ResultSet rs = stmt.executeQuery(qry);
            Vector v = new Vector();
            while (rs.next()){
                v.add(rs.getString("TABLE_ID"));
            }
            
            // now prune out those tables belonging into other
            // datasets as well
            Vector legal = new Vector();
            for (int j=0; j<v.size(); j++){
                String tblID = (String)v.get(j);
                qry = "select count(*) from DST2TBL where TABLE_ID=" + tblID +
                      " and DATASET_ID<>" + ds_ids[i];
                rs = stmt.executeQuery(qry);
                if (rs.next()){
                    if (rs.getInt(1) == 0)
                        legal.add(tblID);
                }
            }
            
            // delete the tables in Vector legal
            if (legal.size()==0)
                return;
            
            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            for (int j=0; j<legal.size(); j++){
                params.addParameterValue("del_id", (String)legal.get(j));
            }
                        
            DsTableHandler tableHandler = new DsTableHandler(conn, params, ctx);
            tableHandler.setUser(user);
            // once we've found the dataset legal for deletion, we delete
            // its tables regardless of versioning
            tableHandler.setVersioning(false);
            tableHandler.execute();
            
			stmt.executeUpdate("delete from DST2TBL where DATASET_ID=" +
																ds_ids[i]);
        }
        
        stmt.close();
	}
    
    private void deleteNamespaces() throws SQLException{
        
        StringBuffer buf = new StringBuffer("delete from NAMESPACE where ");
        for (int i=0; i<ds_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("NAMESPACE_ID=");
            buf.append(ds_ids[i]);
        }
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void setLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        
        log(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        if (rs.next())
            lastInsertID = rs.getString(1);
        stmt.close();
    }
    
    public String getLastInsertID(){
        return lastInsertID;
    }
    
    public boolean exists() throws SQLException {
        
        String qry =
        "select count(*) as COUNT from DATASET " +
        "where IDENTIFIER=" + com.tee.util.Util.strLiteral(this.idfier);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }
        
        stmt.close();
        
        return false;
    }
    
    /**
    *
    */
    private void processOriginals(HashSet originals) throws Exception {
        
        if (originals==null || originals.size()==0)
            return;

        // build the SQL
        StringBuffer buf = new StringBuffer();
        buf.append("update DATASET set WORKING_USER=NULL where ");
        buf.append("WORKING_USER='" + user.getUserName() + "' and (");
        int i=0;
        for (Iterator iter=originals.iterator(); iter.hasNext(); i++){
            if (i>0) buf.append(" or ");
            buf.append("IDENTIFIER='" + (String)iter.next() + "'");
        }
        buf.append(")");
        
        conn.createStatement().executeUpdate(buf.toString());
    }
    
	private void unlockNamespace(String nsID) throws SQLException{
		String s =
		"update NAMESPACE set WORKING_USER=NULL where NAMESPACE_ID=" + nsID;
		conn.createStatement().executeUpdate(s);
	}
	
    public static void main(String[] args){
        
        try{
        	
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                //DriverManager.getConnection("jdbc:mysql://192.168.1.6:3306/DataDict", "dduser", "xxx");
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");

            AppUserIF testUser = new TestUser();
            testUser.authenticate("jaanus", "jaanus");

            Parameters pars = new Parameters();
            pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
						
            pars.addParameterValue("ds_id", "1437");
			pars.addParameterValue("ds_id", "1431");
			pars.addParameterValue("ds_id", "1430");
            
            DatasetHandler handler = new DatasetHandler(conn, pars, null);
            handler.setUser(testUser);
            handler.setVersioning(false);
            handler.execute();
       }
        catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace(new PrintStream(System.out));
        }
        
    }
}