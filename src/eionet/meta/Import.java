package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.caucho.sql.DBPool;
import eionet.meta.schema.*;
import eionet.meta.imp.*;

import java.net.*;
import java.util.*;
import java.lang.reflect.Constructor;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
//import org.apache.xerces.parsers.*;
//import org.apache.xerces.framework.*;

//import org.w3c.dom.*;
//import org.xml.sax.*;

public class Import extends HttpServlet {

    private static final int BUF_SIZE = 1024;
    private static final String START_XML_STRING = "<?xml";
    
    private static final String PAR_TEMP_FILE_PATH = "temp-file-path";
    private static final String TMP_FILE_PREFIX = "import_";

    private static PrintStream s = System.out;
//    private static XMLFactoryIF xmlFactory = new com.tee.xmlserver.apache.ApacheXMLFactory();

    private static boolean validation = false;
    private static String  drv = "";
    private static String  url = "";
    private static String  usr = "";
    private static String  psw = "";
    private static String  xmlDir = "";

    private static File[]  xmlFiles;

    private static Hashtable conf;
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

       req.getRequestDispatcher("import_results.jsp").forward(req, res);
        //doPost(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

        StringBuffer responseText = new StringBuffer();
        boolean bException = false;
        //Writer writer = res.getWriter();

  			DDuser user=getUser(req);

        if ( user == null){
            responseText.append("<h1>Not authorized to store data!</h1><br/>");
            bException = true;
         }

        String contentType = req.getContentType();

        if (contentType == null ||
            !(contentType.toLowerCase().startsWith("multipart/form-data") ||
            contentType.toLowerCase().startsWith("text/xml"))){
            responseText.append("<h1>Posted content type is unknown!</h1>");
            bException = true;
          	//throw new IOException("Posted content type is unknown!");
        }

        res.setContentType("text/html");

        String boundary = null;
        String sUrl = null;
        String type = null;

        if (contentType.toLowerCase().startsWith("multipart/form-data")){
            String fileORurl = req.getParameter("fileORurl");
            type = req.getParameter("type");

            if (fileORurl != null && fileORurl.equalsIgnoreCase("url")){
                sUrl = req.getParameter("url_input");
            }
            boundary = extractBoundary(contentType);
        }
        //writer.write("type:" + type);
        if (type == null){
            responseText.append("<h1>Data Dictionary importer could not get data elment type!</h1><br/>");
            bException = true;
            //writer.write("<html><body><h1>Data Dictionary importer could not get data elment type</h1><br/>"
             //   + "<br/><br/></body></html>");
            //return;
        }
        if (!bException){
          try{
            if (sUrl == null){
                
                ServletContext ctx = getServletContext();
                
                String tmpFilePath = ctx.getInitParameter(PAR_TEMP_FILE_PATH);
                if (tmpFilePath == null)
                    tmpFilePath = System.getProperty("user.dir");
                
                if (!tmpFilePath.endsWith(File.separator))
                    tmpFilePath = tmpFilePath + File.separator;
                
                StringBuffer tmpFileName = new StringBuffer(TMP_FILE_PREFIX);
                tmpFileName.append("_");
                tmpFileName.append(req.getRequestedSessionId().replace('-', '_'));
                tmpFileName.append("_");
                tmpFileName.append(System.currentTimeMillis());
                tmpFileName.append(".xml");
                
                PipedOutputStream pipeOut = new PipedOutputStream();
                PipedInputStream pipeIn = new PipedInputStream(pipeOut);
                WritingPipe writingPipe = new WritingPipe(pipeOut,
                                                          req.getInputStream(),
                                                          boundary,
                                                          tmpFilePath + tmpFileName);
                writingPipe.start();

                importContext(user, pipeIn, responseText, type);
            }
            else{
                URL url = new URL(sUrl);
                HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();

                importContext(user, httpConn.getInputStream(), responseText, type);
            }
          }
          catch (Exception e){
            responseText.append("<h1>Data Dictionary importer encountered an exception:</h1><br/>" +
                                    e.getMessage() + "<br/><br/>");
            //e.printStackTrace(new PrintWriter(writer));
           // return;
          }
        }

        //responseText.append("<h1>Data was successfully imported!</h1>");
        req.setAttribute("TEXT", responseText.toString());
        doGet(req, res);
    }
