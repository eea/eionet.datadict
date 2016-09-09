package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularySetDao;
import eionet.datadict.model.VocabularySet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Repository
public class VocabularySetDaoImpl extends JdbcDaoBase implements VocabularySetDao {

    @Autowired
    public VocabularySetDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public boolean exists(String identifier) {
        return this.resolve(identifier) != null;
    }

    @Override
    public Integer resolve(String identifier) {
        String sql = "select ID from VOCABULARY_SET where IDENTIFIER = :identifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", identifier);
        List<Integer> results = this.getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getInt(1);
            }
        });
        
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public VocabularySet get(String identifier) {
        String sql = "select * from VOCABULARY_SET where IDENTIFIER = :identifier";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", identifier);
        List<VocabularySet> results = this.getNamedParameterJdbcTemplate().query(sql, params, new VocabularySetRowMapper());
        
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void create(VocabularySet vocabularySet) {
        String sql = "insert into VOCABULARY_SET (IDENTIFIER, LABEL) values (:identifier, :label)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("identifier", vocabularySet.getIdentifier());
        params.put("label", vocabularySet.getLabel());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    private static class VocabularySetRowMapper implements RowMapper<VocabularySet> {

        @Override
        public VocabularySet mapRow(ResultSet rs, int i) throws SQLException {
            VocabularySet vs = new VocabularySet();
            vs.setId(rs.getInt("ID"));
            vs.setIdentifier(rs.getString("IDENTIFIER"));
            vs.setLabel(rs.getString("LABEL"));
            
            return vs;
        }
        
    }
    
}
