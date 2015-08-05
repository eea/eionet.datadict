package eionet.meta.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.meta.dao.domain.FixedValue;
import eionet.meta.service.DBUnitHelper;
import eionet.meta.service.IDataService;
import eionet.util.SecurityUtil;
// import eionet.web.action.FixedValuesActionBean;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

/**
 *  Tests Fixed Values Action Bean
 * @author Raptis Dimos
 */
/*
@SpringApplicationContext("mock-spring-context.xml")
public class FixedValuesActionBeanTest extends UnitilsJUnit4 {
    
    @SpringBeanByType
    private IDataService dataService;
  */  
    /**
     * Load seed data file.
     * @throws Exception if loading fails
     */ /*
    @Before
    public void loadData() throws Exception {
        DBUnitHelper.loadData("seed-dataelements.xml");
        DBUnitHelper.loadData("seed-fixedValues.xml");
    }
    */
    /**
     * Delete helper data.
     * @throws Exception if delete fails
     */ /*
    @After
    public void deleteData() throws Exception {
        DBUnitHelper.deleteData("seed-dataelements.xml");
        DBUnitHelper.deleteData("seed-fixedValues.xml");
    } */
    /*
    @Test
    public void testView() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FixedValuesActionBean.class);
        
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("ownerType", "elem");
        trip.addParameter("ownerId", "1");
        trip.execute("view");
        
        MockHttpServletResponse response = trip.getResponse();
        assertEquals("Response is not HTML", "text/html", response.getContentType());
        
        FixedValuesActionBean bean = trip.getActionBean(FixedValuesActionBean.class);
        
        Collection<FixedValue> fixedValues = bean.getFixedValues();
        
        Collection valueNames = new HashSet(Arrays.asList("element first fixed value", "element second fixed value"));
        for(FixedValue value : fixedValues){
            assertTrue("Fixed Value with id " + value.getId() + "and value " + value.getValue() + " not expected", valueNames.contains(value.getValue()));
        }
        
        trip = new MockRoundtrip(ctx, FixedValuesActionBean.class);
        
        user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("ownerType", "attr");
        trip.addParameter("ownerId", "1");
        trip.execute("view");
        
        response = trip.getResponse();
        assertEquals("Response is not HTML", "text/html", response.getContentType());
        
        bean = trip.getActionBean(FixedValuesActionBean.class);
        
        fixedValues = bean.getFixedValues();
        
        assertEquals("Size of Fixed Values does not match", 2, fixedValues.size());
        
        valueNames = new HashSet(Arrays.asList("attribute first fixed value", "attribute second fixed value"));
        for(FixedValue value : fixedValues){
            assertTrue("Fixed Value with id " + value.getId() + "and value " + value.getValue() + " not expected", valueNames.contains(value.getValue()));
        }
    }
    
    @Test
    public void testEdit() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FixedValuesActionBean.class);
        
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("fixedValue.id", "1");
        trip.addParameter("ownerId", "1");
        trip.addParameter("ownerType", "elem");
        trip.addParameter("fixedValue.isDefault", "NO");
        trip.addParameter("fixedValue.value", "new element first fixed value");
        trip.addParameter("fixedValue.definition", "new definition 1");
        trip.addParameter("fixedValue.shortDescription", "new description 1");
        trip.execute("edit");
        
        MockHttpServletResponse response = trip.getResponse();
        assertEquals("Response is not HTML", "text/html", response.getContentType());
        
        FixedValue fixedValue = dataService.getFixedValueById(1);
        assertEquals("Value of edited fixed value does not match ", "new element first fixed value", fixedValue.getValue());
        assertEquals("Definition of edited fixed value does not match ", "new definition 1", fixedValue.getDefinition());
        assertEquals("Short Description of edited fixed value does not match ", "new description 1", fixedValue.getShortDescription());
    }
    
    @Test
    public void testDelete() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, FixedValuesActionBean.class);
        
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        
        trip.addParameter("fixedValue.id", "1");
        trip.addParameter("ownerId", "1");
        trip.addParameter("ownerType", "elem");
        trip.execute("delete");
        
        MockHttpServletResponse response = trip.getResponse();
        assertEquals("Response is not HTML", "text/html", response.getContentType());
        
        assertFalse("Fixed value was not successfully deleted", dataService.fixedValueExists(1));
    } */
/*
}
*/