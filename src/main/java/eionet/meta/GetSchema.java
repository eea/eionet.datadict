package eionet.meta;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.exports.schema.DstSchema;
import eionet.meta.exports.schema.ElmSchema;
import eionet.meta.exports.schema.ElmsContainerSchema;
import eionet.meta.exports.schema.SchemaIF;
import eionet.meta.exports.schema.TblSchema;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class GetSchema extends HttpServlet {

    /** */
    public static final String DST = "DST";
    public static final String TBL = "TBL";
    public static final String ELM = "ELM";

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

        PrintWriter writer = null;
        Connection conn = null;

        try {
            // get request parameters

            String compID = req.getParameter("id");
            if (Util.isEmpty(compID))
                throw new Exception("Schema ID missing!");

            String compType = null;
            if (compID.startsWith(DST)) {
                compType = DST;
                compID = compID.substring(DST.length());
            } else if (compID.startsWith(TBL)) {
                compType = TBL;
                compID = compID.substring(TBL.length());
            } else if (compID.startsWith(ELM)) {
                compType = ELM;
                compID = compID.substring(ELM.length());
            } else
                throw new Exception("Malformed schema ID!");

            // see if this is a "container schema"
            String servletPath = req.getServletPath();
            boolean isContainerSchema = servletPath != null && servletPath.trim().startsWith("/GetContainerSchema");

            ServletContext ctx = getServletContext();

            // get the DB connection, initialize DDSearchEngine
            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

            // set response content type
            res.setContentType("text/xml; charset=UTF-8");

            // init stream writer
            OutputStreamWriter osw = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            writer = new PrintWriter(osw);

            // create schema genereator
            SchemaIF schema = null;
            if (!isContainerSchema) {
                if (compType.equals(DST))
                    schema = new DstSchema(searchEngine, writer);
                else if (compType.equals(TBL))
                    schema = new TblSchema(searchEngine, writer);
                else if (compType.equals(ELM))
                    schema = new ElmSchema(searchEngine, writer);
                else
                    throw new Exception("Invalid component type!");
            } else {
                if (compType.equals(TBL))
                    schema = new ElmsContainerSchema(searchEngine, writer);
                else
                    throw new Exception("Invalid component type for a container schema!");
            }
            schema.setIdentitation("\t");

            // build application context
            String reqUri = req.getRequestURL().toString();
            int i = reqUri.lastIndexOf("/");
            if (i != -1) schema.setAppContext(reqUri.substring(0, i));

            // set content disposition header
            StringBuffer strBuf = new StringBuffer("attachment; filename=\"schema-").
            append(compType.toLowerCase()).append("-").append(compID).append(".xsd\"");
            res.setHeader("Content-Disposition", strBuf.toString());

            // write & flush schema
            schema.write(compID);
            schema.flush();

            // flush and close stream writer
            writer.flush();
            osw.flush();
            writer.close();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new ServletException(e.toString());
        } finally {
            try {
                if (writer != null) writer.close();
                if (conn != null) conn.close();
            } catch (Exception ee) {}
        }
    }
}
