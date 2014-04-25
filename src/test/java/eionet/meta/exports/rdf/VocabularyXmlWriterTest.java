package eionet.meta.exports.rdf;

import static junit.framework.Assert.assertEquals;
import eionet.util.StringEncoder;
import org.junit.Test;

/**
 * This class has no relevant tests VocabularyXmlWriter.
 */
public class VocabularyXmlWriterTest {

    /* ESCAPE IRI */
    @Test
    public void escapeSampleIRI() {
        String input = "http://site/sp ace";
        String expct = "http://site/sp%20ace";
        assertEquals(expct, StringEncoder.encodeToIRI(input));
    }
}
