package eionet.datadict.dal;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import eionet.config.ApplicationTestContext;
import eionet.datadict.model.DataSet;
import eionet.datadict.model.Namespace;
import eionet.meta.dao.domain.DatasetRegStatus;
import java.util.ArrayList;
import java.util.List;

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

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationTestContext.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class})

public class DatasetDaoTestIT {

    @Autowired
    DatasetDao datasetDao;


    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-datasetIT.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-datasetIT.xml");
    }


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

    @Test
    public void testGetByIdentifierAndWorkingCopyAndRegStatusesOrderByIdentifierAscAndIdDesc() {
        List<DatasetRegStatus> regStatuses = new ArrayList<DatasetRegStatus>();
        regStatuses.add(DatasetRegStatus.RELEASED);
        List<DataSet> datasets = this.datasetDao.getByIdentifierAndWorkingCopyAndRegStatusesOrderByIdentifierAscAndIdDesc("NoiseDirectiveDF1", false, regStatuses);

        DataSet expectedDatasetWithId2827 = new DataSet();
        expectedDatasetWithId2827.setId(2827);
        expectedDatasetWithId2827.setShortName("NoiseDirectiveDF1");
        expectedDatasetWithId2827.setVersion(5);
        expectedDatasetWithId2827.setWorkingCopy(false);
        expectedDatasetWithId2827.setRegStatus(DatasetRegStatus.RELEASED);
        expectedDatasetWithId2827.setDate(1272974730726L);
        Namespace namespace = new Namespace();
        namespace.setId(688);
        expectedDatasetWithId2827.setCorrespondingNS(namespace);
        expectedDatasetWithId2827.setIdentifier("NoiseDirectiveDF1");
        expectedDatasetWithId2827.setDispCreateLinks(43);

        DataSet expectedDatasetWithId2828 = new DataSet();
        expectedDatasetWithId2828.setId(2828);
        expectedDatasetWithId2828.setShortName("NoiseDirectiveDF1");
        expectedDatasetWithId2828.setVersion(5);
        expectedDatasetWithId2828.setWorkingCopy(false);
        expectedDatasetWithId2828.setRegStatus(DatasetRegStatus.RELEASED);
        expectedDatasetWithId2828.setDate(1272974730726L);
        Namespace namespace2 = new Namespace();
        namespace2.setId(688);
        expectedDatasetWithId2828.setCorrespondingNS(namespace2);
        expectedDatasetWithId2828.setIdentifier("NoiseDirectiveDF1");
        expectedDatasetWithId2828.setDispCreateLinks(43);

        //Given that the order of the Datasets to be defined is determined by the method explicitly
        //and we know before hand their order, we can  do the assertions below:
        Assert.assertEquals(datasets.get(0).getId(), expectedDatasetWithId2828.getId());
        Assert.assertEquals(datasets.get(0).getIdentifier(), expectedDatasetWithId2828.getIdentifier());
        Assert.assertEquals(datasets.get(0).getShortName(), expectedDatasetWithId2828.getShortName());
        Assert.assertEquals(datasets.get(0).getVersion(), expectedDatasetWithId2828.getVersion());
        Assert.assertEquals(datasets.get(0).getRegStatus(), expectedDatasetWithId2828.getRegStatus());
        Assert.assertEquals(datasets.get(0).getWorkingCopy(), expectedDatasetWithId2828.getWorkingCopy());
        Assert.assertEquals(datasets.get(0).getCorrespondingNS().getId(), expectedDatasetWithId2828.getCorrespondingNS().getId());
        Assert.assertEquals(datasets.get(0).getDispCreateLinks(), expectedDatasetWithId2828.getDispCreateLinks());
        Assert.assertEquals(datasets.get(1).getId(), expectedDatasetWithId2827.getId());
        Assert.assertEquals(datasets.get(1).getIdentifier(), expectedDatasetWithId2827.getIdentifier());
        Assert.assertEquals(datasets.get(1).getShortName(), expectedDatasetWithId2827.getShortName());
        Assert.assertEquals(datasets.get(1).getVersion(), expectedDatasetWithId2827.getVersion());
        Assert.assertEquals(datasets.get(1).getRegStatus(), expectedDatasetWithId2827.getRegStatus());
        Assert.assertEquals(datasets.get(1).getWorkingCopy(), expectedDatasetWithId2827.getWorkingCopy());
        Assert.assertEquals(datasets.get(1).getCorrespondingNS().getId(), expectedDatasetWithId2827.getCorrespondingNS().getId());
        Assert.assertEquals(datasets.get(1).getDispCreateLinks(), expectedDatasetWithId2827.getDispCreateLinks());
    }
}
