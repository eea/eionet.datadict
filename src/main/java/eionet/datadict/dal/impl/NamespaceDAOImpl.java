package eionet.datadict.dal.impl;

import eionet.datadict.dal.NamespaceDAO;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author exorx-alk
 */
@Repository("ddNamespaceDAOImpl")
public class NamespaceDAOImpl extends JdbcRepositoryBase implements NamespaceDAO {

    @Autowired
    public NamespaceDAOImpl(DataSource dataSource) {
        super(dataSource);
    }

//    @Override
//    public Namespace getNamespaceById(int id) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    
}
