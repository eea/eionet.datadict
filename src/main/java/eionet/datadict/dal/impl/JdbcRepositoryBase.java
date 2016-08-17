package eionet.datadict.dal.impl;

import java.util.HashMap;
import java.util.Map;
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
    
    protected Map<String, Object> createParameterMap() {
        return new HashMap<String, Object>();
    }
    
}
