package eionet.test;

import eionet.util.PropsIF;
import eionet.util.Props;
import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class PropsTest extends TestCase {
	
	/** */
	protected Props props = null;

	/**
	 * 
	 * @param name
	 */
	public PropsTest(String name) {
		super(name);
	}

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		props = new Props();
	}

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'eionet.util.Props.isXHTMLEntity(String)'
	 */
	public void test_getProperty() {
		assertEquals("com.mysql.jdbc.Driver", Props.getProperty(PropsIF.DBDRV));
	}

}
