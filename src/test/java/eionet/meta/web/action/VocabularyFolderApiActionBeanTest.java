/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Data Dictionary
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        TripleDev
 */

package eionet.meta.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.service.IJWTService;
import eionet.util.Props;
import eionet.util.PropsIF;
import eionet.web.action.VocabularyFolderApiActionBean;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.ValidationErrors;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tests for VocabularyFolderApiActionBean.
 *
 * @author enver
 */
@SpringApplicationContext("spring-context.xml")
public class VocabularyFolderApiActionBeanTest extends UnitilsJUnit4 {
//public class VocabularyFolderApiActionBeanTest extends DDDatabaseTestCase {
    /**
     * Keyword for content type.
     */
    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * API key header for request.
     */
    public static final String JWT_API_KEY_HEADER = "X-DD-API-KEY";

    /**
     * API Key identifier in json.
     */
    public static final String API_KEY_IDENTIFIER_IN_JSON = "API_KEY";

    /**
     * JWT Key. i.e. ddTestAp1KeyF4VocUpload_87923!@Q
     */
    private static final String VALID_JWT_KEY = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_KEY);

    /**
     * JWT Audience. i.e. ContentRegistry
     */
    private static final String VALID_JWT_AUDIENCE = Props.getProperty(PropsIF.DD_VOCABULARY_API_JWT_AUDIENCE);

    /**
     * JWT Expiration in minutes for signing.
     */
    private static final int VALID_JWT_EXPIRATION_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_EXP_IN_MINUTES);

    /**
     * JWT Timeout in minutes for verification (used to validate if sent token is still active or deprecated).
     */
    private static final int VALID_JWT_TIMEOUT_IN_MINUTES = Props.getIntProperty(PropsIF.DD_VOCABULARY_API_JWT_TIMEOUT_IN_MINUTES);

    /**
     * JWT Algorithm for signing. i.e. HS512
     */
    private static final String VALID_JWT_SIGNING_ALGORITHM = Props.getProperty(PropsIF.DD_VOCABULARY_ADI_JWT_ALGORITHM);

    /**
     * Valid content type for RDF upload.
     */
    public static final String VALID_CONTENT_TYPE_FOR_RDF_UPLOAD = "application/rdf+xml";

    /**
     * Invalid input status code.
     */
    public static final int INVALID_INPUT_STATUS_CODE = HttpServletResponse.SC_NOT_ACCEPTABLE;

    /**
     * Invalid content type error message.
     */
    public static final String INVALID_CONTENT_TYPE_ERROR_MESSAGE = "Invalid content-type for RDF upload";

    /**
     * Unauthorized status code.
     */
    public static final int UNAUTHORIZED_STATUS_CODE = HttpServletResponse.SC_UNAUTHORIZED;


    /**
     * JWT service.
     */
    @SpringBeanByType
    private IJWTService jwtService;

    //TODO commented out code should be checked.
