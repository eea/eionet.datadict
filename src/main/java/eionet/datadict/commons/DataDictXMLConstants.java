package eionet.datadict.commons;

import eionet.datadict.model.Namespace;
import eionet.util.Props;
import eionet.util.PropsIF;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DataDictXMLConstants {

    public static final String DATASETS_NAMESPACE_ID = "1";
    public static final String ISOATTRS_NAMESPACE_ID = "2";
    public static final String DDATTRS_NAMESPACE_ID = "3";
    public static final String DATASETS = "datasets";
    public static final String DATASET = "dataset";
    public static final String DD_ATTRS = "ddattrs";
    public static final String ISO_ATTRS = "isoattrs";
    public static final String TARGET_NAMESPACE = "targetNamespace";
    public static final String ELEMENT = "element";
    public static final String ANNOTATION = "annotation";
    public static final String COMPLEX_TYPE = "complexType";
    public static final String SIMPLE_TYPE = "simpleType";
    public static final String RESTRICTION = "restriction";
    public static final String ATTRIBUTE = "attribute";
    public static final String DATASET_SCHEMA_LOCATION_PARTIAL_FILE_NAME = "schema-dst-";
    public static final String TABLE_SCHEMA_LOCATION_PARTIAL_FILE_NAME = "schema-tbl-";
    public static final String XSD_FILE_EXTENSION = ".xsd";
    public static final String SEQUENCE = "sequence";
    public static final String REF = "ref";
    public static final String DOCUMENTATION = "documentation";
    public static final String DEFAULT_XML_LANGUAGE = "en";
    public static final String NAME = "name";
    public static final String TYPE= "type";
    public static final String APP_CONTEXT = Props.getRequiredProperty(PropsIF.DD_URL);
    public static final String XS_PREFIX = "xs";
    public static final String DD_PREFIX = "dd";
    public static final String XSI_PREFIX = "xsi";
    public static final String BASE = "base";
    public static final String ISOATTRS_NAMESPACE = APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + ISOATTRS_NAMESPACE_ID;
    public static final String DDATTRS_NAMESPACE = APP_CONTEXT + "/" + Namespace.URL_PREFIX + "/" + DDATTRS_NAMESPACE_ID;
    public static final String NAMESPACE = "namespace";
    public static final String SCHEMA_LOCATION = "schemaLocation";
    public static final String IMPORT = "import";
    public static final String SCHEMA = "schema";
    public static final String ROW = "Row";
    public static final String LANGUAGE_PREFIX="lang";
    public static final String MIN_OCCURS="minOccurs";
    public static final String MAX_OCCURS="maxOccurs";
    public static final String SCHEMAS_API_V2_PREFIX="v2";
    
}
