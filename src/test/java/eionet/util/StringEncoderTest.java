package eionet.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StringEncoderTest {

    @Test
    public void noEscapeNeeded() {
        String input = "This is a simple string";
        String expct = "This is a simple string";
        assertEquals(expct, StringEncoder.escapeXml(input));
    }

    @Test
    public void noEscapeOfNatl() {
        String input = "€";
        String expct = "€";
        assertEquals(expct, StringEncoder.escapeXml(input));
    }

    @Test
    public void escapeAmp() {
        String input = "Fruit & vegetables";
        String expct = "Fruit &amp; vegetables";
        assertEquals(expct, StringEncoder.escapeXml(input));
    }

    @Test
    public void escapeApos() {
        String input = "<div class='Apostrophs'>";
        String expct = "&lt;div class=&#39;Apostrophs&#39;&gt;";
        assertEquals(expct, StringEncoder.escapeXml(input));
    }

    @Test
    public void escapeQuot() {
        String input = "<div class=\"Quotes\">";
        String expct = "&lt;div class=&quot;Quotes&quot;&gt;";
        assertEquals(expct, StringEncoder.escapeXml(input));
    }

    /* ENCODE URI */
    @Test
    public void testEncodeURIComponent() {
        String testString = ";/?:@&=+$,aA-_.!~*'()[]<>#%\"{}\n\t ";
        String expected = "%3B%2F%3F%3A%40%26%3D%2B%24%2CaA-_.!~*'()%5B%5D%3C%3E%23%25%22%7B%7D%0A%09%20";
        String actual = StringEncoder.encodeURIComponent(testString);
        assertEquals(expected, actual);
    }

    @Test
    public void encodeURI() {
        String input = "http://site/sp ace";
        String expct = "http%3A%2F%2Fsite%2Fsp%20ace";
        assertEquals(expct, StringEncoder.encodeURIComponent(input));
    }

    @Test
    public void encodeUsuals() {
        String input = "#hash+amp&";
        String expct = "%23hash%2Bamp%26";
        assertEquals(expct, StringEncoder.encodeURIComponent(input));
    }

    @Test
    public void noEncodeOfSpecials() {
        String input = "(char)quote'~excl!!";
        String expct = "(char)quote'~excl!!";
        assertEquals(expct, StringEncoder.encodeURIComponent(input));
    }

    /* Encode IRI */
    @Test
    public void encodeIRISomeChars() {
        String testString = ";/?:@&=+$,aA-_.!~*'()[]<>#%\"{}\n\t ";
        String expected = ";/?:@&=+$,aA-_.!~*'()[]%3C%3E#%%22%7B%7D\n\t%20";
        String actual = StringEncoder.encodeToIRI(testString);
        assertEquals(expected, actual);
    }

    @Test
    public void encodeIRISampleURL() {
        String input = "http://site/sp ace";
        String expct = "http://site/sp%20ace";
        assertEquals(expct, StringEncoder.encodeToIRI(input));
    }

    @Test
    public void encodeIRIAllSpecial() {
        String input = " {}<>\"|\\^`";
        String expct = "%20%7B%7D%3C%3E%22%7C%5C%5E%60";
        assertEquals(expct, StringEncoder.encodeToIRI(input));
    }

    @Test
    public void noEncodeIRIOfSpecials() {
        String input = "(char)quote'~excl!!";
        String expct = "(char)quote'~excl!!";
        assertEquals(expct, StringEncoder.encodeToIRI(input));
    }

	/* ESCAPE IRI */
	@Test
	public void escapeSampleIRI() {
		String input = "http://site/sp ace";
		String expct = "http://site/sp%20ace";
		assertEquals(expct, StringEncoder.encodeToIRI(input));
	}
}
