/*
 * Created on Jan 31, 2006
 */
package eionet.meta.exports.mdb;

import javax.servlet.http.*;
import javax.servlet.*;

import com.tee.xmlserver.DBPoolIF;
import com.tee.xmlserver.XDBApplication;

import java.io.*;
import java.sql.*;

/**
 * @author jaanus
 */
public class MdbServlet extends HttpServlet {

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse res)
								throws ServletException, IOException {

		res.setContentType("application/vnd.ms-access");

		Connection conn = null;
		OutputStream os = null;
		FileInputStream in = null;
		File file = null;
		
		boolean cacheUsed = true;
		try{
			String dstID = req.getParameter("dstID");
			if (dstID==null || dstID.length()==0)
				throw new MdbException("Missing request parameter: dstID");

			ServletContext ctx = getServletContext();
			String filePath = ctx.getInitParameter("doc-path");
			if (filePath==null)
				throw new MdbException("Missing context parameter: doc-path");
			else if (!filePath.endsWith(File.separator))
				filePath = filePath + File.separator;

			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();            
			conn = pool.getConnection();
			
			file = Mdb.getCached(conn, dstID, filePath);
			if (file==null || !file.exists()){
				cacheUsed = false;
				String fullPath = filePath + dstID + "-" + req.getSession().getId() + ".mdb";
				file = Mdb.getNew(conn, dstID, fullPath);
			}
			
			if (file==null || !file.exists())
				throw new MdbException("No exceptions thrown, but no file created either");

			os = res.getOutputStream();
			in = new FileInputStream(file);
			
			String downloadFileName = Mdb.getFileNameFor(conn, dstID);
			StringBuffer strBuf = new StringBuffer("attachment; filename=\"").
			append(downloadFileName).append("\"");
			res.setHeader("Content-Disposition", strBuf.toString());

			int i = 0;
			byte[] buf = new byte[1024];
			while ((i=in.read(buf, 0, buf.length)) != -1){
				os.write(buf, 0, i);
			}
			
			os.flush();
		}
		catch (Throwable t){
			t.printStackTrace(System.out);
			throw new ServletException(t.toString());
		}
		finally{
			try{
				if (conn != null) conn.close();
				if (in!=null) in.close();
				if (os != null) os.close();
				if (!cacheUsed && file!=null && file.exists()) file.delete();
			}
			catch(Exception ee){}
		}
	}
}
