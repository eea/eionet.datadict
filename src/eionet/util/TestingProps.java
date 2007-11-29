package eionet.util;

import java.util.Hashtable;


/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class TestingProps extends Props {

	/** */
	private static TestingProps instance = null;
	private static LogServiceIF logger = new Log4jLoggerImpl();

	/**
	 *
	 */
	protected TestingProps(){
		super();
	}

	/**
	 * 
	 * @return
	 */
	private static TestingProps getInstance(){
		if (TestingProps.instance==null)
			TestingProps.instance = new TestingProps();
		
		return TestingProps.instance;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized String getProperty(String name){
		return TestingProps.getInstance().getProperty_(name);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static synchronized int getIntProperty(String name) throws Exception{
		return TestingProps.getInstance().getIntProperty_(name);
	}

	/*
	 *  (non-Javadoc)
	 * @see eionet.util.Props#getBundleName()
	 */
	protected String getBundleName(){
		return TEST_PROP_FILE;
	}

	/*
	 *  (non-Javadoc)
	 * @see eionet.util.Props#setDefaults(java.util.Hashtable)
	 */
	protected void setDefaults(Hashtable defaults){
		
		super.setDefaults(defaults);
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
	 */
	public static void main(String[] args){
		
		System.out.println(Props.getProperty(PropsIF.DBURL));
		System.out.println(TestingProps.getProperty(PropsIF.DBURL));
	}
}
