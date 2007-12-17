package eionet.util;

import eionet.util.QueryString;
import junit.framework.TestCase;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class QueryStringTest
     extends TestCase {

        private boolean hasParam(QueryString queryString, String param){

                if (queryString.getValue().indexOf(param + "=")>0)
                        return true;
                return false;
        }

        public void testHasParam(){

                QueryString qryStr = new QueryString("param1=value1&param2=");
                assertTrue(hasParam(qryStr, "param2"));
        }

}
