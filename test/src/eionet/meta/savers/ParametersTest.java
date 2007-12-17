// The doPost method of DocUpload is protected
// Therefore we must be in the same package
package eionet.meta.savers;

import junit.framework.TestCase;
import java.io.IOException;
import javax.servlet.http.*;
import java.util.Enumeration;
import java.util.Vector;

import eionet.meta.savers.Parameters;


/**
 */
public class ParametersTest extends TestCase {
	
	/**
	 * Test
	 */
	public void testSimple() {
                Parameters pars = new Parameters();
		Vector verification = new Vector(3);
		Vector verifyvals = new Vector(3);
                
		verification.add("kala");
                pars.addParameterValue("kala", "ahven");
		verifyvals.add("ahven");
                pars.addParameterValue("kala", "haug");
		verifyvals.add("haug");
                pars.addParameterValue("kala", "siig");
		verifyvals.add("siig");
                
		verification.add("auto");
                pars.addParameterValue("auto", "mersu");
                
                Enumeration names = pars.getParameterNames();
		Enumeration everification = verification.elements();
                while (names.hasMoreElements()){
		    assertEquals(names.nextElement(), everification.nextElement());
                }
                
                
                String[] kalad = pars.getParameterValues("kala");
                for (int i=0; kalad!=null && i<kalad.length; i++)
		    assertEquals(verifyvals.get(i), kalad[i]);
                
		assertEquals(pars.getParameter("auto"),"mersu");

		assertNotNull(pars.getID());
	}

}

