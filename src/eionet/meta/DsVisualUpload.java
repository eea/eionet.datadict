package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.net.*;
import java.sql.*;

import com.tee.util.Util;

import eionet.meta.savers.*;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.sql.ConnectionUtil;

public class DsVisualUpload extends HttpServlet {

    private static final int BUF_SIZE = 1024;
    private static final String WEB_ROOT = "x:\\temp\\";
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

       doPost(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

		req.setCharacterEncoding("UTF-8");
						
        ServletContext ctx = getServletContext();
		Connection conn = null;
        
        // authenticate user
		DDUser user = SecurityUtil.getUser(req);

        if (user == null)
            throw new ServletException("User not authenticated!");

        String dsID = req.getParameter("ds_id");
        if (Util.nullString(dsID))
            throw new ServletException("Dataset ID is not specified!");
            
        String strType = req.getParameter("str_type");
        if (Util.nullString(strType))
            throw new ServletException("Structure type not specified!");
            
        String dsVisual = req.getParameter("visual");

        // get the file's physical path        
        String filePath = Props.getProperty(PropsIF.VISUALS_PATH);
        if (filePath == null)
            filePath = System.getProperty("user.dir");
                    
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
                
        // check if 'remove' mode and if so, just set DATASET.VISUAL=NULL
        String mode = req.getParameter("mode");
        if (mode != null && mode.equals("remove")){
            
            eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
            pars.addParameterValue("mode", "edit");
            pars.addParameterValue("ds_id", dsID);
            pars.addParameterValue("visual", "NULL");
            pars.addParameterValue("str_type", strType);
            
            try{
                conn = ConnectionUtil.getConnection();
                
                DatasetHandler dsHandler = new DatasetHandler(conn, pars, ctx);
                dsHandler.execute();
            }
            catch (Exception e){
                throw new ServletException(e.toString());
            }
            finally{
            	try{
            		if (conn!=null) conn.close();
            	}
            	catch(SQLException e){}
            }
            
            if (!Util.nullString(dsVisual)){
                File file = new File(filePath + dsVisual);
                if (file.exists())
                    file.delete();
            }
            
            //res.sendRedirect(req.getContextPath() + "dsvisual.jsp?ds_id=" + dsID + "&str_type=" + strType);
            res.sendRedirect("dsvisual.jsp?ds_id=" + dsID + "&str_type=" + strType);
            return;
        }
        
        // check the request content type
        String contentType = req.getContentType();
        if (contentType == null || !(contentType.toLowerCase().startsWith("multipart/form-data")))
            throw new ServletException("Posted content type is unknown!");
        
        String boundary = extractBoundary(contentType);        

        // set the response content type
        res.setContentType("text/html");

        // set the name of the file or url to get the stream from 
        String sUrl = null;
        String sFile = null;

        String fileORurl = req.getParameter("fileORurl");
        if (fileORurl != null && fileORurl.equalsIgnoreCase("url")){
            sUrl = req.getParameter("url_input");
        }
        else if (fileORurl != null && fileORurl.equalsIgnoreCase("file")){
            sFile = req.getParameter("file_input");
        }
        
        if (Util.nullString(sFile) && Util.nullString(sUrl)){
            throw new ServletException("You have to specify at least a file or url!");
        }
        
        try{
            // set up the file to write to
            
            // First the new file is saved with a temporary name, to see if it
            // goes OK at all. If goes then later the old file will be deleted
            // and the new renamed from temporary name to official one.
            
            // build the new file's official name in advance
            StringBuffer buf = new StringBuffer(filePath);
            if (sUrl != null){
                int i = sUrl.lastIndexOf("/");
                //if (i==-1) i = 0;
                buf.append(sUrl.substring(i+1, sUrl.length()));
            }
            else{
                int i = sFile.lastIndexOf("\\");
                if (i==-1) i = sFile.lastIndexOf("/");
                //if (i==-1) i = 0;
                buf.append(sFile.substring(i+1, sFile.length()));
            }
            
            String newFileName = buf.toString();
            
            // build the new file's temporary name
            buf = new StringBuffer(filePath);
            if (!Util.nullString(dsID))
                buf.append(dsID);
            buf.append(System.currentTimeMillis());
            
            String tmpFileName = buf.toString();
                
            File file = new File(tmpFileName);
            RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        
            // set up the stream to read from and call file writer
            
            InputStream in = null;
            if (sUrl!=null){
                URL url = new URL(sUrl);
                writeToFile(raFile, url.openStream());
            }
            else
                writeToFile(raFile, req.getInputStream(), boundary);
            
            // seems we've managed to save the new file without any problems,
            // so check if there was an old file and if so, delete it and
            // rename the new one from temporary name to the official one.
            
            if (!Util.nullString(dsVisual)){
                ctx.log("got in dsVisual!");
                buf = new StringBuffer(filePath);
                buf.append(dsVisual);
                file = new File(buf.toString());
                if (file.exists())
                    file.delete();
            }
            
            ctx.log("going to rename");
            
            File tmpFile = new File(tmpFileName);
            File newFile = new File(newFileName);
            tmpFile.renameTo(newFile);
            
            ctx.log("all renamed");
            
            // We seem to have successfully replaced the old file with new one.
            // Now let's store the new file name into DATASET.VISUAL where DATASET_ID=<dsID>
            eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
            pars.addParameterValue("mode", "edit");
            pars.addParameterValue("ds_id", dsID);
            pars.addParameterValue("str_type", strType);
            
            int i = newFileName.lastIndexOf(File.separator);
            //if (i == -1) i = 0;
            pars.addParameterValue("visual", newFileName.substring(i+1, newFileName.length()));
            
            conn = ConnectionUtil.getConnection();
            DatasetHandler dsHandler = new DatasetHandler(conn, pars, ctx);
            dsHandler.execute();
        }
        catch (Exception e){
            throw new ServletException(e.toString());
        }
        finally{
        	try{
        		if (conn!=null) conn.close();
        	}
        	catch (SQLException e){}
        }
        
        if (Util.nullString(dsID))
            //res.sendRedirect(req.getContextPath() + "/index.jsp");
            res.sendRedirect("index.jsp");
        else
            //res.sendRedirect(req.getContextPath() +
            //                "/dsvisual.jsp?ds_id=" + dsID + "&str_type=" + strType);
            res.sendRedirect("dsvisual.jsp?ds_id=" + dsID + "&str_type=" + strType);
    }

