package eionet.web.action.di;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Intercepts({
    LifecycleStage.ActionBeanResolution,
    LifecycleStage.HandlerResolution,
    LifecycleStage.BindingAndValidation,
    LifecycleStage.CustomValidation,
    LifecycleStage.EventHandling,
    LifecycleStage.ResolutionExecution
})
public class ActionBeanDependencyInjectionInterceptor implements Interceptor {

    public static ActionBeanDependencyInjector dependencyInjector;
    
    @Override
    public Resolution intercept(ExecutionContext ec) throws Exception {
        if (dependencyInjector == null) {
            return ec.proceed();
        }
        
        ActionBean bean = ec.getActionBean();
        
        if (dependencyInjector.accepts(bean)) {
            dependencyInjector.injectDependencies(bean);
        }

        return ec.proceed();
    }

}
