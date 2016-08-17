
package eionet.datadict.services.impl.data;

import eionet.datadict.dal.NamespaceDao;
import eionet.datadict.model.Namespace;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class NamespaceDataServiceTest {

    @Mock
    NamespaceDao namespaceDao;
    
    @Spy
    @InjectMocks
    NamespaceDataServiceImpl namespaceDataService;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetAttributeNamespaces() {
        Mockito.doReturn(new ArrayList<Namespace>()).when(namespaceDao).getAttributeNamespaces();
        assertNotNull(namespaceDataService.getAttributeNamespaces());
    }
}
