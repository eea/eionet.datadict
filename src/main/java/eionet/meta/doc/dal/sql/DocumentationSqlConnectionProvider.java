package eionet.meta.doc.dal.sql;

import eionet.doc.dal.sql.SqlConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Nikolaos Nakas
 */
@Component
public final class DocumentationSqlConnectionProvider implements SqlConnectionProvider {

    private final DataSource dataSource;

    @Autowired
    public DocumentationSqlConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Connection createConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
    
}
