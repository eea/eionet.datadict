package eionet.meta.exports;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import eionet.meta.GetPrintout;
import eionet.meta.DDSearchEngine;
import eionet.meta.exports.*;
import eionet.meta.exports.pdf.*;
import eionet.meta.exports.xls.*;

import com.tee.util.Util;
import com.tee.xmlserver.*;
import com.tee.uit.security.*;

public class CacheServlet extends HttpServlet {
	
	public static final String KEY_ARTICLE  = "article";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_CREATED  = "created";
	
	private Hashtable objTypes = null;
	private String cachePath = null;
	
	public void init() throws ServletException{
		cachePath = getServletConfig().getServletContext().getInitParameter("doc-path");
		if (!Util.nullString(cachePath)){
			cachePath.trim();
			if (!cachePath.endsWith(File.separator))
				cachePath = cachePath + File.separator;
		}
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse res)
											throws ServletException, IOException {
		
		if (objTypes==null) setObjectTypes();
		if (Util.nullString(cachePath))
			throw new ServletException("Missing the path to cache directory!");
			
		try{
			if (req.getMethod().equalsIgnoreCase("GET"))
				get(req);
			else{
				post(req);
				//loadEntries(req);
			}
			
			dispatch(req, res);
		}
		catch (Exception e){
			e.printStackTrace(System.out);
			throw new ServletException(e.toString());
		}
		finally{
			closeConnection(req);
		}
	}
	
	private void get(HttpServletRequest req) throws Exception {
		loadEntries(req);
		setTitleParts(req);
	}

	private void post(HttpServletRequest req) throws Exception {
		
		String objID = req.getParameter("obj_id");
		if (Util.nullString(objID)) throw new Exception("Missing object ID!");
		String objType = req.getParameter("obj_type");
		if (objType==null || !objTypes.containsKey(objType))
				throw new Exception("Object type is missing or is illegal!");
		String action = req.getParameter("action");
		if (Util.nullString(action)) throw new Exception("Missing action parameter!");
		String[] articles = req.getParameterValues("article");
		if (articles==null || articles.length==0) throw new Exception("Missing articles!");
		
		ServletContext ctx = getServletContext();
		
		for (int i=0; i<articles.length; i++){
			
			String article = articles[i];
			CachableIF cachable = null;
			
			if (article.equals("pdf") && objType.equals("dst")){
				cachable = new DstPdfGuideline(getConnection(req));
				cachable.setCachePath(cachePath);
				((PdfHandout)cachable).setVsPath(ctx.getInitParameter("visuals-path"));
				((PdfHandout)cachable).setLogo(ctx.getRealPath(GetPrintout.PDF_LOGO_PATH));
			}
			else if (article.equals("xls") && objType.equals("dst")){
				cachable = new DstXls(getConnection(req));
				cachable.setCachePath(cachePath);
			}
			else if (article.equals("xls") && objType.equals("tbl")){
				cachable = new TblXls(getConnection(req));
				cachable.setCachePath(cachePath);
			}
			else
				throw new Exception("Article <" + article + "> for object <" + objType +
															"> is not handled right now!");

			if (action.equals("update"))
				cachable.updateCache(objID);
			else if (action.equals("clear"))
				cachable.clearCache(objID);
			else
				throw new Exception("Unknown action: " + action);			
			
		}
	}
	
