package eionet.datadict.dal.impl;

import eionet.datadict.dal.CleanMysqlSystemDbLoggingTableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Repository
public class CleanMysqlSystemDbLoggingTableDaoImpl extends JdbcDaoBase implements CleanMysqlSystemDbLoggingTableDao {


    @Autowired
    public CleanMysqlSystemDbLoggingTableDaoImpl(@Qualifier("mysqlSystemDatabase") DataSource mySqlSystemDataSource) {
        super(mySqlSystemDataSource);
    }

    @Override
    public void delete() {
        String sql1 = "SET GLOBAL general_log = 'OFF';";
        Map<String, Object> params = new HashMap<>();
        getNamedParameterJdbcTemplate().update(sql1, params);
        String sql2 = "RENAME TABLE general_log TO general_log_temp";
        getNamedParameterJdbcTemplate().update(sql2, params);
        String sql3 = "delete from general_log_temp where event_time < now() - interval 30 DAY";
        getNamedParameterJdbcTemplate().update(sql3, params);
        String sql4 = "RENAME TABLE general_log_temp TO general_log;";
        getNamedParameterJdbcTemplate().update(sql4, params);
        String sql5 = "SET GLOBAL log_output = 'table'";
        getNamedParameterJdbcTemplate().update(sql5, params);
        String sql6 = "SET GLOBAL general_log = 'ON'";
        getNamedParameterJdbcTemplate().update(sql6, params);
    }
}
