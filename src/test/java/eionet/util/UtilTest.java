package eionet.util;

import junit.framework.TestCase;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Assert;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class UtilTest extends TestCase {

    /**
     *
     *
     */
    public void test_replaceTags() {
        assertEquals(Util.processForDisplay("http://cdr.eionet.europa.eu/search?y=1&z=2"),
        "<a href=\"http://cdr.eionet.europa.eu/search?y=1&amp;z=2\">http://cdr.eionet.europa.eu/search?y=1&amp;z=2</a>");

        // Test simple &
        assertEquals(Util.processForDisplay("Fruit & Vegetables"), "Fruit &amp; Vegetables");

        // Test newline
        assertEquals(Util.processForDisplay("Fruit\nVegetables"), "Fruit<br/>Vegetables");

        // Don't create anchors = true
        assertEquals(Util.processForDisplay("http://cdr.eionet.europa.eu/search?y=1&z=7", true),
        "http://cdr.eionet.europa.eu/search?y=1&amp;z=7");

        // Test Unicode char
        assertEquals(Util.processForDisplay("€"), "&#8364;");

        // Test HTML tags
        assertEquals(Util.processForDisplay("<div class='Apostrophs'>"), "&lt;div class=&#039;Apostrophs&#039;&gt;");
        assertEquals(Util.processForDisplay("<div class=\"Quotes\">"), "&lt;div class=&quot;Quotes&quot;&gt;");
    }

    /**
     *
     *
     */
    public void test_isURI() {
        assertTrue(Util.isURI("http://cdr.eionet.europa.eu/"));
        assertTrue(Util.isURI("ftp://ftp.eionet.europa.eu/"));
        assertTrue(Util.isURI("XXX"));
    }

    public void test_setAnchorsInParenthesis() {
        assertEquals(
                "Some text (<a href=\"http://en.wikipedia.org/wiki/Fahrvergnügen\">http://en.wikipedia.org/wiki/Fahrvergnügen</a>).",
                Util.processForLink("Some text (http://en.wikipedia.org/wiki/Fahrvergnügen).", false, 100));
        assertEquals(
                "Some text (<a href=\"http://en.wikipedia.org/wiki/Fahrvergnügen\">http://en.wikipedia.org/wiki/Fahrvergnügen</a> ).",
                Util.processForLink("Some text (http://en.wikipedia.org/wiki/Fahrvergnügen ).", false, 100));
        assertEquals(
                "Some text ( <a href=\"http://en.wikipedia.org/wiki/Fahrvergnügen\">http://en.wikipedia.org/wiki/Fahrvergnügen</a>).",
                Util.processForLink("Some text ( http://en.wikipedia.org/wiki/Fahrvergnügen).", false, 100));
    }

    public void testEncodeUrl() {
        assertEquals("a%20b", Util.encodeURLPath("a b"));
        assertEquals("ABC", Util.encodeURLPath("ABC"));
        assertEquals("A+b%20%20%20c", Util.encodeURLPath("A+b   c"));



    }

    public void testIrrelevantAttributes() {
        assertTrue(Util.skipAttributeByDatatype("MinSize", "localref"));
        assertTrue(!Util.skipAttributeByDatatype("Name", "string"));
    }

    public void testvalidUrl() {

        Assert.assertTrue(Util.isValidUri("http://java.sun.com"));
        Assert.assertTrue(!Util.isValidUri("http://www. spacein.there.dk"));

        Assert.assertTrue(!Util.isValidUri("appi"));
        Assert.assertTrue(!Util.isValidUri("http://"));
        Assert.assertTrue(Util.isValidUri("ftp://someftp.outthere.org"));

        Assert.assertTrue(Util.isValidUri("urn:aa:lv"));

        Assert.assertTrue(Util.isValidUri("mailto:juhan@hot.ee"));

    }

    private static boolean isValid(String str, UrlValidator val) {
        System.out.println(str + " " + val.isValid(str));
            return Util.isURI(str);
    }


}
