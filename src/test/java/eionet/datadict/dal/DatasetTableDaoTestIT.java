package eionet.datadict.dal;

import eionet.config.ApplicationTestContext;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.model.Namespace;
import eionet.meta.service.DBUnitHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class
    })

public class DatasetTableDaoTestIT {

    @Autowired
    DatasetTableDao datasetTableDao;

    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-datasetTableIT.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-datasetTableIT.xml");
    }


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

