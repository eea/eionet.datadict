package eionet.meta.dao;

import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.dao.domain.RdfNamespace;
import eionet.meta.spring.SpringApplicationContext;

/**
 * RDFNamespaceDAO tests.
 *
 * @author Kaido LAine
 */
public class IRdfNamespaceTestIT extends DDDatabaseTestCase {

    /** local dao instance. */
    private IRdfNamespaceDAO dao;

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
        dao = SpringApplicationContext.getContext().getBean(IRdfNamespaceDAO.class);
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
        dao = SpringApplicationContext.getContext().getBean(IRdfNamespaceDAO.class);
        RdfNamespace namespace = dao.getNamespace("ns2");
        assertEquals("http://namespace2.somewhere.com", namespace.getUri());

    }
}
