/*
 * Created on 26.04.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.notif;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClient;

import eionet.meta.DDRuntimeException;
import eionet.meta.DataElement;
import eionet.meta.Dataset;
import eionet.meta.DsTable;
import eionet.meta.notif.util.RDFTriple;
import eionet.meta.notif.util.XmlRpcCallThread;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;

/**
 * @author jaanus
 */
public class UNSEventSender {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UNSEventSender.class);

    /** */
    public static final String PROP_UNS_EVENTS_NAMESPACE = "uns.events-namespace";
    public static final String PROP_UNS_USER_PREDICATE = "uns.user.predicate";
    public static final String PROP_UNS_DEFINITION_URL_PREDICATE = "uns.definition-url.predicate";
    public static final String PROP_UNS_DEFINITION_STATUS_PREDICATE = "uns.definition-status.predicate";

    /**
     *
     * @param elm
     * @param eventType
     * @param user
     */
    public void definitionChanged(DataElement elm, String eventType, String user) {

        if (elm == null || eventType == null) {
            return;
        }

        String elmIdfier = elm.getIdentifier();
        String elmURL = elm.getReferenceURL();
        String elmStatus = elm.getStatus();
        boolean isCommon = elm.getNamespace() == null || elm.getNamespace().getID() == null;
        if (elmIdfier == null && elmURL == null) {
            return;
        }

        Hashtable predicateObjects = new Hashtable();
        Vector objects = null;

        if (elmIdfier != null) {
            objects = new Vector();
            objects.add(elmIdfier);
            predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_COMMONELEM_PREDICATE), objects);
        }

        if (elmURL != null) {
            objects = new Vector();
            objects.add(elmURL);
            predicateObjects.put(Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
        }

        if (isCommon && elmStatus != null) {
            objects = new Vector();
            objects.add(elmStatus);
            predicateObjects.put(Props.getProperty(PROP_UNS_DEFINITION_STATUS_PREDICATE), objects);
        }

        if (user != null) {
            objects = new Vector();
            objects.add(user);
            predicateObjects.put(Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
        }

        objects = new Vector();
        objects.add(eventType);
        predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE), objects);

        objects = new Vector();
        StringBuffer buf = new StringBuffer("DD ");
        buf.append(eventType);
        if (elmIdfier != null) {
            buf.append(" ").append(elmIdfier);
        }
        objects.add(buf.toString());
        predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

        sendEvent(predicateObjects, user);
    }

    /**
     *
     * @param tbl
     * @param eventType
     * @param user
     */
    public void definitionChanged(DsTable tbl, String eventType, String user) {

        if (tbl == null || eventType == null) {
            return;
        }

        String tblIdfier = tbl.getIdentifier();
        String tblURL = tbl.getReferenceURL();
        if (tblIdfier == null && tblURL == null) {
            return;
        }

        // the identifier sent to UNS must be like dataset_identifer/table_identifer
        String dstIdfier = tbl.getDstIdentifier();
        if (dstIdfier != null) {
            tblIdfier = dstIdfier + "/" + tblIdfier;
        }

        Hashtable predicateObjects = new Hashtable();
        Vector objects = null;

        if (tblIdfier != null) {
            objects = new Vector();
            objects.add(tblIdfier);
            predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_TABLE_PREDICATE), objects);
        }

        if (tblURL != null) {
            objects = new Vector();
            objects.add(tblURL);
            predicateObjects.put(Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
        }

        if (user != null) {
            objects = new Vector();
            objects.add(user);
            predicateObjects.put(Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
        }

        objects = new Vector();
        objects.add(eventType);
        predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE), objects);

        objects = new Vector();
        StringBuffer buf = new StringBuffer("DD ");
        buf.append(eventType);
        if (tblIdfier != null) {
            buf.append(" ").append(tblIdfier);
        }
        objects.add(buf.toString());
        predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

        sendEvent(predicateObjects, user);
    }

    /**
     *
     * @param dst
     * @param eventType
     * @param user
     */
    public void definitionChanged(Dataset dst, String eventType, String user) {

        if (dst == null || eventType == null) {
            return;
        }

        String dstIdfier = dst.getIdentifier();
        String dstURL = dst.getReferenceURL();
        String dstStatus = dst.getStatus();
        if (dstIdfier == null && dstURL == null) {
            return;
        }

        Hashtable predicateObjects = new Hashtable();
        Vector objects = null;

        if (dstIdfier != null) {
            objects = new Vector();
            objects.add(dstIdfier);
            predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_DATASET_PREDICATE), objects);
        }

        if (dstURL != null) {
            objects = new Vector();
            objects.add(dstURL);
            predicateObjects.put(Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
        }

        if (dstStatus != null) {
            objects = new Vector();
            objects.add(dstStatus);
            predicateObjects.put(Props.getProperty(PROP_UNS_DEFINITION_STATUS_PREDICATE), objects);
        }

        if (user != null) {
            objects = new Vector();
            objects.add(user);
            predicateObjects.put(Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
        }

        objects = new Vector();
        objects.add(eventType);
        predicateObjects.put(Props.getProperty(Subscriber.PROP_UNS_EVENTTYPE_PREDICATE), objects);

        objects = new Vector();
        StringBuffer buf = new StringBuffer("DD ");
        buf.append(eventType);
        if (dstIdfier != null) {
            buf.append(" ").append(dstIdfier);
        }
        objects.add(buf.toString());
        predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

        sendEvent(predicateObjects, user);
    }

    /**
     * @param siteCodeAddedNotif
     * @param user
     */
    public void siteCodesAdded(SiteCodeAddedNotification siteCodeAddedNotif, String user) {

        if (siteCodeAddedNotif == null) {
            return;
        }
        Hashtable predicateObjects = new Hashtable();
        Vector objects = null;

        // TODO convert siteCodeAddedNotif object to XML - RPC equest.
        predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

        // FIXME
        // sendEvent(predicateObjects, user);
    }

    /**
     *
     * @param siteCodeAllocatedNotif
     * @param user
     */
    public void siteCodesAllocated(SiteCodeAllocationNotification siteCodeAllocatedNotif, String user) {

        if (siteCodeAllocatedNotif == null) {
            return;
        }
        Hashtable predicateObjects = new Hashtable();
        Vector objects = null;

        // TODO convert siteCodeAllocatedNotif object to XML - RPC equest.
        predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

        // FIXME
        // sendEvent(predicateObjects, user);
    }

    /**
     *
     * @param predicateObjects
     * @param eventIDTrailer
     */
    protected void sendEvent(Hashtable predicateObjects, String eventIDTrailer) {

        if (predicateObjects == null || predicateObjects.size() == 0) {
            return;
        }

        Vector rdfTriples = prepareTriples(predicateObjects, eventIDTrailer);
        logTriples(rdfTriples);
        makeCall(rdfTriples);
    }

    /**
     *
     * @param predicateObjects
     * @param eventIDTrailer
     * @return
     */
    protected Vector prepareTriples(Hashtable predicateObjects, String eventIDTrailer) {

        Vector rdfTriples = new Vector();
        RDFTriple rdfTriple = new RDFTriple();
        String eventID = String.valueOf(System.currentTimeMillis());
        if (eventIDTrailer != null) {
            eventID = eventID + eventIDTrailer;
        }

        try {
            String digest = Util.digestHexDec(eventID, "MD5");
            if (digest != null && digest.length() > 0) {
                eventID = digest;
            }
        } catch (GeneralSecurityException e) {
            throw new DDRuntimeException("Error generating an MD5 hash", e);
        }

        eventID = Props.getProperty(PROP_UNS_EVENTS_NAMESPACE) + eventID;

        rdfTriple.setSubject(eventID);
        rdfTriple.setPredicate(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));
        rdfTriple.setObject("Data Dictionary event");
        rdfTriples.add(rdfTriple.toVector());

        Enumeration predicates = predicateObjects.keys();
        while (predicates.hasMoreElements()) {
            String predicate = (String) predicates.nextElement();
            Vector objects = (Vector) predicateObjects.get(predicate);
            for (int i = 0; objects != null && i < objects.size(); i++) {
                String object = (String) objects.get(i);

                rdfTriple = new RDFTriple();
                rdfTriple.setSubject(eventID);
                rdfTriple.setPredicate(predicate);
                rdfTriple.setObject(object);
                rdfTriples.add(rdfTriple.toVector());
            }
        }
        return rdfTriples;
    }

    /**
     *
     * @param rdfTriples
     */
    protected void makeCall(Object rdfTriples) {

        if (dontCallActually()) {
            return;
        }

        // get server URL, channel name and function-name from configuration
        String serverURL = Props.getProperty(Subscriber.PROP_UNS_XMLRPC_SERVER_URL);
        String channelName = Props.getProperty(Subscriber.PROP_UNS_CHANNEL_NAME);
        String functionName = Props.getProperty(Subscriber.PROP_UNS_SEND_NOTIFICATION_FUNC);
        String userName = Props.getProperty(Subscriber.PROP_UNS_USERNAME);
        String password = Props.getProperty(Subscriber.PROP_UNS_PASSWORD);

        try {
            // instantiate XML-RPC client object, set username/password from configuration
            XmlRpcClient client = newXmlRpcClient(serverURL);
            client.setBasicAuthentication(userName, password);

            // prepare call parameters
            Vector params = new Vector();
            params.add(channelName);
            params.add(rdfTriples);

            // perform the call
            XmlRpcCallThread.execute(client, functionName, params);
        } catch (IOException e) {
            LOGGER.error("Sending UNS notification failed: " + e.toString(), e);
        }
    }

    /**
     *
     * @return
     */
    protected boolean dontCallActually() {
        return StringUtils.isNotBlank(Props.getProperty(Subscriber.PROP_UNS_DONTSENDEVENTS)) || File.separatorChar == '\\';
    }

    /**
     *
     * @param serverURL
     * @return
     * @throws MalformedURLException
     */
    protected XmlRpcClient newXmlRpcClient(String serverURL) throws MalformedURLException {
        return new XmlRpcClient(serverURL);
    }

    /**
     *
     * @param triples
     */
    private void logTriples(Vector triples) {

        if (triples != null) {

            int noOfTriples = triples.size();
            for (int i = 0; i < noOfTriples; i++) {

                Vector triple = (Vector) triples.get(i);
                if (triple != null) {

                    int tripleSize = triple.size();
                    if (tripleSize > 0) {

                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < tripleSize; j++) {

                            if (j > 0) {
                                sb.append(" | ");
                            }
                            sb.append(triple.get(j));
                        }
                        LOGGER.debug(sb.toString());
                    }
                }
            }
        }
    }
}
