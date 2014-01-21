package eionet.meta.exports.xls;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDSearchEngine;
import eionet.meta.exports.CachableIF;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

public class XlsServlet extends HttpServlet {

    private HashSet<String> validObjTypes = null;

    @Override
    public void init() throws ServletException {
        validObjTypes = new HashSet<String>();
        validObjTypes.add("dst");
        validObjTypes.add("tbl");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        OutputStream os = null;
        Connection conn = null;

        try {

            String id = req.getParameter("obj_id");
            if (Util.isEmpty(id)) {
                throw new Exception("Missing object id!");
            }

            String type = req.getParameter("obj_type");
            if (Util.isEmpty(type) || !validObjTypes.contains(type)) {
                throw new Exception("Missing object type or object type invalid!");
            }

            String action = req.getParameter("obj_act");
            boolean dropDownAction = !Util.isEmpty(action) && action.equals("dd"); // if no action sent or invalid action send
                                                                                   // ignore it

            // ServletContext ctx = getServletContext();
            String cachePath = Props.getProperty(PropsIF.DOC_PATH);

            // get the DB connection
            conn = ConnectionUtil.getConnection();

            DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
            os = res.getOutputStream();

            XlsIF xls = null;
            if (type.equals("dst")) {
                xls = new DstXls(searchEngine, os, dropDownAction);
                ((CachableIF) xls).setCachePath(cachePath);
            } else {
                xls = new TblXls(searchEngine, os, dropDownAction);
                ((CachableIF) xls).setCachePath(cachePath);
            }

            xls.create(id);
            res.setContentType("application/vnd.ms-excel");
            StringBuffer buf = new StringBuffer("attachment; filename=\"").append(xls.getName()).append("\"");
            res.setHeader("Content-Disposition", buf.toString());
            xls.write();
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            res.setContentType(null);
            res.sendError(500, e.getMessage());
            // this exception is not caught by DDexceptinHandler hence this class is plain HttpServlet not a subclass of Stripes
            // framework
            // when content type is not set and error is sent, then browser is redirected to application server's error page
            throw new ServletException(e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ee) {
            }
        }
    }
}
