package eionet.meta;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import eionet.meta.exports.rdf.Rdf;
import eionet.util.DDServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class TablesServlet extends HttpServlet {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(TablesServlet.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Entered request: " + request.getRequestURL());
        }

        // If path info is blank or equals to "/", just forward to the list of all tables
        String pathInfo = request.getPathInfo();
        if (StringUtils.isBlank(pathInfo) || pathInfo.trim().equals("/")) {
            request.getRequestDispatcher("/search_results_tbl.jsp").forward(request, response);
            return;
        }

        String[] pathInfoSegments = StringUtils.split(pathInfo, "/");

        // If the path has only one segment and its "rdf" or "add",
        // then forward to the manifest-RDF of all tables or to add-table-to-dataset page respectively.
        if (pathInfoSegments.length == 1) {

            RequestDispatcher requestDispatcher = null;
            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            if (pathInfoSegments[0].equals("rdf")) {
                wrappedRequest.addParameterValue("type", Rdf.TABLE_TYPE);
                requestDispatcher = wrappedRequest.getRequestDispatcher("/GetRdf");
            } else if (pathInfoSegments[0].equals("add")) {
                wrappedRequest.addParameterValue("mode", "add");
                requestDispatcher = wrappedRequest.getRequestDispatcher("/dstable.jsp");
            }

            if (requestDispatcher != null) {
                requestDispatcher.forward(wrappedRequest, response);
                return;
            }
        }

        // At this point we know the array is not empty, and its first segment identifies a table.
        String tableId = pathInfoSegments[0];

        // The default event is "view", overridden by the path's 2nd segment if such exists.
        String event = "view";
        if (pathInfoSegments.length>1) {
            event = pathInfoSegments[1];
        }

        // If event is "rdf", forward to the RDF-generating servlet
        if (event.equals("rdf")) {

            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("id", tableId);
            wrappedRequest.addParameterValue("type", Rdf.TABLE_TYPE);
            wrappedRequest.getRequestDispatcher("/GetRdf").forward(wrappedRequest, response);
        } else {
            // Make sure that the event and table id detected from the path info
            // will be added as query parameters to the wrapped request.
            // If the event is "subscribe", add "action=subscribe" query parameter
            // and set event to "view".

            DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
            wrappedRequest.addParameterValue("table_id", tableId);

            if (event.equals("subscribe")) {
                wrappedRequest.addParameterValue("mode", "view");
                wrappedRequest.addParameterValue("action", event);
            } else {
                wrappedRequest.addParameterValue("mode", event);
            }

            RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher("/dstable.jsp");
            requestDispatcher.forward(wrappedRequest, response);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
        RequestDispatcher requestDispatcher = wrappedRequest.getRequestDispatcher("/dstable.jsp");
        requestDispatcher.forward(wrappedRequest, response);
    }
}
