package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import eionet.meta.*;

import com.tee.util.*;

public class DsTableHandler {
    
    public static String ATTR_PREFIX = "attr_";

    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String lastInsertID = null;

    private DDSearchEngine searchEngine = null;
    
    private String nsID = null;
    
    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public DsTableHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
    }
    
    public DsTableHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                           !mode.equalsIgnoreCase("edit") &&
                           !mode.equalsIgnoreCase("delete")))
            throw new Exception("DsTableHandler mode unspecified!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        String dsID = req.getParameter("ds_id");
        if (dsID == null)
            throw new Exception("DsTableHandler: ds_id not specified!");
        
        String shortName = req.getParameter("short_name");
        if (Util.nullString(shortName))
            throw new Exception("DsTableHandler: table short name not specified!");
            
        if (exists(dsID, shortName))
            throw new Exception("DsTableHandler: a table with this short name in this dataset already exists!");

        //These are attributes
      //  String fullName = req.getParameter("full_name");
      //  String definition = req.getParameter("definition");
        String type = req.getParameter("type");

        // create a new context corresponding namespace to this newly created table
        
        String dsName = req.getParameter("ds_name");
        if (dsName == null){
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            Dataset ds = searchEngine.getDataset(dsID);
            if (ds != null) dsName = ds.getShortName();
        }
        
        if (dsName == null) dsName = dsID;
        
        Parameters pars = new Parameters();
        pars.addParameterValue("mode", "add");
        pars.addParameterValue("ns_id", dsName + "_" + shortName);
        pars.addParameterValue("fullName", shortName + " table in " + dsName + " dataset");
        pars.addParameterValue("description", shortName + " table in " + dsName + " dataset");
        
        NamespaceHandler nsHandler = new NamespaceHandler(conn, pars, ctx);
        nsHandler.execute();
        
        nsID = nsHandler.getLastInsertID();
        
        // insert the data element
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("SHORT_NAME", shortName);
        gen.setField("DATASET_ID", dsID);
     /*   if (!Util.nullString(fullName))
            gen.setField("NAME", fullName);
        if (!Util.nullString(definition))
            gen.setField("DEFINITION", definition);
    */
        if (!Util.nullString(type))
            gen.setField("TYPE", type);
        if (!Util.nullString(nsID))
            gen.setField("NAMESPACE_ID", nsID);
        
        String sql = gen.insertStatement();
        ctx.log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
        
        setLastInsertID();
        processAttributes();
    }
    
    private void update() throws Exception {
        
        String tableID = req.getParameter("table_id");
        if (tableID == null)
            throw new Exception("DsTableHandler: table_id not specified!");
        
        lastInsertID = tableID;
        
      //  String fullName = req.getParameter("full_name");
      //  String definition = req.getParameter("definition");
        String type = req.getParameter("type");

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        /*if (!Util.nullString(fullName))
            gen.setField("NAME", fullName);
        if (!Util.nullString(definition))
            gen.setField("DEFINITION", definition);*/
        if (!Util.nullString(type))
            gen.setField("TYPE", type);

        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append("where TABLE_ID=");
        buf.append(tableID);
        
        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();

        String[] delIDs = {tableID};
        deleteAttributes(delIDs);
        processAttributes();
    }

    private void delete() throws Exception {

        String[] del_IDs = req.getParameterValues("del_id");
        if (del_IDs==null || del_IDs.length==0) return;

        deleteAttributes(del_IDs);

        // get the elements

        Vector elems = new Vector();
        StringBuffer buf = new StringBuffer("select distinct DATAELEM_ID from TBL2ELEM where ");
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
        // get the namespaces

        Vector nss = new Vector();
        buf = new StringBuffer("select distinct NAMESPACE_ID from DS_TABLE where ");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("TABLE_ID=");
            buf.append(del_IDs[i]);
        }

        stmt = conn.createStatement();
        rs = stmt.executeQuery(buf.toString());
        while (rs.next()){
            nss.add(rs.getString("NAMESPACE_ID"));
        }

        // delete the tables

        buf = new StringBuffer("delete from DS_TABLE where ");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("TABLE_ID=");
            buf.append(del_IDs[i]);
        }

        stmt.executeUpdate(buf.toString());

        // delete the table-elem relations

        buf = new StringBuffer("delete from TBL2ELEM where ");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("TABLE_ID=");
            buf.append(del_IDs[i]);
        }

        stmt.executeUpdate(buf.toString());

        // delete the namespaces

        if (nss.size() > 0){
            buf = new StringBuffer("delete from NAMESPACE where ");
            for (int i=0; i<nss.size(); i++){
                if (i>0) buf.append(" or ");
                buf.append("NAMESPACE_ID='");
                buf.append((String)nss.get(i));
                buf.append("'");
            }
            stmt.executeUpdate(buf.toString());
        }

        stmt.close();

        // delete the elements

        if (elems.size() == 0) return;

        Parameters params = new Parameters();
        params.addParameterValue("mode", "delete");
        for (int i=0; i<elems.size(); i++){
            params.addParameterValue("delem_id", (String)elems.get(i));
        }

        DataElementHandler delemHandler = new DataElementHandler(conn, params, ctx);
        try{ delemHandler.execute(); } catch (Exception e){
            throw new SQLException(e.toString());
        }
    }

    private void deleteDsTable(String table_id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from DS_TABLE where TABLE_ID=");
        buf.append(table_id);

        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }

    private void deleteAttributes(String[] del_IDs) throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where (");
        for (int i=0; i<del_IDs.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(del_IDs[i]);
        }

        buf.append(") and PARENT_TYPE='T'");

        ctx.log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    private void processAttributes() throws SQLException {
        Enumeration parNames = req.getParameterNames();
        while (parNames.hasMoreElements()){
            String parName = (String)parNames.nextElement();
            if (!parName.startsWith(ATTR_PREFIX))
                continue;
            String attrValue = req.getParameter(parName);
            if (attrValue.length()==0)
                continue;
            String attrID = parName.substring(ATTR_PREFIX.length());
            insertAttribute(attrID, attrValue);
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
        ctx.log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    private void setLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";

        ctx.log(qry);
        
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
    
    public boolean exists(String dsID, String shortName) throws SQLException {
        
        String qry =
        "select count(*) as COUNT from DS_TABLE " +
        "where SHORT_NAME=" + com.tee.util.Util.strLiteral(shortName) +
        " and DATASET_ID=" + dsID;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        if (rs.next()){
            if (rs.getInt("COUNT")>0){
                return true;
            }
        }
        
        return false;
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}