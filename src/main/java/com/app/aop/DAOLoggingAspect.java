package com.app.aop;

import com.app.annotations.NoLogging;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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

    @Around("daoLayer()")
    public Object logOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        if (method.isAnnotationPresent(NoLogging.class)) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        log.info("Method {} called with args {} took {}ms", method, Arrays.toString(args), duration);

        return result;
    }
    @After("daoLayer()")
    public void logAfterDAO(JoinPoint joinPoint) {
        log.info("Exiting Method: {} in {}",
                joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getSimpleName()
        );
    }
}
