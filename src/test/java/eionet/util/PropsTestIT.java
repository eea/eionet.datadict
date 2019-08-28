package eionet.util;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test if we can get a property.
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PropsTestIT extends TestCase {

    /**
     *
     */
    @Test
    public void test_getProperty() {
        assertEquals("com.mysql.jdbc.Driver", Props.getProperty(PropsIF.DBDRV));
        assertEquals(0, Props.getIntProperty("arbitrary"));
    }

}
