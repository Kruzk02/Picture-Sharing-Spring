package com.app.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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

    @Around("messageLayer()")
    public Object logOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        log.info("Method {} called with args {} took {}ms", method, Arrays.toString(args), duration);

        return result;
    }

    @After("messageLayer()")
    public void logAfterMessage(JoinPoint joinPoint) {
        log.info("Exiting method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }
}
