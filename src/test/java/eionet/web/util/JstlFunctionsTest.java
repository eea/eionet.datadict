package eionet.web.util;

import org.junit.Assert;
import org.junit.Test;

public class JstlFunctionsTest {

    /**
     * tests on cut space.
     */
    @Test
    public void testCutAtSpace() {

        char c = (char) Integer.parseInt(String.valueOf(2026), 16);
        final String ellipsis = String.valueOf(c);

         Assert.assertEquals("abcd", JstlFunctions.cutAtSpace("abcd", 50));

         Assert.assertEquals("abc" + ellipsis, JstlFunctions.cutAtSpace("abcde ghij", 3));

         Assert.assertEquals("abcde" + ellipsis, JstlFunctions.cutAtSpace("abcdefghij", 5));
         Assert.assertEquals("I am \" a good man " + ellipsis, JstlFunctions.cutAtSpace("I am \" a good man like the other men", 17));
         Assert.assertEquals("I am \" a good man " + ellipsis, JstlFunctions.cutAtSpace("I am \" a good man like the other men", 19));
         Assert.assertEquals("I am ", JstlFunctions.cutAtSpace("I am ", 5));
         Assert.assertEquals("I " + ellipsis, JstlFunctions.cutAtSpace("I ammm", 5));
    }

    @Test
    public void testUrlEncode() {
        Assert.assertEquals("I%20like%20spaces", JstlFunctions.urlEncode("I like spaces"));
        Assert.assertEquals("keepsame", JstlFunctions.urlEncode("keepsame"));
    }
}
