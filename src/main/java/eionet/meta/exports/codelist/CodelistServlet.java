package eionet.meta.exports.codelist;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.meta.DDUser;
import eionet.meta.exports.VocabularyOutputHelper;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import java.io.OutputStream;
import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 * 
 * changed by Lena Kargioti, eka@eworx.gr on Sept 2015
 *
 */
public class CodelistServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodelistServlet.class);

    @Autowired
    private CodeValueHandlerProvider codeValueHandlerProvider;

    private WebApplicationContext springContext;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter writer = null;
        OutputStreamWriter osw = null;

        try {
            // guard(req);
            // get the object ID
            String id = req.getParameter("id");
            if (Util.isEmpty(id)) throw new Exception("Missing object id!");
            // get the object type
            String type = req.getParameter("type");
            if (Util.isEmpty(type)) throw new Exception("Missing object type!");
            // get codelist format
            String format = req.getParameter("format");
            if (Util.isEmpty(format)) {
                format = "csv";
            }

            // prepare output stream and writer
            OutputStream out = res.getOutputStream();
            osw = new OutputStreamWriter(out, "UTF-8");

            //set export type
            ExportStatics.ExportType exportType = ExportStatics.ExportType.UNKNOWN;
            String filename = "codelist_"+id+"_"+type;
            // set response content type
            if (format.equals("csv")) {
                // Issue 29890
                addBOM(out);
                exportType = ExportStatics.ExportType.CSV;
                res.setContentType("text/csv; charset=UTF-8");
                res.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
            }
            else if (format.equals("xml")) {
                exportType = ExportStatics.ExportType.XML;
                res.setContentType("text/xml; charset=UTF-8");
                res.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xml");
            } else {
                throw new Exception("Unknown codelist format requested: " + format);
            }

            writer = new PrintWriter(osw);

            // construct codelist writer
            Codelist codelist = new Codelist(exportType, codeValueHandlerProvider);
            // since CodelistServlet is invoked through the use of the old url format 
            // an ObjectMapper has to be created in order to use getLegacy() which appends the old names to elements
            codelist.setObjectMapper(new ObjectMapper());
            // write & flush
            String listStr = codelist.write(id, type);

            writer.write(listStr);
            writer.flush();
            osw.flush();
            writer.close();
            osw.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e.toString());
        }
    }

    /**
     * Writes utf-8 BOM in the given writer.
     *
     * @param out
     *            current outputstream
     * @throws IOException
     *             if connection fails
     */
    private static void addBOM(OutputStream out) throws IOException {
        byte[] bomByteArray = VocabularyOutputHelper.getBomByteArray();
        for (byte b : bomByteArray) {
            out.write(b);
        }
    }

    /**
     *
     * @param req
     * @throws Exception
     */
    private void guard(HttpServletRequest req) throws Exception {
        DDUser user = SecurityUtil.getUser(req);
        if (user == null) throw new Exception("Not logged in!");

        if (!SecurityUtil.hasPerm(user.getUserName(), "/", "xmli"))
            throw new Exception("Not permitted!");
    }

}
