package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.File;

import eionet.meta.schema.*;
import eionet.meta.DataElement;
import eionet.meta.Namespace;
import eionet.meta.DDSearchEngine;

import com.tee.util.*;

public class SubElemsHandler {
    
    private Connection conn = null;
    //private HttpServletRequest req = null;
    private Parameters req = null;
    private ServletContext ctx = null;
    
    private String mode = null;
    private String parent_id = null;
    private String contentID = null;
    private String parent_name = null;
    private String parent_ns = null;
    private String parent_type = null;
    private String minOcc = null;
    private String maxOcc = null;
    
    private DDSearchEngine searchEngine = null;
    
    private String lastInsertChildID = null;
    private String lastInsertChildType = null;
    
    public SubElemsHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public SubElemsHandler(Connection conn, Parameters req, ServletContext ctx){
        
        this.conn = conn;
        this.req  = req;
        this.ctx  = ctx;
        this.mode = req.getParameter("mode");
        this.parent_id = req.getParameter("parent_id");
        this.contentID = req.getParameter("content_id");
        this.parent_name = req.getParameter("parent_name");
        this.parent_ns = req.getParameter("parent_ns");
        this.parent_type = req.getParameter("parent_type");
        this.minOcc = req.getParameter("min_occ");
        this.maxOcc = req.getParameter("max_occ");
        
        this.searchEngine = new DDSearchEngine(conn, req.getID(), ctx);
    }
    
