/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2003 European Environment Agency. All
 * Rights Reserved.
 *
 * Contributor(s):
 */

/*
 * Created on Sep 30, 2003
 */
package eionet.util;

/**
 * @author jaanus
 */
public interface PropsIF {

    /** */
    public static final String PROP_FILE = "datadict";
    public static final String TEST_PROP_FILE = "datadict-test";

    /** */
    public static final String DBDRV = "db.drv";
    public static final String DBURL = "db.url";
    public static final String DBUSR = "db.usr";
    public static final String DBPSW = "db.psw";

    /** */
    public static final String DB_UNITTEST_DRV = "db.unitest.drv";
    public static final String DB_UNITTEST_URL = "db.unitest.url";
    public static final String DB_UNITTEST_USR = "db.unitest.usr";
    public static final String DB_UNITTEST_PWD = "db.unitest.psw";

    /** */
    public static final String HRV_LOG = "harvester.log";
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
    public static final String INSERV_ROD_RA_IDPATTERN = "<RA_ID>";

    public static final String OUTSERV_ELM_URLPATTERN = "outserv.elm-details-url";
    public static final String OUTSERV_ELM_IDPATTERN = "<ELM_ID>";
    public static final String OUTSERV_ROD_OBLIG_URL = "outserv.rod-obligation-url";
    public static final String OUTSERV_PRED_IDENTIFIER = "outserv.pred-identifier";
    public static final String OUTSERV_PRED_TITLE = "outserv.pred-title";

    public static final String JSP_URL_PREFIX = "jsp.url-prefix";
    public static final String DD_URL = "dd.url";

    public static final String DD_RDF_SCHEMA_URL = "dd.rdf-schema.url";
    public static final String PREDICATE_RDF_TYPE = "predicate.rdf-type";
    public static final String PREDICATE_RDF_LABEL = "predicate.rdf-label";

    public static final String TEMP_FILE_PATH = "general.temp-file-path";
    public static final String DOC_PATH = "general.doc-path";
    public static final String OPENDOC_ODS_PATH = "opendoc.ods.path";

    /** */
    public static final String SCREEN_NAME = "documentation.screen-name";

    /** */
    public static final String IRRELEVANT_ATTRS_PREFIX = "irrelevantAttrs.";

    /** */
    public static final String RDF_TABLES_BASE_URI = "rdf.tables.baseUri";
    public static final String RDF_DATAELEMENTS_BASE_URI = "rdf.dataelements.baseUri";

    /** */
    public static final String GENERAL_SCHEMA_URI = "dd.generalSchemaUri";
    public static final String MULTIVAL_DELIM_ATTR = "dd.multiValueDelimAttrName";

    /** */
    public static final String DATASET_MS_ACCESS_TEMPLTAE = "datasetMSAccessTemplate";

    /** Full path to the root directory of DD's file store. */
    public static final String FILESTORE_PATH = "filestore.path";

    /** */
    public static final String SCHEMA_REPO_LOCATION = "schemaRepo.location";

    /** */
    public static final String CHECK_IN_COMMENTS_REQUIRED = "checkInCommentsRequired";

    /** */
    public static final String XML_CONV_URL = "xmlConv.url";

    /** Master password for simulating users */
    public static final String DD_MASTER_PASSWORD_HASH = "dd.master.pwd.hash";

    /** Site code notification addresses. */
    public static final String SITE_CODE_NOTIFICATION_FROM = "siteCode.notification.from";
    public static final String SITE_CODE_ALLOCATE_NOTIFICATION_TO = "siteCode.allocate.notification.to";
    public static final String SITE_CODE_RESERVE_NOTIFICATION_TO = "siteCode.reserve.notification.to";
}
