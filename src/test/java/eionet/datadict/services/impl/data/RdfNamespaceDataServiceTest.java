
package eionet.datadict.services.impl.data;


import eionet.datadict.dal.RdfNamespaceDao;
import eionet.datadict.model.RdfNamespace;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.junit.Assert.assertNotNull;

public class RdfNamespaceDataServiceTest {

    @Mock
    RdfNamespaceDao rdfNamespaceDao;
    
    @Spy
    @InjectMocks
    RdfNamespaceDataServiceImpl rdfNamespaceDataService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetRdfNamespaces() {
        Mockito.doReturn(new ArrayList<RdfNamespace>()).when(rdfNamespaceDao).getRdfNamespaces();
        assertNotNull(rdfNamespaceDataService.getRdfNamespaces());
    }    
}
