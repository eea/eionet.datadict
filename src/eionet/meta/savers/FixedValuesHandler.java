package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import eionet.meta.*;

import com.tee.util.*;

public class FixedValuesHandler {
	
	private static final String DEFAULT_OWNER_TYPE = "elem";

    private Connection conn = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String ownerID = null;
	private String ownerType = DEFAULT_OWNER_TYPE;
    
    private String lastInsertID = null;
	private boolean versioning = true;    
    private HashSet prhbDatatypes = new HashSet();
    
    private boolean allowed = true;
	private boolean allowanceChecked = false;
    
    public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public FixedValuesHandler(Connection conn, Parameters req, ServletContext ctx){
    	
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        
        mode = req.getParameter("mode");
        ownerID = req.getParameter("delem_id");
		String _ownerType = req.getParameter("parent_type");
		if (!Util.nullString(_ownerType))
			ownerType = _ownerType;
		
        if (ctx!=null){
	        String _versioning = ctx.getInitParameter("versioning");
	        if (_versioning!=null && _versioning.equalsIgnoreCase("false"))
	            setVersioning(false);
    	}
            
        // set the prohibited datatypes of parent element
        prhbDatatypes.add("BOOLEAN");
    }

    public FixedValuesHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void setVersioning(boolean f){
        this.versioning = f;
    }
    
    public String getOwnerID(){
    	return ownerID;
    }
    
	public boolean execute(Parameters pars) throws Exception {
		this.req = pars;
		return execute();
	}
    
    public boolean execute() throws Exception {
    	
    	if (!allowanceChecked)
			 checkAllowance();
    	else if (!allowed)
    		return false; 

        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else
            delete();
        
        return true;
    }

    private void insert() throws Exception {

        String[] newValues = req.getParameterValues("new_value");
        for (int i=0; newValues!=null && i<newValues.length; i++)
            insertValue(newValues[i]);
    }

    private void insertValue(String value) throws Exception {

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("FXV");
        gen.setFieldExpr("OWNER_ID", ownerID);
		gen.setField("OWNER_TYPE", ownerType);
        gen.setField("VALUE", value);

        String isDefault = req.getParameter("is_default");
        if (isDefault!=null && isDefault.equalsIgnoreCase("true"))
            gen.setField("IS_DEFAULT", "Y");
        
		String definition = req.getParameter("definition");
		if (definition!=null) gen.setField("DEFINITION", definition);
		String shortDesc = req.getParameter("short_desc");
		if (shortDesc!=null) gen.setField("SHORT_DESC", shortDesc);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(gen.insertStatement());
        setLastInsertID();
        
        stmt.close();
    }

	private void update() throws SQLException {
		
		String fxvID = req.getParameter("fxv_id");
		if (Util.nullString(fxvID))
			return;

		String isDefault = req.getParameter("is_default");
		String definition = req.getParameter("definition");
		String shortDesc = req.getParameter("short_desc");
		
		SQLGenerator gen = new SQLGenerator();
		
		if (isDefault!=null)
			gen.setField("IS_DEFAULT", isDefault.equals("true") ? "Y" : "N");
		if (definition!=null)
			gen.setField("DEFINITION", definition);
		if (definition!=null)
			gen.setField("SHORT_DESC", shortDesc);

		if (gen.getValues().length()==0) return;
		
		gen.setTable("FXV");
		
		StringBuffer buf = new StringBuffer(gen.updateStatement()).
		append(" where FXV_ID=").append(fxvID);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(buf.toString());
		stmt.close();
	}

    private void delete() throws Exception {

        String[] fxvID = req.getParameterValues("del_id");
        if (fxvID == null || fxvID.length == 0) return;

        for (int i=0; i<fxvID.length; i++){
            deleteValue(fxvID[i]);
        }
    }

    private void deleteValue(String id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from FXV where FXV_ID=").
        append(id);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void checkAllowance() throws Exception {
    	
		allowanceChecked = true;
		
		// check if legal mode
		if (mode==null || (!mode.equalsIgnoreCase("add") &&
						   !mode.equalsIgnoreCase("edit") &&
						   !mode.equalsIgnoreCase("delete")) &&
						   !mode.equalsIgnoreCase("edit_positions"))
			throw new Exception("FixedValuesHandler mode unspecified!");

		// check if owner id specified
		if (ownerID == null && !mode.equals("delete"))
			throw new Exception("FixedValuesHandler delem_id unspecified!");

		// legalize owner type 
		if (ownerType.equals("CH1") || ownerType.equals("CH2"))
			ownerType = "elem";

		if (!ownerType.equals("elem") && !ownerType.equals("attr"))
			throw new Exception("FixedValuesHandler: unknown parent type!");
    	
    	// for owners with type!="elem" fixed values always allowed
		if (!ownerType.equals("elem"))
			return;
		
		// if in versioning mode, we cannot edit fixed-values of
		// non-working copies
		DDSearchEngine searchEngine = new DDSearchEngine(conn);
		boolean wc = true;
		try{
			wc = searchEngine.isWorkingCopy(ownerID, "elm");
		}
		catch (Exception e){}
		            
		if (!wc && versioning){
			throw new Exception(
				"Cannot edit fixed values of a non-working copy!");
		}
		
		// get the element's datatype and check if fxvalues are allowed
		DDSearchEngine eng = new DDSearchEngine(conn);
		DataElement elm = eng.getDataElement(ownerID);
		String dtype = elm==null ? "" :
								   elm.getAttributeValueByShortName("Datatype");
		dtype = dtype==null ? "" : dtype.toUpperCase();
		if (prhbDatatypes.contains(dtype.toUpperCase()))
			allowed = false;
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
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }

    public static void main(String[] args){

        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn = DriverManager.getConnection(
				"jdbc:mysql://195.250.186.16:3306/DataDict", "root", "ABr00t");

            Parameters pars = new Parameters();
            pars.addParameterValue("mode", "edit");
            pars.addParameterValue("fxv_id", "2324");
            pars.addParameterValue("delem_id", "9923");
            pars.addParameterValue("parent_type", "elem");
			//pars.addParameterValue("new_value", "kola1");
			pars.addParameterValue("definition", "plaaplaatt");
            pars.addParameterValue("short_desc", "plaaplaarrrr");
            
            FixedValuesHandler handler = new FixedValuesHandler(conn, pars, null);
            handler.execute();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}