/*
    private void importMultipart(DDuser user,
                            ServletInputStream instream,
                            String boundary, StringBuffer responseText, String type) throws Exception {

      PipedOutputStream pipeOut = new PipedOutputStream();
      PipedInputStream pipeIn = new PipedInputStream(pipeOut);
      WritingPipe writingPipe = new WritingPipe(pipeOut, instream, boundary);
      writingPipe.start();

      importContext(user, pipeIn, responseText, type);
    }*/
    private void importContext(DDuser user, InputStream instream, StringBuffer responseText, String type) throws Exception {

      ServletContext ctx = getServletContext();

      String basensPath = ctx.getInitParameter("basens-path");
      if (basensPath == null){
	        throw new Exception("Could not get base namespace url path!");
      }
      BaseHandler handler = new BaseHandler();
      if (type.equals("DST")){
          handler = new DatasetImportHandler();
      }
      else{
          handler=new SchemaHandler();
      }
      SAXParserFactory spfact = SAXParserFactory.newInstance();
      SAXParser parser = spfact.newSAXParser();
      XMLReader reader = parser.getXMLReader();

      reader.setContentHandler(handler);

      try{
         reader.parse(new InputSource(instream));
         if (!handler.hasError()){
             if(type.equals("DST")){
                 DatasetImport dbImport = new DatasetImport((DatasetImportHandler)handler, user.getConnection(), ctx, basensPath, type);
                try{
                   dbImport.execute();
                }
                catch (Exception e){
                  responseText.append(e.toString());
                }
                responseText.append(dbImport.getResponseText());
             }
             else{
                 SchemaImp dbImport = new SchemaImp((SchemaHandler)handler, user.getConnection(), ctx, basensPath, type);
                 dbImport.execute();
                 responseText.append(dbImport.getResponseText());
             }
         }
         else{
             responseText.append("Import failed!<br>");
             responseText.append(handler.getErrorBuff());
         }
      }
      catch (Exception e){
          int lineNumber = handler.getLine();
          responseText.append("Import failed!<br>");
          responseText.append(e.toString());
          if (lineNumber>0)
              responseText.append(" Line " + String.valueOf(lineNumber));
          //throw new Exception(e.toString() + " Line " + String.valueOf(lineNumber));
        }
    }

    public static void main(String[] args) {

    }

    private String extractBoundary(String contentType){
        int i = contentType.indexOf("boundary=");
        if (i == -1) return null;
        String boundary = contentType.substring(i + 9); // 9 for "boundary="
        return "--" + boundary; // the real boundary is always preceded by an extra "--"
    }


    class WritingPipe extends Thread {

        private PipedOutputStream out = null;
        private ServletInputStream in = null;
        private String boundary = null;
        private String filePath = null;

        WritingPipe(PipedOutputStream out, ServletInputStream in, String boundary, String filePath){
            super();
            this.out = out;
            this.in = in;
            this.boundary = boundary;
            this.filePath = filePath;
        }
        
        public void run(){
            
            byte[] buf = new byte[BUF_SIZE];
            int i;
            boolean xmlStarted = false;
        
            try {
                
                File file = new File(filePath);
                RandomAccessFile raFile = new RandomAccessFile(file, "rw");
                
                while ((i=in.readLine(buf, 0, buf.length)) != -1){
                    
                    raFile.write(buf, 0, i);
                    
                    String line = new String(buf, 0, i);
                    if (!xmlStarted){
                        if (line.startsWith(START_XML_STRING)){
                            xmlStarted = true;
                            out.write(buf, 0, i);
                            out.flush();
                            //raFile.write(buf, 0, i);
                        }
                    }
                    else{
                        if (boundary != null && line.startsWith(boundary))
                            break;
                        out.write(buf, 0, i);
                        out.flush();
                        //raFile.write(buf, 0, i);
                    }
                }
            
                raFile.close();
                out.close();
                in.close();
                file.delete();
                stop();
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
  private DDuser getUser(HttpServletRequest req) {

  	DDuser user = null;

      HttpSession httpSession = req.getSession(false);
      if (httpSession != null) {
      	user = (DDuser)httpSession.getAttribute("DataDictionaryUser");
  	}

      if (user != null)
      	return user.isAuthentic() ? user : null;
  	else
      	return null;
  }
}
