package eionet.meta.exports.dbf;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tee.xmlserver.DBPoolIF;
import com.tee.xmlserver.XDBApplication;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DbfServlet extends HttpServlet{

	/** */
	private static final String FILE_EXT = ".dbf";

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	
    	String pathInfo = req.getPathInfo();
    	if (pathInfo==null || pathInfo.length()<2)
    		throw new ServletException("Missing request path info");

        Connection conn = null;
        try{

        	// init SQL connection
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();            
			conn = pool.getConnection();
			
			// create DBF
			Dbf dbf = new Dbf(pathInfo.substring(1), conn);
			
			// write DBF into servlet output stream
			String fileName = dbf.getFileName();
			res.setContentType("application/x-dbf");
			res.setHeader("Content-Disposition", "attachment; filename=\"" + (fileName==null ? pathInfo.substring(1) : fileName) + ".dbf\"");
			dbf.write(res.getOutputStream());
        }
	    catch (Exception e){
	        e.printStackTrace(System.out);
			throw new ServletException("Error when creating DBF", e);
	    }
		finally{
			try{
				if (conn != null) conn.close();
			}
			catch(Exception ee){}
		}
    }
}
