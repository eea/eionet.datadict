package eionet.meta;

import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.help.Helps;


/**
 * Integration test to verify that the eionet.help module works correctly.
 * As long as DD doesn't configure where to load the help module's configuration
 * this test shows that the fallback to properties file works.
 */
public class HelpModuleTest extends DDDatabaseTestCase {

    @Override
    protected String getSeedFilename() {
        return "seed-hlp.xml";
    }

    @Test
    public void testSimpleValues() throws Exception {
        Helps.load();
        String html = Helps.get("documentation", "doc1", "");
        assertEquals("<h1>Data Dictionary - functions</h1>", html);
    }

}
