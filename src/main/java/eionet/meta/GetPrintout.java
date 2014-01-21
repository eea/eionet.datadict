package eionet.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.exports.CachableIF;
import eionet.meta.exports.pdf.DstCombinedPdfGuideline;
import eionet.meta.exports.pdf.DstPdfGuideline;
import eionet.meta.exports.pdf.PdfHandoutIF;
import eionet.meta.savers.Parameters;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tripledev.ee">jaanus.heinlaid@tripledev.ee</a>
 * 
 */
public class GetPrintout extends HttpServlet {

    public static final String PDF_LOGO_PATH = "images/pdf_logo.png";

    private static final String DEFAULT_HANDOUT_TYPE = PdfHandoutIF.GUIDELINE;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Connection conn = null;
        try {
            // get the servlet context
            ServletContext ctx = getServletContext();

            String userAgent = req.getHeader("User-Agent");
            if (!Util.isEmpty(userAgent)) {
                ctx.log("User-Agent= " + userAgent);
            }

            // get printout format
            String printoutFormat = req.getParameter("format");
            if (Util.isEmpty(printoutFormat)) {
                printoutFormat = "PDF";
            }

            if (!printoutFormat.equals("PDF") && !printoutFormat.equals("RTF")) {
                throw new Exception("Unknown format requested!");
            }

            // currently RTF is not supported
            if (printoutFormat.equals("RTF")) {
                throw new Exception("RTF not supported right now!");
            }

            // get object type
            String objType = req.getParameter("obj_type");
            if (Util.isEmpty(objType)) {
                throw new Exception("Object type not specified!");
            }

            // get handout type
            String outType = req.getParameter("out_type");
            if (Util.isEmpty(outType)) {
                outType = DEFAULT_HANDOUT_TYPE;
            }

            // get object ID
            String objID = req.getParameter("obj_id");
            if (Util.isEmpty(objID)) {
                throw new Exception("Object ID not specified!");
            }

            String[] objIDs = objID.split("[:]");
            if (objIDs.length == 0 || Util.isEmpty(objIDs[0])) {// there should be at least one object id
                throw new Exception("Object ID not specified!");
            }

            // get the paths of images and cache
            String fileStorePath = Props.getRequiredProperty(PropsIF.FILESTORE_PATH);
            String cachePath = Props.getProperty(PropsIF.DOC_PATH);

            // get the DB connection
            conn = ConnectionUtil.getConnection();

            // set up the OutputStream to write to
            ByteArrayOutputStream barray = new ByteArrayOutputStream();

            PdfHandoutIF handout = null;
            if (outType.equals(PdfHandoutIF.GUIDELINE)) {
                if (objType.equals(PdfHandoutIF.DATASET)) {
                    if (objIDs.length == 1) {
                        objID = objIDs[0];
                        handout = new DstPdfGuideline(conn, barray);
                        ((CachableIF) handout).setCachePath(cachePath);
                    } else {
                        handout = new DstCombinedPdfGuideline(conn, barray);
                    }
                } else {
                    throw new Exception("Unknown object type- " + objType + "- for this handout type!");
                }
            } else {
                throw new Exception("Unknown handout type- " + outType);
            }

            // set handout logo
            handout.setLogo(ctx.getRealPath(PDF_LOGO_PATH));

            // set images path
            handout.setVisualsPath(new File(fileStorePath, "visuals").toString());

            // set parameters
            handout.setParameters(new Parameters(req));

            // write the handout
            handout.write(objID);
            handout.flush();

            // flush the handout to the servlet output stream
            res.setContentType("application/pdf");
            res.setContentLength(barray.size()); // not supported by Resin version < 2.x.x
            StringBuffer buf = new StringBuffer("attachment; filename=\"").append(handout.getFileName()).append("\"");
            res.setHeader("Content-Disposition", buf.toString());

            ServletOutputStream out = res.getOutputStream();
            barray.writeTo(out);
            out.flush();

            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace(new PrintStream(res.getOutputStream()));
            res.setContentType(null);
            res.sendError(500, e.getMessage());
            throw new ServletException(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }
}