package eionet.meta.dao.domain;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * FixedValue test.
 *
 * @author kaido
 */
public class FixedValueTest {

/**
 * tests getLabel().
 */
@Test
public void getLabelTest() {
    FixedValue fxv1 = new FixedValue();
    fxv1.setValue("EE");
    fxv1.setShortDescription("EST");
    fxv1.setDefinition("Estonia");

    FixedValue fxv2 = new FixedValue();
    fxv2.setValue("GR");
    fxv2.setDefinition("Greece");

    assertEquals("EE [EST]", fxv1.getLabel());
    assertEquals("GR [Greece]", fxv2.getLabel());

    }

    @Test
    public void testOwnerTypeMatch() {
        assertTrue(FixedValue.OwnerType.DATA_ELEMENT.isMatch("elem"));
        assertTrue(FixedValue.OwnerType.ATTRIBUTE.isMatch("attr"));
    }

    @Test
    public void testOwnerTypeParse() {
        assertEquals(FixedValue.OwnerType.DATA_ELEMENT, FixedValue.OwnerType.parse("elem"));
        assertEquals(FixedValue.OwnerType.ATTRIBUTE, FixedValue.OwnerType.parse("attr"));
        assertNull(FixedValue.OwnerType.parse(null));
        assertNull(FixedValue.OwnerType.parse(""));
        assertNull(FixedValue.OwnerType.parse("   "));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testOwnerTypeParseFail1() {
        FixedValue.OwnerType.parse("dfhgd");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testOwnerTypeParseFail2() {
        FixedValue.OwnerType.parse("elem111");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testOwnerTypeParseFail3() {
        FixedValue.OwnerType.parse("attr111");
    }
    
}