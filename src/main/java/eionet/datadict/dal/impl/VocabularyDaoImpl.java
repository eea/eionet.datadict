package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularyDao;
import eionet.meta.dao.domain.StandardGenericStatus;
import eionet.meta.dao.domain.VocabularyConcept;
import eionet.meta.dao.domain.VocabularyFolder;
import java.sql.ResultSet;
import java.sql.SQLException;
import eionet.datadict.model.Vocabulary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Repository
public class VocabularyDaoImpl extends JdbcDaoBase implements VocabularyDao {

    @Autowired
    public VocabularyDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

     public static final String GET_BY_ID = "SELECT VOCABULARY.IDENTIFIER as IDENTIFIER, VOCABULARY.LABEL as LABEL, " + 
             "VOCABULARY.CONCEPT_IDENTIFIER_NUMERIC AS CONCEPT_IDENTIFIER_NUMERIC, VOCABULARY_SET.IDENTIFIER as F_IDENTIFIER " + 
             "FROM VOCABULARY LEFT JOIN VOCABULARY_SET ON VOCABULARY.FOLDER_ID = VOCABULARY_SET.ID WHERE VOCABULARY_ID = :id";

   
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
                voc.setNumericConceptIdentifiers(rs.getBoolean("CONCEPT_IDENTIFIER_NUMERIC"));
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
    @Override
    public boolean exists(Integer vocabularySetId, String vocabularyIdentifier) {
        String sql = "select count(*) from VOCABULARY where FOLDER_ID = :vocabularySetId and IDENTIFIER = :vocabularyIdentifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularySetId", vocabularySetId);
        params.put("vocabularyIdentifier", vocabularyIdentifier);
        int count = this.getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);
        
        return count > 0;
    }
    
    @Override
    public boolean exists(String vocabularySetIdentifier, String vocabularyIdentifier) {
        String sql = "select count(*) from VOCABULARY_SET vs inner join VOCABULARY v on vs.ID = v.FOLDER_ID where vs.IDENTIFIER = :vocabularySetIdentifier and v.IDENTIFIER = :vocabularyIdentifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularySetIdentifier", vocabularySetIdentifier);
        params.put("vocabularyIdentifier", vocabularyIdentifier);
        int count = this.getNamedParameterJdbcTemplate().queryForObject(sql, params,Integer.class);
        
        return count > 0;
    }

    @Override
    public List<Vocabulary> getValueListCodesOfDataElementsInTable(int tableId) {
        /*
        String sql = 
                "select\n" +
                "	elm.DATAELEM_ID, v.VOCABULARY_ID, vc.VOCABULARY_CONCEPT_ID, vc.IDENTIFIER, vc.NOTATION\n" +
                "from\n" +
                "	TBL2ELEM tbl2elm\n" +
                "inner join\n" +
                "	DATAELEM elm\n" +
                "on\n" +
                "	tbl2elm.DATAELEM_ID = elm.DATAELEM_ID and elm.TYPE = 'CH3'\n" +
                "inner join\n" +
                "	VOCABULARY v\n" +
                "on\n" +
                "	elm.VOCABULARY_ID = v.VOCABULARY_ID\n" +
                "inner join\n" +
                "	VOCABULARY_CONCEPT vc\n" +
                "on\n" +
                "	v.VOCABULARY_ID = vc.VOCABULARY_ID\n" +
                "where\n" +
                "	tbl2elm.TABLE_ID = :tableId\n" +
                "order by\n" +
                "	elm.DATAELEM_ID, v.VOCABULARY_ID";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tableId", tableId);
        final Map<Integer, Vocabulary> results = new HashMap<Integer, Vocabulary>();
        this.getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {

            private Integer currentDataElementId;
            private Vocabulary currentVocabulary;
            
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int dataElementId = rs.getInt("DATAELEM_ID");
                
                if (currentDataElementId == null || currentDataElementId != dataElementId) {
                    currentDataElementId = dataElementId;
                    currentVocabulary = new Vocabulary();
                    currentVocabulary.setId(rs.getInt("VOCABULARY_ID"));
                    results.put(currentDataElementId, currentVocabulary);
                }
                
                Concept concept = new Concept();
                concept.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                concept.setIdentifier(rs.getString("IDENTIFIER"));
                concept.setNotation(rs.getString("NOTATION"));
                concept.setVocabulary(currentVocabulary);
                currentVocabulary.getConcepts().add(concept);
            }
        });
        
        return results;
                */
        throw new UnsupportedOperationException();
    }
    
}
