package eionet.meta.exports.xmlinst;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDUser;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

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

            // get the DB connection
	        conn = ConnectionUtil.getConnection();
                
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
    	
    	DDUser user = SecurityUtil.getUser(req);
		if (user==null) throw new Exception("Not logged in!");
		
		if (!SecurityUtil.hasPerm(user.getUserName(), "/", "xmli"))
			throw new Exception("Not permitted!");
    }
}
