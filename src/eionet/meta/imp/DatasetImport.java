

// Copyright (c) 2000 TietoEnator
package eionet.meta.imp;


import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import eionet.meta.*;
import eionet.meta.savers.*;

import javax.servlet.ServletContext;

/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class DatasetImport{

    public static String SEP = "_";

    private DatasetImportHandler handler;
    private DDSearchEngine searchEngine;
    private Connection conn = null;
    private ServletContext ctx = null;

    private String baseUrl=null;
    private StringBuffer responseText = new StringBuffer();
    private String lastInsertID = null;
    private Hashtable tables;
    private Vector dbSimpleAttrs;
    private Vector dbComplexAttrs;

//Parameter vectors
/*    private Vector ds_params;
//    private Vector tbl_params;
    private Vector delem_params;
    private Vector fxv_params;
    private Vector tbl2elem_params;
*/
    private Hashtable all_params=null;
    private Hashtable tblMap=null;
    private Vector complex_attrs=null;
    private Hashtable unknown_tbl=null;
//id mapping
    private Hashtable dsID;
    private Hashtable tblID;
    private Hashtable delemID;
    private Hashtable fxvID;

    private int ds_count=0;
    private int delem_count=0;
    private int tbl_count=0;

    private int ds_count_all=0;
    private int delem_count_all=0;
    private int tbl_count_all=0;
  /**
   * Constructor
   */
    public DatasetImport(DatasetImportHandler handler, Connection conn, ServletContext ctx, String basensPath, String type){
        this.handler = handler;
        this.conn = conn;
        this.searchEngine = new DDSearchEngine(conn, null, ctx);
        this.baseUrl = basensPath;
        this.ctx=ctx;
        tblMap = new Hashtable();
        all_params = new Hashtable();
        setMapping();
    }

    public void execute() throws Exception {

   /*   Parameters params=null;
      Hashtable dataset;
      Hashtable ds_table;
      Hashtable delem;
      Hashtable fxv;
      Hashtable tbl2elem;
      ds_params = new Vector();
      //tbl_params = new Vector();
      delem_params = new Vector();
      fxv_params = new Vector();
      tbl2elem_params = new Vector();*/
      tables = handler.getTables();
      setDBAttrs();
/*
      // make dataset parameters vector
      Vector ds = (Vector)tables.get("DATASET");
      for (int i=0; i<ds.size(); i++){
          dataset= (Hashtable)ds.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          String ds_id = (String)dataset.get("dataset_id");
          String ds_name = (String)dataset.get("short_name");
          String ds_version = (String)dataset.get("version");

          if (ds_id==null || ds_id.length() == 0){
            responseText.append("Dataset id is empty!<br>");
            break;
          }
          if (ds_name==null || ds_name.length() == 0){
            responseText.append("Dataset name is empty!<br>");
            break;
          }
          if (ds_version==null || ds_version.length() == 0){
            responseText.append("Dataset version is empty!<br>");
            break;
          }

          params.addParameterValue("ds_id", ds_id);
          params.addParameterValue("ds_name", ds_name);
          params.addParameterValue("version", ds_version);

          dataset.remove("short_name");dataset.remove("version");dataset.remove("dataset_id");
          getSimpleAttrs(dataset, params, "DST", ds_name);

          ds_params.add(params);
      }
      responseText.append("<br>tables:");
      // make tables parameters vector
      Vector tbl = (Vector)tables.get("DS_TABLE");
      for (int i=0; i<tbl.size(); i++){
          ds_table= (Hashtable)tbl.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          String tbl_id = (String)ds_table.get("table_id");
          String ds_id = (String)ds_table.get("dataset_id");
          String tbl_sname = (String)ds_table.get("short_name");
          String tbl_fname = (String)ds_table.get("name");
          String tbl_def = (String)ds_table.get("definition");

          params.addParameterValue("ds_id", ds_id);
          params.addParameterValue("tbl_id", tbl_id);
          params.addParameterValue("short_name", tbl_sname);
          params.addParameterValue("full_name", tbl_fname);
          params.addParameterValue("definition", tbl_def);


          tbl_params.add(params);
      }
      responseText.append("<br>data elements:");
      // make data element parameters vector
      Vector dataelems = (Vector)tables.get("DATAELEM");
      for (int i=0; i<dataelems.size(); i++){
          delem = (Hashtable)dataelems.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          String delem_id = (String)delem.get("dataelem_id");
          String delem_name = (String)delem.get("short_name");
          String type = (String)delem.get("type");
          String ns_id = (String)delem.get("namespace_id");

          params.addParameterValue("delem_id", delem_id);
          params.addParameterValue("type", type);
          params.addParameterValue("delem_name", delem_name);
          params.addParameterValue("ns", ns_id);

          delem.remove("short_name");delem.remove("type");delem.remove("namespace_id");delem.remove("dataelem_id");
          getSimpleAttrs(delem, params, type, delem_name);

          delem_params.add(params);
      }
      responseText.append("<br>fixed values:");
      // make data element parameters vector
      Vector fxvalues = (Vector)tables.get("FIXED_VALUE");
      for (int i=0; i<fxvalues.size(); i++){
          fxv = (Hashtable)fxvalues.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          String delem_id = (String)fxv.get("dataelem_id");
          String fxv_id = (String)fxv.get("fixed_value_id");
          String value = (String)fxv.get("value");

          //params.addParameterValue("fxv_id", fxv_id);
          params.addParameterValue("delem_id", delem_id);
          params.addParameterValue("new_value", value);

          fxv.remove("fxv_id");fxv.remove("value");fxv.remove("dataelem_id");
          getSimpleAttrs(fxv, params, "FXV", value);

          fxv_params.add(params);
      }
      responseText.append("<br>table 2 elems:");
      // make tbl 2 elems parameters vector
      Vector tbl2elems = (Vector)tables.get("TBL2ELEM");
      for (int i=0; i<tbl2elems.size(); i++){
          tbl2elem = (Hashtable)tbl2elems.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          String delem_id = (String)tbl2elem.get("dataelem_id");
          String table_id = (String)tbl2elem.get("table_id");
          String position = (String)tbl2elem.get("position");

          params.addParameterValue("table_id", table_id);
          params.addParameterValue("delem_id", delem_id);
          params.addParameterValue("position", position);

          //fxv.remove("fxv_id");fxv.remove("value");fxv.remove("dataelem_id");
          //getSimpleAttrs(fxv, params, "FXV", value);

          tbl2elem_params.add(params);
      }*/

      setParams("DATASET", true, "DST", "ds_name", "DS", "ds_id");
      setParams("DS_TABLE", true, "TBL", "short_name", null, null);
      setParams("DATAELEM", true, null, "delem_name", "E", "delem_id");
      setParams("TBL2ELEM", false, null, null, null, null);
      setParams("FIXED_VALUE", true, "FXV", "new_value", null, null);

                        /*For testing
                        for (int i=0; i<complex_attrs.size(); i++){
                          Parameters params = (Parameters)complex_attrs.get(i);
                          Enumeration pars = params.getParameterNames();
                          String parname;
                          while (pars.hasMoreElements()){
                              parname=(String)pars.nextElement();
                              responseText.append(parname + "=" + params.getParameter(parname) + "|");
                          }
                          responseText.append("<br>");
                        }
                        */

      saveDataset();
      saveTables();
      saveDElem();
      saveFixedValues();
      saveComplexAttrs();

      responseText.append("<br>");
      responseText.append("<br>Datasets found:" + ds_count_all + "; successfully imported:" + ds_count);
      responseText.append("<br>Dataset tables found:" + tbl_count_all + "; successfully imported:" + tbl_count);
      responseText.append("<br>Data elements found:" + delem_count_all + "; successfully imported:" + delem_count);
      responseText.append("<br>");
      responseText.append("<br>Unknown fields found from the following tables:");
      Enumeration keys = unknown_tbl.keys();
      while (keys.hasMoreElements()){
        String key = (String)keys.nextElement();
        responseText.append("<br>" + key + ": " + unknown_tbl.get(key).toString());
      }


    }
    private void saveDataset() throws Exception{
        DatasetHandler dsHandler;
        Parameters par;
        dsID = new Hashtable();

        Vector ds_params = (Vector)all_params.get("DATASET");
        if(ds_params == null) return;
        if(ds_params.size()==0) return;

        for (int i=0; i< ds_params.size(); i++){
            par =(Parameters)ds_params.get(i);
            try{
                dsHandler = new DatasetHandler(conn, par, ctx);
                dsHandler.execute();
                ds_count++;
                dsID.put((String)par.getParameter("ds_id"), (String)dsHandler.getLastInsertID());
             }
             catch(Exception e){
                 responseText.append("Dataset import failed! Could not store dataset into database - " +
                par.getParameter("ds_name") + "<br>");
                 responseText.append(e.toString() + "<br>");
             }
        }
      //  responseText.append(dsID.toString());
    }
    private void saveTables() throws Exception{
        DsTableHandler tblHandler;
        Parameters par;
        String ds_id;
        tblID = new Hashtable();

        Vector tbl_params = (Vector)all_params.get("DS_TABLE");

        if(tbl_params == null) return;
        if(tbl_params.size()==0) return;

        for (int i=0; i< tbl_params.size(); i++){
            par =(Parameters)tbl_params.get(i);
            ds_id = par.getParameter("ds_id");
            if (dsID.containsKey(ds_id)){
                par.removeParameter("ds_id");
                par.addParameterValue("ds_id", (String)dsID.get(ds_id));
            }
            else{
                responseText.append("Dataset id was not found for table: " +
                par.getParameter("short_name") + "<br>");
                continue;
            }
            try{
                tblHandler = new DsTableHandler(conn, par, ctx);
                tblHandler.execute();
                tbl_count++;
                tblID.put((String)par.getParameter("tbl_id"), (String)tblHandler.getLastInsertID());
            }
            catch(Exception e){
                responseText.append("Dataset import failed! Could not store dataset table into database - " +
                par.getParameter("short_name") + "<br>");
                responseText.append(e.toString() + "<br>");
            }
        }
  //      responseText.append(tblID.toString());
    }
    private void saveDElem() throws Exception{
        DataElementHandler delemHandler;
        Parameters par;
        delemID = new Hashtable();
        String delem_id;

        Vector delem_params = (Vector)all_params.get("DATAELEM");

        if(delem_params == null) return;
        if(delem_params.size()==0) return;

        for (int i=0; i< delem_params.size(); i++){
            par =(Parameters)delem_params.get(i);
            delem_id=(String)par.getParameter("delem_id");
            getTbl2elem(delem_id, par);
            par.removeParameter("delem_id");
            try{
                delemHandler = new DataElementHandler(conn, par, ctx);
                delemHandler.execute();
                delem_count++;
                delemID.put(delem_id, (String)delemHandler.getLastInsertID());
            }
            catch(Exception e){
                responseText.append("Dataset import failed! Could not store data element into database - " +
                par.getParameter("delem_name") + "<br>");
                responseText.append(e.toString() + "<br>");
            }
        }
       // responseText.append(delemID.toString());
    }
    private void getTbl2elem(String elem_id, Parameters par){
        Parameters tbl2elem_par;
        String tbl_id;
        String parelem_id;

        Vector tbl2elem_params = (Vector)all_params.get("TBL2ELEM");

        for(int i=0; i<tbl2elem_params.size(); i++){
            tbl2elem_par = (Parameters)tbl2elem_params.get(i);
            parelem_id = (String)tbl2elem_par.getParameter("delem_id");
            if (parelem_id.equals(elem_id)){
                tbl_id = (String)tbl2elem_par.getParameter("table_id");
                par.addParameterValue("table_id", (String)tblID.get(tbl_id));
                par.addParameterValue("pos", (String)tbl2elem_par.getParameter("pos"));
                break;
            }
        }
    }
    private void saveFixedValues() throws Exception{
        FixedValuesHandler fxvHandler;
        Parameters par;
        String delem_id;
        String fxv_val=null;
        fxvID = new Hashtable();

        Vector fxv_params = (Vector)all_params.get("FIXED_VALUE");

        if(fxv_params == null) return;
        if(fxv_params.size()==0) return;


        for (int i=0; i< fxv_params.size(); i++){
            par =(Parameters)fxv_params.get(i);

            fxv_val = par.getParameter("new_value");
            if (fxv_val == null) par.addParameterValue("new_value", "");

            delem_id = par.getParameter("delem_id");
            if (delemID.containsKey(delem_id)){
                par.removeParameter("delem_id");
                par.addParameterValue("delem_id", (String)delemID.get(delem_id));
            }
            else{
                responseText.append("Data element id was not found for fixed value " +
                fxv_val + "<br>");
                continue;
            }
            try{
                fxvHandler = new FixedValuesHandler(conn, par, ctx);
                fxvHandler.execute();
             }
            catch(Exception e){
                responseText.append("Dataset import failed! Could not store fixed value into database - " +
                fxv_val + "<br>");
                responseText.append(e.toString() + "<br>");
            }
        }
    }
    private void saveComplexAttrs() throws Exception{
        AttrFieldsHandler saveHandler;
        Parameters par;
        String parent_id;
        String parent_type;

        if(complex_attrs == null) return;
        if(complex_attrs.size()==0) return;

        for (int i=0; i< complex_attrs.size(); i++){
            par =(Parameters)complex_attrs.get(i);
            parent_id = par.getParameter("parent_id");
            parent_type = par.getParameter("parent_type");
//responseText.append("d:" + parent_id);
            if (parent_type.equals("E")){  //element
                if (delemID.containsKey(parent_id)){
                    par.removeParameter("parent_id");
                    par.addParameterValue("parent_id", (String)delemID.get(parent_id));
                }
                else{
                    responseText.append("Data element id was not found for complex attribute.<br>");
                    continue;
                }
            }
            else if (parent_type.equals("DS")){  //dataset
                if (dsID.containsKey(parent_id)){
                    par.removeParameter("parent_id");
                    par.addParameterValue("parent_id", (String)dsID.get(parent_id));
                }
                else{
                    responseText.append("Data element id was not found for complex attribute.<br>");
                    continue;
                }
            }
            else
                continue;
            try{
                saveHandler = new AttrFieldsHandler(conn, par, ctx);
                saveHandler.execute();
             }
            catch(Exception e){
                responseText.append("Could not store complex attributes into database");
                responseText.append(e.toString() + "<br>");
            }
        }
    }
    private void replaceAttributes(Hashtable impAttrs, Parameters params){
       String attrName;
       Enumeration attrKeys = impAttrs.keys();
       while (attrKeys.hasMoreElements()){
          attrName = (String)attrKeys.nextElement();
          String attrValue = (String)impAttrs.get(attrName);
      }
    }
    private void setParams(String table, boolean hasAttrs, String type, String context, String parent_type, String id_field){
      Parameters params;
      Vector tbl_params;
      Hashtable row;
      Vector rowMap;
      Hashtable fieldMap;
      String importValue;
      String allowNull;

      tbl_params=new Vector();

      Vector tbl = (Vector)tables.get(table);
      if (table.equals("DATASET"))
          ds_count_all=tbl.size();
      else if (table.equals("DS_TABLE"))
          tbl_count_all=tbl.size();
      else if (table.equals("DATAELEM"))
          delem_count_all=tbl.size();

      for (int i=0; i<tbl.size(); i++){
          row= (Hashtable)tbl.get(i);

          params = new Parameters();
          params.addParameterValue("mode", "add");

          rowMap=(Vector)tblMap.get(table);
          for (int c=0; c<rowMap.size(); c++){
              fieldMap=(Hashtable)rowMap.get(c);
              importValue = (String)row.get((String)fieldMap.get("imp"));
              allowNull=(String)fieldMap.get("allowNull");
              if (allowNull.equals("false")){
                  if (importValue==null || importValue.length() == 0){
                    responseText.append((String)fieldMap.get("text") + " is empty!<br>");
                    break;
                  }
              }
              row.remove((String)fieldMap.get("imp"));
              params.addParameterValue((String)fieldMap.get("param"), importValue);
          }
          if (hasAttrs){
              if (type==null)
                  type=params.getParameter("type");
              getSimpleAttrs(row, params, type, (String)params.getParameter(context));
              if (parent_type!=null){
                try{
                  getComplexAttrs(row, (String)params.getParameter(id_field), parent_type);
                }
                catch(Exception e){
                  responseText.append("Reading complex attributes failed - " +
                  (String)params.getParameter("context") + "<br>");
                  responseText.append(e.toString() + "<br>");
                }
              }
          }
                        /*For testing
                          Enumeration pars = params.getParameterNames();
                          String parname;
                          while (pars.hasMoreElements()){
                              parname=(String)pars.nextElement();
                              responseText.append(parname + "=" + params.getParameter(parname) + "|");
                          }
                          responseText.append("<br>");
                        */
          addUnknown(table, row);
          tbl_params.add(params);
      }
      all_params.put(table, tbl_params);
    }
    private void getSimpleAttrs(Hashtable row, Parameters params, String type, String context_name){
        String attrName;
        String attrValue;
        for (int i=0; i< dbSimpleAttrs.size(); i++){
            DElemAttribute delemAttr = (DElemAttribute)dbSimpleAttrs.get(i);
            if (delemAttr.displayFor(type)){
                attrName = delemAttr.getShortName();
//find mandatory attributes
                if (delemAttr.getObligation().equals("M")){
                    if (row.containsKey(attrName.toLowerCase())){
                        attrValue = (String)row.get(attrName.toLowerCase());
                        if (attrValue == null || attrValue.length()==0){
                            responseText.append("Could not find mandatory attribute (" + attrName + ") value from specified xml for " + getContextName(type) + " - " + context_name + "!<br>");
                        }
                        else{
                            params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                        }
                        row.remove(attrName.toLowerCase());
                    }
                    else{
                        responseText.append("Could not find mandatory attribute (" + attrName + ") value from specified xml for " + getContextName(type) + " - " + context_name + "!<br>");
                    }
                }
//find other attributes
                else{
                    if (row.containsKey(attrName.toLowerCase())){
                        attrValue = (String)row.get(attrName.toLowerCase());
                        if (attrValue != null && attrValue.length() != 0){
                            params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                        }
                        row.remove(attrName.toLowerCase());
                    }
                }
            }
        }
    }
    private void  getComplexAttrs(Hashtable row, String parent_id, String parent_type) throws Exception{
        String attrName;
        String attrValue;
        String attr_id;
        Enumeration impAttrKeys;
        String impFieldName=null;
        Parameters par;
        String fieldName;
        Vector attrFields=null;
        Hashtable field;
        String impAttrValue;
        boolean bHasAttr=false;
        boolean bHasField=true;

        if (complex_attrs == null) complex_attrs = new Vector();
        for (int i=0; i< dbComplexAttrs.size(); i++){
            bHasAttr = false;
            DElemAttribute delemAttr = (DElemAttribute)dbComplexAttrs.get(i);
            attrName = delemAttr.getShortName();
            attr_id = delemAttr.getID();
            impAttrKeys = row.keys();
            while (impAttrKeys.hasMoreElements()){
              impFieldName = (String)impAttrKeys.nextElement();
              bHasAttr = (impFieldName.startsWith(attrName.toLowerCase() + SEP)) ? true : false;
              if (bHasAttr) break;
            }
            if (bHasAttr){
                attrFields = searchEngine.getAttrFields(attr_id);
                if (attrFields==null) continue;
                for(int c=1; bHasField; c++){
                  bHasField = false;
                  par = new Parameters();
                  for (int j=0; j< attrFields.size(); j++){
                    field =  (Hashtable)attrFields.get(j);
                    fieldName = (String)field.get("name");
                    impFieldName = attrName.toLowerCase() + SEP + fieldName.toLowerCase() + SEP + Integer.toString(c);
                    if(row.containsKey(impFieldName)){
                        impAttrValue = (String)row.get(impFieldName);
                        if (impAttrValue == null || impAttrValue.length() == 0){
                          row.remove(impFieldName);
                          continue;
                        }

                        par.addParameterValue(AttrFieldsHandler.FLD_PREFIX + (String)field.get("id"), impAttrValue);
                        bHasField = true;
                    }
                  }

                  if (bHasField){
                    par.addParameterValue("mode", "add");
                    par.addParameterValue("parent_type", parent_type);
                    par.addParameterValue("parent_id",  parent_id);
                    par.addParameterValue("position", Integer.toString(c));
                   // par.addParameterValue("name", impFieldName);
                    par.addParameterValue("attr_id", attr_id);

                    complex_attrs.add(par);
                  }
                }
                bHasField=true;
            }
            //remove empty attributes rows
            impAttrKeys = row.keys();
            while (impAttrKeys.hasMoreElements()){
              impFieldName = (String)impAttrKeys.nextElement();
              for (int j=0; j< attrFields.size(); j++){
                field =  (Hashtable)attrFields.get(j);
                fieldName = (String)field.get("name");
                if (impFieldName.startsWith(attrName.toLowerCase() + SEP + fieldName.toLowerCase()))
                    row.remove(impFieldName);
              }
            }
        }
    }

    private void setDBAttrs() throws Exception{
        dbSimpleAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);
        dbComplexAttrs = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
    }
    private void setMapping(){
        Vector rowMap=new Vector();

        //DATASET
        rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DATASET table"));
        rowMap.add(getFieldMap("short_name", "ds_name", false, "dataset short name in DATASET table"));
        rowMap.add(getFieldMap("version", "version", false, "Dataset version in DATASET table"));
        tblMap.put("DATASET", rowMap);
        rowMap = new Vector();

        //DS_TABLE
        rowMap.add(getFieldMap("table_id", "tbl_id", false, "dataset table id in DS_TABLE table"));
        rowMap.add(getFieldMap("dataset_id", "ds_id", false, "dataset id in DS_TABLE table"));
        rowMap.add(getFieldMap("short_name", "short_name", false, "dataset table short name in DS_TABLE table"));
        //rowMap.add(getFieldMap("name", "full_name", true, "dataset table full name in DS_TABLE table"));
        //rowMap.add(getFieldMap("definition", "definition", true, "Dataset table definition short name in DS_TABLE table"));
        tblMap.put("DS_TABLE", rowMap);
        rowMap = new Vector();

        //DATA ELEMENT
        rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in DATAELEM table"));
        rowMap.add(getFieldMap("type", "type", false, "data element type in DATAELEM table"));
        rowMap.add(getFieldMap("short_name", "delem_name", false, "data element short name in DATAELEM table"));
 //       rowMap.add(getFieldMap("namespace_id", "ns", true, "data element namespace_id in DATAELEM table"));
        tblMap.put("DATAELEM", rowMap);
        rowMap = new Vector();

        //TBL2ELEM
        rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in TBL2ELEM table"));
        rowMap.add(getFieldMap("table_id", "table_id", false, "dataset table id in TBL2ELEM"));
        rowMap.add(getFieldMap("position", "pos", true, "position in TBL2ELEM table"));
        tblMap.put("TBL2ELEM", rowMap);
        rowMap = new Vector();

        //FIXED VALUE
        rowMap.add(getFieldMap("dataelem_id", "delem_id", false, "data element id in FIXED_VALUE table"));
        rowMap.add(getFieldMap("fixed_value_id", "id", true, "id"));
        rowMap.add(getFieldMap("value", "new_value", true, "fixed value in FIXED_VALUE table"));
        tblMap.put("FIXED_VALUE", rowMap);
    }
    private void addUnknown(String table, Hashtable row){
        Vector unknown=null;
        String field;

        if (unknown_tbl == null) unknown_tbl = new Hashtable();
        if (unknown_tbl.containsKey(table)){
            unknown=(Vector)unknown_tbl.get(table);
        }
        if (unknown==null) unknown=new Vector();
        Enumeration keys = row.keys();
        while (keys.hasMoreElements()){
            field = (String)keys.nextElement();
            if (unknown.indexOf(field)==-1){
                unknown.add(field);
            }
        }
        if(unknown.size()>0) unknown_tbl.put(table, unknown);

    }
    private Hashtable getFieldMap(String impField, String param, boolean allowNull, String text){
        Hashtable fieldMap = new Hashtable();
        fieldMap.put("imp", impField);
        fieldMap.put("param", param);
        fieldMap.put("allowNull", String.valueOf(allowNull));
        fieldMap.put("text", text);

        return fieldMap;

    }
    private String getContextName(String code){
        String ret="";
        if (code.equals("DST"))
            ret = "dataset";
        if (code.equals("FXV"))
            ret = "allowable value";
        if (code.equals("CH1"))
            ret = "data element with fixed values";
        if (code.equals("CH2"))
            ret = "data element with quantitative values";
        if (code.equals("TBL"))
            ret = "dataset table";
        return ret;
    }
    public String getResponseText(){
        return responseText.toString();
    }
}

