package eionet.meta.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationError;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;

import eionet.DDDatabaseTestCase;
import eionet.meta.DDUser;
import eionet.meta.FakeUser;
import eionet.util.SecurityUtil;
import eionet.web.action.VocabularyFoldersActionBean;

/**
 * Tests for VocabularyFoldersActionBean.
 *
 * @author enver
 */
public class VocabularyFoldersActionBeanTest extends DDDatabaseTestCase {

    /**
     * Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean.
     */
    public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";
    
    private static MockServletContext ctxWithProperyBinder;
    
    /**
     * test when not allowed user tries to go to maintain page.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testMaintainForNotAllowedUser() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser2", "testUser2");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("maintain");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "No permission to modify folders", message);
    }// end of test step testMaintainForNotAllowedUser

    /**
     * test when not allowed user tries to populate empty base uris.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testPopulateForNotAllowedUser() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser2", "testUser2");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("populate");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "No permission to modify folders", message);
    }// end of test step testMaintainNotAllowedUser

    /**
     * test when not allowed user tries to change site prefix.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixForNotAllowedUser() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("newSitePrefix", "http://dd.eionet.eea.eu/");
        trip.addParameter("oldSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser2", "testUser2");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "No permission to modify folders", message);
    }// end of test step testChangeSitePrefixForNotAllowedUser

    /**
     * test user tries to change site prefix with empty site prefix.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixWithBlankNewSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("newSitePrefix", "  ");
        trip.addParameter("oldSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "New Site Prefix is missing", message);
    }// end of test step testChangeSitePrefixWithBlankNewSitePrefix

    /**
     * test user tries to change site prefix with invalid site prefix.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixWithInvalidNewSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("newSitePrefix", "http://dd.eionat.ee/invalid//");
        trip.addParameter("oldSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());

        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message",
                "New Site prefix is not a valid URI. \n The allowed schemes are: http, https, ftp, mailto, tel and urn.", message);
    }// end of test step testChangeSitePrefixWithInvalidNewSitePrefix

    /**
     * test user tries to change empty site prefix.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixWithBlankOldSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("oldSitePrefix", "  ");
        trip.addParameter("newSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "Old Site Prefix is missing", message);
    }// end of test step testChangeSitePrefixWithBlankOldSitePrefix

    /**
     * test when not allowed user tries to change invalid site prefix.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixWithInvalidOldSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("oldSitePrefix", "http://dd.eionat.ee/i n v a l i d/");
        trip.addParameter("newSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "Old Site prefix is not a valid URI. \n"
                + " The allowed schemes are: http, https, ftp, mailto, tel and urn.", message);
    }// end of test step testChangeSitePrefixWithInvalidOldSitePrefix

    /**
     * test when user tries to change site prefix with same value.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefixWithSameSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("oldSitePrefix", "http://tripledev.ee");
        trip.addParameter("newSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertNotNull("Validation Errors", validationErrors);
        Assert.assertEquals("Validation Error Size", 1, validationErrors.size());
        List<ValidationError> errors = validationErrors.get(ValidationErrors.GLOBAL_ERROR);
        String message = ((SimpleError) errors.get(0)).getMessage();
        Assert.assertEquals("Validation Error Message", "Old and New Site Prefixes are the same.", message);
    }// end of test step testChangeSitePrefixWithInvalidOldSitePrefix

    /**
     * test when user tries to populate.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testPopulate() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("populate");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertEquals("Validation Error Size", 0, validationErrors.size());
    }// end of test step testPopulate

    /**
     * test when user tries to change site prefix with valid values.
     *
     * @throws Exception
     *             if test fails
     */
    @Test
    public void testChangeSitePrefix() throws Exception {
        MockServletContext ctx = getServletContextWithProperyBinder();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFoldersActionBean.class);
        trip.addParameter("oldSitePrefix", "http://tripledev.ee/datadict");
        trip.addParameter("newSitePrefix", "http://tripledev.ee/");
        // set a user
        DDUser user = new FakeUser();
        user.authenticate("testUser", "testUser");
        trip.getRequest().getSession().setAttribute(SecurityUtil.REMOTEUSER, user);
        // call test method
        trip.execute("changeSitePrefix");
        ValidationErrors validationErrors = trip.getValidationErrors();
        Assert.assertEquals("Validation Error Size", 0, validationErrors.size());
    }// end of test step testChangeSitePrefix

    @Override
    protected String getSeedFilename() {
        return "seed-vocabularycsv.xml";
    }

    /**
     * This method creates and returns a mock servlet context with a property finder to be used with file bean
     *
     * @return
     */
    private static MockServletContext getServletContextWithProperyBinder() {
        if (VocabularyFoldersActionBeanTest.ctxWithProperyBinder == null) {
            MockServletContext ctx = new MockServletContext("test");

            Map<String, String> filterParams = new HashMap<String, String>();

            filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
            filterParams.put("ActionResolver.Packages", "eionet.web.action");

            filterParams.put("ActionBeanContext.Class", "eionet.web.DDActionBeanContext");
            filterParams.put("ActionBeanPropertyBinder.Class",
                    "eionet.meta.web.action.VocabularyFoldersActionBeanTest$MyActionBeanPropertyBinder");

            ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
            ctx.addInitParameter("contextConfigLocation", "classpath:mock-spring-context.xml");

            ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

            ContextLoaderListener springContextLoader = new ContextLoaderListener();
            springContextLoader.contextInitialized(new ServletContextEvent(ctx));
            VocabularyFoldersActionBeanTest.ctxWithProperyBinder = ctx;
        }
        
        return VocabularyFoldersActionBeanTest.ctxWithProperyBinder;
    }// end of method getServletContextWithProperyBinder

    /**
     * Extension of {@link net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder} in order to directly inject the
     * proper file bean.
     *
     * @author Jaanus
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class MyActionBeanPropertyBinder extends DefaultActionBeanPropertyBinder {

        /**
         * Default constructor.
         */
        public MyActionBeanPropertyBinder() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder#bind(net.sourceforge.stripes.action.ActionBean,
         * net.sourceforge.stripes.action.ActionBeanContext, boolean)
         */
        @Override
        public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {

            ValidationErrors validationErrors = super.bind(bean, context, validate);

            if (bean != null && context != null) {
                HttpServletRequest request = context.getRequest();
                if (request != null) {
                    Object o = request.getAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME);
                    if (o instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> richTypeRequestParams = (Map<String, Object>) o;
                        for (Entry<String, Object> entry : richTypeRequestParams.entrySet()) {
                            String paramName = entry.getKey();
                            Object paramValue = entry.getValue();
                            BeanUtil.setPropertyValue(paramName, bean, paramValue);
                        }
                    }
                }
            }
            return validationErrors;
        }
    }// end of inner class MyActionBeanPropertyBinder

}// end of test VocabularyFolderActionBean
