package eionet.meta.exports.xforms;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.meta.exports.schema.*;
import eionet.util.Util;
import eionet.meta.DDSearchEngine;

import com.tee.xmlserver.*;

public class XFormServlet extends HttpServlet {
	
	private static final String CTXPAR_TMP_FILE_PATH = "temp-file-path";
	private static final String TEMPLATE_NAME = "xform.xhtml";
    
	protected void service(HttpServletRequest req, HttpServletResponse res)
								throws ServletException, IOException {

		res.setContentType("text/xml");
		
		PrintWriter writer = null;
		Connection conn = null;
        
		try{
			String id = req.getParameter("id");
			if (Util.voidStr(id))
				throw new Exception("Missing id!");
	        
			ServletContext ctx = getServletContext();
			String appName = ctx.getInitParameter("application-name");

			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();            
			conn = pool.getConnection();
                
			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			writer = new PrintWriter(res.getOutputStream());

			XFormIF xForm = new TblXForm(searchEngine, writer);
            
			// build application context (protocol + host + port + context path)
			String reqUrl = req.getRequestURL().toString();
			int i = reqUrl.lastIndexOf("/");
			if (i != -1) xForm.setAppContext(reqUrl.substring(0,i));
				
			// set up the template path
			String template = getServletContext().getInitParameter(CTXPAR_TMP_FILE_PATH);
			if (template!=null){
				if (!template.endsWith(File.separator)) template = template + File.separator;
				template = template + TEMPLATE_NAME;
			}
            
			xForm.write(id);
			xForm.flush(template);
			writer.flush();
			writer.close();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
			throw new ServletException(e.toString());
		}
		finally{
			try{
				if (writer != null) writer.close();
				if (conn != null) conn.close();
			}
			catch(Exception ee){}
		}
	}
}