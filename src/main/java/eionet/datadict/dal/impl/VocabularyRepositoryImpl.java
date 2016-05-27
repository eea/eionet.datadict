package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularyRepository;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Repository
public class VocabularyRepositoryImpl extends JdbcRepositoryBase implements VocabularyRepository {

    @Autowired
    public VocabularyRepositoryImpl(DataSource dataSource) {
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
    
}
