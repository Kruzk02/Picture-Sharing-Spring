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
public class ServiceLoggingAspect {

    @Pointcut("execution(* com.app.Service..*.*(..))")
    public void serviceLayer(){}

    @Before("serviceLayer()")
    public void logBeforeService(JoinPoint joinPoint) {
        log.info("Entering Method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }

    @After("serviceLayer()")
    public void logAfterService(JoinPoint joinPoint) {
        log.info("Exiting Method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }
}
