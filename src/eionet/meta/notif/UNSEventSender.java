/*
 * Created on 26.04.2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package eionet.meta.notif;

import eionet.meta.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletException;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import eionet.util.*;

/**
 * @author jaanus
 */
public class UNSEventSender {
	
	/** */
	public static final String PROP_UNS_EVENTS_NAMESPACE = "uns.events-namespace";
	public static final String PROP_UNS_USER_PREDICATE = "uns.user.predicate";
	public static final String PROP_UNS_DEFINITION_URL_PREDICATE =
		"uns.definition-url.predicate";
	public static final String PROP_UNS_DEFINITION_STATUS_PREDICATE =
		"uns.definition-status.predicate";
	
	/**
	 * 
	 *
	 */
	public UNSEventSender(){
	}

	/**
	 * 
	 * @param elm
	 * @param eventType
	 * @param user
	 */
	public static void definitionChanged(DataElement elm, String eventType, String user){
		
		if (elm==null || eventType==null)
			return;
		
		String elmIdfier = elm.getIdentifier();
		String elmURL = elm.getReferenceURL();
		String elmStatus = elm.getStatus();
		boolean isCommon = elm.getNamespace()==null || elm.getNamespace().getID()==null; 
		if (elmIdfier==null && elmURL==null)
			return;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (elmIdfier!=null){
			objects = new Vector();
			objects.add(elmIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_COMMONELEM_PREDICATE), objects);
		}

