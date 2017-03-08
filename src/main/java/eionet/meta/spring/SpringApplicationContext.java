package eionet.meta.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("contextAware")
public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext context;
    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        context = ac;
    }
    
    public static ApplicationContext getContext() {
        return context;
    }
    
    public static <T> T getBean(Class<T> type) {
        return getContext().getBean(type);
    }
    
}
