
// Copyright (c) 2000 TietoEnator
package eionet.meta.imp;

import org.xml.sax.*;

//import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
/**
 * A Class class.
 * <P>
 * @author Enriko Käsper
 */
public class DatasetImportHandler extends BaseHandler{

    public static String ROWSET = "RowSet";
    public static String ROW = "Row";

	//	buffer for collecting characters
    private StringBuffer fieldData = new StringBuffer();                                                                              
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
      //this.ctx=ctx;
  }

    public void startElement(String uri, String localName, String name, Attributes attributes){
    	
		String lowName = name.toLowerCase();
		if (lowName.indexOf("methodology") != -1)
			System.out.println("entered methodology!");

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
    
	/**
	* This one converts the given string into UTF-8 bytes.
	* If the string is already UTF-8 encoded, its bytes are simply returned.
	*/
	private byte[] getUTF8Bytes(String literal) throws Exception {
        
		if (literal == null || literal.length()==0)
			return null;

		StringBuffer buf = new StringBuffer();
		for (int i=0; i<literal.length(); i++){
            
			char c = literal.charAt(i);
            
			if (c=='&'){
				int j = literal.indexOf(";", i);
				if (j > i){
					char cc = literal.charAt(i+1);
					int decimal = -1;
					if (cc=='#'){
						// handle Unicode decimal escape
						String sDecimal = literal.substring(i+2, j);
                        
						try{
							decimal = Integer.parseInt(sDecimal);
						}
						catch (Exception e){}
					}
					else{
						// handle entity
						String ent = literal.substring(i+1, j);
						decimal = 0;//unicodeEscapes.getDecimal(ent);
					}
                    
					if (decimal >= 0){
						// if decimal was found, use the corresponding char. otherwise stick to c.
						c = (char)decimal;
						i = j;
					}
				}
			}

			buf.append(c);
		}
        
		String unicodeLiteral = buf.toString();
		return unicodeLiteral.getBytes("UTF-8");
	}


    public void endElement(String uri, String localName, String name){
    	
		String lowName = name.toLowerCase();
		if (lowName.indexOf("methodology") != -1){
			String s = fieldData.toString();			
			try {
				byte[] bs = getUTF8Bytes(s);
				
				System.out.println("here we go!");
				
				for (int i=0; i<bs.length; i++){
					Byte bt = new Byte(bs[i]);
					int intVal = bt.intValue();            
					System.out.println(Integer.toHexString(intVal));
				}
			}
			catch (Exception e){
				System.out.println(e.toString());
			}
			System.out.println("end of definition!");
		}
					
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
      fieldData =  new StringBuffer();
    }
    public Hashtable getTables(){
        return tables;
    }
    public static void main(String[] args){
    	
      StringBuffer errorBuff = new StringBuffer();
      String srcFile = "X:\\Projects\\datadict\\import\\importtables.xml";

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
