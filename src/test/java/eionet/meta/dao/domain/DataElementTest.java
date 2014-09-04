package eionet.meta.dao.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Data element test.
 * @author Kaido Laine
 */
public class DataElementTest {
    @Test
    public void testHashValue() {
        DataElement elem = new DataElement();
        elem.setId(1);
        elem.setAttributeLanguage("en");
        elem.setAttributeValue("This is the value");

        assertEquals("782fb9c4ce6fc639352db595b603384d", elem.getUniqueValueHash());

    }

    @Test
    public void testRelatedHashValue() {
        DataElement elem = new DataElement();
        elem.setId(2);

        elem.setAttributeLanguage("en");
        elem.setAttributeValue("This is a pointless value");
        elem.setRelatedConceptId(13);

        assertEquals("a6964cac032ad015273a5b1d7b3e7500", elem.getUniqueValueHash());

    }
    @Test
    public void testSetRelatedConceptBaseURI() {
        DataElement elem = new DataElement();
        String baseUri = "base/";
        elem.setRelatedConceptBaseURI(baseUri);
        assertEquals("base/", elem.getRelatedConceptBaseURI());

        baseUri = "base";
        elem.setRelatedConceptBaseURI(baseUri);
        assertEquals("base/", elem.getRelatedConceptBaseURI());

        baseUri = "urn::";
        elem.setRelatedConceptBaseURI(baseUri);
        assertEquals("urn::", elem.getRelatedConceptBaseURI());

        baseUri = "base#";
        elem.setRelatedConceptBaseURI(baseUri);
        assertEquals("base#", elem.getRelatedConceptBaseURI());
    }


}
