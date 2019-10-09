
package eionet.meta.imp;

import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import eionet.util.UnicodeEscapes;

/**
 * @author Enriko Käsper
 */
public class DatasetImportHandler extends BaseHandler {

    /** */
    public static String ROWSET = "RowSet";
    public static String ROW = "Row";
    public static String IMPORT = "import";

    /** buffer for collecting characters. */
    private StringBuffer fieldData = new StringBuffer();

    /** */
    private Hashtable tables;
    private Vector table;
    private Hashtable row;

    /** */
    private boolean bOK = false;
    private boolean bTableStart = false;
    private String tableName;
    private String importType;

    /** */
    private UnicodeEscapes unicodeEscapes = new UnicodeEscapes();

    /**
     *
     */
    public DatasetImportHandler() {
        super();
        tables = new Hashtable();
        table = new Vector();
        row = new Hashtable();
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String name, Attributes attributes) {

          if (bTableStart == true) {   //start of table
              bTableStart = false;
              tableName = name;
          }

          if (name.equals(ROWSET)) {
              bTableStart = true;
              table = new Vector();
          }

          if (name.equals(ROW)) {
              row = new Hashtable();
              bOK = true;
          }

          if (name.equals(IMPORT))
              this.importType = attributes.getValue("name");
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int len) {
          if (bOK == true)
              fieldData.append(ch, start, len);
    }

    /*
     *  (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String name) {

      if (name.equals(ROWSET))  //end of table
          tables.put(tableName, table);

      if (name.equals(ROW)) {
          table.add(row);
          bOK = false;
      } else {
            if (bOK == true)
                row.put(name.toLowerCase(), processFieldData(fieldData.toString().trim()));
      }

      fieldData =  new StringBuffer();
    }

    /**
     *
     * @return
     */
    public Hashtable getTables() {
        return tables;
    }

    /**
     *
     * @return
     */
    public String getImportType() {
        return importType;
    }

    /**
     *
     * @param data
     * @return
     */
    private String processFieldData(String data) {

        return processUnicodeEscapes(data);
    }

    /**
     * Turns all UNICODE esacpes like &#000; and &nbsp; into correct UTF-8 chars.
     */
    private String processUnicodeEscapes(String data) {

        if (data == null || data.length() == 0) return data;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length(); i++) {

            char c = data.charAt(i);

            if (c == '&') {
                int j = data.indexOf(";", i);
                if (j > i) {
                    char cc = data.charAt(i + 1);
                    int decimal = -1;
                    if (cc == '#') {
                        // handle Unicode decimal escape
                        String sDecimal = data.substring(i + 2, j);
                        try {
                            decimal = Integer.parseInt(sDecimal);
                        } catch (Exception e) {}
                    } else {
                        // handle entity
                        String ent = data.substring(i + 1, j);
                        decimal = unicodeEscapes.getDecimal(ent);
                    }

                    // if decimal found, use the corresponding char, otherwise stick to c.
                    if (decimal >= 0) {
                        c = (char) decimal;
                        i = j;
                    }
                }
            }

            buf.append(c);
        }

        return buf.toString();
    }


    /**
     * Substitutes &apos; entities with &#39;, because &apos; is not supported in older
     * browsers and IE6.
     */
    private String processApos(String data) {

        String oldEntity = "&apos;";
        String newEntity = "&#39;";

        StringBuffer buf = null;
        int i = data.indexOf(oldEntity);
        while (i != -1) {
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
    public static void main(String[] args) {

        StringBuffer errorBuff = new StringBuffer();
        String srcFile = "D:\\projects\\datadict\\tmp\\valid_nuka_tbl.xml";

        try {
            DatasetImportHandler handler = new DatasetImportHandler();
            SAXParserFactory spfact = SAXParserFactory.newInstance();
            SAXParser parser = spfact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            spfact.setValidating(true);

            reader.setContentHandler(handler);
            reader.parse(srcFile);
            if (handler.hasError())
                System.out.println(handler.getErrorBuff().toString());

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
