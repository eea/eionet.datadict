package eionet.meta.imp;

import org.xml.sax.*;
import javax.xml.parsers.*;

import java.util.Hashtable;
import java.util.Vector;

public class SchemaHandler extends BaseHandler{

    private StringBuffer fieldData = new StringBuffer(); // buffer for collecting characters
    private Hashtable tagMap;   //Hashtable for specifing tag names

    private static final String MAIN_NS = "xs";     //main namespace
    private static final String ATTR_ISO_DATATYPE = "iso:Datatype";
    private static final String ATTR_ISO_MINSIZE = "iso:MinSize";
    private static final String ATTR_ISO_MAXSIZE = "iso:MaxSize";

    private boolean bOK=false;

// helper parameters for finding attributes
    private boolean bAttributes=false;
    private boolean bSubElems=false;
    private boolean bSubElemsChild=false;
    private int attrLevel=0;
    private String type;
    private String attrTagName=null;
    private Hashtable attrFields;
    private Hashtable importAttrs;

    private String schemaAttr;
    private String ns;
    private String shortName;

// parameters for storing data, found from schema
    private Hashtable namespaces;
    private Hashtable simpleAttributes;
    private Hashtable complexAttributes;
    private Vector fixedValues;
    private Vector imports;
    private String contentType = null;
 //   private Locator locator = null;
    private Vector subElements;
    private Vector subElementsChild;

    public SchemaHandler()
    {
      super();
      simpleAttributes = new Hashtable();
      complexAttributes = new Hashtable();
      namespaces = new Hashtable();
      fixedValues = new Vector();
      imports = new Vector();
      subElements = new Vector();
      subElementsChild = new Vector();

      tagMap = new Hashtable();
      tagMap.put("schema", MAIN_NS + ":schema");
      tagMap.put("element", MAIN_NS + ":element");
      tagMap.put("restriction", MAIN_NS + ":restriction");
      tagMap.put("minLength", MAIN_NS + ":minLength");
      tagMap.put("maxLength", MAIN_NS + ":maxLength");
      tagMap.put("enumeration", MAIN_NS + ":enumeration");
      tagMap.put("simpleType", MAIN_NS + ":simpleType");
      tagMap.put("complexType", MAIN_NS + ":complexType");
      tagMap.put("sequence", MAIN_NS + ":sequence");
      tagMap.put("choice", MAIN_NS + ":choice");
      tagMap.put("documentation", MAIN_NS + ":documentation");
      tagMap.put("import", MAIN_NS + ":import");
      tagMap.put("sequence", MAIN_NS + ":sequence");
      tagMap.put("choice", MAIN_NS + ":choice");
    }


    public void startElement(String uri, String localName, String name, Attributes attributes){


      if (tagMap.containsValue(name)){
          if(name.equals((String)tagMap.get("schema"))){  //get namespaces from schema tag
              addNamespaces(attributes);
      		}
          else if(name.equals((String)tagMap.get("import"))){    //get import schemas (namespace, schemaLocation)
              addImport(attributes);
          }
          else if(name.equals((String)tagMap.get("element"))){
              if(bSubElems) addSubElems(putAttrToHash(attributes), "elm");
              else shortName =attributes.getValue("name");   //get short name and identifier from element tag
          }
          else if(name.equals((String)tagMap.get("restriction"))){
            String base = attributes.getValue("base");
            if (base.startsWith(MAIN_NS + ":"))
              base = base.substring(3);
            simpleAttributes.put(ATTR_ISO_DATATYPE,base);
          }
          else if(name.equals((String)tagMap.get("minLength"))){
            simpleAttributes.put(ATTR_ISO_MINSIZE,attributes.getValue("value"));
          }
          else if(name.equals((String)tagMap.get("maxLength"))){
            simpleAttributes.put(ATTR_ISO_MAXSIZE,attributes.getValue("value"));
          }
          else if(name.equals((String)tagMap.get("enumeration"))){    //get fixed values from enumeration tag
            fixedValues.add(attributes.getValue("value"));
          }
          else if(name.equals((String)tagMap.get("documentation"))){ // documentation tag includes all the attributes
              bAttributes = true;
          }
          else if(name.equals((String)tagMap.get("complexType"))){ // start of the subelements
              bSubElems = true;
          }
          else if(name.equals((String)tagMap.get("sequence"))){ // start of the sequence
              startSubElements(attributes, "seq");
          }
          else if(name.equals((String)tagMap.get("choice"))){ // start of the choice
              startSubElements(attributes, "chc");
          }
  		}
      else{        //get attributes data
          if (bAttributes){
              if (attrLevel==0){
                  attrLevel++;
                  attrTagName = name;
              }
              else if (attrLevel==1){ // It's complex attribute
                  attrFields = new Hashtable();
                  attrLevel++;
              }
              bOK=true;
          }
      }
      if (name.equals((String)tagMap.get("schema")) == false){
          hasNamespace(name);
      }
    }

    public void characters(char[] ch,int start,int len){
		  if (bOK==true){
        fieldData.append(ch, start, len);
      }
    }

    public void endElement(String uri, String localName, String name){
      if(name.equals((String)tagMap.get("documentation"))){
        bAttributes = false;
      }
      else if(name.equals((String)tagMap.get("complexType"))){
        bSubElems = false;
      }
      else if(name.equals((String)tagMap.get("sequence"))){ // end of the sequence
          endSubElements();
      }
      else if(name.equals((String)tagMap.get("choice"))){ // end of the choice
          endSubElements();
      }
      if (bAttributes){
          if (attrLevel==1 && attrTagName.equals(name)){   //end of simple attribute
              simpleAttributes.put(name, fieldData.toString().trim());
              attrLevel=0;
          }
          else if(attrLevel>1 && attrTagName.equals(name)){   //end of complex attribute
              addComplexAttrRow(name, attrFields);    //add complex attribute row
              attrLevel=0;
          }
          else if(attrLevel==2){    // end of complex attribute field
              attrFields.put(name, fieldData.toString().trim()); // add complex attribute fieldname and value into fields Hashtable
          }
      }
      fieldData.setLength(0);
      bOK=false;
    }


