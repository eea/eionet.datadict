/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.datadict.dal.impl;

import eionet.datadict.dal.VocabularyDAO;
import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.dao.domain.VocabularyFolder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Aliki Kopaneli
 */
@Repository
public class VocabularyDAOImpl extends JdbcRepositoryBase implements VocabularyDAO {

    public static final String GET_BY_ID = "SELECT VOCABULARY.IDENTIFIER as IDENTIFIER, VOCABULARY.LABEL as LABEL, VOCABULARY_SET.IDENTIFIER as F_IDENTIFIER "
            + "FROM VOCABULARY LEFT JOIN VOCABULARY_SET ON VOCABULARY.FOLDER_ID = VOCABULARY_SET.ID WHERE VOCABULARY_ID = :id";

    @Autowired
    public VocabularyDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public VocabularyFolder getPlainVocabularyById(int id) throws ResourceNotFoundException {
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

}
