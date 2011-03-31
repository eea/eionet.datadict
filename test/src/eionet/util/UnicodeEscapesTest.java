package eionet.util;

import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class UnicodeEscapesTest extends TestCase {
	
	/** */
	protected UnicodeEscapes unicodeEscapes = null;

	/**
	 * 
	 * @param name
	 */
	public UnicodeEscapesTest(String name) {
		super(name);
	}

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		unicodeEscapes = new UnicodeEscapes();
	}

	/*
	 *  (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'eionet.util.UnicodeEscapes.isXHTMLEntity(String)'
	 */
	public void testIsXHTMLEntity() {
		
		assertEquals(true, unicodeEscapes.isXHTMLEntity("&euro;"));
		assertEquals(false, unicodeEscapes.isXHTMLEntity("&euro"));
		assertEquals(false, unicodeEscapes.isXHTMLEntity("euro;"));
		assertEquals(false, unicodeEscapes.isXHTMLEntity("&;"));
	}

}
