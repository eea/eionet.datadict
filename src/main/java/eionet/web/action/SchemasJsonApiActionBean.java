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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */
package eionet.web.action;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import eionet.meta.DownloadServlet;
import eionet.meta.dao.domain.DataSetTable;
import eionet.meta.dao.domain.Schema;
import eionet.meta.schemas.SchemaRepository;
import eionet.meta.service.ISchemaService;
import eionet.meta.service.ITableService;
import eionet.meta.service.ServiceException;
import eionet.util.Props;
import eionet.util.PropsIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action bean for serving RESTful API methods for retrieving information related to XML Schemas.
 *
 * @author Enriko Käsper
 */
@UrlBinding("/api/schemas/{$event}")
public class SchemasJsonApiActionBean extends AbstractActionBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemasJsonApiActionBean.class);

    /** Table search service. */
    @SpringBean
    private ITableService tableService;

    /** Schemas search service. */
    @SpringBean
    private ISchemaService schemaService;

    /** Schema repository. */
    @SpringBean
    private SchemaRepository schemaRepository;

    /** Reporting Obligation ID. */
    private String obligationId;

    /** Request parameter to query only released schemas. */
    private boolean releasedOnly;

    /**
     * Returns information on released/public schemas associated with the given obligation in DD. Both generated schemas (e.g.
     * http://dd.eionet.europa.eu/GetSchema?id=TBL8286) and manually uploaded schemas (e.g.
     * http://dd.eionet.europa.eu/schemas/fgases/FGasesReporting.xsd) are to be included in the response.
     *
     * The response of the method is to be sent as JSON objects (application/json) representing each schema. Every object shall have
     * the following attributes: url identifier (for generated schemas it's the table's Identifier) name (for generated schemas it's
     * the table's Short name). status - Dataset/Schemaset status eg. Released, Recorded or Public draft.
     *
     * If no schemas are found for the requested obligation ID, the method shall return HTTP 404.
     *
     * Parameters: obligationId - Obligation Identifier. Eg, ROD URL: http://rod.eionet.europa.eu/obligations/28 releasedOnly - if
     * true, then returns only released schemas. Otherwise all public schemas will be returned
     *
     * @return Stripes StreamingResolution or ErrorResolution
     */
    @HandlesEvent("forObligation")
    public Resolution getSchemasForObligation() {

        if (StringUtils.isEmpty(obligationId)) {
            return new ErrorResolution(HttpServletResponse.SC_BAD_REQUEST, "Missing obligationId parameter.");
        }
        List<DataSetTable> datasetTables;
        List<Schema> schemas;
        try {
            datasetTables = tableService.getTablesForObligation(obligationId, releasedOnly);
            schemas = schemaService.getSchemasForObligation(obligationId, releasedOnly);

            if (CollectionUtils.isNotEmpty(datasetTables) || CollectionUtils.isNotEmpty(schemas)) {
                String jsonResult = convertToJson(datasetTables, schemas);
                return new StreamingResolution("application/json", jsonResult);
            } else {
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND, "Schemas not found for this obligation.");
            }

        } catch (ServiceException e) {
            LOGGER.error(e.getMessage(), e);
            return new ErrorResolution(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System error occurred.");
        }
    }

    /**
     * Converts business objects to JSON result.
     *
     * @param datasetTables
     *            List of DataSetTable objects.
     * @param schemas
     *            List of Schema objects.
     * @return JSON string
     */
    private String convertToJson(List<DataSetTable> datasetTables, List<Schema> schemas) {

        String webAppUrl = Props.getRequiredProperty(PropsIF.DD_URL);
        if (webAppUrl.endsWith("/")) {
            webAppUrl = StringUtils.substringBeforeLast(webAppUrl, "/");
        }

        JSONArray itemList = new JSONArray();
        if (CollectionUtils.isNotEmpty(datasetTables)) {
            for (DataSetTable dsTable : datasetTables) {
                JSONObject ci = new JSONObject();
                ci.put("url", webAppUrl + "/GetSchema?id=TBL" + dsTable.getId());
                ci.put("identifier", dsTable.getIdentifier());
                ci.put("name", dsTable.getName());
                ci.put("status", dsTable.getDataSetStatus());
                itemList.add(ci);
            }
        }
        if (CollectionUtils.isNotEmpty(schemas)) {
            for (Schema schema : schemas) {
                JSONObject ci = new JSONObject();
                String relPath =
                        schemaRepository.getSchemaRelativePath(schema.getFileName(), schema.getSchemaSetIdentifier(), false);
                ci.put("url", webAppUrl + DownloadServlet.SCHEMAS_PATH + "/" + relPath);
                ci.put("identifier", schema.getSchemaSetIdentifier());
                ci.put("name", schema.getNameAttribute());
                ci.put("status", schema.getSchemaSetRegStatus().toString());
                itemList.add(ci);
            }
        }
        return itemList.toString(2);
    }

    /**
     * @return the obligationId
     */
    public String getObligationId() {
        return obligationId;
    }

    /**
     * @param obligationId
     *            the obligationId to set
     */
    public void setObligationId(String obligationId) {
        this.obligationId = obligationId;
    }

    /**
     * @return the releasedOnly
     */
    public boolean isReleasedOnly() {
        return releasedOnly;
    }

    /**
     * @param releasedOnly
     *            the releasedOnly to set
     */
    public void setReleasedOnly(boolean releasedOnly) {
        this.releasedOnly = releasedOnly;
    }

}
