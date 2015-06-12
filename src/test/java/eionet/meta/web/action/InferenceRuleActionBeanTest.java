package eionet.meta.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.meta.dao.domain.DataElement;
import eionet.meta.dao.domain.InferenceRule;
import eionet.meta.dao.domain.InferenceRule.RuleType;
import eionet.meta.service.DBUnitHelper;
import eionet.util.SecurityUtil;
import eionet.web.action.InferenceRuleActionBean;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dr
 */
public class InferenceRuleActionBeanTest {
    
    /**
     * Load seed data file.
     *
     * @throws Exception
     *             if loading fails
     */
    @BeforeClass
    public static void loadData() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
        DBUnitHelper.loadData("seed-inferenceRules.xml");
    }
    
    /**
     * Delete helper data.
     *
     * @throws Exception
     *             if delete fails
     */
    @AfterClass
    public static void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-dataelements.xml");
        DBUnitHelper.deleteData("seed-inferenceRules.xml");
    }
    
    @Test
    public void testGrepElementsJsonResponse() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, InferenceRuleActionBean.class);
        
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("parentElementId", "1");
        trip.addParameter("pattern", "comm");
        trip.execute("search");
        
        MockHttpServletResponse response = trip.getResponse();
        assertEquals("Response is not JSON", "application/json", response.getContentType());
        
        InferenceRuleActionBean bean = trip.getActionBean(InferenceRuleActionBean.class);
        
        String output = trip.getResponse().getOutputString();
        JSONArray jsonArray = JSONArray.fromObject(output);
        Collection<DataElement> searchedElements = JSONArray.toCollection(jsonArray, DataElement.class);
        
        assertEquals("Number of matched elements is not right ", 3, searchedElements.size());
        
        
        Set expectedNames = new HashSet(Arrays.asList("common1", "common2", "common3"));
        
        Set searchedNames = new HashSet();
        for(DataElement element : searchedElements){
            searchedNames.add(element.getShortName());
        }
        
        assertEquals("Searched elements do not match", expectedNames, searchedNames);
    }
    
    @Test
    public void testViewRules() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, InferenceRuleActionBean.class);
        
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("parentElementId", "1");
        trip.execute("view");
        
        MockHttpServletResponse response = trip.getResponse();
        assertEquals("Response is not HTML", "text/html", response.getContentType());
        
        InferenceRuleActionBean bean = trip.getActionBean(InferenceRuleActionBean.class);
        
        Collection<InferenceRule> rules = bean.getRules();
        
        Collection<InferenceRule> expectedRules = new HashSet();
        DataElement elem1 = new DataElement();
        elem1.setId(1);
        DataElement elem3 = new DataElement();
        elem3.setId(3);
        DataElement elem4 = new DataElement();
        elem4.setId(4);
        expectedRules.add(new InferenceRule(elem1, RuleType.INVERSE, elem3));
        expectedRules.add(new InferenceRule(elem1, RuleType.INVERSE, elem4));
        
        assertEquals("Size of Element rules does not match", expectedRules.size(), rules.size());
        for(InferenceRule rule : expectedRules){
            assertTrue("Element rule not found : (" + rule.getSourceDElement().getId() + ", " + rule.getTypeName() + ", " + rule.getTargetDElement().getId() + ")", rules.contains(rule));
        }
    }
}
