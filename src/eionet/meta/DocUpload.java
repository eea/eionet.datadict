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

public class DocUpload extends HttpServlet{
	
	public  static final String REQPAR_FILE   = "file";
	public  static final String REQPAR_DSID   = "ds_id";
	public  static final String REQPAR_TITLE  = "title";
	public  static final String REQPAR_IDF    = "idf";
	public  static final String REQPAR_DELETE = "delete";
	
	private static final String PERM = "du";
	private static final String CTXPAR_FILEPATH = "doc-path";
	
	Connection conn = null;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		doPost(req,res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		
		try{
			guard(req);
			
			String del = req.getParameter(REQPAR_DELETE);
			if (!Util.voidStr(del)){
				delete(del);
				writeResponse(res);
				return;
			}
			
			String filePath = getServletContext().getInitParameter(CTXPAR_FILEPATH);
			if (Util.voidStr(filePath))
				throw new ServletException("Missing " + CTXPAR_FILEPATH + " context param!");
			
			String dstID = req.getParameter(REQPAR_DSID);
			if (Util.voidStr(dstID))
				throw new ServletException("Missing " + REQPAR_DSID + " request parameter!");
			
			File file = new File(getAbsFilePath(req));
			HttpUploader.upload(req, file);
			save(dstID, file, req.getParameter(REQPAR_TITLE));
			writeResponse(res);
			//req.getRequestDispatcher("doc_upload.jsp").forward(req, res);
		}
		catch (Exception e){
			throw new ServletException(e.toString());
		}
		finally{
			closeConnection();
		}
	}
	
	private void writeResponse(HttpServletResponse res) throws IOException{
		res.setContentType("text/html");
		res.getWriter().println("<html><script>window.opener.location.reload(true);window.close();</script></html>");
	}
	
	private String getAbsFilePath(HttpServletRequest req) throws Exception{
		
		String path = getServletContext().getInitParameter(CTXPAR_FILEPATH);
		if (Util.voidStr(path))
			throw new Exception("Missing " + CTXPAR_FILEPATH + " context param!");
			
		if (!path.endsWith(File.separator)) path = path + File.separator;
		return path + extractFileName(req);
	}
	
	private String extractFileName(HttpServletRequest req) throws Exception{
		String fullName = req.getParameter(REQPAR_FILE);
		if (Util.voidStr(fullName)) throw new Exception("Missing file path!");
		int i = fullName.lastIndexOf("\\");
		if (i==-1) i = fullName.lastIndexOf("/");
		if (i==-1) throw new Exception("Invalid file path!");
		return fullName.substring(i+1, fullName.length());
	}
	
	private void guard(HttpServletRequest req) throws Exception{
		AppUserIF user = SecurityUtil.getUser(req);
		if (user == null) throw new Exception("Not authenticated!");
		
		String idf = req.getParameter(REQPAR_IDF);
		if (Util.voidStr(idf))
			throw new Exception("Missing " + REQPAR_IDF + " request parameter!");
		
		if (!SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + idf, "u"))
			throw new Exception("Not permitted!");
	}
	
	private void save(String dstID, File file, String title) throws Exception{
		
		openConnection();
		
		String legalizedPath = legalizePath(file.getAbsolutePath());
		
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DOC");
		gen.setField("OWNER_ID", dstID);
		gen.setFieldExpr("MD5_PATH", "md5(" + Util.strLiteral(legalizedPath) + ")");
		gen.setField("ABS_PATH", legalizedPath);
		if (title==null || title.length()==0) title = file.getName();
		gen.setField("TITLE", title);
		
		conn.createStatement().executeUpdate(gen.insertStatement());
	}

	private void delete(String md5) throws Exception{
		
		openConnection();
		String q = "select * from DOC where MD5_PATH=" + Util.strLiteral(md5);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(q);
		String absPath = rs.next() ? rs.getString("ABS_PATH") : null;
		if (absPath==null) return;
		
		stmt.executeUpdate("delete from DOC where MD5_PATH=" + Util.strLiteral(md5));
		File file = new File(absPath);
		if (file.exists() && !file.isDirectory()) file.delete();
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
	
	private String legalizePath(String path){
		StringBuffer buf = new StringBuffer();
		for (int i=0; path!=null && i<path.length(); i++){
			char c = path.charAt(i);
			if (c=='\\')
				buf.append("\\\\");
			else
				buf.append(c);
		}
		
		return buf.toString();
	}
}