//    @Override
//    protected String getSeedFilename() {
//        //return "seed-vocabularycsv.xml";
//        return "seed-emptydb.xml";
//    }

    /**
     * This method creates and returns a mock servlet context with a property finder to be used with file bean
     *
     * @return
     */
    private MockServletContext getServletContextWithPropertyBinder() {
        MockServletContext ctx = new MockServletContext("test");

        Map<String, String> filterParams = new HashMap<String, String>();

        filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
        filterParams.put("ActionResolver.Packages", "eionet.web.action");

        filterParams.put("ActionBeanContext.Class", "eionet.web.DDActionBeanContext");
//        filterParams.put("ActionBeanPropertyBinder.Class",
//                "eionet.meta.web.action.VocabularyFolderApiActionBeanTest$MyActionBeanPropertyBinder");
        filterParams.put("ActionBeanPropertyBinder.Class", MyActionBeanPropertyBinder.class.getName());//better to use this way, so class can be found by usages.

        ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        ctx.addInitParameter("contextConfigLocation", "classpath:mock-spring-context.xml");

        ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

        ContextLoaderListener springContextLoader = new ContextLoaderListener();
        springContextLoader.contextInitialized(new ServletContextEvent(ctx));
        return ctx;
    }// end of method getServletContextWithPropertyBinder

    /**
     * Call api with no content type.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testNoContentType() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", INVALID_INPUT_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", INVALID_CONTENT_TYPE_ERROR_MESSAGE, response.getErrorMessage());
    } // end of test step testNoContentType

    /**
     * Call api with invalid content type.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testInvalidContentType() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, "asdadsasd");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", INVALID_INPUT_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", INVALID_CONTENT_TYPE_ERROR_MESSAGE, response.getErrorMessage());
    } // end of test step testInvalidContentType

    /**
     * Call api with valid content type and empty jwt.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidContentTypeEmptyJwt() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "API Key cannot be missing", response.getErrorMessage());
    } // end of test step testValidContentTypeEmptyJwt

    /**
     * Call api with valid content type and blank jwt.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidContentTypeBlankJwt() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, " ");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "API Key cannot be missing", response.getErrorMessage());
    } // end of test step testValidContentTypeBlankJwt

    /**
     * Call api with valid content type and invalid jwt.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidContentTypeInvalidJwt() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "asldjkalsdk");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: Wrong number of segments: 1", response.getErrorMessage());
    } // end of test step testValidContentTypeInvalidJwt

    /**
     * Call api with valid content type, valid jwt but invalid token content (dummy payload).
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidJwtInvalidApiKeyString() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: signature verification failed", response.getErrorMessage());
    } // end of test step testValidJwtInvalidApiKeyString

    /**
     * Call api with valid but expired jwt.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidAlreadyExpiredJwt() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

//        Map<String, String> jwtPayload = new HashMap<String, String>();
//        jwtPayload.put(API_KEY_IDENTIFIER_IN_JSON, "NotExistingAPIKey");
        //Payload, generated at: http://jwtbuilder.jamiekurtz.com/ with VALID_JWT_KEY and VALID_JWT_AUDIENCE and VALID_JWT_SIGNING_ALGORITHM
//        {
//            "iss": "DDTest",
//                "iat": 1451610061,
//                "exp": 1451613661,
//                "aud": "ContentRegistry",
//                "sub": "",
//                "API_KEY": "NotExistingAPIKey"
//        }

        //trip.getRequest().addHeader(JWT_API_KEY_HEADER, jwtService.sign(VALID_JWT_KEY, VALID_JWT_AUDIENCE, jwtPayload, VALID_JWT_EXPIRATION_IN_MINUTES, VALID_JWT_SIGNING_ALGORITHM));
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJERFRlc3QiLCJpYXQiOjE0NTE2MTAwNjEsImV4cCI6MTQ1MTYxMzY2MSwiYXVkIjoiQ29udGVudFJlZ2lzdHJ5Iiwic3ViIjoiIiwiQVBJX0tFWSI6Ik5vdEV4aXN0aW5nQVBJS2V5In0.Mje0oidK35MwDr_O5CfFwvQ6brYNLRZK8jMmZaoMkAksKgzDE1mi9THHEDKw_Hb4XhpQgyHSRotrWNzj4IfhQg");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: jwt expired", response.getErrorMessage());
    } // end of test step testValidAlreadyExpiredJwt

    /**
     * Call api with valid but deprecated jwt.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidDeprecatedJwt() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

        //Payload, generated at: http://jwtbuilder.jamiekurtz.com/ with VALID_JWT_KEY and VALID_JWT_AUDIENCE and VALID_JWT_SIGNING_ALGORITHM
//        {
//            "iss": "DDTest",
//            "iat": 1451610061,
//            "exp": 4607287261,
//            "aud": "ContentRegistry",
//            "sub": "",
//            "API_KEY": "NotExistingAPIKey"
//        }

        //trip.getRequest().addHeader(JWT_API_KEY_HEADER, jwtService.sign(VALID_JWT_KEY, VALID_JWT_AUDIENCE, jwtPayload, VALID_JWT_EXPIRATION_IN_MINUTES, VALID_JWT_SIGNING_ALGORITHM));
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJERFRlc3QiLCJpYXQiOjE0NTE2MTAwNjEsImV4cCI6NDYwNzI4NzI2MSwiYXVkIjoiQ29udGVudFJlZ2lzdHJ5Iiwic3ViIjoiIiwiQVBJX0tFWSI6Ik5vdEV4aXN0aW5nQVBJS2V5In0.0TyN0RebnKJJr28-jZ_d0R6mHUEvbsD2GY6h7tZVHsOsxuUTiC5-_cpd3DCFnWX2bhzydYEZS0EapIPG1ym-4g");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: Deprecated token", response.getErrorMessage());
    } // end of test step testValidDeprecatedJwt

    /**
     * Call api with valid jwt with not existing api key.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidJwtNotExistingApiKey() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

        //Payload, generated at: http://jwtbuilder.jamiekurtz.com/ with VALID_JWT_KEY and VALID_JWT_AUDIENCE and VALID_JWT_SIGNING_ALGORITHM
//        {
//            "iss": "DDTest",
//            "iat": 4607283661,
//            "exp": 4607287261,
//            "aud": "ContentRegistry",
//            "sub": "",
//            "API_KEY": "NotExistingAPIKey"
//        }

        //trip.getRequest().addHeader(JWT_API_KEY_HEADER, jwtService.sign(VALID_JWT_KEY, VALID_JWT_AUDIENCE, jwtPayload, VALID_JWT_EXPIRATION_IN_MINUTES, VALID_JWT_SIGNING_ALGORITHM));
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJERFRlc3QiLCJpYXQiOjQ2MDcyODM2NjEsImV4cCI6NDYwNzI4NzI2MSwiYXVkIjoiQ29udGVudFJlZ2lzdHJ5Iiwic3ViIjoiIiwiQVBJX0tFWSI6Ik5vdEV4aXN0aW5nQVBJS2V5In0.nkKp1oRDxviYG0bsPMDNeuheJsy30oXT21KvjdoTrZ4k3WKZJLg6uaVcf571NmZpUDWrXLMVf3qFWw3XZMRO6Q");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: Failed to get api key", response.getErrorMessage());
    } // end of test step testValidJwtNotExistingApiKey

    /**
     * Call api with valid jwt with blank api key.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidJwtBlankApiKey() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

        //Payload, generated at: http://jwtbuilder.jamiekurtz.com/ with VALID_JWT_KEY and VALID_JWT_AUDIENCE and VALID_JWT_SIGNING_ALGORITHM
//        {
//            "iss": "DDTest",
//            "iat": 4607283661,
//            "exp": 4607287261,
//            "aud": "ContentRegistry",
//            "sub": "",
//            "API_KEY": " "
//        }

        //trip.getRequest().addHeader(JWT_API_KEY_HEADER, jwtService.sign(VALID_JWT_KEY, VALID_JWT_AUDIENCE, jwtPayload, VALID_JWT_EXPIRATION_IN_MINUTES, VALID_JWT_SIGNING_ALGORITHM));
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJERFRlc3QiLCJpYXQiOjQ2MDcyODM2NjEsImV4cCI6NDYwNzI4NzI2MSwiYXVkIjoiQ29udGVudFJlZ2lzdHJ5Iiwic3ViIjoiIiwiQVBJX0tFWSI6IiAifQ.Iq8KPUwAJp6R1pKuxJZCh8PHoctRZAVzgr3RHaP0bKnIK9acyhDV9a-f6AviM5E0Ua7RMzbuRW3WQ1W7y5bq5g");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: Failed to get api key", response.getErrorMessage());
    } // end of test step testValidJwtBlankApiKey

    /**
     * Call api with valid jwt without api key.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testValidJwtWithoutApiKey() throws Exception {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderApiActionBean.class);
        trip.getRequest().addHeader(CONTENT_TYPE_HEADER, VALID_CONTENT_TYPE_FOR_RDF_UPLOAD);

        //Payload, generated at: http://jwtbuilder.jamiekurtz.com/ with VALID_JWT_KEY and VALID_JWT_AUDIENCE and VALID_JWT_SIGNING_ALGORITHM
//        {
//            "iss": "DDTest",
//            "iat": 4607283661,
//            "exp": 4607287261,
//            "aud": "ContentRegistry",
//            "sub": ""
//        }

        //trip.getRequest().addHeader(JWT_API_KEY_HEADER, jwtService.sign(VALID_JWT_KEY, VALID_JWT_AUDIENCE, jwtPayload, VALID_JWT_EXPIRATION_IN_MINUTES, VALID_JWT_SIGNING_ALGORITHM));
        trip.getRequest().addHeader(JWT_API_KEY_HEADER, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJERFRlc3QiLCJpYXQiOjQ2MDcyODM2NjEsImV4cCI6NDYwNzI4NzI2MSwiYXVkIjoiQ29udGVudFJlZ2lzdHJ5Iiwic3ViIjoiIn0.2nd6OoGzcI-Xt3eB4gRuIr4M95q1-1SfvLsQKG5XJJwziutD_1l7JFH42ub-hH-uWOjcN8baiREJLKIA8zKC9Q");
        trip.execute("uploadRdf");
        MockHttpServletResponse response = trip.getResponse();
        Assert.assertEquals("Status code", UNAUTHORIZED_STATUS_CODE, response.getStatus());
        Assert.assertEquals("Error message", "Cannot authorize: JSONObject[\"API_KEY\"] not found.", response.getErrorMessage());
    } // end of test step testValidJwtWithoutApiKey

    /**
     * Extension of {@link net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder} in order to directly inject the proper file bean.
     *
     * @author Jaanus
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class MyActionBeanPropertyBinder extends DefaultActionBeanPropertyBinder {
        /**
         * Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean.
         */
        public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";

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

}// end of test VocabularyFolderApiActionBeanTest
