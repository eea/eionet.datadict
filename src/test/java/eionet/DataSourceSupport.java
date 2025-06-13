package eionet;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Support functions to set up MySQL data source
 */
public class DataSourceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceSupport.class);

    public static DataSource getDataSource() {
        Properties props = new Properties();
        InputStream fis = null;
        BasicDataSource ds = new BasicDataSource();
        try {
            fis = DataSourceSupport.class.getClassLoader().getResourceAsStream("liquibase.properties");
            props.load(fis);
            ds.setDriverClassName(props.getProperty("driver"));
            ds.setUrl(props.getProperty("url"));
            ds.setUsername(props.getProperty("username"));
            ds.setPassword(props.getProperty("password"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ds;
    }

}
