package eionet;

import java.util.Date;
import junit.framework.TestCase;

/**
 * 
 * @author Søren Roug, e-mail: <a href="mailto:soren.roug@eea.europa.eu">soren.roug@eea.europa.eu</a>
 *
 */
public class LocaleTest extends TestCase {
    

    public void test_javac_utf8() {
        // Verify that javac can handle "Elektra" in the Greek alphabet
        char elektra[] = { 
            '\u0397', '\u03bb', '\u03ad', '\u03ba', '\u03c4', '\u03c1', '\u03b1'
        };
        String Elektra = new String(elektra);
        assertEquals("Dude, set your locale to UTF-8", "Ηλέκτρα", Elektra);
    }

}
