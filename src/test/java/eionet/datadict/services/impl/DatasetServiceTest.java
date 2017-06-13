package eionet.datadict.services.impl;

import eionet.datadict.dal.AttributeDao;
import eionet.datadict.dal.AttributeValueDao;
import eionet.datadict.dal.DataElementDao;
import eionet.datadict.dal.DatasetDao;
import eionet.datadict.dal.DatasetTableDao;
import eionet.datadict.model.DatasetTable;
import eionet.datadict.services.DataSetService;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
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
    @Ignore
    public void testGetDataSetXMLSchema(){
    
        Integer datasetId = 2127;
        DatasetTable dsTable = new DatasetTable();
        List<DatasetTable> dsTables = new ArrayList<DatasetTable>();
        when(datasetTableDao.getAllByDatasetId(Matchers.anyInt())).thenReturn(dsTables);
        
    }

}
