/*
 * Created on Sep 30, 2003
 */
package eionet.util;

import java.util.*;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Props implements PropsIF{
	
	/** */
	private ResourceBundle bundle = null;
	private Hashtable defaults = null;
	
	/** */
	private static Props instance = null;
	private static LogServiceIF logger = new Log4jLoggerImpl();
	
	/**
	 * 
	 *
	 */
	protected Props(){
		
		try {
			bundle = ResourceBundle.getBundle(getBundleName());
		}
		catch (MissingResourceException mre) {
			logger.warning("Properties file " + getBundleName() + ".properties not found. Using defaults.");
		}
		
		defaults = new Hashtable();
		setDefaults(defaults);
	}
	
	/**
	 * 
	 * @return
	 */
	private static Props getInstance(){
		if (Props.instance==null)
			Props.instance = new Props();
		
		return Props.instance;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized String getProperty(String name){
		return Props.getInstance().getProperty_(name);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static synchronized int getIntProperty(String name) throws Exception{
		return Props.getInstance().getIntProperty_(name);
	}

	/**
	 * get String property 
	 * @param name
	 * @return
	 */
	protected final String getProperty_(String name){
		
		String value = null;
		if (bundle!=null){
			try{
				value = bundle.getString(name);
			}
			catch (MissingResourceException mre){}
		}
		
		if (value==null){
			value = (String)defaults.get(name);
			if (value!=null)
				logger.warning("Property value for key " + name + " not found. Using default.");
		}
		
		return value;
	}
	
	/**
	 * Get int property. Throws an exception if Integer.parseInt() failed.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	protected final int getIntProperty_(String name) throws Exception{
		
		String stringValue = getProperty_(name);
		try{
			return Integer.parseInt(stringValue);
		}
		catch (NumberFormatException nfe){
			String deflt = (String)defaults.get(name);
			if (deflt!=null){
				logger.warning("Invalid property value for key " + name + ". Using default.");
				return Integer.parseInt(deflt);
			}
			else
				throw new Exception("Invalid property value for key " + name);
		}
	}

	/**
	 * Sets the default properties.
	 * @param defaults
	 */
	protected void setDefaults(Hashtable defaults){
		
		defaults.put(XFORM_TEMPLATE_URL,"http://cdr-ewn.eionet.eu.int/webq/GetXFormTemplate");
		defaults.put(INSERV_ROD_RA_URLPATTERN,"http://rod.eionet.eu.int/show.jsv?id=<RA_ID>&mode=A");
		defaults.put(XLS_SCHEMA_URL_SHEET, "DO_NOT_DELETE_THIS_SHEET");
		defaults.put(DD_RDF_SCHEMA_URL, "http://dd.eionet.europa.eu/schema.rdf#");
		defaults.put(PREDICATE_RDF_TYPE, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		defaults.put(PREDICATE_RDF_LABEL, "http://www.w3.org/2000/01/rdf-schema#label");
		defaults.put(OUTSERV_PRED_TITLE, "http://purl.org/dc/elements/1.1/title");
	}
	
	/**
	 * 
	 * @return
	 */
	protected String getBundleName(){
		return PROP_FILE;
	}
}
