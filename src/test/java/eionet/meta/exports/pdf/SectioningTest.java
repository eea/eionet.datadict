package eionet.meta.exports.pdf;

import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author Søren Roug
 *
 */
public class SectioningTest extends TestCase {

    public void testLevels() {
        Sectioning sec = new Sectioning();
        sec.level("Title1", 1);
        sec.level("Title2", 1);
        sec.level("Title2.1", 2);
        sec.level("Title2.2", 2);
        sec.level("Title3", 1);
        sec.level("Title3.1", 2);
        sec.level("Title3.1.1", 3);
        sec.level("Title3.1.2", 3);
        sec.level("Title3.2", 2);
        sec.level("Title4", 1);

        sec.inc("title1", false);
        assertEquals("5.", sec.getNumber());

        sec.inc("title2", false);
        assertEquals("6.", sec.getNumber());

        sec.down("title2.1", false);
        assertEquals("6.1", sec.getNumber());

        sec.inc("title2.2", false);
        assertEquals("6.2", sec.getNumber());

        sec.inc("title2.3", false);
        assertEquals("6.3", sec.getNumber());

        sec.up("title3", false);
        assertEquals("7.", sec.getNumber());

        sec.down("title3.1", false);
        assertEquals("7.1", sec.getNumber());

        sec.down("title3.1.1", false);
        assertEquals("7.1.1", sec.getNumber());

        sec.inc("title3.1.2", false);
        assertEquals("7.1.2", sec.getNumber());

        sec.up("title4", 1, false);
        assertEquals("8.", sec.getNumber());

        sec.down("title4.1", false);
        assertEquals("8.1", sec.getNumber());

        sec.down("title4.1.1", false);
        assertEquals("8.1.1", sec.getNumber());

        sec.up("title4.2", false);
        assertEquals("8.2", sec.getNumber());

        Vector t = sec.getTOCformatted("\t");
        assertEquals(10, t.size());
        assertEquals("1. Title1", t.get(0));
        assertEquals("2. Title2", t.get(1));
        assertEquals("\t2.1 Title2.1", t.get(2));
        assertEquals("\t2.2 Title2.2", t.get(3));
        assertEquals("3. Title3", t.get(4));
        assertEquals("\t3.1 Title3.1", t.get(5));
        assertEquals("\t\t3.1.1 Title3.1.1", t.get(6));
        assertEquals("\t\t3.1.2 Title3.1.2", t.get(7));
        assertEquals("\t3.2 Title3.2", t.get(8));
        assertEquals("4. Title4", t.get(9));
    }
}
