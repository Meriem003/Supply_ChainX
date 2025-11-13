package com.supplychainx.security;

import com.supplychainx.common.entity.User;
import com.supplychainx.common.enums.UserRole;
import com.supplychainx.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final AuthenticationService authenticationService;

    @Before("@annotation(com.supplychainx.security.RequiresAuth)")
    public void checkAuthentication(JoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();
        
        String email = request.getHeader("email");
        String password = request.getHeader("password");
        
        authenticationService.authenticate(email, password);
    }

    @Before("@annotation(com.supplychainx.security.RequiresRole)")
    public void checkAuthorization(JoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();        
        String email = request.getHeader("email");
        String password = request.getHeader("password");        
        User user = authenticationService.authenticate(email, password);        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);
        UserRole[] requiredRoles = requiresRole.value();  
        authenticationService.checkRole(user, requiredRoles);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("Impossible de récupérer la requête HTTP");
        }
        return attributes.getRequest();
    }
}
