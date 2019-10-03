package eionet.meta.web.action;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eionet.DDDatabaseTestCase;
import eionet.meta.ActionBeanUtils;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.web.action.ElementsRdfActionBean;

/**
 *
 * Tests elements RDf.
 *
 * @author Kaido Laine
 */
public class ElementsRdfActionBeanTestIT  extends DDDatabaseTestCase {

    /**
     * looks for some expected Strings in the output.
     * @throws Exception if error happens
     */
    @Test
    public void testElementsRdf() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, ElementsRdfActionBean.class);
        trip.execute();

        String output = trip.getOutputString();

        String baseUri = Props.getRequiredProperty(PropsIF.DD_URL);

        String expectedHeader = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
                + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xml:base=\"" + baseUri +  "/property/\">";
        String expectedProp1 = "<rdf:Property rdf:about=\"E1\">";

        assertTrue(StringUtils.contains(output, expectedHeader));
        assertTrue(StringUtils.contains(output, expectedProp1));
    }

    @Override
    protected String getSeedFilename() {
        return "seed-dataelementsrdf.xml";
    }

}
