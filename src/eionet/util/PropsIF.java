/*
 * Created on Sep 30, 2003
 */
package eionet.util;

/**
 * @author jaanus
 */
public interface PropsIF {
	
	/** properties file name prefix */
	public static final String PROP_FILE = "datadict";
	
	/** properties names*/
	public static final String DBDRV = "db.drv";
	public static final String DBURL = "db.url";
	public static final String DBUSR = "db.usr";
	public static final String DBPSW = "db.psw";
	
	public static final String HRV_LOG    = "harvester.log";
	public static final String HRV_LOGLEV = "harvester.log-level";
	
	public static final String UNI_FONT = "pdf.uni-font";
	
	public static final String XFORMS_NSS = "xforms.nss";
	public static final String XFORMS_NS = "xforms.ns";
	public static final String XFORM_TEMPLATE_URL = "xforms.templ.url";

	public static final String INSERV_PREFIX = "inserv.";
	public static final String INSERV_NAME = ".name";
	public static final String INSERV_URL = ".url";
	public static final String INSERV_USR = ".usr";
	public static final String INSERV_PSW = ".psw";
	
	public static final String INSERV_ROD_RA_URLPATTERN = "inserv.webrod.ra-url-pattern";
	public static final String INSERV_ROD_RA_IDPATTERN  = "<RA_ID>";
	
	public static final String OUTSERV_ELM_URLPATTERN = "outserv.elm-details-url";
	public static final String OUTSERV_ELM_IDPATTERN  = "<ELM_ID>";
	
	/** defaults */
	
}
