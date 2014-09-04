package eionet.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class UtilTest {

    /**
     *
     *
     */
    @Test
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

    /*
     * Check that newlines in input immediately after a URL are handled correctly.
     */
    @Test
    public void test_replaceTagsMultiLine() {
        String input =
                "The templates (XML schema) and the reporting manual are available"
                        + " at: http://www.eionet.europa.eu/schemas/eprtr\n\nTemplate for resubmissions is"
                        + " available at: http://circa.europa.eu/Public/irc/env/e_prtr/library?l=/e-prtr_r"
                        + "e-delivery/resubmissionxls/_EN_1.0_&a=i";

        String expected =
                "The templates (XML schema) and the reporting manual are available"
                        + " at: <a href=\"http://www.eionet.europa.eu/schemas/eprtr\">http://www.eionet.e"
                        + "uropa.eu/schemas/eprtr</a><br/><br/>Template for resubmissions is"
                        + " available at: <a href=\"http://circa.europa.eu/Public/irc/env/e_prtr/library?"
                        + "l=/e-prtr_re-delivery/resubmissionxls/_EN_1.0_&amp;a=i\">http://circa.europa.eu"
                        + "/Public/irc/env/e_prtr/libra...</a>";

        String result = Util.processForDisplay(input);
        // System.out.println(result);
        assertEquals(expected, result);
    }

    /**
     *
     *
     */
    @Test
    public void test_isURI() {
        assertTrue(Util.isURI("http://cdr.eionet.europa.eu/"));
        assertTrue(Util.isURI("ftp://ftp.eionet.europa.eu/"));
        assertTrue(Util.isURI("XXX"));
    }

    @Test
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

    @Test
    public void testEncodeUrl() {
        assertEquals("a%20b", Util.encodeURLPath("a b"));
        assertEquals("ABC", Util.encodeURLPath("ABC"));
        assertEquals("A+b%20%20%20c", Util.encodeURLPath("A+b   c"));
    }

    @Test
    public void testIrrelevantAttributes() {
        assertTrue(Util.skipAttributeByDatatype("MinSize", "localref"));
        assertTrue(!Util.skipAttributeByDatatype("Name", "string"));
    }

    @Test
    public void testvalidUrl() {
        assertTrue(Util.isValidUri("http://java.sun.com"));
        assertTrue(Util.isValidUri("http://java.sun.com/"));
        assertTrue(Util.isValidUri("http://test.tripledev.ee#"));
        assertFalse(Util.isValidUri("http://www. spacein.there.dk"));
        assertFalse(Util.isValidUri("appi"));
        assertFalse(Util.isValidUri("http://"));
        assertFalse(Util.isValidUri("ftp://someftp.outthere.org//"));
        assertFalse(Util.isValidUri("ftp://someftp.outthere.org.."));
        assertFalse(Util.isValidUri("ftp://someftp.outthere.org\\"));
        assertFalse(Util.isValidUri("http://www.spacein.there.dk/there is  something.html"));
        assertTrue(Util.isValidUri("http://www.spacein.there.dk/thereissomething/"));
        assertTrue(Util.isValidUri("ftp://someftp.outthere.org"));
        assertTrue(Util.isValidUri("urn:aa:lv"));
        assertTrue(Util.isValidUri("urn:aa:lv:"));
        assertTrue(Util.isValidUri("urn:aa:lv::"));
        assertTrue(Util.isValidUri("urn:ogc:def:crs:EPSG::4258"));
        assertTrue(Util.isValidUri("mailto:juhan@hot.ee"));
        assertTrue(Util.isValidUri("http://tripledev.ee/"));
        assertFalse(Util.isValidUri("http://test.tripledev.ee//"));
    }

    @Test
    public void testValidIdentifiers() {
        assertTrue(Util.isValidIdentifier("id"));
        assertTrue(Util.isValidIdentifier("id123IDž"));
        assertTrue(Util.isValidIdentifier("id--2"));
    }
    @Test
    public void testInvalidIdentifiers() {
        assertFalse(Util.isValidIdentifier("id:id"));
        assertFalse(Util.isValidIdentifier("id#id"));
        assertFalse(Util.isValidIdentifier("id+id"));
        assertFalse(Util.isValidIdentifier("id?id"));
        assertFalse(Util.isValidIdentifier("id id"));
        assertFalse(Util.isValidIdentifier(" "));
    }

}
