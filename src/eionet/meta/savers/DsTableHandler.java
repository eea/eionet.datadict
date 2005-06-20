package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;

import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;
import com.tee.uit.security.*;

public class DsTableHandler extends BaseHandler {

    public static String ATTR_PREFIX = "attr_";
    public static String ATTR_MULT_PREFIX = "attr_mult_";

    public static String INHERIT_ATTR_PREFIX = "inherit_";
    public static String INHERIT_COMPLEX_ATTR_PREFIX = "inherit_complex_";

    private String mode = null;
    private String lastInsertID = null;

    private DDSearchEngine searchEngine = null;
    
    private String nsID = null;
    private String dsID = null;

    boolean copy = false; //making a copy, exists() not performed
    String version = null; //used only when making a copy
    
    boolean versioning = true;
    boolean superUser = false;
	private String date = null;
    
	/** indicates if top namespace needs to be released after an exception*/
    private boolean doCleanup = false;
    
    /** hashes for remembering originals and top namespaces for cleanup */
	HashSet originals = new HashSet();
	HashSet topns = new HashSet();
	
	/** for storing dataset ID returned by VersionManager.deleteTbl() */
	private String newDstID = null;
	
	/** for storing restored table ID returned by Restorer.restoreTbl() */
	private String restoredID = null;
	
	private boolean importMode = false;

