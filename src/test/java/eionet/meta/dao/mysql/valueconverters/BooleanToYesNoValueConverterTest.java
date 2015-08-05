package eionet.meta.dao.mysql.valueconverters;

import static org.junit.Assert.*;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
public class BooleanToYesNoValueConverterTest extends UnitilsJUnit4 {
    
    private final BooleanToYesNoConverter converter = new BooleanToYesNoConverter();
    
    @Test
    public void testConvert() {
        assertEquals("Y", converter.convert(true));
        assertEquals("N", converter.convert(false));
    }
    
    @Test
    public void testConvertBack() {
        assertTrue(converter.convertBack("Y"));
        assertTrue(converter.convertBack("y"));
        assertFalse(converter.convertBack("N"));
        assertFalse(converter.convertBack("n"));
        assertFalse(converter.convertBack(null));
        assertFalse(converter.convertBack(""));
        assertFalse(converter.convertBack(" "));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConvertBackFail1() {
        converter.convertBack("Yy");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConvertBackFail2() {
        converter.convertBack("Nn");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConvertBackFail3() {
        converter.convertBack("w");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConvertBackFail4() {
        converter.convertBack("dfhg");
    }
}
