package eionet.util;

import junit.framework.TestCase;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class OptionsTest extends TestCase {

    public static void test_Options(){
        Options opts = new Options("");

        opts.add("kala", "ahven, angerjas");
        opts.add("auto", "ford, nissan, bmw");
        opts.add("loom", null);
        String[] args = new String[4];
        args[0] = "-kala";
        args[1] = "ahven";
        args[2] = "-auto";
        args[3] = "bmw";
        assertFalse(opts.parse(args));

        assertEquals("ahven" , opts.get("kala"));
        assertEquals("bmw" , opts.get("auto"));
        assertEquals(null , opts.get("loom"));

    }

    public static void test_ParseFailure(){
        Options opts = new Options("");

        opts.add("kala", "ahven, angerjas");
        opts.add("auto", "ford, nissan, bmw");
        opts.add("loom", null);
        String[] args = new String[2];
        args[0] = "-kala";
        args[1] = "XXX";
        assertTrue(opts.parse(args));
        assertEquals("Illegal option or option value XXX !\n", opts.getErrorMsg());
        assertEquals(null, opts.get("kala"));

    }


}
