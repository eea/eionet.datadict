package eionet.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class UnicodeEscapesTest {
    
    /** */
    protected UnicodeEscapes unicodeEscapes = null;

    /*
     *  (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        unicodeEscapes = new UnicodeEscapes();
    }


    /*
     * Test method for 'eionet.util.UnicodeEscapes.isXHTMLEntity(String)'
     */
    @Test
    public void testIsXHTMLEntity() {
        
        assertEquals(true, unicodeEscapes.isXHTMLEntity("&euro;"));
        assertEquals(false, unicodeEscapes.isXHTMLEntity("&euro"));
        assertEquals(false, unicodeEscapes.isXHTMLEntity("euro;"));
        assertEquals(false, unicodeEscapes.isXHTMLEntity("&;"));
    }

}
