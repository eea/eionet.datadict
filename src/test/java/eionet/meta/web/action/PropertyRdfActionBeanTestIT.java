package eionet.meta.web.action;

import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import eionet.meta.ActionBeanUtils;
import eionet.web.action.PropertyRdfActionBean;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests on PropertyRdfActionBean.
 *
 * @author Kaido Laine
 */

@ContextConfiguration(locations = {"classpath:mock-spring-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class PropertyRdfActionBeanTestIT  {

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
