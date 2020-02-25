package eionet.web.action;

import eionet.datadict.services.JWTService;
import eionet.web.DDActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

public class JWTActionBeanTest {
    @Mock
    JWTActionBean jwtActionBean;

    @Mock
    JWTService jwtService;

    @Mock
    DDActionBeanContext ctx;

    MockHttpServletRequest request;

    MockHttpServletResponse response;

    final static String expectedToken = "testToken";

    final String GENERATE_TOKEN_PAGE = "/pages/generateJWTToken.jsp";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        when(jwtActionBean.getJwtService()).thenReturn(jwtService);
        when(jwtActionBean.getJwtService().generateJWTToken()).thenReturn(expectedToken);
        when(jwtActionBean.generateToken()).thenCallRealMethod();
        when(jwtActionBean.getContext()).thenReturn(ctx);
        when(ctx.getRequest()).thenReturn(request);
        when(ctx.getResponse()).thenReturn(response);
    }

    /* Test case: successful generation of token */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {

        ForwardResolution resolution = (ForwardResolution) jwtActionBean.generateToken();
        Assert.assertThat(resolution, is(notNullValue()));
        Assert.assertThat(resolution.getPath(), is(GENERATE_TOKEN_PAGE));
        Assert.assertThat(resolution.getParameters().size(), is(1));

        for (Map.Entry<String, Object> entry : resolution.getParameters().entrySet()) {
            Assert.assertThat(entry.getKey(), is("generated_token"));
            for(Object obj: (Object[]) entry.getValue()){
                Assert.assertThat(obj, is("testToken"));
            }
        }
    }

}