    private void addComplexAttrRow(String attrName, Hashtable row){
        Vector attrRows;
        if (complexAttributes.containsKey(attrName)){
            attrRows = (Vector)complexAttributes.get(attrName);
            attrRows.add(row);
            complexAttributes.put(attrName, attrRows);
        }
        else{   //add new attribute into complex attributes hashtable
            attrRows = new Vector();
            attrRows.add(row);
            complexAttributes.put(attrName, attrRows);
        }

    }
    private void hasNamespace(String tagName){
        int i = tagName.indexOf(":");
        if (i>0){
            String ns = tagName.substring(0, i);
            if (!namespaces.containsKey(ns)){
              setError("Namespace is not defined: " + tagName + "<br>");
            }
        }
        return;
    }
    private void addImport(Attributes attr){
        Hashtable imp = new Hashtable();

        if (attr.getValue("namespace")!=null &&
                attr.getValue("schemaLocation")!=null){
            imp.put("namespace", attr.getValue("namespace"));
            imp.put("schemaLocation", attr.getValue("schemaLocation"));
            imports.add(imp);
        }
        else{
            setError("Import tag does not have namespace or schemaLocation attribute!<br>");
        }
    }
    private void addNamespaces(Attributes attr){
        ns =attr.getValue("targetNamespace");
        for (int i=0; i < attr.getLength(); i++){
            schemaAttr = attr.getQName(i);
            if (schemaAttr != null && schemaAttr.length()>6){
                if (schemaAttr.startsWith("xmlns:")){
                    namespaces.put(attr.getQName(i).substring(6), attr.getValue(i));
                }
            }
        }
    }
    private void startSubElements(Attributes attr, String type){
        if(contentType==null)
            contentType=type;
        else{
            addSubElems(putAttrToHash(attr), type);
            bSubElemsChild=true;
        }
    }
    private void endSubElements(){
        if (bSubElemsChild){
            Hashtable childHash=(Hashtable)subElements.lastElement();
            childHash.put("elems", subElementsChild.clone());  //add child elements into subElements Vector
            subElementsChild.clear();
            bSubElemsChild=false;
        }
        else
          bSubElems=false;
    }
    private void addSubElems(Hashtable hash, String type){

        hash.put("type", type);

        if (bSubElemsChild) subElementsChild.add(hash);
        else subElements.add(hash);
    }
    private Hashtable putAttrToHash(Attributes attr){
        Hashtable hash = new Hashtable();
        for (int i=0; i < attr.getLength(); i++){
            hash.put(attr.getQName(i), attr.getValue(i));
        }
        return hash;
    }
/*
    Public functions for getting data outside from class
*/
    public String getNamespace(){
        return ns;
    }
    public String getShortName(){
        return shortName;
    }
    public Hashtable getNamespaces(){
        return namespaces;
    }
    public Hashtable getSimpleAttributes(){
        return simpleAttributes;
    }
    public Hashtable getComplexAttributes(){
        return complexAttributes;
    }
    public Vector getFixedValues(){
        return fixedValues;
    }
    public Vector getImports(){
        return imports;
    }
    public Vector getSubElems(){
        return subElements;
    }
    public String getContentType(){
        return contentType;
    }

    public static void main(String[] args){
      StringBuffer errorBuff = new StringBuffer();
	  String srcFile = "F:\\Projects\\DD\\tmp\\CountryCode.xsd";  //Fixed values

      try{
        SchemaHandler handler=new SchemaHandler();
     //   XMLReader reader =  SAXParserFactory.newInstance().newSAXParser().getXMLReader();
      SAXParserFactory spfact = SAXParserFactory.newInstance();
      SAXParser parser = spfact.newSAXParser();
      XMLReader reader = parser.getXMLReader();
        spfact.setValidating(true);

       reader.setContentHandler(handler);
       reader.parse(srcFile);
       if (handler.hasError())
          System.out.println(handler.getErrorBuff().toString());

       System.out.println(handler.getSimpleAttributes().toString());
/*       Hashtable fields;
       String attrName;
       Hashtable hash =handler.getComplexAttributes();
       Enumeration attrKeys = hash.keys();
       while (attrKeys.hasMoreElements()){
          attrName = (String)attrKeys.nextElement();
          Vector rows = (Vector)hash.get(attrName);
          for (int i=0; i<rows.size(); i++){
              fields = (Hashtable)rows.get(i);
              System.out.println(attrName + ": " + fields.toString());
          }
       }
*/
/*        Vector imports = handler.getImports();
        Hashtable h;
        for (int i=0; i<imports.size(); i++){
              h = (Hashtable)imports.get(i);
              System.out.println("ns: " + h.get("namespace"));
              System.out.println("sch: " + h.get("schemaLocation"));
        }
*/
        Vector sub = handler.getSubElems();
        Hashtable h;
        for (int i=0; i<sub.size(); i++){
              h = (Hashtable)sub.get(i);
              System.out.println(i + ": " + h.toString());
        }

    //   System.out.println(handler.getFixedValues().toString());
    //   System.out.println(handler.getNamespaces().toString());
      }
      catch (Exception e){
          System.out.println(e.toString());
      }
    }
}