    public SubElemsHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }
    
    public void execute() throws Exception {
        if (mode==null || (!mode.equalsIgnoreCase("add") && !mode.equalsIgnoreCase("delete")))
            throw new Exception("SubElemsHandler mode unspecified!");
        
        if (parent_id == null) throw new Exception("SubElemsHandler parent_id unspecified!");
        if (parent_type == null) throw new Exception("SubElemsHandler parent_type unspecified!");
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else
            delete();
    }
    
    private void insert() throws Exception {
        
        String contentType = req.getParameter("content_type");
        if (contentType == null || (!contentType.equals("seq") && !contentType.equals("chc")))
            throw new Exception("Content type is not specified!");
        
        String childType = req.getParameter("childType");
        if (childType == null || (!contentType.equals("seq") && !contentType.equals("chc") && !contentType.equals("elm")))
            throw new Exception("Child type is not specified or is unknown!");
            
        if (contentType.equals("chc")){
            String[] children = req.getParameterValues("child");
            if (childType.equals("elm") && children!=null){
                for (int i=0; i<children.length; i++){                    
                    insertChoice(children[i], childType);
                }
            }
            else{
                insertChoice(null, childType);
            }
        }
        else{
            
            String position = req.getParameter("position");
            if (position == null || position.length()==0) position = "0";            
            String[] min_occs = req.getParameterValues("min_occ");
            String[] max_occs = req.getParameterValues("max_occ");
            String[] children = req.getParameterValues("child");
            
            if (childType.equals("elm") && children!=null){
                for (int i=0; i<children.length; i++){
                    insertSequence(children[i], childType, position, min_occs[i], max_occs[i]);
                }
            }
            else{
                insertSequence(null, childType, position, min_occs[0], max_occs[0]);
            }
        }
    }
    
    private void insertChoice(String child_id, String child_type) throws Exception {
        
        String choiceID = contentID;
        StringBuffer buf = new StringBuffer();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;

        // check if a choice or sequence already exists for this data element
        // if there's sequence, exit
        
        if (choiceID == null){
            buf.append("select CHILD_ID,CHILD_TYPE from CONTENT where ");
            buf.append("PARENT_TYPE='elm' and CHILD_TYPE='chc' and PARENT_ID=");
            buf.append(parent_id);
            
            ctx.log(buf.toString());
            
            rs = stmt.executeQuery(buf.toString());
            
            if (rs.next()){
                if (rs.getString(2).equals("chc"))
                    choiceID = rs.getString(1);
                else
                    throw new Exception("Cannot create a choice, a sequence is already present!");
                    
                if (rs.next())
                    throw new Exception("A data element cannot have more than one content!");
            }
        }
        
        // if there is no choice or sequence for this data element, create a choice
        // and relate it with this data element
        
        SQLGenerator gen = new SQLGenerator();
        
        if (choiceID == null){
            
            gen.setTable("AUTO_ID");
            gen.setField("TYPE", "chc");
            
            stmt.executeUpdate(gen.insertStatement());
            
            rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if (rs.next()) choiceID = rs.getString(1);
            if (choiceID != null){
                
                gen = new SQLGenerator();
                gen.setTable("CONTENT");
                gen.setFieldExpr("PARENT_ID", parent_id);
                gen.setField("PARENT_TYPE", "elm");
                gen.setFieldExpr("CHILD_ID", choiceID);
                gen.setField("CHILD_TYPE", "chc");
                
                stmt.executeUpdate(gen.insertStatement());
            }
        }
        
        if (choiceID == null)
            throw new Exception("Failed to create or find a choice for this data element!");

        // if the child is to be a new sequence, create it
        
        if (child_id == null){
            
            if (!child_type.equals("seq"))
                throw new Exception("Was expecting a new sequence to be added!");
            
            gen.clear();
            gen.setTable("AUTO_ID");
            gen.setField("TYPE", child_type);
            
            stmt.executeUpdate(gen.insertStatement());
            
            rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if (rs.next()) child_id = rs.getString(1);
            
            lastInsertChildID = child_id;
            lastInsertChildType = "seq";
            
            /*gen.clear();
            gen.setTable("CONTENT");
            gen.setFieldExpr("PARENT_ID", parent_id);
            gen.setField("PARENT_TYPE", parent_type);
            gen.setFieldExpr("CHILD_ID", child_id);
            gen.setField("CHILD_TYPE", child_type);
            
            stmt.executeUpdate(gen.insertStatement());*/
        }
        
        // take the already existed or newly created choice and insert the child
        
        gen = new SQLGenerator();
        gen.setTable("CHOICE");
        gen.setFieldExpr("CHOICE_ID", choiceID);
        gen.setFieldExpr("CHILD_ID", child_id);
        gen.setField("CHILD_TYPE", child_type);
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        contentID = choiceID;
    }
    
    private void insertSequence(String child_id,
                                String child_type,
                                String position,
                                String minOccurs,
                                String maxOccurs) throws Exception {
                                    
        String sequenceID = contentID;
        StringBuffer buf = new StringBuffer();
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        
        // check if a choice or sequence already exists for this data element
        // if there's choice, exit
        
        if (sequenceID == null){
            
            buf = new StringBuffer();
            buf.append("select CHILD_ID,CHILD_TYPE from CONTENT where ");
            buf.append("PARENT_TYPE='elm' and CHILD_TYPE='seq' and PARENT_ID=");
            buf.append(parent_id);
            
            ctx.log(buf.toString());
            
            rs = stmt.executeQuery(buf.toString());
            
            if (rs.next()){
                if (rs.getString(2).equals("seq"))
                    sequenceID = rs.getString(1);
                else
                    throw new Exception("Cannot create a sequence, a choice is already present!");
                    
                if (rs.next())
                    throw new Exception("A data element cannot have more than one content!");
            }
        }
        
        // if there is no choice or sequence for this data element, create a sequence
        // and relate it with this data element
        
        SQLGenerator gen = new SQLGenerator();
        
        if (sequenceID == null){
            
            gen.setTable("AUTO_ID");
            gen.setField("TYPE", "seq");
            
            stmt.executeUpdate(gen.insertStatement());
            
            rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if (rs.next()) sequenceID = rs.getString(1);
            if (sequenceID != null){
                
                gen = new SQLGenerator();
                gen.setTable("CONTENT");
                gen.setFieldExpr("PARENT_ID", parent_id);
                gen.setField("PARENT_TYPE", "elm");
                gen.setFieldExpr("CHILD_ID", sequenceID);
                gen.setField("CHILD_TYPE", "seq");
                
                String sql = gen.insertStatement();
                ctx.log(sql);
                stmt.executeUpdate(sql);
            }
        }
        
        if (sequenceID == null)
            throw new Exception("Failed to create or find a sequence for this data element!");

        // if the child is to be a new choice, create it
        
        if (child_id == null){
            
            if (!child_type.equals("chc"))
                throw new Exception("Was expecting a new choice to be added!");
            
            gen.clear();
            gen.setTable("AUTO_ID");
            gen.setField("TYPE", child_type);
            
            stmt.executeUpdate(gen.insertStatement());
            
            rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if (rs.next()) child_id = rs.getString(1);
            
            lastInsertChildID = child_id;
            lastInsertChildType = "chc";
            
            /*gen.clear();
            gen.setTable("CONTENT");
            gen.setFieldExpr("PARENT_ID", parent_id);
            gen.setField("PARENT_TYPE", parent_type);
            gen.setFieldExpr("CHILD_ID", child_id);
            gen.setField("CHILD_TYPE", child_type);
            
            stmt.executeUpdate(gen.insertStatement());*/
        }
        
        // take the already existed or newly created sequence and insert the child
        
        gen = new SQLGenerator();
        gen.setTable("SEQUENCE");
        
        gen.setFieldExpr("SEQUENCE_ID", sequenceID);
        gen.setFieldExpr("CHILD_ID", child_id);
        gen.setField("CHILD_TYPE", child_type);
        gen.setFieldExpr("POSITION", position);
        gen.setField("MIN_OCCURS", minOccurs);
        gen.setField("MAX_OCCURS", maxOccurs);
        
        String sql = gen.insertStatement();
        ctx.log(sql);
        
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        
        contentID = sequenceID;
    }
    
    private void delete() throws Exception {
        
        if (contentID == null) throw new Exception("ID of sequence or choice is not given!");
        String contentType = req.getParameter("content_type");
        if (contentType == null || (!contentType.equals("seq") && !contentType.equals("chc")))
            throw new Exception("Relation type is not specified!");
        
        String[] del_IDs = req.getParameterValues("del_id");
        if (del_IDs == null || del_IDs.length == 0) return;
        
        for (int i=0; i<del_IDs.length; i++){
            
            String childType = req.getParameter("del_type_" + del_IDs[i]);
            if (childType == null || childType.length()==0)
                throw new Exception("Type of the child to be deleted is not specified!");
            
            if (contentType.equals("seq"))
                deleteFromSequence(del_IDs[i], childType);
            else
                deleteFromChoice(del_IDs[i], childType);
            
            if (!childType.equals("elm")){
                removeChildren(del_IDs[i], childType);
            }
        }

        if (del_IDs != null && del_IDs.length==1 && contentID.equals(del_IDs[0]))
            removeEmptySub(req.getParameter("del_type_" + del_IDs[0]));
        else
            removeEmptySub(contentType);
    }
    
    private void deleteFromSequence(String child_id, String child_type) throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from SEQUENCE where SEQUENCE_ID=");
        buf.append(contentID);
        buf.append(" and CHILD_ID=");
        buf.append(child_id);
        buf.append(" and CHILD_TYPE='");
        buf.append(child_type);
        buf.append("'");
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());                
    }
    
    private void deleteFromChoice(String child_id, String child_type) throws SQLException {
        
        StringBuffer buf = new StringBuffer("delete from CHOICE where CHOICE_ID=");
        buf.append(contentID);
        buf.append(" and CHILD_ID=");
        buf.append(child_id);
        buf.append(" and CHILD_TYPE='");
        buf.append(child_type);
        buf.append("'");
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void removeChildren(String id, String type) throws SQLException{
        
        String tableName = type.equals("seq") ? "SEQUENCE" : "CHOICE";
        
        StringBuffer buf = new StringBuffer("select * from ");
        buf.append(tableName);
        buf.append(" where ");
        buf.append(tableName);
        buf.append("_ID=");
        buf.append(id);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        Vector children = new Vector();
        while (rs.next()){
            String childType = rs.getString("CHILD_TYPE");
            if (childType.equals("elm"))
                continue;
            Hashtable hash = new Hashtable();
            hash.put("id", rs.getString("CHILD_ID"));
            hash.put("type", childType);
            children.add(hash);
        }
        
        for (int i=0; i<children.size(); i++){
            Hashtable hash = (Hashtable)children.get(i);
            removeChildren((String)hash.get("id"), (String)hash.get("type"));
        }
        
        buf = new StringBuffer("delete from ");
        buf.append(tableName);
        buf.append(" where ");
        buf.append(tableName);
        buf.append("_ID=");
        buf.append(id);
        
        stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void removeEmptySub(String contentType) throws SQLException {
        
        if (!parent_type.equals("elm"))
            return;
        
        String tableName = contentType.equals("seq") ? "SEQUENCE" : "CHOICE";
        String qry = "select count(*) from " + tableName + " where " + tableName + "_ID=" + contentID;
        
        log(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        
        int count=-1;
        if (rs.next()) count = rs.getInt(1);
        if (count > 0) return;
        
        qry = "delete from AUTO_ID where ID=" + contentID + " and TYPE='" + contentType + "'";
        
        log(qry);
        
        stmt.executeUpdate(qry);
        qry =
        "delete from CONTENT where PARENT_ID=" + parent_id +
        " and PARENT_TYPE='" + parent_type + "' and CHILD_ID=" + contentID +
        " and CHILD_TYPE='" + contentType + "'";
        
        log(qry);
        
        stmt.executeUpdate(qry);
        
        contentID = null;
    }
    
    public String getContentID(){
        return contentID;
    }
    
    public String getLastInsertChildID(){
        return lastInsertChildID;
    }
    
    public String getLastInsertChildType(){
        return lastInsertChildType;
    }
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
}
