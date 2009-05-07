package eionet.meta.exports.rdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.meta.exports.dbf.Dbf;
import eionet.meta.exports.xmlinst.DstXmlInst;
import eionet.meta.exports.xmlinst.TblXmlInst;
import eionet.meta.exports.xmlinst.XmlInstIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RdfServlet extends HttpServlet{

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

		PrintWriter writer = null;
        Connection conn = null;
        try{
            String id = request.getParameter("id");
            if (Util.voidStr(id))
            	throw new Exception("Missing id!");

	        conn = ConnectionUtil.getConnection();
	        Rdf rdf = new Rdf(id, conn);
	        
			response.setContentType("text/xml; charset=UTF-8");
            writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			rdf.write(writer);
	        writer.flush();
	    }
	    catch (Exception e){
	        e.printStackTrace(System.out);
			throw new ServletException(e.toString(), e);
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
