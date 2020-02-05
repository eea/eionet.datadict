package eionet.datadict.services.impl;

import eionet.datadict.services.JWTService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;

import static org.hamcrest.CoreMatchers.*;

@SpringApplicationContext("mock-spring-context.xml")
public class JWTServiceTest extends UnitilsJUnit4 {

    JWTService jwtService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtService = new JWTServiceImpl();
    }

    /* Test case:  */
    @Test
    public void testGenerateJWTToken() throws Exception {
        String result = jwtService.generateJWTToken("testValue");
        Assert.assertThat(result, is(nullValue()));
    }
}
