package eionet.datadict.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingAspectExpressions {

    @Pointcut("execution(* org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.update(..))")
    public void namedParameterJdbcTemplateUpdate() {}

    @Pointcut("execution(* org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.batchUpdate(..))")
    public void namedParameterJdbcTemplateBatchUpdate() {}

    @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.update(..))")
    public void jdbcTemplateUpdate() {}

    @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.batchUpdate(..))")
    public void jdbcTemplateBatchUpdate() {}

    @Pointcut("namedParameterJdbcTemplateUpdate() || jdbcTemplateBatchUpdate() || namedParameterJdbcTemplateBatchUpdate() || jdbcTemplateUpdate()")
    public void allOperationsIncluded() {}
}
