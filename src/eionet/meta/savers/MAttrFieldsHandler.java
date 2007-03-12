package eionet.meta.savers;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.tee.util.*;

import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;

public class MAttrFieldsHandler {
    
    public static String POS_PREFIX = "pos_";
    public static String OLDPOS_PREFIX = "oldpos_";

    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    String mode = null;
    String attr_id = null;
    String attr_name = null;
    private static LogServiceIF logger = new Log4jLoggerImpl();

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

    public void execute() throws Exception {
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
    
    private void insertField(String field) throws SQLException {
        
        String definition = req.getParameter("definition");
        if (definition == null) definition = "";

        String position = req.getParameter("position");
        if (position == null || position.length()==0) position = "0";
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("M_COMPLEX_ATTR_FIELD");
        
        gen.setField("M_COMPLEX_ATTR_ID", attr_id);
        gen.setField("NAME", field);
        gen.setField("DEFINITION", definition);
        gen.setField("POSITION", position);
        
		String harvFld = req.getParameter("harv_fld");
		if (harvFld != null && !harvFld.equals("null"))
			gen.setField("HARV_ATTR_FLD_NAME", harvFld);
		else
			gen.setFieldExpr("HARV_ATTR_FLD_NAME", "NULL");
        
        String sql = gen.insertStatement();

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private void delete() throws Exception {

        String[] del_Fields = req.getParameterValues("del_field");
        if (del_Fields == null || del_Fields.length == 0) return;

        for (int i=0; i<del_Fields.length; i++){
            deleteField(del_Fields[i]);
        }
    }

    private void deleteField(String id) throws SQLException {
        StringBuffer buf = new StringBuffer("delete from M_COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(id);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());

        buf = new StringBuffer("delete from COMPLEX_ATTR_FIELD ");
        buf.append("where M_COMPLEX_ATTR_FIELD_ID=");
        buf.append(id);

        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    private void update() throws SQLException {

        String field_id = req.getParameter("field_id");
        if (field_id == null) return;

        String definition = req.getParameter("definition");
        if (definition == null) definition = "";

        String priority = req.getParameter("priority");
        if (priority == null) priority = "1";

        SQLGenerator gen = new SQLGenerator();
        gen.setTable("M_COMPLEX_ATTR_FIELD");

        gen.setField("DEFINITION", definition);
        gen.setField("PRIORITY", priority);
        
		String harvFld = req.getParameter("harv_fld");
		if (harvFld != null && !harvFld.equals("null"))
			gen.setField("HARV_ATTR_FLD_NAME", harvFld);
		else
			gen.setFieldExpr("HARV_ATTR_FLD_NAME", "NULL");

        StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
        sqlBuf.append(" where M_COMPLEX_ATTR_FIELD_ID=");
        sqlBuf.append(field_id);

        logger.debug(sqlBuf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sqlBuf.toString());
        stmt.close();
    }

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
    private void updateFieldPos(String fieldId, String pos) throws Exception {
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("M_COMPLEX_ATTR_FIELD");

        gen.setField("POSITION", pos);

        StringBuffer sqlBuf = new StringBuffer(gen.updateStatement());
        sqlBuf.append(" where M_COMPLEX_ATTR_FIELD_ID=");
        sqlBuf.append(fieldId);

        logger.debug(sqlBuf.toString());

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sqlBuf.toString());
        stmt.close();
    }
}
