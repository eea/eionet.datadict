/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Web Dashboards Service
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency (EEA).  Portions created by European Dynamics (ED) company are
 * Copyright (C) by European Environment Agency.  All Rights Reserved.
 * 
 * Contributors(s):
 *    Original code: Dusko Kolundzija (ED)
 *    				 Istvan Alfeldi (ED)
 */

package eionet.meta.exports.xmlmeta;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;

import eionet.util.Util;
import eionet.meta.DDSearchEngine;

import com.tee.xmlserver.*;

public class TblXmlMetaServlet extends HttpServlet {

	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		PrintWriter writer = null;
		Connection conn = null;

		try {
			String compID = req.getParameter("id");
			if (Util.voidStr(compID))
				throw new Exception("Table ID missing!");

			ServletContext ctx = getServletContext();
			String appName = ctx.getInitParameter("application-name");

			// getting the DB pool through XmlServer
			XDBApplication xdbapp = XDBApplication
					.getInstance(getServletContext());
			DBPoolIF pool = XDBApplication.getDBPool();
			conn = pool.getConnection();

			DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
			res.setContentType("text/xml; charset=UTF-8");
			OutputStreamWriter osw = new OutputStreamWriter(res
					.getOutputStream(), "UTF-8");
			writer = new PrintWriter(osw);

			XmlMetaIF schema = null;

			schema = new TblXmlMeta(searchEngine, writer);

			// build application context
			String reqUri = req.getRequestURL().toString();
			int i = reqUri.lastIndexOf("/");
			if (i != -1)
				schema.setAppContext(reqUri.substring(0, i));

			schema.write(compID);
			schema.flush();

			writer.flush();
			osw.flush();
			writer.close();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new ServletException(e.toString());
		} finally {
			try {
				if (writer != null)
					writer.close();
				if (conn != null)
					conn.close();
			} catch (Exception ee) {
			}
		}
	}
}
