package eionet.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;
import eionet.meta.imp.BaseHandler;
import eionet.meta.imp.DatasetImport;
import eionet.meta.imp.DatasetImportHandler;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;

public class Import extends HttpServlet {

    private static final int BUF_SIZE = 1024;
    private static final String TMP_FILE_PREFIX = "import_";
    private static final Logger LOGGER = LoggerFactory.getLogger(Import.class);

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {
        if (req.getAttribute("TEXT") != null) {
            req.setCharacterEncoding("UTF-8");
            req.getRequestDispatcher("import_results.jsp").forward(req, res);
        } else {
            forwardToErrorPage(req, res, "Access not allowed.");
        }
    }

    /*
     *  (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {
        req.setCharacterEncoding("UTF-8");

        // init response text and exception indicator
        StringBuilder responseText = new StringBuilder();

        // authenticate user
        DDUser user = SecurityUtil.getUser(req);
        try {
            if (!SecurityUtil.hasPerm(user, "/import", "x")) {
                forwardToErrorPage(req, res, "Access not allowed.");
                return;
            }
        } catch (Exception e) {
            forwardToErrorPage(req, res, "An error occurred while checking user permissions.");
            return;
        }

        // get content type, check that it's valid
        String contentType = req.getContentType();
        if (contentType == null ||
                !(contentType.toLowerCase().startsWith("multipart/form-data") ||
                        contentType.toLowerCase().startsWith("text/xml"))) {
            forwardToErrorPage(req, res, "Posted content type is unknown.");
            return;
        }

        // set the response content type
        res.setContentType("text/html");

        // start processing the request
        String boundary = null;
        String sUrl = null;
        String type = null;
        String delem_id = null;

        if (contentType.toLowerCase().startsWith("multipart/form-data")) {
            // file upload, multipart request
            String fileORurl = req.getParameter("fileORurl");
            type = req.getParameter("type");
            delem_id = req.getParameter("delem_id");

            if (fileORurl != null && fileORurl.equalsIgnoreCase("url")) {
                sUrl = req.getParameter("url_input");
            }
            // extract the multipart request's boundary string
            boundary = extractBoundary(contentType);
        }

        // check that the element type is valid
        if (type == null || (!type.equals("FXV") && !type.equals("DST"))) {
            forwardToErrorPage(req, res, "Invalid import type.");
            return;
        }

        // check that the element id is valid, when type is FXV
        if (type.equals("FXV") && (delem_id == null || NumberUtils.toInt(delem_id) <= 0)) {
            forwardToErrorPage(req, res, "Invalid data element id.");
            return;
        }

        // since the data is going to be saved into a file
        // (regardless of whether we have a file upload or
        // URL stream, we initialize the file
        String tmpFilePath = Props.getProperty(PropsIF.TEMP_FILE_PATH);
        if (tmpFilePath == null) {
            tmpFilePath = System.getProperty("user.dir");
        }

        if (!tmpFilePath.endsWith(File.separator)) {
            tmpFilePath = tmpFilePath + File.separator;
        }

        StringBuilder tmpFileName = new StringBuilder(tmpFilePath + TMP_FILE_PREFIX);
        tmpFileName.append("_")
                .append(req.getRequestedSessionId().replace('-', '_'))
                .append("_")
                .append(System.currentTimeMillis())
                .append(".xml");

        File file = new File(tmpFileName.toString());
        try (RandomAccessFile raFile = new RandomAccessFile(file, "rw")) {
            // set up handler
            BaseHandler handler = new DatasetImportHandler();
            try (Connection userConn = user.getConnection()) {
                // get the data and save to file
                if (sUrl == null) {
                    writeToFile(raFile, req.getInputStream(), boundary);
                } else {
                    URL url = new URL(sUrl);
                    writeToFile(raFile, url.openStream());
                }

                // parse the file
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader reader = parser.getXMLReader();
                reader.setContentHandler(handler); // pass our handler to SAX

                reader.parse(tmpFileName.toString());

                // SAX was OK, but maybe handler problems of its own
                if (!handler.hasError()) {
                    DatasetImport dbImport = new DatasetImport((DatasetImportHandler) handler, userConn, getServletContext());
                    dbImport.setUser(user);
                    dbImport.setDate(String.valueOf(System.currentTimeMillis()));
                    dbImport.setImportType(type);
                    if (type.equals("FXV")) {
                        dbImport.setParentID(delem_id);
                    }
                    dbImport.execute();

                    responseText.append(dbImport.getResponseText());
                } else {
                    throw new Exception(handler.getErrorBuff().toString());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                file.delete();
                forwardToErrorPage(req, res, "An error occurred during import.");
                return;
            }
        }
        // if was fixed values import explicitly, add a link back to the element
        if (type.equals("FXV")) {
            responseText.append("<br><br><a href='")
                    .append(req.getContextPath())
                    .append("/dataelements/")
                    .append(delem_id)
                    .append("'>Back to data element</a>");
        }

        file.delete();
        req.setAttribute("TEXT", responseText.toString());
        doGet(req, res);
    }

    /**
     * Write to file, if import goes through URL
     */
    private void writeToFile(RandomAccessFile raFile, InputStream in) throws Exception {
        byte[] buf = new byte[BUF_SIZE];
        int i;
        while ((i = in.read(buf, 0, buf.length)) != -1) {
            raFile.write(buf, 0, i);
        }

        raFile.close();
        in.close();
    }

    /**
     * Write to file, if import goes through a file upload.
     */
    private void writeToFile(RandomAccessFile raFile, ServletInputStream in, String boundary) throws Exception {
        byte[] buf = new byte[BUF_SIZE];
        int i;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        boolean fileStart = false;
        boolean pastContentType = false;

        do {
            int b = in.read();
            if (b == -1)
            {
                break; // if end of stream, break
            }
            bout.write(b);

            if (!pastContentType) { // if Content-Type not passed, no check of LNF
                String s = bout.toString();
                if (s.indexOf("Content-Type") != -1) {
                    pastContentType = true;
                }
            } else {
                // Content-Type is passed, after next double LNF is file start
                byte[] bs = bout.toByteArray();
                if (bs != null && bs.length >= 4) {
                    if (bs[bs.length - 1] == 10
                        && bs[bs.length - 2] == 13
                        && bs[bs.length - 3] == 10
                        && bs[bs.length - 4] == 13) {

                        fileStart = true;
                    }
                }
            }
        } while (!fileStart);

        while ((i = in.readLine(buf, 0, buf.length)) != -1) {
            String line = new String(buf, 0, i);
            if (boundary != null && line.startsWith(boundary)) {
                break;
            }
            raFile.write(buf, 0, i);
        }

        raFile.close();
        in.close();
    }

    /**
     * Extract the boundary string in multipart request.
     */
    private String extractBoundary(String contentType) {
        int i = contentType.indexOf("boundary=");
        if (i == -1) {
            return null;
        }
        String boundary = contentType.substring(i + 9); // 9 for "boundary="
        return "--" + boundary; // the real boundary is always preceded by an extra "--"
    }

    private void forwardToErrorPage(HttpServletRequest req, HttpServletResponse res, String message) throws ServletException, java.io.IOException {
        req.setAttribute("DD_ERR_MSG", message);
        req.getRequestDispatcher("error.jsp").forward(req, res);
    }

}
