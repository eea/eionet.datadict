package eionet.meta.exports.codelist;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.meta.exports.schema.*;
import eionet.util.*;
import eionet.meta.DDSearchEngine;
import com.tee.xmlserver.*;
import com.tee.uit.security.*;

public class CodelistServlet extends HttpServlet {
	
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        PrintWriter writer = null;
		OutputStreamWriter osw = null;
        Connection conn = null;
        
        try{
			//guard(req);
			
            // get the object ID
            String id = req.getParameter("id");
	        if (Util.voidStr(id)) throw new Exception("Missing object id!");

			// get the object type
			String type = req.getParameter("type");
			if (Util.voidStr(type)) throw new Exception("Missing object type!");

			// get the delimiter
			String delim = req.getParameter("delim");
	        
	        // get the application name
	        ServletContext ctx = getServletContext();
	        String appName = ctx.getInitParameter("application-name");

            // JH 300603 - getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            conn = pool.getConnection();
                
			res.setContentType("text/plain; charset=UTF-8");
			osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

			Codelist codelist = new Codelist(conn, writer, delim);
			codelist.write(id, type);
			codelist.flush();
	        writer.flush();
	        osw.flush();
			writer.close();
			osw.close();
	    }
	    catch (Exception e){
	        e.printStackTrace(System.out);
			throw new ServletException(e.toString());
	    }
		finally{
			try{ if (conn != null) conn.close(); } catch(Exception ee){}
		}
    }
    
    private void guard(HttpServletRequest req) throws Exception{
    	
		AppUserIF user = SecurityUtil.getUser(req);
		if (user==null) throw new Exception("Not logged in!");
		
		if (!SecurityUtil.hasPerm(user.getUserName(), "/", "xmli"))
			throw new Exception("Not permitted!");
    }
}
