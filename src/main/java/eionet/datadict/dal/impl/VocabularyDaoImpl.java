package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularyDao;
import eionet.datadict.model.Vocabulary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public boolean exists(Integer vocabularySetId, String vocabularyIdentifier) {
        String sql = "select count(*) from VOCABULARY where FOLDER_ID = :vocabularySetId and IDENTIFIER = :vocabularyIdentifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularySetId", vocabularySetId);
        params.put("vocabularyIdentifier", vocabularyIdentifier);
        int count = this.getNamedParameterJdbcTemplate().queryForInt(sql, params);
        
        return count > 0;
    }
    
    @Override
    public boolean exists(String vocabularySetIdentifier, String vocabularyIdentifier) {
        String sql = "select count(*) from VOCABULARY_SET vs inner join VOCABULARY v on vs.ID = v.FOLDER_ID where vs.IDENTIFIER = :vocabularySetIdentifier and v.IDENTIFIER = :vocabularyIdentifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vocabularySetIdentifier", vocabularySetIdentifier);
        params.put("vocabularyIdentifier", vocabularyIdentifier);
        int count = this.getNamedParameterJdbcTemplate().queryForInt(sql, params);
        
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
