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
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.service.data.ObsoleteStatus;
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
        sql.append("select VOCABULARY_CONCEPT_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, CREATION_DATE, OBSOLETE_DATE ");
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
                        vc.setCreated(rs.getTimestamp("CREATION_DATE"));
                        vc.setObsolete(rs.getTimestamp("OBSOLETE_DATE"));
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
        sql.append("select SQL_CALC_FOUND_ROWS VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, ")
                .append("CREATION_DATE, OBSOLETE_DATE ");
        sql.append("from VOCABULARY_CONCEPT where 1 = 1 ");
        if (filter.getVocabularyFolderId() > 0) {
            params.put("vocabularyFolderId", filter.getVocabularyFolderId());
            sql.append("and VOCABULARY_ID=:vocabularyFolderId ");
        }
        if (StringUtils.isNotEmpty(filter.getText())) {
            params.put("text", "%" + filter.getText() + "%");
            sql.append("and (NOTATION like :text ");
            sql.append("or LABEL like :text ");
            sql.append("or DEFINITION like :text ");
            sql.append("or IDENTIFIER like :text) ");
        }
        if (StringUtils.isNotEmpty(filter.getIdentifier())) {
            params.put("identifier", filter.getIdentifier());
            sql.append("and IDENTIFIER = :identifier ");
        }
        if (StringUtils.isNotEmpty(filter.getDefinition())) {
            params.put("definition", filter.getDefinition());
            sql.append("and DEFINITION = :definition ");
        }
        if (StringUtils.isNotEmpty(filter.getLabel())) {
            params.put("label", filter.getLabel());
            sql.append("and LABEL = :label ");
        }
        if (filter.getExcludedIds() != null && !filter.getExcludedIds().isEmpty()) {
            params.put("excludedIds", filter.getExcludedIds());
            sql.append("and VOCABULARY_CONCEPT_ID not in (:excludedIds) ");
        }
        if (filter.getIncludedIds() != null && !filter.getIncludedIds().isEmpty()) {
            params.put("includedIds", filter.getIncludedIds());
            sql.append("and VOCABULARY_CONCEPT_ID in (:includedIds) ");
        }
        if (filter.getObsoleteStatus() != null) {
            if (ObsoleteStatus.VALID_ONLY.equals(filter.getObsoleteStatus())) {
                sql.append("and OBSOLETE_DATE IS NULL ");
            }
            if (ObsoleteStatus.OBSOLETE_ONLY.equals(filter.getObsoleteStatus())) {
                sql.append("and OBSOLETE_DATE IS NOT NULL ");
            }
        }

        if (filter.isNumericIdentifierSorting()) {
            sql.append("order by IDENTIFIER + 0 ");
        } else {
            sql.append("order by IDENTIFIER ");
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
                        vc.setCreated(rs.getDate("CREATION_DATE"));
                        vc.setObsolete(rs.getDate("OBSOLETE_DATE"));
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
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, ORIGINAL_CONCEPT_ID, ")
                .append("OBSOLETE_DATE, CREATION_DATE) ");

        sql.append("select :newVocabularyFolderId, IDENTIFIER, LABEL, DEFINITION, NOTATION, VOCABULARY_CONCEPT_ID, ")
                .append("OBSOLETE_DATE, CREATION_DATE ");
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
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION) ");
        sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("vocabularyFolderId", vocabularyFolderId);
        parameters.put("identifier", vocabularyConcept.getIdentifier());
        parameters.put("label", vocabularyConcept.getLabel());
        parameters.put("definition", vocabularyConcept.getDefinition());
        if (vocabularyConcept.getNotation() != null) {
            vocabularyConcept.setNotation(vocabularyConcept.getNotation().trim());
        }
        parameters.put("notation", vocabularyConcept.getNotation());

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
        return getLastInsertId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) {

        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT set IDENTIFIER = :identifier, LABEL = :label, ")
                .append("DEFINITION = :definition, NOTATION = :notation ");

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
    public void markConceptsObsolete(List<Integer> ids) {
        String sql = "update VOCABULARY_CONCEPT set OBSOLETE_DATE = now() where VOCABULARY_CONCEPT_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unMarkConceptsObsolete(List<Integer> ids) {
        String sql = "update VOCABULARY_CONCEPT set OBSOLETE_DATE = NULL where VOCABULARY_CONCEPT_ID in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);

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
        String sql = "SELECT MAX(0 + IDENTIFIER) FROM VOCABULARY_CONCEPT GROUP BY VOCABULARY_ID "
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
        sql.append("insert into VOCABULARY_CONCEPT (VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION) ");
        sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation)");

        @SuppressWarnings("unchecked")
        Map<String, Object>[] batchValues = new HashMap[amount];

        for (int i = 0; i < batchValues.length; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("vocabularyFolderId", vocabularyFolderId);
            params.put("identifier", Integer.toString(identifier));
            params.put("label", label);
            params.put("definition", definition);
            params.put("notation", Integer.toString(identifier));
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
                return new Integer(rs.getString("IDENTIFIER"));
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
        sql.append("select VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, ")
                .append("CREATION_DATE, OBSOLETE_DATE ");
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
                        vc.setCreated(rs.getDate("CREATION_DATE"));
                        vc.setObsolete(rs.getDate("OBSOLETE_DATE"));
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
        sql.append("select VOCABULARY_CONCEPT_ID, VOCABULARY_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION, CREATION_DATE, ")
                .append("OBSOLETE_DATE ");
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
                        vc.setCreated(rs.getDate("CREATION_DATE"));
                        vc.setObsolete(rs.getDate("OBSOLETE_DATE"));
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

        sql.append("where cev.VOCABULARY_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID ")
                .append("AND c.ORIGINAL_CONCEPT_ID IS NOT NULL ")
                .append("AND cev.DATAELEM_ID = :elementId ")
                .append("AND c.VOCABULARY_ID = :vocabularyId");

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
    public void moveReferenceConcepts(int oldVocabularyId, int newVocabularyId) {
        StringBuilder sql = new StringBuilder();
        sql.append("update VOCABULARY_CONCEPT_ELEMENT vce, VOCABULARY_CONCEPT vco, VOCABULARY_CONCEPT vcn  "
                + "SET vce.RELATED_CONCEPT_ID = vcn.VOCABULARY_CONCEPT_ID WHERE vcn.VOCABULARY_ID = :newVocabularyId "
                + "AND vco.VOCABULARY_ID = :oldVocabularyId AND vco.IDENTIFIER=vcn.IDENTIFIER  "
                + "AND vce.RELATED_CONCEPT_ID=vco.VOCABULARY_CONCEPT_ID");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("newVocabularyId", newVocabularyId);
        parameters.put("oldVocabularyId", oldVocabularyId);

        getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
    }

    @Override
    public List<VocabularyConcept> getValidConceptsWithValuedElements(int vocabularyId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularyId", vocabularyId);

        StringBuilder sql = new StringBuilder();

        sql.append("select distinct c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.ELEMENT_VALUE, v.LANGUAGE, v.RELATED_CONCEPT_ID, "
                + "d.IDENTIFIER AS ELEMIDENTIFIER, a.VALUE as DATATYPE, c.VOCABULARY_ID, c.IDENTIFIER, c.LABEL, "
                + "c.DEFINITION, c.NOTATION, c.CREATION_DATE, "
                + "c.OBSOLETE_DATE, rcvs.IDENTIFIER as RVOCSETIDENTIFIER, rcv.IDENTIFIER as RVOCIDENTIFIER, "
                + "rc.IDENTIFIER AS RCONCEPTIDENTIFIER, rc.LABEL as RCONCEPTLABEL "
                + "from  VOCABULARY_CONCEPT c "
                + "left join VOCABULARY_CONCEPT_ELEMENT v on v.VOCABULARY_CONCEPT_ID = c.VOCABULARY_CONCEPT_ID "
                + "LEFT JOIN DATAELEM d ON (v.DATAELEM_ID = d.DATAELEM_ID) "
                + "LEFT JOIN VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID "
                + "LEFT JOIN VOCABULARY rcv ON rc.VOCABULARY_ID = rcv.VOCABULARY_ID "
                + "LEFT JOIN VOCABULARY_SET rcvs ON (rcv.FOLDER_ID = rcvs.ID ) "
                + "left join (ATTRIBUTE a, M_ATTRIBUTE ma)  on (a.DATAELEM_ID = d.DATAELEM_ID "
                + "and PARENT_TYPE = 'E' and a.M_ATTRIBUTE_ID = ma.M_ATTRIBUTE_ID and ma.NAME='Datatype') "
                + "where c.VOCABULARY_ID = :vocabularyId AND c.OBSOLETE_DATE IS NULL "
                + "ORDER by c.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, d.IDENTIFIER, v.LANGUAGE, rcv.IDENTIFIER ");

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

                //concept changed:
                if (conceptId != previousConceptId) {
                    vc = new VocabularyConcept();
                    vc.setId(conceptId);
                    vc.setLabel(rs.getString("LABEL"));
                    vc.setIdentifier(rs.getString("IDENTIFIER"));
                    vc.setDefinition(rs.getString("DEFINITION"));
                    vc.setNotation(rs.getString("NOTATION"));
                    vc.setCreated(rs.getDate("CREATION_DATE"));
                    vc.setObsolete(rs.getDate("OBSOLETE_DATE"));

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
                    }
                    //add Datatype -  is used in RDF output
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
