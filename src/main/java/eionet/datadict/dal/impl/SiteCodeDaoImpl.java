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
import org.springframework.stereotype.Repository;

/**
 *
 * @author nta@eworx.gr
 */
@Repository
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
                sc.setSiteCode(rs.getString("SITE_CODE"));
                sc.setInitialSiteName(rs.getString("INITIAL_SITE_NAME"));
                sc.setSiteCodeNat(rs.getString("SITE_CODE_NAT"));
                sc.setStatus(rs.getString("STATUS"));
                sc.setCcIso2(rs.getString("CC_ISO2"));
                sc.setParentIso(rs.getString("PARENT_ISO"));
                sc.setDateCreated(rs.getString("DATE_CREATED"));
                sc.setUserCreated(rs.getString("USER_CREATED"));
                sc.setDateAllocated(rs.getString("DATE_ALLOCATED"));
                sc.setUserAllocated(rs.getString("USER_ALLOCATED"));
                sc.setYearsDeleted(rs.getInt("YEARS_DELETED"));
                sc.setYearsDisappeared(rs.getInt("YEARS_DISAPPEARED"));
                sc.setDateDeleted(rs.getString("DATE_DELETED"));
                
                return sc;
            }

        });

        return result;
    } 
}
