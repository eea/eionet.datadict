package eionet.meta;

import eionet.meta.DDSearchParameter;
import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 *
 *
 */
public class DDSearchParameterTest extends TestCase {

    private static DDSearchParameter par;

    protected void setUp(){
        Vector v = new Vector();
        v.add("kal'a");
        v.add("maja");
        par = new DDSearchParameter("9", v);
    }

    public static void test_apostrophizeValues(){
        par.apostrophizeValues();
        
        Vector vv = par.getAttrValues();
	assertEquals(vv.get(0),"'kal''a'");
	assertEquals(vv.get(1),"'maja'");
    }

    public static void test_getAttrID(){
	assertEquals(par.getAttrID(),"9");
    }

}