		if (elmURL!=null){
			objects = new Vector();
			objects.add(elmURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}
		
		if (isCommon && elmStatus!=null){
			objects = new Vector();
			objects.add(elmStatus);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_STATUS_PREDICATE), objects);
		}


		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}

		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);

		objects = new Vector();
		StringBuffer buf = new StringBuffer("DD ");
		buf.append(eventType);
		if (elmIdfier!=null)
			buf.append(" ").append(elmIdfier);
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
	public static void definitionChanged(DsTable tbl, String eventType, String user){
		
		if (tbl==null || eventType==null)
			return;
		
		String tblIdfier = tbl.getIdentifier();
		String tblURL = tbl.getReferenceURL();
		if (tblIdfier==null && tblURL==null)
			return;
		
		// the identifier sent to UNS must be like dataset_identifer/table_identifer
		String dstIdfier = tbl.getDstIdentifier();
		if (dstIdfier!=null)
			tblIdfier = dstIdfier + "/" + tblIdfier;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (tblIdfier!=null){
			objects = new Vector();
			objects.add(tblIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_TABLE_PREDICATE), objects);
		}

		if (tblURL!=null){
			objects = new Vector();
			objects.add(tblURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}

		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}

		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);

		objects = new Vector();
		StringBuffer buf = new StringBuffer("DD ");
		buf.append(eventType);
		if (tblIdfier!=null)
			buf.append(" ").append(tblIdfier);
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
	public static void definitionChanged(Dataset dst, String eventType, String user){
		
		if (dst==null || eventType==null)
			return;
		
		String dstIdfier = dst.getIdentifier();
		String dstURL = dst.getReferenceURL();
		String dstStatus = dst.getStatus();
		if (dstIdfier==null && dstURL==null)
			return;
		
		Hashtable predicateObjects = new Hashtable();
		Vector objects = null;
		
		if (dstIdfier!=null){
			objects = new Vector();
			objects.add(dstIdfier);
			predicateObjects.put(
					Props.getProperty(Subscribe.PROP_UNS_DATASET_PREDICATE), objects);
		}

		if (dstURL!=null){
			objects = new Vector();
			objects.add(dstURL);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_URL_PREDICATE), objects);
		}
		
		if (dstStatus!=null){
			objects = new Vector();
			objects.add(dstStatus);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_DEFINITION_STATUS_PREDICATE), objects);
		}
		
		if (user!=null){
			objects = new Vector();
			objects.add(user);
			predicateObjects.put(
					Props.getProperty(PROP_UNS_USER_PREDICATE), objects);
		}
		
		objects = new Vector();
		objects.add(eventType);
		predicateObjects.put(
				Props.getProperty(Subscribe.PROP_UNS_EVENTTYPE_PREDICATE), objects);

		objects = new Vector();
		StringBuffer buf = new StringBuffer("DD ");
		buf.append(eventType);
		if (dstIdfier!=null)
			buf.append(" ").append(dstIdfier);
		objects.add(buf.toString());
		predicateObjects.put(Props.getProperty(PropsIF.OUTSERV_PRED_TITLE), objects);

		sendEvent(predicateObjects, user);
	}

	/*
	 * 
	 */
	public static void sendEvent(Hashtable predicateObjects, String eventIDTrailer){
		
		if (predicateObjects==null || predicateObjects.size()==0)
			return;
		
		Vector rdfTriples = new Vector();
		RDFTriple rdfTriple = new RDFTriple();
		String eventID = String.valueOf(System.currentTimeMillis());
		if (eventIDTrailer!=null)
			eventID = eventID + eventIDTrailer;
		
		try{
			String digest = Util.digestHexDec(eventID, "MD5");
			if (digest!=null && digest.length()>0)
				eventID = digest;
		}
		catch (GeneralSecurityException e){
			throw new DDRuntimeException("Error generating an MD5 hash", e);
		}
			
		eventID = Props.getProperty(PROP_UNS_EVENTS_NAMESPACE) + eventID;
		
		rdfTriple.setSubject(eventID);
		rdfTriple.setPredicate(Props.getProperty(PropsIF.PREDICATE_RDF_TYPE));
		rdfTriple.setObject("Data Dictionary event");
		rdfTriples.add(rdfTriple.toVector());
		
		Enumeration predicates = predicateObjects.keys();
		while (predicates.hasMoreElements()){
			String predicate = (String)predicates.nextElement();
			Vector objects = (Vector)predicateObjects.get(predicate);
			for (int i=0; objects!=null && i<objects.size(); i++){
				String object = (String)objects.get(i);
				
				rdfTriple = new RDFTriple();
				rdfTriple.setSubject(eventID);
				rdfTriple.setPredicate(predicate);
				rdfTriple.setObject(object);
				rdfTriples.add(rdfTriple.toVector());
			}
		}
		
		// DEBUG
		logTriples(rdfTriples);
		
		makeCall(rdfTriples);
	}

	/*
	 * 
	 */
	public static void makeCall(Object rdfTriples){
		
		// we don't make UNS calls when in development environment (assume it's Win32)
		if (File.separatorChar == '\\')
			return;

		// don't send if the configuration says so
		String dontSendEvents = Props.getProperty(Subscribe.PROP_UNS_DONTSENDEVENTS);
		if (dontSendEvents!=null && dontSendEvents.trim().length()>0)
			return;
		
		// get server URL and channel name from configuration
        String serverURL = Props.getProperty(Subscribe.PROP_UNS_XMLRPC_SERVER_URL);
        String channelName = Props.getProperty(Subscribe.PROP_UNS_CHANNEL_NAME);
        
        try{
	        // instantiate XML-RPC client object, set username/password from configuration
	        XmlRpcClient client = new XmlRpcClient(serverURL);
	        client.setBasicAuthentication(Props.getProperty(Subscribe.PROP_UNS_USERNAME),
					Props.getProperty(Subscribe.PROP_UNS_PASSWORD));
	        
	        // prepare call parameters
	        Vector params = new Vector();
	        params.add(channelName);
	        params.add(rdfTriples);
	        
	        // perform the call
	        XmlRpcCallThread.execute(client, Props.getProperty(Subscribe.PROP_UNS_SEND_NOTIFICATION_FUNC), params);
        }
        catch (IOException e){
        	e.printStackTrace(System.out);
        }
    }
	
	/*
	 * 
	 */
	private static void logTriples(Vector triples){
		
		for (int i=0; triples!=null && i<triples.size(); i++){
			Vector triple = (Vector)triples.get(i);
			for (int j=0; triple!=null && j<triple.size(); j++){
				if (j>0)System.out.print(" | ");
				System.out.print(triple.get(j));				
			}
			System.out.println();
		}
		System.out.println();
	}
}