
// Copyright (c) 2000 TietoEnator
package eionet.meta.imp;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.*;

/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class DatasetImportHandler extends BaseHandler{

    public static String ROWSET = "RowSet";
    public static String ROW = "Row";

    private StringBuffer fieldData = new StringBuffer(); // buffer for collecting characters

    private Hashtable tables;
    private Vector table;
    private Hashtable row;

    private boolean bOK=false;
    private boolean bTableStart=false;
    private String tableName;
  /**
   * Constructor
   */
  public DatasetImportHandler() {
      super();

      tables = new Hashtable();
      table = new Vector();
      row = new Hashtable();
  }

    public void startElement(String uri, String localName, String name, Attributes attributes){

      if (bTableStart==true){   //start of table
          bTableStart=false;
          tableName=name;
      }
      if (name.equals(ROWSET)){

          bTableStart=true;
          table = new Vector();
      }
      if (name.equals(ROW)){
          row = new Hashtable();
          bOK = true;
      }
    }

    public void characters(char[] ch,int start,int len){
		  if (bOK==true){
        fieldData.append(ch, start, len);
      }
    }

    public void endElement(String uri, String localName, String name){
      if (name.equals(ROWSET)){  //end of table
          tables.put(tableName, table);
      }

      if (name.equals(ROW)){
          table.add(row);
          bOK = false;
      }
      else{
        if (bOK==true){
            row.put(name.toLowerCase(), fieldData.toString().trim());
        }
      }
      fieldData.setLength(0);
    }
    public Hashtable getTables(){
        return tables;
    }
    public static void main(String[] args){
      StringBuffer errorBuff = new StringBuffer();
      String srcFile = "F:\\Projects\\DD\\tmp\\importtables.xml";

      try{
        DatasetImportHandler handler=new DatasetImportHandler();
        SAXParserFactory spfact = SAXParserFactory.newInstance();
        SAXParser parser = spfact.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        spfact.setValidating(true);

       reader.setContentHandler(handler);
       reader.parse(srcFile);
       if (handler.hasError())
          System.out.println(handler.getErrorBuff().toString());

        String tblName;
        Hashtable row;

       Hashtable tbls = handler.getTables();
       Enumeration tblsKeys = tbls.keys();
       while (tblsKeys.hasMoreElements()){
          tblName = (String)tblsKeys.nextElement();
          Vector tbl = (Vector)tbls.get(tblName);
          for (int i=0; i<tbl.size(); i++){
              row = (Hashtable)tbl.get(i);
              System.out.println(tblName + ": " + row.toString());
          }
       }
      }
      catch (Exception e){
          System.out.println(e.toString());
      }
    }
}
