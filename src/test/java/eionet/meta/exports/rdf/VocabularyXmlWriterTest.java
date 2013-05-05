package eionet.meta.exports.rdf;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

public class VocabularyXmlWriterTest {

    /* ESCAPE IRI */
    @Test
    public void escapeSomeChars() {
        String testString = ";/?:@&=+$,aA-_.!~*'()[]<>#%\"{}\n\t ";
        String expected = ";/?:@&=%2B$,aA-_.!~*'()[]%3C%3E#%%22%7B%7D\n\t%20";
        String actual = VocabularyXmlWriter.escapeIRI(testString);
        assertEquals(expected, actual);
    }

    @Test
    public void escapeSampleIRI() {
        String input = "http://site/sp ace";
        String expct = "http://site/sp%20ace";
        assertEquals(expct, VocabularyXmlWriter.escapeIRI(input));
    }

    @Test
    public void encodeAllSpecial() {
        String input = " {}<>\"|\\^`+";
        String expct = "%20%7B%7D%3C%3E%22%7C%5C%5E%60%2B";
        assertEquals(expct, VocabularyXmlWriter.escapeIRI(input));
    }

    @Test
    public void noEncodeOfSpecials() {
        String input = "(char)quote'~excl!!";
        String expct = "(char)quote'~excl!!";
        assertEquals(expct, VocabularyXmlWriter.escapeIRI(input));
    }
}
