package eionet.meta.exports.rdf;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RdfServlet extends HttpServlet {

    /** Logger. */
    protected static final Logger LOGGER = Logger.getLogger(RdfServlet.class);

    /** */
    public static final String URL_MAPPING = "/GetRdf";

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter writer = null;
        Connection conn = null;

        try {
            String tblID = request.getParameter("id");
            String type = request.getParameter("type");
            conn = ConnectionUtil.getConnection();
            Rdf rdf = new Rdf(tblID, type, conn);

            response.setContentType("text/xml; charset=UTF-8");
            writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
            rdf.write(writer);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new ServletException(e.toString(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ee) {
            }
        }
    }
}
