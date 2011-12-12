package eionet.meta;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.util.DDServletRequestWrapper;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class DatasetsServlet extends HttpServlet{

    /** */
    private static final Logger LOGGER = Logger.getLogger(DatasetsServlet.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (LOGGER.isTraceEnabled()){
            LOGGER.trace("Entered request: " + request.getRequestURL());
        }

        // If path info is blank or equals to "/", just forward to the list of all tables
        String pathInfo = request.getPathInfo();
        if (StringUtils.isBlank(pathInfo) || pathInfo.trim().equals("/")) {
            request.getRequestDispatcher("/datasets.jsp").forward(request, response);
            return;
        }

        String[] pathInfoSegments = StringUtils.split(pathInfo, "/");

        if (pathInfoSegments[0].equals("latest")){

            String datasetIdentifier = pathInfoSegments.length>1 ? pathInfoSegments[1] : null;
            if (datasetIdentifier==null){
                throw new DDRuntimeException("Missing datast identifier in path info!");
            }

            String tableIdentifier = null;
            if (pathInfoSegments.length>2 && pathInfoSegments[2].equals("tables")){
                tableIdentifier = pathInfoSegments.length>3 ? pathInfoSegments[3] : null;
                if (tableIdentifier==null){
                    throw new DDRuntimeException("Missing table identifier in path info!");
                }
            }

            if (tableIdentifier!=null){
                DDServletRequestWrapper wrappedRequest = new DDServletRequestWrapper(request);
                wrappedRequest.addParameterValue("table_idf", tableIdentifier);
                wrappedRequest.addParameterValue("dataset_idf", datasetIdentifier);
                wrappedRequest.getRequestDispatcher("/dstable.jsp").forward(wrappedRequest, response);
                return;
            }
            else{
                throw new DDRuntimeException("Request not supported: " + request.getRequestURL());
            }
        }
        else{
            throw new DDRuntimeException("Request not supported: " + request.getRequestURL());
        }
    }
}
