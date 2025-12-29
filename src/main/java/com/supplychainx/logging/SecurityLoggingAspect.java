package com.supplychainx.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class SecurityLoggingAspect {

    @Before("execution(* com.supplychainx.security.service.AuthService.login(..))")
    public void logLoginAttempt(JoinPoint joinPoint) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            log.info("SECURITY_EVENT: Login attempt for user request");
        }
    }

    @AfterReturning("execution(* com.supplychainx.security.service.AuthService.login(..))")
    public void logLoginSuccess(JoinPoint joinPoint) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        log.info("SECURITY_EVENT: Login successful");
    }


    @AfterThrowing(
        pointcut = "execution(* com.supplychainx.security.service.AuthService.login(..))",
        throwing = "exception"
    )
    public void logLoginFailure(JoinPoint joinPoint, Exception exception) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        log.warn("SECURITY_EVENT: Login failed - Reason: {}", exception.getMessage());
    }

    @AfterReturning("execution(* com.supplychainx.security.service.AuthService.refreshAccessToken(..))")
    public void logTokenRefresh(JoinPoint joinPoint) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        log.info("SECURITY_EVENT: Token refreshed successfully");
    }

    @AfterThrowing(
        pointcut = "execution(* com.supplychainx.security.service.AuthService.refreshAccessToken(..))",
        throwing = "exception"
    )
    public void logTokenRefreshFailure(JoinPoint joinPoint, Exception exception) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        log.warn("SECURITY_EVENT: Token refresh failed - Reason: {}", exception.getMessage());
    }

    @AfterReturning("execution(* com.supplychainx.security.service.AuthService.logout(..))")
    public void logLogout(JoinPoint joinPoint) {
        LoggingContext.setLogType(LoggingContext.LogType.SECURITY);

        log.info("SECURITY_EVENT: User logged out successfully");
    }

    @AfterThrowing(
        pointcut = "execution(* com.supplychainx..*Controller.*(..))",
        throwing = "exception"
    )
    public void logAuthorizationError(JoinPoint joinPoint, Exception exception) {
        if (exception.getClass().getName().contains("AccessDenied") ||
            exception.getMessage().contains("Access is denied")) {

            LoggingContext.setLogType(LoggingContext.LogType.SECURITY);
            LoggingContext.setHttpStatus(403);

            log.warn("SECURITY_EVENT: Access denied - User attempted to access: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        }
    }
}

