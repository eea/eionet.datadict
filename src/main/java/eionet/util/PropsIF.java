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
 * An interface container for the application's properties-related constants. TODO: Interfaces should not be used as mere constant
 * containers, so it's better to mve them into {@link Props} for example.
 *
 * @author Jaanus Heinlaid
 */
public interface PropsIF {

    /** */
    String PROP_FILE = "datadict";
    String TEST_PROP_FILE = "datadict-test";
    String APP_HOME = "app.home";

    /** */
    String DBDRV = "db.drv";
    String DBURL = "db.url";
    String DBUSR = "db.usr";
    String DBPSW = "db.psw";

    /** */
    String DB_UNITTEST_DRV = "db.unitest.drv";
    String DB_UNITTEST_URL = "db.unitest.url";
    String DB_UNITTEST_USR = "db.unitest.usr";
    String DB_UNITTEST_PWD = "db.unitest.psw";

    String UNI_FONT = "pdf.uni-font";

    String XFORMS_NSS = "xforms.nss";
    String XFORMS_NS = "xforms.ns";
    String XFORM_TEMPLATE_URL = "xforms.templ.url";

    String XLS_SCHEMA_URL = "xls.schema-url";
    String XLS_SCHEMA_URL_SHEET = "xls.schema-url-sheet";
    String XLS_DROPDOWN_FXV_SHEET = "xls.schema-dropdown-fxv-sheet";

    String OUTSERV_ELM_URLPATTERN = "outserv.elm-details-url";
    String OUTSERV_ELM_IDPATTERN = "<ELM_ID>";
    String OUTSERV_ROD_OBLIG_URL = "outserv.rod-obligation-url";
    String OUTSERV_PRED_IDENTIFIER = "outserv.pred-identifier";
    String OUTSERV_PRED_TITLE = "outserv.pred-title";

    String JSP_URL_PREFIX = "jsp.url-prefix";
    String DD_URL = "dd.url";

    String DD_RDF_SCHEMA_URL = "dd.rdf-schema.url";
    String PREDICATE_RDF_TYPE = "predicate.rdf-type";
    String PREDICATE_RDF_LABEL = "predicate.rdf-label";

    String TEMP_FILE_PATH = "general.temp-file-path";
    String DOC_PATH = "general.doc-path";
    String OPENDOC_ODS_PATH = "opendoc.ods.path";

    /** */
    String SCREEN_NAME = "documentation.screen-name";

    /** */
    String IRRELEVANT_ATTRS_PREFIX = "irrelevantAttrs.";

    /** */
    String RDF_TABLES_BASE_URI = "rdf.tables.baseUri";
    String RDF_DATAELEMENTS_BASE_URI = "rdf.dataelements.baseUri";

    /** */
    String GENERAL_SCHEMA_URI = "dd.generalSchemaUri";
    String MULTIVAL_DELIM_ATTR = "dd.multiValueDelimAttrName";

    /** */
    String DATASET_MS_ACCESS_TEMPLTAE = "datasetMSAccessTemplate";

    /**
     * Full path to the root directory of DD's file store.
     */
    String FILESTORE_PATH = "filestore.path";

    /** */
    String SCHEMA_REPO_LOCATION = "schemaRepo.location";

    /** */
    String CHECK_IN_COMMENTS_REQUIRED = "checkInCommentsRequired";

    /** */
    String XML_CONV_URL = "xmlConv.url";

    /**
     * Master password for simulating users.
     */
    String DD_MASTER_PASSWORD_HASH = "dd.master.pwd.hash";

    /**
     * Site code notification addresses.
     */
    String SITE_CODE_NOTIFICATION_FROM = "siteCode.notification.from";
    String SITE_CODE_ALLOCATE_NOTIFICATION_TO = "siteCode.allocate.notification.to";
    String SITE_CODE_RESERVE_NOTIFICATION_TO = "siteCode.reserve.notification.to";
    String SITE_CODE_TEST_NOTIFICATION_TO = "siteCode.test.notification.to";

    /**
     * Content Registry URL for making the request to reharvest the source.
     */
    String CR_PING_URL = "cr.reharvest.request.url";

    /**
     * The property that withholds the friendly URI template for DD namespaces.
     */
    String NAMESPACE_FRIENDLY_URI_TEMPLATE = "dd.namespaces.friendlyUriTemplate";

    /**
     * Maximum amount site codes to allocate.
     */
    String SITE_CODES_MAX_ALLOCATE = "siteCode.allocate.maxAmount";

    /**
     * Maximum amount site codes to allocate without name.
     */
    String SITE_CODES_MAX_ALLOCATE_WITHOUT_NAMES = "siteCode.allocate.maxAmountWithoutName";

    /**
     * Maximum amount site codes to allocate by ETC or EEA users.
     */
    String SITE_CODES_MAX_ALLOCATE_ETC_EEA = "siteCode.allocate.maxAmountForEtcEeaUsers";

    /**
     * Maximum amount available site codes to reserve.
     */
    String SITE_CODES_MAX_RESERVE_AMOUNT = "siteCode.reserve.maxAmount";

