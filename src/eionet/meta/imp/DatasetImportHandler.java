
// Copyright (c) 2000 TietoEnator
package eionet.meta.imp;

import org.xml.sax.*;
import java.util.*;
import javax.xml.parsers.*;

/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class DatasetImportHandler extends BaseHandler{

    public static String ROWSET = "RowSet";
    public static String ROW = "Row";
    public static String IMPORT = "import";

    private StringBuffer fieldData = new StringBuffer(); // buffer for collecting characters

    private Hashtable tables;
    private Vector table;
    private Hashtable row;

    private boolean bOK=false;
    private boolean bTableStart=false;
    private String tableName;
    private String importName;
  /**
   * Constructor
   */
  public DatasetImportHandler() {
      super();

      tables = new Hashtable();
      table = new Vector();
      row = new Hashtable();
      //this.ctx=ctx;
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
      if (name.equals(IMPORT)){
          this.importName = attributes.getValue("name");
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
            row.put(name.toLowerCase(), processFieldData(fieldData.toString().trim()));
        }
      }
      fieldData =  new StringBuffer();
    }
    public Hashtable getTables(){
        return tables;
    }
    public String getImportName(){
        return importName;
    }
    
    /**
     * 
     */
    private String processFieldData(String data){
    	
    	return processApos(data);
    }
    
    /**
     * Substitutes &apos; entities with &#39;, because &apos; is not supported in older
     * browsers and IE6 :(
     */
    private String processApos(String data){
    	
    	String oldEntity = "&apos;";
		String newEntity = "&#39;";
    	
		StringBuffer buf = null;
		int i = data.indexOf(oldEntity);
		while (i != -1){
			buf = new StringBuffer(data);
			buf.replace(i, i + oldEntity.length(), newEntity);
			data = buf.toString();
			i = data.indexOf(oldEntity);
		}

		return data;
    }
    
    /**
     * 
     */
    public static void main(String[] args){
      StringBuffer errorBuff = new StringBuffer();
      //String srcFile = "C:\\Documents and Settings\\jaanus\\Desktop\\import013_testMay.xml";
	  String srcFile = "D:\\projects\\datadict\\tmp\\valid_nuka_tbl.xml";

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

        /*String tblName;
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
       }*/
      }
      catch (Exception e){
          System.out.println(e.toString());
      }
    }
}
