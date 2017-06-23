package eionet.datadict.dal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import java.util.List;
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
            value ="classpath:seed-datasetTableIT.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL,
            value ="classpath:seed-datasetTableIT.xml")
public class DatasetTableDaoTestIT {

    @Autowired
    DatasetTableDao datasetTableDao;

    @Test
     public void testGetById(){
         DatasetTable expectedDsTable = new DatasetTable();
         expectedDsTable.setId(3);
         expectedDsTable.setIdentifier("NiD_GW_Stat");
         expectedDsTable.setShortName("GW_MonitoringStations");
         expectedDsTable.setName("List of groundwater monitoring stations");
         expectedDsTable.setCorrespondingNS(new Namespace(4));
         DatasetTable actualTable = datasetTableDao.getById(3);
         Assert.assertEquals(expectedDsTable.getId(),actualTable.getId());
         Assert.assertEquals(expectedDsTable.getIdentifier(),actualTable.getIdentifier());
         Assert.assertEquals(expectedDsTable.getShortName(),actualTable.getShortName());
         Assert.assertEquals(expectedDsTable.getName(),actualTable.getName());
         Assert.assertEquals(expectedDsTable.getCorrespondingNS().getId(),actualTable.getCorrespondingNS().getId());
     }
     
     @Test
     public void testGetParentDatasetId(){
        Integer expectedDatasetId = 4;
        Integer actualDatasetId = datasetTableDao.getParentDatasetId(3);
        Assert.assertEquals(expectedDatasetId,actualDatasetId);
     }
     
     @Test
     public void testGetAllByDatasetId(){
         List<DatasetTable> actualTables = datasetTableDao.getAllByDatasetId(4);
         Assert.assertEquals(2, actualTables.size());
     }
}

