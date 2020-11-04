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
import java.util.stream.Collectors;


import eionet.datadict.model.enums.Enumerations;
import eionet.datadict.model.enums.Enumerations.SiteCodeBoundElementIdentifiers;
import eionet.meta.dao.IDataElementDAO;
import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.util.Pair;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.EnumUtils;
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
    public SiteCodeResult searchSiteCodes(SiteCodeFilter filter) throws Exception {
        StopWatch timer = new StopWatch();
        timer.start();

        /* Retrieve the elements' identifiers from the enumeration*/
        List<String> elementIdentifiers = Enumerations.SiteCodeBoundElementIdentifiers.getEnumValuesAsList();

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);
        LOGGER.info(String.format("Retrieved ids for site code bound elements"));

        /* Create a list of site codes */
        Pair<Integer, List<SiteCode>> siteCodesPair = createQueryAndRetrieveSiteCodes(filter, elementMap);
        LOGGER.info(String.format("Retrieved site code list"));
        SiteCodeResult result = new SiteCodeResult(siteCodesPair.getRight(), siteCodesPair.getLeft(), filter);
        timer.stop();
        LOGGER.info("Method searchSiteCodes lasted: " + timer.toString());
        return result;
    }

    /**
     * Creates the query and retrieves site codes
     *
     * @param filter filtering
     * @param elementMap map for elements' identifier and id
     * @return a pair where left is the total number of site codes and right is the site code list
     */
    public Pair<Integer, List<SiteCode>> createQueryAndRetrieveSiteCodes(SiteCodeFilter filter, Map<String, Integer> elementMap) throws Exception {
        StopWatch timer = new StopWatch();
        timer.start();

        Map<String, Object> params = new HashMap<>();
        StringBuilder sqlBeggining = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        sqlBeggining.append("select vc.* ");
        sql.append("from VOCABULARY_CONCEPT vc ");

        int vocabularyFolderId = this.getSiteCodeVocabularyFolderId();

        StringBuilder sqlWhereClause = new StringBuilder();
        sqlWhereClause.append(" where vc.VOCABULARY_ID = :siteCodesVocabularyId ");
        params.put("siteCodesVocabularyId", vocabularyFolderId);

        if (StringUtils.isNotEmpty(filter.getSiteName())) {
            sqlWhereClause.append(" and vc.LABEL like :text ");
            params.put("text", "%" + filter.getSiteName() + "%");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            sqlWhereClause.append("and vc.IDENTIFIER like :identifier ");
            params.put("identifier", filter.getIdentifier());
        }
        if (StringUtils.isNotEmpty(filter.getUserAllocated())) {
            if ( elementMap.get(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier()) == null){
                String exMsg = String.format("There was no element id found for identifier: %s", SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier());
                throw new Exception(exMsg);
            }
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce1.DATAELEM_ID = :userAllocatedElemId and vce1.ELEMENT_VALUE like :userAllocated ");
            params.put("userAllocatedElemId", elementMap.get(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier()));
            params.put("userAllocated", filter.getUserAllocated());
        }
        if (filter.getDateAllocated() != null) {
            if ( elementMap.get(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier()) == null){
                String exMsg = String.format("There was no element id found for identifier: %s", SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier());
                throw new Exception(exMsg);
            }
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vc.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce2.DATAELEM_ID = :dateAllocatedElemId and vce2.ELEMENT_VALUE = :dateAllocated ");
            params.put("dateAllocatedElemId", elementMap.get(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier()));
            params.put("dateAllocated", filter.getDateAllocated());
        }
        if (filter.getStatus() != null) {
            if ( elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()) == null){
                String exMsg = String.format("There was no element id found for identifier: %s", SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());
                throw new Exception(exMsg);
            }
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce3 on vc.VOCABULARY_CONCEPT_ID=vce3.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce3.DATAELEM_ID = :statusElemId and vce3.ELEMENT_VALUE = :status ");
            params.put("statusElemId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
            params.put("status", filter.getStatus().toString());
        } else if (filter.isAllocatedUsedStatuses()) {
            if ( elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()) == null){
                String exMsg = String.format("There was no element id found for identifier: %s", SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());
                throw new Exception(exMsg);
            }
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce3 on vc.VOCABULARY_CONCEPT_ID=vce3.VOCABULARY_CONCEPT_ID ");
            sqlWhereClause.append("and vce3.DATAELEM_ID = :statusElemId and vce3.ELEMENT_VALUE in (:statuses) ");
            params.put("statusElemId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
            params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
        }
        if (filter.getCountryCode() != null) {
            if ( elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()) == null){
                String exMsg = String.format("There was no element id found for identifier: %s", SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier());
                throw new Exception(exMsg);
            }
            sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce4 on vc.VOCABULARY_CONCEPT_ID=vce4.VOCABULARY_CONCEPT_ID ");
            sql.append("left join VOCABULARY_CONCEPT vc2 on vc2.VOCABULARY_CONCEPT_ID=vce4.RELATED_CONCEPT_ID ");
            sqlWhereClause.append("and vce4.DATAELEM_ID = :countryCodeElemId and (vce4.ELEMENT_VALUE like :countryCode or vc2.IDENTIFIER like :countryCode) ");
            params.put("countryCodeElemId", elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()));
            /* The symbol '%' is inserted in the beggining of the country code in order to retrieve all country codes without taking into consideration the url (rdf resource)*/
            StringBuilder countryCode = new StringBuilder(filter.getCountryCode());
            countryCode.insert(0, '%');
            params.put("countryCode", countryCode.toString());
        }

        sql.append(sqlWhereClause);

        // sorting
        if (StringUtils.isNotEmpty(filter.getSortProperty())) {
            if (filter.getSortProperty().equals("identifier")) {
                sql.append("order by vc.IDENTIFIER + 0");
            } else {
                sql.append("order by " + filter.getSortProperty());
            }
            if (SortOrderEnum.ASCENDING.equals(filter.getSortOrder())) {
                sql.append(" ASC ");
            } else {
                sql.append(" DESC ");
            }
        } else {
            sql.append("order by vc.IDENTIFIER + 0 ");
        }

        StringBuilder sqlWithoutLimit = new StringBuilder();
        sqlWithoutLimit.append("select count(*) ");
        sqlWithoutLimit.append(sql.toString());

        if (filter.isUsePaging()) {
            sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
        }
        else{
            if (filter.getNumberOfElements() != null) {
                sql.append("LIMIT ").append(filter.getNumberOfElements());
            }
        }

        StringBuilder finalSqlWithLimit = new StringBuilder();
        finalSqlWithLimit.append(sqlBeggining.toString());
        finalSqlWithLimit.append(sql.toString());

        LOGGER.debug(String.format("Query is: '%s'", finalSqlWithLimit.toString()));
        String parameterStr = "";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            parameterStr += entry.getKey() + ":" + entry.getValue().toString() + " ";
        }
        LOGGER.debug(String.format("Parameters are: '%s'", parameterStr));
        List<SiteCode> scList = getSiteCodeList(finalSqlWithLimit.toString(), params, elementMap);

        Integer totalItems = getNamedParameterJdbcTemplate().queryForObject(sqlWithoutLimit.toString(), params, Integer.class);
        LOGGER.info(String.format("The total number of results is: %d", totalItems));

        Pair<Integer, List<SiteCode>> scPair= new Pair<>(totalItems, scList);
        timer.stop();
        LOGGER.info("Method createQueryAndRetrieveSiteCodes lasted: " + timer.toString());
        return scPair;
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
        StopWatch timer = new StopWatch();
        timer.start();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        List<SiteCode> resultList = getNamedParameterJdbcTemplate().query(query, params, new RowMapper<SiteCode>() {
            @Override
            public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                StopWatch timer = new StopWatch();
                timer.start();
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
                            if(entry.getValue() != null) {
                                String countryCode = entry.getValue();
                                StringBuilder countryCodeSb = new StringBuilder();
                                countryCodeSb.append(Props.getRequiredProperty(PropsIF.DD_URL));
                                countryCodeSb.append("/vocabulary/common/countries/");
                                if(countryCode.contains(countryCodeSb.toString())) {
                                    countryCode = countryCode.replaceAll(countryCodeSb.toString(), "");
                                }
                                sc.setCountryCode(countryCode);
                            }
                            else{
                                //get identifier of related concept
                                sc.setCountryCode(vocabularyConceptDAO.getIdentifierOfRelatedConcept(sc.getId(), entry.getKey()));
                            }
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier())) {
                            sc.setInitialSiteName(entry.getValue());
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier())) {
                            if(EnumUtils.isValidEnum(SiteCodeStatus.class, entry.getValue())){
                                sc.setSiteCodeStatus(SiteCodeStatus.valueOf(entry.getValue()));
                            }
                            else{
                                sc.setSiteCodeStatus(null);
                            }
                        } else if (identifier.equals(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier())) {
                            try {
                                sc.setDateAllocated(formatter.parse(entry.getValue()));
                            } catch (ParseException e) {
                                LOGGER.error("Error while parsing allocated date for site code with element id: #" +entry.getKey());
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
                timer.stop();
                LOGGER.info("Method getSiteCodeList (creating the site codes list) lasted: " + timer.toString());
                return sc;
            }
        });
        timer.stop();
        LOGGER.info("Method getSiteCodeList lasted: " + timer.toString());
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
        StopWatch timer = new StopWatch();
        timer.start();

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
        timer.stop();
        LOGGER.info("Method getBoundElementIdAndValue lasted: " + timer.toString());
        return elementInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertAvailableSiteCodes(List<VocabularyConcept> vocabularyConcepts, String userName) throws Exception {
        StopWatch timer = new StopWatch();
        timer.start();
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sql.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        Date dateCreated = new Date();
        @SuppressWarnings("unchecked")
        /*The size of the map will be the concepts' size * 3 because of the three bound elements*/
        Map<String, Object>[] batchValues = new HashMap[vocabularyConcepts.size()*3];

        /* Create a list for the identifiers needed*/
        List<String> elementIdentifiers = new ArrayList<>();
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);

        if (elementMap.containsKey(SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier())) {
            LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier())));
        }
        else{
            String exMsg = String.format("Data element with identifier '%s' doesn't exist", SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier());
            throw new Exception(exMsg);
        }
        if (elementMap.containsKey(SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier())) {
            LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier())));
        }
        else{
            String exMsg = String.format("Data element with identifier '%s' doesn't exist", SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier());
            throw new Exception(exMsg);
        }
        if (elementMap.containsKey(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier())) {
            LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.STATUS.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier())));
        }
        else{
            String exMsg = String.format("Data element with identifier '%s' doesn't exist", SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());
            throw new Exception(exMsg);
        }

        int batchValuesCounter = 0;
        for (VocabularyConcept vocabularyConcept : vocabularyConcepts) {
            //insert date created information
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyConceptId", vocabularyConcept.getId());
            params.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.DATE_CREATED.getIdentifier()));
            params.put("elementValue", dateCreated);
            batchValues[batchValuesCounter] = params;
            batchValuesCounter++;

            //insert user created information
            Map<String, Object> params2 = new HashMap<String, Object>();
            params2.put("vocabularyConceptId", vocabularyConcept.getId());
            params2.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.USER_CREATED.getIdentifier()));
            params2.put("elementValue", userName);
            batchValues[batchValuesCounter] = params2;
            batchValuesCounter++;

            //insert available status
            Map<String, Object> params3 = new HashMap<String, Object>();
            params3.put("vocabularyConceptId", vocabularyConcept.getId());
            params3.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
            params3.put("elementValue", SiteCodeStatus.AVAILABLE.name());
            batchValues[batchValuesCounter] = params3;
            batchValuesCounter++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
        timer.stop();
        LOGGER.info("Method insertAvailableSiteCodes lasted: " + timer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void allocateSiteCodes(List<SiteCode> freeSiteCodes, String countryCode, String userName, String[] siteNames,
            Date allocationTime) throws Exception {

        if(freeSiteCodes == null || freeSiteCodes.size() == 0){
            throw new Exception("No site codes were allocated");
        }
        StopWatch timer = new StopWatch();
        timer.start();

        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT_ELEMENT (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE) ");
        sql.append("values (:vocabularyConceptId, :dataElemId, :elementValue)");

        Date dateCreated = new Date();
        @SuppressWarnings("unchecked")
        /*The size of the map will be the site codes size * 5 because of the 4 bound elements*/
        Map<String, Object>[] batchValues = new HashMap[freeSiteCodes.size() * 4];

        /* Create a list for the identifiers needed*/
        List<String> elementIdentifiers = new ArrayList<>();
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()); // This will be updated instead of inserted
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier());

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);

        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.STATUS.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier())));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier())));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier())));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier())));
        LOGGER.info(String.format("Data element id for identifier '%s' is #%s", SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier(), elementMap.get(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier())));

        /* update the status for the site codes */
        List<Integer> vocabularyIds = freeSiteCodes.stream().map(SiteCode::getId).collect(Collectors.toList());
        updateSiteCodeStatus(vocabularyIds, elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()), SiteCodeStatus.ALLOCATED.name());

        /* insert the rest of the information */
        int batchValuesCounter = 0;
        for (int i = 0; i < freeSiteCodes.size(); i++) {

            String countryUrl = getFullCountryUrl(countryCode);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
            params.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()));
            params.put("elementValue", countryUrl);
            batchValues[batchValuesCounter] = params;
            batchValuesCounter++;

            //insert initial site name information
            Map<String, Object> params1 = new HashMap<String, Object>();
            params1.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
            params1.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier()));
            if (siteNames.length > i && siteNames[i] != null) {
                params1.put("elementValue", siteNames[i]);
            } else {
                params1.put("elementValue", "");
            }
            batchValues[batchValuesCounter] = params1;
            batchValuesCounter++;

            //insert date allocated information
            Map<String, Object> params2 = new HashMap<String, Object>();
            params2.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
            params2.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.DATE_ALLOCATED.getIdentifier()));
            params2.put("elementValue", allocationTime);
            batchValues[batchValuesCounter] = params2;
            batchValuesCounter++;

            //insert user allocated information
            Map<String, Object> params3 = new HashMap<String, Object>();
            params3.put("vocabularyConceptId", freeSiteCodes.get(i).getId());
            params3.put("dataElemId", elementMap.get(SiteCodeBoundElementIdentifiers.USER_ALLOCATED.getIdentifier()));
            params3.put("elementValue", userName);
            batchValues[batchValuesCounter] = params3;
            batchValuesCounter++;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
        timer.stop();
        LOGGER.info("Method allocateSiteCodes lasted: " + timer.toString());
    }

    @Override
    public void updateSiteCodeStatus(List<Integer> vcIds, Integer statusId, String status) throws Exception {

        if(vcIds == null || vcIds.size() == 0){
            throw new Exception("No site codes were given for status update");
        }
        StopWatch timer = new StopWatch();
        timer.start();

        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT_ELEMENT set ELEMENT_VALUE = :elementValue ");
        sql.append("where VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds) and DATAELEM_ID = :dataElemId ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyConceptIds", vcIds);
        params.put("dataElemId", statusId);
        params.put("elementValue", status);

        getNamedParameterJdbcTemplate().update(sql.toString(), params);
        LOGGER.info(String.format("Status for site codes with ids %s was successfully updated to %s",vcIds.toString(), status));
        timer.stop();
        LOGGER.info("Method updateSiteCodeStatus lasted: " + timer.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSiteCodeVocabularyFolderId() {

        StopWatch timer = new StopWatch();
        timer.start();
        StringBuilder sql = new StringBuilder();
        sql.append("select min(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        timer.stop();
        LOGGER.info("Method getSiteCodeVocabularyFolderId lasted: " + timer.toString());
        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFeeSiteCodeAmount() {
        StopWatch timer = new StopWatch();
        timer.start();
        String statusIdentifier = SiteCodeBoundElementIdentifiers.STATUS.getIdentifier();
        int statusElementId = dataElementDAO.getCommonDataElementId(statusIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce on vc.VOCABULARY_CONCEPT_ID=vce.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType " );
        sql.append("and vce.DATAELEM_ID = :statusElementId and vce.ELEMENT_VALUE = :status ");
        sql.append("and v.WORKING_COPY = 0 ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", SiteCodeStatus.AVAILABLE.name());
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", statusElementId);

        timer.stop();
        LOGGER.info("Method getFeeSiteCodeAmount lasted: " + timer.toString());
        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountryUnusedAllocations(String countryCode, boolean withoutInitialName) {

        StopWatch timer = new StopWatch();
        timer.start();

        /* Create a list for the identifiers needed*/
        List<String> elementIdentifiers = new ArrayList<>();
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier());

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vce1.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType and v.WORKING_COPY = 0 and vce1.ID != vce2.ID " );
        sql.append("and vce1.DATAELEM_ID = :statusElementId and vce1.ELEMENT_VALUE in (:statuses) ");
        sql.append("and vce2.DATAELEM_ID = :countryCodeElementId and vce2.ELEMENT_VALUE = :countryCode ");

        if(withoutInitialName) {
            sql.append("and vc.VOCABULARY_CONCEPT_ID not in (select vce3.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT vce3 ");
            sql.append("where vce3.DATAELEM_ID = :initialNameElementId and vce3.ELEMENT_VALUE is not null and vce3.ELEMENT_VALUE != '') ");
        }
        String countryUrl = getFullCountryUrl(countryCode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryUrl);
        params.put("statuses", SiteCodeStatus.ALLOCATED.name());
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
        params.put("countryCodeElementId", elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()));

        if (withoutInitialName){
            params.put("initialNameElementId", elementMap.get(SiteCodeBoundElementIdentifiers.INITIAL_SITE_NAME.getIdentifier()));
        }

        timer.stop();
        LOGGER.info("Method getCountryUnusedAllocations lasted: " + timer.toString());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCountryUsedAllocations(String countryCode) {

        StopWatch timer = new StopWatch();
        timer.start();

        /* Create a list for the identifiers needed*/
        List<String> elementIdentifiers = new ArrayList<>();
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier());
        elementIdentifiers.add(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier());

        /* Create a hashmap with key being the identifier and value being the id of the element*/
        Map<String, Integer> elementMap = dataElementDAO.getMultipleCommonDataElementIds(elementIdentifiers);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct vc.VOCABULARY_CONCEPT_ID) ");
        sql.append("from VOCABULARY v inner join VOCABULARY_CONCEPT vc on v.VOCABULARY_ID=vc.VOCABULARY_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce1 on vc.VOCABULARY_CONCEPT_ID=vce1.VOCABULARY_CONCEPT_ID ");
        sql.append("inner join VOCABULARY_CONCEPT_ELEMENT vce2 on vce1.VOCABULARY_CONCEPT_ID=vce2.VOCABULARY_CONCEPT_ID ");
        sql.append("where v.VOCABULARY_TYPE = :siteCodeType and v.WORKING_COPY = 0 and vce1.ID != vce2.ID " );
        sql.append("and vce1.DATAELEM_ID = :statusElementId and vce1.ELEMENT_VALUE in (:statuses) ");
        sql.append("and vce2.DATAELEM_ID = :countryCodeElementId and vce2.ELEMENT_VALUE = :countryCode");

        String countryUrl = getFullCountryUrl(countryCode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("countryCode", countryUrl);
        params.put("statuses", Arrays.asList(SiteCodeFilter.ALLOCATED_USED_STATUSES));
        params.put("siteCodeType", VocabularyType.SITE_CODE.name());
        params.put("statusElementId", elementMap.get(SiteCodeBoundElementIdentifiers.STATUS.getIdentifier()));
        params.put("countryCodeElementId",elementMap.get(SiteCodeBoundElementIdentifiers.COUNTRY_CODE.getIdentifier()));

        timer.stop();
        LOGGER.info("Method getCountryUsedAllocations lasted: " + timer.toString());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean siteCodeFolderExists() {

        StopWatch timer = new StopWatch();
        timer.start();

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_ID) from VOCABULARY where VOCABULARY_TYPE = :type");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", VocabularyType.SITE_CODE.name());

        timer.stop();
        LOGGER.info("Method siteCodeFolderExists lasted: " + timer.toString());

        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params,Integer.class) > 0;
    }

    private String getFullCountryUrl(String countryCode){
        StringBuilder countryCodeSb = new StringBuilder();
        countryCodeSb.append(Props.getRequiredProperty(PropsIF.DD_URL));
        countryCodeSb.append("/vocabulary/common/countries/");
        countryCodeSb.append(countryCode);
        return countryCodeSb.toString();
    }

}
