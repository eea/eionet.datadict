package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.meta.exports.schema.*;
import eionet.util.Util;

import com.tee.xmlserver.*;

public class GetSchema extends HttpServlet {
    
    public static final String DST = "DST"; 
    public static final String TBL = "TBL"; 
    public static final String ELM = "ELM"; 
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        PrintWriter writer = null;
        Connection conn = null;
        
        try{
            String compID = req.getParameter("id");
	        if (Util.voidStr(compID))
	            throw new Exception("Schema ID missing!");
	        
			String compType = null;
	        if (compID.startsWith(DST)){
				compType = DST;
				compID = compID.substring(DST.length());
	        }
	        else if (compID.startsWith(TBL)){
				compType = TBL;
				compID = compID.substring(TBL.length());
	        }
			else if (compID.startsWith(ELM)){
				compType = ELM;
				compID = compID.substring(ELM.length());
			}
			else
				throw new Exception("Malformed schema ID!");
			
	        ServletContext ctx = getServletContext();
	        String appName = ctx.getInitParameter("application-name");

            // JH 300603 - getting the DB pool through XmlServer
            XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
            DBPoolIF pool = XDBApplication.getDBPool();            
            conn = pool.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            writer = new PrintWriter(res.getOutputStream());

            SchemaIF schema = null;
            if (compType.equals(DST))
                schema = new DstSchema(searchEngine, writer);
            else if (compType.equals(TBL))
                schema = new TblSchema(searchEngine, writer);
            else if (compType.equals(ELM))
                schema = new ElmSchema(searchEngine, writer);
            else
                throw new Exception("Invalid component type!");
                
            schema.setIdentitation("\t");
            
            // build application context
            String reqUri = req.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) schema.setAppContext(reqUri.substring(0,i));
            
            schema.write(compID);
            schema.flush();
    	    
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
}