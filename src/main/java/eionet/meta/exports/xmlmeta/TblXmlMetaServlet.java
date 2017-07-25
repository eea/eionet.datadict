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
 *                   Istvan Alfeldi (ED)
 */

package eionet.meta.exports.xmlmeta;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TblXmlMetaServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(TblXmlMetaServlet.class);

    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        PrintWriter writer = null;
        Connection conn = null;

        try {
            String compID = req.getParameter("id");
            if (Util.isEmpty(compID))
                throw new Exception("Table ID missing!");

            ServletContext ctx = getServletContext();

            // get the DB connection
            conn = ConnectionUtil.getConnection();

            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
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
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e.toString(), e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
