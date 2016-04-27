/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.web.DDActionBeanContext;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 * @author eworx-alk
 */
public class WelcomePageActionBeanTest {
    
    @Spy
    WelcomePageActionBean bean;
    
    @Mock
    DDActionBeanContext context;
    
    @Mock
    HttpServletRequest request;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testWelcomePage(){
        //mocking the actionBean HttpServletRequest
        when(bean.getContext()).thenReturn(context);
        when(context.getRequest()).thenReturn(request);
        
        //stubbing the methods that return the info for news and support
        when(bean.getFrontPageNews()).thenReturn("front page news");
        when(bean.getFrontPageSupport()).thenReturn("front page support");
        
        //actual method to test
        Resolution res = bean.welcome();
        
        //testing error strings setting (otherwise jsp will complain for null)
        assertNotNull(bean.errorMessage);
        assertNotNull(bean.errorTrace);
        
        //testing return resolution type
        assertEquals (res.getClass(), ForwardResolution.class);
        
        //testing actionBean properties setting
        assertEquals("Front page news was not set properly","front page news", bean.getHelps());
        assertEquals("Front page support was not set properly", "front page support", bean.getSupport());
    }
    
    
}
