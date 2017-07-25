/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.meta.dao.mysql;

import eionet.meta.dao.IVocabularySetDAO;
import eionet.meta.dao.domain.VocabularySet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Lena KARGIOTI eka@eworx.gr
 */
@Repository
public class VocabularySetDAOImpl extends GeneralDAOImpl implements IVocabularySetDAO {
 
    protected static final Logger LOGGER = LoggerFactory.getLogger(VocabularySetDAOImpl.class);

    @Override
    public VocabularySet get(int vocabularySetID) {
        String sql = "select * FROM VOCABULARY_SET where ID = :ID";
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ID", vocabularySetID );
        
        List<VocabularySet> vocabularySet = getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<VocabularySet>() {

            @Override
            public VocabularySet mapRow(ResultSet rs, int rowNum) throws SQLException {
                VocabularySet vocabularySet = new VocabularySet();
                vocabularySet.setId( Integer.parseInt(rs.getString("ID")) );
                vocabularySet.setLabel(rs.getString("LABEL"));
                vocabularySet.setIdentifier(rs.getString("IDENTIFIER"));
                return vocabularySet;
            }
        });
        
        return vocabularySet.get(0);
    }
    
    
}
