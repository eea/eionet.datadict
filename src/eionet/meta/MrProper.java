package eionet.meta;

import eionet.meta.savers.*;

import java.util.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import com.tee.util.Util;
import com.tee.xmlserver.AppUserIF;

public class MrProper {
    
    public static final String FUNCTIONS_PAR = "functs";
    public static final String DST_NAME = "dsname";
    
    public static final String RLS_DST = "rls_dst";
    public static final String ORPHAN_ELM = "orphan_elm";
    public static final String ORPHAN_TBL = "orphan_tbl";
    public static final String RMV_MULT_VERS = "rmv_mult_vers";
    public static final String RLS_NOWC = "rls_nowc";
	public static final String RMV_WC_NORIG = "rmv_wc_noorig";
    
    /** */
    private Connection conn = null;
    private ServletContext ctx = null;
    private AppUserIF user = null;
    private Vector response = new Vector();
    
    private Hashtable funNames = null;
    
    /**
    *
    */
    public MrProper(Connection conn){
        this.conn = conn;
        
        funNames = new Hashtable();
        funNames.put(RLS_DST, "Releasing the dataset");
        funNames.put(ORPHAN_ELM, "Deleting elements without parent tables");
        funNames.put(ORPHAN_TBL, "Deleting tables without parent datasets");
        funNames.put(RMV_MULT_VERS, "Removing multiple versions");
        funNames.put(RLS_NOWC, "Releasing locked objects");
		funNames.put(RMV_WC_NORIG, "Removing working copies with no originals");
    }
    
    /**
    *
    */
    public void setContext(ServletContext ctx){
        this.ctx = ctx;
    }
    
    /**
    *
    */
    public void setUser(AppUserIF user){
        this.user = user;
    }
    
    /**
    *
    */
    public void execute(HttpServletRequest req){
        execute(new Parameters(req));
    }
    
    /**
    *
    */
    public void execute(Parameters pars){
        
        // check if user is authentic
        if (user==null || !user.isAuthentic()){
            response.add("Unauthorized user!");
            return;
        }
        
        /* check the user permissions
        try{
            AccessControlListIF acl = getAcl(DDuser.ACL_SERVICE_NAME );
            boolean isOK =
                acl.checkPermission(userName, DDuser.ACL_CLEANUP_PRM);
            if (!isOK)
                throw new Exception("User " + userName +
                                    " does not have this permission!");
        }
        catch (Exception e){
            response.add(e.getMessage());
            return;
        }*/
        
        // start execution
        String[] functs = pars.getParameterValues(FUNCTIONS_PAR);
        if (functs==null || functs.length==0){
            response.add("No functions specified!");
            return;
        }
        
        for (int i=0; i<functs.length; i++){
            
            String fun = functs[i];
            
            try{
                if (fun.equals(RLS_DST))
                    releaseDataset(pars.getParameter(DST_NAME));
                else if (fun.equals(ORPHAN_ELM))
                    orphanElements();
                else if (fun.equals(ORPHAN_TBL))
                    orphanTables();
                else if (fun.equals(RMV_MULT_VERS))
                    multipleVersions();
                else if (fun.equals(RLS_NOWC))
                    releaseNonWC();
                else if (fun.equals(RMV_WC_NORIG))
					removeHangingWCs();
            }
            catch (Exception e){
                response.add((String)funNames.get(fun) +
                                " failed: <b>" + e.toString() + "</b>");
                continue;
            }
            
            response.add((String)funNames.get(fun) + " was <b>OK!</b>");
        }
    }
    
    /**
    *
    */
    private void releaseDataset(String shortName) throws Exception{
        
        if (Util.nullString(shortName))
            throw new Exception("Dataset short name not given!");
        
        String q = "select distinct CORRESP_NS from DATASET where " +
                   "SHORT_NAME=" + Util.strLiteral(shortName);
        
        String ns = null;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        if (rs.next())
            ns = rs.getString(1);
        
        if (ns!=null)
            stmt.execute("update NAMESPACE set WORKING_USER=NULL " +
                         "where NAMESPACE_ID=" + ns);
        stmt.close();
    }
    
