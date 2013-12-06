package eionet.util;

import junit.framework.TestCase;

/**
 * Test if we can get a property.
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class PropsTest extends TestCase {

    /**
     *
     */
    public void test_getProperty() {
        assertEquals("com.mysql.jdbc.Driver", Props.getProperty(PropsIF.DBDRV));
        assertEquals(0, Props.getIntProperty("arbitrary"));
    }

}