	/**
	 * 
	 * @param conn
	 * @param req
	 * @param ctx
	 */
    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public DsTableHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        
        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
        }
    }

    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
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
    
    public void setSuperUser(boolean su){
        this.superUser = su;
    }
    
    /**
     * 
     * @return
     */
	public String getNewDstID(){
		return this.newDstID;
	}
    
    /**                                                                        
     * 
     * @throws Exception
     */
	public void cleanup() throws Exception{
		
		if (!doCleanup) return;
		
		// set WORKING_USER to null in all originals
		processOriginals(originals);
		
		// release the top namespaces
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("NAMESPACE");
		gen.setFieldExpr("WORKING_USER", "NULL");
		for (Iterator i=topns.iterator(); i.hasNext(); ){            
		 conn.createStatement().executeUpdate(gen.updateStatement() +
				 " where NAMESPACE_ID=" + (String)i.next());
		}
	}

    public void execute() throws Exception {
    	
		// initialize this.topNsReleaseNeeded (just in case)
		doCleanup = false;

        if (mode!=null && mode.equalsIgnoreCase("copy")){
            mode = "add";
            copy = true;
            version = req.getParameter("version");
        }

        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                           !mode.equalsIgnoreCase("edit") &&
                           !mode.equalsIgnoreCase("delete")))
            throw new Exception("DsTableHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else{
            delete();
            cleanVisuals();
        }
    }
    
    private void insert() throws Exception {
    	
    	// see if this is just linking to another element
		String link_elm = req.getParameter("link_elm");
		if (!Util.nullString(link_elm)){
			SQLGenerator gen = new SQLGenerator();
			gen.setTable("TBL2ELEM");
			gen.setFieldExpr("TABLE_ID", req.getParameter("table_id"));
			gen.setFieldExpr("DATAELEM_ID", req.getParameter("link_elm"));
			gen.setFieldExpr("POSITION", req.getParameter("elmpos"));
			conn.createStatement().executeUpdate(gen.insertStatement());
			return;
		}
        
        // get the onwer dataset id
        dsID = req.getParameter("ds_id");
        if (dsID == null)
            throw new Exception("DsTableHandler: ds_id not specified!");

        // get the table short name
		String idfier = req.getParameter("idfier");
        if (Util.nullString(idfier))
            throw new Exception("DsTableHandler: table identifier not found!");

        // get the parent namespace and dataset name
        String parentNS = req.getParameter("parent_ns");
        String dsName = req.getParameter("ds_name");
        if (parentNS == null){
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            Dataset ds = searchEngine.getDataset(dsID);
            if (ds != null){
                parentNS = ds.getNamespaceID();
                dsName = ds.getShortName();
            }
        }

        if (parentNS == null)
            throw new Exception("DsTableHandler: could not find " +
                                    "the parent dataset's namespace!");
        
        // now make sure that such a table does not exist
        if (exists(parentNS, idfier)) throw new Exception("Such a table already exists!");

        SQLGenerator gen = new SQLGenerator();
        Statement stmt = conn.createStatement();

        // close the parent namespace
        if (versioning && !Util.nullString(parentNS)){
            gen.setTable("NAMESPACE");
            gen.setField("WORKING_USER", user.getUserName());
            stmt.executeUpdate(gen.updateStatement() +
                    " where NAMESPACE_ID=" + parentNS);
        }
        
        // all well, create the new table
        
        String type = req.getParameter("type");
		String shn  = req.getParameter("short_name");
		if (Util.nullString(shn))
			shn = idfier;

        gen.clear();
        gen.setTable("DS_TABLE");
        gen.setField("IDENTIFIER", idfier);
		gen.setField("SHORT_NAME", shn);
        gen.setField("PARENT_NS", parentNS);

        // new tables we treat as working copies until checked in
		// Unless we are inserting from Import Tool (in which case versioning==false).
        if (versioning){
            gen.setField("WORKING_COPY", "Y");
            if (user!=null && user.isAuthentic())
                gen.setField("WORKING_USER", user.getUserName());
            gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        }
        else{
			if (user!=null) gen.setField("USER", user.getUserName());
			if (date!=null) gen.setFieldExpr("DATE", date);
        }

        if (!Util.nullString(type))
            gen.setField("TYPE", type);
        if (!versioning && !Util.nullString(version))
            gen.setField("VERSION", version);

        // insert the table
        stmt.executeUpdate(gen.insertStatement());
        setLastInsertID();

        // create the corresponding namespace
        String correspNS = createNamespace(dsName, idfier);
        if (correspNS==null)
            throw new Exception("DsTableHandler: failed to create " +
                "a corresponding namespace!");
        
        if (correspNS!=null){
            gen.clear();
            gen.setTable("DS_TABLE");
            gen.setField("CORRESP_NS", correspNS);
            stmt.executeUpdate(gen.updateStatement() + 
                        " where TABLE_ID=" + lastInsertID);
        }

        // JH140303 - enter a row into DST2TBL
        gen.clear();
        gen.setTable("DST2TBL");
        gen.setField("TABLE_ID",   lastInsertID);
        gen.setField("DATASET_ID", dsID);
        stmt.executeUpdate(gen.insertStatement());

        stmt.close();
        
        //copy table attributes and structure
        String copy_tbl_id = req.getParameter("copy_tbl_id");
        if (copy_tbl_id != null && copy_tbl_id.length()!=0){

            CopyHandler copier = new CopyHandler(conn, ctx);
			copier.setUser(user);

            gen.clear();
            gen.setTable("ATTRIBUTE");
            gen.setField("DATAELEM_ID", getLastInsertID());
            copier.copy(gen, "DATAELEM_ID=" + copy_tbl_id + " and PARENT_TYPE='T'");

            // copy rows in COMPLEX_ATTR_ROW, with lastInsertID
            copier.copyComplexAttrs(lastInsertID, copy_tbl_id, "T");

            copyTbl2Elem(copy_tbl_id);
            copy=true;
        }

        // process table attributes
        if (!copy){
           processAttributes();
        }
    }

    private void update() throws Exception {

        String tableID = req.getParameter("table_id");
        if (tableID == null)
            throw new Exception("DsTableHandler: table_id not specified!");

		// short name
		String shn = req.getParameter("short_name");
		if (!Util.nullString(shn)){
			SQLGenerator gen = new SQLGenerator();
			gen.setTable("DS_TABLE");
			gen.setField("SHORT_NAME", shn);
			conn.createStatement().executeUpdate(gen.updateStatement() +
									" where TABLE_ID=" + tableID);
		}

        // if check-in, do the action and exit
        String checkIn = req.getParameter("check_in");
        if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
        	
            VersionManager verMan = new VersionManager(conn, user);
			verMan.setContext(ctx);
			
			String updVer = req.getParameter("upd_version");
			if (updVer!=null && updVer.equalsIgnoreCase("true")){
				verMan.updateVersion();
				verMan.setUpwardsVersioning(true);
			}
				
            verMan.checkIn(tableID, "tbl",
                                    req.getParameter("reg_status"));
            return;
        }
        
        lastInsertID = tableID;
        String[] delIDs = {tableID};
        deleteAttributes(delIDs);
        processAttributes();
    }

    private void delete() throws Exception {
    	
        // do not allow deletion by unauthorized users
        if (user==null || !user.isAuthentic())
            throw new Exception("Unauthorized user!");

        String[] del_IDs = req.getParameterValues("del_id");
        if (del_IDs==null || del_IDs.length==0) return;
        
        // get more data about each table
        StringBuffer buf = new StringBuffer();
        buf.append("select * from DS_TABLE where ");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("TABLE_ID=");
            buf.append(del_IDs[i]);
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

		// loop over all found tables
        Vector wrkCopies = new Vector();
        HashSet delns = new HashSet();
        VersionManager verMan = new VersionManager(conn, user);
		verMan.setContext(ctx);
		String updVer = req.getParameter("upd_version");
		if (updVer!=null && updVer.equalsIgnoreCase("true"))
			verMan.updateVersion();
		
        while (rs.next()){
            
            // if table in work by another user, throw something
            String userName = rs.getString("WORKING_USER");
            if (userName!=null &&
                !userName.equals(user.getUserName()) && !superUser)
                throw new Exception("Table " +
                            rs.getString("TABLE_ID") +
                            " is in work by another user: " + userName);
            
            boolean wrkCopy =
                rs.getString("WORKING_COPY").equals("Y") ?
                true : false;
            
            // if no versioning logic should be applied (for
            // example when overwriting a previous version),
            // then treat the given tables as if they were
            // working copies 
            if (!versioning){
                wrkCopies.add(rs.getString("TABLE_ID"));
                if (wrkCopy && !superUser)
                    topns.add(rs.getString("PARENT_NS"));
                if (verMan.isLastTbl(rs.getString("TABLE_ID"),
                                     rs.getString("IDENTIFIER"),
                                     rs.getString("PARENT_NS"))){
                    delns.add(rs.getString("CORRESP_NS"));
                }
            }
            // working copies are to be left for DsTableHandler
            else if (wrkCopy){
                wrkCopies.add(rs.getString("DS_TABLE.TABLE_ID"));
                originals.add(rs.getString("PARENT_NS") + "," +
                              rs.getString("IDENTIFIER"));
                topns.add(rs.getString("PARENT_NS"));
                
                if (verMan.isLastTbl(rs.getString("TABLE_ID"),
                                     rs.getString("IDENTIFIER"),
                                     rs.getString("PARENT_NS"))){
                    delns.add(rs.getString("CORRESP_NS"));
                }
            }
            else{
                // non-working copies are handled by VersionManager
                newDstID = verMan.deleteTbl(rs.getString("TABLE_ID"));
            }
        }
        
        if (wrkCopies.size()==0)
			return;

        // start working with those legal for deletion
        del_IDs = new String[wrkCopies.size()];
        for (int i=0; i<wrkCopies.size(); i++)
            del_IDs[i] = (String)wrkCopies.get(i);
        
        // delete table attributes
        deleteAttributes(del_IDs);
        deleteComplexAttributes(del_IDs);
        
        // delete table elements
		deleteElements(del_IDs);
		
		// JH140803 - delete the tbl-dst relations
 		buf = new StringBuffer("delete from DST2TBL where ");
 		for (int i=0; i<del_IDs.length; i++){
	 		if (i>0) buf.append(" or ");
 			buf.append("TABLE_ID=");
	 		buf.append(del_IDs[i]);
 		}
		stmt.executeUpdate(buf.toString());
		
		// we've passed the critical point, set cleanup is needed
		// in case an exception happens now
		doCleanup = true;
		
		// delete the corresponding namespaces
		for (Iterator i=delns.iterator(); i.hasNext(); ){            
			stmt.executeUpdate("delete from NAMESPACE " +
					" where NAMESPACE_ID=" + (String)i.next());
		}
        
        // delete the tables
        buf = new StringBuffer("delete from DS_TABLE where ");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("TABLE_ID=");
            buf.append(del_IDs[i]);
        }
        stmt.executeUpdate(buf.toString());
        
        stmt.close();

        // release originals and top namespaces
        cleanup();
    }
    
    /*
     * 
     */
    private void deleteElements(String[] del_IDs) throws Exception{

		// since because of the versioning an element can belong
		// into several tables, we can here delete only those
		// elements belonging ONLY TO THESE tables selected for deletion.

		// first get all elements belonging to these tables
		HashSet elems = new HashSet();
		StringBuffer buf = new
			StringBuffer("select distinct DATAELEM_ID from TBL2ELEM where ");
		for (int i=0; i<del_IDs.length; i++){
			if (i>0) buf.append(" or ");
			buf.append("TABLE_ID=");
			buf.append(del_IDs[i]);
		}

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			elems.add(rs.getString("DATAELEM_ID"));
		}
        
		// no prune out those elements belonging into other tables as well
		buf =
		new StringBuffer("select count(*) from TBL2ELEM where DATAELEM_ID=?");
		for (int i=0; i<del_IDs.length; i++){
			buf.append(" and TABLE_ID<>");
			buf.append(del_IDs[i]);
		}
        
		PreparedStatement ps = conn.prepareStatement(buf.toString());
        
		Iterator iter = elems.iterator();
		while (iter.hasNext()){
			ps.setInt(1, Integer.parseInt((String)iter.next()));
			rs = ps.executeQuery();
			if (rs.next()){
				if (rs.getInt(1) > 0)
					iter.remove();
			}
		}
		
		// delete the elements found legal for deletion
		if (elems.size() != 0){
			Parameters params = new Parameters();
			params.addParameterValue("mode", "delete");
			for (iter = elems.iterator(); iter.hasNext(); ){
				params.addParameterValue("delem_id", (String)iter.next());
			}

			DataElementHandler delemHandler =
								new DataElementHandler(conn, params, ctx);
			delemHandler.setUser(user);
			delemHandler.setSuperUser(superUser);
			// here we must not use versioning, otherwise we might
			// end up in VersionManager.deleteElm()
			delemHandler.setVersioning(false);
			try{ delemHandler.execute(); } catch (Exception e){
				throw new SQLException(e.toString());
			}
		}
		
		// delete the tbl-elm relations
		 buf = new StringBuffer("delete from TBL2ELEM where ");
		 for (int i=0; i<del_IDs.length; i++){
			 if (i>0) buf.append(" or ");
			 buf.append("TABLE_ID=");
			 buf.append(del_IDs[i]);
		 }
		stmt.executeUpdate(buf.toString());
		
		stmt.close();
    }
    
    private void processOriginals(HashSet originals) throws Exception {
        
        if (originals==null || originals.size()==0)
            return;

        // build the query
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct TABLE_ID from DS_TABLE where ");
        Iterator iter=originals.iterator();
        int i = 0;
        while (iter.hasNext()){
            String s = (String)iter.next();
            int pos = s.indexOf(",");
            String tblName = s.substring(pos+1);
            String parentNs = s.substring(0,pos);
            
            if (i>0) buf.append(" or ");
            buf.append("(IDENTIFIER='");
            buf.append(tblName);
            buf.append("' and PARENT_NS='");
            buf.append(parentNs);
            buf.append("')");
        }
        
        // execute the query
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        
        // get the ids of originals
        HashSet hash = new HashSet();
        while (rs.next())
            hash.add(rs.getString("TABLE_ID"));
        
        // reset the WORKING_USER in all found originals
        iter=hash.iterator();
        while (iter.hasNext()){
            stmt.executeUpdate("update DS_TABLE set WORKING_USER=NULL " +
                                    "where TABLE_ID=" + (String)iter.next());
        }
    }
    
    /**
    *
    */
    private String createNamespace(String dstName, String idfier)
                                                    throws Exception{
        
        String shortName  = idfier + "_tbl_" + dstName + "_dst";
        String fullName   = idfier + " table in " + dstName + " dataset";
        String definition = "The namespace of " + fullName;
        
        Parameters pars = new Parameters();        
        pars.addParameterValue("mode", "add");
        pars.addParameterValue("short_name", shortName);
        pars.addParameterValue("fullName", fullName);
        pars.addParameterValue("description", definition);

        NamespaceHandler nsHandler = new NamespaceHandler(conn, pars, ctx);
        nsHandler.execute();

        return nsHandler.getLastInsertID();
    }

    private void deleteAttributes(String[] del_IDs) throws SQLException {
    	
		// find out image attributes, so to skip them later
		StringBuffer buf = new StringBuffer("select M_ATTRIBUTE_ID ");
		buf.append("from M_ATTRIBUTE where DISP_TYPE='image'");
		
		Vector imgAttrs = new Vector();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next())
			imgAttrs.add(rs.getString(1));

        buf = new StringBuffer("delete from ATTRIBUTE where (");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(del_IDs[i]);
        }

        buf.append(") and PARENT_TYPE='T'");
        
		// skip image attributes        
		for (int i=0; i<imgAttrs.size(); i++)
			buf.append(" and M_ATTRIBUTE_ID<>").append((String)imgAttrs.get(i));

        log(buf.toString());

        stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }

    /**
    *
    */
    private void deleteComplexAttributes(String[] del_IDs) throws SQLException {

        for (int i=0; del_IDs!=null && i<del_IDs.length; i++){

            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("legal_delete", "true");
            params.addParameterValue("parent_id", del_IDs[i]);
            params.addParameterValue("parent_type", "T");

            AttrFieldsHandler attrFieldsHandler =
                                new AttrFieldsHandler(conn, params, ctx);
            attrFieldsHandler.setVersioning(this.versioning);
            try{
                attrFieldsHandler.execute();
            }
            catch (Exception e){
                throw new SQLException(e.toString());
            }
        }
    }

    /**
    *
    */
    private void processAttributes() throws SQLException {
        String attrID=null;
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()){
            String parName = (String)parNames.nextElement();
            if (parName.startsWith(ATTR_PREFIX) &&
                  !parName.startsWith(ATTR_MULT_PREFIX)){
               String attrValue = req.getParameter(parName);
               if (attrValue.length()==0)
                  continue;
               attrID = parName.substring(ATTR_PREFIX.length());
               if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from dataset level
               insertAttribute(attrID, attrValue);
            }
            else if(parName.startsWith(ATTR_MULT_PREFIX)){
              String[] attrValues = req.getParameterValues(parName);
              if (attrValues == null || attrValues.length == 0) continue;
              attrID = parName.substring(ATTR_MULT_PREFIX.length());

              if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from dataset level

              for (int i=0; i<attrValues.length; i++){
                  insertAttribute(attrID, attrValues[i]);
              }
            }
            else if (parName.startsWith(INHERIT_ATTR_PREFIX) &&
                  !parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
              attrID = parName.substring(INHERIT_ATTR_PREFIX.length());
              if (dsID==null) continue;
              CopyHandler ch = new CopyHandler(conn, ctx);
              ch.setUser(user);
              ch.copyAttribute(lastInsertID, dsID, "T", "DS", attrID);
            }
            else if (parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
              attrID = parName.substring(INHERIT_COMPLEX_ATTR_PREFIX.length());
              if (dsID==null) continue;
              CopyHandler ch = new CopyHandler(conn, ctx);
			  ch.setUser(user);
              ch.copyComplexAttrs(lastInsertID, dsID, "DS", "T", attrID);
            }
        }
    }

    private void insertAttribute(String attrId, String value) throws SQLException {

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");

        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setField("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "T");

        String sql = gen.insertStatement();
        log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
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

    public String getNamespaceID(){
        return nsID;
    }

    public boolean exists(String parentNS, String idfier) throws SQLException {

        if (copy)
            return false;

        String qry =
        "select count(*) as COUNT from DS_TABLE " +
        "where DS_TABLE.IDENTIFIER=" + com.tee.util.Util.strLiteral(idfier) +
        " and DS_TABLE.PARENT_NS=" + parentNS;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);

        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }

        return false;
    }
    /**
    *
    */
    public void copyTbl2Elem(String srcTblID) throws SQLException{

        if (searchEngine==null)
            searchEngine=new DDSearchEngine(conn, "", ctx);
        searchEngine.setUser(user);
        Vector elems = searchEngine.getDataElements(null, null, null, null, srcTblID);

        if (elems==null) return;

        for (int i=0;i<elems.size();i++){
            DataElement elem = (DataElement)elems.get(i);

            String elem_id=elem.getID();

            Parameters pars = new Parameters();
            // "copy" is a new mode where exists() is not performed
            pars.addParameterValue("mode", "copy");
            pars.addParameterValue("table_id", getLastInsertID());
            pars.addParameterValue("delem_name", elem.getShortName());
			pars.addParameterValue("idfier", elem.getIdentifier());
			pars.addParameterValue("ns", elem.getNamespace().getID());
            pars.addParameterValue("type", elem.getType());
            pars.addParameterValue("copy_elem_id", elem_id);

            try{
                DataElementHandler handler = new DataElementHandler(conn, pars, ctx);
                handler.setUser(user);
                handler.setVersioning(false);
                handler.execute();
            }
            catch(Exception e){
                throw new SQLException(e.toString());
            }
        }

    }

    public void setUser(AppUserIF user){
        this.user = user;
    }
    
    public String getRestoredID(){
    	return this.restoredID;
    }

	/*
	 * 
	 */
	public void setDate(String unixTimestampMillisec){
		this.date = unixTimestampMillisec;
	}

    public static void main(String[] args){

        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.33:3306/dd",
				"dduser", "xxx");

            AppUserIF testUser = new TestUser();
            testUser.authenticate("heinlja", "xxx");

            Parameters pars = new Parameters();
            pars.addParameterValue("mode", "delete");
            pars.addParameterValue("del_id", "2010");

            DsTableHandler handler = new DsTableHandler(conn, pars, null);
            handler.setUser(testUser);
            handler.setVersioning(true);
            handler.execute();
       }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
