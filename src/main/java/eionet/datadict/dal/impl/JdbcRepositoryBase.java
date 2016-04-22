package eionet.datadict.dal.impl;

import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public abstract class JdbcRepositoryBase extends NamedParameterJdbcDaoSupport {
    
    public JdbcRepositoryBase(DataSource dataSource) {
        super.setDataSource(dataSource);
    }
    
}
