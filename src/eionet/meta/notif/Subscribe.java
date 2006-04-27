/*
 * Created on 25.04.2006
 * 
 */
package eionet.meta.notif;

import java.io.IOException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.tee.xmlserver.*;

import eionet.util.*;

import org.apache.xmlrpc.XmlRpcClient;

/**
 * 
 * @author Jaanus Heinlaid
 */
public class Subscribe extends HttpServlet{
	
	/** */
	public static final String PROP_UNS_EVENTTYPE_PREDICATE = "uns.eventtype.predicate";
	public static final String PROP_UNS_DATASET_PREDICATE = "uns.dataset.predicate";
	public static final String PROP_UNS_TABLE_PREDICATE = "uns.table.predicate";
	public static final String PROP_UNS_COMMONELEM_PREDICATE = "uns.commonelem.predicate";
	public static final String PROP_UNS_XMLRPC_SERVER_URL = "uns.xml.rpc.server.url";
	public static final String PROP_UNS_CHANNEL_NAME = "uns.channel.name";
	public static final String PROP_UNS_SUBSCRIPTIONS_URL = "uns.subscriptions.url";
	public static final String PROP_UNS_USERNAME = "uns.username";
	public static final String PROP_UNS_PASSWORD = "uns.password";
	
	/** */
	public static final String DATASET_CHANGED_EVENT = "Dataset changed";
	public static final String TABLE_CHANGED_EVENT = "Table changed";
	public static final String COMMON_ELEMENT_CHANGED_EVENT = "Common element changed";
	
	public static final String NEW_DATASET_EVENT = "New dataset";
	public static final String NEW_TABLE_EVENT = "New table";
	public static final String NEW_COMMON_ELEMENT_EVENT = "New common element";

	/** */
	public static String predEventType = null;
	public static String channelName = null;
	public static String serverURL = null;
    private Hashtable predsMap = null;
    private Hashtable eventsMap = null;
    private HashSet eventTypes = null;

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init() throws ServletException{
		try{
			
			eventTypes = new HashSet();
			eventTypes.add("Definition changed");
			
            predEventType = Props.getProperty(PROP_UNS_EVENTTYPE_PREDICATE);
            serverURL = Props.getProperty(PROP_UNS_XMLRPC_SERVER_URL);
            channelName = Props.getProperty(PROP_UNS_CHANNEL_NAME);
            
    		predsMap = new Hashtable();
    		predsMap.put("dataset", Props.getProperty(PROP_UNS_DATASET_PREDICATE));
    		predsMap.put("table", Props.getProperty(PROP_UNS_TABLE_PREDICATE));
    		predsMap.put("common_element", Props.getProperty(PROP_UNS_COMMONELEM_PREDICATE));

    		eventsMap = new Hashtable();
    		eventsMap.put("dataset", DATASET_CHANGED_EVENT);
    		eventsMap.put("table", TABLE_CHANGED_EVENT);
    		eventsMap.put("common_element", COMMON_ELEMENT_CHANGED_EVENT);

		}
		catch (Throwable t) {
            t.printStackTrace(System.out);
            throw new ServletException(t);
        }
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest req, HttpServletResponse res)
                                            throws ServletException, IOException {
        
        try {
        	// make sure SUCCESS flag is cleared from session,
        	// even though it is a responsibility of the one who checks the flag
	        req.getSession().removeAttribute("SUCCESS");
	        
	        // get user object from session
			AppUserIF user = SecurityUtil.getUser(req);
			// if no user object found, it means the user's session has
			// expired, so we refresh subscribe.jsp which enables login then
			if (user==null)
				req.getRequestDispatcher("subscribe.jsp").forward(req, res);				
			String username = user.getUserName();
			if (username==null)
				throw new Exception("User object did not contain the username");
			
			// set up the filters
			Hashtable filter;
			Vector filters = new Vector();
			
			String newDatasets = req.getParameter("new_datasets");
			String newTables = req.getParameter("new_tables");
			String newCommonElems = req.getParameter("new_common_elems");
			if (newDatasets!=null){
				filter = new Hashtable();
				filter.put(predEventType, NEW_DATASET_EVENT);
				filters.add(filter);
			}
			if (newTables!=null){
				filter = new Hashtable();
				filter.put(predEventType, NEW_TABLE_EVENT);
				filters.add(filter);
			}
			if (newCommonElems!=null){
				filter = new Hashtable();
				filter.put(predEventType, NEW_COMMON_ELEMENT_EVENT);
				filters.add(filter);
			}
			
			Enumeration parNames = req.getParameterNames();
			while (parNames!=null && parNames.hasMoreElements()){
				
				String parName = (String)parNames.nextElement();
				String pred = (String)predsMap.get(parName);
				String event = (String)eventsMap.get(parName);
				if (pred!=null && event!=null){
					String parValue = req.getParameter(parName);
					if (parValue!=null && !parValue.equals("_none_")){
						filter = new Hashtable();
						filter.put(predEventType, event);
						if (!parValue.equals("_all_")){
							filter.put(pred, parValue);
						}
						filters.add(filter);
					}
				}
			}
			
			// DEBUG
			//logFilters(filters);
			
			// call RPC method
	        if (filters.size()>0){
		        	
				// set up the xml-rpc server object
	        	XmlRpcClient server = new XmlRpcClient(serverURL);
				server.setBasicAuthentication(Props.getProperty(PROP_UNS_USERNAME),
						Props.getProperty(PROP_UNS_PASSWORD));
	        	
	            // make subscription
				Vector params = new Vector();
				params = new Vector();
	            params.add(channelName);
	            params.add(username);
	            params.add(filters);            
	            String makeSubscription =
	            (String) server.execute("makeSubscription", params);
	
	        }
	        
	        req.getSession().setAttribute("SUCCESS", "");
	        res.sendRedirect("subscribe.jsp");
	        
        }
        catch (Throwable t) {
            t.printStackTrace(System.out);
            String msg = t.getMessage();
            if (msg==null) msg = t.toString();
			req.setAttribute("DD_ERR_MSG", msg);
			req.setAttribute("DD_ERR_BACK_LINK", "subscribe.jsp");
			req.getRequestDispatcher("error.jsp").forward(req, res);
        }
    }
	
	/*
	 * 
	 */
	private void logFilters(Vector filters){
		
		for (int i=0; filters!=null && i<filters.size(); i++){
			Hashtable filter = (Hashtable)filters.get(i);
			System.out.println("========================= filter " + i);
			System.out.println(filter);
		}
	}
}
