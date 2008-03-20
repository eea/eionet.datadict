package eionet.meta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.meta.exports.schema.*;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
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
			
	        String servletPath = req.getServletPath();
			boolean isContainerSchema =
			servletPath!=null && servletPath.trim().startsWith("/GetContainerSchema");
	        
	        ServletContext ctx = getServletContext();

            // get the DB connection
	        conn = ConnectionUtil.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	        res.setContentType("text/xml; charset=UTF-8");
	        OutputStreamWriter osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

            SchemaIF schema = null;
            if (!isContainerSchema){
	            if (compType.equals(DST))
	                schema = new DstSchema(searchEngine, writer);
	            else if (compType.equals(TBL))
	                schema = new TblSchema(searchEngine, writer);
	            else if (compType.equals(ELM))
	                schema = new ElmSchema(searchEngine, writer);
	            else
	                throw new Exception("Invalid component type!");
            }
            else{
				if (compType.equals(TBL))
					schema = new ElmsContainerSchema(searchEngine, writer);
				else
					throw new Exception("Invalid component type for a container schema!");
            }
                
            schema.setIdentitation("\t");
            
            // build application context
            String reqUri = req.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) schema.setAppContext(reqUri.substring(0,i));
            
            schema.write(compID);
            schema.flush();
    	    
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
}
