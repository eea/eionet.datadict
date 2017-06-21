package eionet.datadict.dal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.model.DataSet;
import eionet.meta.dao.domain.DatasetRegStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup(type = DatabaseOperation.CLEAN_INSERT,
            value ="classpath:seed-datasetIT.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL,
            value ="classpath:seed-datasetIT.xml")
public class DatasetDaoTestIT { 
    
    
    @Autowired
    DatasetDao datasetDao;
    
    @Test
    public void testGetById() {
        DataSet expectedDataset = new DataSet();
        expectedDataset.setId(4);
        expectedDataset.setIdentifier("NiD_test");
        expectedDataset.setShortName("NiD_water");
        expectedDataset.setVersion(2007);
        expectedDataset.setRegStatus(DatasetRegStatus.INCOMPLETE);
        DataSet dataset = datasetDao.getById(4);
        Assert.assertEquals(expectedDataset.getId(), dataset.getId());
        Assert.assertEquals(expectedDataset.getIdentifier(), dataset.getIdentifier());
        Assert.assertEquals(expectedDataset.getShortName(), dataset.getShortName());
        Assert.assertEquals(expectedDataset.getVersion(), dataset.getVersion());
        Assert.assertEquals(expectedDataset.getRegStatus(), dataset.getRegStatus());
    }
}
