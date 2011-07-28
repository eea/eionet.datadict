package eionet.meta.savers;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

import eionet.util.sql.ConnectionUtil;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class CopyHandlerTest extends DatabaseTestCase {

    /*
     * (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return new DatabaseConnection(ConnectionUtil.getConnection());
    }

    /*
     * (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
