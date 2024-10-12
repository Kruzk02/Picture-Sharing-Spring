package com.app.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class DAOLoggingAspect {

    @Pointcut("execution(* com.app.DAO..*.*(..))")
    public void daoLayer() {}

    @Before("daoLayer()")
    public void logBeforeDAo(JoinPoint joinPoint) {
        log.info("Entering Method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
            );
    }

    @After("daoLayer()")
    public void logAfterDAO(JoinPoint joinPoint) {
        log.info("Exiting Method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }
}
