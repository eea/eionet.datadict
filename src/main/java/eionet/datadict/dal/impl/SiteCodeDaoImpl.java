package eionet.datadict.dal.impl;

import eionet.datadict.dal.SiteCodeDao;
import eionet.datadict.model.SiteCode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author nta@eworx.gr
 */
public class SiteCodeDaoImpl extends JdbcDaoBase implements SiteCodeDao {
   
    @Autowired
    public SiteCodeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateVocabularyConceptId() {

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE T_SITE_CODE, VOCABULARY_CONCEPT set T_SITE_CODE.VOCABULARY_CONCEPT_ID = VOCABULARY_CONCEPT.VOCABULARY_CONCEPT_ID "
                + "  where VOCABULARY_CONCEPT.ORIGINAL_CONCEPT_ID = T_SITE_CODE.VOCABULARY_CONCEPT_ID ");

        getJdbcTemplate().update(sql.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SiteCode> getAllSiteCodes() {

        String sql = "SELECT * FROM T_SITE_CODE";

        List<SiteCode> result = getNamedParameterJdbcTemplate().query(sql, new RowMapper<SiteCode>() {

            @Override
            public SiteCode mapRow(ResultSet rs, int rowNum) throws SQLException {
                SiteCode sc = new SiteCode();
                sc.setVocabularyConceptId(rs.getString("VOCABULARY_CONCEPT_ID"));
                
                //TODO set all variables
                return sc;
            }

        });

        return result;
    } 
}
