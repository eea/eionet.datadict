package eionet.meta;

import org.junit.Assert;
import org.junit.Test;

/**
 * Datalement helper method tests.
 *
 * @author Kaido Laine
 */
public class DataElementTest {

    /**
     * tests element with external prefix.
     */
    @Test
    public void testGetPrefix() {
        DataElement elem = new DataElement();
        elem.setIdentifier("geo:lat");
        Assert.assertEquals("geo", elem.getNameSpacePrefix());
    }

    /**
     * tests element with no NS prefix.
     */
    @Test
    public void testGetPrefixNoPrefix() {
        DataElement elem = new DataElement();
        elem.setIdentifier("Description");
        Assert.assertNull(elem.getNameSpacePrefix());
    }
}