    /**
    *
    */
    private void orphanElements() throws Exception{
    	
		// There might be elements that have relations
		// both to existing tables and non-existing tables.
		// So first find the tables that are present in TBL2ELEM,
		// but actually do not exist any more.
		// Then remove all TBL2ELEM rows with such tables.
		// And then delete all elements that do not seem to have a
		// parent table by join through TBL2ELEM->DS_TABLE.

		// find & delete related, yet non-existing tables
		String q =
		"select distinct TBL2ELEM.TABLE_ID from TBL2ELEM " +
		"left outer join DS_TABLE " +
		"on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
		"where DS_TABLE.SHORT_NAME is null";

		Vector v = new Vector();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		while (rs.next())
			v.add(rs.getString(1));
		
		for (int i=0; i<v.size(); i++)
			stmt.executeQuery("delete from TBL2ELEM where TABLE_ID=" +
															(String)v.get(i));
       
        // get the elements
        q =
        "select distinct DATAELEM.DATAELEM_ID from DATAELEM " +
        "left outer join TBL2ELEM " +
        "on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
        "left outer join DS_TABLE " +
        "on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
        "where DS_TABLE.SHORT_NAME is null";
        
        v = new Vector();
        rs = stmt.executeQuery(q);
        while (rs.next())
            v.add(rs.getString(1));
        
        if (v.size()==0) return;
        
        // delete the found elements
        Parameters params = new Parameters();
        params.addParameterValue("mode", "delete");
        for (int i=0; i<v.size(); i++)
			params.addParameterValue("delem_id", (String)v.get(i));
			
        DataElementHandler delemHandler =
								new DataElementHandler(conn, params, ctx);
		delemHandler.setUser(user);
        delemHandler.setVersioning(false);
        delemHandler.setSuperUser(true);
		delemHandler.execute();
        
        // close statement
        stmt.close();
    }
    
    private void orphanTables() throws Exception{
    	
    	// There might be tables that have relations
    	// both to existing datasets and non-existing datasets.
    	// So first find the datasets that are present in DST2TBL,
    	// but actually do not exist any more.
    	// Then remove all DST2TBL rows with such datasets
    	// And then delete all tables that do not seem to have a
    	// parent dataset by join through DST2TBL->DATASET.
        
        
		// find & delete related, yet non-existing datasets		
		String q =
		"select distinct DST2TBL.DATASET_ID from DST2TBL " +
		"left outer join DATASET " +
		"on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
		"where DATASET.SHORT_NAME is null";
		
		Vector v = new Vector();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		while (rs.next())
			v.add(rs.getString(1));
		
		for (int i=0; i<v.size(); i++)
			stmt.executeQuery("delete from DST2TBL where DATASET_ID=" +
															(String)v.get(i));
		
        // get orphan tables
        q =
        "select distinct DS_TABLE.TABLE_ID from DS_TABLE " +
        "left outer join DST2TBL " +
        "on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
        "left outer join DATASET " +
        "on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
        "where DATASET.SHORT_NAME is null";
        
        v = new Vector();
        rs = stmt.executeQuery(q);
        while (rs.next())
            v.add(rs.getString(1));
        
        if (v.size()==0) return;
        
        // delete the found tables
        Parameters params = new Parameters();
        params.addParameterValue("mode", "delete");
        for (int i=0; i<v.size(); i++)
			params.addParameterValue("del_id", (String)v.get(i));
			
        DsTableHandler dsTableHandler =
								new DsTableHandler(conn, params, ctx);
		dsTableHandler.setUser(user);
        dsTableHandler.setVersioning(false);
        dsTableHandler.setSuperUser(true);
		dsTableHandler.execute();
        
        // close statement
        stmt.close();
    }
    
    /**
    *
    */
    private void multipleVersions() throws Exception{
    	
    	// data elements
    	StringBuffer buf = new StringBuffer().
    	append("select * from DATAELEM order by DATE desc");
		
		Vector odd = new Vector();
		HashSet all = new HashSet();
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			
			if (rs.getString("WORKING_COPY").equals("Y"))
				continue;
			
			String parentNs = rs.getString("PARENT_NS");
			if (parentNs==null) parentNs="";
			
			Hashtable hash = new Hashtable();			
			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
			hash.put("PARENT_NS", parentNs);
			hash.put("VERSION", rs.getString("VERSION"));
			if (all.contains(hash))
				odd.add(rs.getString("DATAELEM_ID"));
			else
				all.add(hash);
		}
		
		for (int i=0; i<odd.size(); i++){
			
			String id = (String)odd.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("delem_id", (String)odd.get(i));

			DataElementHandler h = new DataElementHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			h.execute();
		}

		// tables
		buf = new StringBuffer().
		append("select * from DS_TABLE order by DATE desc");
		
		odd = new Vector();
		all = new HashSet();
		
		rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			
			if (rs.getString("WORKING_COPY").equals("Y"))
				continue;

			String parentNs = rs.getString("PARENT_NS");
			if (parentNs==null) parentNs="";
			
