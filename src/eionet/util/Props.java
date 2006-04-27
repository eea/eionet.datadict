/*
 * Created on Sep 30, 2003
 */
package eionet.util;

import java.util.*;

/**
 * @author jaanus
 */
public class Props implements PropsIF{
	
	private static ResourceBundle props = null;
	private static Hashtable defaultProps = null;

	/**
	 * get String property 
	 * @param name
	 * @return
	 */
	public static synchronized String getProperty(String name){
		
		if (props==null)
			init();
		
		String prop = null;
		if (props!=null){
			try{
				prop = props.getString(name);
			}
			catch (MissingResourceException mre){}
		}
		
		if (prop==null){
			prop = (String)defaultProps.get(name);
			if (prop!=null)
				System.out.println("Property value for key " +
									name + " not found. Using default.");
		}
		
		return prop;
	}
	
	/**
	 * Get int property. Throws an exception if Integer.parseInt() failed.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static synchronized int getIntProperty(String name) throws Exception{
		String sProp = Props.getProperty(name);
		try{
			return Integer.parseInt(sProp);
		}
		catch (NumberFormatException nfe){
			String dflt = (String)defaultProps.get(name);
			if (dflt!=null){
				System.out.println("Invalid property value for key " +
									name + ". Using default.");
				return Integer.parseInt(dflt);
			}
			else
				throw new Exception("Invalid property value for key " + name);
		}
	}
	
	/**
	 * initializes the properties through ResourceBundle, sets the defaults too
	 */
	private static synchronized void init(){
		try {
			props = ResourceBundle.getBundle(PROP_FILE);
		} catch (MissingResourceException mre) {
			System.out.println("Properties file " + PROP_FILE +
								".properties not found. Using defaults.");
		}
		setDefaults();
	}

	/**
	 * sets the default properties
	 *
	 */	
	private static synchronized void setDefaults(){
		defaultProps = new Hashtable();
		defaultProps.put(XFORM_TEMPLATE_URL,"http://cdr-ewn.eionet.eu.int/webq/GetXFormTemplate");
		defaultProps.put(INSERV_ROD_RA_URLPATTERN,"http://rod.eionet.eu.int/show.jsv?id=<RA_ID>&mode=A");
		defaultProps.put(XLS_SCHEMA_URL_SHEET, "DO_NOT_DELETE_THIS_SHEET");
		defaultProps.put(DD_RDF_SCHEMA_URL, "http://dd.eionet.eu.int/schema.rdf#");
		defaultProps.put(PREDICATE_RDF_TYPE, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		defaultProps.put(PREDICATE_RDF_LABEL, "http://www.w3.org/2000/01/rdf-schema#label");
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		String dbDrv = Props.getProperty(DBDRV);
		System.out.println(dbDrv);
	}
}
