/*
 * Created on Jan 31, 2006
 */
package eionet.meta.exports.mdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.sql.ConnectionUtil;

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

			boolean vmdOnly = false;
			String strVmdOnly = req.getParameter("vmdonly");
			if (strVmdOnly!=null && strVmdOnly.equals("true"))
				vmdOnly = true;

			ServletContext ctx = getServletContext();
			String filePath = Props.getProperty(PropsIF.DOC_PATH);
			if (filePath==null)
				throw new MdbException("Missing property: " + PropsIF.DOC_PATH);
			else if (!filePath.endsWith(File.separator))
				filePath = filePath + File.separator;

			conn = ConnectionUtil.getConnection();
			
			if (!vmdOnly)
				file = Mdb.getCached(conn, dstID, filePath);
				
			if (file==null || !file.exists()){
				cacheUsed = false;
				String fullPath = filePath + dstID + "-" + req.getSession().getId() + ".mdb";
				file = Mdb.getNew(conn, dstID, fullPath, vmdOnly);
			}
			
			if (file==null || !file.exists())
				throw new MdbException("No exceptions thrown, but no file created either");

			os = res.getOutputStream();
			in = new FileInputStream(file);
			
			String downloadFileName = Mdb.getFileNameFor(conn, dstID, vmdOnly);
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
