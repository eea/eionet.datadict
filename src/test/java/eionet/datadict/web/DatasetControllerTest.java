package eionet.datadict.web;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
import eionet.datadict.services.DataSetTableService;
import eionet.meta.outservices.OutService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;
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
public class DatasetControllerTest {

    @Mock
    private DataSetService dataSetService;
    @Mock
    private DataSetTableService dataSetTableService;
    @Mock
    private OutService outService;

    DataSetController dataSetController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.dataSetController = new DataSetController(dataSetService,dataSetTableService,outService);
        MockMvcBuilders.standaloneSetup(dataSetController).build();
    }

    @Test
    public void successToGetDataSetXMLSchema() throws XmlExportException, ResourceNotFoundException, ServletException, IOException, TransformerException {
        ArgumentCaptor<Integer> datasetIdCaptor = ArgumentCaptor.forClass(Integer.class);
        dataSetController.getDataSetSchema(2827, new MockHttpServletResponse());
        verify(dataSetService, times(1)).getDataSetXMLSchema(datasetIdCaptor.capture());
        assertThat(datasetIdCaptor.getValue(), equalTo(2827));
    }

    @Test
    public void successToGetDataSetXMLInstance() throws ResourceNotFoundException, ServletException, IOException, TransformerException, XmlExportException {
        ArgumentCaptor<Integer> datasetIdCaptor = ArgumentCaptor.forClass(Integer.class);
        dataSetController.getDataSetInstance(2827, new MockHttpServletResponse());
        verify(dataSetService, times(1)).getDataSetXMLInstance(datasetIdCaptor.capture());
        assertThat(datasetIdCaptor.getValue(), equalTo(2827));
    }

}
