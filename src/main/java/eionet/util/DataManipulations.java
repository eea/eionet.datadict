/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Data Dictionary.
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2006 European Environment Agency. All
 * Rights Reserved.
 * 
 * Contributor(s): 
 */
/*
 * Created on 30.10.2006
 */
package eionet.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import eionet.util.sql.SQLGenerator;

import eionet.meta.savers.CopyHandler;
import eionet.meta.savers.DataElementHandler;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;

/**
 * 
 * @author jaanus
 */
public class DataManipulations{
    
    /** */
    protected static final int IDX_TBL = 0;
    protected static final int IDX_POS = 1;
    protected static final String DELETE = "delete";
    protected static final String PRINT = "print";
    
    public static final String PARAM_ACTION = "action";
    public static final String ACTION_CLEANUP = "CLEANUP";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_CLEANUP_CREATE = "CLEANUP_CREATE";
    public static final String ACTION_BOOLEAN_VALUES = "BOOLEAN_VALUES";
    
    /** */
    protected Connection conn = null;
    protected PrintWriter outputWriter = null;
    
    /**
     * 
     * @param ctx
     * @param request
     * @param response
     * @throws IOException 
     */
    public DataManipulations(Connection conn, PrintWriter outputWriter) throws IOException{
        this.conn = conn;
        this.outputWriter = outputWriter;
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testWrite() throws Exception{
        
        for (int i=0; i<10; i++){
            outputWrite(i + " ...");
            //Thread.sleep(3000);
        }
    }

    /**
     * @throws SQLException 
     */
    public void createBooleanFixedValues() throws Exception{
        
        Statement stmt = null;
        ResultSet rs = null;
        try{
            outputWriteln("");
            outputWriteln("Creating fixed values for boolean data elements that have no fixed values yet...");
            
            // get distinct boolean data elements with no fixed values
            HashSet hashSet = new HashSet();
            StringBuffer buf = new StringBuffer();
            buf.
            append("select distinct ATTRIBUTE.DATAELEM_ID from ATTRIBUTE ").
            append("left outer join FXV on (ATTRIBUTE.DATAELEM_ID=FXV.OWNER_ID and FXV.OWNER_TYPE='elem') ").
            append("where ATTRIBUTE.PARENT_TYPE='E' and M_ATTRIBUTE_ID=25 and ATTRIBUTE.VALUE='boolean' and ").
            append("FXV.FXV_ID is null");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                hashSet.add(rs.getString(1));
            }
            rs.close();
            
            outputWriteln(hashSet.size() + " such boolean data elements found...");
            
            // auto-create fixed values for the above found elements
            int count = 0;
            for (Iterator i=hashSet.iterator(); !hashSet.isEmpty() && i.hasNext(); count++){
                DataElementHandler.autoCreateBooleanFixedValues(stmt, (String)i.next()); 
            }
            outputWriteln("Created fixed values for " + count + " boolean data elements");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @param tblID
     * @return
     * @throws SQLException 
     */
    protected String copyTbl(String tblID) throws SQLException{
        
        // copy row in DS_TABLE table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("TABLE_ID", "");
        CopyHandler copyHandler = new CopyHandler(conn, null, null);
        String newID = copyHandler.copy(gen, "TABLE_ID=" + tblID, false);
        
        if (newID!=null){
            // copy simple attributes
            gen.clear();
            gen.setTable("ATTRIBUTE");
            gen.setField("DATAELEM_ID", newID);
            copyHandler.copy(gen, "DATAELEM_ID=" + tblID + " and PARENT_TYPE='T'");
            
            // copy complex attributes
            copyHandler.copyComplexAttrs(newID, tblID, "T");

            // copy TBL2ELEM rows
            gen.clear();
            gen.setTable("TBL2ELEM");
            gen.setField("TABLE_ID", newID);
            copyHandler.copy(gen, "TABLE_ID=" + tblID);

            return newID;
        }
        
        return null;
    }
    
    /**
     * 
     * @param elmID
     * @return
     * @throws SQLException
     */
    protected String copyElm(String elmID) throws SQLException{
        
        // copy row in DATAELEM table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("DATAELEM_ID", "");
        CopyHandler copyHandler = new CopyHandler(conn, null, null);
        String newID = copyHandler.copy(gen, "DATAELEM_ID=" + elmID, false);
        
        if (newID!=null){
            
            // copy simple attributes
            gen.clear();
            gen.setTable("ATTRIBUTE");
            gen.setField("DATAELEM_ID", newID);
            copyHandler.copy(gen, "DATAELEM_ID=" + elmID + " and PARENT_TYPE='E'");
            
            // copy complex attributes
            copyHandler.copyComplexAttrs(newID, elmID, "E");
            
            // copy fixed values
            copyHandler.copyFxv(newID, elmID, "elem");
            
            // copy fk relations
            gen.clear();
            gen.setTable("FK_RELATION");
            gen.setField("REL_ID", "");
            gen.setField("A_ID", newID);
            copyHandler.copy(gen, "A_ID=" + elmID, false);
            gen.clear();
            gen.setTable("FK_RELATION");
            gen.setField("REL_ID", "");
            gen.setField("B_ID", newID);
            copyHandler.copy(gen, "B_ID=" + elmID);
            
            return newID;
        }
        
        return null;
    }

    /**
     * Deletes tables with no parent dataset.
     * @throws Exception
     */
    public void deleteOrphanTables() throws Exception{
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            outputWriteln("");
            
            // delete tables with no parent dataset         
            outputWriteln("searching tables with no parent dataset...");
            StringBuffer buf = new StringBuffer();
            buf.append("select DS_TABLE.TABLE_ID from DS_TABLE left outer join DST2TBL on DS_TABLE.TABLE_ID=DST2TBL.TABLE_ID ").
            append("where DST2TBL.TABLE_ID is null");
            int count = 0;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                String tblID = rs.getString("TABLE_ID");
                if (!hashSet.contains(tblID)){
                    hashSet.add(tblID);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such tables found, now deleting them...");
            }
            else
                outputWriteln(count + " such tables found");
            
            count = 0;
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                deleteTbl((String)i.next());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * Deletes DST2TBL relations where the dataset or the table does not actually exist.
     * @throws Exception
     */
    public void deleteBrokenDstToTblRelations() throws Exception{
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            outputWriteln("");
                    
            // delete DST2TBL relations where the dataset or the table does not actually exist
            outputWriteln("searching DST2TBL relations where the dataset or the table does not actually exist...");
            StringBuffer buf = new StringBuffer();
            buf.
            append("select DST2TBL.* from DST2TBL ").
            append("left outer join DATASET on DST2TBL.DATASET_ID=DATASET.DATASET_ID "). 
            append("left outer join DS_TABLE on DST2TBL.TABLE_ID=DS_TABLE.TABLE_ID ").
            append("where DATASET.DATASET_ID is null or DS_TABLE.TABLE_ID is null");
            int count = 0;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                Hashtable hash = new Hashtable();
                hash.put("DATASET_ID", rs.getString("DATASET_ID"));
                hash.put("TABLE_ID", rs.getString("TABLE_ID"));
                if (!hashSet.contains(hash)){
                    hashSet.add(hash);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such relations found, now deleting them...");
            }
            else
                outputWriteln(count + " such relations found");
            
            count = 0;
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                Hashtable hash = (Hashtable)i.next();
                buf = new StringBuffer();
                buf.append("delete from DST2TBL where DATASET_ID=").append(hash.get("DATASET_ID")).
                append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
                stmt.executeUpdate(buf.toString());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * Deletes non-common elements with no parent table.
     * @throws Exception
     */
    public void deleteOrphanNonCommonElements() throws Exception{
        
        ResultSet rs = null;
        Statement stmt = null;
        try{
            outputWriteln("");
            
            // delete non-common elements with no parent table          
            outputWriteln("searching non-common elements with no parent table...");
            StringBuffer buf = new StringBuffer();
            buf.append("select DATAELEM.DATAELEM_ID from DATAELEM left outer join TBL2ELEM on DATAELEM.DATAELEM_ID=TBL2ELEM.DATAELEM_ID ").
            append("where DATAELEM.PARENT_NS is not null and TBL2ELEM.DATAELEM_ID is null");
            int count = 0;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                String elmID = rs.getString("DATAELEM_ID");
                if (!hashSet.contains(elmID)){
                    hashSet.add(elmID);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such non-common elements found, now deleting them...");
            }
            else
                outputWriteln(count + " such non-common elements found");
            
            count = 0;
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                deleteElm((String)i.next());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
        
    
    /**
     * Deletes TBL2ELEM relations where the table or the element does not actually exist.
     * @throws Exception
     */
    public void deleteBrokenTblToElmRelations() throws Exception{
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            outputWriteln("");
            
            // delete TBL2ELEM relations where the table or the element does not actually exist
            outputWriteln("searching TBL2ELEM relations where the table or the element does not actually exist...");
            StringBuffer buf = new StringBuffer();
            buf.
            append("select TBL2ELEM.* from TBL2ELEM ").
            append("left outer join DS_TABLE on TBL2ELEM.TABLE_ID=DS_TABLE.TABLE_ID "). 
            append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ").
            append("where DS_TABLE.TABLE_ID is null or DATAELEM.DATAELEM_ID is null");
            int count = 0;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                Hashtable hash = new Hashtable();
                hash.put("DATAELEM_ID", rs.getString("DATAELEM_ID"));
                hash.put("TABLE_ID", rs.getString("TABLE_ID"));
                if (!hashSet.contains(hash)){
                    hashSet.add(hash);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such relations found, now deleting them...");
            }
            else
                outputWriteln(count + " such relations found");
            
            count = 0;
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                Hashtable hash = (Hashtable)i.next();
                buf = new StringBuffer();
                buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(hash.get("DATAELEM_ID")).
                append(" and TABLE_ID=").append(hash.get("TABLE_ID"));
                stmt.executeUpdate(buf.toString());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @param tblID
     * @throws Exception
     */
    public void deleteTblWithElements(String tblID) throws Exception{
        
        if (tblID==null)
            return;
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            // find elements in the table
            Vector elmIDs = new Vector();
            StringBuffer buf = new StringBuffer();
            buf.append("select DATAELEM_ID from TBL2ELEM where TABLE_ID=").append(tblID);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                elmIDs.add(rs.getString(1));
            }

            // delete the elements found
            for (int i=0; i<elmIDs.size(); i++)
                this.deleteElm((String)elmIDs.get(i));
            
            // now delete the table itself
            this.deleteTbl(tblID);
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }

    /**
     * 
     * @param tblID
     */
    public void deleteTbl(String tblID) throws Exception{
        
        if (tblID==null)
            return;
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            // delete entries in ATTRIBUTE
            StringBuffer buf = new StringBuffer();
            buf.append("delete from ATTRIBUTE where PARENT_TYPE='T' and DATAELEM_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD
            HashSet hashSet = new HashSet();
            buf = new StringBuffer();
            buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_TYPE='T' and PARENT_ID=").append(tblID);
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                hashSet.add(rs.getString(1));
            }
            rs.close();
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                String rowID = (String)i.next();
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_FIELD where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_ROW where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
            }

            // delete entries in DST2TBL
            buf = new StringBuffer();
            buf.append("delete from DST2TBL where TABLE_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in TBL2ELEM
            buf = new StringBuffer();
            buf.append("delete from TBL2ELEM where TABLE_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in CACHE
            buf = new StringBuffer();
            buf.append("delete from CACHE where OBJ_TYPE='tbl' and OBJ_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DOC
            buf = new StringBuffer();
            buf.append("delete from DOC where OWNER_TYPE='tbl' and OWNER_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DS_TABLE
            buf = new StringBuffer();
            buf.append("delete from DS_TABLE where TABLE_ID=").append(tblID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @param dstID
     * @throws Exception
     */
    public void deleteDstWithTablesAndElements(String dstID) throws Exception{
        
        if (dstID==null)
            return;
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            // find tables in the dataset
            Vector tblIDs = new Vector();
            StringBuffer buf = new StringBuffer();
            buf.append("select TABLE_ID from DST2TBL where DATASET_ID=").append(dstID);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                tblIDs.add(rs.getString(1));
            }

            // delete the tables found
            for (int i=0; i<tblIDs.size(); i++)
                deleteTblWithElements((String)tblIDs.get(i));
            
            // now delete the dataset itself
            deleteDst(dstID);
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }

    }

    /**
     * 
     * @param dstID
     * @throws Exception
     */
    public void deleteDst(String dstID) throws Exception{
        
        if (dstID==null)
            return;
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            // delete entries in ATTRIBUTE
            StringBuffer buf = new StringBuffer();
            buf.append("delete from ATTRIBUTE where PARENT_TYPE='DS' and DATAELEM_ID=").append(dstID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD
            HashSet hashSet = new HashSet();
            buf = new StringBuffer();
            buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_TYPE='DS' and PARENT_ID=").append(dstID);
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                hashSet.add(rs.getString(1));
            }
            rs.close();
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                String rowID = (String)i.next();
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_FIELD where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_ROW where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
            }

            // delete entries in DST2TBL
            buf = new StringBuffer();
            buf.append("delete from DST2TBL where DATASET_ID=").append(dstID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in CACHE
            buf = new StringBuffer();
            buf.append("delete from CACHE where OBJ_TYPE='dst' and OBJ_ID=").append(dstID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DOC
            buf = new StringBuffer();
            buf.append("delete from DOC where OWNER_TYPE='dst' and OWNER_ID=").append(dstID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DATASET
            buf = new StringBuffer();
            buf.append("delete from DATASET where DATASET_ID=").append(dstID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            
            // delete orphan ACLS and NAMESPACES in case they might result from the delete of this dataset
            deleteOrphanAcls();
            deleteOrphanNamespaces();
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }

    /**
     * 
     * @param elmID
     */
    public void deleteElm(String elmID) throws Exception{
        
        if (elmID==null)
            return;
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            // delete entries in ATTRIBUTE
            StringBuffer buf = new StringBuffer();
            buf.append("delete from ATTRIBUTE where PARENT_TYPE='E' and DATAELEM_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in COMPLEX_ATTR_ROW and COMPLEX_ATTR_FIELD
            HashSet hashSet = new HashSet();
            buf = new StringBuffer();
            buf.append("select distinct ROW_ID from COMPLEX_ATTR_ROW where PARENT_TYPE='E' and PARENT_ID=").append(elmID);
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                hashSet.add(rs.getString(1));
            }
            rs.close();
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                String rowID = (String)i.next();
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_FIELD where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
                buf = new StringBuffer();
                buf.append("delete from COMPLEX_ATTR_ROW where ROW_ID='").append(rowID).append("'");
                stmt.executeUpdate(buf.toString());
            }

            // delete entries in TBL2ELEM
            buf = new StringBuffer();
            buf.append("delete from TBL2ELEM where DATAELEM_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in CACHE
            buf = new StringBuffer();
            buf.append("delete from CACHE where OBJ_TYPE='elm' and OBJ_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DOC
            buf = new StringBuffer();
            buf.append("delete from DOC where OWNER_TYPE='elm' and OWNER_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            
            // delete entries in FK_RELATION
            buf = new StringBuffer();
            buf.append("delete from FK_RELATION where A_ID=").append(elmID).append(" or B_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in FXV
            buf = new StringBuffer();
            buf.append("delete from FXV where OWNER_TYPE='elem' and OWNER_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());

            // delete entries in DATAELEM
            buf = new StringBuffer();
            buf.append("delete from DATAELEM where DATAELEM_ID=").append(elmID);
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    public void deleteOrphanNamespaces() throws Exception{
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            outputWriteln("");
            
            // delete NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table
            outputWriteln("searching NAMESPACE entries that don't have a corresponding dataset, nor a corresponding table...");
            StringBuffer buf = new StringBuffer();
            buf.
            append("select NAMESPACE.NAMESPACE_ID ").
            append("from NAMESPACE left outer join DATASET on NAMESPACE.NAMESPACE_ID=DATASET.CORRESP_NS ").
            append("left outer join DS_TABLE on NAMESPACE.NAMESPACE_ID=DS_TABLE.CORRESP_NS where ").
            append("DATASET.CORRESP_NS is null and DS_TABLE.CORRESP_NS is null");
            int count = 0;
            String nsID = null;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                nsID = rs.getString(1);
                if (!hashSet.contains(nsID)){
                    hashSet.add(nsID);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such namespaces found, now deleting them...");
            }
            else
                outputWriteln(count + " such namespaces found");
            
            count = 0;          
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                buf = new StringBuffer();
                buf.append("delete from NAMESPACE where NAMESPACE_ID=").append(i.next());
                stmt.executeUpdate(buf.toString());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void deleteOrphanAcls() throws Exception{
        
        ResultSet rs = null;
        Statement stmt = null;      
        try{
            outputWriteln("");
            
            // delete object ACLs of objects that do not actually exist
            outputWriteln("searching object ACLs of objects that do not actually exist...");
            StringBuffer buf = new StringBuffer();
            buf.
            append("select ACL_ID from ACLS left outer join DATASET on ACLS.ACL_NAME=DATASET.IDENTIFIER ").
            append("where ACLS.PARENT_NAME='/datasets' and DATASET.IDENTIFIER is null");
            int count = 0;
            String aclID = null;
            HashSet hashSet = new HashSet();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                aclID = rs.getString(1);
                if (!hashSet.contains(aclID)){
                    hashSet.add(aclID);
                    count++;
                }
            }
            rs.close();
            buf = new StringBuffer();
            buf.
            append("select ACL_ID from ACLS left outer join DATAELEM on ACLS.ACL_NAME=DATAELEM.IDENTIFIER ").
            append("where ACLS.PARENT_NAME='/elements' and DATAELEM.PARENT_NS is null and DATAELEM.IDENTIFIER is null");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs!=null && rs.next()){
                aclID = rs.getString(1);
                if (!hashSet.contains(aclID)){
                    hashSet.add(aclID);
                    count++;
                }
            }
            rs.close();
            
            boolean attemptingDelete = false;
            if (count>0){
                attemptingDelete = true;
                outputWriteln(count + " such ACLs found, now deleting them...");
            }
            else
                outputWriteln(count + " such ACLs found");
            
            count = 0;
            for (Iterator i = hashSet.iterator(); !hashSet.isEmpty() && i.hasNext();){
                aclID = (String)i.next();
                buf = new StringBuffer();
                buf.append("delete from ACL_ROWS where ACL_ID=").append(aclID);
                stmt.executeUpdate(buf.toString());
                buf = new StringBuffer();
                buf.append("delete from ACLS where ACL_ID=").append(aclID);
                stmt.executeUpdate(buf.toString());
                count++;
            }
            
            if (attemptingDelete)
                outputWriteln(count + " deleted");
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            }
            catch (SQLException e){}
        }
    }

    /**
     * Cleans the database from broken DST2TBL and TBL2ELEM relations, orphan tables and non-common elements,
     * orphan namespaces and orphan acls.
     * @throws Exception
     */
    public void cleanup() throws Exception{

        // Orphan tables must be delete before orphan elements, because deleteTbl(String) does not delete
        // the table's elements. As a result, the latter become orphans, which are then nicely eliminated
        // by deleteing orphan elements.
        
        // cleanup tables
        deleteBrokenDstToTblRelations();
        deleteOrphanTables();

        // cleanup elements
        deleteBrokenTblToElmRelations();
        deleteOrphanNonCommonElements();        
        
        // cleanup namespaces
        deleteOrphanNamespaces();
        
        // cleanup acls
        deleteOrphanAcls();
    }

    /**
     * 
     * @param message
     * @throws IOException 
     */
    public void outputWrite(String message) throws IOException{
        if (outputWriter!=null && message!=null){
            outputWriter.print(message);
            outputWriter.flush();
        }
    }

    /**
     * 
     * @param message
     * @throws IOException 
     */
    public void outputWriteln(String message) throws IOException{
        if (outputWriter!=null && message!=null){
            outputWriter.println(message);
            outputWriter.flush();
        }
    }
    
    /**
     * 
     * @return
     * @throws DDConnectionException 
     */
    public static Connection getTestConnection() throws DDConnectionException{
        return ConnectionUtil.getSimpleConnection();
    }
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args){
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            DataManipulations script = new DataManipulations(DataManipulations.getTestConnection(), new PrintWriter(System.out));
    }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
        finally{
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
            }
            catch (SQLException e){}            
        }
    }
}