package eionet.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.eteks.awt.PJAToolkit;

import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class ImgUpload extends HttpServlet {

    private static final int BUF_SIZE = 1024;
    private static final String QRYSTR_ATTR = "imgattr_qrystr";

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {

        doPost(req, res);
    }

    /**
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws java.io.IOException
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {

        req.setCharacterEncoding("UTF-8");

        Connection conn = null;

        res.setContentType("text/html");
        ServletContext ctx = getServletContext();
        HttpSession session = req.getSession();
        String qryStr = (String) session.getAttribute(QRYSTR_ATTR);
        if (Util.voidStr(qryStr))
            qryStr = "index.jsp";
        else
            session.removeAttribute(QRYSTR_ATTR);

        // authenticate user
        DDUser user = SecurityUtil.getUser(req);

        if (user == null)
            throw new ServletException("User not authenticated!");

        String objID = req.getParameter("obj_id");
        if (Util.voidStr(objID))
            throw new ServletException("Object ID is not specified!");

        String objType = req.getParameter("obj_type");
        if (Util.voidStr(objType))
            throw new ServletException("Object type not specified!");

        String attrID = req.getParameter("attr_id");
        if (Util.voidStr(attrID))
            throw new ServletException("Attribute ID not specified!");

        // get the file's physical path
        String fileStorePath = Props.getRequiredProperty(PropsIF.FILESTORE_PATH);
        String visualsPath = new File(fileStorePath, "visuals").toString() + File.separator;

        // check mode
        String mode = req.getParameter("mode");

        // HANDLE REMOVE
        if (mode != null && mode.equals("remove")) {

            String[] fileNames = req.getParameterValues("file_name");
            if (fileNames == null || fileNames.length == 0)
                throw new ServletException("No images selected!");

            String sqlStr = "delete from ATTRIBUTE where M_ATTRIBUTE_ID=? and DATAELEM_ID=? and PARENT_TYPE=? and VALUE=?";

            PreparedStatement stmt = null;
            try {
                conn = ConnectionUtil.getConnection();
                stmt = conn.prepareStatement(sqlStr);
                for (int i = 0; i < fileNames.length; i++) {

                    INParameters inParams = new INParameters();
                    inParams.add(attrID, Types.INTEGER);
                    inParams.add(objID, Types.INTEGER);
                    inParams.add(objType);
                    inParams.add(fileNames[i]);

                    SQL.populate(stmt, inParams);
                    stmt.executeUpdate();
                }

            } catch (Exception e) {
                throw new ServletException(e.toString());
            } finally {
                try {
                    if (stmt != null)
                        stmt.close();
                    if (conn != null)
                        conn.close();
                } catch (SQLException e) {
                }
            }

            for (int i = 0; i < fileNames.length; i++) {
                if (!Util.voidStr(fileNames[i])) {
                    File file = new File(visualsPath + fileNames[i]);
                    if (file.exists())
                        file.delete();
                }
            }

            res.sendRedirect("imgattr.jsp?" + qryStr);

            return;
        }

        // READY TO UPLOAD THE FILE

        // check the request content type
        String contentType = req.getContentType();
        if (contentType == null || !(contentType.toLowerCase().startsWith("multipart/form-data")))
            throw new ServletException("Posted content type is unknown!");

        String boundary = extractBoundary(contentType);

        // get the image file name

        String sUrl = null;
        String sFile = null;

        String fileORurl = req.getParameter("fileORurl");
        if (fileORurl != null && fileORurl.equalsIgnoreCase("url")) {
            sUrl = req.getParameter("url_input");
        } else if (fileORurl != null && fileORurl.equalsIgnoreCase("file")) {
            sFile = req.getParameter("file_input");
        }

        if (Util.voidStr(sFile) && Util.voidStr(sUrl))
            throw new ServletException("You have to specify at least a file or url!");

        String fileName = null;
        if (sUrl != null) {
            int i = sUrl.lastIndexOf("/");
            fileName = sUrl.substring(i + 1, sUrl.length());
        } else {
            int i = sFile.lastIndexOf("\\");
            if (i == -1)
                i = sFile.lastIndexOf("/");
            fileName = sFile.substring(i + 1, sFile.length());
        }

        if (Util.voidStr(fileName)){
            throw new ServletException("Failed to extract the file name!");
        }

        try {
            // set up the file to write to

            File file = new File(visualsPath + fileName);
            if (file.exists()) {
                String msg = "A file with such a name already " + "exists! Choose another name.";
                req.setAttribute("DD_ERR_MSG", msg);
                req.setAttribute("DD_ERR_BACK_LINK", "imgattr.jsp?" + qryStr);
                req.getRequestDispatcher("error.jsp").forward(req, res);
                return;
            }

            file.getParentFile().mkdirs();
            RandomAccessFile raFile = new RandomAccessFile(file, "rw");

            // set up the stream to read from and call file writer

            InputStream in = null;
            if (sUrl != null) {
                URL url = new URL(sUrl);
                writeToFile(raFile, url.openStream());
            } else
                writeToFile(raFile, req.getInputStream(), boundary);

            // We seem to have successfully uploaded the file.
            // However, we must be srue that PJAToolkit can handle
            // the file later when inserting into generated PDF
            try {
                checkPJA(visualsPath + fileName);
            } catch (Exception e) {
                req.setAttribute("DD_ERR_MSG", e.getMessage());
                req.setAttribute("DD_ERR_BACK_LINK", "imgattr.jsp?" + qryStr);
                req.getRequestDispatcher("error.jsp").forward(req, res);
                return;
            }

            // Now let's store the image relation in the DB as well.
            conn = ConnectionUtil.getConnection();

            INParameters inParams = new INParameters();
            LinkedHashMap map = new LinkedHashMap();
            map.put("M_ATTRIBUTE_ID", inParams.add(attrID, Types.INTEGER));
            map.put("DATAELEM_ID", inParams.add(objID, Types.INTEGER));
            map.put("PARENT_TYPE", inParams.add(objType));
            map.put("VALUE", inParams.add(fileName));

            PreparedStatement stmt = null;
            try {
                stmt = SQL.preparedStatement(SQL.insertStatement("ATTRIBUTE", map), inParams, conn);
                stmt.executeUpdate();
            } finally {
                try {
                    if (stmt != null)
                        stmt.close();
                } catch (SQLException e) {
                }
            }
        } catch (Exception e) {
            throw new ServletException(e.toString());
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
            }
        }

        res.sendRedirect("imgattr.jsp?" + qryStr);
    }

    /**
     *
     * @param raFile
     * @param in
     * @throws Exception
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

    private void writeToFile(RandomAccessFile raFile, ServletInputStream in, String boundary) throws Exception {

        byte[] buf = new byte[BUF_SIZE];
        int i;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        boolean fileStart = false;
        boolean pastContentType = false;
        do {
            int b = in.read();
            if (b == -1)
                break; // if end of stream, break

            bout.write(b);

            if (!pastContentType) { // if Content-Type not passed, no check of LNF
                String s = bout.toString();
                if (s.indexOf("Content-Type") != -1)
                    pastContentType = true;
            } else {
                // Content-Type is passed, after next double LNF is file start
                byte[] bs = bout.toByteArray();
                if (bs != null && bs.length >= 4) {
                    if (bs[bs.length - 1] == 10 && bs[bs.length - 2] == 13 && bs[bs.length - 3] == 10 && bs[bs.length - 4] == 13) {

                        fileStart = true;
                    }
                }
            }
        } while (!fileStart);

        while ((i = in.readLine(buf, 0, buf.length)) != -1) {
            String line = new String(buf, 0, i);
            if (boundary != null && line.startsWith(boundary))
                break;
            raFile.write(buf, 0, i);
        }

        raFile.close();
        in.close();
    }

    private String extractBoundary(String contentType) {
        int i = contentType.indexOf("boundary=");
        if (i == -1)
            return null;
        String boundary = contentType.substring(i + 9); // 9 for "boundary="
        return "--" + boundary; // the real boundary is always preceded by an extra "--"
    }

    public static void checkPJA(String filePath) throws Exception {
        // get old properties
        String propToolkit = System.getProperty("awt.toolkit");
        String propGraphics = System.getProperty("java.awt.graphicsenv");
        String propFonts = System.getProperty("java.awt.fonts");

        // set new properties
        System.setProperty("awt.toolkit", "com.eteks.awt.PJAToolkit");
        System.setProperty("java.awt.graphicsenv", "com.eteks.java2d.PJAGraphicsEnvironment");
        System.setProperty("java.awt.fonts", System.getProperty("user.dir"));

        java.awt.Image jImg = null;
        try {
            PJAToolkit kit = new PJAToolkit();
            // create java.awt.Image
            jImg = kit.createImage(filePath);
            if (jImg == null)
                throw new Exception();
            // of the java.awt.Image, create com.lowagie.text.Image
            com.lowagie.text.Image vsImage = com.lowagie.text.Image.getInstance(jImg, null);
            if (vsImage == null)
                throw new Exception();
        } catch (Exception e) {

            // reset old properties
            if (propToolkit != null)
                System.setProperty("awt.toolkit", propToolkit);
            if (propGraphics != null)
                System.setProperty("java.awt.graphicsenv", propGraphics);
            if (propFonts != null)
                System.setProperty("java.awt.fonts", propFonts);

            throw new Exception("Failed to recognize the image!" + " Make sure it's a JPG, GIF or PNG");
        }
    }

    public static void main(String[] args) {

    }
}

/*
 * class PJAThread extends Thread{
 *
 * private String absPath = null; private boolean wasOK = false;
 *
 * PJAThread(String absPath) { super(); this.absPath = absPath; }
 *
 * public void run() {
 *
 * try { ImgUpload.checkPJA(absPath); wasOK = true; } catch (Exception e) { File file = new File(absPath); file.renameTo(new
 * File(absPath + ".rmv")); } }
 *
 * public boolean success() { return wasOK; } }
 */
