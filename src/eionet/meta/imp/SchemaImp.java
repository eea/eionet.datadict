package eionet.meta.imp;

import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.meta.savers.*;
import javax.servlet.ServletContext;

/**
* As of 290503 this whole class needs a re-look.
* because a lot has changed. For now it's deprecated!
*/
public class SchemaImp{

    private SchemaHandler handler;
    private DDSearchEngine searchEngine;
    private Parameters params;
    private Connection conn = null;
    private ServletContext ctx = null;
    private Vector dbNamespaces = null;
    private Hashtable schNamespaces = null;

    private String ns_id=null;
    
    // JH290503 - trying to get rid of basensPath
    //private String baseUrl=null;
    
    private String type=null;
    private StringBuffer responseText = new StringBuffer();
    private String lastInsertID = null;

    private String delem_name=null;

    //public SchemaImp(SchemaHandler handler, Connection conn, ServletContext ctx, String basensPath, String type){
    // JH290503 - trying to get rid of basensPath
    public SchemaImp(SchemaHandler handler, Connection conn, ServletContext ctx, String type){
        this.handler = handler;
        this.conn = conn;
        this.searchEngine = new DDSearchEngine(conn, null, ctx);
        this.params = new Parameters();
        //this.baseUrl = basensPath; // JH290503 - trying to get rid of basensPath
        this.type = type;
        this.ctx=ctx;
    }

