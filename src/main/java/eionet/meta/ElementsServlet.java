package eionet.meta;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import eionet.meta.exports.rdf.Rdf;
import eionet.util.DDServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class ElementsServlet extends HttpServlet {

    /** */
    private static final String DATA_ELEMENT_JSP = "/data_element.jsp";
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementsServlet.class);

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Entered request: " + request.getRequestURL());
        }

        // If path info is blank or equals to "/", just forward to the data elements search page
        String pathInfo = request.getPathInfo();
        if (StringUtils.isBlank(pathInfo) || pathInfo.trim().equals("/")) {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
            return;
        }

        String[] pathInfoSegments = StringUtils.split(pathInfo, "/");
        if (pathInfoSegments[0].equals("latest")) {

            // a latest common element definition is requested (i.e. by its alfa-numeric identifier)
            handleRequestForLatest(request, response, pathInfoSegments);
            return;
        } else if (pathInfoSegments[0].equals("add")) {

            // a request for adding a new element
            handleRequestForAdd(request, response, pathInfoSegments);
        } else if (NumberUtils.toInt(pathInfoSegments[0]) > 0) {

            // a request specific to a particular element (i.e. by its auto-generated identifier)
            handleRequestForParticular(request, response, pathInfoSegments);
        } else {
            throw new DDRuntimeException("Request not supported: " + request.getRequestURL());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher(DATA_ELEMENT_JSP);
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
    private void handleRequestForAdd(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments)
            throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.addParameterValue("mode", "add");
        wrappedRequest.getRequestDispatcher(DATA_ELEMENT_JSP).forward(wrappedRequest, response);
    }

    /**
     *
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestForLatest(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments)
            throws ServletException, IOException {

        String elmIdentifier = pathInfoSegments.length > 1 ? pathInfoSegments[1] : null;
        if (elmIdentifier == null) {
            throw new DDRuntimeException("Missing data element identifier in path info!");
        }

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.addParameterValue("element_idf", elmIdentifier);
        wrappedRequest.getRequestDispatcher(DATA_ELEMENT_JSP).forward(wrappedRequest, response);
    }

    /**
     *
     * @param request
     * @param response
     * @param pathInfoSegments
     * @throws IOException
     * @throws ServletException
     */
    private void handleRequestForParticular(HttpServletRequest request, HttpServletResponse response, String[] pathInfoSegments)
            throws ServletException, IOException {

        String elementId = pathInfoSegments[0];
        String event = "view";
        if (pathInfoSegments.length > 1) {
            event = pathInfoSegments[1];
        }

        // If event is "rdf", forward to the RDF-generating servlet
        if (event.equals("rdf")) {
            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("id", elementId);
            wrappedRequest.addParameterValue("type", Rdf.CODE_LIST_TYPE);
            wrappedRequest.getRequestDispatcher("/GetRdf").forward(wrappedRequest, response);
        }

        // Make sure that the event and element id detected from the path info
        // will be added as query parameters to the wrapped request.
        // If the event is one of "subscribe", "checkout" or "newversion", set
        // it as "action" parameter instead.

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        wrappedRequest.addParameterValue("delem_id", elementId);

        if (event.equals("subscribe") || event.equals("checkout") || event.equals("newversion")) {
            wrappedRequest.addParameterValue("mode", "view");
            wrappedRequest.addParameterValue("action", event);
        } else {
            wrappedRequest.addParameterValue("mode", event);
        }

        RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher(DATA_ELEMENT_JSP);
        requestDispatcher.forward(wrappedRequest, response);
    }
}
