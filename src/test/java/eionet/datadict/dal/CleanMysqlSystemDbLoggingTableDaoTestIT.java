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
        testLogAssistanceDao.enableTableLogging();
//        testLogAssistanceDao.createGeneralLogTable();
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

        public void createGeneralLogTable() {
            String sql = "CREATE TABLE `general_log`" +
                    "   `event_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    "                          ON UPDATE CURRENT_TIMESTAMP," +
                    "   `user_host` mediumtext NOT NULL," +
                    "   `thread_id` int(10) unsigned NOT NULL," +
                    "   `server_id` int(10) unsigned NOT NULL," +
                    "   `command_type` varchar(64) NOT NULL," +
                    "   `argument` mediumtext NOT NULL" +
                    "  ) ENGINE=CSV DEFAULT CHARSET=utf8 COMMENT='General log'";
            Map<String, Object> params = new HashMap<>();
            getNamedParameterJdbcTemplate().update(sql, params);
        }

        public void enableTableLogging() {
            String sql1 = "SET GLOBAL log_output = 'table'";
            Map<String, Object> params = new HashMap<>();
            getNamedParameterJdbcTemplate().update(sql1, params);
            String sql2 = "SET GLOBAL general_log = 'ON'";
            getNamedParameterJdbcTemplate().update(sql2, params);
        }

        public void insertLoggingEntries(GeneralLogEntry entry){
            String sql = "insert into general_log(event_time, user_host, thread_id, server_id, command_type, argument) "
                       + "values (:event_time, :user_host, :thread_id, :server_id, :command_type, :argument)";
            Map<String, Object> params = new HashMap<>();
            params.put("event_time", entry.getEvent_time());
            params.put("user_host", entry.getUser_host());
            params.put("thread_id", entry.getThread_id());
            params.put("server_id", entry.getServer_id());
            params.put("command_type", entry.getCommand_type());
            params.put("argument", entry.getArgument());
            getNamedParameterJdbcTemplate().update(sql, params);
        }

    }
}