    public void execute() throws Exception {

        Vector fixedValues = null;
        Vector complexAttributes = null;
        Vector subElements = null;

//Get namespace id from database
        String nsUrl = handler.getNamespace();
        if (nsUrl != null && nsUrl.length() > 0){
            try{
              ns_id= getNamespaceID(nsUrl, null);
            }
            catch(Exception e){
                responseText.append(e.toString() + "<br>");
                return;
            }
        }
        else{
            responseText.append("Could not find targetNamspace attribute from specified schema!<br>");
            return;
        }
        if (ns_id == null){
            responseText.append("Could not find target namespace " + nsUrl + " from database. Please add it before into the Data dictionary!");
            return;
        }

//Check, if dataelement shortname exists
        delem_name = handler.getShortName();

        if (delem_name == null || delem_name.length() == 0){
            responseText.append("Could not find name attribute from specified schema element tag!<br>");
            return;
        }

// set parameters for savehandler

        params.addParameterValue("ns", ns_id);
        params.addParameterValue("delem_name", delem_name);
        params.addParameterValue("mode", "add");
        params.addParameterValue("type", type);




        try{
            getSimpleAttributes();      //get simple attributes
            complexAttributes = getComplexAttributes();  //get complex attributes

            // check, if there are  any fixed values
            fixedValues = handler.getFixedValues();
            if(fixedValues.size()>0){
                if (!type.equals("CH1")){
                    responseText.append("This data element type can not have fixed values!<br>");
                    return;
                }
                else{
                    params.addParameterValues("new_value", fixedValues);
                }
            }
            // check, if there are  subelements
            subElements = getSubElements();
            if(subElements.size()>0 &&
                !type.equals("AGG")){
                    responseText.append("This data element type can not have subelements!<br>");
                    return;
            }

            // insert data element and simple attributes with DataElementHandler
            DataElementHandler delemHandler = new DataElementHandler(conn, params, ctx);
            delemHandler.execute();
            lastInsertID = delemHandler.getLastInsertID();
            responseText.append("Successfully imported a data element named <a href='data_element.jsp?delem_id=" + lastInsertID +"&type=" + type + "&mode=edit'>" + delem_name + "</a><br>");
        }
        catch(Exception e){
            responseText.append("<h2>Import failed!<h2><br><br>");
            responseText.append(e.toString());
            return;
        }

        if (lastInsertID != null){

            insertComplexAttributes(complexAttributes);

            insertSubElements(subElements);

            params.addParameterValue("delem_id", lastInsertID);
            insertFixedValues();
        }
    }

/*
insert fixed values with FixedValuesHandler
*/
    private void insertFixedValues(){
        try{
            FixedValuesHandler valuesHandler = new FixedValuesHandler(conn, params, ctx);
            valuesHandler.execute();
        }
        catch(Exception e){
            responseText.append("Fixed values import failed!<br>");
            responseText.append(e.toString() + "<br>");
        }
        return;
    }

/*
 insert complex attributes with AttrFieldsHandler
*/
    private void insertComplexAttributes(Vector paramsVector){
        AttrFieldsHandler attrFieldsHandler;
        Parameters par;

        if(paramsVector == null) return;
        if(paramsVector.size()==0) return;

        try{
            for (int i=0; i< paramsVector.size(); i++){
                par =(Parameters)paramsVector.get(i);
                        /*For testing
                          Enumeration pars = par.getParameterNames();
                          String parname;
                          while (pars.hasMoreElements()){
                              parname=(String)pars.nextElement();
                              responseText.append(parname + "=" + par.getParameter(parname) + "|");
                          }
                          responseText.append("<br>");
                        */
                 par.addParameterValue("parent_id", lastInsertID);
                 attrFieldsHandler = new AttrFieldsHandler(conn, par, ctx);
                 attrFieldsHandler.execute();
             }
        }
        catch(Exception e){
            responseText.append("Complex Attributes import failed!<br>");
            responseText.append(e.toString() + "<br>");
        }
        return;
    }
/*
 insert subelement with SubElemsHandler
*/
    private void insertSubElements(Vector paramsVector){
        SubElemsHandler subElemsHandler;
        Parameters par;
        String parentID=null;
        String contentID=null;

        if (paramsVector == null) return;
        if(paramsVector.size()==0) return;

        try{
            for (int i=0; i< paramsVector.size(); i++){
                par =(Parameters)paramsVector.get(i);
/*    For testing only
                          Enumeration pars = par.getParameterNames();
                          String parname;
                          while (pars.hasMoreElements()){
                              parname=(String)pars.nextElement();
                              responseText.append(parname + "=" + par.getParameter(parname) + "|");
                          }
                          responseText.append("<br>");
/* */
                 if (par.getParameter("parent_type").equals("elm"))
                     parentID = lastInsertID;
                 else
                     par.addParameterValue("content_id", contentID);

                 par.addParameterValue("parent_id", parentID);

                 subElemsHandler = new SubElemsHandler(conn, par, ctx);

                 if(parentID == null || parentID.length()==0){
                     responseText.append("Some of the subelements have not been imported!<br>");
                     responseText.append("Could not get parent_id<br>");
                     continue;
                 }

                 subElemsHandler.execute();

                 if (!par.getParameter("childType").equals("elm")){
                     parentID = subElemsHandler.getContentID();
                     contentID = subElemsHandler.getLastInsertChildID();
                 }
            }
        }
        catch(Exception e){
            responseText.append("Subelements import failed!<br>");
            responseText.append(e.toString() + "<br>");
        }
        return;
    }

/*
    returns the vector with parameters for sub elements (subelement = parameter)
*/
    private Vector getComplexAttributes() throws Exception{
        String attrName;
        String attrID;
        String attrShortName;
        String attrValue;
        Namespace ns;

        DElemAttribute delemAttr;
        Vector schAttrRows;
        Vector paramsVector = new Vector();
        Vector complexDelemAttr = searchEngine.getDElemAttributes(DElemAttribute.TYPE_COMPLEX);
        Hashtable schComplexAttrs = handler.getComplexAttributes();
        Hashtable schAttrFields;

        for (int i=0; i< complexDelemAttr.size(); i++){  // get all attributes from db
            delemAttr = (DElemAttribute)complexDelemAttr.get(i);
            attrShortName = delemAttr.getShortName();
            ns = delemAttr.getNamespace();
            attrName = ns.getShortName() + ":" + attrShortName;
            if (schComplexAttrs.containsKey(attrName)){  // attribute found from schema
                attrID = delemAttr.getID();
                schAttrRows = (Vector)schComplexAttrs.get(attrName); //read the attr rows from schema
                for (int c=0; c< schAttrRows.size(); c++){
                    schAttrFields = (Hashtable)schAttrRows.get(c);   //get fields hashtable from schema
                    paramsVector.add(getRowParams(schAttrFields, attrID, attrName, Integer.toString(c)));  // add the fields into params
                }
               schComplexAttrs.remove(attrName);
            }
        }

        String unknown = getUnknownAttrs(schComplexAttrs);
        if (unknown != null)
            throw new Exception("Unknown complex attribute found from schema: " + unknown + ". Please add it into the data dictionary or remove it from schema!<br>");

        return paramsVector;
    }
/*
    returns the vector with subelements  (Parameter)
*/
    private Vector getSubElements() throws Exception{

        Hashtable content;
        Hashtable childContent;
        Vector paramsVector = new Vector();
        Vector subElems = handler.getSubElems();
        Vector childElems;
        String content_type = handler.getContentType();

        if (content_type==null) return paramsVector;

        for (int i=0; i< subElems.size(); i++){
            content = (Hashtable)subElems.get(i);
            if (content.get("type").equals(content_type) && content_type.equals("chc"))
                throw new Exception("Choice can not contain choice!");
            if (content.get("type").equals(content_type) && content_type.equals("seq"))
                throw new Exception("Sequence can not contain sequence!");

            if (content.get("type").equals("elm")){
                paramsVector.add(getSubElemParams(content, "elm", content_type, "elm", Integer.toString(i)));
            }
            else{
                paramsVector.add(getSubElemParams(content, "elm", content_type, (String)content.get("type"), Integer.toString(i)));
                //get child content elements
                childElems = (Vector)content.get("elems");
                for (int j=0; j< childElems.size(); j++){
                    childContent = (Hashtable)childElems.get(j);
                    if (childContent.get("type").equals("elm")){
                        paramsVector.add(getSubElemParams(childContent, content_type, (String)content.get("type"), "elm", Integer.toString(j)));
                    }
                }
            }
        }


        return paramsVector;
    }
/*
    creates the parameters for one subelement
*/
    private Parameters getSubElemParams(Hashtable subElem, String parent_type, String content_type, String child_type, String pos) throws Exception{

        String ns_prefix;
        String delem_ref;
        String idfier;
        String delem_id;
        Namespace ns;
        Parameters paramsFields = new Parameters();

        if (content_type.equals("elm"))
            if (!subElem.containsKey("ref"))throw new Exception("Subelement ref attribute is missing: " + subElem.toString());

        paramsFields.addParameterValue("mode", "add");
        paramsFields.addParameterValue("parent_type", parent_type);
        paramsFields.addParameterValue("content_type", content_type);
        paramsFields.addParameterValue("childType", child_type);

        if(child_type.equals("elm")){
            delem_ref = (String)subElem.get("ref");

            if (delem_ref.indexOf(":")==-1)throw new Exception("Bad format of subelement reference: " + subElem.toString());

            ns_prefix = delem_ref.substring(0, delem_ref.indexOf(":"));
			idfier = delem_ref.substring(delem_ref.indexOf(":") + 1);
            delem_id= searchEngine.getDataElementID(getNamespaceID(null, ns_prefix), idfier);

            if (delem_id == null || delem_id.length()==0)
                throw new Exception("Unknown subelement reference: " + delem_ref + "!<br>"); 

            paramsFields.addParameterValue("child", delem_id); //subelement id
        }


        if (content_type.equals("seq")){
            if (subElem.containsKey("minOccurs"))
                paramsFields.addParameterValue("min_occ", (String)subElem.get("minOccurs"));
            else
                paramsFields.addParameterValue("min_occ", "0");
            if (subElem.containsKey("maxOccurs"))
                paramsFields.addParameterValue("max_occ", (String)subElem.get("maxOccurs"));
            else
                paramsFields.addParameterValue("max_occ", "1");

            paramsFields.addParameterValue("position", pos);
        }

        return paramsFields;
    }
/*
    creates the parameters for one row
*/
    private Parameters getRowParams(Hashtable schFields, String attr_id, String attr_name, String pos) throws Exception{
        String fieldName;
        Hashtable field;
        Vector attrFields;
        Parameters paramsFields = new Parameters();

        paramsFields.addParameterValue("mode", "add");
        paramsFields.addParameterValue("parent_type", "E");
        paramsFields.addParameterValue("attr_id", attr_id);

        attrFields = searchEngine.getAttrFields(attr_id);
        for (int j=0; j< attrFields.size(); j++){
            field =  (Hashtable)attrFields.get(j);
            fieldName = (String)field.get("name");
            if (schFields.containsKey(fieldName)){
                paramsFields.addParameterValue(AttrFieldsHandler.FLD_PREFIX + (String)field.get("id"), (String)schFields.get(fieldName));
                paramsFields.addParameterValue("position", pos);
            }
            else{
                throw new Exception("Missing field - " + fieldName + " - inside the complex attribute - " + attr_name);
            }
        }
        return paramsFields;
    }

/*
    Check, if all mandatory simple attributes exists
    Check, if there no unwanted attributes in the schema
    If OK, returns Hashtable with attribute id and value
*/
    private void getSimpleAttributes() throws Exception{

        String attrName;
        String attrShortName;
        String attrValue;
        //Hashtable h = new Hashtable();
        Namespace ns;
        boolean hasAttr;

        Vector simpleDelemAttr = searchEngine.getDElemAttributes(DElemAttribute.TYPE_SIMPLE);
        Hashtable schSimpleAttrs = handler.getSimpleAttributes();

        for (int i=0; i< simpleDelemAttr.size(); i++){
            DElemAttribute delemAttr = (DElemAttribute)simpleDelemAttr.get(i);
            if (delemAttr.displayFor(type)){
                attrShortName = delemAttr.getShortName();
                ns = delemAttr.getNamespace();
                attrName = ns.getShortName() + ":" + attrShortName;
//find mandatory attributes
                if (delemAttr.getObligation().equals("M")){
                    if (schSimpleAttrs.containsKey(attrName)){
                        attrValue = (String)schSimpleAttrs.get(attrName);
                        if (attrValue == null || attrValue.length()==0){
                            throw new Exception("Could not find mandatory attribute (" + attrName + ") value from specified schema for data element " + delem_name + "!<br>");
                        }
                        else{
                            //delemAttr.setValue(attrValue);
                             //h.put(attrPrefix + attrName, attrValue);
                            params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                            schSimpleAttrs.remove(attrName);
                        }
                    }
                    else{
                        throw new Exception("Could not find mandatory attribute (" + attrName + ") from specified schema for data element " + delem_name + "!<br>");
                    }
                }
//find other attributes
                else{
                    if (schSimpleAttrs.containsKey(attrName)){
                        attrValue = (String)schSimpleAttrs.get(attrName);
                        if (attrValue != null){
                            //delemAttr.setValue(attrValue);
                            params.addParameterValue(DataElementHandler.ATTR_PREFIX + delemAttr.getID(), attrValue);
                            //h.put(attrPrefix + attrName, attrValue);
                            schSimpleAttrs.remove(attrName);
                        }
                    }
                }
            }
        }

        String unknown = getUnknownAttrs(schSimpleAttrs);
        if (unknown != null)
            throw new Exception("Unknown simple attribute found from schema: " + unknown + ". Please add it into the data dictionary or remove it from schema!<br>");

        return ;
    }
/*
    check if there are any undefined attributes in the hashtable
*/
    private String getUnknownAttrs(Hashtable hash){
        String attrName = null;
        Enumeration schKeys = hash.keys();
        while (schKeys.hasMoreElements()){
            attrName = (String)schKeys.nextElement();
        }
        return attrName;
    }

    private String getNamespaceID(String url, String shortName) throws Exception{

        if (dbNamespaces == null)
            dbNamespaces = searchEngine.getNamespaces();  //get namespace definitions from database
        if (schNamespaces == null)
            schNamespaces = handler.getNamespaces();    //get namespace definitions from schema

        if (url == null && shortName != null){
            if(schNamespaces.containsKey(shortName))
                url = (String)schNamespaces.get(shortName);
            else
                throw new Exception("Namespace prefix " + shortName + " is not defined!");
        }

        /* JH290503 - trying to get rid of baseUrl usage, not sure if it's gonna work        
        if (url!=null){
            if (url.startsWith(baseUrl)){
              url=url.substring(baseUrl.length());
            }
        }
        */

        for (int i=0; i< dbNamespaces.size(); i++){
            Namespace ns = (Namespace)dbNamespaces.get(i);
            if (ns.getUrl().equals(url)) return ns.getID();
        }
        return null;
    }
    public String getResponseText(){
        return responseText.toString();
    }

    // JH290503 - trying to get rid of basensPath
    /*
    public void setBaseUrl(String url){
        baseUrl=url;
    }*/

}
