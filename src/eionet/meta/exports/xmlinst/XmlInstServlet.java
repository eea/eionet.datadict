package eionet.meta.exports.xmlinst;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.meta.exports.schema.*;
import eionet.util.*;
import eionet.meta.DDSearchEngine;
import com.tee.xmlserver.*;
import com.tee.uit.security.*;

public class XmlInstServlet extends HttpServlet {
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

		res.setContentType("text/xml");
		
        PrintWriter writer = null;
        Connection conn = null;
        
        try{
        	
			//guard(req);
			
            String id = req.getParameter("id");
	        if (Util.voidStr(id))
	            throw new Exception("Missing id!");
	        
	        ServletContext ctx = getServletContext();
	        String appName = ctx.getInitParameter("application-name");

            // JH 300603 - getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            conn = pool.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            writer = new PrintWriter(res.getOutputStream());

            XmlInstIF xmlInst = new TblXmlInst(searchEngine, writer);
            
            // build application context
            String reqUri = req.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) xmlInst.setAppContext(reqUri.substring(0,i));
            
			xmlInst.write(id);
			xmlInst.flush();
	        writer.flush();
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
    
    private void guard(HttpServletRequest req) throws Exception{
    	
		AppUserIF user = SecurityUtil.getUser(req);
		if (user==null) throw new Exception("Not logged in!");
		
		if (!SecurityUtil.hasPerm(user.getUserName(), "/", "xmli"))
			throw new Exception("Not permitted!");
    }
}