package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.util.*;
import eionet.meta.exports.*;
import eionet.meta.exports.pdf.*;
import eionet.meta.savers.Parameters;

import com.tee.xmlserver.XDBApplication;
import com.tee.xmlserver.DBPoolIF;

public class GetPrintout extends HttpServlet {
	
	public static final String PDF_LOGO_PATH = "images/pdf_logo.png";
    
    private static final String DEFAULT_HANDOUT_TYPE = PdfHandoutIF.GUIDELINE;
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        // get the servlet context and db-pool name
        ServletContext ctx = getServletContext();
        String appName = ctx.getInitParameter("application-name");
        if (Util.voidStr(appName))
            throw new ServletException("Application name not specified!");
        
        String userAgent = req.getHeader("User-Agent");
        if (!Util.voidStr(userAgent))
            ctx.log("User-Agent= " + userAgent);
            
        // get printout format
        String printoutFormat = req.getParameter("format");
        if (Util.voidStr(printoutFormat))
            printoutFormat = "PDF";
        
        if (!printoutFormat.equals("PDF") && !printoutFormat.equals("RTF"))
            throw new ServletException("Unknown format requested!");
        
        // currently RTF is not supported
        if (printoutFormat.equals("RTF"))
            throw new ServletException("RTF not supported right now!");
        
        // get object type
        String objType = req.getParameter("obj_type");
        if (Util.voidStr(objType))
            throw new ServletException("Object type not specified!");
            
        // get handout type
        String outType = req.getParameter("out_type");
        if (Util.voidStr(outType))
            outType = DEFAULT_HANDOUT_TYPE;
        
        // get object ID
        String objID = req.getParameter("obj_id");
        if (Util.voidStr(objID))
            throw new ServletException("Object ID not specified!");
        
        // get the path of images
        String visualsPath = ctx.getInitParameter("visuals-path");
		// get the path of cache
		String cachePath = ctx.getInitParameter("doc-path");
        
        Connection conn = null;
        
        // get to the business
        try{
            // get database connection
            
	        // JH 300603 - getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            conn = pool.getConnection();
    	
    	    // set up the OutputStream to write to
    	    ByteArrayOutputStream barray = new ByteArrayOutputStream();
    	    
            // construct the handout
            PdfHandoutIF handout = null;
            if (outType.equals(PdfHandoutIF.FACTSHEET)){
                if (objType.equals(PdfHandoutIF.DATASET))
                    handout = new DstPdfAll(conn, barray);
                else if (objType.equals(PdfHandoutIF.DSTABLE))
                    handout = new TblPdfFactsheet(conn, barray);
                else if (objType.equals(PdfHandoutIF.DATAELEM))
                    handout = new ElmPdfFactsheet(conn, barray);
                else
                    throw new Exception("Unknown object type- " + objType +
                                        "- for this handout type!");
            }
            else if (outType.equals(PdfHandoutIF.GUIDELINE)){
                if (objType.equals(PdfHandoutIF.DATASET)){
                    handout = new DstPdfGuideline(conn, barray);
                    ((CachableIF)handout).setCachePath(cachePath);
                }
                else 
                    throw new Exception("Unknown object type- " + objType +
                                        "- for this handout type!");
            }
            else
                throw new Exception("Unknown handout type- " + outType);
            
            // set handout logo
            handout.setLogo(ctx.getRealPath(PDF_LOGO_PATH));
            
            // set images path
			handout.setVsPath(visualsPath);
			
			// set parameters
			handout.setParameters(new Parameters(req));
            
            // write the handout
            handout.write(objID);
            handout.flush();
            
            // flush the handout to the servlet output stream
            res.setContentType("application/pdf");
            res.setContentLength(barray.size()); // not supported by Resin version < 2.x.x
			StringBuffer buf = new StringBuffer("attachment; filename=\"").
			append(handout.getFileName()).append("\"");
			res.setHeader("Content-Disposition", buf.toString());
            
            ServletOutputStream out = res.getOutputStream();
            barray.writeTo(out);
            out.flush();
            
            if (conn != null)
                conn.close();
	    }
	    catch (Exception e){
	        e.printStackTrace(new PrintStream(res.getOutputStream()));
	        //throw new ServletException(e.toString());
	    }
	    finally{
	        try{ if (conn != null) conn.close(); }
	        catch (Exception e) {}
	    }
    }
}