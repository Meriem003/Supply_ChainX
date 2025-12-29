package com.supplychainx.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class BusinessLoggingAspect {

    @Pointcut("execution(* com.supplychainx..service.*.*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);

        log.info("Executing business method: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("Business method completed successfully: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("Business method failed: {}.{} - Error: {}",
                className, methodName, e.getMessage(), e);
            throw e;
        }
    }
    @AfterReturning(
        pointcut = "execution(* com.supplychainx..service.*.create*(..))",
        returning = "result"
    )
    public void logEntityCreation(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String entityType = extractEntityType(methodName);

        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);

        String businessId = extractIdFromResult(result);
        if (businessId != null) {
            LoggingContext.setBusinessId(entityType + "_" + businessId);
        }

        log.info("BUSINESS_EVENT: {} created successfully - ID: {}", entityType, businessId);
    }
    @AfterReturning(
        pointcut = "execution(* com.supplychainx..service.*.update*(..))",
        returning = "result"
    )
    public void logEntityUpdate(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String entityType = extractEntityType(methodName);

        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);

        String businessId = extractIdFromArgs(joinPoint.getArgs());
        if (businessId != null) {
            LoggingContext.setBusinessId(entityType + "_" + businessId);
        }

        log.info("BUSINESS_EVENT: {} updated successfully - ID: {}", entityType, businessId);
    }

    @AfterReturning("execution(* com.supplychainx..service.*.delete*(..))")
    public void logEntityDeletion(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String entityType = extractEntityType(methodName);

        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);

        String businessId = extractIdFromArgs(joinPoint.getArgs());
        if (businessId != null) {
            LoggingContext.setBusinessId(entityType + "_" + businessId);
        }

        log.info("BUSINESS_EVENT: {} deleted successfully - ID: {}", entityType, businessId);
    }

    @AfterThrowing(
        pointcut = "serviceMethods()",
        throwing = "exception"
    )
    public void logBusinessException(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        LoggingContext.setLogType(LoggingContext.LogType.BUSINESS);

        log.error("BUSINESS_ERROR: Exception in {}.{} - Type: {} - Message: {}",
            className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }

    private String extractEntityType(String methodName) {
        return methodName.replaceAll("create|update|delete|get|find|search", "");
    }

    private String extractIdFromResult(Object result) {
        if (result == null) return null;

        try {
            var method = Arrays.stream(result.getClass().getMethods())
                .filter(m -> m.getName().startsWith("getId"))
                .findFirst();

            if (method.isPresent()) {
                Object id = method.get().invoke(result);
                return id != null ? id.toString() : null;
            }
        } catch (Exception e) {

        }

        return null;
    }

    private String extractIdFromArgs(Object[] args) {
        if (args.length > 0 && args[0] instanceof Long) {
            return args[0].toString();
        }
        return null;
    }
}

