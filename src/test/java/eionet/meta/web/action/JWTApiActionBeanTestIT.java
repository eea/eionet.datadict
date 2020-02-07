package eionet.meta.web.action;


import eionet.datadict.services.JWTService;
import eionet.meta.service.DBUnitHelper;
import eionet.web.action.JWTApiActionBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

@SpringApplicationContext("mock-spring-context.xml")
public class JWTApiActionBeanTestIT extends UnitilsJUnit4 {

    @Mock
    JWTApiActionBean jwtApiActionBean;

    @Mock
    JWTService jwtService;

    final static String expectedToken = "testToken";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(jwtApiActionBean.getJwtService()).thenReturn(jwtService);
        when(jwtApiActionBean.getJwtService().generateJWTToken("1")).thenReturn(expectedToken);
        when(jwtApiActionBean.generateJWTToken()).thenCallRealMethod();
        DBUnitHelper.loadData("seed-apiKey.xml");
    }


    /* Test case: successful generation of token */
    @Test
    public void testGenerateJWTTokenSuccessful() throws Exception {
        String actualToken = jwtApiActionBean.generateJWTToken();
        Assert.assertThat(actualToken, is(expectedToken));
    }
}

