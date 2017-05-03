package eionet.datadict.dal.impl;

import eionet.datadict.dal.SimpleAttributeDao;
import eionet.datadict.model.SimpleAttribute;
import eionet.datadict.model.SimpleAttributeValues;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Repository
public class SimpleAttributeDaoImpl extends JdbcDaoBase implements SimpleAttributeDao {

        @Autowired
    public SimpleAttributeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    
    
    
    @Override
    public List<SimpleAttribute> getSimpleAttributesOfDataTable(int tableId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SimpleAttributeValues> getSimpleAttributesValuesOfDataTable(int tableId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Set<SimpleAttribute>> getSimpleAttributesOfDataElementsInTable(int tableId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SimpleAttributeValues> getSimpleAttributesValuesOfDataElementsInTable(int tableId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
