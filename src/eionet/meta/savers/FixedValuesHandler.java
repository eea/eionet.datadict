package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import java.io.File;

import eionet.meta.DataElement;
import eionet.meta.DElemAttribute;
import eionet.meta.schema.SchemaExp;

import com.tee.util.*;

public class FixedValuesHandler {

    public static String ATTR_PREFIX = "attr_";
    
    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    String mode = null;
    String delem_id = null;
    String delem_name = null;
    String ns = null;
    String fxv_id = null;
    private String[] fxv_ids = null;
    private String lastInsertID = null;    
    private String parentType = "elem";

    public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public FixedValuesHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.delem_id = req.getParameter("delem_id");
        this.delem_name = req.getParameter("delem_name");
        this.ns = req.getParameter("ns");
        this.fxv_id = req.getParameter("fxv_id");
        this.fxv_ids = req.getParameterValues("fxv_id");
        String parentType = req.getParameter("parent_type");
        if (!Util.nullString(parentType))
            this.parentType = parentType;
    }

    public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("edit") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("FixedValuesHandler mode unspecified!");
        
        if (delem_id == null && !mode.equals("delete"))
            throw new Exception("FixedValuesHandler delem_id unspecified!");
        
        if (!parentType.equals("elem") && !parentType.equals("attr"))
            throw new Exception("FixedValuesHandler: unknown parent type!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
    }

    private void insert() throws Exception {

        String[] newValues = req.getParameterValues("new_value");
        if (newValues!=null){

            for (int i=0; i<newValues.length; i++){
                if (exists(newValues[i]))
                    throw new SQLException("This allowable value already exists!");

                insertValue(newValues[i]);
            }
        }
        
        //There is only one fixed value in the Parameters, then it's possible to insert attributes
        if (newValues!=null && newValues.length==1){
            // JH140303 - both done in insertValue() now
            // setLastInsertID();
            // fxv_id = getLastInsertID();
            processAttributes();
        }
    }

    private void insertValue(String value) throws Exception {

        //String csID = req.getParameter("cs_id");
        //if (Util.nullString(csID)){
            
        /*String csID = "";
        if (!componentHasValues()){
            
            Parameters pars = new Parameters();
            pars.addParameterValue("mode", "add");
            StringBuffer buf = new StringBuffer("Fixed values for ");
            buf.append(delem_id + " ");
            buf.append(parentType.equals("elem") ? "element" : "attribute");
            pars.addParameterValue("name", buf.toString());
            pars.addParameterValue("type", "Fixed values");
            pars.addParameterValue("version", "0.1");
            pars.addParameterValue("version", buf.toString());
            
            ClsfSchemeHandler clsfSchemeHandler = new ClsfSchemeHandler(conn, pars, ctx);
            clsfSchemeHandler.execute();
            csID = clsfSchemeHandler.getLastInsertID();
        }*/

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("CS_ITEM");

        //gen.setField("CSI_ID", fxv_id);
        //gen.setField("CS_ID", csID);
        gen.setFieldExpr("COMPONENT_ID", delem_id);
        gen.setField("CSI_VALUE", value);
        gen.setField("COMPONENT_TYPE", parentType);

        String isDefault = req.getParameter("is_default");
        if (!Util.nullString(isDefault) && isDefault.equalsIgnoreCase("true"))
            gen.setField("IS_DEFAULT", "Y");

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        setLastInsertID();
        fxv_id = getLastInsertID();
        
        // if parent  has been specified, insert CSI_RELATION (levelled lists)
        String parentCSI = req.getParameter("parent_csi");
        if (!Util.nullString(parentCSI)){
            gen.clear();
            gen.setTable("CSI_RELATION");
            gen.setFieldExpr("PARENT_CSI", parentCSI);
            gen.setFieldExpr("CHILD_CSI", fxv_id);
            stmt.executeUpdate(gen.insertStatement());
        }
        
        stmt.close();
    }

    private void delete() throws Exception {

        this.fxv_ids = req.getParameterValues("del_id");
        if (fxv_ids == null || fxv_ids.length == 0) return;

        for (int i=0; i<fxv_ids.length; i++){
            deleteValue(fxv_ids[i]);
        }
        
        deleteAttributes();
        deleteRelations();
    }

    private void deleteValue(String id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from CS_ITEM where CSI_ID=");
        buf.append(id);

        log(buf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void update() throws SQLException {

        String isDefault = req.getParameter("is_default");
        if (!Util.nullString(isDefault)){
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("CS_ITEM");
            gen.setField("IS_DEFAULT", isDefault.equals("true") ? "Y" : "N");
            
            StringBuffer buf = new StringBuffer(gen.updateStatement());
            buf.append(" where CSI_ID=");
            buf.append(fxv_id);
            
            log(buf.toString());
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            stmt.close();
        }
        
        deleteAttributes();
        processAttributes();
    }
    
    private void deleteAttributes() throws SQLException {

        //StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where PARENT_TYPE='FV'");
        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where PARENT_TYPE='CSI'");
        for (int i=0; i<fxv_ids.length; i++){
            if (i==0)
                buf.append(" AND (");
            if (i>0)
                buf.append(" or ");
            buf.append("DATAELEM_ID=");
            buf.append(fxv_ids[i]);
            if (i==fxv_ids.length -1)
                buf.append(")");
        }

        log(buf.toString());

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
        gen.setFieldExpr("DATAELEM_ID", fxv_id);
        //gen.setField("PARENT_TYPE", "FV");
        gen.setField("PARENT_TYPE", "CSI");
        gen.setField("VALUE", value);

        String sql = gen.insertStatement();
        log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private void updateAttribute(String attrId, String value) throws SQLException {

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        gen.setFieldExpr("M_ATTRIBUTE_ID", attrId);
        gen.setFieldExpr("DATAELEM_ID", fxv_id);
        //gen.setField("PARENT_TYPE", "FV");
        gen.setField("PARENT_TYPE", "CSI");
        gen.setField("VALUE", value);
        
        String sql = gen.updateStatement();
        log(sql);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    
    private void deleteRelations() throws SQLException {
        StringBuffer buf = new StringBuffer("delete from CSI_RELATION where ");
        for (int i=0; i<fxv_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("PARENT_CSI=");
            buf.append(fxv_ids[i]);
            buf.append(" or CHILD_CSI=");
            buf.append(fxv_ids[i]);
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

    public boolean exists(String value) throws SQLException {

        String qry =
        "select count(*) as COUNT from CS_ITEM where " +
        "COMPONENT_ID=" + delem_id +
        " AND CSI_VALUE=" + com.tee.util.Util.strLiteral(value) +
        " AND COMPONENT_TYPE=" + com.tee.util.Util.strLiteral(parentType);

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
    * Function for checking if this component already has values
    */
    public boolean componentHasValues() throws SQLException {

        String qry =
        "select count(*) as COUNT from CS_ITEM where " +
        "COMPONENT_ID=" + delem_id +
        " AND COMPONENT_TYPE=" + com.tee.util.Util.strLiteral(parentType);

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
    
    public static void main(String[] args){
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn =
                DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            
            String qry = "select distinct * from FIXED_VALUE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(qry);
            
            while (rs.next()){
                Parameters pars = new Parameters();
                pars.addParameterValue("mode", "add");
                pars.addParameterValue("fxv_id", rs.getString("FIXED_VALUE_ID"));
                pars.addParameterValue("delem_id", rs.getString("DATAELEM_ID"));
                pars.addParameterValue("new_value", rs.getString("VALUE"));
                pars.addParameterValue("parent_type", rs.getString("PARENT_TYPE"));
                pars.addParameterValue("is_default", rs.getString("IS_DEFAULT"));
                
                FixedValuesHandler handler = new FixedValuesHandler(conn, pars, null);
                handler.execute();
            }
            
            stmt.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
