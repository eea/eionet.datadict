package eionet.meta.exports.xls;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.meta.exports.CachableIF;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

public class XlsServlet extends HttpServlet {
	
	private HashSet validObjTypes = null;
	
	public void init() throws ServletException{
		validObjTypes = new HashSet();
		validObjTypes.add("dst");
		validObjTypes.add("tbl");
	}
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

		res.setContentType("application/vnd.ms-excel");
		
		OutputStream os = null;
        Connection conn = null;
        
        try{
        	
            String id = req.getParameter("obj_id");
	        if (Util.voidStr(id))
	            throw new Exception("Missing object id!");
	        
			String type = req.getParameter("obj_type");
			if (Util.voidStr(type) || !validObjTypes.contains(type))
				throw new Exception("Missing object type or object type invalid!");
	        
	        ServletContext ctx = getServletContext();
			String cachePath = Props.getProperty(PropsIF.DOC_PATH);

            // get the DB connection
			conn = ConnectionUtil.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            os = res.getOutputStream();
            
            XlsIF xls = null;
            if (type.equals("dst")){
            	xls = new DstXls(searchEngine, os);
				((CachableIF)xls).setCachePath(cachePath);
            }
            else{
            	xls = new TblXls(searchEngine, os);
				((CachableIF)xls).setCachePath(cachePath);
            }
            
			xls.create(id);
			StringBuffer buf = new StringBuffer("attachment; filename=\"").
			append(xls.getName()).append("\"");
			res.setHeader("Content-Disposition", buf.toString());
			
			xls.write();
	        os.flush();
	        os.close();
	    }
	    catch (Exception e){
	        e.printStackTrace(System.out);
			throw new ServletException(e.toString());
	    }
		finally{
			try{
				if (os != null) os.close();
				if (conn != null) conn.close();
			}
			catch(Exception ee){}
		}
    }
}
