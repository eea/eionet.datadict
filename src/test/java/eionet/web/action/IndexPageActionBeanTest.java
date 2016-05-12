/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.doc.dto.DocPageDTO;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import static org.mockito.Mockito.*;

/**
 *
 * @author eworx-alk
 */
public class IndexPageActionBeanTest {

    @Spy
    IndexPageActionBean bean;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testView() throws Exception{
        // stubbing the methods that return the info for news and support
        when(bean.getFrontPageNews()).thenReturn("front page news");
        when(bean.getFrontPageSupport()).thenReturn("front page support");

        // actual method to test
        Resolution res = bean.view();
        // testing return resolution type
        assertEquals (res.getClass(), ForwardResolution.class);

        verify(bean, times(1)).getFrontPageNews();
        verify(bean, times(1)).getFrontPageSupport();
        verify(bean, times(1)).getDocumentationItems();

        // testing actionBean properties setting
        assertEquals("Front page news was not set properly","front page news", bean.getNewsSection());
        assertEquals("Front page support was not set properly", "front page support", bean.getSupportSection());
    }

}
