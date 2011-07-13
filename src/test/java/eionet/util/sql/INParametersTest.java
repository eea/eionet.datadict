package eionet.util.sql;

import java.sql.Types;

import junit.framework.TestCase;

/**
 * A class for unit testing <code>eionet.util.sql.INParameters</code>.
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class INParametersTest extends TestCase{

	/**
	 * 
	 */
	public void test(){
		
		INParameters inParams = new INParameters();
		inParams.add("string1");
		inParams.add("string2", Types.VARCHAR);
		inParams.add("345", Types.INTEGER);
		inParams.add("+", Types.CHAR);
		
		assertEquals((int)4, inParams.size());
		assertEquals("345", inParams.getValue(2));
		assertEquals(Types.CHAR, inParams.getSQLType(3).intValue());
		
		String str = inParams.toString();
		assertNotNull(str);
		assertTrue(str.indexOf("string1, null")>=0);
		assertTrue(str.indexOf("string2, Types.VARCHAR")>=0);
		assertTrue(str.indexOf("345, Types.INTEGER")>=0);
		assertTrue(str.indexOf("+, Types.CHAR")>=0);
	}
}
