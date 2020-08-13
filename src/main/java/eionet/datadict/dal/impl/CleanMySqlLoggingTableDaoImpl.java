package eionet.datadict.dal.impl;

import eionet.datadict.dal.CleanMySqlLoggingTableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Repository
public class CleanMySqlLoggingTableDaoImpl extends JdbcDaoBase implements CleanMySqlLoggingTableDao {


    @Autowired
    public CleanMySqlLoggingTableDaoImpl(@Qualifier("mySqlSystemDataSource") DataSource mySqlSystemDataSource) {
        super(mySqlSystemDataSource);
    }

    @Override
    public void delete() {
        String sql1 = "SET GLOBAL general_log = 'OFF';";
        Map<String, Object> params1 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql1, params1);
        String sql2 = "RENAME TABLE general_log TO general_log_temp";
        Map<String, Object> params2 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql2, params2);
        String sql3 = "delete from general_log_temp where event_time < now() - interval 30 DAY";
        Map<String, Object> params3 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql3, params3);
        String sql4 = "RENAME TABLE general_log_temp TO general_log;";
        Map<String, Object> params4 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql4, params4);
        String sql5 = "SET GLOBAL log_output = 'table'";
        Map<String, Object> params5 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql5, params5);
        String sql6 = "SET GLOBAL general_log = 'ON'";
        Map<String, Object> params6 = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql6, params6);
    }
}
