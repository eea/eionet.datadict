package eionet.datadict.dal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import eionet.config.ApplicationTestContext;
import eionet.datadict.dal.impl.JdbcDaoBase;
import eionet.datadict.model.GeneralLogEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
public class CleanMysqlSystemDbLoggingTableDaoTestIT {

    @Autowired
    private CleanMysqlSystemDbLoggingTableDao cleanMySqlLoggingTableDao;
    @Autowired
    @Qualifier("mysqlSystemDatabase")
    private DataSource mySqlSystemDataSource;

    private TestLogAssistanceDao testLogAssistanceDao;
    private GeneralLogEntry generalLogEntry;
    private static Date entryDate = Date.from(ZonedDateTime.now().minusMonths(2).toInstant());

    @Before
    public void setup() {
        this.testLogAssistanceDao = new TestLogAssistanceDao(mySqlSystemDataSource);
        generalLogEntry = createGeneralLogEntry();
        testLogAssistanceDao.insertLoggingEntries(generalLogEntry);
    }

    private GeneralLogEntry createGeneralLogEntry() {
        GeneralLogEntry generalLogEntry = new GeneralLogEntry();
        generalLogEntry.setEvent_time(entryDate);
        generalLogEntry.setUser_host("test");
        generalLogEntry.setThread_id(1);
        generalLogEntry.setServer_id(0);
        generalLogEntry.setCommand_type("Query");
        generalLogEntry.setArgument("show tables");
        return generalLogEntry;
    }

    @Test
    public void testDelete() {
        cleanMySqlLoggingTableDao.delete();
    }


    private static class TestLogAssistanceDao extends JdbcDaoBase {

        public TestLogAssistanceDao(@Qualifier("mysqlSystemDatabase") DataSource mySqlSystemDataSource) {
            super(mySqlSystemDataSource);
        }

        public void insertLoggingEntries(GeneralLogEntry entry){
            String sql1 = "SET GLOBAL general_log = 'OFF';";
            Map<String, Object> params = new HashMap<>();
            getNamedParameterJdbcTemplate().update(sql1, params);
            String sql2 = "RENAME TABLE general_log TO general_log_temp";
            getNamedParameterJdbcTemplate().update(sql2, params);
            String sql3 = "insert into general_log_temp(event_time, user_host, thread_id, server_id, command_type, argument) "
                    + "values (:event_time, :user_host, :thread_id, :server_id, :command_type, :argument)";
            Map<String, Object> params1 = new HashMap<>();
            params1.put("event_time", entry.getEvent_time());
            params1.put("user_host", entry.getUser_host());
            params1.put("thread_id", entry.getThread_id());
            params1.put("server_id", entry.getServer_id());
            params1.put("command_type", entry.getCommand_type());
            params1.put("argument", entry.getArgument());
            getNamedParameterJdbcTemplate().update(sql3, params1);
            String sql4 = "RENAME TABLE general_log_temp TO general_log";
            getNamedParameterJdbcTemplate().update(sql4, params);
            String sql5 = "SET GLOBAL log_output = 'table'";
            getNamedParameterJdbcTemplate().update(sql5, params);
            String sql6 = "SET GLOBAL general_log = 'ON'";
            getNamedParameterJdbcTemplate().update(sql6, params);

        }

    }
}
