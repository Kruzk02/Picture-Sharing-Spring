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
public class MessageLoggingAspect {

    @Pointcut("execution(* com.app.message..*.*(..))")
    public void messageLayer() {}

    @Before("messageLayer()")
    public void logBeforeMessage(JoinPoint joinPoint) {
        log.info("Entering method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }

    @After("messageLayer()")
    public void logAfterMessage(JoinPoint joinPoint) {
        log.info("Exiting method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }
}
