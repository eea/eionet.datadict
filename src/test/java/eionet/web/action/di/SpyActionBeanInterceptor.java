package eionet.web.action.di;


import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

@Intercepts({
    LifecycleStage.ActionBeanResolution,
    LifecycleStage.HandlerResolution,
    LifecycleStage.BindingAndValidation,
    LifecycleStage.CustomValidation,
    LifecycleStage.EventHandling,
    LifecycleStage.ResolutionExecution
})
public class SpyActionBeanInterceptor implements Interceptor{
    
    public static SpyDependencyInjector dependencyInjector;
     
    @Override
    public Resolution intercept(ExecutionContext ec) throws Exception {
        if (dependencyInjector == null) {
            return ec.proceed();
        }
        
        ActionBean bean = ec.getActionBean();
        
        if (dependencyInjector.accepts(bean)) {
            if (ec.getLifecycleStage().equals(LifecycleStage.CustomValidation)) {
                ec.setActionBean(dependencyInjector.getSpyActionBean(bean));
            }
        }

        return ec.proceed();
    }
    
}
