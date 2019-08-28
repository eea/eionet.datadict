package eionet.datadict.dal;

import eionet.datadict.model.Namespace;
import eionet.meta.service.DBUnitHelper;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

@SpringApplicationContext("mock-spring-context.xml")
public class NamespaceDaoTestIT extends UnitilsJUnit4 {
    
    @SpringBeanByType
    NamespaceDao namespaceDao;
    
    @Before
    public void setUp() throws Exception {
        DBUnitHelper.loadData("seed-namespace.xml");
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        DBUnitHelper.deleteData("seed-namespace.xml");
    }
    
    @Test
    public void testGetAttributeNamespaces() {
        List<Namespace> list = this.namespaceDao.getAttributeNamespaces();
        assertEquals(2, list.size());
    }
    
}
