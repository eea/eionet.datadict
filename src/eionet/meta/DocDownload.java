package eionet.meta;

import eionet.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

import com.tee.uit.security.*;
import com.tee.xmlserver.*;
import com.tee.util.SQLGenerator;

public class DocDownload extends HttpServlet{
	
	private static final int BUF_SIZE = 1024;
	public  static final String REQPAR_FILE  = "file";
	
	private Connection conn = null;
	private String mimeType = null;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		
		try{
			//guard(req);
			
			mimeType = "application/octet-stream";
			String absPath = getAbsPath(req);
			if (Util.voidStr(absPath))
				throw new Exception("Failed to get the file path from db!");
			
			File file = new File(absPath);
			if (!file.exists() || file.isDirectory())
				throw new Exception("The file does not exist!");
			
			String fileName = file.getName();
			if (fileName==null) fileName = "unknown.unknown";

			res.setContentType(mimeType);

			StringBuffer strBuf = new StringBuffer("attachment; filename=\"").
			append(fileName).append("\"");
			res.setHeader("Content-Disposition", strBuf.toString());
			
			writeFile(file, res);
		}
		catch (Exception e){
			throw new ServletException(e.toString());
		}
		finally{
			closeConnection();
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		doGet(req,res);
	}
	
	private void writeFile(File file, HttpServletResponse res) throws IOException{
		
		int i = 0;
		byte[] buf = new byte[BUF_SIZE];
		FileInputStream in = null;
		OutputStream out = null;
		
		try{
			in = new FileInputStream(file);
			res.setContentLength(in.available());
			out = res.getOutputStream();
			while ((i=in.read(buf, 0, buf.length)) != -1){
				out.write(buf, 0, i);
			}
		}
		finally{
			if (in!=null) in.close();
			out.close();
		}
	}
	
	private String getAbsPath(HttpServletRequest req) throws Exception{
		
		String md5 = req.getParameter(REQPAR_FILE);
		if (Util.voidStr(md5))
			throw new Exception("Missing " + REQPAR_FILE + " request parameter!");
			
		openConnection();
		String absPath = null;
		String q = "select * from DOC where MD5_PATH=" + Util.strLiteral(md5);
		ResultSet rs = conn.createStatement().executeQuery(q);
		if (rs.next()){
			absPath = rs.getString("ABS_PATH");
			setMimeType(absPath);
		}
		
		return absPath;
	}
	
	private void setMimeType(String absPath){
		
		String s = absPath.toLowerCase();
		
		if (s.endsWith(".pdf"))
			mimeType = "application/pdf";
		else if (s.endsWith(".doc"))
			mimeType = "application/msword"; 
		else if (s.endsWith(".rtf"))
			mimeType = "text/rtf";
		else if (s.endsWith(".xls"))
			mimeType = "application/vnd.ms-excel";
		else if (s.endsWith(".ppt"))
			mimeType = "application/vnd.ms-powerpoint";
		else if (s.endsWith(".txt"))
			mimeType = "text/plain";
		else if (s.endsWith(".zip"))
			mimeType = "application/zip";
		else if (s.endsWith(".htm"))
			mimeType = "text/html";
		else if (s.endsWith(".html"))
			mimeType = "text/html";
		else if (s.endsWith(".xml"))
			mimeType = "text/xml";
		else if (s.endsWith(".xsd"))
			mimeType = "text/xml";
		else if (s.endsWith(".mdb"))
			mimeType = "application/vnd.ms-access";
	}

	private void guard(HttpServletRequest req) throws Exception{
		AppUserIF user = SecurityUtil.getUser(req);
		if (user == null) throw new Exception("Not authenticated!");
	}

	private void openConnection(){
		if (conn==null){
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();            
			conn = pool.getConnection();
		}
	}
	
	private void closeConnection(){
		if (conn!=null){
			try {
				conn.close();
			}
			catch (SQLException sqle){
			}
			finally {
				conn=null;
			}
		}
	}
}
