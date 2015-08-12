package eionet.meta;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang.StringUtils;

import org.springframework.web.context.ContextLoaderListener;

/**
 * Utilities for testing Action Beans.
 *
 * @author kaido
 */
public final class ActionBeanUtils {
    /** test context. */
    private static MockServletContext context;

    /** prevent initialization. */
    private ActionBeanUtils() {
    }

    /**
     * Initializes Mock servlet context.
     *
     * @return test context
     */
    public static MockServletContext getServletContext() {
        if (context == null) {
            MockServletContext ctx = new MockServletContext("test");

            Map filterParams = new HashMap();
            filterParams.put("Interceptor.Classes", getInterceptorClassesParameter());
            filterParams.put("ActionResolver.Packages", "eionet.web.action");
            filterParams.put("ActionBeanContext.Class", "eionet.web.DDActionBeanContext");

            ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
            ctx.addInitParameter("contextConfigLocation", "classpath:mock-spring-context.xml");

            ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

            context = ctx;

            ContextLoaderListener springContextLoader = new ContextLoaderListener();
            springContextLoader.contextInitialized(new ServletContextEvent(context));
        }

        return ActionBeanUtils.context;
    }
    
    private static String getInterceptorClassesParameter() {
        String[] interceptors = new String[] {
            "net.sourceforge.stripes.integration.spring.SpringInterceptor",
            "eionet.web.action.di.ActionBeanDependencyInjectionInterceptor"
        };
        
        return StringUtils.join(interceptors, ", ");
    }
    
}
