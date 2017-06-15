package eionet.datadict.services.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.model.DataSet;
import eionet.datadict.services.DataSetService;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DatasetServiceTest {

    @Mock
    private DatasetDao datasetDao;
    @Mock
    private DatasetTableDao datasetTableDao;

    @Mock
    private AttributeValueDao attributeValueDao;

    @Mock
    private AttributeDao attributeDao;

    @Mock
    private DataElementDao dataElementDao;

    private DataSetService dataSetService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.dataSetService = new DataSetServiceImpl(datasetDao, datasetTableDao, attributeValueDao, attributeDao, dataElementDao);
    }

    @Test
    public void testGetDatasetSuccessfully() throws ResourceNotFoundException {
        DataSet dataset = new DataSet();
        dataset.setId(2827);
        Mockito.doReturn(dataset).when(datasetDao).getById(2827);
        DataSet actualDataSet = dataSetService.getDataset(2827);
        assertEquals(dataset.getId(), actualDataSet.getId());
        Mockito.verify(datasetDao, times(1)).getById(2827);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetDatasetFailure() throws ResourceNotFoundException {
        dataSetService.getDataset(2827);
    }
}
