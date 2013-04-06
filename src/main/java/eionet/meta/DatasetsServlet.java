package eionet.meta;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import eionet.meta.exports.rdf.RdfServlet;
import eionet.util.DDServletRequestWrapper;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class DatasetsServlet extends HttpServlet{

    private static final String DATASET_JSP = "/dataset.jsp";
    /** */
    private static final Logger LOGGER = Logger.getLogger(DatasetsServlet.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Entered request: " + request.getRequestURL());
        }

        // If path info is blank or equals to "/", just forward to the list of all datasets
        String pathInfo = request.getPathInfo();
        if (StringUtils.isBlank(pathInfo) || pathInfo.trim().equals("/")) {
            request.getRequestDispatcher("/datasets.jsp").forward(request, response);
            return;
        }

        String[] pathInfoSegments = StringUtils.split(pathInfo, "/");
        if (pathInfoSegments[0].equals("latest")) {

            // a latest dataset definition is requested (i.e. by its alfa-numeric identifier)
            handleRequestForLatest(request, response, pathInfoSegments);
            return;
        }
        else if (pathInfoSegments[0].equals("rdf")) {

            // RDF of all datasets is requested
            handleRequestForAllRdf(request, response, pathInfoSegments);
            return;
        }
        else if (pathInfoSegments[0].equals("add")) {

            // a request for adding a new dataset
            handleRequestForAdd(request, response, pathInfoSegments);
        }
        else if (NumberUtils.toInt(pathInfoSegments[0]) > 0) {

            // a request specific to a particular dataset (i.e. by its auto-generated identifier)
            handleRequestForParticular(request, response, pathInfoSegments);
        }
        else{
            throw new DDRuntimeException("Request not supported: " + request.getRequestURL());
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher(DATASET_JSP);
        requestDispatcher.forward(wrappedRequest, response);
    }

    /**
     *
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestForAdd(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments) throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.addParameterValue("mode", "add");
        wrappedRequest.getRequestDispatcher(DATASET_JSP).forward(wrappedRequest, response);
    }

    /**
     *
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestForAllRdf(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments) throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.getRequestDispatcher(RdfServlet.URL_MAPPING).forward(wrappedRequest, response);
    }

    /**
     *
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws ServletException
     * @throws IOException
     */
    private void handleRequestForParticular(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments)
    throws ServletException, IOException {

        String datasetId = pathInfoSegments[0];
        String event = "view";
        if (pathInfoSegments.length>1) {
            event = pathInfoSegments[1];
        }

        // Make sure that the event and dataset id detected from the path info
        // will be added as query parameters to the wrapped request.
        // If the event is one of "subscribe", "checkout" or "newversion", set
        // it as "action" parameter instead.

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.addParameterValue("ds_id", datasetId);

        if (event.equals("subscribe") || event.equals("checkout") || event.equals("newversion")) {
            wrappedRequest.addParameterValue("mode", "view");
            wrappedRequest.addParameterValue("action", event);
        }
        else{
            wrappedRequest.addParameterValue("mode", event);
        }

        RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher(DATASET_JSP);
        requestDispatcher.forward(wrappedRequest, response);
    }

    /**
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws ServletException
     * @throws IOException
     */
    private void handleRequestForLatest(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments)
    throws ServletException, IOException {

        String datasetIdentifier = pathInfoSegments.length > 1 ? pathInfoSegments[1] : null;
        if (datasetIdentifier == null) {
            throw new DDRuntimeException("Missing datast identifier in path info!");
        }

        String tableIdentifier = null;
        if (pathInfoSegments.length > 2 && pathInfoSegments[2].equals("tables")) {
            tableIdentifier = pathInfoSegments.length > 3 ? pathInfoSegments[3] : null;
            if (tableIdentifier == null) {
                throw new DDRuntimeException("Missing table identifier in path info!");
            }
        }

        String elementIdentifier = null;
        if (pathInfoSegments.length > 4 && pathInfoSegments[4].equals("elements")) {
            elementIdentifier = pathInfoSegments.length > 5 ? pathInfoSegments[5] : null;
            if (elementIdentifier == null) {
                throw new DDRuntimeException("Missing element identifier in path info!");
            }
        }

        if (elementIdentifier != null && tableIdentifier != null) {
            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("element_idf", elementIdentifier);
            wrappedRequest.addParameterValue("table_idf", tableIdentifier);
            wrappedRequest.addParameterValue("dataset_idf", datasetIdentifier);
            wrappedRequest.getRequestDispatcher("/data_element.jsp").forward(wrappedRequest, response);
        } else if (tableIdentifier != null) {
            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("table_idf", tableIdentifier);
            wrappedRequest.addParameterValue("dataset_idf", datasetIdentifier);
            wrappedRequest.getRequestDispatcher("/dstable.jsp").forward(wrappedRequest, response);
        } else {
            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("dataset_idf", datasetIdentifier);
            wrappedRequest.getRequestDispatcher(DATASET_JSP).forward(wrappedRequest, response);
        }
    }
}
