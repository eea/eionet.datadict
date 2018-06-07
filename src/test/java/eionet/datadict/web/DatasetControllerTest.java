package eionet.datadict.web;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.datadict.errors.XmlExportException;
import eionet.datadict.services.DataSetService;
import eionet.datadict.services.DataSetTableService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class DatasetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DataSetService dataSetService;
    @Mock
    private DataSetTableService dataSetTableService;

    DataSetController dataSetController;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.dataSetController = new DataSetController(dataSetService,dataSetTableService);
        mockMvc = MockMvcBuilders.standaloneSetup(dataSetController).build();
    }

    @Test
    public void successToGetDataSetXMLSchema() throws XmlExportException, ResourceNotFoundException, ParserConfigurationException, ServletException, IOException, TransformerException {
        ArgumentCaptor<Integer> datasetIdCaptor = ArgumentCaptor.forClass(Integer.class);
        dataSetController.getDataSetSchema(2827, new MockHttpServletResponse());
        verify(dataSetService, times(1)).getDataSetXMLSchema(datasetIdCaptor.capture());
        assertThat(datasetIdCaptor.getValue(), equalTo(2827));
    }

    @Test
    public void successToGetDataSetXMLInstance() throws ResourceNotFoundException, ServletException, IOException, TransformerException, TransformerConfigurationException, XmlExportException {
        ArgumentCaptor<Integer> datasetIdCaptor = ArgumentCaptor.forClass(Integer.class);
        dataSetController.getDataSetInstance(2827, new MockHttpServletResponse());
        verify(dataSetService, times(1)).getDataSetXMLInstance(datasetIdCaptor.capture());
        assertThat(datasetIdCaptor.getValue(), equalTo(2827));
    }
    
    @Test
    public void failToUpdateDisplayDownLoadLinksDueToUnauthorized(){
    
    }
}
