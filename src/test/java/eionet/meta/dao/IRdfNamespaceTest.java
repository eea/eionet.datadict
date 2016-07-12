package eionet.meta.dao;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eionet.DDDatabaseTestCase;
import eionet.meta.dao.domain.RdfNamespace;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * RDFNamespaceDAO tests.
 *
 * @author Kaido LAine
 */
public class IRdfNamespaceTest extends DDDatabaseTestCase {

    /** local dao instance. */
    private static IRdfNamespaceDAO dao;

    /** spring context .*/
    private static ClassPathXmlApplicationContext springContext;

    @Override
    protected String getSeedFilename() {
        return "seed-rdfnamespace.xml";
    }

    @BeforeClass
    public static void setUpClass() {
        springContext = new ClassPathXmlApplicationContext("mock-spring-context.xml");
        dao = springContext.getBean(IRdfNamespaceDAO.class);
    }
    
    @AfterClass
    public static void tearDownClass() {
        springContext.close();
    }
    
    /**
     * test for namespaceExists method.
     * @throws Exception if test fails
     */
    @Test
    public void testNameSpaceExists() throws Exception {
        assertTrue(dao.namespaceExists("ns2"));
        assertTrue(dao.namespaceExists("ns3"));
        assertTrue(!dao.namespaceExists("ns4"));
    }

    /**
     * test get namespace.
     *
     */
    @Test
    public void testGetNamespace() throws Exception {
        RdfNamespace namespace = dao.getNamespace("ns2");
        assertEquals("http://namespace2.somewhere.com", namespace.getUri());

    }
}