    private void writeToFile(RandomAccessFile raFile, InputStream in) throws Exception{
        
        byte[] buf = new byte[BUF_SIZE];
        int i;
        while ((i=in.read(buf, 0, buf.length)) != -1){
            raFile.write(buf, 0, i);
        }
            
        raFile.close();
        in.close();
    }
    
    private void writeToFile(RandomAccessFile raFile, ServletInputStream in, String boundary) throws Exception{
        
        byte[] buf = new byte[BUF_SIZE];
        int i;
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        boolean fileStart = false;
        boolean pastContentType = false;
        do{
            int b = in.read();
            if (b == -1) break; // if end of stream, break
            
            bout.write(b);
            
            if (!pastContentType){ // if Content-Type not passed, no check of LNF
                String s = bout.toString();
                if (s.indexOf("Content-Type") != -1)
                    pastContentType = true;
            }
            else{
                // Content-Type is passed, after next double LNF is file start
                byte[] bs = bout.toByteArray();
                if (bs != null && bs.length >= 4){
                    if (bs[bs.length-1]==10 &&
                        bs[bs.length-2]==13 &&
                        bs[bs.length-3]==10 &&
                        bs[bs.length-4]==13){
                        
                        fileStart = true;
                    }
                }
            }
        }
        while(!fileStart);
        
        while ((i=in.readLine(buf, 0, buf.length)) != -1){
            String line = new String(buf, 0, i);
            if (boundary != null && line.startsWith(boundary))
                break;
            raFile.write(buf, 0, i);
        }
            
        raFile.close();
        in.close();
    }
    
    private String extractBoundary(String contentType){
        int i = contentType.indexOf("boundary=");
        if (i == -1) return null;
        String boundary = contentType.substring(i + 9); // 9 for "boundary="
        return "--" + boundary; // the real boundary is always preceded by an extra "--"
    }
    
    public static void main(String[] args) {

    }
}