    // Site code LDAP parent roles
    String SITE_CODE_PARENT_ROLES = "siteCode.parent.roles";

    /**
     * Data Dictionary Working Language.
     */
    String DD_WORKING_LANGUAGE_KEY = "dd.working.language";
    /**
     * Data Dictionary Default Working Language Values (used when working language is not defined in local.properties).
     */
    String DD_DEFAULT_WORKING_LANGUAGE_VALUE = "en";
    /**
     * Recently released vocabularies count to be displayed, key.
     */
    String DD_RECENTLY_RELEASED_VOCABULARIES_KEY = "dd.recently.released.vocabularies";
    /**
     * Recently released vocabularies count to be displayed, default value.
     */
    String DD_DEFAULT_RECENTLY_RELEASED_VOCABULARIES_VALUE = "4";
    /**
     * Recently released schemas count to be displayed, key.
     */
    String DD_RECENTLY_RELEASED_SCHEMAS_KEY = "dd.recently.released.schemas";
    /**
     * Recently released schemas count to be displayed, default value.
     */
    String DD_DEFAULT_RECENTLY_RELEASED_SCHEMAS_VALUE = "4";
    /**
     * Recently released datasets count to be displayed, key.
     */
    String DD_RECENTLY_RELEASED_DATASETS_KEY = "dd.recently.released.datasets";
    /**
     * Recently released datasets count to be displayed, default value.
     */
    String DD_DEFAULT_RECENTLY_RELEASED_DATASETS_VALUE = "4";
    /**
     * If false, the Central Authentication Service (CAS) is not used.
     * Instead, the mechanism provided by the eionetdir.properties and the local users file defined in uit.properties is used.
     */
    String USE_CENTRAL_AUTHENTICATION_SERVICE = "useCentralAuthenticationService";
    /**
     * Data Dictionary Vocabulary API JWT Key.
     */
    String DD_VOCABULARY_API_JWT_KEY = "dd.vocabulary.api.key";
    /**
     * Data Dictionary Vocabulary API JWT Audience.
     */
    String DD_VOCABULARY_API_JWT_AUDIENCE = "dd.vocabulary.api.audience";
    /**
     * Data Dictionary Vocabulary API JWT Exp in minutes (used for signing).
     */
    String DD_VOCABULARY_API_JWT_EXP_IN_MINUTES = "dd.vocabulary.api.exp";
    /**
     * Data Dictionary Vocabulary API JWT Timeout in minutes.
     */
    String DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES = "dd.vocabulary.api.jwt.timeout";
    /**
     * Data Dictionary Vocabulary API JWT Timeout default value in minutes.
     */
    int DD_VOCABULARY_API_JWT_TIMEOUT_DEFAULT_VALUE_IN_MINUTES = 10;
    /**
     * Data Dictionary Vocabulary API JWT Signing algorithm.
     */
    String DD_VOCABULARY_ADI_JWT_ALGORITHM = "dd.vocabulary.api.jwt.algorithm";
    /**
     * Data Dictionary Vocabulary API JWT Signing algorithm default value.
     */
    String DD_VOCABULARY_ADI_JWT_ALGORITHM_DEFAULT_VALUE = "HS512";

    String DD_JWT_HEADER = "env.dd.jwt.header";

    /** Name of the folder of DD's own vocabularies. */
    String DD_OWN_VOCABULARIES_FOLDER_NAME = "dd.ownVocabulariesFolderName";

    /** Name of DD's own status vocabulary (that lives inside {@link PropsIF#DD_OWN_VOCABULARIES_FOLDER_NAME}). */
    String DD_OWN_STATUS_VOCABULARY_IDENTIFIER = "dd.ownStatusVocabularyIdentifier";

    String NOTIFICATION_EMAIL_FROM="notification.email.from";

    /**
     * The property that withholds the friendly URI template for DD namespaces.
     */
    String SSO_LOGIN_PAGE_URI = "edu.yale.its.tp.cas.client.filter.loginUrl";

    String API_USER_MODIFIED_IDENTIFIER= "external_api_request";

    String LDAP_URL = "ldap.url";

    String LDAP_PRINCIPAL = "ldap.principal";

    String LDAP_PASSWORD = "ldap.password";

    String LDAP_CONTEXT = "ldap.context";

    String LDAP_ROLE_DIR = "ldap.role.dir";

    String LDAP_USER_DIR = "ldap.user.dir";

    String LDAP_ROLE_NAME = "ldap.attr.rolename";

    String LDAP_DESCRIPTION = "ldap.attr.roledescription";

    String LDAP_ATTR_MAIL = "ldap.attr.mail";

    String EXCLUDED_COUNTY_CODES = "env.dd.countryCodes.excluded";

    String UNS_URL = "env.uns.url";

    String UNS_SEND_NOTIFICATION_METHOD = "uns.sendNotification.method";

    String UNS_MAKE_SUBSCRIPTION_METHOD = "uns.makeSubscription.method";

    String UNS_REST_USERNAME = "env.uns.rest.username";

    String UNS_REST_PASSWORD = "env.uns.rest.password";
}