			Hashtable hash = new Hashtable();
			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
			hash.put("PARENT_NS", parentNs);
			hash.put("VERSION", rs.getString("VERSION"));
			if (all.contains(hash))
				odd.add(rs.getString("TABLE_ID"));
			else
				all.add(hash);
		}
		
		for (int i=0; i<odd.size(); i++){
			String id = (String)odd.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("del_id", (String)odd.get(i));

			DsTableHandler h = new DsTableHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			h.execute();
		}

		// datasets
		buf = new StringBuffer().
		append("select * from DATASET order by DATE desc");
		
		odd = new Vector();
		all = new HashSet();
		
		rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			
			if (rs.getString("WORKING_COPY").equals("Y"))
				continue;
			
			Hashtable hash = new Hashtable();
			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
			hash.put("VERSION", rs.getString("VERSION"));
			if (all.contains(hash))
				odd.add(rs.getString("DATASET_ID"));
			else
				all.add(hash);
		}
		
		for (int i=0; i<odd.size(); i++){
			String id = (String)odd.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("ds_id", (String)odd.get(i));

			DatasetHandler h = new DatasetHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			h.execute();
		}
    }
    
    /**
    *
    */
    private void releaseNonWC() throws Exception{
        //releaseNonWC("DATAELEM");
        //releaseNonWC("DS_TABLE");
        releaseNonWC("DATASET");
    }
    
    /**
    *
    */
    private void releaseNonWC(String tblName) throws Exception{
        
        // get the locked non-wcs
        StringBuffer buf = new StringBuffer();
		buf.append("select distinct SHORT_NAME, VERSION");
        if (!tblName.equals("DATASET"))
            buf.append(", PARENT_NS");
        buf.append(" from ");
        buf.append(tblName);
        buf.append(" where WORKING_USER is not null");
        
        //System.out.println(buf.toString());
 
        Vector v = new Vector();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
        	Hashtable hash = new Hashtable();
            hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
			hash.put("VERSION", rs.getString("VERSION"));
			if (!tblName.equals("DATASET"))
				hash.put("PARENT_NS", rs.getString("PARENT_NS"));
            v.add(hash);
        }
        
        
        // get the wcs
        buf = new StringBuffer();
		buf.append("select distinct SHORT_NAME, VERSION");
        if (!tblName.equals("DATASET"))
            buf.append(", PARENT_NS");
        buf.append(" from ");
        buf.append(tblName);
        buf.append(" where WORKING_COPY='Y'");
        
        //System.out.println(buf.toString());
        
        HashSet wcs = new HashSet();
        rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
			Hashtable hash = new Hashtable();
			hash.put("SHORT_NAME", rs.getString("SHORT_NAME"));
			hash.put("VERSION", rs.getString("VERSION"));
			if (!tblName.equals("DATASET"))
				hash.put("PARENT_NS", rs.getString("PARENT_NS"));
			wcs.add(hash);
        }
        
        // loop over locked objects, delete those not present in WC hash 
        for (int i=0; i<v.size(); i++){
            
            Hashtable hash = (Hashtable)v.get(i);
            
            if (wcs.contains(hash)) // if has a WC then skip
            	continue;
            	
            buf = new StringBuffer();
            buf.append("update ");
			buf.append(tblName);
			buf.append(" set WORKING_USER=NULL where SHORT_NAME=");
			buf.append(Util.strLiteral((String)hash.get("SHORT_NAME")));
			buf.append(" and VERSION=");
			buf.append((String)hash.get("VERSION"));
			if (!tblName.equals("DATASET")){
				buf.append(" and PARENT_NS=");
				buf.append((String)hash.get("PARENT_NS"));
			}
            
            stmt.executeUpdate(buf.toString());
        }
        
        stmt.close();
    }
    
    /*
     * 
     */
    private void removeHangingWCs() throws Exception{
    	
		// data elements
		StringBuffer buf = new StringBuffer().
		append("select count(*) from DATAELEM where WORKING_COPY='N' and ").
		append("WORKING_USER=").append(Util.strLiteral(user.getUserName())).
		append(" and SHORT_NAME=? and PARENT_NS=? and VERSION=? and ").
		append("DATE<?");

		PreparedStatement pstmt= conn.prepareStatement(buf.toString());

		Vector hangingWcs = new Vector();
		 
    	Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.
    	executeQuery("select * from DATAELEM where WORKING_COPY='Y'");
    	
    	while (rs.next()){
    		// execute prep statement with qry for original
			pstmt.setString(1, rs.getString("SHORT_NAME"));
			pstmt.setInt(2, rs.getInt("PARENT_NS"));
			pstmt.setInt(3, rs.getInt("VERSION"));
			pstmt.setLong(4, rs.getLong("DATE"));
			ResultSet rs2 = pstmt.executeQuery();

    		// if no original found, add WC ID to hash
			if (!rs2.next() || rs2.getInt(1)==0)
				hangingWcs.add(rs.getString("DATAELEM_ID"));
    	}
    	
    	for (int i=0; i<hangingWcs.size(); i++){
    		
			String id = (String)hangingWcs.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("delem_id", id);

			DataElementHandler h = new DataElementHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			h.execute();
    	}
    	
		// tables
		buf = new StringBuffer().
		append("select count(*) from DS_TABLE where WORKING_COPY='N' and ").
		append("WORKING_USER=").append(Util.strLiteral(user.getUserName())).
		append(" and SHORT_NAME=? and PARENT_NS=? and VERSION=? and ").
		append("DATE<?");

		pstmt= conn.prepareStatement(buf.toString());

		hangingWcs = new Vector();
		 
		stmt = conn.createStatement();
		rs = stmt.executeQuery("select * from DS_TABLE where WORKING_COPY='Y'");
    	
		while (rs.next()){
			// execute prep statement with qry for original
			pstmt.setString(1, rs.getString("SHORT_NAME"));
			pstmt.setInt(2, rs.getInt("PARENT_NS"));
			pstmt.setInt(3, rs.getInt("VERSION"));
			pstmt.setLong(4, rs.getLong("DATE"));
			ResultSet rs2 = pstmt.executeQuery();

			// if no original found, add WC ID to hash
			if (!rs2.next() || rs2.getInt(1)==0)
				hangingWcs.add(rs.getString("TABLE_ID"));
		}
    	
		for (int i=0; i<hangingWcs.size(); i++){
    		
			String id = (String)hangingWcs.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("del_id", id);

			DsTableHandler h = new DsTableHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			//h.execute();
			System.out.println("table " + id);
		}

		// datasets
		buf = new StringBuffer().
		append("select count(*) from DATASET where WORKING_COPY='N' and ").
		append("WORKING_USER=").append(Util.strLiteral(user.getUserName())).
		append(" and SHORT_NAME=? and VERSION=? and ").
		append("DATE<?");

		pstmt= conn.prepareStatement(buf.toString());

		hangingWcs = new Vector();
		 
		stmt = conn.createStatement();
		rs = stmt.executeQuery("select * from DATASET where WORKING_COPY='Y'");
    	
		while (rs.next()){
			// execute prep statement with qry for original
			pstmt.setString(1, rs.getString("SHORT_NAME"));
			pstmt.setInt(2, rs.getInt("VERSION"));
			pstmt.setLong(3, rs.getLong("DATE"));
			ResultSet rs2 = pstmt.executeQuery();

			// if no original found, add WC ID to hash
			if (!rs2.next() || rs2.getInt(1)==0)
				hangingWcs.add(rs.getString("DATASET_ID"));
		}
    	
		for (int i=0; i<hangingWcs.size(); i++){
    		
			String id = (String)hangingWcs.get(i);
			
			Parameters pars = new Parameters();
			pars.addParameterValue("mode", "delete");
			pars.addParameterValue("complete", "true");
			pars.addParameterValue("ds_id", id);

			DatasetHandler h = new DatasetHandler(conn, pars, null);
			h.setUser(user);
			h.setVersioning(false);
			//h.execute();
			System.out.println("dataset " + id);
		}
    }
    
    /**
    *
    */
    public Vector getResponse(){
        return response;
    }
    
    /**
    *
    */
    public static void main(String[] args){

        MrProper mrProper = null;
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn = DriverManager.getConnection(
			//"jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
			"jdbc:mysql://192.168.1.6:3306/DataDict", "dduser", "xxx");

			AppUserIF testUser = new TestUser();
			testUser.authenticate("jaanus", "jaanus");

			/*StringBuffer buf = new StringBuffer().
			append("select count(*) from DATAELEM where WORKING_COPY='N'").
			append(" and SHORT_NAME=? and PARENT_NS=? and VERSION=?");

			PreparedStatement pstmt= conn.prepareStatement(buf.toString());
			pstmt.setString(1, "enriko");
			pstmt.setInt(2, 197);
			pstmt.setInt(3, 1);
			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				System.out.println(rs.getString(1));
			} */

            mrProper = new MrProper(conn);    
            mrProper.setUser(testUser);
            
            Parameters pars = new Parameters();
            //pars.addParameterValue(FUNCTIONS_PAR, ORPHAN_ELM);
            //pars.addParameterValue(FUNCTIONS_PAR, ORPHAN_TBL);
            pars.addParameterValue(FUNCTIONS_PAR, RMV_MULT_VERS);            
			//pars.addParameterValue(FUNCTIONS_PAR, RMV_WC_NORIG);
            
            mrProper.execute(pars);
            
            System.out.println(mrProper.getResponse());
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        finally{
        }
    }
}