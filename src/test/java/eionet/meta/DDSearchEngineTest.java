package eionet.meta;

import eionet.DDDatabaseTestCase;

/*
 * This unittest tests the DDSearchEngine on an empty database.
 */
public class DDSearchEngineTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-emptydb.xml";
    }

    /**
     * Test instantiation. If it succeeds, then this is the test.
     *
     * @throws Exception
     */
    public void testInstantiation() throws Exception {
        DDSearchEngine d = new DDSearchEngine(getConnection().getConnection());
    }

    /**
     * Test instantiation. If it succeeds, then this is the test.
     *
     * @throws Exception
     */
    public void testInstantiationWithSession() throws Exception {
        DDSearchEngine d = new DDSearchEngine(getConnection().getConnection(), "fake-session");
    }

}
