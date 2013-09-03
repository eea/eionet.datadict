package eionet.meta.dao;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eionet.DDDatabaseTestCase;
import eionet.meta.dao.domain.RdfNamespace;

/**
 * RDFNamespaceDAO tests.
 *
 * @author Kaido LAine
 */
public class IRdfNamespaceTest extends DDDatabaseTestCase {

    /** local dao instance. */
    private IRdfNamespaceDAO dao;

    /** spring context .*/
    private ApplicationContext springContext;

    @Override
    protected String getSeedFilename() {
        return "seed-rdfnamespace.xml";
    }

    /**
     * test for namespaceExists method.
     * @throws Exception if test fails
     */
    @Test
    public void testNameSpaceExists() throws Exception {

        springContext = new ClassPathXmlApplicationContext("mock-spring-context.xml");
        dao = springContext.getBean(IRdfNamespaceDAO.class);
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
        //TODO init static
        springContext = new ClassPathXmlApplicationContext("mock-spring-context.xml");
        dao = springContext.getBean(IRdfNamespaceDAO.class);
        RdfNamespace namespace = dao.getNamespace("ns2");
        assertEquals("http://namespace2.somewhere.com", namespace.getUri());

    }
}
