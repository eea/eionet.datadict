package eionet.util.sql;

import java.sql.Types;

import junit.framework.TestCase;

/**
 * A class for unit testing <code>eionet.util.sql.SQLArgumentsTest</code>.
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 */
public class SQLArgumentsTest extends TestCase{

	/**
	 * 
	 */
	public void test(){
		
		SQLArguments sqlArgs = new SQLArguments();
		sqlArgs.add("string1");
		sqlArgs.add("string2", Types.VARCHAR);
		sqlArgs.add("345", Types.INTEGER);
		sqlArgs.add("+", Types.CHAR);
		
		assertEquals((int)4, sqlArgs.size());
		assertEquals("345", sqlArgs.getValue(2));
		assertEquals(Types.CHAR, sqlArgs.getSQLType(3));
		
		String str = sqlArgs.toString();
		assertNotNull(str);
		assertTrue(str.indexOf("string1, Types.JAVA_OBJECT")>=0);
		assertTrue(str.indexOf("string2, Types.VARCHAR")>=0);
		assertTrue(str.indexOf("345, Types.INTEGER")>=0);
		assertTrue(str.indexOf("+, Types.CHAR")>=0);
	}
}
