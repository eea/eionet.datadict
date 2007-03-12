package eionet.meta.savers;

import java.io.PrintStream;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;
import eionet.util.SecurityUtil;

import com.tee.util.*;
import com.tee.xmlserver.*;
import com.tee.uit.security.*;

public class DataElementHandler extends BaseHandler {

    public static String ATTR_PREFIX = "attr_";
    public static String ATTR_MULT_PREFIX = "attr_mult_";

    public static String INHERIT_ATTR_PREFIX = "inherit_";
    public static String INHERIT_COMPLEX_ATTR_PREFIX = "inherit_complex_";

    public static String POS_PREFIX = "pos_";
    public static String OLDPOS_PREFIX = "oldpos_";

    private String mode = null;
    private String elmValuesType = null;
    private String delem_id = null;
    private String[] delem_ids = null;
    private String elmShortName = null;
	private String elmIdfier = null;
    private String lastInsertID = null;
    
    private String tblNamespaceID = null;
    private String dstNamespaceID = null;
    
    private String tableID = null;
    
    private String schemaPhysPath = null;
    private String schemaUrlPath = null;
    
    private HashSet ch1ProhibitedAttrs = new HashSet();
    
    private String mDatatypeID = null; 
    private String datatypeValue = null;
    
    private DDSearchEngine searchEngine = null;
    
    private boolean checkInResult = false;
    
    boolean versioning = true;    
    boolean superUser = false;
	private String date = null;
	private boolean isImportMode = false;
	private boolean useForce = false;
    
    /** indicates if top namespace needs to be released after an exception*/
    private boolean doCleanup = false;
    
    /**
    for deletion - a HashSet for remembering namespace ids and short_names
    of all working copies, so later we can find originals and deal with them*/
    HashSet originals = new HashSet();
        
    /** for deletion - remember the top namespaces */
    HashSet topns = new HashSet();
    
    /** for storing table ID returned by VersionManager.deleteElm() */
    private String newTblID = null;

	/** for storing restored elm ID returned by Restorer.restoreElm() */
	private String restoredID = null;

	/** for storing the ID of the next-in-line version of the deleted common element */
	private String latestCommonElmID = null;

	/** */
	private String checkedInCopyID = null;

