package eionet.meta;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eionet.util.HttpUploader;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.DDConnectionException;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class DocUpload extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocUpload.class);
    public  static final String REQPAR_FILE   = "file";
    public  static final String REQPAR_DSID   = "ds_id";
    public  static final String REQPAR_TITLE  = "title";
    public  static final String REQPAR_IDF    = "idf";
    public  static final String REQPAR_DELETE = "delete";

    private static final String PERM = "du";

    Connection conn = null;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        doPost(req, res);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        try {
            guard(req);

            String dstID = req.getParameter(REQPAR_DSID);
            if (Util.isEmpty(dstID)) {
                throw new ServletException("Missing " + REQPAR_DSID + " request parameter!");
            }

            String del = req.getParameter(REQPAR_DELETE);
            if (!Util.isEmpty(del)) {
                delete(dstID, del);
                res.sendRedirect(req.getContextPath() + "/datasets/" + dstID);
                return;
            }

            String filePath = Props.getProperty(PropsIF.DOC_PATH);
            if (Util.isEmpty(filePath)) {
                throw new ServletException("Missing property: " + PropsIF.DOC_PATH);
            }

            File file = new File(getAbsFilePath(req.getParameter(REQPAR_FILE)));
            HttpUploader.upload(req, file);
            save(dstID, file, req.getParameter(REQPAR_TITLE));
            res.sendRedirect(req.getContextPath() + "/datasets/" + dstID);
        } catch (Exception e) {
            if (e instanceof SQLException) {
                int errCode = ((SQLException) e).getErrorCode();
                LOGGER.warn(new Integer(errCode).toString());
            }
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e.getMessage() == null ? "" : e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    /**
     * @param req
     * @return
     * @throws Exception
     */
    public static String getAbsFilePath(String submittedFilePath) throws Exception {

        String path = Props.getProperty(PropsIF.DOC_PATH);
        if (Util.isEmpty(path)) {
            throw new Exception("Missing property: " + PropsIF.DOC_PATH);
        }

        File f = new File(path, extractFileName(submittedFilePath));
        return f.getAbsolutePath();
    }

    /**
     * Removes the directory structure from a file path.
     *
     * @param submittedFilePath
     *         The path like C:\Windows\registry.reg
     * @return the file name (registry.reg)
     * @throws DDException if the file path is empty or missing
     */
    private static String extractFileName(String submittedFilePath) throws DDException {

        if (Util.isEmpty(submittedFilePath)) {
            throw new DDException("Missing file path!");
        }

        if (submittedFilePath.indexOf("\\") < 0 && submittedFilePath.indexOf("/") < 0) {
            return submittedFilePath;
        } else {
            int i = submittedFilePath.lastIndexOf("\\");
            if (i < 0) {
                i = submittedFilePath.lastIndexOf("/");
            }
            return submittedFilePath.substring(i + 1, submittedFilePath.length());
        }
    }

    /**
     * @param req
     * @throws Exception
     */
    private void guard(HttpServletRequest req) throws Exception {
        DDUser user = SecurityUtil.getUser(req);
        if (user == null) {
            throw new Exception("Not authenticated!");
        }

        String idf = req.getParameter(REQPAR_IDF);
        if (Util.isEmpty(idf)) {
            throw new Exception("Missing " + REQPAR_IDF + " request parameter!");
        }

        if (!SecurityUtil.hasPerm(user, "/datasets/" + idf, "u")) {
            throw new Exception("Not permitted!");
        }
    }

    /**
     * @param dstID
     * @param file
     * @param title
     * @throws Exception
     */
    private void save(String dstID, File file, String title) throws Exception {

        openConnection();

        String legalizedPath = legalizePath(file.getAbsolutePath());

        INParameters inParams = new INParameters();
        LinkedHashMap insertCols = new LinkedHashMap();
        insertCols.put("OWNER_ID", inParams.add(dstID, Types.INTEGER));
        insertCols.put("MD5_PATH", "md5(" + inParams.add(legalizedPath) + ")");
        insertCols.put("ABS_PATH", inParams.add(legalizedPath));

        if (title == null || title.length() == 0) {
            title = file.getName();
        }
        insertCols.put("TITLE", inParams.add(title));

        SQL.preparedStatement(SQL.insertStatement("DOC", insertCols), inParams, conn).executeUpdate();
    }

    /**
     * @param md5
     * @throws DDConnectionException
     * @throws SQLException
     */
    private void delete(String dstId, String md5) throws DDConnectionException, SQLException {

        openConnection();

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            // we might need to delete the physical file as well, so before deleting the row in
            // DOC table, we must query the physical path of the physical file
            INParameters inParams = new INParameters();
            String sqlStr = "select * from DOC where MD5_PATH=" + inParams.add(md5) +
            " and OWNER_TYPE='dst' and OWNER_ID=" + inParams.add(dstId, Types.INTEGER);
            stmt = SQL.preparedStatement(sqlStr, inParams, conn);
            rs = stmt.executeQuery();
            String absPath = null;
            if (rs.next()) {
                absPath = rs.getString("ABS_PATH");
            } else {
                return; // if the above query returned no rows, there's nothing to de here no more
            }

            SQL.close(rs);
            SQL.close(stmt);

            // delete the row in DOC table
            stmt = SQL.preparedStatement("delete from DOC where MD5_PATH=? and OWNER_TYPE='dst' and OWNER_ID=?", inParams, conn);
            stmt.executeUpdate();
            SQL.close(stmt);

            // see if there are any more rows left with the same file path,
            // if no then delete the physically as well
            stmt = conn.prepareStatement("select count(*) from DOC where MD5_PATH=?");
            stmt.setString(1, md5);
            rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                File file = new File(absPath);
                if (file.exists() && !file.isDirectory()) {
                    file.delete();
                }
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     * @throws SQLException
     * @throws DDConnectionException
     *
     */
    private void openConnection() throws DDConnectionException, SQLException {
        if (conn == null) {
            conn = ConnectionUtil.getConnection();
        }
    }

    /**
     *
     */
    private void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqle) {
            } finally {
                conn = null;
            }
        }
    }

    /**
     * @param path
     * @return
     */
    public static String legalizePath(String path) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; path != null && i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '\\') {
                buf.append("\\\\");
            } else {
                buf.append(c);
            }
        }

        return buf.toString();
    }
}
