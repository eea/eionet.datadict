package eionet.meta;

import eionet.meta.savers.*;

import java.util.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import com.tee.util.Util;
import com.tee.xmlserver.AppUserIF;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

public class MrProper {
    
    public static final String FUNCTIONS_PAR = "functs";
    public static final String DST_NAME = "dsname";
    
    public static final String RLS_DST = "rls_dst";
    public static final String ORPHAN_ELM = "orphan_elm";
    public static final String ORPHAN_TBL = "orphan_tbl";
    public static final String RMV_MULT_VERS = "rmv_mult_vers";
    public static final String RLS_NOWC = "rls_nowc";
    
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
        funNames.put(RMV_MULT_VERS, "");
        funNames.put(RLS_NOWC, "Releasing locked objects");
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
            }
            catch (Exception e){
                response.add((String)funNames.get(fun) +
                                " failed: <b>" + e.getMessage() + "</b>");
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
        
        // get the elements
        String q =
        "select distinct DATAELEM.DATAELEM_ID from DATAELEM " +
        "left outer join TBL2ELEM " +
        "on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID " +
        "left outer join DS_TABLE " +
        "on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID " +
        "where DS_TABLE.SHORT_NAME is null";
        
        Vector v = new Vector();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
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
        
        // get the tables
        String q =
        "select distinct DS_TABLE.TABLE_ID from DS_TABLE " +
        "left outer join DST2TBL " +
        "on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID " +
        "left outer join DATASET " +
        "on DST2TBL.DATASET_ID=DATASET.DATASET_ID " +
        "where DATASET.SHORT_NAME is null";
        
        Vector v = new Vector();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
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
        throw new Exception("Not supported right now!");
    }
    
    /**
    *
    */
    private void releaseNonWC() throws Exception{
        releaseNonWC("DATAELEM");
        releaseNonWC("DS_TABLE");
        releaseNonWC("DATASET");
    }
    
    /**
    *
    */
    private void releaseNonWC(String tblName) throws Exception{
        
        // get the locked non-wcs
        StringBuffer buf = new StringBuffer("select distinct SHORT_NAME");
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
            String s = rs.getString(1);
            if (!tblName.equals("DATASET"))
                s = s + "," + rs.getString(2);                
            v.add(s);
        }
        
        
        // get the wcs
        buf = new StringBuffer("select distinct SHORT_NAME");
        if (!tblName.equals("DATASET"))
            buf.append(", PARENT_NS");
        buf.append(" from ");
        buf.append(tblName);
        buf.append(" where WORKING_COPY='Y'");
        
        //System.out.println(buf.toString());
        
        HashSet hash = new HashSet();
        rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
            String s = rs.getString(1);
            if (!tblName.equals("DATASET"))
                s = s + "," + rs.getString(2);                
            hash.add(s);
        }
        
        // prune out locked non-wcs that have wcs
        for (int i=0; i<v.size(); i++){
            if (hash.contains(v.get(i))){
                v.remove(i);
                i--;
            }
        }
        
        // delete remaining non-wcs 
        for (int i=0; i<v.size(); i++){
            
            String s = (String)v.get(i);
            int k = s.indexOf(",");
            
            buf = new StringBuffer();
            buf.append("update DATAELEM set WORKING_USER=NULL ");
            buf.append("where SHORT_NAME=");
            if (k<0)
                buf.append(Util.strLiteral(s));
            else{
                buf.append(Util.strLiteral(s.substring(0,k)));
                buf.append(" and PARENT_NS=");
                buf.append(s.substring(k+1));
            }
            
            //System.out.println(buf.toString());
            
            stmt.executeUpdate(buf.toString());
        }
        
        stmt.close();
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
                "jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
        
            AppUserIF testUser = new TestUser();
            testUser.authenticate("jaanus", "jaanus");
            
            mrProper = new MrProper(conn);
            mrProper.setUser(testUser);
            
            Parameters pars = new Parameters();
            //pars.addParameterValue(FUNCTIONS_PAR, ORPHAN_ELM);
            //pars.addParameterValue(FUNCTIONS_PAR, ORPHAN_TBL);
            pars.addParameterValue(FUNCTIONS_PAR, RLS_NOWC);            
            
            mrProper.execute(pars);
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        finally{
        }
    }
}