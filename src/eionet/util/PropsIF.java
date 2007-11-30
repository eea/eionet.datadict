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
	public static final String TEST_PROP_FILE = "datadict-test";
	
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
	
	public static final String XLS_SCHEMA_URL = "xls.schema-url";
	public static final String XLS_SCHEMA_URL_SHEET = "xls.schema-url-sheet";

	public static final String INSERV_PREFIX = "inserv.";
	public static final String INSERV_NAME = ".name";
	public static final String INSERV_URL = ".url";
	public static final String INSERV_USR = ".usr";
	public static final String INSERV_PSW = ".psw";
	
	public static final String INSERV_ROD_RA_URLPATTERN = "inserv.webrod.ra-url-pattern";
	public static final String INSERV_ROD_RA_IDPATTERN  = "<RA_ID>";
	
	public static final String OUTSERV_ELM_URLPATTERN = "outserv.elm-details-url";
	public static final String OUTSERV_ELM_IDPATTERN  = "<ELM_ID>";
	public static final String OUTSERV_ROD_OBLIG_URL = "outserv.rod-obligation-url";
	public static final String OUTSERV_PRED_IDENTIFIER = "outserv.pred-identifier";
	public static final String OUTSERV_PRED_TITLE = "outserv.pred-title";
	
	public static final String JSP_URL_PREFIX = "jsp.url-prefix";
	
	public static final String DD_RDF_SCHEMA_URL = "dd.rdf-schema.url";
	public static final String PREDICATE_RDF_TYPE = "predicate.rdf-type";
	public static final String PREDICATE_RDF_LABEL = "predicate.rdf-label";
	
	public static final String VISUALS_PATH = "visuals.path";
	public static final String TEMP_FILE_PATH = "general.temp-file-path";
	public static final String DOC_PATH = "general.doc-path";
	public static final String OPENDOC_ODS_PATH = "opendoc.ods.path";

	/** */
	public static final String SCREEN_NAME = "documentation.screen-name";

	/** */
	public static final String SRC_DIR = "src.dir";

	/** defaults */
	
}
