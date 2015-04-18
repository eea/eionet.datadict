package eionet;

import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * Support functions to set up MySQL data source
 */
public class DataSourceSupport {

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
            e.printStackTrace();
        }
        return ds;
    }

}
