package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.net.*;
import java.sql.*;

import com.tee.util.*;
import com.tee.xmlserver.*;

import eionet.util.SecurityUtil;

import com.eteks.awt.PJAToolkit;

public class ImgUpload extends HttpServlet {

    private static final int BUF_SIZE = 1024;
    private static final String PAR_WEB_ROOT = "visuals-path";
	private static final String QRYSTR_ATTR = "imgattr_qrystr";
    
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

		doPost(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, java.io.IOException {

		res.setContentType("text/html");
        ServletContext ctx = getServletContext();
		HttpSession session = req.getSession();
		String qryStr = (String)session.getAttribute(QRYSTR_ATTR);
		if (Util.nullString(qryStr))
			qryStr = "index.jsp";
		else
			session.removeAttribute(QRYSTR_ATTR);
        
        // authenticate user
        AppUserIF user = SecurityUtil.getUser(req);

        if (user == null)
            throw new ServletException("User not authenticated!");

        String objID = req.getParameter("obj_id");
        if (Util.nullString(objID))
            throw new ServletException("Object ID is not specified!");
            
        String objType = req.getParameter("obj_type");
        if (Util.nullString(objType))
            throw new ServletException("Object type not specified!");
        
		String attrID = req.getParameter("attr_id");
		if (Util.nullString(attrID))
			throw new ServletException("Attribute ID not specified!");

        String appName = ctx.getInitParameter("application-name");
        if (Util.nullString(appName))
            throw new ServletException("Application name in servlet conf is not specified!");
        
        // get the file's physical path
        String filePath = ctx.getInitParameter(PAR_WEB_ROOT);
                    
        if (filePath == null)
            filePath = System.getProperty("user.dir");
                    
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
                
        StringBuffer buf = new StringBuffer();
        
        // check mode
        String mode = req.getParameter("mode");
        
        // HANDLE REMOVE
        if (mode != null && mode.equals("remove")){
        	
			String[] fileNames = req.getParameterValues("file_name");
			if (fileNames==null || fileNames.length==0)
				throw new ServletException("No images selected!");
			
			buf.append("delete from ATTRIBUTE where M_ATTRIBUTE_ID=").
			append(attrID).append(" and DATAELEM_ID=").append(objID).
			append(" and PARENT_TYPE=").append(Util.strLiteral(objType)).
			append(" and VALUE=");
						
			String s = buf.toString();
						         
            try{
                // getting the DB pool through XmlServer
                XDBApplication xdbapp =
                			XDBApplication.getInstance(getServletContext());
                DBPoolIF pool = XDBApplication.getDBPool();            
                Connection conn = pool.getConnection();
                Statement stmt = conn.createStatement();
                
                for (int i=0; i<fileNames.length; i++)
                	stmt.executeUpdate(s + Util.strLiteral(fileNames[i]));
                
                conn.close();
            }
            catch (Exception e){
                throw new ServletException(e.toString());
            }
            
			for (int i=0; i<fileNames.length; i++){
	            if (!Util.nullString(fileNames[i])){
	                File file = new File(filePath + fileNames[i]);
	                if (file.exists())
	                    file.delete();
	            }
			}

			res.sendRedirect("imgattr.jsp?" + qryStr);
            
            return;
        }
        
        // READY TO UPLOAD THE FILE
        
        // check the request content type
        String contentType = req.getContentType();
        if (contentType == null ||
        	!(contentType.toLowerCase().startsWith("multipart/form-data")))
            throw new ServletException("Posted content type is unknown!");
        
        String boundary = extractBoundary(contentType);        

        // get the image file name
        
        String sUrl = null;
        String sFile = null;

        String fileORurl = req.getParameter("fileORurl");
        if (fileORurl != null && fileORurl.equalsIgnoreCase("url")){
            sUrl = req.getParameter("url_input");
        }
        else if (fileORurl != null && fileORurl.equalsIgnoreCase("file")){
            sFile = req.getParameter("file_input");
        }
        
        if (Util.nullString(sFile) && Util.nullString(sUrl)) throw new
            ServletException("You have to specify at least a file or url!");
        
        String fileName = null;
		if (sUrl != null){
			int i = sUrl.lastIndexOf("/");
			fileName = sUrl.substring(i+1, sUrl.length());
		}
		else{
			int i = sFile.lastIndexOf("\\");
			if (i==-1) i = sFile.lastIndexOf("/");
			fileName = sFile.substring(i+1, sFile.length());
		}
		
		if (Util.nullString(fileName)) throw new
			ServletException("Failed to extract the file name!");
        
        try{
            // set up the file to write to
            
            File file = new File(filePath + fileName);
            if (file.exists()){            	
            	String msg = "A file with such a name already " +
								"exists! Choose another name.";
				req.setAttribute("DD_ERR_MSG", msg);
				req.setAttribute("DD_ERR_BACK_LINK", "imgattr.jsp?" + qryStr);
				req.getRequestDispatcher("error.jsp").forward(req, res);
            }
            
            RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        
            // set up the stream to read from and call file writer
            
            InputStream in = null;
            if (sUrl!=null){
                URL url = new URL(sUrl);
                writeToFile(raFile, url.openStream());
            }
            else
                writeToFile(raFile, req.getInputStream(), boundary);
            
            // We seem to have successfully uploaded the file.
            // However, we must be srue that PJAToolkit can handle
            // the file later when inserting into generated PDF
			try{
				checkPJA(filePath + fileName);
            }
            catch (Exception e){
				req.setAttribute("DD_ERR_MSG", e.getMessage());
				req.setAttribute("DD_ERR_BACK_LINK", "imgattr.jsp?" + qryStr);
				req.getRequestDispatcher("error.jsp").forward(req, res);
				return;
            }
                        
            // Now let's store the image relation in the DB as well.
            
            // getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            Connection conn = pool.getConnection();
            Statement stmt = conn.createStatement();         
            
			SQLGenerator gen = new SQLGenerator();
			gen.setTable("ATTRIBUTE");
			gen.setField("M_ATTRIBUTE_ID", attrID);
			gen.setField("DATAELEM_ID", objID);
			gen.setField("PARENT_TYPE", objType);
			gen.setField("VALUE", fileName);
			
            stmt.executeUpdate(gen.insertStatement());
            conn.close();
        }
        catch (Exception e){
            throw new ServletException(e.toString());
        }
        
		res.sendRedirect("imgattr.jsp?" + qryStr);
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
    
    public static void checkPJA(String filePath) throws Exception{
		// get old properties
		String propToolkit = System.getProperty("awt.toolkit");
		String propGraphics = System.getProperty("java.awt.graphicsenv");
		String propFonts = System.getProperty("java.awt.fonts");
        
		// set new properties
		System.setProperty ("awt.toolkit", "com.eteks.awt.PJAToolkit");
		System.setProperty ("java.awt.graphicsenv", "com.eteks.java2d.PJAGraphicsEnvironment");
		System.setProperty ("java.awt.fonts", System.getProperty("user.dir"));
		
		java.awt.Image jImg = null;
		try{
			PJAToolkit kit = new PJAToolkit();
			// create java.awt.Image
			jImg = kit.createImage(filePath);
			if (jImg==null) throw new Exception(); 
			// of the java.awt.Image, create com.lowagie.text.Image
			com.lowagie.text.Image vsImage =
				com.lowagie.text.Image.getInstance(jImg, null);
			if (vsImage==null) throw new Exception();
		}
		catch (Exception e){
			
			// reset old properties
			if (propToolkit != null)
				System.setProperty ("awt.toolkit", propToolkit);
			if (propGraphics != null)
				System.setProperty ("java.awt.graphicsenv", propGraphics);
			if (propFonts != null)
				System.setProperty ("java.awt.fonts", propFonts);
			
			throw new Exception("Failed to recognize the image!" + 
						" Make sure it's a JPG, GIF or PNG"); 
		}
    }
    
    public static void main(String[] args) {

    }
}

/*class PJAThread extends Thread{
	
	private String absPath = null;
	private boolean wasOK = false;
	
	PJAThread(String absPath){
		super();
		this.absPath = absPath;
	}
	
	public void run() {
		
		try{
			ImgUpload.checkPJA(absPath);
			wasOK = true;
		}
		catch (Exception e){
			File file = new File(absPath);
			file.renameTo(new File(absPath + ".rmv"));
		}
	}
	
	public boolean success(){
		return wasOK;
	}
}*/