package eionet.web.action;

import eionet.datadict.services.data.AttributeDataService;
import eionet.meta.ActionBeanUtils;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

public class AttributesActionBeanTest extends AbstractActionBean {
    
    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final AttributeDataService attributeDataService;

        public DependencyInjector(AttributeDataService attributeDataService) {
            this.attributeDataService = attributeDataService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof AttributesActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            AttributesActionBean actionBean = (AttributesActionBean) bean;
            actionBean.setAttributeDataService(attributeDataService);
        }

    }

    @Mock
    private AttributeDataService attributeDataService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = new AttributesActionBeanTest.DependencyInjector(attributeDataService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test 
    public  void testView() throws Exception {
        MockRoundtrip trip = this.createRoundtrip();
        trip.execute("view");

        verify(attributeDataService, times(1)).getAllAttributes();
        assertEquals(AttributesActionBean.ATTRIBUTES_PAGE, trip.getForwardUrl());
    }


    private MockRoundtrip createRoundtrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, AttributesActionBean.class);
        return trip;
    }

}
