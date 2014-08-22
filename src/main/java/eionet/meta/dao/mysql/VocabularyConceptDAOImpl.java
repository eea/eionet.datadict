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
 *        Juhan Voolaid
 */

package eionet.meta.dao.mysql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptResult;

/**
 * Vocabulary concept DAO.
 *
 * @author Juhan Voolaid
 */
@Repository
public class VocabularyConceptDAOImpl extends GeneralDAOImpl implements IVocabularyConceptDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select VOCABULARY_CONCEPT_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ACCEPTED_DATE, ");
        sql.append("NOT_ACCEPTED_DATE, STATUS_MODIFIED ");
        sql.append("from VOCABULARY_CONCEPT where VOCABULARY_ID=:vocabularyFolderId order by IDENTIFIER + 0");

        List<VocabularyConcept> resultList =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                    @Override
                    public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConcept vc = new VocabularyConcept();
                        vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                        vc.setIdentifier(rs.getString("IDENTIFIER"));
                        vc.setLabel(rs.getString("LABEL"));
                        vc.setDefinition(rs.getString("DEFINITION"));
                        vc.setNotation(rs.getString("NOTATION"));
                        vc.setStatus(rs.getInt("STATUS"), true);
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        return vc;
                    }
                });

        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) {
        Map<String, Object> params = new HashMap<String, Object>();

        StringBuilder sql = new StringBuilder();
        sql.append("select SQL_CALC_FOUND_ROWS c.VOCABULARY_CONCEPT_ID, c.VOCABULARY_ID, c.IDENTIFIER, c.LABEL, c.DEFINITION, ");
        sql.append("c.NOTATION, c.STATUS, c.ACCEPTED_DATE, c.NOT_ACCEPTED_DATE, c.STATUS_MODIFIED, v.LABEL AS VOCABULARY_LABEL, ");
        sql.append("v.IDENTIFIER AS VOCABULARY_IDENTIFIER, s.ID AS VOCSET_ID, s.LABEL as VOCSET_LABEL, ");
        sql.append("s.IDENTIFIER as VOCSET_IDENTIFIER ");
        sql.append("from VOCABULARY_CONCEPT c, VOCABULARY v, VOCABULARY_SET s ");
        sql.append("where v.VOCABULARY_ID = c.VOCABULARY_ID AND v.FOLDER_ID = s.ID ");
        if (filter.getVocabularyFolderId() > 0) {
            params.put("vocabularyFolderId", filter.getVocabularyFolderId());
            sql.append("and c.VOCABULARY_ID=:vocabularyFolderId ");
        }
        if (StringUtils.isNotEmpty(filter.getText())) {
            if (filter.isWordMatch()) {
                params.put("text", "[[:<:]]" + filter.getText() + "[[:>:]]");
                sql.append("and (c.NOTATION REGEXP :text ");
                sql.append("or c.LABEL REGEXP :text ");
                sql.append("or c.DEFINITION REGEXP :text ");
                sql.append("or c.IDENTIFIER REGEXP :text) ");
                // word match overrides exactmatch as it contains also exact matches
            } else if (filter.isExactMatch()) {
                params.put("text", filter.getText());
                sql.append("and (c.NOTATION = :text ");
                sql.append("or c.LABEL = :text ");
                sql.append("or c.DEFINITION = :text ");
                sql.append("or c.IDENTIFIER = :text) ");

            } else {
                params.put("text", "%" + filter.getText() + "%");
                sql.append("and (c.NOTATION like :text ");
                sql.append("or c.LABEL like :text ");
                sql.append("or c.DEFINITION like :text ");
                sql.append("or c.IDENTIFIER like :text) ");
            }
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            params.put("identifier", filter.getIdentifier());
            sql.append("and c.IDENTIFIER = :identifier ");
        }
        if (StringUtils.isNotEmpty(filter.getDefinition())) {
            params.put("definition", filter.getDefinition());
            sql.append("and c.DEFINITION = :definition ");
        }
        if (StringUtils.isNotEmpty(filter.getLabel())) {
            params.put("label", filter.getLabel());
            sql.append("and c.LABEL = :label ");
        }
        if (filter.getExcludedIds() != null && !filter.getExcludedIds().isEmpty()) {
            params.put("excludedIds", filter.getExcludedIds());
            sql.append("and c.VOCABULARY_CONCEPT_ID not in (:excludedIds) ");
        }
        if (filter.getIncludedIds() != null && !filter.getIncludedIds().isEmpty()) {
            params.put("includedIds", filter.getIncludedIds());
            sql.append("and c.VOCABULARY_CONCEPT_ID in (:includedIds) ");
        }

        if (filter.getConceptStatus() != null) {
            params.put("conceptStatus", filter.getConceptStatus().getValue());
            sql.append("and c.STATUS & :conceptStatus = :conceptStatus ");
        }

        if (StringUtils.isNotEmpty(filter.getVocabularyText())) {
            if (filter.isExactMatch()) {
                params.put("vocabularyText", filter.getVocabularyText());
                sql.append("and (v.IDENTIFIER = :vocabularyText ");
                sql.append("or v.LABEL = :vocabularyText) ");

            } else {
                params.put("vocabularyText", "%" + filter.getVocabularyText() + "%");
                sql.append("and (v.IDENTIFIER like :vocabularyText ");
                sql.append("or v.LABEL like :vocabularyText) ");
            }
        }

        if (filter.getExcludedVocabularySetIds() != null && filter.getExcludedVocabularySetIds().size() > 0) {
            params.put("excludedVocSetIds", filter.getExcludedVocabularySetIds());
            sql.append("AND s.ID NOT IN (:excludedVocSetIds) ");
        }
        if (filter.isNumericIdentifierSorting()) {
            sql.append("order by c.IDENTIFIER + 0 ");
        } else {
            sql.append("order by c.IDENTIFIER ");
        }

        if (filter.isUsePaging()) {
            sql.append("LIMIT ").append(filter.getOffset()).append(",").append(filter.getPageSize());
        }

        List<VocabularyConcept> resultList =
                getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                    @Override
                    public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConcept vc = new VocabularyConcept();
                        vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                        vc.setVocabularyId(rs.getInt("VOCABULARY_ID"));
                        vc.setIdentifier(rs.getString("IDENTIFIER"));
                        vc.setLabel(rs.getString("LABEL"));
                        vc.setDefinition(rs.getString("DEFINITION"));
                        vc.setNotation(rs.getString("NOTATION"));
                        vc.setStatus(rs.getInt("STATUS"), true);
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        vc.setVocabularyIdentifier(rs.getString("VOCABULARY_IDENTIFIER"));
                        vc.setVocabularyLabel(rs.getString("VOCABULARY_LABEL"));
                        vc.setVocabularySetLabel(rs.getString("VOCSET_LABEL"));
                        vc.setVocabularySetId(rs.getInt("VOCSET_ID"));
                        vc.setVocabularySetIdentifier("VOCSET_IDENTIFIER");
                        return vc;
                    }
                });

        String totalSql = "SELECT FOUND_ROWS()";
        int totalItems = getJdbcTemplate().queryForInt(totalSql);

        VocabularyConceptResult result = new VocabularyConceptResult(resultList, totalItems, filter);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyVocabularyConcepts(int oldVocabularyFolderId, int newVocabularyFolderId) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT ");
        sql.append("(VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, ORIGINAL_CONCEPT_ID, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED) ");
        sql.append("select :newVocabularyFolderId, IDENTIFIER, LABEL, DEFINITION, NOTATION, VOCABULARY_CONCEPT_ID, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED ");
        sql.append("from VOCABULARY_CONCEPT where VOCABULARY_ID = :oldVocabularyFolderId");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyFolderId", newVocabularyFolderId);
        parameters.put("oldVocabularyFolderId", oldVocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED) ");
        sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation, :status, ");
        sql.append(":acceptedDate, :notAcceptedDate, :statusModified)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolderId);
        parameters.put("identifier", vocabularyConcept.getIdentifier());
        parameters.put("label", vocabularyConcept.getLabel());
        parameters.put("definition", vocabularyConcept.getDefinition());
        if (vocabularyConcept.getNotation() != null) {
            vocabularyConcept.setNotation(vocabularyConcept.getNotation().trim());
        }
        parameters.put("notation", vocabularyConcept.getNotation());
        parameters.put("status", vocabularyConcept.getStatusValue());
        parameters.put("acceptedDate", vocabularyConcept.getAcceptedDate());
        parameters.put("notAcceptedDate", vocabularyConcept.getNotAcceptedDate());
        parameters.put("statusModified", vocabularyConcept.getStatusModified());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
        return getLastInsertId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT set IDENTIFIER = :identifier, LABEL = :label, ");
        sql.append("DEFINITION = :definition, NOTATION = :notation, STATUS = :status, ACCEPTED_DATE = :acceptedDate, ");
        sql.append("NOT_ACCEPTED_DATE= :notAcceptedDate, STATUS_MODIFIED = :statusModified ");
        sql.append("where VOCABULARY_CONCEPT_ID = :vocabularyConceptId");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyConceptId", vocabularyConcept.getId());
        parameters.put("identifier", vocabularyConcept.getIdentifier());
        parameters.put("label", vocabularyConcept.getLabel());
        parameters.put("definition", vocabularyConcept.getDefinition());
        if (vocabularyConcept.getNotation() != null) {
            vocabularyConcept.setNotation(vocabularyConcept.getNotation().trim());
        }
        parameters.put("notation", vocabularyConcept.getNotation());
        parameters.put("status", vocabularyConcept.getStatusValue());
        parameters.put("acceptedDate", vocabularyConcept.getAcceptedDate());
        parameters.put("notAcceptedDate", vocabularyConcept.getNotAcceptedDate());
        parameters.put("statusModified", vocabularyConcept.getStatusModified());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyConcepts(List<Integer> ids) {
        String sql = "delete from VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsInvalid(List<Integer> ids) {
        String sql =
                "update VOCABULARY_CONCEPT set STATUS = :invalid, NOT_ACCEPTED_DATE = now(), STATUS_MODIFIED = now() "
                        + "where VOCABULARY_CONCEPT_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        parameters.put("invalid", StandardGenericStatus.INVALID.getValue());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsValid(List<Integer> ids) {
        String sql =
                "update VOCABULARY_CONCEPT set STATUS = :valid, ACCEPTED_DATE = now(), STATUS_MODIFIED = now() "
                        + "where VOCABULARY_CONCEPT_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        parameters.put("valid", StandardGenericStatus.VALID.getValue());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteVocabularyConcepts(int vocabularyFolderId) {
        String sql = "delete from VOCABULARY_CONCEPT where VOCABULARY_ID = :vocabularyFolderId";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveVocabularyConcepts(int fromVocabularyFolderId, int toVocabularyFolderId) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT set VOCABULARY_ID = :toVocabularyFolderId, ORIGINAL_CONCEPT_ID = null ");
        sql.append("where VOCABULARY_ID = :fromVocabularyFolderId");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("fromVocabularyFolderId", fromVocabularyFolderId);
        parameters.put("toVocabularyFolderId", toVocabularyFolderId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("identifier", identifier);
        parameters.put("vocabularyFolderId", vocabularyFolderId);

        StringBuilder sql = new StringBuilder();
        sql.append("select count(VOCABULARY_CONCEPT_ID) from VOCABULARY_CONCEPT ");
        sql.append("where IDENTIFIER = :identifier and VOCABULARY_ID = :vocabularyFolderId ");
        if (vocabularyConceptId != 0) {
            sql.append("and VOCABULARY_CONCEPT_ID != :vocabularyConceptId");
            parameters.put("vocabularyConceptId", vocabularyConceptId);
        }

        int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);

        return result == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextIdentifierValue(int vocabularyFolderId) {
        String sql =
                "SELECT MAX(0 + IDENTIFIER) FROM VOCABULARY_CONCEPT GROUP BY VOCABULARY_ID "
                        + "HAVING VOCABULARY_ID = :vocabularyFolderId";

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolderId);

        try {
            int result = getNamedParameterJdbcTemplate().queryForInt(sql, parameters);
            return result + 1;
        } catch (EmptyResultDataAccessException e) {
            return 1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEmptyConcepts(int vocabularyFolderId, int amount, int identifier, String label, String definition) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("NOT_ACCEPTED_DATE, STATUS_MODIFIED) ");
        sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation, :status, ");
        sql.append(":notAcceptedDate, :statusModified)");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[amount];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyFolderId", vocabularyFolderId);
            params.put("identifier", Integer.toString(identifier));
            params.put("label", label);
            params.put("definition", definition);
            params.put("notation", Integer.toString(identifier));
            params.put("status", StandardGenericStatus.INVALID.getValue());
            Date now = new Date(System.currentTimeMillis());
            params.put("notAcceptedDate", now);
            params.put("statusModified", now);
            identifier++;
            batchValues[i] = params;
        }

        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier) {
        StringBuilder sql = new StringBuilder();
        sql.append("select IDENTIFIER from VOCABULARY_CONCEPT where VOCABULARY_ID = :vocabularyFolderId ");
        sql.append("and IDENTIFIER between :start and :end");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolderId);
        parameters.put("start", startingIdentifier);
        parameters.put("end", startingIdentifier + amount);

        List<Integer> resultList = getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Integer.valueOf(rs.getString("IDENTIFIER"));
            }
        });

        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);
        params.put("identifier", conceptIdentifier);

        StringBuilder sql = new StringBuilder();
        sql.append("select VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED ");
        sql.append("from VOCABULARY_CONCEPT where VOCABULARY_ID=:vocabularyFolderId and IDENTIFIER=:identifier");

        VocabularyConcept result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                    @Override
                    public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConcept vc = new VocabularyConcept();
                        vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                        vc.setVocabularyId(rs.getInt("VOCABULARY_ID"));
                        vc.setIdentifier(rs.getString("IDENTIFIER"));
                        vc.setLabel(rs.getString("LABEL"));
                        vc.setDefinition(rs.getString("DEFINITION"));
                        vc.setNotation(rs.getString("NOTATION"));
                        vc.setStatus(rs.getInt("STATUS"), true);
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        return vc;
                    }
                });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VocabularyConcept getVocabularyConcept(int vocabularyConceptId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyConceptId", vocabularyConceptId);

        StringBuilder sql = new StringBuilder();
        sql.append("select VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED ");
        sql.append("from VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID=:vocabularyConceptId");

        VocabularyConcept result =
                getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                    @Override
                    public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConcept vc = new VocabularyConcept();
                        vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                        vc.setVocabularyId(rs.getInt("VOCABULARY_ID"));
                        vc.setIdentifier(rs.getString("IDENTIFIER"));
                        vc.setLabel(rs.getString("LABEL"));
                        vc.setDefinition(rs.getString("DEFINITION"));
                        vc.setNotation(rs.getString("NOTATION"));
                        vc.setStatus(rs.getInt("STATUS"), true);
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        return vc;
                    }
                });

        return result;
    }

    @Override
    public List<VocabularyConcept> getConceptsWithValuedElement(int elementId, int vocabularyId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("elementId", elementId);
        parameters.put("vocabularyId", vocabularyId);

        StringBuilder sql = new StringBuilder();
        sql.append("select DISTINCT cev.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT cev, VOCABULARY_CONCEPT c ");
        sql.append("where cev.VOCABULARY_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID ");
        // .append("AND c.ORIGINAL_CONCEPT_ID IS NOT NULL ")
        sql.append("AND cev.DATAELEM_ID = :elementId ");
        sql.append("AND c.VOCABULARY_ID = :vocabularyId");

        final List<VocabularyConcept> result = new ArrayList<VocabularyConcept>();
        getNamedParameterJdbcTemplate().query(sql.toString(), parameters, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int conceptId = rs.getInt("VOCABULARY_CONCEPT_ID");
                VocabularyConcept concept = getVocabularyConcept(conceptId);
                result.add(concept);
            }
        });

        return result;
    }

    @Override
    public void updateReferringReferenceConcepts(int oldVocabularyId) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT_ELEMENT vce, VOCABULARY_CONCEPT vco, VOCABULARY_CONCEPT vcn  ");
        sql.append("SET vce.RELATED_CONCEPT_ID = vcn.VOCABULARY_CONCEPT_ID WHERE ");
        sql.append("vcn.ORIGINAL_CONCEPT_ID = vco.VOCABULARY_CONCEPT_ID ");
        sql.append("AND vco.VOCABULARY_ID = :oldVocabularyId ");
        sql.append("AND vce.RELATED_CONCEPT_ID=vco.VOCABULARY_CONCEPT_ID");
        // TODO_20044 - check
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("oldVocabularyId", oldVocabularyId);
        LOGGER.debug(StringUtils.replace(sql.toString(), ":oldVocabularyId", String.valueOf(oldVocabularyId)));
        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    @Override
    public List<VocabularyConcept> getValidConceptsWithValuedElements(int vocabularyId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.ELEMENT_VALUE, v.LANGUAGE, v.RELATED_CONCEPT_ID, ");
        sql.append("d.IDENTIFIER AS ELEMIDENTIFIER, a.VALUE as DATATYPE, c.VOCABULARY_ID, c.IDENTIFIER, c.LABEL, ");
        sql.append("c.DEFINITION, c.NOTATION, c.STATUS, c.ACCEPTED_DATE, c.NOT_ACCEPTED_DATE, c.STATUS_MODIFIED, ");
        sql.append("rcvs.IDENTIFIER as RVOCSETIDENTIFIER, rcv.IDENTIFIER as RVOCIDENTIFIER, rcv.BASE_URI as RVOCBASE_URI, ");
        sql.append("rc.IDENTIFIER AS RCONCEPTIDENTIFIER, rc.LABEL as RCONCEPTLABEL ");
        sql.append("from VOCABULARY_CONCEPT c ");
        sql.append("left join VOCABULARY_CONCEPT_ELEMENT v on v.VOCABULARY_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID ");
        sql.append("LEFT JOIN DATAELEM d ON (v.DATAELEM_ID = d.DATAELEM_ID) ");
        sql.append("LEFT JOIN VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID ");
        sql.append("LEFT JOIN VOCABULARY rcv ON rc.VOCABULARY_ID = rcv.VOCABULARY_ID ");
        sql.append("LEFT JOIN VOCABULARY_SET rcvs ON (rcv.FOLDER_ID = rcvs.ID ) ");
        sql.append("left join (ATTRIBUTE a, M_ATTRIBUTE ma)  on (a.DATAELEM_ID = d.DATAELEM_ID ");
        sql.append("and PARENT_TYPE = 'E' and a.M_ATTRIBUTE_ID = ma.M_ATTRIBUTE_ID and ma.NAME='Datatype') ");
        sql.append("where c.VOCABULARY_ID = :vocabularyId AND c.STATUS & :acceptedStatus = :acceptedStatus ");
        sql.append("ORDER by c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, d.IDENTIFIER, v.LANGUAGE, rcv.IDENTIFIER ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);
        params.put("acceptedStatus", StandardGenericStatus.ACCEPTED.getValue());

        final List<VocabularyConcept> resultList = new ArrayList<VocabularyConcept>();
        getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
            int previousConceptId = -1;
            int previousElemId = -1;
            VocabularyConcept vc;
            List<DataElement> oneElementValues;
            List<List<DataElement>> elementValues;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int conceptId = rs.getInt("VOCABULARY_CONCEPT_ID");

                // concept changed:
                if (conceptId != previousConceptId) {
                    vc = new VocabularyConcept();
                    vc.setId(conceptId);
                    vc.setLabel(rs.getString("LABEL"));
                    vc.setIdentifier(rs.getString("IDENTIFIER"));
                    vc.setDefinition(rs.getString("DEFINITION"));
                    vc.setNotation(rs.getString("NOTATION"));
                    vc.setStatus(rs.getInt("STATUS"), true);
                    vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                    vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                    vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                    elementValues = new ArrayList<List<DataElement>>();
                    vc.setElementAttributes(elementValues);
                    resultList.add(vc);
                }

                int elemId = rs.getInt("DATAELEM_ID");

                if (elemId != previousElemId || conceptId != previousConceptId) {
                    oneElementValues = new ArrayList<DataElement>();
                    elementValues.add(oneElementValues);
                }

                if (elemId > 0) {
                    DataElement elem = new DataElement();
                    elem.setId(elemId);
                    elem.setIdentifier(rs.getString("ELEMIDENTIFIER"));
                    elem.setAttributeLanguage(rs.getString("LANGUAGE"));
                    elem.setAttributeValue(rs.getString("ELEMENT_VALUE"));

                    Integer relatedConceptId = rs.getInt("RELATED_CONCEPT_ID");

                    if (relatedConceptId != 0) {
                        elem.setRelatedConceptId(relatedConceptId);
                        elem.setRelatedConceptVocSet(rs.getString("RVOCSETIDENTIFIER"));
                        elem.setRelatedConceptVocabulary(rs.getString("RVOCIDENTIFIER"));
                        elem.setRelatedConceptIdentifier(rs.getString("RCONCEPTIDENTIFIER"));
                        elem.setRelatedConceptLabel(rs.getString("RCONCEPTLABEL"));
                        elem.setRelatedConceptBaseURI(rs.getString("RVOCBASE_URI"));
                    }
                    // add Datatype - is used in RDF output
                    String dataType = rs.getString("DATATYPE");
                    if (dataType != null) {
                        Map<String, List<String>> elemAttributeValues = new HashMap<String, List<String>>();
                        List<String> elemDatatypeValues = new ArrayList<String>();
                        elemDatatypeValues.add(dataType);
                        elemAttributeValues.put("Datatype", elemDatatypeValues);
                        elem.setElemAttributeValues(elemAttributeValues);
                    }

                    oneElementValues.add(elem);
                }
                previousConceptId = conceptId;
                previousElemId = elemId;
            }
        });

        return resultList;
    }

}