    /**
    *
    */
    public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    /**
     * 
     * @param conn
     * @param req
     * @param ctx
     */
    public DataElementHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");
        this.elmValuesType = req.getParameter("type");
        this.delem_id = req.getParameter("delem_id");
        this.delem_ids = req.getParameterValues("delem_id");
        this.elmShortName = req.getParameter("delem_name");
		this.elmIdfier = req.getParameter("idfier");
        this.tableID = req.getParameter("table_id");
        this.dstNamespaceID = req.getParameter("dst_namespace_id");
        this.tblNamespaceID = req.getParameter("tbl_namespace_id");        
        // some code might still supply parent namespace id with "ns"
        if (this.tblNamespaceID==null || this.tblNamespaceID.length()==0)
        	this.tblNamespaceID = req.getParameter("ns");
        
        
        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
        }
        
        // set the attributes prohibited to set for CH1 type elements
        try{
            searchEngine = new DDSearchEngine(conn, "", ctx);
            Vector v = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE);
            for (int i=0; v!=null && i<v.size(); i++){
                DElemAttribute attr = (DElemAttribute)v.get(i);
                if (attr.getShortName().equalsIgnoreCase("MinSize"))
                    ch1ProhibitedAttrs.add(attr.getID());
                if (attr.getShortName().equalsIgnoreCase("MaxSize"))
                    ch1ProhibitedAttrs.add(attr.getID());
                if (attr.getShortName().equalsIgnoreCase("Datatype"))
                    this.mDatatypeID = attr.getID();
            }
        }
        catch (Exception e){}
    }
    
    public DataElementHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void setUser(AppUserIF user){
        this.user = user;
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
    public String getNewTblID(){
    	return this.newTblID;
    }
    
    /**
    *
    */
    public void cleanup() throws Exception{
        
        if (!doCleanup) return;
        
        Statement stmt = conn.createStatement();
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        
        // release originals
		for (Iterator i=originals.iterator(); i.hasNext(); ){
			StringBuffer buf = new StringBuffer().
			append("update DATAELEM set WORKING_USER=NULL where IDENTIFIER=");
			String origID = (String)i.next();
			int commaPos = origID.indexOf(',');
			if (commaPos<0)
				buf.append(Util.strLiteral(origID));
			else
				buf.append(Util.strLiteral(origID.substring(0,commaPos))).
				append(" and PARENT_NS=").append(origID.substring(commaPos+1));
			stmt.executeUpdate(buf.toString());
		}
		
        // release the top namespaces
        if (req.getParameter("common")==null){
	        gen.clear();
	        gen.setTable("NAMESPACE");
	        gen.setFieldExpr("WORKING_USER", "NULL");
	        for (Iterator i=topns.iterator(); i.hasNext(); ){            
	            stmt.executeUpdate(gen.updateStatement() +
	                    " where NAMESPACE_ID=" + (String)i.next());
	        }
        }
        
        stmt.close();
    }
    
    public void execute() throws Exception {
        
        // initialize this.topNsReleaseNeeded (just in case)
        doCleanup = false;
        
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete") &&
                          !mode.equalsIgnoreCase("copy") &&
                          !mode.equalsIgnoreCase("edit_tblelems")))
            throw new Exception("DataElementHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add")){
            if (elmValuesType==null || (!elmValuesType.equalsIgnoreCase("CH1") &&
                            !elmValuesType.equalsIgnoreCase("CH2")))
                throw new Exception("Element type not specified!");
        }

        if (mode.equalsIgnoreCase("add") || mode.equalsIgnoreCase("copy")){
            insert();
            delem_id = getLastInsertID();
        }
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else if (mode.equalsIgnoreCase("edit_tblelems"))
            processTableElems();
        else{
            delete();
            cleanVisuals();
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    private void insert() throws Exception {
    	
    	// init the flag indicating if this is a common element
    	boolean elmCommon = req.getParameter("common")!=null; 

        // if making a copy, do the copy and return
		String copyElemID = req.getParameter("copy_elem_id");
		if (copyElemID != null && copyElemID.length()!=0){
			if (elmCommon)
				convertElm(copyElemID);
			else
				copyElem(copyElemID, dstNamespaceID);
			return;
		}

        // check missing parameters
		if (elmIdfier == null)
			throw new SQLException("Missing request parameter: idfier");
		if (!elmCommon && (this.tableID==null || this.tableID.length()==0))
			throw new Exception("Missing request parameter: table_id");
		
		// if non-common element, make sure such does not already exist within this table
		if (!elmCommon && existsInTable())
			throw new SQLException("The table already has an element with this Identifier");
		
		// if common element, make sure such does not already exist
		if (elmCommon && exists())
			throw new SQLException("An existing common element already this Identifier");

		// prepare SQL generator for element insert
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("IDENTIFIER", elmIdfier);
        if (elmShortName!=null)
        	gen.setField("SHORT_NAME", elmShortName);
        if (elmValuesType!=null)
        	gen.setField("TYPE", elmValuesType);
        if (!elmCommon){        	
        	gen.setFieldExpr("PARENT_NS", tblNamespaceID);
        	gen.setFieldExpr("TOP_NS", dstNamespaceID);
        }
        
		// set the element's registration status (relevant for common elements only)
		if (elmCommon){
			String regStatus = req.getParameter("reg_status");
			if (regStatus!=null && regStatus.length()>0)
				gen.setField("REG_STATUS", regStatus);
		}
        
        // set gis type
		String gisType = req.getParameter("gis");
		if (gisType!=null && gisType.length()==0 && isImportMode)
			gisType = null;
		if (gisType!=null && !gisType.equals("nogis"))
			gen.setField("GIS", gisType);

		// set IS_ROD_PARAM
		String isRodParam = req.getParameter("is_rod_param");
		if (isRodParam!=null){
			if (!isRodParam.equals("true") && !isRodParam.equals("false"))
				throw new Exception("Invalid value for is_rod_param!");
			gen.setField("IS_ROD_PARAM", isRodParam);
		}

        // if not in import mode, treat new common elements as working copies until checked in
		if (elmCommon && !isImportMode){
            gen.setField("WORKING_COPY", "Y");
            if (user!=null && user.isAuthentic())
                gen.setField("WORKING_USER", user.getUserName());
        }
		if (user!=null)
			gen.setField("USER", user.getUserName());
		if (date==null)
			date = String.valueOf(System.currentTimeMillis());
		gen.setFieldExpr("DATE", date);

		// execute element insert SQL
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(gen.insertStatement());
		setLastInsertID();
		
		// if non-common element, create row in TBL2ELEM
		if (!elmCommon){
			gen.clear();
	        gen.setTable("TBL2ELEM");
	        gen.setFieldExpr("TABLE_ID", tableID);
	        gen.setFieldExpr("DATAELEM_ID", getLastInsertID());
	        stmt.executeUpdate(gen.insertStatement());
		}

		// process the element's attributes
		processAttributes();
		
		// if this is a boolean element, auto-create the fixed values ("true" and "false")
		if (elmValuesType!=null && elmValuesType.equals("CH1") && mDatatypeID!=null && datatypeValue.equals("boolean"))
			autoCreateBooleanFixedValues(stmt, getLastInsertID());
		stmt.close();
		
		// if common element, create corresponding acl
		if (elmCommon && user!=null){
			String aclPath = "/elements/" + elmIdfier;
			HashMap acls = AccessController.getAcls();
			if (!acls.containsKey(aclPath)){
				String aclDesc = "Identifier: " + elmIdfier;
				try{
					AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
				}
				catch (Exception e){}
			}
		}
    }

    /**
     * @throws SQLException 
     * 
     *
     */
    public static void autoCreateBooleanFixedValues(Statement stmt, String elmID) throws SQLException {
    	
    	String[] values = new String[2];
    	values[0] = "true";
    	values[1] = "false";
    	for (int i=0; i<values.length; i++){
    		SQLGenerator gen = new SQLGenerator();
	    	gen.setTable("FXV");
	    	gen.setFieldExpr("OWNER_ID", elmID);
	    	gen.setField("OWNER_TYPE", "elem");
	    	gen.setField("VALUE", values[i]);
	    	gen.setField("DEFINITION", "Value auto-created by DD");
	    	gen.setField("SHORT_DESC", "Auto-created by DD");
	    	stmt.executeUpdate(gen.insertStatement());
    	}
	}

	/**
     * 
     * @throws Exception
     */
    private void update() throws Exception {
    	
		// init the flag indicating if this is a common element
		boolean elmCommon = req.getParameter("common")!=null;

		lastInsertID = delem_id;
		
		// if check-in, do the check-in and exit
		String checkIn = req.getParameter("check_in");
		if (checkIn!=null && checkIn.equalsIgnoreCase("true")){
        	
			VersionManager verMan = new VersionManager(conn, user);
			verMan.setContext(ctx);
			verMan.setServlRequestParams(req);
			
			String updVer = req.getParameter("upd_version");
			if (updVer!=null && updVer.equalsIgnoreCase("true")){
				verMan.updateVersion();
				setCheckedInCopyID(delem_id);
			}
			else
				setCheckedInCopyID(req.getParameter("checkedout_copy_id"));
			
			verMan.checkIn(delem_id, "elm", req.getParameter("reg_status"));
			return;
		}
		
		// prepare SQL generator for element update
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATAELEM");
		if (!Util.nullString(elmShortName))
			gen.setField("SHORT_NAME", elmShortName);

		// if common element, set regisration status
		if (elmCommon){
			String elmRegStatus = req.getParameter("reg_status");
			if (!Util.nullString(elmRegStatus))
				gen.setField("REG_STATUS", elmRegStatus);
		}
		
		// set IS_ROD_PARAM
		String isRodParam = req.getParameter("is_rod_param");
		if (isRodParam!=null){
			if (!isRodParam.equals("true") && !isRodParam.equals("false"))
				throw new Exception("Invalid value for is_rod_param!");
			gen.setField("IS_ROD_PARAM", isRodParam);
		}
		
		// set the gis type (relevant for common elements only)
		String gisType = req.getParameter("gis");
		if (gisType==null || gisType.equals("nogis"))
			gen.setFieldExpr("GIS", "NULL");
		else
			gen.setField("GIS", gisType);
        
		// execute element update SQL if at least one field was set
		if (!Util.nullString(gen.getValues()))
			conn.createStatement().executeUpdate(gen.updateStatement() + 
													" where DATAELEM_ID=" + delem_id);
        // handle element's attributes
        deleteAttributes();
        processAttributes();
    }

    /**
     * 
     * @throws Exception
     */
    private void delete() throws Exception {
    	
    	Statement stmt = null;
		try{
			// create SQL statement object and start transaction
			stmt = conn.createStatement();
	    	
	    	// do the delete
	    	delete(stmt);
		}
		finally{
			try{
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
		}
    }

	/**
	 * 
	 * @throws Exception
	 */
    private void delete(Statement stmt) throws Exception{
        
    	// this method handles deletion of elements and deletion of just tbl2elm relations
    	// of common elements in a given table
    	
    	// if deletion of tbl2elm relations of common elements in a given table is requested,
    	// do them first
    	String[] linkelms = req.getParameterValues("linkelm_id");
        if (linkelms!=null && linkelms.length!=0){
        	if (tableID==null || tableID.length()==0)
        		throw new Exception("Missing request parameter: table_id");
        	StringBuffer buf = new StringBuffer("delete from TBL2ELEM where TABLE_ID=");
    		buf.append(tableID).append(" and (");
    		for (int i=0; i<linkelms.length; i++){
    			if (i>0) buf.append(" or ");
    			buf.append("DATAELEM_ID=").append(linkelms[i]);
    		}
    		buf.append(")");
    		stmt.executeUpdate(buf.toString());
        }
        
        // if no deletion of elements requested, return
        if (delem_ids==null || delem_ids.length==0)
            return;
        
        // go through the given elements in database, make sure they can be deleted
        // and gather information we need when starting the deletion
        HashSet identifiers = new HashSet();
        HashSet unlockCheckedoutCopies = new HashSet();
        StringBuffer buf = new StringBuffer("select * from DATAELEM where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }
        ResultSet rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
        	// skip non-common elements, as this loop is relevant for common elements only
        	String parentNS = rs.getString("PARENT_NS");
        	if (parentNS!=null)
        		continue;
        	
        	String identifier = rs.getString("IDENTIFIER");
        	String workingCopy = rs.getString("WORKING_COPY");
        	String workingUser = rs.getString("WORKING_USER");
        	String regStatus = rs.getString("REG_STATUS");
        	if (workingCopy==null || regStatus==null || identifier==null)
        		throw new NullPointerException();
        	
        	identifiers.add(identifier);
        	if (workingCopy.equals("Y")){
        		if (workingUser==null && useForce==false)
        			throw new Exception("Working copy without a working user");
        		else if (!workingUser.equals(user.getUserName()) && useForce==false)
        			throw new Exception("Cannot delete working copy of another user");
        		else{
        			try{
        				String checkedOutCopyID = rs.getString("CHECKEDOUT_COPY_ID");
        				if (checkedOutCopyID!=null && checkedOutCopyID.length()>0)
        					unlockCheckedoutCopies.add(rs.getString("CHECKEDOUT_COPY_ID"));
        			}
        			catch (NullPointerException npe){}
        		}
        	}
        	else if (workingUser!=null && useForce==false)
        		throw new Exception("Element checked out by another user: " + workingUser);
        	else if (useForce==false){
        		boolean canDelete = false;
        		if (regStatus.equals("Released") || regStatus.equals("Recorded"))
        			canDelete = SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "er");
				else
					canDelete = SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "u") ||
								SecurityUtil.hasPerm(user.getUserName(), "/elements/" + identifier, "er");
        		if (!canDelete)
        			throw new Exception("You have no permission to delete this element: " +
        					rs.getString("DATAELEM_ID"));
        	}
        }
        
		// delete dependencies
		deleteAttributes();
		deleteComplexAttributes();
		deleteFixedValues();
		deleteFkRelations();
		deleteTableElem();
		
		// delete the elements themselves
		buf = new StringBuffer("delete from DATAELEM where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }
        stmt.executeUpdate(buf.toString());
	
        // remove acls of common elements whose identifiers are not present any more
        removeAcls(stmt, identifiers);
        
        // unlock checked out copies whose working copies were deleted
        if (unlockCheckedoutCopies.size()>0){
	        int i=0;
	        buf = new StringBuffer("update DATAELEM set WORKING_USER=NULL where ");
	        for (Iterator iter=unlockCheckedoutCopies.iterator(); iter.hasNext(); i++){
	        	if (i>0) buf.append(" or ");
	        	buf.append("DATAELEM_ID=").append(iter.next());
	        }
	        stmt.executeUpdate(buf.toString());
    	}
    }

    /**
     * 
     * @throws SQLException
     */
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
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }

        buf.append(") and PARENT_TYPE='E'");

		// skip image attributes        
        for (int i=0; i<imgAttrs.size(); i++)
        	buf.append(" and M_ATTRIBUTE_ID<>").append((String)imgAttrs.get(i));

        logger.debug(buf.toString());

        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteComplexAttributes() throws SQLException {

        for (int i=0; delem_ids!=null && i<delem_ids.length; i++){
            
            Parameters params = new Parameters();
            params.addParameterValue("mode", "delete");
            params.addParameterValue("legal_delete", "true");
            params.addParameterValue("parent_id", delem_ids[i]);
            params.addParameterValue("parent_type", "E");
            
            AttrFieldsHandler attrFieldsHandler =
                                new AttrFieldsHandler(conn, params, ctx);
            attrFieldsHandler.setVersioning(versioning);
            try{
                attrFieldsHandler.execute();
            }
            catch (Exception e){
                throw new SQLException(e.toString());
            }
        }
    }
    
    private void deleteRelations() throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from RELATION where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("PARENT_ID=");
            buf.append(delem_ids[i]);
        }

        logger.debug(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }

    private void deleteFixedValues() throws Exception {
        
        StringBuffer buf = new StringBuffer().
        append("select distinct FXV_ID from FXV where ").
        append("OWNER_TYPE='elem' and (");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("OWNER_ID=");
            buf.append(delem_ids[i]);
        }
        buf.append(")");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        Parameters pars = new Parameters();
        while (rs.next()){
            pars.addParameterValue("del_id", rs.getString("FXV_ID"));
        }
        stmt.close();
        
        pars.addParameterValue("mode", "delete");
        pars.addParameterValue("legal_delete", "true");
        FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
        fvHandler.setVersioning(versioning);
        fvHandler.execute();
    }
    
    private void deleteFkRelations() throws Exception{
		StringBuffer buf = new StringBuffer();
		buf.append("delete from FK_RELATION where ");
		for (int i=0; i<delem_ids.length; i++){
			if (i>0)
				buf.append(" or ");
			buf.append("A_ID=");
			buf.append(delem_ids[i]);
			buf.append(" or B_ID=");
			buf.append(delem_ids[i]);
		}
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(buf.toString());
		stmt.close();
    }
    
    private void insertTableElem() throws SQLException {

        if (tableID == null || tableID.length()==0)
            return;
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("TBL2ELEM");
        gen.setField("TABLE_ID", tableID);
        gen.setField("DATAELEM_ID", getLastInsertID());
        
        String position = req.getParameter("pos");
        if (Util.nullString(position))
            position = getTableElemPos();
		    
        gen.setField("POSITION", position);

        String sql = gen.insertStatement();
        logger.debug(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    private String getTableElemPos() throws SQLException{

        StringBuffer buf = new StringBuffer().
		append("select max(POSITION) from TBL2ELEM where TABLE_ID=").
        append(tableID);

        logger.debug(buf.toString());

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        rs.clearWarnings();

        String pos=null;
        if (rs.next())
            pos = rs.getString(1);
        stmt.close();
        if (pos != null){
            try {
              int i = Integer.parseInt(pos) + 1;
              return Integer.toString(i);
            }
            catch(Exception e){
                return "1";
            }
        }

        return "1";
    }
    private void deleteTableElem() throws SQLException {

        if (delem_ids==null || delem_ids.length==0)
            return;

        StringBuffer buf = new StringBuffer("delete from TBL2ELEM where ");
        for (int i=0; i<delem_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(delem_ids[i]);
        }

        logger.debug(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    private void processTableElems() throws Exception {

        String[] posIds = req.getParameterValues("pos_id");
        String old_pos=null;
        String pos=null;
        String parName=null;
        if (posIds==null || posIds.length==0) return;
        if (tableID==null || tableID.length()==0) return;

        for (int i=0; i<posIds.length; i++){
            old_pos = req.getParameter(OLDPOS_PREFIX + posIds[i]);
            pos = req.getParameter(POS_PREFIX + posIds[i]);
            if (old_pos.length()==0 || pos.length()==0)
                continue;
            if (!old_pos.equals(pos))
                updateTableElems(posIds[i], pos);
        }
    }
    private void updateTableElems(String elemId, String pos) throws Exception {
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("TBL2ELEM");

        gen.setField("POSITION", pos);

        StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
        sqlBuf.append(" where TABLE_ID=");
        sqlBuf.append(tableID);
        sqlBuf.append(" and DATAELEM_ID=");
        sqlBuf.append(elemId);

        logger.debug(sqlBuf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sqlBuf.toString());
        stmt.close();
    }
    
    /**
     * 
     * @throws Exception
     */
    private void processAttributes() throws Exception {
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
              if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from table level
              insertAttribute(attrID, attrValue);
            }
            else if(parName.startsWith(ATTR_MULT_PREFIX)){
              String[] attrValues = req.getParameterValues(parName);
              if (attrValues == null || attrValues.length == 0) continue;

              attrID = parName.substring(ATTR_MULT_PREFIX.length());
              if (req.getParameterValues(INHERIT_ATTR_PREFIX + attrID)!=null) continue;  //some attributes will be inherited from table level

              for (int i=0; i<attrValues.length; i++){
                  insertAttribute(attrID, attrValues[i]);
              }
            }
            else if (parName.startsWith(INHERIT_ATTR_PREFIX) &&
                  !parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
              attrID = parName.substring(INHERIT_ATTR_PREFIX.length());
              if (tableID==null) continue;
              CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
              ch.setUser(user);
              ch.copyAttribute(lastInsertID, tableID, "E", "T", attrID);
            }
            else if (parName.startsWith(INHERIT_COMPLEX_ATTR_PREFIX)){
              attrID = parName.substring(INHERIT_COMPLEX_ATTR_PREFIX.length());
              if (tableID==null) continue;
              CopyHandler ch = new CopyHandler(conn, ctx, searchEngine);
			  ch.setUser(user);
              ch.copyComplexAttrs(lastInsertID, tableID, "T", "E", attrID);
            }
        }

        // if there is a Datatype attribute and its value wasn't specified,
        // make it a string.
        if (!Util.nullString(mDatatypeID)){
            if (datatypeValue==null){
                insertAttribute(mDatatypeID, "string");
            }
        }
    }
    
    private void insertAttribute(String attrId, String value) throws Exception {
    	
        // for CH1 certain attributes are not allowed
        if (elmValuesType!=null && elmValuesType.equals("CH1") && ch1ProhibitedAttrs.contains(attrId))
            return;

        // 'Datatype' attribute needs special handling
        if (mDatatypeID!=null && attrId.equals(mDatatypeID)){
            
            // a CH2 cannot be of 'boolean' datatype
            if (elmValuesType!=null && elmValuesType.equals("CH2"))
                if (value.equalsIgnoreCase("boolean"))
                    throw new Exception("An element of CH2 type cannot be a boolean!");
            
            // make sure that the value matches fixed values for 'Datatype'
            // we can do this in insertAttribute() only, because the problem
            // comes from Import tool only.
            if (searchEngine==null) searchEngine = new DDSearchEngine(conn, "", ctx);
            Vector v = searchEngine.getFixedValues(attrId, "attr");
            boolean hasMatch = false;
            for (int i=0; v!=null && i<v.size(); i++){
                FixedValue fxv = (FixedValue)v.get(i);
                if (value.equals(fxv.getValue())){
                    hasMatch = true;
                    break;
                }
            }
            
            if (!hasMatch)
                throw new Exception("Unknown datatype for element " + elmIdfier);
                
            datatypeValue = value;
        }
                    
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");

        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setFieldExpr("DATAELEM_ID", lastInsertID);
        gen.setField("VALUE", value);
        gen.setField("PARENT_TYPE", "E");

        String sql = gen.insertStatement();
        logger.debug(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private void updateAttribute(String attrId, String value) throws SQLException {
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");

        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setFieldExpr("DATAELEM_ID", delem_id);
        gen.setFieldExpr("PARENT_TYPE", "E");
        gen.setField("VALUE", value);

        String sql = gen.updateStatement();
        logger.debug(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void setLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";

        logger.debug(qry);

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

    /**
     * 
     * @param copyElemID
     * @param topNS
     * @throws Exception
     */
    private void copyElem(String copyElemID, String topNS) throws Exception{

        if (copyElemID==null) return;

        CopyHandler copier = new CopyHandler(conn, ctx, searchEngine);
		copier.setUser(user);
        lastInsertID = copier.copyElm(copyElemID, false, false, false);

        if (lastInsertID==null)
            return;

        SQLGenerator gen = new SQLGenerator();

        gen.setTable("DATAELEM");
        gen.setField("IDENTIFIER", elmIdfier);
        gen.setFieldExpr("PARENT_NS", tblNamespaceID);
		gen.setFieldExpr("TOP_NS", topNS);
        gen.setField("VERSION", "1");
        if (versioning==false){
            if (user!=null && user.isAuthentic())
                gen.setField("USER", user.getUserName());
        }
        else{
            gen.setField("WORKING_COPY", "Y");
            if (user!=null && user.isAuthentic())
                gen.setField("WORKING_USER", user.getUserName());
        }
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));

        String q = gen.updateStatement() + " where DATAELEM_ID=" + lastInsertID;
        logger.debug(q);
        conn.createStatement().executeUpdate(q);

        insertTableElem();

    }
    
    /**
     * 
     * @param copyElemID
     * @throws Exception
     */
	private void convertElm(String copyElemID) throws Exception{

		if (copyElemID==null) return;

		CopyHandler copier = new CopyHandler(conn, ctx, searchEngine);
		copier.setUser(user);
		lastInsertID = copier.copyElm(copyElemID, false, false, false);
		if (lastInsertID==null) return;

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATAELEM");
		gen.setField("IDENTIFIER", elmIdfier);
		gen.setField("VERSION", "1");
		gen.setFieldExpr("PARENT_NS", "NULL");
		gen.setFieldExpr("TOP_NS", "NULL");
		if (versioning==false){
			if (user!=null && user.isAuthentic())
				gen.setField("USER", user.getUserName());
		}
		else{
			gen.setField("WORKING_COPY", "Y");
			if (user!=null && user.isAuthentic())
				gen.setField("WORKING_USER", user.getUserName());
		}
		gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));

		String q = gen.updateStatement() + " where DATAELEM_ID=" + lastInsertID;
		conn.createStatement().executeUpdate(q);
	}
    
	/**
	 * @param elmCommon
	 * @return
	 * @throws SQLException
	 */
	private boolean existsInTable() throws SQLException {
		
		StringBuffer buf = new StringBuffer();
        buf.append("select count(DATAELEM.DATAELEM_ID) from TBL2ELEM ").
        append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID where ").
        append("TBL2ELEM.TABLE_ID=").append(this.tableID).
        append(" and DATAELEM.DATAELEM_ID is not null and DATAELEM.IDENTIFIER=").
        append(Util.strLiteral(this.elmIdfier));
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        if (rs.next()){
            if (rs.getInt(1)>0)
                return true;
        }

        return false;
	}
	
	/**
	 * 
	 * @param elmCommon
	 * @return
	 * @throws SQLException
	 */
	private boolean exists() throws SQLException {

		StringBuffer buf = new StringBuffer();
		buf.append("select count(*) as COUNT from DATAELEM where PARENT_NS is null and IDENTIFIER=");
		buf.append(com.tee.util.Util.strLiteral(elmIdfier));
    
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		if (rs.next()){
			if (rs.getInt("COUNT")>0){
				return true;
			}
		}
		stmt.close();
		return false;
	}

    public boolean getCheckInResult(){
        return this.checkInResult;
    }
    
	public String getRestoredID(){
		return this.restoredID;
	}

	private void setLatestCommonElmID(String idf) throws SQLException{
		
		VersionManager verMan = new VersionManager(conn, user);
		DataElement elm = new DataElement();
		elm.setIdentifier(idf);
		this.latestCommonElmID = verMan.getLatestElmID(elm);
	}
	
	public String getLatestCommonElmID(){
		return latestCommonElmID;
	}
	
	/*
	 * 
	 */
	public void setDate(String unixTimestampMillisec){
		this.date = unixTimestampMillisec;
	}

	/**
	 * 
	 * @return
	 */
    public String getCheckedInCopyID() {
		return checkedInCopyID;
	}

	/**
	 * 
	 * @param latestDatasetID
	 */
    private void setCheckedInCopyID(String latestDatasetID) {
		this.checkedInCopyID = latestDatasetID;
	}

    /**
     * 
     * @param importMode
     */
	public void setImportMode(boolean importMode){
		this.isImportMode = importMode;
	}

    /**
     * 
     * @param useForce
     */
    public void setUseForce(boolean useForce) {
		this.useForce = useForce;
	}

    /**
     * 
     * @param oldID
     * @param newID
     * @param conn
     */
    public static void replaceID(String oldID, String newID, Connection conn) throws SQLException{
    	
    	Statement stmt = null;
    	SQLGenerator gen = null;
    	StringBuffer buf = null;
    	try{
    		stmt = conn.createStatement();
	    	
    		gen = new SQLGenerator();
        	gen.setTable("DATAELEM");
        	gen.setFieldExpr("DATAELEM_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where DATAELEM_ID=").append(oldID);
			stmt.executeUpdate(buf.toString());
			
			gen.clear();
			gen.setTable("ATTRIBUTE");
        	gen.setFieldExpr("DATAELEM_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where PARENT_TYPE='E' and DATAELEM_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
        	
			gen.clear();
			gen.setTable("COMPLEX_ATTR_ROW");
        	gen.setFieldExpr("PARENT_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where PARENT_TYPE='E' and PARENT_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
        	
        	gen.clear();
			gen.setTable("TBL2ELEM");
        	gen.setFieldExpr("DATAELEM_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where DATAELEM_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
        	
        	gen.clear();
			gen.setTable("FK_RELATION");
        	gen.setFieldExpr("A_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where A_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
        	gen.clear();
			gen.setTable("FK_RELATION");
        	gen.setFieldExpr("B_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where B_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
        	
        	gen.clear();
			gen.setTable("FXV");
        	gen.setFieldExpr("OWNER_ID", newID);
        	buf = new StringBuffer(gen.updateStatement());
        	buf.append(" where OWNER_TYPE='elem' and OWNER_ID=").append(oldID);
        	stmt.executeUpdate(buf.toString());
    	}
    	finally{
			try{
				if (stmt!=null) stmt.close();
			}
			catch (SQLException e){}
    	}
    }

    /**
     * Removes ACLs of those common elements not present any more in IDENTIFIER from DATAELEM.
     * NB! Modifies the identifiers HashSet by removing those whose acl is not to be deleted.
     * 
     * @throws SQLException 
     * @throws SignOnException 
     */
    private void removeAcls(Statement stmt, HashSet identifiers) throws SQLException, SignOnException{
    	
    	ResultSet rs = stmt.executeQuery("select distinct IDENTIFIER from DATAELEM");
    	while (rs.next()){
    		String identifier = rs.getString(1);
    		if (identifiers.contains(identifier))
    			identifiers.remove(identifier);
    	}
    	
    	if (identifiers.size()==0)
    		return;
    	
    	int i=0;
    	for (Iterator iter = identifiers.iterator(); iter.hasNext(); i++){
    		AccessController.removeAcl("/elements/" + (String)iter.next());
    	}		
    }

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		Connection conn = null;
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			conn =
				DriverManager.getConnection("jdbc:mysql://192.168.10.15:3306/jaanusDD", "dduser", "xxx");

			DataElementHandler.replaceID("16956", "16953", conn);
	   }
		catch (Exception e){
			System.out.println(e.toString());
			e.printStackTrace(new PrintStream(System.out));
		}
		finally{
			if (conn!=null){
				try{
					conn.close();
				}
				catch (SQLException e){}
			}
		}
        
	}
}
