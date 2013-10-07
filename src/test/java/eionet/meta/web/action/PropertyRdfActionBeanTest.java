package eionet.meta.web.action;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.meta.ActionBeanUtils;
import eionet.web.action.PropertyRdfActionBean;

/**
 * Tests on PropertyRdfActionBean.
 *
 * @author Kaido Laine
 */
public class PropertyRdfActionBeanTest  {

    /**
     * tests if bean is redirected correctly to common RDF page.
     * @throws Exception if redirection fails
     */
    @Test
    public void testRedirect() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, PropertyRdfActionBean.class);
        trip.execute();

        //as unit test is not a web browser it has to be redirected to properties/rdf page
        Assert.assertTrue(StringUtils.endsWith(trip.getRedirectUrl(), "/properties/rdf"));

    }
}
