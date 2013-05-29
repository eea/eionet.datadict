package eionet.meta.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Enumeration;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * Tests CASFilterConfig class.
 *
 * @author Rait Väli
 */
public class CASFilterConfigTest {
    /**
     * Adds same parameters to FakeFilterConfig object as CASFilterConfig has and compares objects.
     */
    @Test
    public void testHashtableEquals() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        CASFilterConfig casFC = CASFilterConfig.getInstance();
        FakeFilterConfig casFC2 = new FakeFilterConfig();
        Enumeration<String> names = casFC.getInitParameterNames();
        // fills in FakeFilterConfig HashMap with CASFilterConfig HashMap data
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = casFC.get(name);
            casFC2.put(name, value);
        }
        assertTrue("Test if CASFIlterconfig class is equal to FakeFilterConfig.", casFC.equals(casFC2));
    }

    /**
     * Method first call init method on CASFilterConfig and then control that returned instance is not null.
     */
    @Test
    public void testGetInstance() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        assertNotNull("Test that CASFilterConfig instance is not null.", CASFilterConfig.getInstance());

    }

    /**
     * Test that getFilterName return filter name.
     */
    @Test
    public void testGetFilterName() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        CASFilterConfig casFC = CASFilterConfig.getInstance();
        assertEquals("Test that getFilterName method returns filter name.", "testFilterName", casFC.getFilterName());
    }

    /**
     * Test that getInitParameter method return certain parameter value.
     */
    @Test
    public void testGetInitParameter() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        CASFilterConfig casFC = CASFilterConfig.getInstance();
        assertEquals("Test that getInitParameter methot return parameter 1 value.", "value1", casFC.getInitParameter("param1"));
        assertEquals("Test that getInitParameter methot return parameter 2 value.", "value2", casFC.getInitParameter("param2"));

    }

    /**
     * Counts parameter names and compare them with CASInitParam enumeration length + 2 fake parameters.
     */
    @Test
    public void testGetInitParameterNames() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        CASFilterConfig casFC = CASFilterConfig.getInstance();
        int parameterCounter = 0;
        Enumeration<String> names = casFC.getInitParameterNames();
        while (names.hasMoreElements()) {
            names.nextElement();
            parameterCounter++;
        }
        assertEquals("Test counts, how many parameter keys getInitParameterNames method returns.",
                CASInitParam.values().length + 2, parameterCounter);
    }

    /**
     * Test that servlet context is not null.
     */
    @Test
    public void testGetServletContext() {
        FakeFilterConfig fakeFC = new FakeFilterConfig();
        CASFilterConfig.init(fakeFC);
        CASFilterConfig casFC = CASFilterConfig.getInstance();
        assertNotNull("Test that getServletContext returned object is not null.", casFC.getServletContext());
    }

    /**
     * Reset the {@link CASFilterConfig} singleton before each test, to keep them isolated.
     * 
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Field instance = CASFilterConfig.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
}
