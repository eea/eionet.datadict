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
 * Agency. Portions created by TripleDev or Zero TechnoLOGGERies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.web.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;

import eionet.meta.DDSearchEngine;
import eionet.meta.DDSearchParameter;
import eionet.meta.DElemAttribute;
import eionet.meta.DsTable;
import eionet.meta.dao.domain.DataSetTable;
import eionet.util.sql.ConnectionUtil;

/**
 * Table search action bean.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/tableSearch.action")
public class TableSearchActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(TableSearchActionBean.class);

    /** Search type option in web form. */
    private static String SUBSTRING_SEARCH = "SUBSTRING_SEARCH";
    /** Search type option in web form. */
    private static String EXACT_SEARCH = "EXACT_SEARCH";

    /** Form fields. */
    private String shortName;
    private String identifier;
    private String name;
    private String definition;
    private String keyword;
    private String searchType = SUBSTRING_SEARCH;
    private String descriptiveImage;
    private String eeaIssue;
    private String methodology;
    private String shortDescription;

    /** Table search result. */
    private List<DataSetTable> dataSetTables;

    /**
     * Handles the form page view.
     *
     * @return
     */
    public Resolution form() {
        return new ForwardResolution("/pages/tableSearch.jsp");
    }

    @DefaultHandler
    public Resolution search() {
        // Call search dao.
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("short_name", shortName);
        parameters.put("idfier", identifier);
        parameters.put("attr_1", name);
        parameters.put("attr_4", definition);
        parameters.put("attr_5", keyword);
        parameters.put("attr_15", shortDescription);
        parameters.put("attr_17", methodology);
        parameters.put("attr_37", eeaIssue);
        parameters.put("attr_40", descriptiveImage);
        parameters.put("type", "TBL");

        searchTables(searchType, parameters);
        return new ForwardResolution("/pages/tableResult.jsp");
    }

    public Resolution reset() {
        // Redirect to the same page
        return new RedirectResolution(getContext().getRequest().getServletPath());
    }

    private void searchTables(String srchType, Map<String, String> parameters) {
        Connection conn = null;

        try { // start the whole page try block

            // FIXME
            // session.removeAttribute(oSearchCacheAttrName);

            // we establish a database connection and create a search engine
            conn = ConnectionUtil.getConnection();
            DDSearchEngine searchEngine = new DDSearchEngine(conn, "", null);
            // FIXME
            // searchEngine.setUser(user);

            String oper = "=";
            if (srchType != null && srchType.equals(EXACT_SEARCH)) {
                oper = " match ";
            }
            if (srchType != null && srchType.equals(SUBSTRING_SEARCH)) {
                oper = " like ";
            }

            Vector params = new Vector();
            /*
             * Enumeration parNames = request.getParameterNames(); while (parNames.hasMoreElements()){ String parName =
             * (String)parNames.nextElement(); if (!parName.startsWith(ATTR_PREFIX)) continue;
             *
             * String parValue = request.getParameter(parName); if (parValue.length()==0) continue;
             *
             * DDSearchParameter param = new DDSearchParameter(parName.substring(ATTR_PREFIX.length()), null, oper, "=");
             *
             * if (oper!= null && oper.trim().equalsIgnoreCase("like")) param.addValue("'%" + parValue + "%'"); else
             * param.addValue("'" + parValue + "'"); params.add(param); }
             */
            for (String key : parameters.keySet()) {
                String parName = key;
                String parValue = parameters.get(key);
                LOGGER.info(parName + ":" + parValue);
                if (parValue == null || parValue.length() == 0) {
                    continue;
                }

                String attrPrefix = "attr_";
                if (!parName.startsWith(attrPrefix)) {
                    continue;
                }

                DDSearchParameter param = new DDSearchParameter(parName.substring(attrPrefix.length()), null, oper, "=");
                if (oper != null && oper.trim().equalsIgnoreCase("like")) {
                    param.addValue("'%" + parValue + "%'");
                } else {
                    param.addValue("'" + parValue + "'");
                }
                params.add(param);
            }

            Vector dsTables = searchEngine.getDatasetTables(params, shortName, identifier, "", definition, oper);

            // see if any result were found
            if (dsTables == null || dsTables.size() == 0) {
                return;
            }

            DElemAttribute attr = null;

            // c_SearchResultSet oResultSet=new c_SearchResultSet();
            // oResultSet.isAuth = user!=null;
            // oResultSet.oElements=new Vector();
            // session.setAttribute(oSearchCacheAttrName,oResultSet);

            dataSetTables = new ArrayList<DataSetTable>();
            for (int i = 0; i < dsTables.size(); i++) {
                DsTable table = (DsTable) dsTables.get(i);
                String table_id = table.getID();
                String table_name = table.getShortName();
                String ds_id = table.getDatasetID();
                String ds_name = table.getDatasetName();
                String dsNs = table.getParentNs();
                String tblName = table.getName() == null ? "" : table.getName();
                tblName = tblName.length() > 60 && tblName != null ? tblName.substring(0, 60) + " ..." : tblName;
                String tblFullName = tblName;
                String workingUser = table.getDstWorkingUser();

                /*
                 * String zebraClass = i % 2 != 0 ? "zebraeven" : "zebraodd"; String regStatus = table.getDstStatus(); boolean
                 * clickable = regStatus!=null ? !searchEngine.skipByRegStatus(regStatus) : true; String strDisabled = clickable ?
                 * "" : " class=\"disabled\""; String statusImg = "images/" + Util.getStatusImage(regStatus); String statusTxt =
                 * Util.getStatusRadics(regStatus);
                 *
                 * c_SearchResultEntry oEntry = new c_SearchResultEntry(table_id, ds_id, table_name, tblFullName, ds_name);
                 * oEntry.workingUser = workingUser; oEntry.clickable = clickable; oEntry.regStatus = regStatus;
                 * oResultSet.oElements.add(oEntry);
                 */

                DataSetTable dst = new DataSetTable();
                dst.setDataSetId(table.getDatasetID());
                dst.setShortName(table.getShortName());
                dst.setId(table.getID());
                dst.setDataSetName(table.getDatasetName());
                dst.setName(table.getName());
                dst.setDataSetStatus(table.getDstStatus());

                dataSetTables.add(dst);
            }
        } catch (Exception e) {
            LOGGER.error("Table search failed", e);
        }
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName
     *            the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * @param definition
     *            the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword
     *            the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return the searchType
     */
    public String getSearchType() {
        return searchType;
    }

    /**
     * @param searchType
     *            the searchType to set
     */
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    /**
     * @return the dataSetTables
     */
    public List<DataSetTable> getDataSetTables() {
        return dataSetTables;
    }

    /**
     * @param dataSetTables
     *            the dataSetTables to set
     */
    public void setDataSetTables(List<DataSetTable> dataSetTables) {
        this.dataSetTables = dataSetTables;
    }

    /**
     * @return the descriptiveImage
     */
    public String getDescriptiveImage() {
        return descriptiveImage;
    }

    /**
     * @param descriptiveImage
     *            the descriptiveImage to set
     */
    public void setDescriptiveImage(String descriptiveImage) {
        this.descriptiveImage = descriptiveImage;
    }

    /**
     * @return the eeaIssue
     */
    public String getEeaIssue() {
        return eeaIssue;
    }

    /**
     * @param eeaIssue
     *            the eeaIssue to set
     */
    public void setEeaIssue(String eeaIssue) {
        this.eeaIssue = eeaIssue;
    }

    /**
     * @return the methodology
     */
    public String getMethodology() {
        return methodology;
    }

    /**
     * @param methodology
     *            the methodology to set
     */
    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @param shortDescription
     *            the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

}
