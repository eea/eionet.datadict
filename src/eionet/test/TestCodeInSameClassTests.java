package eionet.test;

import eionet.util.QueryString;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class TestCodeInSameClassTests {

	/**
	 * 
	 * @return
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for tests that are in the same classes that are tested");
		//$JUnit-BEGIN$
		suite.addTest(new JUnit4TestAdapter(QueryString.class));
		//$JUnit-END$
		return suite;
	}

}
