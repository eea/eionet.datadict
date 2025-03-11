package eionet.datadict.web;

import eionet.datadict.errors.EmptyParameterException;
import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetTableService;
import eionet.datadict.services.data.DatasetTableDataService;
import eionet.meta.outservices.OutService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DatasetTableControllerTest {

    @Mock
    private DataSetTableService dataSetTableService;

    @Mock
    private DatasetTableDataService datasetTableDataService;

    @Mock
    private OutService outService;

     DatasetTableController datasetTableController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.datasetTableController = new DatasetTableController(dataSetTableService, datasetTableDataService, outService);
        MockMvcBuilders.standaloneSetup(datasetTableController).build();
    }

    @Test
    public void successToGetDatasetTableXMLSchema() throws ResourceNotFoundException, ServletException, IOException, TransformerException, XmlExportException {
        ArgumentCaptor<Integer> datasetTableIdCaptor = ArgumentCaptor.forClass(Integer.class);
        datasetTableController.getDatasetTableSchema(6661, new MockHttpServletResponse());
        verify(dataSetTableService, times(1)).getDataSetTableXMLSchema(datasetTableIdCaptor.capture());
        assertThat(datasetTableIdCaptor.getValue(), equalTo(6661));
    }

    @Test
    public void successToGetDatasetTableXMLInstance() throws ResourceNotFoundException, ServletException, IOException, TransformerException, XmlExportException, EmptyParameterException {
        ArgumentCaptor<Integer> datasetTableIdCaptor = ArgumentCaptor.forClass(Integer.class);
        datasetTableController.getDataSetTableInstance(6661, new MockHttpServletResponse());
        verify(dataSetTableService, times(1)).getDataSetTableXMLInstance(datasetTableIdCaptor.capture());
        assertThat(datasetTableIdCaptor.getValue(), equalTo(6661));
    }
}
