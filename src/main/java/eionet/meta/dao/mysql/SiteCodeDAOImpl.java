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
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.meta.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.SiteCodeBoundElementIdentifiers;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.util.Util;
import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.ISiteCodeDAO;
import eionet.meta.dao.domain.SiteCodeStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyType;
import eionet.meta.service.data.SiteCode;
import eionet.meta.service.data.SiteCodeFilter;
import eionet.meta.service.data.SiteCodeResult;

/**
 * Site Code DAO implementation.
 *
 * @author Enriko Käsper
 */
@Repository
public class SiteCodeDAOImpl extends GeneralDAOImpl implements ISiteCodeDAO {

    /**
     * {@inheritDoc}
     */
    /** Data element DAO. */
    @Autowired
    private IDataElementDAO dataElementDAO;

    /**Vocabulary Concept DAO. */
    @Autowired
    private IVocabularyConceptDAO vocabularyConceptDAO;

    @Override
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws ParseException {

        /* Retrieve the elements' identifiers from the enumeration*/
        List<String> elementIdentifiers = Enumerations.SiteCodeBoundElementIdentifiers.getEnumValuesAsList();

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);

        /* Create a list of site codes */
        List<SiteCode> scList = createQueryAndRetrieveSiteCodes(filter, elementMap);
        SiteCodeResult result = new SiteCodeResult(scList, scList.size(), filter);
        return result;
    }

    /**
     * Creates the query and retrieves site codes
     *
     * @param filter filtering
     * @param elementMap map for elements' identifier and id
     * @return a list of site codes
     */
    public List<SiteCode> createQueryAndRetrieveSiteCodes(SiteCodeFilter filter, Map<String, Integer> elementMap) {

        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select vc.* ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");

        StringBuilder sqlWhereClause = new StringBuilder();
        sqlWhereClause.append("where v.VOCABULARY_TYPE = :siteCodeType  ");
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());

        if (StringUtils.isNotEmpty(filter.getSiteName())) {
            sqlWhereClause.append("and vc.LABEL like :text ");
            params.put("text", "%" + filter.getSiteName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            sql.append("and vc.IDENTIFIER like :identifier ");
            params.put("identifier", filter.getIdentifier());
        }
        if (StringUtils.isNotEmpty(filter.getUserAllocated())) {
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce1.DATAELEM_ID = :userAllocatedElemId and vce1.ELEMENT_VALUE like :userAllocated ");
            params.put("userAllocatedElemId", elementMap.get(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier()));
            params.put("userAllocated", filter.getUserAllocated());
        }
        if (filter.getDateAllocated() != null) {
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vc.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce2.DATAELEM_ID = :dateAllocatedElemId and vce2.ELEMENT_VALUE = :dateAllocated ");
            params.put("dateAllocatedElemId", elementMap.get(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier()));
            params.put("dateAllocated", filter.getDateAllocated());
        }
        if (filter.getStatus() != null) {
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce3 on vc.VOCABULARY_CONCEPT_ID=vce3.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce3.DATAELEM_ID = :statusElemId and vce3.ELEMENT_VALUE = :status ");
            params.put("statusElemId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
            params.put("status", filter.getStatus().toString());
        } else if (filter.isAllocatedUsedStatuses()) {
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce3 on vc.VOCABULARY_CONCEPT_ID=vce3.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce3.DATAELEM_ID = :statusElemId and vce3.ELEMENT_VALUE in (:statuses) ");
            params.put("statusElemId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
            params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
        }
        if (filter.getCountryCode() != null) {
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce4 on vc.VOCABULARY_CONCEPT_ID=vce4.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce4.DATAELEM_ID = :countryCodeElemId and vce4.ELEMENT_VALUE = :countryCode ");
            params.put("countryCodeElemId", elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()));
            params.put("countryCode", filter.getCountryCode());
        }

        sql.append(sqlWhereClause);

        // sorting
        if (StringUtils.isNotEmpty(filter.getSortProperty())) {
            if (filter.getSortProperty().equals("identifier")) {
                sql.append("order by IDENTIFIER + 0");
            } else {
                sql.append("order by " + filter.getSortProperty());
            }
            if (SortOrderEnum.ASCENDING.equals(filter.getSortOrder())) {
                sql.append(" ASC ");
            } else {
                sql.append(" DESC ");
            }
        } else {
            sql.append("order by IDENTIFIER + 0 ");
        }
        if (filter.isUsePaging()) {
            sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
        }

        List<SiteCode> scList = getSiteCodeList(sql.toString(), params, elementMap);
        return scList;
    }

    /**
     * Executes a query and returns a site code list
     *
     * @param query the sql query
     * @param params the parameters for the query
     * @param elementMap map for elements' identifier and id
     * @return a list of site codes
     */
    public List<SiteCode> getSiteCodeList(String query, Map<String, Object> params, Map<String, Integer> elementMap) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SiteCode> resultList = getNamedParameterJdbcTemplate().query(query, params, new RowMapper<SiteCode>() {
            @Override
            public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteCode sc = new SiteCode();
                sc.setId(rs.getInt("vc.VOCABULARY_CONCEPT_ID"));
                sc.setIdentifier(rs.getString("vc.IDENTIFIER"));
                sc.setLabel(rs.getString("vc.LABEL"));
                sc.setDefinition(rs.getString("vc.DEFINITION"));
                sc.setNotation(rs.getString("vc.NOTATION"));

                if(elementMap != null && elementMap.size() != 0) {

                    /* Retrieve a hashmap that contains the data element's id (key) and the element's value (value)*/
                    Map<Integer, String> elementInfo = getBoundElementIdAndValue(sc.getId(), new ArrayList<>(elementMap.values()));

                    /*Iterate through the hashmap and fill the SiteCode object*/
                    for (Map.Entry<Integer, String> entry : elementInfo.entrySet()) {
                        /* Get element idntifier based on element id*/
                        Set<String> elementIdentifierSet = Util.getKeysByValue(elementMap, entry.getKey());

                        /*map the identifier to the field that the data should be stored in*/
                        String identifier = elementIdentifierSet.iterator().next();
                        if (identifier.equals(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier())) {
                            sc.setCountryCode(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier())) {
                            sc.setInitialSiteName(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier())) {
                            sc.setSiteCodeStatus(SiteCodeStatus.valueOf(entry.getValue()));
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier())) {
                            try {
                                sc.setDateAllocated(formatter.parse(entry.getValue()));
                            } catch (ParseException e) {
                                LOGGER.error("Error while parsing allocated date for site code with element id: #%d", entry.getKey());
                            }
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier())) {
                            sc.setUserAllocated(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier())) {
                            sc.setUserCreated(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier())) {
                            try {
                                sc.setDateCreated(formatter.parse(entry.getValue()));
                            } catch (ParseException e) {
                                LOGGER.error("Error while parsing allocated date for site code with element id: #" + entry.getKey());
                            }
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.YEARS_DELETED.getIdentifier())) {
                            sc.setYearsDeleted(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.YEARS_DISAPPEARED.getIdentifier())) {
                            sc.setYearsDisappeared(entry.getValue());
                        }
                    }
                }
                return sc;
            }
        });
        return resultList;
    }

    /**
     * Returns a list of SiteCode objects which contains information for the codes.
     *
     * @param vcId the site code
     * @param dataElementIds
     * @return a hashmap with key the element's id and value, the element's value
     */
    @Override
    public Map<Integer, String> getBoundElementIdAndValue(Integer vcId, List<Integer> dataElementIds) {
        StringBuilder sqlForElementValue = new StringBuilder();
        sqlForElementValue.append("select vce.DATAELEM_ID, vce.ELEMENT_VALUE from VOCABULARY_CONCEPT_ELEMENT vce inner join VOCABULARY_CONCEPT vc on " +
                " vce.VOCABULARY_CONCEPT_ID = vc.VOCABULARY_CONCEPT_ID inner join VOCABULARY v on v.VOCABULARY_ID = vc.VOCABULARY_ID " +
                "where vce.DATAELEM_ID in (:dataElemIds) and vce.VOCABULARY_CONCEPT_ID = :vocabularyConceptId " +
                "and v.VOCABULARY_TYPE = :siteCodeType ");
        Map<String, Object> paramsForElementValue = new HashMap<String, Object>();
        paramsForElementValue.put("dataElemIds", dataElementIds);
        paramsForElementValue.put("vocabularyConceptId", vcId);
        paramsForElementValue.put("siteCodeType", VocabularyType.SITE_CODE.name());

        /* Retrieve all bound elements for site codes based on vocabulary concept id */
        List<Map<Integer, String>> elementInfoMapList = getNamedParameterJdbcTemplate().query(sqlForElementValue.toString(), paramsForElementValue, new RowMapper<Map<Integer, String> >() {
            @Override
            public Map<Integer, String>  mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<Integer, String> result = new HashMap<>();
                result.put(rs.getInt("vce.DATAELEM_ID"), rs.getString(("vce.ELEMENT_VALUE")));
                return result;
            }
        });

        /* Convert list to map */
        Map<Integer, String> elementInfo = new HashMap();
        for ( Map<Integer, String> singleLineMap : elementInfoMapList) {
            for (Integer key : singleLineMap.keySet()) {
                elementInfo.put(key, singleLineMap.get(key));
            }
        }
        return elementInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertUserAndDateCreatedForSiteCodes(List<VocabularyConcept> vocabularyConcepts, String userName) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sql.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        Date dateCreated = new Date();
        @SuppressWarnings("unchecked")
        /*The size of the map will be the concepts' size * 2 because of the two bound elements*/
        Map<String, Object>[] batchValues = new HashMap[vocabularyConcepts.size()*2];

        //retrieve data element id for identifier sitecodes_DATE_CREATED and sitecodes_USER_CREATED
        String dateCreatedIdentifier = SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier();
        String userCreatedIdentifier = SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier();

        int dateCreatedElementId = dataElementDAO.getCommonDataElementId(dateCreatedIdentifier);
        int userCreatedElementId = dataElementDAO.getCommonDataElementId(userCreatedIdentifier);

        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", dateCreatedIdentifier, dateCreatedElementId));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", userCreatedIdentifier, userCreatedElementId));

        int batchValuesCounter = 0;
        /*A loop is performed in order to insert the username that reserved the site codes and the date*/
        for(int j=0; j < 2; j++) {

            for (int i = 0; i < vocabularyConcepts.size(); i++) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("vocabularyConceptId", vocabularyConcepts.get(i).getId());

                if (j == 0) {
                    params.put("dataElemId", dateCreatedElementId);
                    params.put("elementValue", dateCreated);
                } else {
                    params.put("dataElemId", userCreatedElementId);
                    params.put("elementValue", userName);
                }
                batchValues[batchValuesCounter] = params;
                batchValuesCounter++;
            }
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(List<SiteCode> freeSiteCodes, String countryCode, String userName, String[] siteNames,
            Date allocationTime) {

            /* TODO
                insert value of bound elements:  sitecodes_CC_ISO2, sitecodes_INITIAL_SITE_NAME, sitecodes_STATUS, sitecodes_DATE_ALLOCATED, sitecodes_USER_ALLOCATED
            */

        StringBuilder sql = new StringBuilder();
        sql.append("update T_SITE_CODE set CC_ISO2 = :country, INITIAL_SITE_NAME = :siteName, STATUS = :status, "
                + "DATE_ALLOCATED = :dateAllocated, USER_ALLOCATED = :userAllocated ");
        sql.append("where VOCABULARY_CONCEPT_ID = :vocabularyConceptId");


        StringBuilder sqlForBoundElements = new StringBuilder();
        sqlForBoundElements.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sqlForBoundElements.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        List<String> elementIdentifiers = Enumerations.SiteCodeBoundElementIdentifiers.getEnumValuesAsList();

        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);



        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[siteNames.length];
        int batchValuesCounter = 0;

        for ( Map.Entry<String, Integer> entry : elementMap.entrySet()) {
            for (int i = 0; i < freeSiteCodes.size(); i++) {
                Map<String, Object> paramsForBoundElements = new HashMap<String, Object>();
                paramsForBoundElements.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
                paramsForBoundElements.put("dataElemId", entry.getValue());
                if(entry.getKey().equals("sitecodes_CC_ISO2")){
                    paramsForBoundElements.put("elementValue", countryCode);
                }
                else if(entry.getKey().equals("sitecodes_INITIAL_SITE_NAME")){
                    if (siteNames.length > i && siteNames[i] != null) {
                        paramsForBoundElements.put("elementValue",  siteNames[i]);
                    } else {
                        paramsForBoundElements.put("elementValue", "");
                    }
                }
                else if(entry.getKey().equals("sitecodes_STATUS")){
                    paramsForBoundElements.put("elementValue", SiteCodeStatus.ALLOCATED.name());
                }
                else if(entry.getKey().equals("sitecodes_DATE_ALLOCATED")){
                    paramsForBoundElements.put("elementValue", allocationTime);
                }
                else if(entry.getKey().equals("sitecodes_USER_ALLOCATED")){
                    paramsForBoundElements.put("elementValue", userName);
                }
                if(batchValues.length > i) {
                    batchValues[batchValuesCounter] = paramsForBoundElements;
                    batchValuesCounter++;
                }
            }
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);








        //TODO query below can be updated to use vocabulary_concept_id ?
        // update place-holder value in concept label to <allocated>
        StringBuilder sqlForConcepts = new StringBuilder();
        sqlForConcepts.append("update VOCABULARY_CONCEPT set LABEL = :label where VOCABULARY_CONCEPT_ID IN "
                + " (select VOCABULARY_CONCEPT_ELEMENT.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT "
                + "INNER JOIN DATAELEM on VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID = DATAELEM.DATAELEM_ID "
                + "WHERE VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID IN "
                + "(SELECT VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID FROM DATAELEM INNER JOIN VOCABULARY_CONCEPT_ELEMENT on VOCABULARY_CONCEPT_ELEMENT.DATAELEM_ID = DATAELEM.DATAELEM_ID "
                + "WHERE (IDENTIFIER='sitecodes_STATUS' AND ELEMENT_VALUE = :status) OR (IDENTIFIER='sitecodes_DATE_ALLOCATED' AND ELEMENT_VALUE = dateAllocated) "
                + "OR (IDENTIFIER='sitecodes_USER_ALLOCATED' AND ELEMENT_VALUE = userAllocated)))");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("status", SiteCodeStatus.ALLOCATED.name());
        parameters.put("dateAllocated", allocationTime);
        parameters.put("userAllocated", userName);
        parameters.put("label", "<" + SiteCodeStatus.ALLOCATED.name().toLowerCase() + ">");
        getNamedParameterJdbcTemplate().update(sqlForConcepts.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSiteCodeVocabularyFolderId() {

        StringBuilder sql = new StringBuilder();
        sql.append("select min(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFeeSiteCodeAmount() {
        String statusIdentifier = SiteCodeBoundElementIdentifiers.STATUS.getIdentifier();
        int statusElementId = dataElementDAO.getCommonDataElementId(statusIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce on vc.VOCABULARY_CONCEPT_ID=vce.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType " );
        sql.append("and vce.DATAELEM_ID = :statusElementId and vce.ELEMENT_VALUE = :status ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", SiteCodeStatus.AVAILABLE.name());
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", statusElementId);

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountryUnusedAllocations(String countryCode, boolean withoutInitialName) {
        String statusIdentifier = SiteCodeBoundElementIdentifiers.STATUS.getIdentifier();
        String countryCodeIdentifier = SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier();

        int statusElementId = dataElementDAO.getCommonDataElementId(statusIdentifier);
        int countryCodeElementId = dataElementDAO.getCommonDataElementId(countryCodeIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vce1.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType and vce1.ID != vce2.ID " );
        sql.append("and vce1.DATAELEM_ID = :statusElementId and vce1.ELEMENT_VALUE in (:statuses) ");
        sql.append("and vce2.DATAELEM_ID = :countryCodeElementId and vce2.ELEMENT_VALUE = :countryCode ");

        if(withoutInitialName) {
            sql.append("and vc.VOCABULARY_CONCEPT_ID not in (select vce3.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT vce3 ");
            sql.append("where vce3.DATAELEM_ID = :initialNameElementId and vce3.ELEMENT_VALUE is not null and vce3.ELEMENT_VALUE != '') ");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryCode);
        params.put("statuses", SiteCodeStatus.ALLOCATED.name());
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", statusElementId);
        params.put("countryCodeElementId", countryCodeElementId);

        if (withoutInitialName){
            String initialNameIdentifier = SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier();
            int initialNameElementId = dataElementDAO.getCommonDataElementId(initialNameIdentifier);
            params.put("initialNameElementId", initialNameElementId);
        }

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountryUsedAllocations(String countryCode) {

        String statusIdentifier = SiteCodeBoundElementIdentifiers.STATUS.getIdentifier();
        String countryCodeIdentifier = SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier();

        int statusElementId = dataElementDAO.getCommonDataElementId(statusIdentifier);
        int countryCodeElementId = dataElementDAO.getCommonDataElementId(countryCodeIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vce1.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType and vce1.ID != vce2.ID " );
        sql.append("and vce1.DATAELEM_ID = :statusElementId and vce1.ELEMENT_VALUE in (:statuses) ");
        sql.append("and vce2.DATAELEM_ID = :countryCodeElementId and vce2.ELEMENT_VALUE = :countryCode");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryCode);
        params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", statusElementId);
        params.put("countryCodeElementId", countryCodeElementId);

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean siteCodeFolderExists() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class) > 0;
    }

}
