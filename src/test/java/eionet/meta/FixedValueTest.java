package eionet.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;


/**
 * Test the FixedValue class.
 */
public class FixedValueTest {

    @Test
    public void simpleStoreAndGetByShortName() {
        String VALUE = "Ηλέκτρα";

        FixedValue fv = new FixedValue("testid");
        DElemAttribute attr = new DElemAttribute("id", "name", "shortName", VALUE);
        fv.addAttribute(attr);
        String result = fv.getAttributeValueByShortName("shortName");
        //String s = result.getShortName();

        assertEquals(VALUE, result);
    }

    /**
     * Is getItems() used in the production code?
     */
    @Test
    public void simpleAddAndGetItem() {
        FixedValue fv = new FixedValue("testid");
        fv.addItem("Item1");
        fv.addItem("Item2");
        Vector itemlist = fv.getItems();
        assertEquals(2, itemlist.size());
    }
}
