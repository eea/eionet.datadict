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

import eionet.meta.dao.IVocabularyConceptDAO;
import eionet.meta.dao.domain.*;
import eionet.meta.dao.mysql.concepts.util.ConceptsWithAttributesQueryBuilder;
import eionet.meta.service.data.VocabularyConceptFilter;
import eionet.meta.service.data.VocabularyConceptFilter.BoundElementFilterResult;
import eionet.meta.service.data.VocabularyConceptResult;
import eionet.util.sql.SQL;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.*;
import java.util.*;

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
                        vc.setStatus(rs.getInt("STATUS"));
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

        if (!filter.getBoundElements().isEmpty()) {
            int index = 0;
            for (BoundElementFilterResult boundElement : filter.getBoundElements()) {
                if (StringUtils.isNotBlank(boundElement.getValue())) {
                    String keyParam = "key" + index;
                    params.put(keyParam, boundElement.getId());
                    String valueParam = "value" + index;
                    params.put(valueParam, boundElement.getValue());
                    sql.append("and c.VOCABULARY_CONCEPT_ID in (select e.VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT e where e.DATAELEM_ID = :" + keyParam + " ");
                    sql.append("and (e.RELATED_CONCEPT_ID = :" + valueParam + " ");
                    sql.append("or e.ELEMENT_VALUE = :" + valueParam + ")) ");
                    index++;
                }
            }
        }

        if (filter.isOrderByConceptId()) {
            sql.append("order by c.VOCABULARY_CONCEPT_ID");
        } else if (filter.isNumericIdentifierSorting()) {
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
                        vc.setStatus(rs.getInt("STATUS"));
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
        int totalItems = getJdbcTemplate().queryForObject(totalSql,Integer.class);

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
        LOGGER.info("Deleting concepts with ids: " + ids);
        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsInvalid(List<Integer> ids) {
        StringBuffer sql = new StringBuffer();
        sql.append("update VOCABULARY_CONCEPT set STATUS = :invalid, STATUS_MODIFIED = now(), ");
        sql.append("NOT_ACCEPTED_DATE = IF(NOT_ACCEPTED_DATE IS NULL OR ");
        sql.append("(STATUS & :acceptedState) = :acceptedState, now(), NOT_ACCEPTED_DATE) ");
        sql.append("where VOCABULARY_CONCEPT_ID in (:ids) AND STATUS != :invalid");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        parameters.put("invalid", StandardGenericStatus.INVALID.getValue());
        parameters.put("acceptedState", StandardGenericStatus.ACCEPTED.getValue());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markConceptsValid(List<Integer> ids) {
        StringBuffer sql = new StringBuffer();
        sql.append("update VOCABULARY_CONCEPT set STATUS = :valid, STATUS_MODIFIED = now(), ACCEPTED_DATE = ");
        sql.append("IF(ACCEPTED_DATE IS NULL OR (STATUS & :notAcceptedState) = :notAcceptedState, now(), ACCEPTED_DATE) ");
        sql.append("where VOCABULARY_CONCEPT_ID in (:ids) AND STATUS != :valid");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        parameters.put("valid", StandardGenericStatus.VALID.getValue());
        parameters.put("notAcceptedState", StandardGenericStatus.NOT_ACCEPTED.getValue());

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
        LOGGER.info("Deleting all concepts from vocabulary folder: " + vocabularyFolderId);
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

        int result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), parameters,Integer.class);

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
            int result = getNamedParameterJdbcTemplate().queryForObject(sql, parameters,Integer.class);
            return result + 1;
        } catch (EmptyResultDataAccessException e) {
            return 1;
        }
    }

    //could be that
    /**
     * {@inheritDoc}
     */
    @Override
    public void insertEmptyConcepts(int vocabularyFolderId, int amount, int identifier, String label, String definition) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, STATUS_MODIFIED) ");
        sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation, :status, ");
        sql.append(":acceptedDate, :statusModified)");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[amount];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyFolderId", vocabularyFolderId);
            params.put("identifier", Integer.toString(identifier));
            params.put("label", label);
            params.put("definition", definition);
            params.put("notation", Integer.toString(identifier));
            params.put("status", StandardGenericStatus.SUBMITTED.getValue());
            Date now = new Date(System.currentTimeMillis());
            params.put("acceptedDate", now);
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
                        vc.setStatus(rs.getInt("STATUS"));
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
                        vc.setStatus(rs.getInt("STATUS"));
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        return vc;
                    }
                });

        return result;
    }

    @Override
    public List<VocabularyConcept> getVocabularyConcepts(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);

        StringBuilder sql = new StringBuilder();
        sql.append("select VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED ");
        sql.append("from VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID in (:ids)");

        return getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                    @Override
                    public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VocabularyConcept vc = new VocabularyConcept();
                        vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                        vc.setVocabularyId(rs.getInt("VOCABULARY_ID"));
                        vc.setIdentifier(rs.getString("IDENTIFIER"));
                        vc.setLabel(rs.getString("LABEL"));
                        vc.setDefinition(rs.getString("DEFINITION"));
                        vc.setNotation(rs.getString("NOTATION"));
                        vc.setStatus(rs.getInt("STATUS"));
                        vc.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                        vc.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                        vc.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                        return vc;
                    }
                }
        );
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
      //TODO_20044 - check
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("oldVocabularyId", oldVocabularyId);

        LOGGER.debug(StringUtils.replace(sql.toString(), ":oldVocabularyId", String.valueOf(oldVocabularyId)));
        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    @Override
    public List<VocabularyConcept> getConceptsWithAttributeValues(int vocabularyId) {
        return this.getConceptsWithAttributeValues(vocabularyId, null);
    }

    @Override
    public List<VocabularyConcept> getConceptsWithAttributeValues(int vocabularyId, StandardGenericStatus conceptStatus) {
        return this.getConceptsWithAttributeValues(vocabularyId, conceptStatus, null, null);
    }
    
    @Override
    public List<VocabularyConcept> getConceptsWithAttributeValues(int vocabularyId, StandardGenericStatus conceptStatus, String conceptIdentifier, String conceptLabel) {
        Map<Integer, DataElement> conceptAttributes = this.getVocabularyConceptAttributes(vocabularyId);
        ConceptsWithAttributesQueryBuilder queryBuilder = new ConceptsWithAttributesQueryBuilder(vocabularyId, conceptIdentifier, conceptLabel, conceptStatus);
        List<VocabularyConcept> concepts = this.getVocabularyConcepts(queryBuilder);
        this.attachConceptAttributeValues(queryBuilder, conceptAttributes, concepts);
        
        return concepts;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getVocabularyConceptIds(int vocabularyFolderId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyFolderId", vocabularyFolderId);
        
        String sql = "select VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT where VOCABULARY_ID=:vocabularyFolderId";
        List<Integer> resultList =
                getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Integer(rs.getInt("VOCABULARY_CONCEPT_ID"));
                    }
                });
        return resultList;
    }

    @Override
    public List<VocabularyConcept> getConceptsWithValuedElement(int elementId) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("elementId", elementId);

        StringBuilder sql = new StringBuilder();
        sql.append("select DISTINCT VOCABULARY_CONCEPT_ID from VOCABULARY_CONCEPT_ELEMENT ");
        sql.append("where DATAELEM_ID = :elementId ");

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
    public Map<Integer, DataElement> getVocabularyConceptAttributes(int vocabularyId) {
        String sql = 
            "select v2e.DATAELEM_ID Id, d.IDENTIFIER Identifier, attrs.Name as AttributeName, attrs.VALUE as AttributeValue \n" +
            "from VOCABULARY2ELEM v2e \n" +
            "inner join DATAELEM d on v2e.DATAELEM_ID = d.DATAELEM_ID \n" +
            "left join ( \n" +
            "	select a.DATAELEM_ID, a.PARENT_TYPE, ma.NAME, a.VALUE \n" +
            "	from ATTRIBUTE a inner join M_ATTRIBUTE ma \n" +
            "       on a.M_ATTRIBUTE_ID = ma.M_ATTRIBUTE_ID and ma.NAME in ('Datatype', 'Name') \n" +
            "	where a.PARENT_TYPE = 'E' \n" +
            ") attrs on v2e.DATAELEM_ID = attrs.DATAELEM_ID \n" +
            "where v2e.VOCABULARY_ID = :vocabularyId \n" +
            "order by v2e.DATAELEM_ID";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);
        final List<DataElement> conceptAttributes = new ArrayList<DataElement>();
        this.getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {

            DataElement workingConceptAttribute;
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int currentAttributeId = rs.getInt("Id");
                
                if (workingConceptAttribute == null || workingConceptAttribute.getId() != currentAttributeId) {
                    workingConceptAttribute = new DataElement();
                    workingConceptAttribute.setId(currentAttributeId);
                    workingConceptAttribute.setIdentifier(rs.getString("Identifier"));
                    conceptAttributes.add(workingConceptAttribute);
                }
                
                String attributeName = rs.getString("AttributeName");
                
                if (attributeName != null) {
                    String attributeValue = rs.getString("AttributeValue");

                    if (workingConceptAttribute.getElemAttributeValues() == null) {
                        workingConceptAttribute.setElemAttributeValues(new HashMap<String, List<String>>());
                    }
                    
                    if (!workingConceptAttribute.getElemAttributeValues().containsKey(attributeName)) {
                        workingConceptAttribute.getElemAttributeValues().put(attributeName, new ArrayList<String>());
                    }

                    workingConceptAttribute.getElemAttributeValues().get(attributeName).add(attributeValue);
                }
            }

        });
        Map<Integer, DataElement> result = new HashMap<Integer, DataElement>();
        
        for (DataElement conceptAttribute : conceptAttributes) {
            result.put(conceptAttribute.getId(), conceptAttribute);
        }
        
        return result;
    }
    
    protected List<VocabularyConcept> getVocabularyConcepts(ConceptsWithAttributesQueryBuilder queryBuilder) {
        String sql = queryBuilder.buildConceptsSqlQuery();
        Map<String, Object> params = queryBuilder.buildConceptsSqlQueryParameters();
        
        return this.getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<VocabularyConcept>() {

            @Override
            public VocabularyConcept mapRow(ResultSet rs, int i) throws SQLException {
                VocabularyConcept concept = new VocabularyConcept();
                concept.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                concept.setIdentifier(rs.getString("IDENTIFIER"));
                concept.setLabel(rs.getString("LABEL"));
                concept.setDefinition(rs.getString("DEFINITION"));
                concept.setNotation(rs.getString("NOTATION"));
                concept.setStatus(rs.getInt("STATUS"));
                concept.setAcceptedDate(rs.getDate("ACCEPTED_DATE"));
                concept.setNotAcceptedDate(rs.getDate("NOT_ACCEPTED_DATE"));
                concept.setStatusModified(rs.getDate("STATUS_MODIFIED"));
                concept.setElementAttributes(new ArrayList<List<DataElement>>());
                
                return concept;
            }
        });
    }
    
    protected void attachConceptAttributeValues(final ConceptsWithAttributesQueryBuilder queryBuilder, 
            final Map<Integer, DataElement> conceptAttributes, List<VocabularyConcept> targetConcepts) {
        String sql = queryBuilder.buildConceptAttributesSqlQuery();
        Map<String, Object> params = queryBuilder.buildConceptAttributesSqlQueryParameters();
        final Iterator<VocabularyConcept> conceptIterator = targetConcepts.iterator();
        final Map<Integer, Integer> relatedConceptIdToVocabularyIdMappings = new HashMap<Integer, Integer>();
        final Set<Integer> relatedConceptVocabularyIds = new HashSet<Integer>();
        
        this.getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {

            VocabularyConcept workingConcept;
            List<DataElement> workingConceptAttributeValues;
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int conceptId = rs.getInt("ConceptId");
                
                while (workingConcept == null || workingConcept.getId() < conceptId) {
                    if (conceptIterator.hasNext()) {
                        workingConcept = conceptIterator.next();
                        workingConcept.setElementAttributes(new ArrayList<List<DataElement>>());
                        workingConceptAttributeValues = null;
                    }
                    else {
                        return;
                    }
                }

                if (workingConcept.getId() > conceptId) {
                    return;
                }

                DataElement conceptAttributeValue = new DataElement();
                conceptAttributeValue.setId(rs.getInt("AttributeId"));
                conceptAttributeValue.setAttributeValue(rs.getString("AttributeValue"));
                conceptAttributeValue.setAttributeLanguage(rs.getString("AttributeLanguage"));

                if (!conceptAttributes.containsKey(conceptAttributeValue.getId())) {
                    String msgFormat = "Could not find vocabulary concept attribute definition; vocabulary id: %d; concept id: %d; bound element id: %d";
                    String msg = String.format(msgFormat, queryBuilder.getVocabularyId(), conceptId, conceptAttributeValue.getId());
                    throw new IllegalStateException(msg);
                }
                
                DataElement conceptAttribute = conceptAttributes.get(conceptAttributeValue.getId());
                conceptAttributeValue.setIdentifier(conceptAttribute.getIdentifier());
                conceptAttributeValue.setElemAttributeValues(conceptAttribute.getElemAttributeValues());

                int relatedConceptVocabularyId = rs.getInt("RelatedConceptVocabularyId");

                if (!rs.wasNull()) {
                    relatedConceptVocabularyIds.add(relatedConceptVocabularyId);
                    conceptAttributeValue.setRelatedConceptId(rs.getInt("RelatedConceptId"));
                    conceptAttributeValue.setRelatedConceptIdentifier(rs.getString("RelatedConceptIdentifier"));
                    conceptAttributeValue.setRelatedConceptLabel(rs.getString("RelatedConceptLabel"));
                    conceptAttributeValue.setRelatedConceptNotation(rs.getString("RelatedConceptNotation"));
                    conceptAttributeValue.setRelatedConceptDefinition(rs.getString("RelatedConceptDefinition"));
                    
                    if (!relatedConceptIdToVocabularyIdMappings.containsKey(conceptAttributeValue.getRelatedConceptId())) {
                        relatedConceptIdToVocabularyIdMappings.put(conceptAttributeValue.getRelatedConceptId(), relatedConceptVocabularyId);
                    }
                }
                
                if (workingConceptAttributeValues == null || workingConceptAttributeValues.get(0).getId() != conceptAttributeValue.getId()) {
                    workingConceptAttributeValues = new ArrayList<DataElement>();
                    workingConcept.getElementAttributes().add(workingConceptAttributeValues);
                }
                
                workingConceptAttributeValues.add(conceptAttributeValue);
            }
        });
        
        this.attachRelatedConceptVocabularyInfo(relatedConceptVocabularyIds, relatedConceptIdToVocabularyIdMappings, targetConcepts);
    }
    
    protected void attachRelatedConceptVocabularyInfo(Set<Integer> relatedConceptVocabularyIds, 
            Map<Integer, Integer> relatedConceptIdToVocabularyIdMappings, List<VocabularyConcept> targetConcepts) {
        Integer[] vocabularyIds = relatedConceptVocabularyIds.toArray(new Integer[relatedConceptVocabularyIds.size()]);
        final int maxBatchSize = 80;
        int index = 0;
        
        while (index < vocabularyIds.length) {
            int batchSize = Math.min(maxBatchSize, vocabularyIds.length - index);
            Integer[] vocabularyIdBatch = Arrays.copyOfRange(vocabularyIds, index, index + batchSize);
            Map<Integer, VocabularyFolder> relatedVocabularyInfo = this.getRelatedConceptVocabularyInfo(vocabularyIdBatch);
            this.applyRelatedConceptVocabularyInfo(relatedVocabularyInfo, relatedConceptIdToVocabularyIdMappings, targetConcepts);
            index += batchSize;
        }
    }
    
    protected Map<Integer, VocabularyFolder> getRelatedConceptVocabularyInfo(Integer[] relatedConceptVocabularyIds) {
        String sql =
            "select \n" +
            "	vs.IDENTIFIER as VocabularySetIdentifier, \n" +
            "	vs.LABEL as VocabularySetLabel, \n" +
            "	v.VOCABULARY_ID as VocabularyId, \n" +
            "	v.IDENTIFIER as VocabularyIdentifier, \n" +
            "	v.LABEL as VocabularyLabel, \n" +
            "	v.BASE_URI as VocabularyBaseUri \n" +
            "from \n" +
            "	VOCABULARY v inner join VOCABULARY_SET vs \n" +
            "		on v.FOLDER_ID = vs.ID \n" +
            "where \n" +
            "	v.VOCABULARY_ID in ( :vocabularyIds )";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vocabularyIds", Arrays.asList(relatedConceptVocabularyIds));
        final Map<Integer, VocabularyFolder> vocabularies = new HashMap<Integer, VocabularyFolder>();
        
        this.getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                VocabularyFolder vocabulary = new VocabularyFolder();
                vocabulary.setId(rs.getInt("VocabularyId"));
                vocabulary.setIdentifier(rs.getString("VocabularyIdentifier"));
                vocabulary.setLabel(rs.getString("VocabularyLabel"));
                vocabulary.setBaseUri(rs.getString("VocabularyBaseUri"));
                
                VocabularySet vocabularySet = new VocabularySet();
                vocabularySet.setIdentifier(rs.getString("VocabularySetIdentifier"));
                vocabularySet.setLabel(rs.getString("VocabularySetLabel"));
                vocabulary.setVocabularySet(vocabularySet);
                
                vocabularies.put(vocabulary.getId(), vocabulary);
            }
        });
        
        return vocabularies;
    }
    
    protected void applyRelatedConceptVocabularyInfo(Map<Integer, VocabularyFolder> vocabularies, 
            Map<Integer, Integer> relatedConceptIdToVocabularyIdMappings, List<VocabularyConcept> targetConcepts) {
        
        
        for (VocabularyConcept concept : targetConcepts) {
            for (List<DataElement> conceptAttributeValues : concept.getElementAttributes()) {
                for (DataElement conceptAttributeValue : conceptAttributeValues) {
                    if (conceptAttributeValue.getRelatedConceptId() == null) {
                        continue;
                    }
                    
                    int vocabularyId = relatedConceptIdToVocabularyIdMappings.get(conceptAttributeValue.getRelatedConceptId());
                    
                    if (!vocabularies.containsKey(vocabularyId)) {
                        String msgFormat = "Failed to resolve vocabulary for related concept with id %d. Vocabulary with id %d was not found.";
                        String msg = String.format(msgFormat, conceptAttributeValue.getRelatedConceptId(), vocabularyId);
                        throw new IllegalStateException(msg);
                    }
                    
                    VocabularyFolder vocabulary = vocabularies.get(vocabularyId);
                    conceptAttributeValue.setRelatedConceptVocabulary(vocabulary.getIdentifier());
                    conceptAttributeValue.setRelatedConceptVocabularyLabel(vocabulary.getLabel());
                    conceptAttributeValue.setRelatedConceptBaseURI(vocabulary.getBaseUri());
                    conceptAttributeValue.setRelatedConceptVocSet(vocabulary.getVocabularySet().getIdentifier());
                    conceptAttributeValue.setRelatedConceptVocSetLabel(vocabulary.getVocabularySet().getLabel());
                }
            }
        }
    }

    /**
     * 
     * Use of plain JDBC prepared statements to get the generated keys. 
     * Spring JDBC batchUpdate does not offer a solution as the JDBC spec doesn't 
     * guarantee that the generated keys will be made available after a batch update. 
     * JDBC drivers are free to implement this feature as they see fit.
     * 
     * http://stackoverflow.com/questions/2423815/is-there-anyway-to-get-the-generated-keys-when-using-spring-jdbc-batchupdate
     */
    @Override
    public List<Integer> batchCreateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, STATUS, ");
        sql.append("ACCEPTED_DATE, NOT_ACCEPTED_DATE, STATUS_MODIFIED) ");
        sql.append("values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        PreparedStatement pst = null;

        List<Integer> generatedKeys = new ArrayList<Integer>();
        int index = 0;

        try {
            Connection connection = getConnection();
            pst = connection.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS);

            for (VocabularyConcept vocabularyConcept : vocabularyConcepts) {
                pst.setInt(1, vocabularyConcept.getVocabularyId());
                pst.setString(2, vocabularyConcept.getIdentifier());
                pst.setString(3, vocabularyConcept.getLabel());
                pst.setString(4, vocabularyConcept.getDefinition());
                pst.setString(5, vocabularyConcept.getNotation() != null ? vocabularyConcept.getNotation().trim() : null);
                pst.setInt(6, vocabularyConcept.getStatusValue());
                pst.setDate(7, vocabularyConcept.getAcceptedDate() != null ? new Date(vocabularyConcept.getAcceptedDate().getTime()) : null);
                pst.setDate(8, vocabularyConcept.getNotAcceptedDate() != null ? new Date(vocabularyConcept.getNotAcceptedDate().getTime()) : null);
                pst.setDate(9, vocabularyConcept.getStatusModified() != null ? new Date(vocabularyConcept.getStatusModified().getTime()) : null);
                pst.addBatch();

                index++;

                if (index % batchSize == 0 || index == vocabularyConcepts.size()) {
                    pst.executeBatch();

                    ResultSet rs = pst.getGeneratedKeys();
                    if (rs != null) {
                        while (rs.next()) {
                            generatedKeys.add(rs.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("SQL Exception", ex);
        } finally {
            SQL.close(pst);
        }
        return generatedKeys;
    }

    @Override
    public int[][] batchUpdateVocabularyConcepts(List<VocabularyConcept> vocabularyConcepts, int batchSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT set IDENTIFIER = ?, LABEL = ?, ");
        sql.append("DEFINITION = ?, NOTATION = ?, STATUS = ?, ACCEPTED_DATE = ?, ");
        sql.append("NOT_ACCEPTED_DATE= ?, STATUS_MODIFIED = ? ");
        sql.append("where VOCABULARY_CONCEPT_ID = ?");

        int[][] result = getJdbcTemplate().batchUpdate(
                sql.toString(),
                vocabularyConcepts,
                batchSize,
                new ParameterizedPreparedStatementSetter<VocabularyConcept>() {
                    @Override
                    public void setValues(PreparedStatement ps, VocabularyConcept vocabularyConcept) throws SQLException {
                        ps.setString(1, vocabularyConcept.getIdentifier());
                        ps.setString(2, vocabularyConcept.getLabel());
                        ps.setString(3, vocabularyConcept.getDefinition());
                        ps.setString(4, vocabularyConcept.getNotation() != null ? vocabularyConcept.getNotation().trim() : null);
                        ps.setInt(5, vocabularyConcept.getStatusValue());
                        ps.setDate(6, vocabularyConcept.getAcceptedDate()!=null ? new Date(vocabularyConcept.getAcceptedDate().getTime()) : null);
                        ps.setDate(7, vocabularyConcept.getNotAcceptedDate()!=null ? new Date(vocabularyConcept.getNotAcceptedDate().getTime()) : null);
                        ps.setDate(8, vocabularyConcept.getStatusModified()!=null ? new Date(vocabularyConcept.getStatusModified().getTime()) : null);
                        ps.setInt(9, vocabularyConcept.getId());
                    }
                });
        return result;
    }

    @Override
    public Map<Integer, Integer> getCheckedOutToOriginalMappings(Collection<Integer> conceptIds) {
        final Map<Integer, Integer> checkedOutToOriginalMappings = new HashMap<Integer, Integer>();

        String sql = "select VOCABULARY_CONCEPT_ID, ORIGINAL_CONCEPT_ID from VOCABULARY_CONCEPT where ORIGINAL_CONCEPT_ID in (:conceptIds)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("conceptIds", conceptIds);

        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                checkedOutToOriginalMappings.put(rs.getInt("VOCABULARY_CONCEPT_ID"), rs.getInt("ORIGINAL_CONCEPT_ID"));
            }
        });

        return checkedOutToOriginalMappings;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConceptLabelStatusModifiedDate(List<Integer> vocabularyConceptIds, String label, Integer status) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT set LABEL = :label, STATUS = :status, STATUS_MODIFIED = :statusModified ");
        sql.append("where VOCABULARY_CONCEPT_ID in (:vocabularyConceptIds)");

        java.util.Date today = new java.util.Date();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("label", label);
        parameters.put("status", status);
        parameters.put("statusModified", today);
        parameters.put("vocabularyConceptIds", vocabularyConceptIds);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifierOfRelatedConcept(Integer conceptId, Integer elementId){
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("elementId", elementId);
        parameters.put("conceptId", conceptId);

        StringBuilder sql = new StringBuilder();
        sql.append("select vc.IDENTIFIER from VOCABULARY_CONCEPT vc inner join VOCABULARY_CONCEPT_ELEMENT vce on vc.VOCABULARY_CONCEPT_ID = vce.RELATED_CONCEPT_ID ");
        sql.append("where vce.DATAELEM_ID = :elementId and vce.VOCABULARY_CONCEPT_ID = :conceptId and vc.ORIGINAL_CONCEPT_ID is null");

        String result = null;
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), parameters, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        return result;
    }
}