	private void loadEntries(HttpServletRequest req) throws Exception{
		
		String objID = req.getParameter("obj_id");
		if (Util.nullString(objID)) throw new Exception("Missing object ID!");
		String objType = req.getParameter("obj_type");
		if (objType==null || !objTypes.containsKey(objType))
					throw new Exception("Object type is missing or is illegal!");
		
		req.removeAttribute("entries");
		
		Vector objArticles = getArticlesForObjType(req, objType);
		if (objArticles==null || objArticles.size()==0) return;
		
		DDSearchEngine searchEngine =
					new DDSearchEngine(getConnection(req), "", getServletContext());
		Hashtable cache = searchEngine.getCache(objID, objType);
		Hashtable supportedArticles = null;
		for (int i=0; i<objArticles.size(); i++){
			supportedArticles = (Hashtable)objArticles.get(i);
			String article = (String)supportedArticles.get("article");
			
			Hashtable cachedArticle = (Hashtable)cache.get(article);
			if (cachedArticle!=null){
				String filename = (String)cachedArticle.get("filename");
				Long created = (Long)cachedArticle.get("created");
				String full = this.cachePath + filename;
				File file = new File(full);
				if (!file.exists() || created==null || created.longValue()==0){
					deleteCacheEntry(objID, objType, article, getConnection(req));
					continue;
				}
				else
					supportedArticles.put("created", created);
			}
		}
		
		req.setAttribute("entries", objArticles);
	}
	
	private void deleteCacheEntry(String objID, String objType, String article, Connection conn)
																throws SQLException{
		StringBuffer buf = new StringBuffer("delete from CACHE where OBJ_ID=").
		append(objID).append(" and OBJ_TYPE=").append(Util.strLiteral(objType)).
		append(" and ARTICLE=").append(Util.strLiteral(article));
		
		conn.createStatement().executeUpdate(buf.toString());
	}
	
	private Hashtable getArticles(HttpServletRequest req){
		
		Hashtable articles = (Hashtable)req.getAttribute("articles");
		if (articles==null) articles = new Hashtable();
		
		// set articles for dst
		Vector v = new Vector();
		Hashtable hash = new Hashtable();
		hash.put("article", "pdf");
		hash.put("text", "Technical specification");
		hash.put("icon", "icon_pdf.jpg");
		v.add(hash);
		
		hash = new Hashtable();
		hash.put("article", "xls");
		hash.put("text", "MS Excel template");
		hash.put("icon", "icon_xls.jpg");
		v.add(hash);
		
		articles.put("dst", v);

		// set articles for tbl
		v = new Vector();
		hash = new Hashtable();
		hash.put("article", "xls");
		hash.put("text", "MS Excel template");
		hash.put("icon", "icon_xls.gif");
		v.add(hash);
		
		articles.put("tbl", v);
		
		return articles;
	}
	
	private Vector getArticlesForObjType(HttpServletRequest req, String objType){
		
		Hashtable articles = getArticles(req);
		return (Vector)articles.get(objType);
	}

	private void setObjectTypes(){
		objTypes = new Hashtable();
		objTypes.put("dst", "dataset");
		objTypes.put("tbl", "table");
		objTypes.put("elm", "element");
	}
	
	private Connection getConnection(HttpServletRequest req){
		
		Connection conn = (Connection)req.getAttribute("connection");
		if (conn==null){
			XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();            
			conn = pool.getConnection();
		}
		
		return conn;
	}
	
	private void closeConnection(HttpServletRequest req){
		
		Connection conn = (Connection)req.getAttribute("connection");
		if (conn!=null){
			try { conn.close(); } catch (SQLException e) {}
		}
	}
	
	private void dispatch(HttpServletRequest req, HttpServletResponse res)
											throws ServletException, IOException {
		
		if (req.getMethod().equalsIgnoreCase("POST")){
			StringBuffer buf = new StringBuffer("GetCache?obj_id=").
			append(req.getParameter("obj_id")).
			append("&obj_type=").append(req.getParameter("obj_type")).
			append("&idf=").append(req.getParameter("idf"));
			res.sendRedirect(buf.toString());
		}
		else
			req.getRequestDispatcher("cache.jsp").forward(req, res);
	}

	private void setTitleParts(HttpServletRequest req) throws Exception{
		
		req.setAttribute("object_type", objTypes.get(req.getParameter("obj_type")));
		String idf = req.getParameter("idf");
		idf = idf==null || idf.length()==0 ? "?" : idf;
		req.setAttribute("identifier", idf);
	}
	
	private void guard(HttpServletRequest req) throws Exception{
	}
}
