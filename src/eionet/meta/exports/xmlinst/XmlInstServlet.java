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

        PrintWriter writer = null;
        Connection conn = null;
        
        try{
        	
			//guard(req);
			
            // get the object ID
            String id = req.getParameter("id");
	        if (Util.voidStr(id)) throw new Exception("Missing id!");

			// get the object type
			String type = req.getParameter("type");
			if (Util.voidStr(type)) throw new Exception("Missing type!");
	        
	        ServletContext ctx = getServletContext();
	        String appName = ctx.getInitParameter("application-name");

            // JH 300603 - getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            conn = pool.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			res.setContentType("text/xml; charset=UTF-8");
			OutputStreamWriter osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

			XmlInstIF xmlInst = null;
			if (type.equals("tbl"))
            	xmlInst = new TblXmlInst(searchEngine, writer);
            else if (type.equals("dst"))
				xmlInst = new DstXmlInst(searchEngine, writer);
			else
				throw new Exception("Unknown type: " + type);
            
            // build application context
            String reqUri = req.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) xmlInst.setAppContext(reqUri.substring(0,i));
            
			xmlInst.write(id);
			xmlInst.flush();
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
