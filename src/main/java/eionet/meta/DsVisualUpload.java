package eionet.meta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.tee.util.Util;

import eionet.meta.savers.DatasetHandler;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.SecurityUtil;
import eionet.util.sql.ConnectionUtil;
import eionet.util.sql.SQL;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class DsVisualUpload extends HttpServlet {

    /** */
    private static final Logger LOGGER = Logger.getLogger(DsVisualUpload.class);

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, java.io.IOException {

        doPost(req, res);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {

        try {
            processRequest(request, response);
        } catch (ServletException servletException) {
            LOGGER.error(servletException);
            throw servletException;
        } catch (IOException ioException) {
            LOGGER.error(ioException);
            throw ioException;
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @SuppressWarnings("rawtypes")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
    java.io.IOException {

        LOGGER.debug("Entered " + this.getClass().getSimpleName() + ".doPost()");

        // Authenticate user.
        DDUser user = SecurityUtil.getUser(request);
        if (user == null) {
            throw new ServletException("User not authenticated!");
        }

        // Get dataset ID from request
        String dstId = request.getParameter("ds_id");
        if (Util.nullString(dstId)) {
            throw new ServletException("Dataset ID is not specified!");
        }

        // Get the visual file's type (i.e. whether it's the detailed or general model).
        String visualType = request.getParameter("str_type");
        if (Util.nullString(visualType)) {
            throw new ServletException("Structure type not specified!");
        }

        // Check is this is a removal or add/replace.
        String mode = request.getParameter("mode");
        if (mode != null && mode.equals("remove")) {

            LOGGER.debug("Going to remove " + visualType + " visual of dataset " + dstId);

            // We're in removal mode, so execute removal.
            processRemoval(dstId, visualType, request);
        } else {

            LOGGER.debug("Going to add/replace " + visualType + " visual of dataset " + dstId);

            // We're doing an add/replace, so try to obtain the file.
            File storedFile = obtainFile(request);
            if (storedFile == null) {
                throw new ServletException("Could not obtain file from request!");
            }

            LOGGER.debug("Updating dataset record in the database...");

            // update the dataset record in the database.
            saveDatasetVisual(dstId, visualType, storedFile);
        }

        // redirect to the view page of this dataset's visuals
        String redirectUrl = "dsvisual.jsp?ds_id=" + dstId + "&str_type=" + visualType;
        LOGGER.debug("Request processed, redirecting to " + redirectUrl);
        response.sendRedirect(redirectUrl);

    }

    /**
     * @param dsID
     * @param strType
     * @param storedFile
     * @throws ServletException
     */
    private void saveDatasetVisual(String dsID, String strType, File storedFile) throws ServletException {

        eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
        pars.addParameterValue("mode", "edit");
        pars.addParameterValue("ds_id", dsID);
        pars.addParameterValue("str_type", strType);
        pars.addParameterValue("visual", storedFile.getName());
        Connection conn = null;
        try {
            conn = ConnectionUtil.getConnection();
            DatasetHandler dsHandler = new DatasetHandler(conn, pars, getServletContext());
            dsHandler.execute();
        } catch (Exception e) {
            throw new ServletException(e.toString(), e);
        }
    }

    /**
     * @param request
     * @return
     * @throws ServletException
     */
    private File obtainFile(HttpServletRequest request) throws ServletException {

        File storedFile = null;

        String urlInput = request.getParameter("url_input");
        String fileInput = request.getParameter("file_input");
        if (!Util.nullString(urlInput) && !Util.nullString(fileInput)) {
            throw new ServletException("Either local file or URL must be supplied- both must not be specififed!");
        } else if (!Util.nullString(urlInput)) {
            LOGGER.debug("File to be downloaded from " + urlInput);
            storedFile = processDownload(urlInput);
        } else if (!Util.nullString(fileInput)) {
            LOGGER.debug("Local file that was uploaded: " + fileInput);
            storedFile = processUpload(request);
        } else {
            throw new ServletException("At least local file or URL must be specififed!");
        }

        return storedFile;
    }

    /**
     *
     * @param request
     * @return
     * @throws ServletException
     */
    @SuppressWarnings("rawtypes")
    private File processUpload(HttpServletRequest request) throws ServletException {

        // Check if multipart request.
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new ServletException("Not a multipart request, even though file input has been specified!");
        }

        DiskFileItemFactory factory = new DiskFileItemFactory(0, getTemporaryStorePath());
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(5000000);

        List items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            throw new ServletException(e.toString(), e);
        }

        File storedFile = null;
        if (items != null && !items.isEmpty()) {

            Iterator iter = items.iterator();
            while (iter.hasNext()) {

                FileItem item = (FileItem) iter.next();
                if (!item.isFormField()) {

                    try {
                        long sizeInBytes = item.getSize();
                        if (sizeInBytes <= 0) {
                            throw new ServletException("The uploaded file was found empty!");
                        } else {
                            storedFile = new File(getVisualsStorePath(), item.getName());
                            LOGGER.debug("Storing uploaded file to " + storedFile);
                            storedFile.getParentFile().mkdirs();
                            try {
                                item.write(storedFile);
                            } catch (Exception e) {
                                throw new ServletException(e.toString(), e);
                            }
                        }
                    } finally {
                        // in any case, delete the temporarily created file
                        LOGGER.debug("Deleting temporary file(s)...");
                        item.delete();
                    }

                    // we expect only one file to be uploaded, so break the loop if one found
                    break;
                }
            }
        } else {
            LOGGER.debug("No uploaded files found in the request!");
        }

        return storedFile;
    }

    /**
     *
     * @param dstId
     * @param visualType
     * @param request
     * @throws ServletException
     */
    private void processRemoval(String dstId, String visualType, HttpServletRequest request) throws ServletException {

        // sanity checking
        String existingFileName = request.getParameter("visual");
        if (Util.nullString(existingFileName)) {
            throw new ServletException("Currently existing file name is missing from the request!");
        }

        LOGGER.debug("Currently existing file name: " + existingFileName);

        // update the dataset's record in the database
        Connection conn = null;
        try {
            eionet.meta.savers.Parameters pars = new eionet.meta.savers.Parameters();
            pars.addParameterValue("mode", "edit");
            pars.addParameterValue("ds_id", dstId);
            pars.addParameterValue("visual", "NULL");
            pars.addParameterValue("str_type", visualType);
            conn = ConnectionUtil.getConnection();
            DatasetHandler dsHandler = new DatasetHandler(conn, pars, getServletContext());
            dsHandler.execute();
        } catch (Exception e) {
            throw new ServletException(e.toString(), e);
        } finally {
            SQL.close(conn);
        }

        LOGGER.debug("Dataset visual removed in the database, deleting from file system too...");

        // delete the file from the file system
        File file = new File(getVisualsStorePath(), existingFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     *
     * @param urlString
     * @return
     * @throws ServletException
     */
    private File processDownload(String urlString) throws ServletException {

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new ServletException("Malformed URL: " + urlString);
        }

        String fileName = new File(url.getPath()).getName();
        if (Util.nullString(fileName)) {
            throw new ServletException("Could not extract file name from this URL: " + urlString);
        }

        LOGGER.debug("File name extracted from URL: " + fileName);

        File tempFile = new File(getTemporaryStorePath(), fileName);
        File destFile = new File(getVisualsStorePath(), fileName);

        InputStream input = null;
        OutputStream output = null;
        try {
            input = url.openStream();
            output = new FileOutputStream(tempFile);
            IOUtils.copy(input, output);

            // need to close output stream already here, otherwise rename will not work later
            output.close();

            LOGGER.debug("File sucessfully downloaded into" + tempFile + ", renaming to " + destFile);

            boolean success = tempFile.renameTo(destFile);
            if (!success) {
                throw new ServletException("Failed to rename " + tempFile + " to " + destFile);
            }
        } catch (IOException e) {
            throw new ServletException(e.toString(), e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }

        return destFile;
    }

    /**
     *
     * @return
     */
    private File getVisualsStorePath() {

        return new File(Props.getRequiredProperty(PropsIF.FILESTORE_PATH), "visuals");
    }

    /**
     *
     * @return
     */
    private File getTemporaryStorePath() {

        return new File(Props.getRequiredProperty(PropsIF.TEMP_FILE_PATH));
    }
}
