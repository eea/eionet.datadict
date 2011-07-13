package eionet.meta.savers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public class MAttrFieldsHandler extends BaseHandler{
    
    public static String POS_PREFIX = "pos_";
    public static String OLDPOS_PREFIX = "oldpos_";

    String mode = null;
    String attr_id = null;
    String attr_name = null;

    public MAttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }

    public MAttrFieldsHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.attr_id = req.getParameter("attr_id");
        this.attr_name = req.getParameter("attr_name");
    }

    public MAttrFieldsHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.savers.BaseHandler#execute_()
     */
    public void execute_() throws Exception {
        if (mode==null ||
           (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("edit") && !mode.equalsIgnoreCase("edit_pos")))
            throw new Exception("MAttrFieldsHandler mode unspecified!");

        if (attr_id == null) throw new Exception("MAttrFieldsHandler attr_id unspecified!");

        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else if (mode.equalsIgnoreCase("edit_pos"))
            processFields();
        else
            delete();
    }

    private void insert() throws Exception {

        String[] newFields = req.getParameterValues("new_field");
        if (newFields!=null){
            for (int i=0; i<newFields.length; i++){
                insertField(newFields[i]);
            }
        }
    }
    
    /**
     * 
     * @param field
     * @throws SQLException
     */
    private void insertField(String field) throws SQLException {
    	
    	String definition = req.getParameter("definition");
    	if (definition == null) definition = "";
    	
    	String position = req.getParameter("position");
    	if (position == null || position.length()==0) position = "0";
    	
    	INParameters inParams = new INParameters();
    	LinkedHashMap map = new LinkedHashMap();

    	map.put("M_COMPLEX_ATTR_ID", inParams.add(attr_id, Types.INTEGER));
    	map.put("NAME", inParams.add(field));
    	map.put("DEFINITION", inParams.add(definition));
    	map.put("POSITION", inParams.add(position, Types.INTEGER));
    	
    	String harvFld = req.getParameter("harv_fld");
    	if (harvFld != null && !harvFld.equals("null"))
    		map.put("HARV_ATTR_FLD_NAME", inParams.add(harvFld));
    	else
    		map.put("HARV_ATTR_FLD_NAME", "NULL");
    	
    	SQL.executeUpdate(SQL.insertStatement("M_COMPLEX_ATTR_FIELD", map), inParams, conn);
    }

    /**
     * 
     * @throws Exception
     */
    private void delete() throws Exception {

        String[] del_Fields = req.getParameterValues("del_field");
        if (del_Fields == null || del_Fields.length == 0) return;

        for (int i=0; i<del_Fields.length; i++){
            deleteField(del_Fields[i]);
        }
    }

    /**
     * 
     * @param id
     * @throws SQLException
     */
    private void deleteField(String id) throws SQLException {
    	
    	INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer("delete from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(inParams.add(id, Types.INTEGER));
        SQL.executeUpdate(buf.toString(), inParams, conn);

        inParams = new INParameters();
        buf = new StringBuffer("delete from COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(inParams.add(id, Types.INTEGER));
        SQL.executeUpdate(buf.toString(), inParams, conn);
    }
    
    /**
     * 
     * @throws SQLException
     */
    private void update() throws SQLException {

        String field_id = req.getParameter("field_id");
        if (field_id == null)
        	return;

        String definition = req.getParameter("definition");
        if (definition == null)
        	definition = "";

        String priority = req.getParameter("priority");
        if (priority == null)
        	priority = "1";

    	INParameters inParams = new INParameters();
    	LinkedHashMap map = new LinkedHashMap();

        map.put("DEFINITION", inParams.add(definition));
        map.put("PRIORITY", inParams.add(priority));
        
		String harvFld = req.getParameter("harv_fld");
		if (harvFld != null && !harvFld.equals("null"))
			map.put("HARV_ATTR_FLD_NAME", inParams.add(harvFld));
		else
			map.put("HARV_ATTR_FLD_NAME", "NULL");

        StringBuffer buf = new StringBuffer(SQL.updateStatement("M_COMPLEX_ATTR_FIELD", map));
        buf.append(" where M_COMPLEX_ATTR_FIELD_ID=").append(inParams.add(field_id, Types.INTEGER));

        SQL.executeUpdate(buf.toString(), inParams, conn);
    }

    /**
     * 
     * @throws Exception
     */
     private void processFields() throws Exception {

        String[] posIds = req.getParameterValues("pos_id");
        String old_pos=null;
        String pos=null;
        String parName=null;
        if (posIds==null || posIds.length==0) return;

        logger.debug(Integer.toString(posIds.length));
        logger.debug(posIds[0]);

        for (int i=0; i<posIds.length; i++){
            old_pos = req.getParameter(OLDPOS_PREFIX + posIds[i]);
            pos = req.getParameter(POS_PREFIX + posIds[i]);
            logger.debug(old_pos + "|" + pos + "|" + posIds[i]);
            if (old_pos.length()==0 || pos.length()==0)
                continue;
            if (!old_pos.equals(pos))
                updateFieldPos(posIds[i], pos);
        }
    }
     
     /**
      * 
      * @param fieldId
      * @param pos
      * @throws Exception
      */
    private void updateFieldPos(String fieldId, String pos) throws Exception {
    	
    	INParameters inParams = new INParameters();
    	LinkedHashMap map = new LinkedHashMap();
        map.put("POSITION", inParams.add(pos, Types.INTEGER));
        StringBuffer buf = new StringBuffer(SQL.updateStatement("M_COMPLEX_ATTR_FIELD", map));
        buf.append(" where M_COMPLEX_ATTR_FIELD_ID=").append(inParams.add(fieldId, Types.INTEGER));
        SQL.executeUpdate(buf.toString(), inParams, conn);
    }
}
