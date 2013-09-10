package eionet.meta.dao.domain;

import static org.junit.Assert.assertEquals;

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


}