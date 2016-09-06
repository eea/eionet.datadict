/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularyDAO;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


@Repository
public class VocabularyDAOImpl extends JdbcRepositoryBase implements VocabularyDAO {

    public static final String GET_BY_ID = "SELECT VOCABULARY.IDENTIFIER as IDENTIFIER, VOCABULARY.LABEL as LABEL, VOCABULARY_SET.IDENTIFIER as F_IDENTIFIER "
            + "FROM VOCABULARY LEFT JOIN VOCABULARY_SET ON VOCABULARY.FOLDER_ID = VOCABULARY_SET.ID WHERE VOCABULARY_ID = :id";

    @Autowired
    public VocabularyDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public VocabularyFolder getPlainVocabularyById(int id) {
        String sql = GET_BY_ID;
        Map paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        VocabularyFolder voc = getNamedParameterJdbcTemplate().queryForObject(sql, paramMap, new RowMapper<VocabularyFolder>() {
            @Override
            public VocabularyFolder mapRow(ResultSet rs, int i) throws SQLException {
                VocabularyFolder voc = new VocabularyFolder();
                voc.setIdentifier(rs.getString("IDENTIFIER"));
                voc.setLabel(rs.getString("LABEL"));
                voc.setFolderLabel(rs.getString("F_IDENTIFIER"));
                return voc;
            }
        });
        voc.setId(id);
        return voc;
    }

    
    @Override
    public boolean existsVocabularyConcept(int vocabularyId, String identifier) {
        String sql = "SELECT IDENTIFIER FROM VOCABULARY_CONCEPT WHERE VOCABULARY_ID = :vocabularyId AND IDENTIFIER = :identifier";
        
        Map paramMap = new HashMap<String, Object>();
        paramMap.put("vocabularyId", vocabularyId);
        paramMap.put("identifier", identifier);
        
        try {
            getNamedParameterJdbcTemplate().queryForObject(sql, paramMap, String.class);
            return true;
        } catch (IncorrectResultSizeDataAccessException ex){
            return false;
        }
    }
    
    @Override
    public List<VocabularyConcept> getVocabularyConcepts(int vocabularyId) {
        String sql = "SELECT IDENTIFIER, LABEL, STATUS FROM VOCABULARY_CONCEPT WHERE VOCABULARY_ID = :vocabularyId";
        Map paramMap = new HashMap<String, Object>();
        paramMap.put("vocabularyId", vocabularyId);
        return getNamedParameterJdbcTemplate().query(sql, paramMap, new VocabularyConceptRowMapper());
    }
    
    @Override
    public List<VocabularyConcept> getVocabularyConcepts(int vocabularyId, List<StandardGenericStatus> allowedStatuses) {
        String sql = "SELECT IDENTIFIER, LABEL, STATUS FROM VOCABULARY_CONCEPT WHERE VOCABULARY_ID = :vocabularyId  AND (";
        Map paramMap = new HashMap<String, Object>();
        paramMap.put("vocabularyId", vocabularyId);
        int statusCounter = 0;
        for (StandardGenericStatus status : allowedStatuses) {
            if (statusCounter != 0) {
                sql = sql + " OR ";
            }
            paramMap.put("status"+statusCounter, status.getValue());
            sql = sql + "STATUS = :status"+statusCounter;
            statusCounter++;
        }
        sql = sql + ")";
        return getNamedParameterJdbcTemplate().query(sql, paramMap, new VocabularyConceptRowMapper());
    }
    
    public static class VocabularyConceptRowMapper implements RowMapper<VocabularyConcept> {

        @Override
        public VocabularyConcept mapRow(ResultSet rs, int i) throws SQLException {
            VocabularyConcept concept = new VocabularyConcept();
            concept.setIdentifier(rs.getString("IDENTIFIER"));
            concept.setLabel(rs.getString("LABEL"));
            concept.setStatus(rs.getInt("STATUS"));
            return concept;
        }
        
    }

}
