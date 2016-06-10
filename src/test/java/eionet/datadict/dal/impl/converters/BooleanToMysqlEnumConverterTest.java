package eionet.datadict.dal.impl.converters;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class BooleanToMysqlEnumConverterTest {

    Boolean defaultValue;
    
    @Spy
    BooleanToMySqlEnumConverter converter = new BooleanToMySqlEnumConverter(defaultValue);
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testConvert() {
        assertEquals("1", converter.convert(Boolean.TRUE));
        assertEquals("0", converter.convert(Boolean.FALSE));
        assertEquals(null, converter.convert(null));
    }
    
    @Test
    public void testConvertBack() {
        assertEquals(defaultValue, converter.convertBack(""));
        assertEquals(Boolean.TRUE, converter.convertBack("1"));
        assertEquals(Boolean.FALSE, converter.convertBack("0"));
        assertEquals(null, converter.convertBack(null));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConvertBackIllegalArgument() {
        converter.convertBack("invalid");
    }
}
