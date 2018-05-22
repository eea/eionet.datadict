/*
 * Created on 25.04.2006
 *
 */
package eionet.meta.notif;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import eionet.meta.DDUser;
import eionet.util.Props;
import eionet.util.SecurityUtil;
import eionet.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid
 */
public class Subscriber extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -556025189112999203L;

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

    /** */
    public static final String PROP_UNS_EVENTTYPE_PREDICATE = "uns.eventtype.predicate";
    public static final String PROP_UNS_DATASET_PREDICATE = "uns.dataset.predicate";
    public static final String PROP_UNS_TABLE_PREDICATE = "uns.table.predicate";
    public static final String PROP_UNS_COMMONELEM_PREDICATE = "uns.commonelem.predicate";
    public static final String PROP_UNS_REGSTATUS_PREDICATE = "uns.definition-status.predicate";
    public static final String PROP_UNS_XMLRPC_SERVER_URL = "uns.xml.rpc.server.url";
    public static final String PROP_UNS_CHANNEL_NAME = "uns.channel.name";
    public static final String PROP_UNS_SUBSCRIPTIONS_URL = "uns.subscriptions.url";
    public static final String PROP_UNS_USERNAME = "uns.username";
    public static final String PROP_UNS_PASSWORD = "uns.password";
    public static final String PROP_UNS_DISABLED = "uns.isDisabled";
    public static final String PROP_UNS_SUBSCRIBE_FUNC = "uns.make.subsription.function";
    public static final String PROP_UNS_SEND_NOTIFICATION_FUNC = "uns.send.notification.function";

    /** */
    public static final String DATASET_CHANGED_EVENT = "Dataset changed";
    public static final String TABLE_CHANGED_EVENT = "Table changed";
    public static final String COMMON_ELEMENT_CHANGED_EVENT = "Common element changed";
    public static final String REGSTATUS_CHANGED_EVENT = "";

    public static final String NEW_DATASET_EVENT = "New dataset";
    public static final String NEW_TABLE_EVENT = "New table";
    public static final String NEW_COMMON_ELEMENT_EVENT = "New common element";

    /** */
    private static String predEventType = null;
    private static String channelName = null;
    private static String serverURL = null;

    private static String unsUsername = null;
    private static String unsPassword = null;

    private static String unsMakeSubscriptionFunction = null;

    private static Hashtable predicatesMap = null;
    private static Hashtable eventsMap = null;
    private static boolean initialized = false;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init() throws ServletException {
        try {
            if (initialized == false) {
                initialize();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        try {
            // make sure SUCCESS flag is cleared from session,
            // even though it is a responsibility of the one who checks the flag
            req.getSession().removeAttribute("SUCCESS");

            // get user object from session
            DDUser user = SecurityUtil.getUser(req);
            // if no user object found, it means the user's session has
            // expired, so we refresh subscribe.jsp which enables login then
            if (user == null) {
                req.getRequestDispatcher("subscribe.jsp").forward(req, res);
            }
            String username = user.getUserName();
            if (username == null) {
                throw new Exception("User object did not contain the username");
            }

            // set up the filters
            Hashtable<String, String> filter = null;
            Vector<Hashtable<String, String>> filters = new Vector<Hashtable<String, String>>();

            String newDatasets = req.getParameter("new_datasets");
            String newTables = req.getParameter("new_tables");
            String newCommonElems = req.getParameter("new_common_elems");
            if (newDatasets != null) {
                filter = new Hashtable<String, String>();
                filter.put(predEventType, NEW_DATASET_EVENT);
                filters.add(filter);
            }
            if (newTables != null) {
                filter = new Hashtable<String, String>();
                filter.put(predEventType, NEW_TABLE_EVENT);
                filters.add(filter);
            }
            if (newCommonElems != null) {
                filter = new Hashtable<String, String>();
                filter.put(predEventType, NEW_COMMON_ELEMENT_EVENT);
                filters.add(filter);
            }

            @SuppressWarnings("rawtypes")
            Enumeration parNames = req.getParameterNames();
            while (parNames != null && parNames.hasMoreElements()) {

                String parName = (String) parNames.nextElement();
                String pred = (String) predicatesMap.get(parName);
                String event = (String) eventsMap.get(parName);
                if (pred != null && event != null) {
                    String parValue = req.getParameter(parName);
                    if (parValue != null && !parValue.equals("_none_")) {
                        filter = new Hashtable<String, String>();
                        if (parName != null && !parName.equals("reg_status")) {
                            filter.put(predEventType, event);
                        }
                        if (!parValue.equals("_all_")) {
                            filter.put(pred, parValue);
                        }
                        filters.add(filter);
                    }
                }
            }

            // call RPC method
            if (filters.size() > 0) {
                subscribe(Collections.singleton(username), filters);
            }

            req.getSession().setAttribute("SUCCESS", "");
            res.sendRedirect("subscribe.jsp");

        } catch (Exception e) {

            LOGGER.error("Subscription failed: " + e.toString(), e);
            req.setAttribute("DD_ERR_MSG", "Subscription failed!");
            req.setAttribute("DD_ERR_TRC", Util.getStack(e));
            req.setAttribute("DD_ERR_BACK_LINK", "subscribe.jsp");
            req.getRequestDispatcher("error.jsp").forward(req, res);
        }
    }

    /**
     *
     *
     */
    private static void initialize() {

        predEventType = Props.getProperty(PROP_UNS_EVENTTYPE_PREDICATE);
        serverURL = Props.getProperty(PROP_UNS_XMLRPC_SERVER_URL);
        channelName = Props.getProperty(PROP_UNS_CHANNEL_NAME);

        predicatesMap = new Hashtable();
        predicatesMap.put("dataset", Props.getProperty(PROP_UNS_DATASET_PREDICATE));
        predicatesMap.put("table", Props.getProperty(PROP_UNS_TABLE_PREDICATE));
        predicatesMap.put("common_element", Props.getProperty(PROP_UNS_COMMONELEM_PREDICATE));
        predicatesMap.put("reg_status", Props.getProperty(PROP_UNS_REGSTATUS_PREDICATE));

        eventsMap = new Hashtable();
        eventsMap.put("dataset", DATASET_CHANGED_EVENT);
        eventsMap.put("table", TABLE_CHANGED_EVENT);
        eventsMap.put("common_element", COMMON_ELEMENT_CHANGED_EVENT);
        eventsMap.put("reg_status", REGSTATUS_CHANGED_EVENT);

        unsUsername = Props.getProperty(PROP_UNS_USERNAME);
        unsPassword = Props.getProperty(PROP_UNS_PASSWORD);

        unsMakeSubscriptionFunction = Props.getProperty(PROP_UNS_SUBSCRIBE_FUNC);

        initialized = true;
    }

    /**
     *
     * @param users
     * @param filters
     * @throws IOException
     * @throws XmlRpcException
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public static void subscribe(Collection<String> users, Collection<Hashtable<String, String>> filters)
    throws IOException, XmlRpcException {

        // Don't send notifications on Windows platform, because this is most likely a developer machine.
        if (File.separatorChar == '\\') {
            return;
        }

        // If no users and no filters given, then nothing to do here.
        if (users == null || users.isEmpty() || filters == null || filters.isEmpty()) {
            return;
        }

        // Make sure initialization has been done.
        if (initialized == false) {
            initialize();
        }

        // Set up the XML-RPC client object.
        XmlRpcClient client = new XmlRpcClient(serverURL);
        client.setBasicAuthentication(unsUsername, unsPassword);

        // Make subscription per each user.
        for (Object user : users) {

            Vector<Object> params = new Vector<Object>();
            params.add(channelName);
            params.add(user);
            params.add(filters);

            client.execute(unsMakeSubscriptionFunction, params);
        }
    }

    /**
     *
     * @param users
     * @param elmIdentifier
     * @throws SubscribeException
     */
    public static void subscribeToElement(Collection<String> users, String elmIdentifier) throws SubscribeException {

        LOGGER.debug("Subscribing " + users + " to element " + elmIdentifier);

        if (users == null || users.isEmpty() || Util.isEmpty(elmIdentifier)) {
            return;
        }

        if (initialized == false) {
            initialize();
        }

        String predicate = (String) predicatesMap.get("common_element");
        if (predicate != null) {
            Vector<Hashtable<String, String>> filters = new Vector<Hashtable<String, String>>();
            Hashtable<String, String> filter = new Hashtable<String, String>();
            filter.put(predEventType, COMMON_ELEMENT_CHANGED_EVENT);
            filter.put(predicate, elmIdentifier);
            filters.add(filter);
            try {
                subscribe(users, filters);
            } catch (Exception e) {
                throw new SubscribeException("Subscription failed!", e);
            }
            LOGGER.debug("Subscription successful!");
        }
    }

    /**
     *
     * @param users
     * @param dstIdentifier
     * @throws SubscribeException
     */
    public static void subscribeToDataset(Collection<String> users, String dstIdentifier) throws SubscribeException {

        LOGGER.debug("Subscribing " + users + " to dataset " + dstIdentifier);

        if (users == null || users.isEmpty() || Util.isEmpty(dstIdentifier)) {
            return;
        }

        if (initialized == false) {
            initialize();
        }

        String predicate = (String) predicatesMap.get("dataset");
        if (predicate != null) {

            Vector<Hashtable<String, String>> filters = new Vector<Hashtable<String, String>>();
            Hashtable<String, String> filter = new Hashtable<String, String>();
            filter.put(predEventType, DATASET_CHANGED_EVENT);
            filter.put(predicate, dstIdentifier);
            filters.add(filter);
            try {
                subscribe(users, filters);
            } catch (Exception e) {
                throw new SubscribeException("Subscription failed!", e);
            }
            LOGGER.debug("Subscription successful!");
        }
    }

    /**
     *
     * @param users
     * @param tblIdentifier
     * @throws SubscribeException
     */
    public static void subscribeToTable(Collection<String> users, String dstIdentifier, String tblIdentifier)
    throws SubscribeException {

        LOGGER.debug("Subscribing " + users + " to table " + tblIdentifier + " in dataset " + dstIdentifier);

        if (users == null || users.isEmpty() || Util.isEmpty(dstIdentifier) || Util.isEmpty(tblIdentifier)) {
            return;
        }

        if (initialized == false) {
            initialize();
        }

        String predicate = (String) predicatesMap.get("table");
        if (predicate != null) {

            Vector<Hashtable<String, String>> filters = new Vector<Hashtable<String, String>>();
            Hashtable<String, String> filter = new Hashtable<String, String>();
            filter.put(predEventType, TABLE_CHANGED_EVENT);
            filter.put(predicate, dstIdentifier + "/" + tblIdentifier);
            filters.add(filter);
            try {
                subscribe(users, filters);
            } catch (Exception e) {
                throw new SubscribeException("Subscription failed!", e);
            }
            LOGGER.debug("Subscription successful!");
        }
    }
}
