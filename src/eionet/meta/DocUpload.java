package eionet.meta;

import eionet.util.*;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

import com.tee.uit.security.*;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DocUpload extends HttpServlet{
	
	public  static final String REQPAR_FILE   = "file";
	public  static final String REQPAR_DSID   = "ds_id";
	public  static final String REQPAR_TITLE  = "title";
	public  static final String REQPAR_IDF    = "idf";
	public  static final String REQPAR_DELETE = "delete";
	
	private static final String PERM = "du";
	
	Connection conn = null;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		doPost(req,res);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
														
		try{
			guard(req);

			String dstID = req.getParameter(REQPAR_DSID);
			if (Util.voidStr(dstID))
				throw new ServletException("Missing " + REQPAR_DSID + " request parameter!");

			String del = req.getParameter(REQPAR_DELETE);
			if (!Util.voidStr(del)){
				delete(del);
				res.sendRedirect("dataset.jsp?mode=view&ds_id=" + dstID);
				return;
			}
			
			String filePath = Props.getProperty(PropsIF.DOC_PATH);
			if (Util.voidStr(filePath))
				throw new ServletException("Missing property: " + PropsIF.DOC_PATH);
			
			File file = new File(getAbsFilePath(req));
			HttpUploader.upload(req, file);
			save(dstID, file, req.getParameter(REQPAR_TITLE));
			res.sendRedirect("dataset.jsp?mode=view&ds_id=" + dstID);
		}
		catch (Exception e){
			if (e instanceof SQLException){
				int errCode = ((SQLException)e).getErrorCode();
				System.out.println(errCode);
			}
			e.printStackTrace();
			throw new ServletException(e.getMessage()==null ? "" : e.getMessage(), e);
		}
		finally{
			closeConnection();
		}
	}
	
	/**
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private String getAbsFilePath(HttpServletRequest req) throws Exception{
		
		String path = Props.getProperty(PropsIF.DOC_PATH);
		if (Util.voidStr(path))
			throw new Exception("Missing property: " + PropsIF.DOC_PATH);
			
		if (!path.endsWith(File.separator)) path = path + File.separator;
		return path + extractFileName(req);
	}
	
	/**
	 * @param req
	 * @return
	 * @throws Exception
	 */
	private String extractFileName(HttpServletRequest req) throws Exception{
		String fullName = req.getParameter(REQPAR_FILE);
		if (Util.voidStr(fullName)) throw new Exception("Missing file path!");
		int i = fullName.lastIndexOf("\\");
		if (i==-1) i = fullName.lastIndexOf("/");
		if (i==-1) throw new Exception("Invalid file path!");
		return fullName.substring(i+1, fullName.length());
	}
	
	/**
	 * @param req
	 * @throws Exception
	 */
	private void guard(HttpServletRequest req) throws Exception{
		DDUser user = SecurityUtil.getUser(req);
		if (user == null) throw new Exception("Not authenticated!");
		
		String idf = req.getParameter(REQPAR_IDF);
		if (Util.voidStr(idf))
			throw new Exception("Missing " + REQPAR_IDF + " request parameter!");
		
		if (!SecurityUtil.hasPerm(user.getUserName(), "/datasets/" + idf, "u"))
			throw new Exception("Not permitted!");
	}
	
	/**
	 * @param dstID
	 * @param file
	 * @param title
	 * @throws Exception
	 */
	private void save(String dstID, File file, String title) throws Exception{
		
		openConnection();
		
		String legalizedPath = legalizePath(file.getAbsolutePath());
		
		INParameters inParams = new INParameters();
		LinkedHashMap insertCols = new LinkedHashMap();
		insertCols.put("OWNER_ID", inParams.add(dstID, Types.INTEGER));
		insertCols.put("MD5_PATH", "md5(" + inParams.add(legalizedPath) + ")");
		insertCols.put("ABS_PATH", inParams.add(legalizedPath));
		
		if (title==null || title.length()==0)
			title = file.getName();
		insertCols.put("TITLE", inParams.add(title));
		
		SQL.preparedStatement(SQL.insertStatement("DOC", insertCols), inParams, conn).executeUpdate();
	}

	/**
	 * @param md5
	 * @throws Exception
	 */
	private void delete(String md5) throws Exception{
		
		openConnection();
		INParameters inParams = new INParameters();
		String sqlStr = "select * from DOC where MD5_PATH=" + inParams.add(md5);
		PreparedStatement stmt = SQL.preparedStatement(sqlStr, inParams, conn);
		ResultSet rs = stmt.executeQuery();
		String absPath = rs.next() ? rs.getString("ABS_PATH") : null;
		if (absPath==null)
			return;
		
		stmt = SQL.preparedStatement("delete from DOC where MD5_PATH=", inParams, conn);
		stmt.executeUpdate();
		File file = new File(absPath);
		if (file.exists() && !file.isDirectory()) file.delete();
	}
		
	/**
	 * @throws SQLException 
	 * @throws DDConnectionException 
	 * 
	 */
	private void openConnection() throws DDConnectionException{
		if (conn==null){
			conn = ConnectionUtil.getConnection();
		}
	}
	
	/**
	 * 
	 */
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
	
	/**
	 * @param path
	 * @return
	 */
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
