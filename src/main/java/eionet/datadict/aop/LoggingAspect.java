package eionet.datadict.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @After("eionet.datadict.aop.LoggingAspectExpressions.allOperationsIncluded()")
    public void logQueries(JoinPoint joinPoint) {
        LOGGER.info("Invocation of Aspect to log query.");
        LOGGER.info("Query run: " +  joinPoint.getArgs()[0]);
    }

}
